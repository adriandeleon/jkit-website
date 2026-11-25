---
title: IDEs
description: Use jkit from Editora, or point IntelliJ IDEA, VS Code, Eclipse and NetBeans at the JDKs, Maven and Gradle that jkit installs.
sidebar:
  order: 11
---

This page shows how to use jkit's JDKs and build tools from an IDE. [Editora](#editora) works with
jkit directly. Other IDEs don't read `jkit.toml` and don't load your shell's startup file, so you
tell them where the JDK is once. There are two kinds of path to give them.

| Path | Follows | Use it for |
| --- | --- | --- |
| `~/.jkit/global/jdk` | `jkit use jdk@… --global` | Your everyday JDK: change it with one command, and the IDE picks up the new one |
| `~/.jkit/tools/jdk/<folder>` | Nothing: one exact build | A project that pins a version in `jkit.toml` |

`~/.jkit/global/jdk` is a link (a directory junction on Windows, at
`%USERPROFILE%\.jkit\global\jdk`). The same goes for `global/maven`, `global/gradle`,
`global/sbt` and `global/graalvm`. Write the full path in IDE settings (`/home/you/.jkit/...` or
`/Users/you/.jkit/...`): most settings files don't expand `~`.

:::tip
`~/.jkit` is a hidden folder. In a file chooser, type the path, or show hidden files (on macOS,
press Cmd+Shift+. in the dialog).
:::

## Find the right folder

```bash
jkit list                     # every install, and which one is global
jkit list --json | jq -r '.installed[] | select(.tool=="jdk") | .home'
jkit env --json | jq -r '.variables.JAVA_HOME'   # the JDK a new process in this folder gets
```

Run the last one inside the project: it gives the JDK the project file pins, or the global one
when there is no pin. The folder names under `tools/jdk/` are the version and vendor, such as
`21.0.9+10-temurin`.

After `jkit upgrade`, a new build lands in a new folder. A global link moves to it; an IDE that
points at an exact `tools/jdk/...` folder keeps the old build until you change it (or until
`jkit prune` removes the old one). That's the reason to prefer `global/jdk` where you can.

## Editora

[Editora](https://editora-project.dev/) 0.29.0 and later support jkit fully, so there are no paths
to set. Editora reads the project's pins through jkit (`jkit.toml`, `.sdkmanrc`, `.tool-versions`,
`.java-version`) and uses jkit's JDKs, Maven, Gradle, sbt and GraalVM for the Java language server
and for Run and Debug. Its **Java Toolchain** commands run jkit's own commands: install, `sync`,
`status`, `upgrade`, `prune`, `doctor`, the default version and vendor, switching on `cd`, the
shell hook, and `self-update`. **Set Project JDK…** can save the choice to `jkit.toml`. See
[Editora's jkit docs](https://editora-project.dev/docs/build-tools/#jkit).

## IntelliJ IDEA

- **JDK:** File → Project Structure → SDKs → **+** → Add JDK from disk, and choose
  `~/.jkit/global/jdk` or a folder under `~/.jkit/tools/jdk/`. Then pick it as the project SDK
  under Project.
- **Maven:** Settings → Build, Execution, Deployment → Build Tools → Maven → Maven home path:
  `~/.jkit/global/maven`. Or keep the bundled Maven or the Maven wrapper.
- **Gradle:** Settings → Build, Execution, Deployment → Build Tools → Gradle → Gradle JVM: the
  JDK you added above. With the Gradle wrapper (the usual case) that is all Gradle needs.

## VS Code

The Extension Pack for Java reads these settings (in `settings.json`, user or workspace):

```json title="settings.json"
{
  "java.jdt.ls.java.home": "/home/you/.jkit/global/jdk",
  "java.configuration.runtimes": [
    { "name": "JavaSE-21", "path": "/home/you/.jkit/tools/jdk/21.0.9+10-temurin", "default": true },
    { "name": "JavaSE-17", "path": "/home/you/.jkit/tools/jdk/17.0.13+11-zulu" }
  ]
}
```

- `java.jdt.ls.java.home` is the JDK that runs the Java language server itself. It needs a recent
  JDK (check the extension's notes for the minimum); `global/jdk` is a good fit.
- `java.configuration.runtimes` lists the JDKs your projects compile and run with. The `name` is
  the Java SE release (`JavaSE-17`, `JavaSE-21`, …); the extension matches it to the project's
  target level.

VS Code started from a terminal with the jkit hook (`code .`) inherits that shell's `JAVA_HOME`
and `PATH`, so its integrated terminal and tasks see the same versions as your shell.

## Eclipse

- **JDK:** Window → Preferences (Eclipse → Settings on macOS) → Java → Installed JREs → Add →
  Standard VM, and set the JRE home to `~/.jkit/global/jdk` or a `tools/jdk/` folder. Tick it
  to make it the default, and map it to an execution environment under Installed JREs →
  Execution Environments if your projects use one.
- **Maven:** Preferences → Maven → Installations → Add: `~/.jkit/global/maven`. Eclipse also
  ships its own embedded Maven.
- The JDK that runs Eclipse itself is set with `-vm` in `eclipse.ini`; you can point it at
  `~/.jkit/global/jdk/bin` too.

## NetBeans

- **JDK:** Tools → Java Platforms → Add Platform, and choose the JDK folder.
- **Maven:** Tools → Options → Java → Maven → Maven Home: `~/.jkit/global/maven`.
- The JDK that runs NetBeans itself is `netbeans_jdkhome` in `etc/netbeans.conf` of the NetBeans
  installation.

## Other IDEs and editors

Anything that asks for a "JDK home", "Java home" or `JAVA_HOME` wants the folder that contains
`bin/java`: `~/.jkit/global/jdk` or `~/.jkit/tools/jdk/<folder>`. Anything that asks for a
"Maven home" or "Gradle home" wants `~/.jkit/global/maven` or `~/.jkit/global/gradle`.

## Why the IDE doesn't see `jkit use`

The jkit hook runs in shells you open in a terminal. An IDE started from the desktop, the Dock or
the Start menu never runs your shell's startup file, so it doesn't get `JAVA_HOME` or `PATH` from
the hook, and it never sees a `jkit use` without `--global` (that only changes one shell).

Two ways around it:

- **Point the IDE at a path** as shown above. `~/.jkit/global/jdk` follows
  `jkit use … --global`, so switching the default for the IDE is one command.
- **Start the IDE from a terminal** with the hook loaded (`code .`, `idea .`, or your IDE's
  launcher script). It then inherits that shell's environment, including a `jkit use` choice.

On Windows, programs outside a hooked PowerShell see the global JDK only if you set `JAVA_HOME`
once; see [Windows](/guides/windows/#programs-outside-powershell).

## Gradle toolchains

Gradle's [toolchains](https://docs.gradle.org/current/userguide/toolchains.html) look for JDKs in
places Gradle knows, and jkit's folder isn't one of them. Tell Gradle where jkit's JDKs are with
`org.gradle.java.installations.paths` in `~/.gradle/gradle.properties`, a comma-separated list of
JDK folders:

```properties title="~/.gradle/gradle.properties"
org.gradle.java.installations.paths=/home/you/.jkit/tools/jdk/17.0.13+11-zulu,/home/you/.jkit/tools/jdk/21.0.9+10-temurin
```

## For IDE plugin authors

`jkit env --json` prints the environment a new process in a folder should get, without running
anything or using the network:

```bash
jkit env --dir ~/src/app --json
```

```json
{
  "schemaVersion": 1,
  "dir": "/home/me/src/app",
  "project": { "dir": "/home/me/src/app", "files": ["/home/me/src/app/jkit.toml"], "warnings": [] },
  "variables": {
    "JAVA_HOME": "/home/me/.jkit/tools/jdk/21.0.9+10-temurin",
    "MAVEN_HOME": "/home/me/.jkit/global/maven"
  },
  "pathPrepend": ["/home/me/.jkit/tools/jdk/21.0.9+10-temurin/bin", "/home/me/.jkit/global/maven/bin"],
  "path": "/home/me/.jkit/tools/jdk/21.0.9+10-temurin/bin:/home/me/.jkit/global/maven/bin:/usr/bin",
  "pathSeparator": ":"
}
```

Set `variables`, put `pathPrepend` first on `PATH`, and the plugin runs builds with the project's
versions. A pin that isn't installed is an error document with exit code 3, whose `hints` name
the `jkit install` command to run. `jkit list --json` lists every install and `jkit status --json` explains what is
active and why. The full shapes are in the [JSON reference](/reference/json/).
