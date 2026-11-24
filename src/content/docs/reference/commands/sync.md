---
title: jkit sync
description: Install the versions a project file pins and switch the current shell to them.
sidebar:
  order: 12
---

Installs the project file's pins and switches this shell to them. Run it after cloning a project,
or after someone changes its `jkit.toml`.

## Usage

```text
jkit sync [-hV] [--allow-unverified] [--dry-run] [--json] [--offline]
          [--refresh]
```

## What it does

1. **Finds the project file** from this folder upwards: `jkit.toml` (or `.jkit.toml`), else
   `.sdkmanrc`, `.tool-versions` and `.java-version`. See
   [which file applies](/reference/project-files/#which-file-applies). Warnings about entries it
   skipped go to stderr.
2. **For each pinned tool**, uses a matching install if there is one (your default vendor's first,
   when the pin names none). Otherwise it downloads and installs the newest matching build, from
   the pin's vendor or else your default vendor, exactly as [`jkit install`](/reference/commands/install/)
   would. A pin to an exact pre-release installs it without `--pre`.
3. **Switches this shell** to the pinned versions through the [shell hook](/reference/shell-hook/):
   each tool's `*_HOME` and `PATH`, recorded in `JKIT_PROJECT_<TOOL>`. A tool you chose in this
   shell with `jkit use` keeps winning until `jkit reset <tool>`, and jkit says so. A tool an
   earlier sync applied that the file no longer pins goes back to the global version.

Without the hook it only installs, and says the shell is unchanged; then use
[`jkit exec`](/reference/commands/exec/), or `eval "$(jkit env)"`.

`jkit sync` never changes the global defaults, and installs from it never become the default:
unlike `jkit install`, the first install of a tool by `sync` doesn't set its global link.

A download with no published checksum, or whose publisher's signature can't be checked, is
refused unless you pass `--allow-unverified`, so cloning a repository can't make jkit install
something unchecked. If one pin fails, what the earlier pins
installed stays installed, and the error says so.

With auto-switch on, the `cd` hook switches to installed pins by itself; `jkit sync` is still what
installs missing ones.

## Options

| Option | Description |
| --- | --- |
| `--dry-run` | Only list what would be installed; install nothing and don't change the shell. |
| `--allow-unverified` | Install pins even when a download has no published checksum, or its publisher's signature can't be checked. |
| `--offline` | Never contact the network; use cached version lists only. Missing pins then fail. |
| `--refresh` | Fetch version lists again instead of using the cache. |
| `--json` | Print one JSON document on stdout instead of text. |
| `-h`, `--help` | Show help and exit. |
| `-V`, `--version` | Print jkit's version and exit. |

## Examples

With the hook loaded:

```console
$ jkit sync
syncing ~/src/app (jkit.toml)
jdk 21: 21.0.13.1+2 temurin (installed)
  set    JKIT_PROJECT=~/src/app/jkit.toml:1795537120159
  set    JKIT_PROJECT_JDK=~/.jkit/tools/jdk/21.0.13.1+2-temurin
  set    JAVA_HOME=~/.jkit/tools/jdk/21.0.13.1+2-temurin
  PATH   + ~/.jkit/tools/jdk/21.0.13.1+2-temurin/bin
  to switch to a project's versions on cd: add auto_switch = true under [shell] in ~/.jkit/config.toml, then open a new shell
```

Each pin gets one line: `(installed)` when it was already there, `installed …` when sync
downloaded it, and in a dry run `would install …` or `would not install …: no published checksum`.

See what it would do first:

```bash
jkit sync --dry-run
```

In CI, without the hook:

```bash
jkit sync
jkit exec -- mvn -B verify
```

## Exit codes

| Code | When |
| --- | --- |
| `0` | Everything pinned is installed (and, with the hook, applied). |
| `1` | No project file here or above, a broken or ignored project file, a download that failed or was refused, or `--offline` with a pin to download. |
| `2` | Bad options. |

## JSON

`--json` prints the project, one row per pinned tool with its `status` (`already-installed`,
`installed`, `would-install` or `would-refuse`), `changes` and `notes`. See
[`jkit sync` in JSON output](/reference/json/#jkit-sync).

## See also

- [`jkit pin`](/reference/commands/pin/), [`jkit status`](/reference/commands/status/)
- [Project files](/reference/project-files/)
- [Projects](/guides/projects/), [CI](/guides/ci/)
