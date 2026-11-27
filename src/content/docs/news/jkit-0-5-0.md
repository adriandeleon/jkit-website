---
title: "jkit 0.5.0: Alpine, env --install on Windows, and stricter download checks"
description: jkit 0.5.0 adds a musl build for Alpine and jkit env --install on Windows, pins more vendors' downloads to their own servers, and marks vendor builds that stopped getting updates.
date: 2026-11-26T23:00:00Z
authors: adrian
tags: [release]
version: "0.5.0"
excerpt: A build for Alpine, global versions for every Windows program, more vendors pinned to their own servers, and stale builds marked.
---

jkit 0.5.0 is out. Update with `jkit self-update`, or with your package manager.

## Alpine and other musl systems

Each release now has a static Linux x64 binary linked against musl,
`jkit-<version>-linux-x64-musl.tar.gz`. It needs nothing from the system, so it runs on Alpine and
on glibc distributions older than the 2.34 the other Linux builds need. `jkit self-update` on a
musl system picks it. There's no arm64 musl build yet: GraalVM's native-image can't make one.
[Install](/start/install/)

## Windows: `jkit env --install`

The PowerShell hook only changes PowerShell. `jkit env --install` sets `JAVA_HOME` and the other
`*_HOME` variables in your user environment to jkit's global links, and puts their `bin` folders
first on your user `PATH`, so cmd.exe, IDEs and programs started from Explorer follow
`jkit use … --global` too. Each change is printed. A `JAVA_HOME` of your own that it replaces is
kept, and `jkit env --uninstall` puts it back and removes only what jkit set.
[Windows](/guides/windows/)

## Stricter download checks

- **foojay can't turn one vendor into another.** A vendor synonym in foojay's answer could make
  `jkit install jdk@21` install a build labelled as another vendor, skipping Temurin's signature
  check. Temurin, Microsoft and Corretto are now known without asking foojay, and a synonym two
  vendors claim is ignored.
- **More vendors must download from their own server**: Zulu, Liberica, SapMachine, Semeru,
  Oracle, GraalVM CE, Oracle GraalVM and Mandrel, as Temurin, Microsoft and Corretto already did.
  Their checksums come from foojay, so before, a compromised foojay could choose both the file and
  its checksum.
- **Temurin and Microsoft JDKs are always checked against the vendor's signature**, whether or not
  foojay lists one, and a signed build must be the version you asked for.
- **The `.deb` and `.rpm` are signed.** `self-update` for a package installed from a file checks
  it against the release's signed `SHA256SUMS` before handing it to `dnf` or `apt`, which don't
  check a package given as a file.

[Security](/reference/security/)

## Builds a vendor stopped updating

openjdk (jdk.java.net) and GraalVM CE publish only the first updates of each release, so
`jkit install jdk@21 openjdk` got 21.0.2 from January 2024 without a word. `jkit search` now marks
such builds, and `jkit install` warns:

```console
$ jkit search jdk@21
…
graalvm-ce       21.0.2  ↓ behind: newest 21 is 21.0.13.1+5
…
openjdk          21.0.2+13  ↓ behind: newest 21 is 21.0.13.1+5
…
$ jkit install jdk@21 openjdk
warning: openjdk stopped updating 21 at 21.0.2+13; the newest 21 is 21.0.13.1+5 (corretto, dragonwell, kona, …): jkit install jdk@21
```

`--json` has a `behind` field. [More](/reference/commands/search/)

## `jkit upgrade` stays within a Maven or Gradle minor

`jkit upgrade maven` now goes from 3.9.9 to 3.9.11, not to 3.10.2, as its help said. JDK, GraalVM
and sbt lines are still the major version, and `jkit prune` treats builds as superseded the same
way. [More](/reference/commands/upgrade/)

## Shell hooks

- **PATH order.** The hook puts the global versions first on PATH even when PATH already has them
  further back (on Windows, where the user PATH comes after the system one, and after macOS's
  `path_helper` in a nested login zsh). A `JAVA_HOME` you set yourself now decides which `java`
  comes first, and isn't lost when a project or `jkit use` overrides it: leaving the project or
  `jkit reset` gives it back. PATH entries that move are reported.
- **zsh**: Tab completion loads under `setopt nounset`, the hook no longer turns on
  `COMPLETE_ALIASES`, and prompt themes' `$( )` subshells don't show jkit messages.
- **fish**: the PATH order is put back once more at the first prompt, after `config.fish`.
- **PowerShell**: `use`, `reset`, `pin`, `sync` and `exec` work with the hook loaded, also under
  Windows PowerShell's default execution policy; paths with accents stay intact.

## Project files

`.tool-versions` reads more asdf and mise Java ids (Corretto, Semeru, Mandrel, JavaFX builds), and
`.java-version` reads jenv's names such as `temurin64-21.0.5`. With several project files, the
first one that mentions a tool decides it. `jdk = 21` as a TOML number in `jkit.toml` now pins JDK
21, and `jkit sync` fails when a file pins something it can't read. [More](/reference/project-files/)

## `--json`

`jkit reset` takes `--json`; `search` and `install` add `behind`, `upgrade` adds `release`,
`use … --global` adds `changed`, `prune` lists builds a running program uses as `inUse`, and PATH
changes can be a `move`. [More](/reference/json/)

All changes are in [What's new](/whats-new/0.5/#050).
