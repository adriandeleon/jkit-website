---
title: jkit search
description: List the JDK, GraalVM, Maven, Gradle and sbt versions you can install, by vendor and release.
sidebar:
  order: 1
---

Lists the versions available to install. It never installs anything.

## Usage

```text
jkit search [-hV] [--fx] [--json] [--maintained] [--offline] [--pre]
            [--refresh] <tool>[@version] [<vendor>]
```

## What it does

`jkit search` asks the tool's source what exists for your OS and CPU: foojay for JDKs and GraalVM,
Maven Central for Maven and sbt, services.gradle.org for Gradle. Answers are cached in
`~/.jkit/index/` for 24 hours, so a second search is instant and works offline with `--offline`.
The table goes to stdout; the line about cache files written goes to stderr.

What it shows depends on the arguments:

| You type | You get |
| --- | --- |
| `jkit search jdk` | A table: one row per vendor, one column per feature release, `✓` where the vendor has a build |
| `jkit search jdk zulu` | The same table, with that vendor first and marked `*` |
| `jkit search jdk@21` | The newest matching build from each vendor |
| `jkit search jdk@21 zulu` | Every matching build from that vendor, newest first |
| `jkit search maven` (or `gradle`, `sbt`) | Every version, grouped by minor release, up to 8 per line |
| `jkit search maven@3.9` | Only the versions that match |

`graalvm` works like `jdk`, limited to GraalVM-based vendors. Versions you have installed are
marked `●`. `@lts` is replaced by the newest LTS release.

Each listed JDK build is looked up the way `jkit install` would (cached for 24 hours, like the
version lists), so the table only promises what install will do:

- `○` marks a build with no published checksum and no signature jkit checks. `jkit install`
  refuses it unless you pass `--allow-unverified`.
- A blank cell (or `no direct download` for `jdk@21`) means foojay lists the release but has no
  build jkit can download, such as Oracle 11.

If the lookup fails, or isn't cached with `--offline`, the build is listed as before, without a
mark. The first `jkit search jdk` of the day takes a few seconds while it looks the builds up.

The JDK table puts maintained releases first and releases that reached their end of life after
them; `‾‾` under a number marks a long-term-support (LTS) release. Your default vendor is the
first row, marked `*`.

A vendor name is checked against foojay's list first, so a typo gets a suggestion instead of an
empty result (exit code 2).

## Options

| Option | Description |
| --- | --- |
| `<tool>[@version]` | `jdk` (or `java`), `graalvm`, `maven` (or `mvn`), `gradle` or `sbt`, optionally with a version prefix, `@lts` or `@latest`. |
| `<vendor>` | A JDK vendor, such as `temurin`. JDK and GraalVM only. See [vendors](/reference/versions-and-vendors/#vendors). |
| `--fx` | JavaFX builds instead of plain ones. |
| `--maintained` | Hide JDK releases that are no longer maintained. |
| `--pre` | Include pre-releases (early-access builds, release candidates, milestones). |
| `--offline` | Never contact the network; use cached version lists only. |
| `--refresh` | Fetch version lists again instead of using the cache. |
| `--json` | Print one JSON document on stdout instead of text. |
| `-h`, `--help` | Show help and exit. |
| `-V`, `--version` | Print jkit's version and exit. |

## Examples

Which vendors publish which releases:

```console
$ jkit search jdk --maintained
                  maintained
                  27 25 21 17 11 8 7
                     ‾‾ ‾‾ ‾‾ ‾‾ ‾ ‾
temurin *          ✓  ✓  ✓  ✓  ✓ ✓
bisheng               ○  ○  ○  ○ ○
corretto           ✓  ✓  ✓  ✓  ✓ ✓
dragonwell            ○  ○  ○  ○ ○
eliya                 ✓
gluon-graalvm            ○
graalvm               ✓  ✓  ✓
graalvm-ce            ✓  ✓  ✓
jetbrains             ✓  ✓  ✓  ○
kona                  ✓  ✓  ✓  ○ ✓
liberica           ✓  ✓  ✓  ✓  ✓ ✓
liberica-nik          ✓  ✓  ✓  ✓
mandrel               ✓  ✓  ○  ○
microsoft             ✓  ✓  ✓  ✓
openjdk            ✓  ✓  ✓  ✓  ○ ○
openlogic             ○  ○  ○  ○ ○
oracle             ✓  ✓  ✓  ✓
sapmachine         ✓  ✓  ✓  ✓  ✓
semeru             ✓  ✓  ✓  ✓  ✓ ✓
semeru-certified      ✓  ✓  ✓  ✓
trava                          ○ ○
zulu               ✓  ✓  ✓  ✓  ✓ ✓ ✓

✓ available   ○ no checksum (needs --allow-unverified)   ● installed   ‾‾ LTS   * default vendor
versions per vendor: jkit search jdk@<major>
```

The newest JDK 21 from every vendor, then every build from one of them:

```console
$ jkit search jdk@21
bisheng          21.0.12+15  ○ no checksum
corretto         21.0.13.1+2
dragonwell       21.0.13.1  ○ no checksum
…
temurin *        21.0.13.1+2
zulu             21.0.13.1+2
* default vendor   ● installed   · all builds of one vendor: jkit search jdk@21 <vendor>
○ no published checksum: install refuses it unless --allow-unverified
$ jkit search jdk@21 zulu
21.0.13.1+2
21.0.13+9
21.0.13+1
21.0.12.1+1
21.0.12+8
…
```

Build tools:

```console
$ jkit search maven
3.10  3.10.2  3.10.1  3.10.0
3.9   3.9.16 ●  3.9.15  3.9.14  3.9.13  3.9.12  3.9.11  3.9.10  3.9.9  …
3.8   3.8.9  3.8.8  3.8.7  3.8.6  3.8.5  3.8.4  3.8.3  3.8.2  …
…
$ jkit search gradle@9.9 --pre
9.9   9.9.2  9.9.1  9.9.0  9.9.0-rc-1 (pre-release)  9.9.0-milestone-2 (pre-release)  9.9.0-milestone-1 (pre-release)
```

More:

```bash
jkit search graalvm                  # GraalVM-based vendors only
jkit search jdk --fx                 # JavaFX builds
jkit search jdk@27 --pre             # early-access builds
jkit search jdk@21 --offline         # from the cache, no network
jkit search jdk@21 --json            # for a program
```

## Exit codes

| Code | When |
| --- | --- |
| `0` | The list was printed (even if empty). |
| `1` | The source couldn't be reached and nothing usable was cached, or `--offline` with nothing cached for this lookup. |
| `2` | Unknown tool or vendor, a version that isn't one, or `@lts` on a build tool. |

## JSON

`jkit search … --json` prints the tool, the vendor, the feature releases and the available builds.
See [`jkit search` in JSON output](/reference/json/#jkit-search).

## See also

- [`jkit install`](/reference/commands/install/)
- [Versions and vendors](/reference/versions-and-vendors/)
- [Choosing a JDK](/guides/choosing-a-jdk/)
