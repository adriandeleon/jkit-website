---
title: jkit vs SDKMAN
description: How jkit compares with SDKMAN, with a command-by-command translation and the differences that matter.
sidebar:
  order: 3
  label: vs SDKMAN
---

[SDKMAN](https://sdkman.io) is the best-known JDK manager, and the one jkit is most often compared
with. This page is for SDKMAN users: what each tool does well, how commands translate, and what's
different. It describes SDKMAN as its documentation and source stood in November 2026.

## What SDKMAN does well

- **A wide catalogue.** About 80 candidates besides Java: Kotlin, Groovy, Scala, JBang, Spring Boot,
  Micronaut, Quarkus, JMeter, VisualVM and more. jkit covers JDKs, GraalVM, Maven, Gradle and sbt
  only.
- **Familiar.** Many tutorials and teams use it, and `.sdkmanrc` files are common in repositories.
- **About 17 JDK distributions**, including Temurin, Corretto, Zulu, Liberica, Oracle, GraalVM and
  SapMachine.
- **Checksums.** When SDKMAN's service publishes a checksum for a download, SDKMAN checks it (this
  is on by default, `sdkman_checksum_enable=true`).

## Side by side

| SDKMAN | jkit |
| --- | --- |
| `sdk list java` | `jkit search jdk` (a grid of vendors and releases); `jkit search jdk@21 temurin` for every build |
| `sdk install java 21.0.5-tem` | `jkit install jdk@21.0.5` (Temurin is the default vendor), or `jkit install jdk@21` for the newest 21 |
| `sdk install java 21.0.5-zulu` | `jkit install jdk@21.0.5 zulu` |
| `sdk use java 17.0.13-amzn` | `jkit use jdk@17 corretto` |
| `sdk default java 21.0.5-tem` | `jkit use jdk@21 --global` |
| `sdk current` | `jkit status` |
| `sdk env init` | `jkit pin jdk@21` (writes `jkit.toml`) |
| `sdk env` / `sdk env install` | `jkit sync` (installs what the project pins and switches this shell) |
| `sdkman_auto_env=true` | `jkit hook --install --auto-switch` |
| `sdk upgrade java` | `jkit upgrade jdk` |
| `sdk uninstall java 17.0.13-amzn` | `jkit remove jdk@17.0.13 corretto` |
| `sdk flush` | `jkit prune` |
| `sdk selfupdate` | `jkit self-update` |
| `sdk install maven 3.9.9` | `jkit install maven@3.9.9` |

SDKMAN's identifiers put the vendor in the version (`21.0.5-tem`); jkit takes the vendor as a
separate word (`jdk@21.0.5 temurin`) and lets you leave the version short: `jdk@21` means the
newest 21.

## The differences that matter

- **No bash needed, and Windows.** SDKMAN is a set of bash scripts (with some native helpers) and
  needs bash, curl, zip and unzip; on Windows it runs only in WSL or Git Bash. jkit is one native
  binary for Linux, macOS and Windows (a preview there), and works in fish and PowerShell too.
- **Never prompts.** SDKMAN asks questions, such as whether to make a new install the default,
  unless you set `sdkman_auto_answer=true`. jkit never asks: the first install of a tool becomes
  the default, `--no-default` skips that, and everything else is a flag.
- **Verification.** SDKMAN checks a checksum when its service supplies one and otherwise installs
  without one. jkit refuses a download with no published checksum unless you pass
  `--allow-unverified`, and also checks the OpenPGP signature of Temurin, Microsoft and Corretto
  JDKs, Maven 3.9.0 and later, and sbt.
- **Vendors.** jkit takes JDKs from the foojay Disco API, so it has every vendor foojay lists
  (20+), including ones SDKMAN doesn't carry.
- **Scripts, CI and agents.** `jkit exec jdk@17 -- mvn test` runs one command with chosen versions
  and changes no shell. `--json` and stable exit codes make jkit easy to drive from other
  programs.
- **Explaining itself.** Every jkit command prints what it changed. `jkit status` says which
  version is active and why, and `jkit doctor` says what's wrong and how to fix it.
- **Scope.** If you use SDKMAN for Kotlin, JBang or Spring Boot too, jkit replaces only the Java
  part. The two can live side by side, but only one should set `JAVA_HOME`; `jkit doctor` warns
  when another tool's `JAVA_HOME` wins over jkit's.

## Your .sdkmanrc keeps working

jkit reads `.sdkmanrc` as it is, and never writes it. SDKMAN's vendor suffixes map to jkit's
vendors (`tem` temurin, `amzn` corretto, `librca` liberica, `ms` microsoft, `graalce` graalvm-ce,
and so on), and `21.0.5.fx-zulu` means a JavaFX build. Candidates jkit doesn't manage, such as
`kotlin`, are skipped with one warning.

```properties title=".sdkmanrc"
java=21.0.5-tem
maven=3.9.9
```

In that folder, `jkit sync` installs Temurin 21.0.5 and Maven 3.9.9. Teammates on SDKMAN and on
jkit can share the repository. See [project files](/reference/project-files/) and
[migrating from SDKMAN](/guides/migrating/).
