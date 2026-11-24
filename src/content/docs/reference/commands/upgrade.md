---
title: jkit upgrade
description: Install the newest build of each installed major version of your JDKs, Maven, Gradle and sbt, moving global defaults along.
sidebar:
  order: 3
---

Installs the newest build of each major version you have installed: JDK 21.0.5 to 21.0.9, Maven
3.9.11 to 3.10.2. It never moves you to a new major version, and it keeps the old builds.

## Usage

```text
jkit upgrade [-hnV] [--allow-unverified] [--fx] [--json] [--offline]
             [--pre] [--refresh] [[tool[@major]]] [<vendor>]
```

## What it does

`jkit upgrade` groups your installs into **release lines**: a tool, its major version, and for
JDKs the vendor and whether it's a JavaFX build. So `jdk 21 temurin`, `jdk 21 zulu`, `jdk 17
temurin` and `maven 3` are four lines. For each line it:

1. Looks up the newest build from the same vendor with the same major version (online, cached 24
   hours, like [`jkit search`](/reference/commands/search/)).
2. If that is newer than your newest install in the line, downloads and checks it exactly as
   [`jkit install`](/reference/commands/install/) does.
3. Moves a global link that pointed at the line's previous newest build to the new one. A global
   link that points at another build stays where it is.

Things it doesn't do:

- **Cross a major version.** JDK 21 stays 21; to move to 25, `jkit install jdk@25`. For Maven,
  Gradle and sbt the major is the first number, so Maven 3.9.x can move to 3.10.x.
- **Remove old builds.** They stay installed; `jkit remove` them, or
  [`jkit prune`](/reference/commands/prune/) to remove every superseded one at once.
- **Change this shell.** If this shell chose the old build with `jkit use`, jkit says so; run
  `jkit use` again to switch.
- **Change a project file.** A pin like `jdk = "21"` picks the new build by itself; an exact pin
  keeps its build.
- **Touch pre-releases**, unless you pass `--pre`: an installed early-access build has no stable
  build to move to.

A line that fails (a download error, a missing checksum) is reported and the others still run;
the exit code is then 1.

## Options

| Option | Description |
| --- | --- |
| `[tool[@major]]` | Only this tool, and with `@major` only that major version. Default: everything. `jdk@lts` means the newest installed LTS release. |
| `<vendor>` | Only that vendor's JDK lines. |
| `-n`, `--dry-run` | Show what would be upgraded; install nothing. |
| `--allow-unverified` | Install upgrades even when the source publishes no checksum for the download. |
| `--fx` | Treat JDKs as JavaFX builds. Only needed for installs made before jkit recorded JavaFX. |
| `--pre` | Allow pre-releases. |
| `--offline` | Never contact the network; use cached version lists only. Useful with `--dry-run`. |
| `--refresh` | Fetch version lists again instead of using the cache. |
| `--json` | Print one JSON document on stdout instead of text. |
| `-h`, `--help` | Show help and exit. |
| `-V`, `--version` | Print jkit's version and exit. |

## Examples

See what would change:

```console
$ jkit upgrade --dry-run
jdk@21 temurin is up to date (21.0.13.1+2)
jdk@17 zulu is up to date (17.0.21.1+2)
maven@3: 3.9.16 → 3.10.2
run without --dry-run to install
```

Upgrade one tool:

```console
$ jkit upgrade maven
downloading maven 3.10.2 from repo.maven.apache.org
upgraded maven@3: 3.9.16 → 3.10.2
  folder ~/.jkit/tools/maven/3.10.2 created
  link   ~/.jkit/global/maven → ~/.jkit/tools/maven/3.10.2
  signature checked: signed by Apache Maven (Tamas Cservenak, key 3A387D43964143E3)
  the old build stays: jkit remove maven@3.9.16
```

More:

```bash
jkit upgrade                  # every line
jkit upgrade jdk              # JDKs only
jkit upgrade jdk@21           # only the 21 lines
jkit upgrade jdk@21 zulu      # only Zulu 21
jkit upgrade && jkit prune    # upgrade, then remove the superseded builds
```

With nothing installed, it says `nothing installed yet · try: jkit search jdk`.

## Exit codes

| Code | When |
| --- | --- |
| `0` | Every line is up to date, was upgraded, or (dry run) can be. |
| `1` | At least one line failed; the others still ran. |
| `2` | Unknown tool, a version that isn't one, or a vendor on Maven, Gradle or sbt. |
| `3` | `jdk@lts` with no LTS release installed. |

## JSON

`--json` prints one row per release line with its `from`, `to` and `status` (`up-to-date`,
`available`, `upgraded` or `failed`). See
[`jkit upgrade` in JSON output](/reference/json/#jkit-upgrade).

## See also

- [`jkit prune`](/reference/commands/prune/) to remove the old builds
- [`jkit install`](/reference/commands/install/)
- [Keeping up to date](/guides/keeping-up-to-date/)
