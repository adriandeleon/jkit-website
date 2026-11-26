---
title: jkit exec
description: Run one command with the JDK, Maven, Gradle and sbt versions jkit picks, without changing any shell; for scripts, CI and AI agents.
sidebar:
  order: 9
---

Runs one command with the versions jkit picks. No shell is changed. It's the way to use jkit
where each command starts in a new shell: scripts, CI jobs and AI agents.

## Usage

```text
jkit exec [-hV] [--no-project] [--dir=<path>] [[<tool>[@version]
          [vendor]]... -- <command> [args]...]
```

## What it does

1. **Works out the environment** as [`jkit env`](/reference/commands/env/) does: the global
   versions, overridden by the project file found from the folder.
2. **Applies the tools you name before `--`** on top: `jkit exec jdk@17 zulu maven@3.9 -- …`. Each
   picks the newest installed match from the vendor you typed or your default vendor, as
   [`jkit use`](/reference/commands/use/) does. A vendor follows its tool.
3. **Runs the command** in that environment: each chosen tool's `*_HOME` is set and its `bin`
   folder put first on `PATH`. The command is looked up on that `PATH`, so `java` is the chosen
   JDK's.
4. **Exits with the command's exit code.**

`--` is required, and everything after it belongs to the command: `jkit exec -- mvn --help` shows
Maven's help.

In jkit 0.4.0, PowerShell with the hook loaded drops the `--` before jkit sees it, and jkit fails
with `Unknown option`. Call `jkit.exe exec … -- …` there, which skips the hook's `jkit` function;
it's fixed for the next release.

It never uses the network and never installs anything. A version that isn't installed (named
before `--`, or pinned by the project) exits with code 3 and the `jkit install` command to run. A
pin for a tool you name before `--` doesn't need to be installed, since your choice replaces it.

The command's environment is this process's environment with those changes. jkit also removes its
own shell bookkeeping (`JKIT_SHELL_*`, `JKIT_PROJECT*`, `JKIT_ENV_FILE`) so a `jkit` run inside the
command can't change the calling shell, and unsets a `*_HOME` that points at a jkit install no
longer chosen. A `jkit use` made in the calling shell doesn't apply inside.

### Exit codes and signals

- The command's own exit code comes through. A command ended by a signal gives 128 plus the
  signal number (`143` for SIGTERM).
- `127`: the command isn't on the `PATH` jkit gives it. `126`: it was found but couldn't start.
- While the command runs, jkit ignores SIGINT: a terminal's Ctrl-C reaches the command directly,
  so a REPL keeps running and its exit code comes through.
- To stop it from another process (a timeout, an agent's cancel), send jkit SIGTERM (or SIGHUP):
  it passes SIGTERM on, waits for the command and exits with its exit code; a second SIGTERM
  kills the command. A SIGINT sent to jkit's process alone does nothing.

jkit's own errors go to stderr as `error: …` followed by indented hints; the command's output is
untouched.

## Options

| Option | Description |
| --- | --- |
| `<tool>[@version] [vendor]` | Zero or more tools to use, before `--`. `jdk@21 zulu`, `maven@3.9`, `gradle`, `graalvm@25`. A bare tool name means its newest installed version (from your default vendor, for a JDK). |
| `-- <command> [args]` | The command to run, and its arguments. Required. |
| `--dir=<path>` | Run in this folder, and look for a project file from it. |
| `--no-project` | Ignore project files; start from the global versions only. |
| `-h`, `--help` | Show help and exit. |
| `-V`, `--version` | Print jkit's version and exit. |

`jkit exec` has no `--json` option: its stdout belongs to the command. It has no `--fx`: name the
JavaFX build's vendor and version, or pin it with `fx = true`.

After a JDK with no vendor, the word `graalvm` is read as the vendor (Oracle GraalVM), as in
`jkit exec jdk@21 graalvm -- …`; `graalvm@21` names the tool.

## Examples

```bash
jkit exec -- mvn test                        # the project's versions (jkit.toml, .sdkmanrc, …), else global
jkit exec jdk@17 -- java -version            # newest installed JDK 17 from the default vendor
jkit exec jdk@21 zulu maven@3.9 -- mvn -v    # several tools; a vendor follows its tool
jkit exec --dir ../other-app -- ./gradlew build
jkit exec --no-project -- java -version      # ignore project files
```

```console
$ jkit exec jdk@17 zulu -- java -version
openjdk version "17.0.21.1" 2026-11-17 LTS
OpenJDK Runtime Environment Zulu17.70+201-CA (build 17.0.21.1+2-LTS)
OpenJDK 64-Bit Server VM Zulu17.70+201-CA (build 17.0.21.1+2-LTS, mixed mode, sharing)
```

Errors:

```console
$ jkit exec jdk@11 -- java -version
error: no installed JDK matches 11 from temurin (default vendor)
  install it:          jkit install jdk@11
$ jkit exec -- nosuchcmd
error: command not found: nosuchcmd
  it isn't on the PATH jkit gives the command: see jkit env
$ jkit exec jdk@21
error: put -- before the command to run
  e.g. jkit exec jdk@21 -- java -version
```

A typical agent or CI loop:

```bash
jkit status --json            # what is active here, and what the project file wants
jkit sync                     # install what it pins, if needed
jkit exec -- mvn -q verify    # build with the project's versions
```

## Exit codes

| Code | When |
| --- | --- |
| The command's | The command ran. |
| `1` | A jkit error before the command started (a broken project file, an unsafe jkit home). |
| `2` | No `--`, nothing after it, or a word before it that is neither a tool nor a vendor. |
| `3` | A version named before `--`, or pinned by the project, isn't installed. |
| `126` | The command couldn't be started. |
| `127` | The command wasn't found. |

See [Exit codes](/reference/exit-codes/#jkit-exec).

## See also

- [`jkit env`](/reference/commands/env/)
- [AI agents](/guides/ai-agents/), [Scripts](/guides/scripts/), [CI](/guides/ci/)
- [`jkit use`](/reference/commands/use/) for an interactive shell
