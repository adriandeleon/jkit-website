---
title: jkit pin
description: Record a JDK, GraalVM, Maven, Gradle or sbt version in the project's jkit.toml, creating the file if needed.
sidebar:
  order: 10
---

Records a version in the project's `jkit.toml`, creating the file if needed. Only that tool's line
changes.

## Usage

```text
jkit pin [-hV] [--exact] [--fx] [--here] [--json] [--offline]
         [--refresh] <tool>[@version] [<vendor>]
```

## What it does

1. **Finds the file to edit:** the `jkit.toml` (or `.jkit.toml`) found here or in a folder above,
   or with `--here` the one in this folder. With no project file at all, it creates `jkit.toml`
   here. If `JKIT_PROJECT_FILE` is set, it edits (or creates) that file.
2. **Checks the version exists.** It must match an install or something the tool's source offers,
   so a typo fails now and not on a teammate's machine. That check may go online (cached 24 hours).
3. **Writes the line**, keeping the file's comments, order and other tools. A new file starts with
   a comment and a `[tools]` table. The line is the short form (`jdk = "21"`), or the long form
   (`jdk = { version = "21", vendor = "zulu", fx = true }`) when the pin has a vendor or JavaFX.
4. **Applies it to this shell** if the shell hook is loaded, as `jkit sync` would for installed
   versions. Without the hook it says the shell is unchanged.

What gets written:

| You type | Written |
| --- | --- |
| `jkit pin jdk@21` | `jdk = "21"`: any vendor, newest installed 21 |
| `jkit pin jdk@21 zulu` | `jdk = { version = "21", vendor = "zulu" }` |
| `jkit pin jdk@21 zulu --exact` | `jdk = { version = "21.0.13.1+2", vendor = "zulu" }`: the build it resolves to |
| `jkit pin jdk` | The feature release of the JDK this shell uses: `jdk = "21"` |
| `jkit pin jdk --exact` | That JDK's full version and vendor |
| `jkit pin maven` | The Maven this shell uses, in full: `maven = "3.9.16"` |
| `jkit pin jdk@latest` | `jdk = "latest"`: always the newest |
| `jkit pin jdk@lts` | The current LTS release as a number: `jdk = "25"` |

Without a version, `jkit pin` pins what this shell uses (its `*_HOME`, or else what jkit would
pick here); for `jdk` and `graalvm` that is the feature release, for the build tools the full
version. `--exact` pins the exact build an install or download resolves to, with its vendor.

`jkit pin` installs nothing; run [`jkit sync`](/reference/commands/sync/) to install what the
file pins.

### What it refuses

- **Other formats.** If the project file here is a `.sdkmanrc`, `.tool-versions` or `.java-version`,
  jkit reads it but won't write it. It suggests `--here`, which creates a `jkit.toml` that then wins
  over them.
- **A symbolic link.** It won't write through a `jkit.toml` that is a link.
- **Hand-written layouts.** It only changes plain `tool = …` lines under a `[tools]` header. A file
  that sets its tools another way (an inline table, dotted keys, a value over several lines) has to
  be edited by hand.
- **`JKIT_PROJECT_FILE` set but empty**, which turns project files off; use `--here`.
- **`--fx` on Maven, Gradle or sbt**, which have no JavaFX builds.

## Options

| Option | Description |
| --- | --- |
| `<tool>[@version]` | `jdk`, `graalvm`, `maven`, `gradle` or `sbt`, with a version prefix, `@lts` or `@latest`. Without a version: what this shell uses. |
| `<vendor>` | Require this JDK vendor. Without it the pin accepts any vendor. |
| `--exact` | Write the full version and vendor of the build it resolves to. |
| `--fx` | Require a JavaFX build (`fx = true`). |
| `--here` | Use `jkit.toml` in this folder, even if a project file is further up. |
| `--offline` | Never contact the network; use cached version lists only. |
| `--refresh` | Fetch version lists again instead of using the cache. |
| `--json` | Print one JSON document on stdout instead of text. |
| `-h`, `--help` | Show help and exit. |
| `-V`, `--version` | Print jkit's version and exit. |

## Examples

```console
$ jkit pin jdk@21
pinned jdk 21 in ~/src/app/jkit.toml
  file   ~/src/app/jkit.toml created
  the jkit shell hook isn't active, so this shell is unchanged; run one command with these versions: jkit exec -- <command>, or eval "$(jkit env)"
$ jkit pin maven@3.9
pinned maven 3.9 in ~/src/app/jkit.toml
  file   ~/src/app/jkit.toml updated
$ cat jkit.toml
# Versions jkit uses in this project: jkit sync installs them.
[tools]
jdk = "21"
maven = "3.9"
```

An exact build, for reproducible builds:

```console
$ jkit pin jdk@17 zulu --exact
pinned jdk 17.0.21.1+2 zulu in ~/src/app/jkit.toml (was jdk 21)
  file   ~/src/app/jkit.toml updated
$ grep jdk jkit.toml
jdk = { version = "17.0.21.1+2", vendor = "zulu" }
```

More:

```bash
jkit pin jdk                     # what this shell uses, as a feature release
jkit pin gradle@8.14.3
jkit pin graalvm@25 graalvm-ce
jkit pin jdk@21 zulu --fx
jkit pin jdk@21 --here           # a jkit.toml in this folder, not the one above
```

## Exit codes

| Code | When |
| --- | --- |
| `0` | Pinned, or the file already pinned exactly that. |
| `1` | No such version, no tool active to pin, a format jkit doesn't write, a symbolic link, a layout it can't edit safely. |
| `2` | Unknown tool or vendor, a version that isn't one, or a vendor on Maven, Gradle or sbt. |

## JSON

`--json` prints the file, whether it was `created`, the new `pin` and the `previous` one, the
install it picks, `changes` and `notes`. See [`jkit pin` in JSON output](/reference/json/#jkit-pin).

## See also

- [`jkit unpin`](/reference/commands/unpin/), [`jkit sync`](/reference/commands/sync/)
- [Project files](/reference/project-files/)
- [Projects](/guides/projects/), [Teams](/guides/teams/)
