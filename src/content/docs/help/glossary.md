---
title: Glossary
description: Plain definitions of the Java and jkit terms used in these docs, from JDK and JAVA_HOME to shell hook, pin and foojay.
sidebar:
  order: 4
---

The Java and jkit terms these docs use, in alphabetical order, each with a link to where it
matters. If you're new to Java, [Java basics](/start/java-basics/) explains the main ones in
context.

## Auto-switch

Switching to a project's versions when you `cd` into its folder, and back when you leave. Off by
default; `jkit hook --install --auto-switch` turns it on for bash, zsh and fish. See
[switching versions](/guides/switching-versions/).

## Build

One specific compiled release of a JDK or tool from one vendor, such as Temurin `21.0.13.1+2`.
`jkit search jdk@21 zulu` lists every build of a release, and `jkit pin --exact` records one. See
[versions and vendors](/reference/versions-and-vendors/).

## Checksum

A short fingerprint of a file, such as a SHA-256 hash, published next to a download. jkit computes
the same fingerprint of what it downloaded and refuses the install when they differ. See
[security](/reference/security/).

## Feature release

A Java version numbered by its first number: 21, 22, 25. One comes out every six months, and some
are [LTS](#lts) releases. In jkit, `jdk@21` means "the newest build of feature release 21". See
[Java basics](/start/java-basics/).

## foojay

The [foojay](https://foojay.io) Disco API, a public catalogue of JDK builds from many vendors.
jkit asks it which JDKs and GraalVM builds exist for your platform and where to download them.
See [versions and vendors](/reference/versions-and-vendors/).

## Global default

The version of a tool that every new shell gets when neither the shell nor a project chooses one.
jkit keeps it as a link at `~/.jkit/global/<tool>`; `jkit use jdk@21 --global` sets it. The first
build of a tool you install becomes the default. See [jkit use](/reference/commands/use/).

## GraalVM

A JDK from Oracle and the GraalVM community that adds [native image](#native-image). jkit
installs it as its own tool, `graalvm`, which sets `GRAALVM_HOME`. See [GraalVM](/guides/graalvm/).

## JAVA_HOME

An environment variable holding the folder a JDK is installed in. Maven, Gradle, IDEs and many
scripts read it to find Java. jkit's shell hook sets it to the JDK it chose. See
[shell hook](/reference/shell-hook/).

## JavaFX

A library for desktop user interfaces that's no longer part of the JDK. Some vendors publish JDK
builds with JavaFX included; jkit installs them with `--fx`. See [JavaFX](/guides/javafx/).

## JDK

The Java Development Kit: the compiler (`javac`), the `java` runtime and the other tools you need to
build and run Java programs. When these docs say "a Java version", they mean a JDK. See
[Java basics](/start/java-basics/).

## JRE

The Java Runtime Environment: the part of a JDK that runs programs, without the tools to build
them. jkit installs full JDKs, which include everything a JRE has.

## LTS

Long-term support. Some Java feature releases (8, 11, 17, 21, 25) get updates from vendors for
years; the others are replaced after six months. `jkit install jdk@lts` installs the newest LTS
release. See [choosing a JDK](/guides/choosing-a-jdk/).

## Native image

A GraalVM tool that compiles a Java program ahead of time into a single native program that starts
fast and needs no JDK to run. jkit itself is built this way. See [GraalVM](/guides/graalvm/).

## OpenJDK

The open-source project where Java itself is developed. Nearly every JDK, from Temurin to Oracle's,
is built from OpenJDK source code. See [choosing a JDK](/guides/choosing-a-jdk/).

## PATH

An environment variable listing the folders your shell searches for commands. When you type
`java`, the first folder on `PATH` that has a `java` wins; jkit's hook puts the chosen JDK's `bin`
folder first. See [shell hook](/reference/shell-hook/).

## Pin

A version recorded in a project file, so everyone working on the project gets the same one.
`jkit pin jdk@21` writes it to `jkit.toml`, and `jkit sync` installs it. See
[jkit pin](/reference/commands/pin/).

## Project file

A file in a project's folder that records the versions it builds with: `jkit.toml`, or an existing
`.sdkmanrc`, `.tool-versions` or `.java-version`. See [project files](/reference/project-files/).

## Shell hook

A few lines added to your shell's startup file (`jkit hook --install`) that put the global
versions on `PATH`, let `jkit use` change the current shell, and run auto-switch. Scripts and CI
use `jkit exec` instead. See [shell hook](/reference/shell-hook/).

## Signature

A cryptographic stamp a publisher makes on a file with a private key. Unlike a
[checksum](#checksum), it shows who built the file. jkit checks OpenPGP signatures for Temurin,
Microsoft and Corretto JDKs, Maven 3.9.0 and later, and sbt, with keys built into jkit. See
[security](/reference/security/).

## Vendor

A company or project that builds and publishes JDKs from [OpenJDK](#openjdk), also called a
distribution: Temurin (Eclipse Adoptium), Corretto (Amazon), Zulu (Azul), Microsoft, Oracle and
others. jkit's default is Temurin. See [choosing a JDK](/guides/choosing-a-jdk/).

## Wrapper

A script checked into a project (`mvnw` for Maven, `gradlew` for Gradle) that downloads and runs
the build tool version the project chose. It still needs a JDK, which jkit provides. See
[build tools](/guides/build-tools/).
