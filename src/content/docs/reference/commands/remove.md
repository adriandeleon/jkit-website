---
title: jkit remove
description: Remove installed JDK, GraalVM, Maven, Gradle or sbt builds, refusing ones that are global or used in this shell unless forced.
sidebar:
  order: 14
---

Removes installed versions. It refuses anything that is global or chosen in this shell unless you
add `--force`.

## Usage

```text
jkit remove [-hV] [--all] [--force] [--fx] [--json] <tool>@<version>
            [<vendor>]
```

## What it does

1. **Finds the installs that match**, as [`jkit use`](/reference/commands/use/) does: the version
   prefix, from the vendor you typed or else your default vendor. Plain builds are matched before
   JavaFX builds unless you pass `--fx`. It never uses the network.
2. **If more than one matches**, it stops and lists them, unless you pass `--all`.
3. **Checks every match first**, so a refusal means nothing was removed. It refuses a build that:
   - a global link points at (`~/.jkit/global/<tool>`), or
   - this shell chose with `jkit use`.

   `--force` removes it anyway, along with its global link.
4. **Removes each one**: it moves the folder out of `tools/` in one step, deletes it, and removes
   any global link that pointed at it.

It warns, but doesn't refuse, when the project file here pins the build (`jkit sync` installs it
again) or when this shell still points at it from a project. A build in use elsewhere (another
shell, another project) isn't detected; that shell keeps a `JAVA_HOME` that no longer exists until
you `jkit reset` or `jkit sync` there.

If an install or another removal is running, it waits for it.

## Options

| Option | Description |
| --- | --- |
| `<tool>@<version>` | `jdk`, `graalvm`, `maven`, `gradle` or `sbt`, with a version prefix or `@lts`. |
| `<vendor>` | A JDK vendor. Default: your default vendor. |
| `--all` | Remove every match. |
| `--force` | Remove even if global or active in this shell; the global link goes too. |
| `--fx` | Remove JavaFX builds. Without it, plain builds are removed first. |
| `--json` | Print one JSON document on stdout instead of text. |
| `-h`, `--help` | Show help and exit. |
| `-V`, `--version` | Print jkit's version and exit. |

## Examples

```console
$ jkit remove jdk@17 zulu
removed jdk 17.0.21.1+2 zulu
  folder ~/.jkit/tools/jdk/17.0.21.1+2-zulu deleted
```

When it's the global default:

```console
$ jkit remove jdk@17 zulu
error: jdk 17.0.21.1+2 zulu is global; nothing removed
  make another version the default first: jkit use jdk@<version> --global
  or clear the default: jkit reset jdk --global
  or remove it anyway (the link goes too): add --force
```

When several builds match:

```console
$ jkit remove maven@3.9
error: maven@3.9 matches 2 installed builds
  3.9.16   (global)
  3.9.15
  remove one:  jkit remove maven@3.9.15
  remove all:  jkit remove maven@3.9 --all --force
```

More:

```bash
jkit remove jdk@17.0.12 temurin
jkit remove jdk@17 --all          # every installed 17 from the default vendor
jkit remove jdk@21 zulu --fx      # the JavaFX build
jkit prune                        # every build a newer one in its line replaces
```

## Exit codes

| Code | When |
| --- | --- |
| `0` | Removed. |
| `1` | Refused (global or used in this shell, without `--force`), several matches without `--all`, or the folder couldn't be moved (permissions; on Windows, a file held open). |
| `2` | Unknown tool, a version that isn't one, a vendor on Maven, Gradle or sbt, or a non-GraalVM vendor for `graalvm`. |
| `3` | No installed build matches. |

If removing several builds fails partway, the error lists what was already removed.

## JSON

`--json` prints the removed installs as they were before removal, and `changes`. See
[`jkit remove` in JSON output](/reference/json/#jkit-remove).

## See also

- [`jkit prune`](/reference/commands/prune/) to remove superseded builds in one go
- [`jkit list`](/reference/commands/list/)
- [Uninstall](/guides/uninstall/) to remove jkit itself
