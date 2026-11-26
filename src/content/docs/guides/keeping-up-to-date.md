---
title: Keeping up to date
description: Get the newest builds of what you have installed with jkit upgrade, free disk space with jkit prune, update jkit itself, and refresh jkit's version lists.
sidebar:
  order: 8
---

This guide covers routine maintenance: security updates for your JDKs and build tools, cleaning
up old builds, and updating jkit itself. It's for anyone who has had jkit for a while; doing it
once a month is plenty for most people.

The short version:

```bash
jkit upgrade --dry-run     # what's new
jkit upgrade               # install it
jkit prune --dry-run       # what old builds and leftovers could go
jkit prune                 # remove them
jkit self-update           # jkit itself
```

## jkit upgrade

`jkit upgrade` installs the newest build of each **release line** you have: each tool and major
version, and for JDKs each vendor and JavaFX or not. It never moves you to another major version.

```console
$ jkit upgrade --dry-run
jdk@21 temurin is up to date (21.0.13.1+2)
jdk@17 zulu is up to date (17.0.21.1+2)
maven@3: 3.9.16 → 3.10.2
run without --dry-run to install
```

- JDK 21.0.5 goes to the newest 21 from the same vendor, never to 25, and never to another
  vendor. To move to a new major version, install it (`jkit install jdk@25`) and switch.
- For Maven, Gradle and sbt the line is the major version too, so Maven 3.9.x can move to 3.10.x,
  and Gradle 8.10 to 8.14.
- Early-access builds are skipped unless you add `--pre`.

Narrow it down:

```bash
jkit upgrade jdk                 # only JDKs
jkit upgrade jdk@21              # only the 21 line(s)
jkit upgrade jdk@21 temurin      # only Temurin 21
jkit upgrade maven
```

What changes:

- **The new build is installed next to the old one.** The old build stays until you
  `jkit prune` or `jkit remove` it, so anything pointing at it keeps working.
- **A global default moves along.** If `~/.jkit/global/jdk` pointed at the newest build of that
  line, it now points at the new one. A global default you deliberately set to an older build
  stays.
- **This shell doesn't change.** A shell that chose the old build with `jkit use` keeps it, and
  `jkit upgrade` prints the `jkit use` command to switch it.
- **Projects follow on their own** if they pin a prefix (`jdk = "21"` takes the newest installed
  21) with `jkit exec`, `jkit sync`, and the next time a shell enters the project. A project with an exact pin
  (`version = "21.0.9+10"`) stays on that build.

Each line is upgraded on its own. If one fails (a download error, a vendor with no build for
your platform), the others still go ahead, and `jkit upgrade` exits 1 at the end. Downloads are
checked as for `jkit install`; one with no published checksum, or whose publisher's signature
can't be checked (a Maven release by a new release manager, say), is refused unless you add
`--allow-unverified`. A newer jkit may know the new key, so run `jkit self-update` first.

`jkit install jdk@21` also installs the newest 21 build if you don't have it, but it doesn't move
the global link: that's the difference from `jkit upgrade`.

## jkit prune

`jkit prune` frees disk space. JDKs are a few hundred MB each, and `jkit upgrade` leaves the old
builds in place.

```console
$ jkit prune --dry-run
would remove jdk 17.0.13+11 zulu (323 MB; 17.0.21.1+2 is installed)
would free 323 MB; run without --dry-run to remove
$ jkit prune
removed jdk 17.0.13+11 zulu
  folder ~/.jkit/tools/jdk/17.0.13+11-zulu deleted
freed 323 MB
```

It removes:

- **Older builds in a release line once a newer one is installed**: 21.0.5 once 21.0.9 from the
  same vendor is in. The release line is the same as for `jkit upgrade` (tool, major version,
  vendor, JavaFX or not), so for Maven, 3.8.8 goes once a 3.9.x is installed.
- **Folders left by interrupted installs and removals.**
- **Cached version lists older than 30 days.** Newer ones stay, for `--offline`.

It keeps:

- The newest build of every line, so you never lose a version you had.
- The global default, whatever it points at.
- The build this shell uses (chosen with `jkit use`, or applied from a project).
- The build the project in the **current folder** pins.
- Early-access builds.

`jkit prune` only knows about the project in the folder you run it from. Another project with an
exact pin to a build it removed gets it back with `jkit sync` there (a download). Run
`--dry-run` first if that matters.

A build it can't remove (on Windows, one an IDE or a Gradle daemon holds open) is a warning, and
the rest are still removed; `freed` counts only what was.

To remove one particular build instead, use `jkit remove`:

```bash
jkit remove jdk@17.0.13 zulu
jkit remove jdk@17 zulu --all      # every Zulu 17
```

`jkit remove` refuses a build that is global or chosen in this shell unless you add `--force`,
and warns (but goes ahead) when the project here pins it.

## Updating jkit itself

```bash
jkit self-update --check     # only say whether a newer release is out
jkit self-update
```

What it does depends on how you installed jkit:

| Installed with | `jkit self-update` |
|---|---|
| Homebrew | prints `brew upgrade jkit` |
| apt (the Vitrina repository) | prints `sudo apt update && sudo apt install --only-upgrade jkit` |
| a `.deb` installed from a file | prints a `curl` command that downloads the new release's `.deb`, and `sudo apt install` for it |
| dnf (an `.rpm`) | prints the `sudo dnf install` command for the new release's `.rpm` |
| winget | prints `winget upgrade AdrianDeLeon.jkit` |
| a `.tar.gz` or `.zip` from the releases | replaces the binary in place |

For a package manager, it only prints the command, because replacing a file that a package
manager owns would confuse it. Run the command it prints.

For a binary from a release archive, it first checks that the release's `SHA256SUMS` is signed by
jkit's release key, which is built into jkit; a release without a good signature is refused. It
then downloads the archive for your platform, checks it against that `SHA256SUMS`, checks that the new binary runs (`jkit --version`), and only then
replaces the old one. If any step fails, nothing changes. It needs write access to the folder jkit
is in: run it as the user who owns that folder, or move jkit to a folder on `PATH` that you own.
On Windows the old binary is kept as `jkit.exe.old` until the next `self-update` deletes it.

`jkit self-update` only moves to full releases. Pre-releases are on the
[releases page](https://github.com/adriandeleon/jkit/releases/latest) only.

## Version lists and the cache

`jkit search`, `install`, `upgrade`, `pin` and `sync` ask the download sources what versions
exist, and cache the answers in `~/.jkit/index/` for 24 hours. So a build published an hour ago
may not show up yet. To ask again:

```bash
jkit search jdk@21 --refresh
jkit upgrade --refresh
```

The other direction:

- `--offline` (or `JKIT_NETWORK_OFFLINE=1`) uses only the cache and never contacts the network.
  Commands that need to download still fail, with a message saying so.
- When a source can't be reached, jkit falls back to the cached list, however old, and says how
  old it is.
- `jkit doctor` shows how old the newest cached list is.

`jkit prune` deletes cached lists older than 30 days.
