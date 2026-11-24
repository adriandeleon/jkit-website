---
title: GraalVM and native-image
description: Install GraalVM with jkit, either in its own slot (GRAALVM_HOME, next to your regular JDK) or as your main JDK, and build native images with it.
sidebar:
  order: 6
  label: GraalVM
---

This guide is for building native executables with GraalVM's `native-image`, or running on a
GraalVM JDK. jkit can keep a GraalVM next to your regular JDK, or use one as the JDK itself.

## Two ways to have GraalVM

**In its own slot, next to your JDK.** `jkit install graalvm` installs a GraalVM and points
`GRAALVM_HOME` at it. `JAVA_HOME` and `java` stay on your regular JDK. This is what most native
image builds expect: build tools look for `native-image` in `GRAALVM_HOME`, and everything else
keeps the JDK you chose.

```bash
jkit install graalvm@25            # GraalVM for Java 25 from your default GraalVM vendor
"$GRAALVM_HOME/bin/native-image" --version
```

**As your main JDK.** Install a GraalVM-based build as a `jdk`, and it becomes `JAVA_HOME`,
`java` and `native-image` on `PATH`, like any other JDK:

```bash
jkit install jdk@25 graalvm-ce
jkit use jdk@25 graalvm-ce --global
native-image --version
```

Both share one copy on disk: a build installed one way is already installed for the other.

## The graalvm slot

`graalvm` is a tool of its own in jkit, with the same commands as the others:

```bash
jkit search graalvm                  # GraalVM vendors and releases
jkit install graalvm@21 mandrel      # the vendor goes after the version, as for JDKs
jkit use graalvm@25 --global         # the default for new shells
jkit use graalvm@21 mandrel          # this shell only
jkit pin graalvm@25                  # this project's jkit.toml
jkit exec graalvm@25 -- ./mvnw -Pnative package
```

The version is the Java version (`graalvm@25` is GraalVM for Java 25), not GraalVM's own release
number.

What it sets:

- **`GRAALVM_HOME` only. It is never put on `PATH`**, so there is only ever one `java` on your
  `PATH`: the one from `JAVA_HOME`. Run `native-image` as `"$GRAALVM_HOME/bin/native-image"`, or
  let your build tool find it there.
- The global default is the link `~/.jkit/global/graalvm`. The first GraalVM you install with
  `jkit install graalvm` becomes it, the same way the first JDK becomes the global JDK. Installing
  a GraalVM as a `jdk` doesn't touch the graalvm slot, and the other way round.
- In `jkit.toml`: `graalvm = "25"`, or with a vendor,
  `graalvm = { version = "25", vendor = "mandrel" }`.

### When no GraalVM is chosen

If the graalvm slot is empty (no `jkit use graalvm`, no pin, no global link) and the JDK you
chose with `jkit use`, a project pin, `jkit env` or `jkit exec` is GraalVM-based, `GRAALVM_HOME`
follows that JDK. So a project that pins `jdk = { version = "25", vendor = "graalvm-ce" }` gets
both `JAVA_HOME` and `GRAALVM_HOME` pointing at it.

An `.sdkmanrc` with a GraalVM `java=` line (`java=21.0.2-graalce`) works the same way: as in
SDKMAN, it becomes the JDK.

## GraalVM vendors

Only these four include `native-image`, and only these are accepted for `graalvm` (and as
`graalvm.vendor`):

| Name | Distribution |
|---|---|
| `graalvm-ce` | GraalVM Community Edition (the default GraalVM vendor) |
| `graalvm` | Oracle GraalVM |
| `liberica-nik` | Liberica Native Image Kit (BellSoft) |
| `mandrel` | Mandrel (Red Hat), a GraalVM distribution focused on native-image, used by Quarkus |

```console
$ jkit search graalvm
              maintained   end of life
              25 21 17 11  24 23 22 20
              ‾‾ ‾‾ ‾‾ ‾‾
graalvm-ce *   ✓  ✓  ✓      ✓  ✓  ✓  ✓
graalvm        ✓  ✓  ✓      ✓  ✓  ✓  ✓
liberica-nik   ✓  ✓  ✓  ✓   ✓  ✓  ✓  ✓
mandrel        ✓  ✓  ✓  ✓   ✓  ✓  ✓  ✓
```

Check how current each one is with `jkit search graalvm@21`: some vendors stop updating a release
once a newer one is out, and others keep fixing LTS releases for longer.

To change the default GraalVM vendor:

```bash
jkit config set graalvm.vendor liberica-nik    # or JKIT_GRAALVM_VENDOR=liberica-nik
```

`gluon-graalvm`, which `jkit search jdk` also lists, is installable as a `jdk`, but jkit doesn't
count it among the GraalVM vendors: it can't go in the graalvm slot, and `GRAALVM_HOME` doesn't
follow it.

## Building a native image

`native-image` also needs a C toolchain from your OS (on Linux, gcc and the zlib and glibc
headers; on macOS, the Xcode command line tools; on Windows, the Visual Studio build tools). jkit
doesn't install those.

With the hook loaded and a GraalVM in the slot:

```bash
"$GRAALVM_HOME/bin/native-image" -jar target/app.jar
```

In a build that reads `GRAALVM_HOME` (GraalVM's Native Build Tools plugins for Maven and Gradle
can), the usual command works as is. jkit builds itself this way:

```bash
GRAALVM_HOME=~/.jkit/global/graalvm ./mvnw -Pnative package
```

In scripts and CI, `jkit exec` sets `GRAALVM_HOME` for one command without the hook:

```bash
jkit exec graalvm@25 -- ./mvnw -Pnative package
jkit exec jdk@25 graalvm-ce -- native-image --version
```

## Keeping it current, and removing it

`jkit upgrade` includes GraalVM builds (they show as `graalvm@25 graalvm-ce`), and `jkit prune`
treats them like JDKs. `jkit list` marks the one in the slot `global graalvm`. To clear the slot
without removing anything:

```bash
jkit reset graalvm --global
```
