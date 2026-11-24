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
release's `SHA256SUMS` and that the new binary runs. A jkit from Homebrew, apt, dnf or winget tells
you the command for that package manager instead. [More](/reference/commands/self-update/)

## Signature checks

Checksums catch a download that was damaged or changed on the way. Signatures also tie it to
its publisher. jkit now checks Temurin, Microsoft and Corretto JDKs, Maven 3.9.0 and later, and
sbt against their publisher's OpenPGP signature, using keys built into jkit, and stops if a
signature doesn't match:

```console
$ jkit install maven@3.9
downloading maven 3.9.16 from repo.maven.apache.org
installed maven 3.9.16 → ~/.jkit/tools/maven/3.9.16
  signature checked: signed by Apache Maven (Slawomir Jaranowski, key 9EB80E92EB2135B1)
```

Corretto publishes no checksum, so its signature is now the check, and Corretto installs no longer
need `--allow-unverified`. [Security](/reference/security/)

All changes are in [What's new](/whats-new/#040).
