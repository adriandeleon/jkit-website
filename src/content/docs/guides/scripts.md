---
title: Scripts and Makefiles
description: Use jkit from shell scripts and Makefiles with jkit exec and jkit env, check its exit codes, and read its JSON output with jq.
sidebar:
  order: 15
---

This page is for anyone who writes shell scripts, Makefiles or other automation around a Java
build. Scripts don't load the shell hook, so `jkit use` (without `--global`) doesn't apply in
them. Use one of two commands instead:

- **`jkit exec -- <command>`** runs one command with the right versions.
- **`eval "$(jkit env)"`** sets `JAVA_HOME`, the other `*_HOME` variables and `PATH` for the rest
  of the script.

Both start from the global versions and apply the project file (`jkit.toml`, `.sdkmanrc`,
`.tool-versions` or `.java-version`) found from the current folder. Neither uses the network.

## `jkit exec` in a script

```bash title="release.sh"
#!/usr/bin/env bash
set -euo pipefail

jkit exec -- mvn -B -q verify               # the project's versions
jkit exec jdk@17 -- mvn -B -q verify        # the same build on JDK 17
jkit exec --dir ../web -- ./gradlew build   # another project, with its own pins
jkit exec --no-project -- java -version     # global versions only
```

Tools named before `--` override the project's: `jkit exec jdk@21 zulu maven@3.9 -- mvn -v`. A
vendor goes right after its tool.

`jkit exec` exits with the command's exit code, so `set -e` works as usual. Its own errors use
these codes:

| Code | When |
| --- | --- |
| `2` | Bad arguments: no `--`, nothing after it, an unknown tool or vendor |
| `3` | A version you named, or the project pins, isn't installed |
| `126` | The command was found but couldn't be started |
| `127` | The command isn't on the `PATH` jkit gives it |

## `jkit env` in a script

```bash
#!/usr/bin/env bash
set -euo pipefail
eval "$(jkit env)"              # this folder's project versions, else the global ones
java -version
mvn -B verify
```

When `jkit env` fails it prints `false` on stdout, so the `eval` fails too and `set -e` stops the
script there.

`jkit env` prints `export` lines for bash and zsh. For other shells, ask for their syntax:

```bash
jkit env --shell fish | source                       # fish
jkit env --shell pwsh | Out-String | Invoke-Expression   # PowerShell
```

`--dir <path>` reads the project file from another folder, and `--no-project` ignores project
files. A pin that isn't installed makes `jkit env` exit `3` and print only `false` on stdout
(`throw '…'` for PowerShell), with the `jkit install` command to run on stderr.

## Makefiles

```make title="Makefile"
JKIT := jkit exec --
MVN  := $(JKIT) mvn -B

.PHONY: setup build test17

setup:
	jkit sync

build:
	$(MVN) verify

test17:
	jkit exec jdk@17 -- mvn -B test
```

Each recipe line runs in its own shell, just as an agent's commands do, so `jkit exec` is the
reliable choice. `make setup` installs what the project pins.

## Checking before you run

```bash
# is jkit there at all?
command -v jkit >/dev/null || { echo "install jkit: https://jkit-project.dev/start/install/" >&2; exit 1; }

# are the project's pins installed? (exit 3 when one isn't)
if ! jkit env >/dev/null 2>&1; then
  jkit sync
fi
```

`jkit sync --dry-run` lists what `jkit sync` would install, without installing it.

## Reading JSON with jq

With `--json`, a command prints exactly one JSON document on stdout, also when it fails; messages
and progress go to stderr. Every document has `"schemaVersion": 1`. New fields can appear, so
read the ones you need and ignore the rest. The [JSON reference](/reference/json/) has every
shape.

**Which JDK will a build here use?**

```bash
jkit env --json | jq -r '.variables.JAVA_HOME // empty'
```

**What does the project pin, and which install does it pick?**

```bash
jkit status --json | jq -r '.tools[] | select(.pin != null) | "\(.tool) \(.pin.version) -> \(.wanted.home)"'
```

**Is this shell using what jkit would pick?**

```bash
jkit status --json | jq -r '.tools[] | select(.matches == false) | .hint'
```

**Every installed JDK, with its folder:**

```bash
jkit list --json | jq -r '.installed[] | select(.tool == "jdk") | "\(.id)\t\(.home)"'
```

**Is a JDK 17 installed?**

```bash
if jkit list --json | jq -e '.installed[] | select(.tool == "jdk" and .major == 17)' >/dev/null; then
  echo "have 17"
fi
```

**What would `jkit sync` do?**

```bash
jkit sync --dry-run --json | jq -r '.tools[] | "\(.tool) \(.status)"'
# jdk already-installed
# maven would-install
```

**Did `jkit doctor` find a problem?**

```bash
jkit doctor --json | jq -r '.checks[] | select(.level == "fail" or .level == "warn") | "\(.id): \(.message)"'
```

Filter on `id` and `level`; the messages are for people and may change.

**Why did it fail?**

```bash
out=$(jkit use jdk@99 --global --json 2>/dev/null) || {
  echo "$out" | jq -r '.error.message, (.error.hints[] | "  " + .)'
  exit "$(echo "$out" | jq -r '.error.exitCode')"
}
```

The error document's `exitCode` is the same code the process exits with.

## Tips

- Run scripts from the project folder, or pass `--dir`, so the right project file applies.
- Use `jkit exec` for one-off commands and `jkit env` when many commands need the same
  environment.
- Don't call `jkit use` without `--global` in a script; it needs the hook and only affects the
  shell it runs in.
- `JKIT_PROJECT_FILE=/path/to/jkit.toml` makes a script use that project file wherever it runs;
  set but empty, it turns project files off.
