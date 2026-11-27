---
title: jkit vs mise and asdf
description: How jkit compares with the general-purpose version managers mise and asdf for Java, with command translations.
sidebar:
  order: 4
  label: vs mise and asdf
---

[mise](https://mise.jdx.dev) and [asdf](https://asdf-vm.com) manage versions of many languages
and tools with one program and one `.tool-versions` file. This page is for people who use either
and want to know what jkit does differently for Java. It describes both as their documentation
stood in November 2026.

## What mise and asdf do well

- **Every language in one tool.** Node, Python, Ruby, Go, Terraform and hundreds more, switched
  the same way. If your projects mix languages, that's a real advantage, and jkit doesn't try to
  match it.
- **mise** is one program, written in Rust, that runs on Linux, macOS and Windows, with Java built in: it reads
  a catalogue of vendor builds (Temurin by default, `zulu-21`, `corretto-21` and so on) and checks
  a download's checksum when the catalogue has one. It also handles Maven, Gradle and sbt through
  its registry, and adds environment variables and tasks.
- **asdf** has been a single Go binary since 0.16. Java comes from the
  [asdf-java](https://github.com/halcyon/asdf-java) plugin, which installs many vendors' builds
  and checks each download's SHA-256 from its release data. Maven, Gradle and sbt come from
  plugins too.

## Side by side

| mise | asdf (0.16+) | jkit |
| --- | --- | --- |
| built in | `asdf plugin add java https://github.com/halcyon/asdf-java.git` | built in |
| `mise ls-remote java` | `asdf list all java` | `jkit search jdk` |
| `mise install java@21` | `asdf install java latest:temurin-21` | `jkit install jdk@21` |
| `mise install java@zulu-21` | `asdf list all java zulu`, then `asdf install java <id>` | `jkit install jdk@21 zulu` |
| `mise use -g java@21` | `asdf set -u java temurin-21.0.5+11.0.LTS` | `jkit use jdk@21 --global` |
| `mise use java@21` (writes `mise.toml`) | `asdf set java temurin-21.0.5+11.0.LTS` (writes `.tool-versions`) | `jkit pin jdk@21` (writes `jkit.toml`) |
| `mise shell java@17` | `export ASDF_JAVA_VERSION=temurin-17.0.13+11` | `jkit use jdk@17` |
| `mise install` (in a project) | `asdf install` (in a project) | `jkit sync` |
| `mise exec java@17 -- java -version` | `ASDF_JAVA_VERSION=… asdf exec java -version` | `jkit exec jdk@17 -- java -version` |
| `mise ls` | `asdf list java` | `jkit list` |
| `mise current` | `asdf current` | `jkit status` |
| `mise upgrade` | (install the newer version, then `asdf set`) | `jkit upgrade` |
| `mise uninstall java@17` | `asdf uninstall java temurin-17.0.13+11` | `jkit remove jdk@17 --all` |
| `mise doctor` | | `jkit doctor` |

## The differences that matter

- **Java depth versus breadth.** jkit only does Java toolchains, and does more there: 20+ vendors
  from the foojay Disco API, JavaFX builds (`--fx`), a separate GraalVM slot (`GRAALVM_HOME` next
  to `JAVA_HOME`), and OpenPGP signature checks for Temurin, Microsoft and Corretto JDKs, Maven
  3.9.0 and later, and sbt, on top of a required checksum.
- **`JAVA_HOME` without extra setup.** jkit sets `JAVA_HOME`, `MAVEN_HOME`, `GRADLE_HOME` and
  `SBT_HOME` from the same choice as `PATH`. With asdf, `JAVA_HOME` needs asdf-java's
  `set-java-home` script in your shell profile.
- **Shims versus a hook.** asdf (and mise, if you use its shims) puts small launcher scripts on
  `PATH` that choose a version each time they run. jkit puts the real `bin` folder on `PATH` and
  sets the variables directly, so tools that read `JAVA_HOME` or look for a JDK folder see the
  same JDK as your shell. `jkit status` shows which one applies and why.
- **Windows.** mise runs on Windows; asdf doesn't document Windows support, and asdf-java needs
  bash 5. jkit runs on Windows as a preview; `jkit env --install` gives cmd.exe, IDEs and other
  programs the global versions too.
- **Trust prompts.** mise may ask you to trust a project's config file before using parts of it.
  jkit's project files only name versions, so there's nothing to trust; it ignores files owned by
  another user or writable by everyone instead.
- **Reporting.** Every jkit command prints what it changed, and `jkit doctor` explains problems
  and how to fix them.

## Your .tool-versions keeps working

jkit reads `.tool-versions` as it is, and never writes it. asdf-java names map to jkit's vendors
(`temurin`, `adoptopenjdk` as temurin, `graalvm-community` as graalvm-ce, …); only the first
version on a line counts, and `system`, `ref:` and `path:` are skipped. Corretto's `21.0.5.11.1`
numbering, Semeru's `_openj9-…` suffix, Mandrel's `mandrel-<release>-java<N>` and `-javafx` read
too; `-jre`, `-lite` and `-musl` ids are pinned as the vendor's full JDK, with a warning. A Zulu
release number such as `zulu-21.38.21` pins only the major, with a warning; write the Java version
(`zulu-21.0.5`) to be exact.

```text title=".tool-versions"
java temurin-21.0.5+11.0.LTS
maven 3.9.9
nodejs 22.11.0
```

In that folder, `jkit sync` installs Temurin 21.0.5 and Maven 3.9.9 and leaves Node to mise or
asdf. jkit doesn't read `mise.toml`. See [project files](/reference/project-files/) and
[migrating from asdf or mise](/guides/migrating/).
