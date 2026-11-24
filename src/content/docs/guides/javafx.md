---
title: JavaFX
description: Install JDK builds with JavaFX bundled, switch between them and plain builds, and pin them in a project with fx = true.
sidebar:
  order: 7
---

This guide is for desktop developers using JavaFX, the Java UI toolkit. JavaFX hasn't been part
of the JDK since Java 11, so there are two ways to get it: as a library your build downloads, or
in a JDK build that bundles it. jkit handles the second.

If your project gets JavaFX from Maven Central (the `org.openjfx` dependencies, with the JavaFX
Maven or Gradle plugin), you don't need a JavaFX JDK: any JDK works, and you can skip this page.

## Which vendors ship JavaFX builds

`--fx` on `jkit search` shows them:

```console
$ jkit search jdk@21 --fx
liberica         21.0.13.1+5
liberica-nik     21.0.13+6
zulu             21.0.13.1
```

At the time of writing, on Linux x64, current releases come from **Liberica**, **Liberica NIK**
and **Zulu**. Corretto and Oracle also have JDK 8 builds with JavaFX (JDK 8 still bundled it).
What's available differs by platform; `jkit search jdk --fx` shows the grid for yours.

## Installing

```bash
jkit install jdk@21 zulu --fx
jkit install jdk@lts liberica --fx
```

A JavaFX build installs **next to** the plain build of the same version, in its own folder ending
in `-fx` (`~/.jkit/tools/jdk/21.0.13.1-zulu-fx`). You can have both, and `jkit list` marks which
is which.

## Switching: plain builds win

When both a plain and a JavaFX build match, commands pick the plain one unless you add `--fx`:

```bash
jkit use jdk@21 zulu           # the plain build
jkit use jdk@21 zulu --fx      # the JavaFX build
jkit use jdk@21 zulu --fx --global
```

Without `--fx`, a JavaFX build is picked only when no plain build matches. With `--fx`, only
JavaFX builds match: if you have none, jkit says so and gives the `jkit install … --fx` command.

`jkit remove` follows the same rule: without `--fx` it removes the plain build first.

## Pinning in a project

In `jkit.toml`, `fx = true` asks for a JavaFX build:

```bash
jkit pin jdk@21 zulu --fx
```

```toml title="jkit.toml"
[tools]
jdk = { version = "21", vendor = "zulu", fx = true }
```

`jkit sync` then installs the JavaFX build, and only a JavaFX build satisfies the pin. Without
`fx = true`, a plain build is preferred, as on the command line.

Other project files can say the same thing:

- `.sdkmanrc`: `.fx` before the vendor suffix, as SDKMAN writes it: `java=21.0.5.fx-zulu`.
- `.tool-versions`: a `-javafx` or `-fx` suffix on the vendor: `java liberica-javafx-21.0.5`.

## Upgrading

`jkit upgrade` treats JavaFX builds as their own release line, so a JavaFX 21 is upgraded to a
newer JavaFX 21, never to a plain one. Installs made with jkit versions that didn't yet record
JavaFX may need `jkit upgrade --fx` once.
