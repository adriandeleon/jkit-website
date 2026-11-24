---
title: jkit list
description: Show every installed JDK, Maven, Gradle and sbt build, and which ones are global, used in this shell or pinned by the project here.
sidebar:
  order: 6
---

Shows what is installed. It never uses the network and never changes anything.

## Usage

```text
jkit list [-hV] [--json]
```

## What it does

It lists every install under `~/.jkit/tools/`: JDKs (GraalVM builds included), then Maven, Gradle
and sbt, each newest first. Each line shows the version, the vendor (with `fx` for a JavaFX
build), and notes on where it is used:

| Note | Meaning |
| --- | --- |
| `global` | `~/.jkit/global/<tool>` points at it |
| `global graalvm` | `~/.jkit/global/graalvm` points at it |
| `this shell` | this shell chose it with `jkit use` |
| `graalvm in this shell` | this shell chose it as `graalvm` |
| `jkit.toml` (or another project file name) | the project file found from here picks it |
| `graalvm in jkit.toml` | the project file picks it as `graalvm` |

With nothing installed it prints `nothing installed yet · try: jkit search jdk`.

## Options

| Option | Description |
| --- | --- |
| `--json` | Print one JSON document on stdout instead of text. |
| `-h`, `--help` | Show help and exit. |
| `-V`, `--version` | Print jkit's version and exit. |

## Examples

```console
$ jkit list
jdk     21.0.13.1+2           temurin       jkit.toml
        17.0.21.1+2           zulu          global
maven   3.9.16                              global
```

For a program, or to find an install's folder:

```bash
jkit list --json
```

## Exit codes

| Code | When |
| --- | --- |
| `0` | Always, unless jkit itself can't run (an unsafe jkit home: `1`). |

A broken `config.toml` only prints a warning.

## JSON

`--json` prints the jkit home, the global links, the project file found from here and what it
picks, and one install object per build. See [`jkit list` in JSON output](/reference/json/#jkit-list).

## See also

- [`jkit status`](/reference/commands/status/) for what is active, and why
- [`jkit remove`](/reference/commands/remove/) and [`jkit prune`](/reference/commands/prune/)
- [Files and folders](/reference/files-and-folders/)
