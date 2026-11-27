---
title: jkit vs OS packages and jEnv
description: How jkit compares with JDKs from apt, dnf and Homebrew, update-alternatives, and jEnv, with command translations.
sidebar:
  order: 5
  label: vs OS packages and jEnv
---

Many people get Java from their operating system's package manager, and some add
[jEnv](https://www.jenv.be) to switch between the JDKs installed that way. This page compares both
with jkit, for anyone using them today. It describes them as their documentation stood in
November 2026.

## What OS packages do well

- **Updates with the rest of the system.** `apt upgrade` or `dnf upgrade` brings JDK security
  fixes along with everything else, from your distribution's signed repositories.
- **Nothing extra to install or trust.** The JDK comes from the same place as the rest of your
  system, and other packages can depend on it.
- **Right for servers.** On a machine built by Ansible, Puppet or a Dockerfile, running one Java
  application on one version, the distribution's package is usually the simplest choice.

Homebrew offers both `openjdk@21`-style formulas and casks for Temurin, Corretto and Zulu.

## What jEnv does well

jEnv doesn't install Java. It switches between JDKs you've already installed (with a package
manager, say) and registered with `jenv add`. It can set the version globally, per directory with
a `.java-version` file, or per shell, and its `export` plugin sets `JAVA_HOME`. It's small and
does that job well on macOS and Linux.

## Side by side

| apt (Debian, Ubuntu) | dnf (Fedora, RHEL) | Homebrew | jkit |
| --- | --- | --- | --- |
| `apt search openjdk` | `dnf search openjdk` | `brew search openjdk` | `jkit search jdk` |
| `sudo apt install openjdk-21-jdk` | `sudo dnf install java-21-openjdk-devel` | `brew install openjdk@21` | `jkit install jdk@21` |
| `sudo update-alternatives --config java` | `sudo alternatives --config java` | `sudo ln -sfn …/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-21.jdk` | `jkit use jdk@21 --global` |
| `sudo apt upgrade` | `sudo dnf upgrade` | `brew upgrade` | `jkit upgrade` |
| `sudo apt remove openjdk-17-jdk` | `sudo dnf remove java-17-openjdk-devel` | `brew uninstall openjdk@17` | `jkit remove jdk@17 --all` |

Package names vary between distributions and releases, and each release ships only some Java
versions. Homebrew's versioned `openjdk` formulas aren't linked into your `PATH`; Homebrew prints
the `ln` command above for the macOS Java wrappers to find them.

| jEnv | jkit |
| --- | --- |
| `jenv add /usr/lib/jvm/java-21-openjdk-amd64` | not needed; `jkit install jdk@21` |
| `jenv versions` | `jkit list` |
| `jenv global 21` | `jkit use jdk@21 --global` |
| `jenv local 17` (writes `.java-version`) | `jkit pin jdk@17` (writes `jkit.toml`) |
| `jenv shell 17` | `jkit use jdk@17` |
| `jenv enable-plugin export` | not needed; the hook sets `JAVA_HOME` |
| `jenv version` | `jkit status` |

## The differences that matter

- **Switching is per user, not per system.** `update-alternatives` changes `java` for every user
  on the machine, needs root, and switches `java`, `javac` and the other commands as separate
  entries (Debian and Ubuntu add `update-java-alternatives` to do them together). It doesn't set
  `JAVA_HOME`. jkit switches per shell, per project or for your user, without root, and sets
  `JAVA_HOME` to match.
- **Versions and vendors.** A distribution release usually ships a few OpenJDK versions, built by
  the distribution, and updates them on its own schedule. jkit installs any version from 20+
  vendors as soon as the foojay Disco API lists it, and keeps several builds of one version side
  by side.
- **Maven, Gradle and sbt.** Distribution packages of these are often older than upstream, or
  missing. jkit installs the upstream releases and switches them like JDKs.
- **jEnv needs JDKs from somewhere else.** jkit installs and switches in one tool, so there's no
  `jenv add` step after each new JDK. jEnv's shims choose the version when a command runs; jkit
  puts the real JDK on `PATH` and sets `JAVA_HOME` from the same choice. jEnv's docs cover macOS
  and Linux with bash and zsh (fish untested); jkit also runs on Windows, as a preview.
- **Verification.** OS packages are signed by the distribution, which is a strong check; keep
  using them where that matters most. jkit checks every download against its published checksum,
  refuses one with none unless you pass `--allow-unverified`, and checks the publisher's
  signature for Temurin, Microsoft and Corretto JDKs, Maven 3.9.0 and later, and sbt. jkit's own
  `.deb` and `.rpm` are signed too, with the key of the Vitrina apt repository.

## Using both

jkit and OS packages can share a machine. jkit's hook puts its JDKs first on `PATH`, so the
system's `java` stays for system services while your shells use jkit's. Remove any `JAVA_HOME`
you set in your profile, or it wins over jkit's; `jkit doctor` warns about it.

jkit reads `.java-version` files, so a repository set up with `jenv local` works with `jkit sync`
unchanged. A plain version means any vendor (`1.8` reads as 8), and jEnv's names pin the vendor
they name (`temurin64-21.0.5`, `corretto64-21`); `openjdk64-17.0.2` means 17.0.2 from any vendor.
jEnv's Oracle JDK 8 and 11 names are skipped with a warning, since Oracle offers those only after
a sign-in. See [migrating](/guides/migrating/).
