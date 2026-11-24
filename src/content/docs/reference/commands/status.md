---
title: jkit status
description: Show which JDK, Maven, Gradle, sbt and GraalVM version is active in this shell, where it comes from, and what to run when it isn't what jkit would pick.
sidebar:
  order: 7
---

Shows which version of each tool is active here, and why. It never uses the network and never
changes anything.

## Usage

```text
jkit status [-hV] [--json]
```

## What it does

For each tool (`jdk`, `maven`, `gradle`, `sbt`, `graalvm`), it compares two things:

- **What this shell has:** the tool's `*_HOME` variable (`JAVA_HOME`, …).
- **What jkit would pick here:** this shell's `jkit use` choice, else the project file's pin, else
  the global default. See [which version wins](/reference/project-files/#which-version-wins).

It prints one line per tool: the version, where it comes from, and a detail. When the shell
doesn't have what jkit would pick, an indented second line says why and which command fixes it.

| Source | Meaning |
| --- | --- |
| `shell` | Chosen in this shell with `jkit use` |
| `project` | Applied from the project file (by `jkit sync`, `jkit pin` or the `cd` hook) |
| `global` | The global default, `~/.jkit/global/<tool>` |
| `outside` | A `*_HOME` jkit didn't set, such as a `JAVA_HOME` exported in `~/.bashrc` |
| `stale` | A jkit install that nothing chooses any more (set earlier, then changed) |
| `not set` | No `*_HOME` for this tool |

Tools that are neither active nor installed are left out. A tool that is installed but chosen
nowhere gets a hint with the `jkit use … --global` command for it. For an `outside` JDK, the
version comes from its `release` file.

Under the tools, `jkit status` names the project file it found (`project: ~/src/app (jkit.toml)`)
and its warnings, or says why it ignored one (another user's folder, a world-writable file), or
shows the error if the file is broken.

## Options

| Option | Description |
| --- | --- |
| `--json` | Print one JSON document on stdout instead of text. |
| `-h`, `--help` | Show help and exit. |
| `-V`, `--version` | Print jkit's version and exit. |

## Examples

In a project, with the hook loaded but `jkit sync` not run yet:

```console
$ jkit status
jdk      17.0.21.1+2 zulu        global
         jkit.toml wants jdk 21.0.13.1+2 temurin; this shell has global: jkit sync
maven    3.9.16                  global
project: ~/src/app (jkit.toml)
```

Without the hook:

```console
$ jkit status
jdk      -                       not set
         the global jdk isn't applied: the jkit hook isn't loaded: jkit hook --install, then open a new shell
maven    -                       not set
         the global maven isn't applied: the jkit hook isn't loaded: jkit hook --install, then open a new shell
```

A `JAVA_HOME` set outside jkit:

```console
$ jkit status
jdk      17.0.2                  outside   JAVA_HOME=/usr/lib/jvm/java-17
         JAVA_HOME is set outside jkit and wins over the global jdk; remove it from your shell profile to use jkit's
```

A `jkit use` that overrides the project:

```console
$ jkit status
jdk      17.0.21.1+2 zulu        shell
         jkit.toml wants jdk 21; this shell chose another with jkit use, to follow it: jkit reset jdk
```

## Hints you may see

| Hint | What to do |
| --- | --- |
| `<file> wants <tool> <version>; this shell has …: jkit sync` | Run `jkit sync` (or enable auto-switch). |
| `<file> wants <tool> <version>, not installed: jkit sync` | `jkit sync` installs it. |
| `… isn't applied: the jkit hook isn't loaded` | `jkit hook --install`, then open a new shell. |
| `… isn't applied in this shell yet: open a new shell` | The hook is loaded, but this shell started before the default was set. |
| `… is set outside jkit and wins over the global …` | Remove the export from your shell profile. |
| `set by jkit earlier but no longer chosen` | Open a new shell, or `jkit reset <tool>`. |
| `global <tool> → …, which no longer exists` | `jkit use <tool>@<version> --global`. |
| `N installed, none chosen: jkit use … --global` | Run that command to make one the default. |

## Exit codes

| Code | When |
| --- | --- |
| `0` | Always, unless jkit itself can't run (an unsafe jkit home: `1`). A broken project file or `config.toml` is reported, not an error. |

## JSON

`--json` prints a `tools` array with each tool's `source`, what jkit would pick (`wanted`), its
pin and global link, whether they `match`, and the `hint`. See
[`jkit status` in JSON output](/reference/json/#jkit-status).

## See also

- [`jkit doctor`](/reference/commands/doctor/) for problems with the setup itself
- [`jkit env`](/reference/commands/env/) for what a new process would get
- [Troubleshooting](/help/troubleshooting/)
