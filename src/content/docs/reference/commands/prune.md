---
title: jkit prune
description: Free disk space by removing superseded builds, leftovers of interrupted installs, and cached version lists older than 30 days.
sidebar:
  order: 15
---

Removes what jkit no longer needs, to free disk space. Run it after
[`jkit upgrade`](/reference/commands/upgrade/), or with `--dry-run` to see what it would remove.

## Usage

```text
jkit prune [-hnV] [--json]
```

## What it does

It removes three kinds of things:

1. **Superseded builds:** older builds in a release line once a newer stable one is installed
   (21.0.5 once 21.0.9 is in). A release line is a tool, its major version, and for JDKs the vendor
   and whether it's a JavaFX build, as `jkit upgrade` groups them. It keeps a build that:
   - a global link points at,
   - this shell chose with `jkit use`, or applied from a project file,
   - the project file here pins.

   Pre-releases are never pruned.
2. **Leftovers** of interrupted installs and removals: folders in `~/.jkit/tmp/`, and
   `.<name>.partial` and `.remove-*` folders next to installs.
3. **Old cache:** cached version lists older than 30 days. Newer ones stay, so `--offline` keeps
   working.

It only knows about the project file in the current folder. A project elsewhere that pins an
exact old build gets it back with `jkit sync`.

Removing builds works like [`jkit remove`](/reference/commands/remove/), with the same locks, and
prints each one. At the end it says how much space it freed. It never uses the network.

## Options

| Option | Description |
| --- | --- |
| `-n`, `--dry-run` | Show what would be removed and how much space it frees; remove nothing. |
| `--json` | Print one JSON document on stdout instead of text. |
| `-h`, `--help` | Show help and exit. |
| `-V`, `--version` | Print jkit's version and exit. |

## Examples

```console
$ jkit prune --dry-run
would remove maven 3.9.15 (10 MB; 3.9.16 is installed)
would free 10 MB; run without --dry-run to remove
$ jkit prune
removed maven 3.9.15
  folder ~/.jkit/tools/maven/3.9.15 deleted
freed 10 MB
```

Nothing to do:

```console
$ jkit prune
nothing to prune
```

After an upgrade:

```bash
jkit upgrade && jkit prune
```

## Exit codes

| Code | When |
| --- | --- |
| `0` | Pruned, or nothing to prune. A folder it couldn't delete is a warning, not a failure. |
| `1` | A build couldn't be removed, or jkit can't run at all (an unsafe jkit home). |

## JSON

`--json` prints the `builds` removed (each with the `newer` build that replaces it and its size),
`leftovers`, `cache`, the total `bytes`, `changes` and `notes`. See
[`jkit prune` in JSON output](/reference/json/#jkit-prune).

## See also

- [`jkit upgrade`](/reference/commands/upgrade/), [`jkit remove`](/reference/commands/remove/)
- [Files and folders](/reference/files-and-folders/)
- [Keeping up to date](/guides/keeping-up-to-date/)
