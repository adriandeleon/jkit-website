---
title: Java basics
description: For people new to Java - JDKs, versions and LTS, vendors, JAVA_HOME and PATH, Maven, Gradle, sbt, wrappers and GraalVM, and how jkit handles each.
sidebar:
  order: 4
---

New to Java, or coming back after a long time? This page explains the words the rest of these docs
use, in a few minutes. If you already know what a JDK vendor and `JAVA_HOME` are, skip to the
[quickstart](/start/quickstart/).

## JDK and JRE

To run Java programs you need a Java runtime: the `java` command and the libraries it uses. To
build them you also need the compiler (`javac`) and other tools. The **JDK** (Java Development
Kit) has both. A **JRE** (Java Runtime Environment) is the runtime alone. Oracle stopped shipping a separate JRE
with Java 11, and some vendors still offer one, but as a developer you want the JDK.

jkit installs JDKs. `jkit install jdk@21` gives you `java`, `javac`, `jar` and the rest.

## Versions and LTS

A new Java version comes out every six months (March and September). Most get updates only until
the next one. Some are **long-term-support (LTS)** releases, which vendors keep fixing for years;
since Java 17 there has been one every two years. The LTS releases are:

| Version | Released | Notes |
| --- | --- | --- |
| 8 | 2014 | Still used by older applications |
| 11 | 2018 | |
| 17 | 2021 | |
| 21 | 2023 | A common choice for new projects |
| 25 | 2025 | The newest LTS |

Use an LTS release unless you need a newer feature. Versions are written as a number with more
parts for each update: `21` is the release, `21.0.9` an update of it, `21.0.9+10` one build.

In jkit, a version is a prefix: `jdk@21` means the newest 21 you have (or can download),
`jdk@21.0.9` narrows it, and `jdk@lts` means the newest LTS. `jkit search jdk` marks LTS
releases.

## Vendors

Java's source code is developed in the open, as **OpenJDK**. Many companies build it, test it and
ship it as their own **distribution**: Eclipse Temurin, Amazon Corretto, Azul Zulu, BellSoft
Liberica, Microsoft, SAP's SapMachine, IBM Semeru, Oracle and others. jkit calls these
**vendors**.

They are built from the same code, and the widely used ones are tested against Java's
compatibility test suite, so for most work any of them will do. They differ in:

- how long each version gets updates, and whether you can buy support;
- extras, such as builds with JavaFX, a different virtual machine (Semeru uses OpenJ9), or
  GraalVM's native-image tool;
- licence terms. Most are free to use anywhere, but some vendors' builds have conditions for
  production or commercial use. Check the vendor's licence before you rely on a build in
  production.

jkit uses Eclipse Temurin unless you name another vendor (`jkit install jdk@21 zulu`) or change
the default (`jkit config set jdk.vendor zulu`). [Choosing a JDK](/guides/choosing-a-jdk/) helps
you pick, and `jkit search jdk` lists every vendor jkit can install.

## JAVA_HOME and PATH

`PATH` is the list of folders your shell searches for commands. When you type `java`, the first
`java` found on `PATH` runs. `JAVA_HOME` is an environment variable naming the folder of the JDK
to use; build tools, IDEs and many scripts read it instead of looking at `PATH`.

Both have to point at the same JDK, or you get odd results: `java -version` says one thing and
your build uses another. Switching JDKs by hand means changing both, in every shell.

jkit sets both for you. Its [shell hook](/reference/shell-hook/) puts the chosen JDK's `bin`
folder first on `PATH` and sets `JAVA_HOME`, plus `MAVEN_HOME`, `GRADLE_HOME`, `SBT_HOME` and
`GRAALVM_HOME` for the other tools. `jkit status` shows what's set and why.

## Build tools: Maven, Gradle and sbt

Real projects are built with a build tool that downloads libraries, compiles and runs tests.

- **Maven** (`mvn`) reads `pom.xml`. Common in enterprise Java.
- **Gradle** (`gradle`) reads `build.gradle` or `build.gradle.kts`. Common for Android and many
  newer projects.
- **sbt** (`sbt`) reads `build.sbt`. Used mostly for Scala.

Each runs on a JDK, found through `JAVA_HOME` or `PATH`. jkit installs all three
(`jkit install maven`, `jkit install gradle@8.14`, `jkit install sbt`) and switches them like
JDKs. For sbt, jkit picks the launcher; the sbt version a build runs on still comes from its
`project/build.properties`.

## Wrappers: mvnw and gradlew

Many projects include a **wrapper**: a small script (`./mvnw` or `./gradlew`) committed with the
code, which downloads the exact Maven or Gradle version the project wants and runs it. If a
project has one, use it.

Wrappers still need a JDK, which they find through `JAVA_HOME`. That's the part jkit provides:
pin the JDK in the project (`jkit pin jdk@21`) and run the wrapper as usual. See
[build tools](/guides/build-tools/).

## GraalVM

GraalVM is a JDK with extra tools, most known for `native-image`, which compiles a Java program
into a standalone native executable (jkit itself is built this way). Build tools find it through
`GRAALVM_HOME`.

jkit keeps GraalVM in a slot of its own, so you can have a regular JDK in `JAVA_HOME` and GraalVM
in `GRAALVM_HOME` at once: `jkit install graalvm@25`. See [GraalVM](/guides/graalvm/).

## Next

- [Quickstart](/start/quickstart/): install two JDKs and switch between them.
- [Glossary](/help/glossary/): more terms, briefly.
