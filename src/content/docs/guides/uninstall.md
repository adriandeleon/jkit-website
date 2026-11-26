---
title: Uninstalling jkit
description: Remove jkit's shell hook, everything it installed, and the jkit binary itself, and know what jkit never touched.
sidebar:
  order: 10
  label: Uninstall
---

This guide removes jkit completely. It takes three steps, because jkit keeps everything in one
folder and changes only one file outside it.

```bash
jkit hook --uninstall          # take the hook out of your shell's startup file
rm -rf ~/.jkit                 # every install, link, cached list and setting
brew uninstall jkit            # or the command for how you installed it (below)
```

Do them in this order: the first step needs jkit. On Windows, skip it and take the hook line out
of your PowerShell `$PROFILE` by hand ([below](#1-remove-the-shell-hook)).

## 1. Remove the shell hook

```console
$ jkit hook --uninstall
removed the jkit hook from ~/.bashrc
  file   ~/.bashrc updated
  open shells keep the hook until they exit
```

It takes out only the marked block that `jkit hook --install` added, and nothing else in the
file. For fish, it deletes `~/.config/fish/conf.d/jkit.fish`, which is jkit's own file.

- Without a shell name it uses your login shell (`$SHELL`). If you added the hook for more than
  one shell, run it for each: `jkit hook zsh --uninstall`, `jkit hook fish --uninstall`.
- If you added the hook by hand (`eval "$(jkit hook bash)"` in your startup file), jkit says which
  line loads it; remove that line yourself.
- **PowerShell:** `jkit hook --install` doesn't edit PowerShell profiles, so neither does
  `--uninstall`. Open your profile (`notepad $PROFILE`) and delete the
  `jkit hook pwsh | Out-String | Invoke-Expression` line.

Shells that are already open keep the hook until you close them.

## 2. Remove jkit's folder

Every JDK, Maven, Gradle and sbt jkit installed, the global links, the cached version lists and
`config.toml` live in one folder:

```bash
rm -rf ~/.jkit
```

If you set `JKIT_HOME`, delete that folder instead, and remove the `export JKIT_HOME=…` line from
your profile. On Windows the folder is `%USERPROFILE%\.jkit`.

This can't be undone; any version you want again has to be downloaded again. If you want to keep
one JDK, copy its folder out of `~/.jkit/tools/jdk/` first.

## 3. Remove the jkit binary

Use the same tool you installed it with:

| Installed with | Remove with |
|---|---|
| Homebrew | `brew uninstall jkit` (and `brew untap adriandeleon/tap` if nothing else uses the tap) |
| apt | `sudo apt remove jkit` |
| dnf (an `.rpm`) | `sudo dnf remove jkit` |
| winget | `winget uninstall AdrianDeLeon.jkit` |
| a `.tar.gz` or `.zip` | delete the `jkit` (or `jkit.exe`) you put on `PATH`, for example `sudo rm /usr/local/bin/jkit`; on Windows, also any `jkit.exe.old` next to it |

If you added the Vitrina apt repository only for jkit, you can remove it too:

```bash
sudo rm /etc/apt/sources.list.d/vitrina.sources /etc/apt/keyrings/vitrina.asc
sudo apt update
```

Not sure how it was installed? `which -a jkit` (or `where.exe jkit` on Windows) shows where it
is: under `/opt/homebrew`, `/usr/local/Cellar` or `/home/linuxbrew` it's Homebrew, in `/usr/bin`
a Linux package.

## Things you may have set up yourself

jkit didn't make these, so it can't undo them, but they point into `~/.jkit` and stop working
once it's gone:

- **IDE settings** that point at `~/.jkit/global/jdk` or a folder under `~/.jkit/tools/`.
- **Windows environment variables**: a `JAVA_HOME` set to `%USERPROFILE%\.jkit\global\jdk`, and
  `%JAVA_HOME%\bin` in your user `Path`.
- **Build tool settings**: `org.gradle.java.installations.paths` in
  `~/.gradle/gradle.properties`, or `jdkHome` entries in `~/.m2/toolchains.xml` (see
  [Build tools](/guides/build-tools/)).
- **Your settings for jkit** in your profile or CI: `JKIT_*` variables, and `jkit exec` or
  `jkit sync` steps in scripts and pipelines.
- **Project files**: `jkit.toml` files you committed stay in their repositories. They're harmless
  without jkit; delete them if no one on the project uses jkit.

## What jkit never touched

jkit writes only inside `~/.jkit` (or `JKIT_HOME`), plus the hook block in your shell's startup
file (or fish's `conf.d/jkit.fish`), and the `jkit.toml` files you asked it to write with
`jkit pin`. While a command runs, the hook also uses a temporary file in your temp folder, which
it deletes right after.

It never changes:

- JDKs installed another way: OS packages, `/usr/lib/jvm`, `/Library/Java/JavaVirtualMachines`,
  `update-alternatives`, `/usr/bin/java`.
- Other tools' folders: `~/.sdkman`, `~/.asdf`, `~/.jenv`, mise's folders.
- Build tool caches and settings: `~/.m2`, `~/.gradle`, `~/.ivy2`, `~/.sbt`. Dependencies and
  wrapper downloads your builds made stay there.
- System-wide settings, or other users' files.

So after these three steps, your machine is as it was before jkit, apart from those caches your
builds filled.
