---
title: Switching versions
description: Change the JDK, Maven, Gradle or sbt you use, for this shell only or as the global default, and go back again.
sidebar:
  order: 1
---

This guide shows how to change which JDK (or Maven, Gradle, sbt) you're using, once it's
installed. It's for everyone who has installed a second version and wants to move between them.
For per-project versions that switch on their own, see [Projects](/guides/projects/).

There are three places a version can come from. When more than one applies, the narrower one wins:

1. **This shell**: `jkit use jdk@17`. Only the terminal you typed it in; gone when you close it.
2. **The project**: a `jkit.toml` (or `.sdkmanrc`, `.tool-versions`, `.java-version`) in the
   folder you're in, or above it.
3. **Global**: `jkit use jdk@21 --global`. The default for new shells and for programs started
   outside a shell.

## The common case

```bash
jkit list                      # what's installed
jkit use jdk@17 zulu           # this shell only
java -version                  # Zulu 17
jkit reset jdk                 # back to the project's or the global version
jkit use jdk@21 --global       # the default for every new shell
```

`jkit use` only picks from what is installed; it never downloads. If the version isn't there, it
exits with code 3 and tells you what to install:

```console
$ jkit use jdk@25
error: no installed JDK matches 25 from temurin (default vendor)
  install it:          jkit install jdk@25
```

## This shell only

`jkit use <tool>@<version> [vendor]` changes the current shell and nothing else. It needs the
[shell hook](/reference/shell-hook/) (`jkit hook --install`), because a program can't change the
shell that started it; the hook's `jkit` function applies the changes jkit prints.

```console
$ jkit use jdk@21
using jdk 21.0.13.1+2 temurin in this shell
  set    JKIT_SHELL_JDK=~/.jkit/tools/jdk/21.0.13.1+2-temurin
  set    JAVA_HOME=~/.jkit/tools/jdk/21.0.13.1+2-temurin
  PATH   + ~/.jkit/tools/jdk/21.0.13.1+2-temurin/bin
  only this shell; for new shells too: jkit use jdk@21.0.13.1+2 temurin --global
```

Every variable it sets is printed. `JAVA_HOME` (or `MAVEN_HOME`, `GRADLE_HOME`, `SBT_HOME`) points
at the install, and its `bin` folder goes first on `PATH`. jkit only ever removes its own `PATH`
entries (under `~/.jkit`), so anything you added yourself stays.

The version is a prefix: `jdk@21` is the newest installed 21, `jdk@21.0.5` narrows it, and
`jdk` alone is the newest installed JDK. `jdk@lts` is the newest long-term-support release you
have installed. See [versions and vendors](/reference/versions-and-vendors/).

### Going back

`jkit reset jdk` drops this shell's choice. The project's version applies again if you're in a
project, otherwise the global one.

```console
$ jkit reset jdk
reset jdk in this shell
  unset  JKIT_SHELL_JDK
  set    JAVA_HOME=~/.jkit/global/jdk
  PATH   - ~/.jkit/tools/jdk/21.0.13.1+2-temurin/bin
```

If the `bin` that comes back is further back on `PATH` (behind `/usr/bin`, say), it moves to the
front, and jkit says so (`PATH   moved ~/.jkit/global/jdk/bin ahead of /usr/bin`). A `JAVA_HOME`
you had set yourself before `jkit use` comes back instead of the global one; jkit kept it in
`JKIT_USER_JDK`. In a shell where jkit chose no JDK, `jkit reset jdk` says there is nothing to
reset and leaves `JAVA_HOME` alone.

Closing the terminal does the same: shell choices live only in that shell's environment.

## The global default

`jkit use <tool>@<version> --global` points the link `~/.jkit/global/<tool>` at the install.
New shells with the hook get it as `JAVA_HOME` and on `PATH`, and so does anything else you
point at `~/.jkit/global/jdk`.

```console
$ jkit use jdk@17 zulu --global
global jdk → 17.0.21.1+2 zulu
  link   ~/.jkit/global/jdk → ~/.jkit/tools/jdk/17.0.21.1+2-zulu
  new shells with the jkit hook: JAVA_HOME=~/.jkit/global/jdk, PATH includes ~/.jkit/global/jdk/bin
```

`--global` works without the hook, since it only changes a link. Shells that are already open
keep what they have: the link changes under them, so a shell using the global JDK sees the new one
on its next command, but a shell with its own `jkit use` choice, or in a project, keeps that.

The first version of a tool you install becomes its global default on its own (unless you pass
`--no-default`). In a shell with the hook where that tool's `*_HOME` isn't set yet, jkit sets it
and puts its `bin` on `PATH` right away, and says so. Later installs don't change it; `jkit install`
prints the `jkit use … --global` command to run if you want them to.

To have no global version of a tool at all:

```bash
jkit reset jdk --global        # removes the link; the installs stay
```

New shells then get no JDK from jkit until you set one again.

## Picking a vendor

A JDK is built by a vendor (Temurin, Zulu, Corretto and others; see
[Choosing a JDK](/guides/choosing-a-jdk/)). The vendor goes after the version, separated by a
space:

```bash
jkit use jdk@17 zulu
jkit use jdk@21 corretto --global
```

Without a vendor, `jkit use`, `jkit exec` and `jkit remove` look only at your **default vendor**
(Temurin unless you changed it). They don't fall back to another vendor; they say which ones you
have instead:

```console
$ jkit use jdk@17
error: no installed JDK matches 17 from temurin (default vendor)
  install it:          jkit install jdk@17
  or use installed:    jkit use jdk@17 zulu
```

If you mostly use one vendor, make it the default so you can leave it out:

```bash
jkit config set jdk.vendor zulu
```

Project files are more forgiving: a pin without a vendor prefers your default vendor, and
otherwise takes the newest matching install from any vendor. See [Projects](/guides/projects/).

## JavaFX and plain builds side by side

Some vendors ship a JDK with JavaFX bundled. jkit installs those next to the plain build of the
same version (the folder ends in `-fx`), and you choose between them with `--fx`:

```bash
jkit use jdk@21 zulu           # the plain build wins when both are installed
jkit use jdk@21 zulu --fx      # the JavaFX build
```

Without `--fx`, a JavaFX build is picked only when no plain build matches. See
[JavaFX](/guides/javafx/).

## Without the hook

Without the hook, nothing sets `JAVA_HOME` or `PATH` in your shells, and `jkit use` (without
`--global`) can't change anything:

```console
$ jkit use jdk@21
error: the jkit shell hook is not active, so this shell can't be changed
  load it in new shells: jkit hook --install
  or set the default: jkit use jdk@21 --global
  or run one command with it: jkit exec jdk@21 -- <command>
```

You have three ways around it:

```bash
jkit hook --install                  # add the hook, then open a new shell
jkit exec jdk@17 -- mvn test         # run one command with a version; no shell change
eval "$(jkit env)"                   # set this shell to the global and project versions once
```

`jkit exec` and `jkit env` describe a new process, so they start from the global versions and the
project file, and ignore a `jkit use` made in the current shell. Tools you name before `--`
override both:

```bash
jkit exec -- mvn verify                      # project versions, else global
jkit exec jdk@17 zulu maven@3.9 -- mvn -v    # these, whatever the shell or project says
```

These are also what scripts, CI and AI agents should use; see [Scripts](/guides/scripts/) and
[AI agents](/guides/ai-agents/).

## Nested shells

A shell you start from another one (typing `bash`, or a terminal inside some editors) inherits
its environment, including a `jkit use` choice and a project's versions. The hook in the new
shell keeps them: it only sets `JAVA_HOME` and the other variables when they're empty, and puts
the `bin` of the `JAVA_HOME` it has (yours, a `jkit use` choice or the global one) first on
`PATH`, without adding it twice.

```console
$ jkit use jdk@21
$ bash
$ jkit status
jdk      21.0.13.1+2 temurin     shell
maven    3.9.16                  global
```

`jkit reset jdk` in the inner shell changes only the inner shell.

## When a JAVA_HOME from elsewhere wins

The hook sets `JAVA_HOME` only when it's empty. If your shell profile (`~/.bashrc`, `~/.zshrc`,
`~/.profile`, `/etc/environment`) or another tool (SDKMAN, jEnv) sets it first, that value wins
over jkit's global default, and the hook puts its `bin` first on `PATH`, so `java` matches it.
`jkit use` and project switching still set it, since you asked for those explicitly; jkit keeps
your value in `JKIT_USER_JDK` and gives it back when you leave the project or run `jkit reset jdk`.

`jkit status` says when this happens, and `jkit doctor` gives the fix:

```console
$ jkit status
jdk      17.0.13                 outside   JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
         JAVA_HOME is set outside jkit and wins over the global jdk; remove it from your shell profile to use jkit's
$ jkit doctor
warn  JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 is set outside jkit and wins over the global jdk
      fix: remove the export from your shell profile (~/.bashrc, ~/.zshrc, ~/.profile)
```

Remove the `export JAVA_HOME=…` line and open a new shell. The same goes for `MAVEN_HOME`,
`GRADLE_HOME`, `SBT_HOME` and `GRAALVM_HOME`. If another tool puts its own `java` earlier on
`PATH`, `jkit doctor` reports that too; load jkit's hook after it, at the end of the startup file.
[Migrating](/guides/migrating/) covers SDKMAN, asdf, mise, jEnv and manual installs.

## Checking what you have

```bash
jkit status        # each tool: the version active here, and where it comes from
jkit list          # everything installed; which is global and which this shell uses
```

`jkit status` names the source of each version (`shell`, `project`, `global`, `outside`, or
`not set`) and, when that isn't what jkit would pick, the command that fixes it. See
[Troubleshooting](/help/troubleshooting/).

## IDEs and other programs

Programs started from your desktop (IntelliJ IDEA, VS Code, Eclipse) don't run your shell's
startup file, so they never see `jkit use`. Point them at `~/.jkit/global/jdk` (or the global
link of another tool): it follows `jkit use … --global`, so the IDE moves when you change the
default. For a project-specific JDK, point the IDE at the install folder under
`~/.jkit/tools/jdk/`. See [IDEs](/guides/ides/).

## PowerShell and Windows

In PowerShell, `jkit use`, `jkit reset` and `--global` work the same once you've added the hook
to your profile by hand (`jkit hook --install` doesn't edit PowerShell profiles yet). Windows
PowerShell 5.1 loads no profile until scripts are allowed: run
`Set-ExecutionPolicy -Scope CurrentUser RemoteSigned` once. Switching on `cd` isn't available in
PowerShell yet. See [Windows](/guides/windows/) and
[Shells](/guides/shells/).
