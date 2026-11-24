---
title: Maven, Gradle and sbt
description: Install and switch Maven, Gradle and sbt with jkit, use the Maven and Gradle wrappers and sbt's launcher with a jkit JDK, and let Gradle and Maven toolchains find jkit's JDKs.
sidebar:
  order: 4
  label: Build tools
---

This guide covers the build tools: installing Maven, Gradle and sbt with jkit, how jkit works with
the wrappers many projects ship (`./mvnw`, `./gradlew`), and how Gradle and Maven toolchains can
use the JDKs jkit installs. It's for Java and Scala developers who already know their build tool.

## Installing and switching

Maven, Gradle and sbt work like JDKs, without vendors:

```bash
jkit search maven               # versions available (also: gradle, sbt)
jkit search maven@3.9           # only 3.9.x
jkit install maven              # newest Maven
jkit install gradle@8.14        # newest 8.14.x
jkit install sbt@1.10
jkit use gradle@9 --global      # the default for new shells
jkit use maven@3.9              # this shell only
jkit pin gradle@8.14            # this project (jkit.toml)
```

The first version of each tool you install becomes its global default. The hook then sets
`MAVEN_HOME`, `GRADLE_HOME` or `SBT_HOME` and puts the tool's `bin` folder on `PATH`, so `mvn`,
`gradle` and `sbt` run the version jkit picked, on the JDK in `JAVA_HOME`.

Where they come from, and how they're checked:

| Tool | Source | Check |
|---|---|---|
| Maven | Maven Central (`apache-maven-<version>-bin.zip`) | its checksum; from 3.9.0, also the release manager's OpenPGP signature, which must be good. Very old releases with only a SHA-1 checksum install with a warning, marked unverified. |
| Gradle | services.gradle.org | its SHA-256 checksum. Gradle publishes no signatures. |
| sbt | sbt's GitHub releases (versions listed from Maven Central); 1.3.0 and later | its checksum and OpenPGP signature |

Release candidates and milestones are hidden unless you add `--pre` (`jkit search maven --pre`,
`jkit install maven@4 --pre`). An exact pre-release version (`maven@4.0.0-rc-4`, say) asks for it
by itself. See [Security](/reference/security/) for how downloads are checked.

Each Maven release is signed by whichever Apache release manager cut it, and jkit has the keys of
the ones who signed 3.9.0 and later. A release by someone new is refused until a jkit release adds
their key: `jkit self-update`, then try again. `--allow-unverified` installs it on the checksum
alone, marked unverified. See [Troubleshooting](/help/troubleshooting/#signature-missing-or-by-an-unknown-key).

## Projects with a wrapper

Many projects ship the Maven Wrapper (`mvnw`) or the Gradle Wrapper (`gradlew`). The wrapper
names the exact Maven or Gradle version in the repository and downloads it on first use, into
`~/.m2/wrapper` or `~/.gradle/wrapper`. jkit doesn't replace that, and doesn't need to: keep
using the wrapper, and let jkit supply the JDK.

Both wrappers run on the JDK in `JAVA_HOME` (or the `java` on `PATH` when it's unset), which is
exactly what jkit sets. So in a wrapper project, pin only the JDK:

```toml title="jkit.toml"
[tools]
jdk = "21"
```

```bash
./mvnw verify                          # with the hook: the project's JDK
jkit exec -- ./gradlew build           # without the hook (scripts, CI)
jkit exec jdk@17 -- ./gradlew build    # try another JDK for one run
```

You don't need Maven or Gradle installed through jkit at all for these projects. You need one
once to create a wrapper in a new project:

```bash
jkit exec gradle@9 -- gradle wrapper       # writes gradlew and gradle/wrapper/
jkit exec maven@3.9 -- mvn -N wrapper:wrapper
```

`jkit exec` with a tool named before `--` uses that version for one command, even if it isn't
the global one. It must be installed (`jkit install gradle@9` first).

Not every build-tool version runs on every JDK: an old Gradle may fail to start on a new JDK.
If a wrapper project breaks after you change the JDK, pin a JDK that its Gradle version
supports, or upgrade the wrapper.

## sbt: the launcher and the build's version

sbt has two versions. The one jkit installs is the **launcher**: the `sbt` command. Each build
names the sbt version it runs on in `project/build.properties`:

```ini title="project/build.properties"
sbt.version=1.10.7
```

The launcher reads that and downloads that sbt itself. So `sbt = "1.10"` in `jkit.toml` picks the
launcher only; the build still runs on whatever `project/build.properties` says. Any recent
launcher can start older builds, so most people install one launcher and leave it:

```bash
jkit install sbt
```

Like the wrappers, sbt runs on the JDK in `JAVA_HOME`, so pin the JDK in `jkit.toml` as for any
other project.

## Gradle toolchains

A Gradle build can ask for a JDK version that differs from the one Gradle itself runs on:

```kotlin title="build.gradle.kts"
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
```

Gradle then looks for a matching JDK on the machine. It finds the JDK it's running on (the one in
`JAVA_HOME`), but it doesn't know about `~/.jkit`, and jkit has no Gradle integration of its own.
Tell Gradle where jkit's JDKs are with a Gradle setting, in `~/.gradle/gradle.properties`:

```ini title="~/.gradle/gradle.properties"
org.gradle.java.installations.paths=/home/you/.jkit/tools/jdk/17.0.21.1+2-zulu,/home/you/.jkit/tools/jdk/21.0.13.1+2-temurin
```

- Use absolute paths: Gradle doesn't expand `~`. On Windows, the folders are under
  `%USERPROFILE%\.jkit\tools\jdk\`.
- Each folder under `~/.jkit/tools/jdk/` is a JDK home on every OS (on macOS, jkit installs the
  `Contents/Home` part), so Gradle can use it as is.
- The folder names change when you upgrade: `jkit upgrade` installs a new folder, and
  `jkit prune` or `jkit remove` deletes the old one. Update the list when that happens.

To print the current list of JDK folders (with `jq`):

```bash
jkit list --json | jq -r '[.installed[] | select(.tool == "jdk") | .home] | join(",")'
```

If the build applies the foojay toolchains resolver plugin, Gradle downloads a JDK it can't find
into its own folder instead. Listing jkit's JDKs as above lets it use those first.

## Maven toolchains

The Maven Toolchains Plugin (and plugins that support toolchains, such as the compiler and
surefire plugins) picks a JDK from `~/.m2/toolchains.xml`. jkit doesn't write that file; add
jkit's JDKs to it yourself:

```xml title="~/.m2/toolchains.xml"
<toolchains>
  <toolchain>
    <type>jdk</type>
    <provides>
      <version>17</version>
      <vendor>zulu</vendor>
    </provides>
    <configuration>
      <jdkHome>/home/you/.jkit/tools/jdk/17.0.21.1+2-zulu</jdkHome>
    </configuration>
  </toolchain>
  <toolchain>
    <type>jdk</type>
    <provides>
      <version>21</version>
      <vendor>temurin</vendor>
    </provides>
    <configuration>
      <jdkHome>/home/you/.jkit/tools/jdk/21.0.13.1+2-temurin</jdkHome>
    </configuration>
  </toolchain>
</toolchains>
```

As with Gradle, these are paths to one build each; after `jkit upgrade` and `jkit prune`, update
them. `~/.jkit/global/jdk` always points at the global default, if one entry should follow
`jkit use … --global` instead.

Maven itself still runs on the JDK in `JAVA_HOME`.

## IDEs

IDEs run Maven and Gradle with a JDK of their own choosing. Point them at a jkit JDK as described
in [IDEs](/guides/ides/).
