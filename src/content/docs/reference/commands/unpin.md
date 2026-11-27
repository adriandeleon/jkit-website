---
title: jkit unpin
description: Remove a tool from the project's jkit.toml, so it follows the shell or global choice again.
sidebar:
  order: 11
---

Removes a tool's line from the project's `jkit.toml`. The tool then follows the shell or global
choice again.

## Usage

```text
jkit unpin [-hV] [--here] [--json] <tool>
```

## What it does

It finds the same file [`jkit pin`](/reference/commands/pin/) would edit (the `jkit.toml` here or
above, `--here` for this folder's, or `JKIT_PROJECT_FILE`), and removes the tool's line, keeping
everything else, the file's line endings included. A line jkit skipped when reading (a version it can't use) is removed too.

With the shell hook loaded, it then applies the project again, so this shell's version of that
tool goes back to its `jkit use` choice or the global default. Without the hook it says the shell
is unchanged.

It never uninstalls anything and never deletes the file, even when the last tool is gone. It
refuses `.sdkmanrc`, `.tool-versions` and `.java-version` files (edit those by hand) and a
`jkit.toml` that is a symbolic link. With no `jkit.toml` here or above, it fails.

## Options

| Option | Description |
| --- | --- |
| `<tool>` | `jdk` (or `java`), `graalvm`, `maven` (or `mvn`), `gradle` or `sbt`. No version. |
| `--here` | Use `jkit.toml` in this folder, even if a project file is further up. |
| `--json` | Print one JSON document on stdout instead of text. |
| `-h`, `--help` | Show help and exit. |
| `-V`, `--version` | Print jkit's version and exit. |

## Examples

```console
$ jkit unpin maven
unpinned maven 3.9 from ~/src/app/jkit.toml
  file   ~/src/app/jkit.toml updated
  the jkit shell hook isn't active, so this shell is unchanged; run one command with these versions: jkit exec -- <command>, or eval "$(jkit env)"
$ jkit unpin maven
~/src/app/jkit.toml doesn't pin maven
```

## Exit codes

| Code | When |
| --- | --- |
| `0` | Removed, or the file didn't pin the tool. |
| `1` | No `jkit.toml` here or above, a format jkit doesn't write, or a symbolic link. |
| `2` | Unknown tool, or a missing tool name. |

## JSON

`--json` prints the file, whether the line was `removed`, the `previous` pin, `changes` and
`notes`. See [`jkit unpin` in JSON output](/reference/json/#jkit-unpin).

## See also

- [`jkit pin`](/reference/commands/pin/)
- [Project files](/reference/project-files/)
