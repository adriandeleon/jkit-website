---
title: jkit install
description: Download, check and install a JDK, GraalVM, Maven, Gradle or sbt version, making the first one of each tool the default.
sidebar:
  order: 2
---

Resolves a version, downloads it, checks it, and installs it under `~/.jkit/tools/`. It never
prompts.

## Usage

```text
jkit install [-hV] [--allow-unverified] [--fx] [--json] [--no-default]
             [--offline] [--pre] [--refresh] <tool>[@version] [<vendor>]
```

## What it does

1. **Resolves the version.** It asks the tool's source for the newest build that matches, from the
   vendor you typed or else your default vendor (`jdk.vendor`, Temurin unless you changed it). It
   never picks another vendor: if yours has no matching build, it fails and lists the vendors that
   do. `@lts` becomes the newest long-term-support release. See
   [how a version resolves](/reference/versions-and-vendors/#how-a-version-resolves).
2. **Stops if it's already installed**, saying so (exit code 0).
3. **Downloads it** over HTTPS, with a progress bar when attached to a terminal.
4. **Checks it** against the checksum its source publishes and, for Temurin, Microsoft, Corretto,
   Maven 3.9.0+ and sbt, the publisher's OpenPGP signature. A download with no checksum is
   refused unless you pass `--allow-unverified` (Corretto's signature stands in for its missing
   checksum). See [Security](/reference/security/).
5. **Unpacks and checks the archive** in `~/.jkit/tmp/`, then moves it into
   `~/.jkit/tools/<tool>/<version>[-<vendor>]` in one step.
6. **Makes it the default** if it's the first install of that tool: it points
   `~/.jkit/global/<tool>` at it, as `jkit use … --global` would. `--no-default` skips this. Later
   installs never change the default; jkit prints the command that would.

Every change is printed: the folder created, the global link, the cache files written, and what
the signature check found. Pre-releases are left out unless you pass `--pre` or name an exact
pre-release version (`maven@4.0.0-rc-4`).

The first `jkit install graalvm` sets the default `graalvm`, not the default `jdk`. Installing a
GraalVM-based JDK doesn't count as the first `jdk` install either.

If two jkit processes install at once, the second waits for the first.

## Options

| Option | Description |
| --- | --- |
| `<tool>[@version]` | `jdk`, `graalvm`, `maven`, `gradle` or `sbt`; `@21`, `@21.0.5`, `@lts` or `@latest` (the default). |
| `<vendor>` | A JDK vendor, such as `temurin`, `zulu`, `corretto` (see `jkit search jdk`). JDK and GraalVM only. |
| `--allow-unverified` | Install even when the source publishes no checksum for the download (or, for Corretto, when its signature can't be checked). |
| `--fx` | A JavaFX build. |
| `--no-default` | Don't make it the default, even when it's the first of its tool. |
| `--pre` | Allow pre-releases. |
| `--offline` | Never contact the network; use cached version lists only. An install still needs to download, so this fails unless the version is already installed. |
| `--refresh` | Fetch version lists again instead of using the cache. |
| `--json` | Print one JSON document on stdout instead of text. |
| `-h`, `--help` | Show help and exit. |
| `-V`, `--version` | Print jkit's version and exit. |

## Examples

The first JDK becomes the default:

```console
$ jkit install jdk@21
downloading jdk 21.0.13.1+2 temurin from github.com
installed jdk 21.0.13.1+2 temurin → ~/.jkit/tools/jdk/21.0.13.1+2-temurin
  folder ~/.jkit/tools/jdk/21.0.13.1+2-temurin created
  link   ~/.jkit/global/jdk → ~/.jkit/tools/jdk/21.0.13.1+2-temurin
  cache  ~/.jkit/index: 2 files saved from api.foojay.io
  signature checked: signed by Adoptium (key 843C48A565F8F04B)
  new shells with the jkit hook: JAVA_HOME=~/.jkit/global/jdk, PATH includes ~/.jkit/global/jdk/bin
  it's the default jdk now (the first one installed); to clear it: jkit reset jdk --global
```

A second JDK doesn't change the default:

```console
$ jkit install jdk@17 zulu
downloading jdk 17.0.21.1+2 zulu from cdn.azul.com
installed jdk 17.0.21.1+2 zulu → ~/.jkit/tools/jdk/17.0.21.1+2-zulu
  folder ~/.jkit/tools/jdk/17.0.21.1+2-zulu created
  cache  ~/.jkit/index: 2 files saved from api.foojay.io
make it the default: jkit use jdk@17 zulu --global
```

Build tools:

```console
$ jkit install maven@3.9
downloading maven 3.9.16 from repo.maven.apache.org
installed maven 3.9.16 → ~/.jkit/tools/maven/3.9.16
  folder ~/.jkit/tools/maven/3.9.16 created
  link   ~/.jkit/global/maven → ~/.jkit/tools/maven/3.9.16
  cache  ~/.jkit/index: 1 file saved from repo.maven.apache.org
  signature checked: signed by Apache Maven (Slawomir Jaranowski, key 9EB80E92EB2135B1)
  new shells with the jkit hook: MAVEN_HOME=~/.jkit/global/maven, PATH includes ~/.jkit/global/maven/bin
  it's the default maven now (the first one installed); to clear it: jkit reset maven --global
```

More:

```bash
jkit install jdk                     # newest JDK from your default vendor
jkit install jdk@lts zulu            # newest LTS release from Zulu
jkit install jdk@21.0.5 corretto     # newest 21.0.5 build from Corretto
jkit install jdk@21 zulu --fx        # with JavaFX
jkit install graalvm@25              # GraalVM Community 25, for GRAALVM_HOME
jkit install gradle@8.14
jkit install sbt
jkit install jdk@21 --no-default     # in CI: don't touch the global default
```

When the vendor has no matching build:

```console
$ jkit install jdk@8 microsoft
error: microsoft has no build matching jdk@8 for linux-x64
  available from: bisheng, corretto, dragonwell, kona, liberica, openjdk, openlogic, semeru, temurin, trava, zulu
```

## Exit codes

| Code | When |
| --- | --- |
| `0` | Installed, or already installed. |
| `1` | No matching build, a download or check failed, a download with no checksum without `--allow-unverified`, or `--offline` when it needs to download. |
| `2` | Unknown tool or vendor, a version that isn't one, a vendor on Maven, Gradle or sbt, or a non-GraalVM vendor for `graalvm`. |

## JSON

`--json` prints `status` (`installed` or `already-installed`), `verified`, `signedBy`, the
install object, `changes` and `notes`. See
[`jkit install` in JSON output](/reference/json/#jkit-install).

## See also

- [`jkit search`](/reference/commands/search/) to see what exists
- [`jkit use`](/reference/commands/use/) to switch, [`jkit upgrade`](/reference/commands/upgrade/)
  to update
- [`jkit pin`](/reference/commands/pin/) and [`jkit sync`](/reference/commands/sync/) for projects
- [Security](/reference/security/) for what is checked
