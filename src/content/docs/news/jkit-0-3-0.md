---
title: jkit 0.3.0, the first public release
description: jkit is out. One native command to install and switch JDKs from 20+ vendors, Maven, Gradle and sbt, per shell, per project or globally.
date: 2026-11-24T12:00:00Z
authors: adrian
tags: [release]
version: "0.3.0"
excerpt: jkit is out. One native command to install and switch JDKs from 20+ vendors, Maven, Gradle and sbt, per shell, per project or globally.
---

jkit 0.3.0 is out, and it's the first release meant for everyone.

jkit installs and switches JDKs from more than 20 vendors, GraalVM, Maven, Gradle and sbt. It is
one native binary: it doesn't need a JDK, bash or a plugin system to run, and it starts in
milliseconds on Linux, macOS and (as a preview) Windows.

```bash
brew install adriandeleon/tap/jkit
jkit hook --install
# open a new shell, then:
jkit install jdk@21
java -version
```

## Why another version manager

Java developers already have good tools, so jkit tries to be better at a few specific things:

- **It says what it changed.** Every variable, `PATH` entry, link, file and folder a command
  touches is printed under its message. Even switching JDKs on `cd` says so.
- **Versions read like versions.** `jdk@21`, `jdk@lts zulu`, `maven@3.9`: a prefix and an optional
  vendor, not an identifier to look up first.
- **Three clear scopes.** This shell (`jkit use`), the project (`jkit.toml`) and the global
  default (`jkit use --global`), with one documented order between them. `jkit status` says which
  one won, and why.
- **Downloads are checked.** HTTPS only, and a download without a published checksum is refused
  unless you say otherwise.
- **Built for CI and agents.** No command ever prompts. `jkit exec -- mvn verify` runs one command
  with the project's versions, `--json` prints one machine-readable document, and exit codes are
  stable.
- **It works with what you have.** jkit reads `.sdkmanrc`, `.tool-versions` and `.java-version`,
  so a team can try it one person at a time.

The [comparison](/why/overview/) goes through how this differs from a manual install, SDKMAN,
mise, asdf and OS packages, and when one of those is the better fit.

## What's in it

Project files with `jkit pin` and `jkit sync`, switching on `cd` in bash, zsh and fish,
`jkit status`, `jkit doctor` and `jkit env` for when something looks wrong, `jkit exec` for
scripts and agents, and packages for Homebrew, apt, winget, `.deb` and `.rpm`. The full list is
in [What's new](/whats-new/0.3/#030).

## What's not there yet

Windows is a preview: PowerShell doesn't switch on `cd` yet, and you add the hook to your profile
by hand. There's no native fish or zsh completion yet, and Windows on ARM runs the x64 build under
emulation.

Bugs and ideas are welcome in the [issue tracker](https://github.com/adriandeleon/jkit/issues).
