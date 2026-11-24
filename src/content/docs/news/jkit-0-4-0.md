---
title: "jkit 0.4.0: config, prune, self-update and signature checks"
description: jkit 0.4.0 adds jkit config, jkit prune and jkit self-update, and checks Temurin, Microsoft, Corretto, Maven and sbt downloads against their publishers' signatures.
date: 2026-12-01T12:00:00Z
draft: true
authors: adrian
tags: [release]
excerpt: Settings you can see and change, disk space back, jkit updating itself, and OpenPGP signature checks.
---

jkit 0.4.0 is out. Update with `jkit self-update` (from 0.3.0, use your package manager or
download the new release; `self-update` arrives in this version).

## See and change your settings: `jkit config`

```console
$ jkit config
jdk.vendor         temurin     default
graalvm.vendor     graalvm-ce  default
shell.auto_switch  false       default
$ jkit config set jdk.vendor zulu
```

Each setting shows where its value comes from: an environment variable, `~/.jkit/config.toml`, or
the default. `set` checks the value and keeps the file's comments. [More](/reference/commands/config/)

## Get disk space back: `jkit prune`

JDKs are a few hundred megabytes each, and `jkit upgrade` keeps the old build so nothing breaks
under you. `jkit prune` removes builds that a newer one in the same release line has replaced,
unless they're global, used in this shell or pinned in the project you're in. It also clears
folders left by interrupted installs and old cached version lists. Run `jkit prune --dry-run`
first to see what would go. [More](/reference/commands/prune/)

## Update jkit itself: `jkit self-update`

A jkit installed from a release archive replaces itself, after checking the download against the
release's `SHA256SUMS` and that the new binary runs. `SHA256SUMS` sits next to the archive, so
whoever could replace one could replace both; jkit first checks `SHA256SUMS.asc`, its signature by
jkit's release key, which is built into jkit, and refuses a release without a good one. A jkit
from Homebrew, apt, dnf or winget tells you the command for that package manager instead.
[More](/reference/commands/self-update/)

## Signature checks

Checksums catch a download that was damaged or changed on the way. Signatures also tie it to
its publisher. jkit now checks Temurin, Microsoft and Corretto JDKs, Maven 3.9.0 and later, and
sbt against their publisher's OpenPGP signature, using keys built into jkit. A signature that
doesn't match stops the install:

```console
$ jkit install maven@3.9
downloading maven 3.9.16 from repo.maven.apache.org
installed maven 3.9.16 → ~/.jkit/tools/maven/3.9.16
  signature checked: signed by Apache Maven (Slawomir Jaranowski, key 9EB80E92EB2135B1)
```

A signature jkit can't fetch, read, or tie to the publisher's keys stops it too: the checksum
usually comes from the same server as the download, and a missing signature is exactly what
someone who replaced both would serve. `--allow-unverified` installs it anyway, on the checksum
alone, marked unverified. You're most likely to meet this with Maven: each release is signed by
whichever Apache release manager cut it, so a release by someone new is refused until a jkit
release adds their key. `jkit self-update` brings it.

Corretto publishes no checksum, so its signature is now the check, and Corretto installs no longer
need `--allow-unverified`. [Security](/reference/security/)

## `eval "$(jkit env)"` stops a script

When `jkit env` fails (a pinned version that isn't installed, say), it now also prints `false` on
stdout (`throw '…'` for PowerShell). `eval "$(jkit env)"` then fails too, so a `set -e` script or
a CI step stops there instead of running with half an environment.
[More](/reference/commands/env/)

All changes are in [What's new](/whats-new/#040).
