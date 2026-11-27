---
title: jkit env
description: Print the environment variables (JAVA_HOME, PATH and the rest) a new process in this folder should get, for CI, IDEs, Docker and scripts without the shell hook.
sidebar:
  order: 8
---

Prints the environment for a new process here: the global versions, overridden by the project
file. It never uses the network and, except for `--install` and `--uninstall` on Windows, never
changes anything.

## Usage

```text
jkit env [-hV] [--json] [--no-project] [--dir=<path>] [--shell=<shell>]
jkit env --install [--json]      # Windows
jkit env --uninstall [--json]    # Windows
```

## What it does

It works out what a fresh process in this folder should get:

1. The global default of each tool (`~/.jkit/global/<tool>`).
2. Overridden, per tool, by the project file found from this folder (or from `--dir`).

Choices made in this shell with `jkit use` don't count, since the output is for a new process.
Tools with neither a global default nor a pin are left out: their variables aren't set, and not
unset either.

It prints shell code that sets each chosen tool's `*_HOME` variable and a `PATH` with those tools'
`bin` folders first. Older jkit install folders are taken out of `PATH`; everything else in it
stays in order. `GRAALVM_HOME` follows a GraalVM-based JDK when no `graalvm` is chosen.

Use it where there is no shell hook:

```bash
eval "$(jkit env)"                 # bash, zsh, sh: CI steps, Docker, scripts
jkit env --shell fish | source     # fish
jkit env --json                    # an IDE or tool: variables and PATH entries
```

To switch the current interactive shell to the project's versions, use
[`jkit sync`](/reference/commands/sync/) instead; to run one command, [`jkit exec`](/reference/commands/exec/).

### Errors on purpose

A project pin with no matching install is an error (exit code 3), with the command that installs
it, so a CI step never runs with half an environment. Add `--no-project` to ignore the project
file. A global link whose install is gone is skipped with a warning.

On any failure jkit env also prints `false` on stdout (`throw '…'` for `pwsh` and `powershell`),
so `eval "$(jkit env)"` fails too and a `set -e` script stops there. With `--json` the error is in
the JSON document instead.

With nothing to set (no project file, no global versions), it prints nothing on stdout and says so
on stderr.

### The output syntax

Without `--shell`, jkit writes for the shell that started it, when that is bash, zsh, fish, pwsh or
powershell (sh, dash and ksh get bash syntax); otherwise for the hook's shell (`JKIT_SHELL`), else
bash.

| `--shell` | Output |
| --- | --- |
| `bash`, `zsh` | `export JAVA_HOME='…'` |
| `fish` | `set -gx JAVA_HOME '…'` |
| `pwsh`, `powershell` | `$env:JAVA_HOME = '…'` |

Values are single-quoted, so paths with spaces are safe to `eval`.

### Windows: the user environment

On Windows, `jkit env --install` (run once) sets `JAVA_HOME` and the other `*_HOME` variables in
your user environment to jkit's global links, and puts their `bin` folders first on your user
`PATH`. Then cmd.exe, IDEs and programs started from Explorer get the global versions too, not
only a hooked PowerShell, and a later `jkit use … --global` reaches new programs with no further
change. Each change is printed, a value it replaces included; the rest of `PATH` stays exactly as
it was, `%VAR%`s and all. Programs already running, this shell included, keep what they have.

A `*_HOME` of your own that it replaces is kept in `JKIT_SAVED_<TOOL>` (`JKIT_SAVED_JDK`, …); a
value it replaces that is one of jkit's own installs isn't kept. `jkit env --uninstall` removes only
what `--install` set and puts your saved value back, unless that folder no longer exists, which it
says.

Windows puts the system `PATH` before the user `PATH`, so a `java` in the system `PATH` still comes
first; [`jkit doctor`](/reference/commands/doctor/) says whether `--install` is set up and warns
about such a `java`. On macOS and Linux, `--install` and `--uninstall` are an error: the shell's
startup file is the place, with [`jkit hook --install`](/reference/commands/hook/).

## Options

| Option | Description |
| --- | --- |
| `--shell=<shell>` | Syntax: `bash` (default), `zsh`, `fish`, `pwsh` or `powershell` (Windows PowerShell 5.1). |
| `--dir=<path>` | The folder to look for a project file from (default: here). |
| `--no-project` | Ignore project files; global versions only. |
| `--install` | Windows: put the global versions in your user environment. |
| `--uninstall` | Windows: take out what `--install` put there. |
| `--json` | Print one JSON document on stdout instead of shell code. |
| `-h`, `--help` | Show help and exit. |
| `-V`, `--version` | Print jkit's version and exit. |

## Examples

In a project that pins JDK 21, with a global Maven:

```console
$ jkit env
export JAVA_HOME='/home/me/.jkit/tools/jdk/21.0.13.1+2-temurin'
export MAVEN_HOME='/home/me/.jkit/global/maven'
export PATH='/home/me/.jkit/tools/jdk/21.0.13.1+2-temurin/bin:/home/me/.jkit/global/maven/bin:/usr/local/bin:/usr/bin:/bin'
```

In a GitHub Actions step:

```bash
jkit sync                              # install what jkit.toml pins
eval "$(jkit env)"
echo "JAVA_HOME=$JAVA_HOME" >> "$GITHUB_ENV"
```

For another folder, or ignoring the project:

```bash
jkit env --dir ../other-app
jkit env --no-project
```

A pin that isn't installed:

```console
$ jkit env
false
error: jkit.toml wants jdk 25, not installed
  install it: jkit install jdk@25
  or ignore the project file: jkit env --no-project
```

## Exit codes

| Code | When |
| --- | --- |
| `0` | Printed (or nothing to set). |
| `1` | The project file is broken, or `--dir` isn't a folder; `--install` or `--uninstall` on macOS or Linux. |
| `2` | Unknown `--shell`. |
| `3` | The project file pins a version that isn't installed. |

## JSON

`--json` prints `variables`, `pathPrepend` (the folders to put first on `PATH`), the whole `path`
and the project file. With `--install` or `--uninstall` it prints the changes made to the user
environment instead. See [`jkit env` in JSON output](/reference/json/#jkit-env).

## See also

- [`jkit exec`](/reference/commands/exec/) to run one command with these versions
- [`jkit sync`](/reference/commands/sync/) to switch this shell
- [CI](/guides/ci/), [Docker](/guides/docker/), [IDEs](/guides/ides/)
