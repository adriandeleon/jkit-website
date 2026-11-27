---
title: Per-project versions
description: Record the JDK and build tools a repository uses in jkit.toml, install them with jkit sync, and switch to them automatically on cd.
sidebar:
  order: 2
  label: Projects
---

This guide shows how to give a repository its own JDK, Maven, Gradle or sbt version, so that
everyone who works on it, and every shell you open in it, uses the same ones. It's for anyone
with more than one project; the file format itself is in
[Project files](/reference/project-files/).

## Pin, sync, switch

From the project's root folder:

```console
$ jkit pin jdk@21
pinned jdk 21 in ~/src/app/jkit.toml
  file   ~/src/app/jkit.toml created
$ jkit pin maven@3.9
$ cat jkit.toml
# Versions jkit uses in this project: jkit sync installs them.
[tools]
jdk = "21"
maven = "3.9"
```

- `jkit pin <tool>@<version> [vendor]` writes one line of `jkit.toml`, creating the file if
  needed. Only that tool's line changes; comments, order and line endings stay.
- `jkit unpin <tool>` removes that line.
- `jkit sync` installs whatever the file asks for that isn't installed yet, and switches this
  shell to it. `jkit sync --dry-run` only lists what it would install.

Commit `jkit.toml`. A teammate who clones the repository runs `jkit sync` and gets the same
versions (see [Teams](/guides/teams/)).

`jkit pin` checks that the version exists, installed or downloadable, so a typo fails on your
machine and not on someone else's. Without a version, it pins what this shell is using: the
feature release for a JDK (`21`), the full version for Maven, Gradle and sbt.

## Switching on cd

With auto-switch on, a shell with the hook switches to the project's versions when you `cd` into
the project, and back to the global versions when you leave. It's off by default; turn it on
with:

```bash
jkit hook --install --auto-switch
```

That sets `auto_switch = true` under `[shell]` in `~/.jkit/config.toml` (or use
`jkit config set shell.auto_switch true`; the environment variable `JKIT_AUTO_SWITCH=1` works
too). New shells pick it up. Every change is printed:

```console
$ cd ~/src/app
jkit: using ~/src/app/jkit.toml: jdk 21.0.13.1+2 temurin
  set    JKIT_PROJECT=~/src/app/jkit.toml:1795536822587
  set    JKIT_PROJECT_JDK=~/.jkit/tools/jdk/21.0.13.1+2-temurin
  set    JAVA_HOME=~/.jkit/tools/jdk/21.0.13.1+2-temurin
  PATH   + ~/.jkit/tools/jdk/21.0.13.1+2-temurin/bin
$ cd ~
jkit: left the project in ~/src/app: back to the global versions
  unset  JKIT_PROJECT
  unset  JKIT_PROJECT_JDK
  set    JAVA_HOME=~/.jkit/global/jdk
  PATH   - ~/.jkit/tools/jdk/21.0.13.1+2-temurin/bin
```

This works in bash, zsh and fish. PowerShell doesn't switch on `cd` yet; run `jkit sync` there
to switch by hand.

What to know:

- **Entering a project never downloads anything.** A pinned version that isn't installed stays
  on its shell or global choice, with a note to run `jkit sync`.
- **Editing the project file** takes effect on the next `cd`, `jkit sync` or `jkit pin`, not
  while you sit in the folder.
- **Without auto-switch**, `jkit sync` (and `jkit pin`) still switch the current shell; you
  run it yourself.
- **A broken project file** (bad TOML) is reported once, and your versions stay as they were.
- **To stop switching in a shell that's already open**, `export JKIT_AUTO_SWITCH=0` there; a
  change to `config.toml` applies to new shells.
- **A `JAVA_HOME` you set yourself** is kept in `JKIT_USER_JDK` while the project applies, and
  comes back when you leave. See [Shell hook](/reference/shell-hook/#a-java_home-you-set-yourself).

### The bash one-liner caveat

bash switches when it draws the next prompt, so a `cd` and a command on one line run with the
old versions:

```bash
cd ~/src/app && java -version          # still the JDK you had before the cd
```

zsh and fish switch on the `cd` itself. In scripts and one-liners, don't rely on switching at
all; use `jkit exec`, which reads the project file for the folder it runs in:

```bash
jkit exec --dir ~/src/app -- java -version
```

## Which version wins

Per tool, from highest to lowest:

1. A `jkit use` in this shell (without `--global`).
2. The project file.
3. The global default (`jkit use … --global`).

So a project that pins only `jdk` leaves Maven and Gradle on their shell or global choice. A
`jkit use` made before you `cd` into a project keeps winning there, and the hook says so:

```console
$ jkit use jdk@17 zulu
$ cd ~/src/app
jkit: using ~/src/app/jkit.toml: jdk 21.0.13.1+2 temurin
  this shell chose its own jdk with jkit use, which wins over jkit.toml: jkit reset jdk to follow jkit.toml
```

`jkit reset jdk` drops the shell's choice and the project's version applies. `jkit status` shows
the same thing at any time:

```console
$ jkit status
jdk      17.0.21.1+2 zulu        shell
         jkit.toml wants jdk 21; this shell chose another with jkit use, to follow it: jkit reset jdk
maven    3.9.16                  global
project: ~/src/app (jkit.toml)
```

`jkit use jdk@… --global` inside a project changes the global default, but the project's pin
still wins in that project; jkit notes that when you run it.

## Which file applies

jkit starts in the current folder and walks up. The first folder that has a project file is the
project:

- In that folder, `jkit.toml` wins. `.jkit.toml` is the same format, for those who want it
  hidden; if both are there, `jkit.toml` wins.
- With no `jkit.toml`, jkit reads `.sdkmanrc`, `.tool-versions` and `.java-version` together,
  the earlier one in that list winning per tool.
- Below your home folder, the search stops at `$HOME`, which is still checked, so a
  `~/jkit.toml` acts as a personal default for every folder under home that has no project file
  of its own. Outside home (a CI workspace, `/srv/...`), it goes up to `/`.

### Nested projects

Files from different folders are never merged. A `jkit.toml` in a subfolder replaces the outer
one completely: if the outer file pins `jdk` and `maven` and the inner one pins only `maven`,
the inner project gets the global JDK, not the outer project's.

In a monorepo where a subproject needs something different, give it its own file, and repeat
whatever it shares with the outer one:

```bash
cd services/legacy
jkit pin jdk@17 --here         # creates services/legacy/jkit.toml, even though one is further up
```

`--here` (on `pin` and `unpin`) uses the `jkit.toml` in the current folder instead of the nearest
one above it.

## Files from other tools

jkit reads these so existing repositories work unchanged. It never writes them:

| File | Example | Read as |
|---|---|---|
| `.sdkmanrc` (SDKMAN) | `java=21.0.5-tem`, `maven=3.9.9` | jdk 21.0.5 temurin, maven 3.9.9 |
| `.tool-versions` (asdf, mise) | `java temurin-21.0.5+11.0.LTS`, `gradle 8.10` | jdk 21.0.5+11.0.LTS temurin, gradle 8.10 |
| `.java-version` (jEnv and others) | `17`, `1.8`, `temurin64-21.0.5` | jdk 17 (any vendor), jdk 8, jdk 21.0.5 temurin |

SDKMAN's vendor suffixes, asdf's vendor names and jEnv's names are mapped to jkit's; tools jkit
doesn't manage (kotlin, node, …) are skipped with one warning. When a folder has several of these
files, the first one in the table that mentions a tool decides it, even when jkit can't read that
entry: `jkit sync` then fails, installing nothing, rather than take a later file's pin. The full
mapping is in [Project files](/reference/project-files/), and [Migrating](/guides/migrating/)
covers moving off those tools.

`jkit pin` won't edit these files. When the project file it finds is one of them, it stops and
suggests `--here`, which creates a `jkit.toml` in the current folder; that `jkit.toml` then wins over the other files
for jkit (but not for SDKMAN or asdf, which keep reading their own).

## Exact pins for reproducible builds

`jdk = "21"` means "any 21, newest installed", so two machines can build with 21.0.5 and 21.0.9,
from different vendors. That's usually what you want. When the exact build matters, pin it:

```console
$ jkit pin jdk@21 temurin --exact
$ cat jkit.toml
# Versions jkit uses in this project: jkit sync installs them.
[tools]
jdk = { version = "21.0.13.1+2", vendor = "temurin" }
```

`--exact` writes the full version and vendor of the build it resolves to. There is no separate
lock file: the exact version in `jkit.toml` is the lock. Everyone who runs `jkit sync` gets that
build, and `jkit upgrade` doesn't move the project (it installs newer builds beside it).

A pin without a vendor prefers your default vendor and otherwise takes the newest match from any
vendor, so the file works on machines with different defaults. `jkit sync` installs a missing
version from your default vendor. Name a vendor when it matters.

## Permissions: when a project file is ignored

A project file can change which programs run in your shell, so jkit ignores one it can't trust,
the way git ignores a repository owned by someone else:

- The folder belongs to another user (other than root), or every user can write to it, such as
  `/tmp`. The search stops there: someone else's `/tmp/jkit.toml` must not change your versions.
- The file itself is owned by another user or is world-writable.
- The file is a symbolic link to something outside its folder.

`jkit status`, `jkit sync` and `jkit doctor` say when they ignored a file, and why:

```console
$ jkit status
...
project: none (ignoring /mnt/c/src/app/jkit.toml: every user can write to the folder)
```

This often happens on WSL's `/mnt/c`, SMB shares and Docker bind mounts, where every file looks
world-writable. Fix the mount options or permissions (for a folder you own, `chmod o-w` on it),
or name the file directly with `JKIT_PROJECT_FILE`. See [WSL](/guides/wsl/) and
[Shared machines](/guides/shared-machines/).

Two more limits: project files over 256 KiB are refused, and `jkit pin` won't write through a
`jkit.toml` that is a symbolic link.

## Naming the file: JKIT_PROJECT_FILE

`JKIT_PROJECT_FILE` overrides the search:

```bash
export JKIT_PROJECT_FILE=/work/ci/jkit.toml     # use this file, wherever you are
export JKIT_PROJECT_FILE=ci/jkit.toml           # relative: looked up from each folder
export JKIT_PROJECT_FILE=                       # set but empty: no project files at all
```

- An **absolute path** is used as is, and skips the ownership checks above: you chose it.
- A **relative path** is looked up from the current folder each time, applies only where that
  file exists, and gets the same checks as a normal search.
- **Set but empty** turns project files off, for when you want only the global versions.

`jkit pin` writes to the file `JKIT_PROJECT_FILE` names (creating it if it ends in `.toml`).
For one command, `jkit exec --no-project` and `jkit env --no-project` ignore project files too.
