---
title: WSL
description: Run the Linux jkit inside the Windows Subsystem for Linux, keep projects in the Linux file system, and connect Windows IDEs to WSL JDKs.
sidebar:
  order: 18
---

This page is for Windows users who build Java projects inside WSL (the Windows Subsystem for
Linux). Inside WSL you use the **Linux** jkit, with the Linux shell hook, `cd` switching and
everything else from the Linux docs. It is a separate install from a Windows jkit, with its own
JDKs.

## Install

Inside your WSL distribution, install jkit as on any Linux:

```bash
sudo apt install ./jkit_*_amd64.deb      # Ubuntu or Debian, from the releases page
# or Homebrew on Linux, the .rpm, or the .tar.gz; see Install
jkit hook --install
```

See [Install](/start/install/) for every option, including the apt repository. The Linux build
needs glibc 2.34 or newer: Ubuntu 22.04 or later, or Debian 12 or later. WSL's default Ubuntu is
recent enough; an older distribution image is not.

JDKs then go to `~/.jkit` in your Linux home (for example `/home/you/.jkit`), and the hook goes
into `~/.bashrc` (or `~/.zshrc`, or fish's config).

## Keep projects in the Linux file system

Clone and build projects under your Linux home (`~/src/app`), not under `/mnt/c/...`:

- **Builds are much faster.** Files under `/mnt/c` cross from Linux to Windows on every access.
- **Project files work.** Windows drives under `/mnt/c` are usually mounted so that every file
  looks writable by every user. jkit ignores a project file (`jkit.toml`, `.sdkmanrc`,
  `.tool-versions`, `.java-version`) that every user can write to, or that sits in such a
  folder, so that someone else's file can't choose which programs you run. You see a note from
  `jkit status`, `jkit sync` or `jkit doctor` that names the file and the reason, and the global
  versions apply instead of the pinned ones.

If a project has to stay on the Windows drive, either:

- name the project file yourself, which skips the checks:

  ```bash
  export JKIT_PROJECT_FILE=/mnt/c/Users/you/src/app/jkit.toml
  ```

- or mount Windows drives with real permissions. In `/etc/wsl.conf`:

  ```ini title="/etc/wsl.conf"
  [automount]
  options = "metadata,umask=22,fmask=11"
  ```

  Then run `wsl --shutdown` from Windows and open WSL again. With `metadata`, files under
  `/mnt/c` keep Linux permissions, so they no longer look world-writable. Check the result with
  `jkit status`.

Keep `JKIT_HOME` in the Linux file system too. jkit refuses a home inside a folder that every
user can write to, which is how `/mnt/c` usually looks.

## Windows IDEs and WSL JDKs

The JDKs jkit installs in WSL are Linux programs. A Windows program can't run them as its JDK.

- **An IDE that works inside WSL** runs its Java tooling in Linux and can use the WSL JDKs:
  VS Code with the WSL extension (open the folder with `code .` from WSL), or IntelliJ IDEA's
  support for projects in WSL. Point it at the Linux path, such as `/home/you/.jkit/global/jdk`.
  See [IDEs](/guides/ides/).
- **An IDE that runs only on Windows** needs a Windows JDK. Install the Windows jkit as well
  (see [Windows](/guides/windows/)); it keeps its own JDKs in `%USERPROFILE%\.jkit`, separate from
  WSL's.

## Windows tools on the WSL PATH

By default WSL appends the Windows `PATH` to the Linux one, so a Windows `java.exe` may be
reachable from WSL as `java.exe`. It doesn't get in the way: the hook puts jkit's folders first
on `PATH`, and `java` (without `.exe`) is the Linux one. `jkit doctor` reports a `java` on `PATH`
that isn't jkit's.

## Two jkits, one machine

If you install jkit on both sides, they don't share anything: different binaries, different
homes, different JDKs (Linux builds in WSL, Windows builds on Windows), different hooks. Pin
versions in `jkit.toml` and both sides pick the same versions for a project.
