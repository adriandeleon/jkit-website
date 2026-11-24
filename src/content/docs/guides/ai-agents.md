---
title: AI agents
description: How coding agents such as Claude Code, Codex, Cursor, Copilot and Aider should use jkit, with instructions to paste into AGENTS.md or CLAUDE.md.
sidebar:
  order: 14
---

This page is for developers who let an AI coding agent build and test their Java projects, and
for the agents themselves. jkit is designed to be driven by programs: nothing prompts, `--json`
gives one machine-readable document, exit codes are stable, and every change is reported. The one
thing an agent must get right is to run builds through `jkit exec`.

## Why `jkit exec`

Agents such as Claude Code, Codex, Cursor's agent, GitHub Copilot's agent mode and Aider run each
shell command in a fresh shell. A `jkit use jdk@17` (without `--global`) changes only the shell
it runs in, through the shell hook, so the next command has forgotten it. The same goes for
`eval "$(jkit env)"`.

`jkit exec` sets the environment and runs the command in one go:

```bash
jkit exec -- mvn test                        # the project's versions (jkit.toml, .sdkmanrc, …), else global
jkit exec jdk@17 -- java -version            # newest installed JDK 17 from the default vendor
jkit exec jdk@21 zulu maven@3.9 -- mvn -v    # several tools; a vendor follows its tool
jkit exec --dir ../other-app -- ./gradlew build
jkit exec --no-project -- java -version      # ignore project files
```

It sets `JAVA_HOME` (and `MAVEN_HOME`, `GRADLE_HOME`, `SBT_HOME`, `GRAALVM_HOME` as chosen), puts
those tools' `bin` folders first on `PATH`, runs the command and exits with the command's exit
code. It never uses the network and never installs anything. `--` is required; everything after
it belongs to the command.

## What an agent can rely on

- **Nothing asks a question.** No jkit command prompts or reads stdin. The download progress bar
  is off when jkit isn't attached to a terminal.
- **`--json`** on `list`, `status`, `env`, `doctor`, `search`, `install`, `upgrade`, `use`, `pin`,
  `unpin`, `sync` and `remove` prints exactly one JSON document on stdout, also on failure, with
  an `error.hints` list of commands to try. See the [JSON reference](/reference/json/).
- **Exit codes** are stable: `0` ok, `1` failure, `2` bad arguments, `3` the version isn't
  installed. `jkit exec` passes the command's own exit code through (`126`: jkit couldn't start
  it, `127`: not found on the `PATH` it gives the command).
- **Errors are readable.** jkit's own errors go to stderr as an `error: …` line followed by
  indented hints, each with the command to run:

  ```console
  $ jkit exec jdk@99 -- java -version
  error: no installed JDK matches 99 from temurin (default vendor)
    install it:          jkit install jdk@99
  ```

- **Every change is reported**: folders created or deleted, links set, variables changed
  (`changes` in JSON). `list`, `status`, `env`, `doctor` and `exec` change nothing in jkit's home.

Because `exec` passes the command's exit code through, a `3` from `jkit exec` could in theory come
from the command. jkit's own "not installed" error always prints the `error:` line on stderr.

## A typical loop

```bash
jkit status --json            # what is active here, and what the project file wants
jkit install jdk@21           # only if needed; reports what it created
jkit exec -- mvn -q verify    # build with the project's versions
```

To find versions: `jkit search jdk@21 --json` (online, cached 24 h; `--offline` uses the cache
only). To check the setup when something looks wrong: `jkit doctor --json`. For a project with a
`jkit.toml`, `jkit sync` installs everything it pins in one step.

## Instructions to give the agent

Paste this into the project's `AGENTS.md`, `CLAUDE.md` or equivalent:

```markdown
## Java toolchain (jkit)

This machine manages JDKs, Maven, Gradle and sbt with jkit. Each of your shell commands starts
a new shell, so don't use `jkit use` (without `--global`) or `eval "$(jkit env)"` to switch
versions; prefix commands with `jkit exec` instead:

- Build with the project's versions: `jkit exec -- mvn verify` (or `./gradlew build`, `sbt test`).
- A specific version: `jkit exec jdk@17 -- java -version`; vendor after the tool: `jkit exec jdk@21 zulu -- …`.
- What is active and what the project wants: `jkit status --json`. Installed builds: `jkit list --json`.
- Exit code 3 means the version isn't installed; install it with `jkit install jdk@17` (downloads)
  only if the user is fine with that.
- Don't change the global default (`jkit use … --global`) or remove installs unless asked. Note
  that the first install of a tool becomes its default; add `--no-default` to avoid that.
- jkit refuses downloads with no published checksum, or whose publisher's signature it can't
  check. Don't add `--allow-unverified` unless the user says so.
- On failure, read the `error:` line and the hints under it on stderr (`--json`: `error.hints`).
```

Where each tool looks for it:

| Agent | File |
| --- | --- |
| Claude Code | `CLAUDE.md` in the project (or `~/.claude/CLAUDE.md` for every project) |
| Codex, Cursor, and other tools that follow the AGENTS.md convention | `AGENTS.md` |
| GitHub Copilot | `.github/copilot-instructions.md` (its coding agent also reads `AGENTS.md`) |
| Aider | a conventions file you load with `--read`, such as `CONVENTIONS.md` |

Check your agent's own documentation if it isn't listed; any file it reads as standing
instructions works.

## Stopping a command

While `jkit exec` runs a command, jkit ignores SIGINT: a terminal's Ctrl-C reaches the command
directly, so a REPL keeps running and its exit code comes through. To stop it from another
process (a timeout, an agent's cancel button), send jkit SIGTERM (or SIGHUP): it passes SIGTERM
on, waits for the command, and exits with the command's exit code. A second SIGTERM kills the
command. A SIGINT sent to jkit's process alone does nothing.

## Permissions to give an agent

Most agents let you allow some commands and ask before others. A reasonable split:

| Allow without asking | Ask first |
| --- | --- |
| `jkit status`, `jkit list`, `jkit env`, `jkit doctor`, `jkit search` | `jkit install`, `jkit sync`, `jkit upgrade` (download and run new programs) |
| | `jkit use … --global`, `jkit reset … --global` (change every new shell and IDE) |
| | `jkit remove`, `jkit prune` (delete installs) |
| | `jkit pin`, `jkit unpin` (edit `jkit.toml`, which the whole team shares) |
| | `jkit config set`, `jkit hook --install` (change your settings and startup files) |
| | anything with `--allow-unverified` |

:::caution[`jkit exec` runs whatever follows `--`]
Allowing `jkit exec` without restriction allows every command, since `jkit exec -- rm -rf …` is
also `jkit exec`. If your agent supports it, allow specific forms such as `jkit exec -- mvn` or
`jkit exec -- ./gradlew` instead.
:::

Never let an agent add `--allow-unverified` on its own: it installs a download that has no
checksum, or whose signature jkit couldn't check. And keep the global default yours: an agent that needs JDK 17 for
one command should use `jkit exec jdk@17 -- …`, not `jkit use jdk@17 --global`.

## Giving the agent the docs

These docs are published as plain text for language models: see [Docs for LLMs](/llms/) for
`llms.txt` and `llms-full.txt`, and how to point an assistant at them.
