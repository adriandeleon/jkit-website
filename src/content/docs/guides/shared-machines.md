---
title: Shared machines
description: Where jkit keeps its files (JKIT_HOME), the ownership rules it enforces, root and sudo, and sharing one set of JDKs between users.
sidebar:
  order: 19
---

This page is for machines with more than one user (build servers, lab machines, shared
development hosts) and for anyone who wants to move jkit's folder. It explains `JKIT_HOME`, the
safety rules jkit applies to it, what happens with `sudo`, and how several users can share one
read-only set of JDKs.

## Each user has their own home by default

jkit keeps everything in `~/.jkit` of the user who runs it: installs, global links, cached
version lists and `config.toml`. Two users on one machine have two separate homes and download
their own JDKs. Nothing is shared, and nothing one user does changes the other's versions. jkit
needs no root rights for any of this.

## `JKIT_HOME`

`JKIT_HOME` moves the whole home:

```bash
export JKIT_HOME=/data/jkit      # in your shell startup file, before the jkit hook line
```

Set it before the jkit hook block in `~/.bashrc` (or `~/.zshrc`, or fish's config), since the
hook reads it when the shell starts. Without `JKIT_HOME`, jkit uses `$HOME/.jkit`.

## The rules jkit enforces

The jkit home holds the programs jkit puts first on your `PATH`, so jkit makes sure nobody else
can put programs there. It checks on every run, including the first one, before it creates
anything:

| Rule | Error when it's broken |
| --- | --- |
| `JKIT_HOME` is an absolute path | `JKIT_HOME must be an absolute path, not …` (hint: a quoted `~` is not expanded) |
| The home, and its `tools` and `global` folders, belong to you or to root | `… belongs to <user>, not to you` |
| None of them is writable by every user | `… is writable by every user, so anyone could put programs on your PATH` (hint: `chmod o-w …`) |
| No folder above the home is writable by every user, unless it has the sticky bit (like `/tmp`) | `… is in <folder>, which every user can write to, so anyone could replace it` |
| Every folder above the home belongs to you or to root | `… is in <folder>, which belongs to <user>, who could replace it` |

Symbolic links on the way are followed: what counts is where they lead. Each error comes with a
hint, usually to use a `JKIT_HOME` of your own such as `~/.jkit`.

Group-writable folders are accepted, because many Linux distributions give each user a private
group and a umask that makes new folders group-writable.

jkit creates a new home readable only by you (`rwx------`).

:::note
A home under `/tmp` (such as `/tmp/jkit-test`) passes the checks thanks to `/tmp`'s sticky bit, as
long as you created it. If another user made that folder first, it belongs to them, and jkit
refuses it.
:::

On Windows there are no POSIX owners, so none of these ownership checks apply there. The default
permissions of your user profile protect `%USERPROFILE%\.jkit`. `JKIT_HOME` must still be an
absolute path.

## Project files from other users

The same idea applies to project files. jkit ignores a `jkit.toml` (or `.sdkmanrc`,
`.tool-versions`, `.java-version`) that belongs to another user (other than root) or that every
user can write to, or that sits in such a folder. The search for a project file also stops at a
folder like that, so another user's `/tmp/jkit.toml` can't change your versions. `jkit status`,
`jkit sync` and `jkit doctor` say when they ignored a file, and why.

To use a checkout that belongs to someone else (a shared `/srv/app` owned by a deploy user, say),
name the file directly; an absolute `JKIT_PROJECT_FILE` is your own choice and skips the checks:

```bash
JKIT_PROJECT_FILE=/srv/app/jkit.toml jkit exec -- mvn -B verify
```

## Root and sudo

jkit never needs root. Run it as yourself.

- **`sudo jkit install …` installs for root, not for you.** Depending on how `sudo` is set up,
  `HOME` is either root's (the JDK goes to `/root/.jkit`, where you never see it) or yours (with
  `sudo -E`, for example). In the second case root writes into your `~/.jkit`, and the folders it
  makes belong to root. jkit accepts folders owned by root, so it won't warn, but your next
  install, `upgrade` or `remove` fails with a permission error. Fix it with:

  ```bash
  sudo chown -R "$(id -u)" ~/.jkit
  ```

- **Root's own shell** (`sudo -i`, `su -`) has root's startup files and root's `~/.jkit`. Add the
  hook there separately if root needs it.
- **Scripts run as root** (cron jobs, systemd units, provisioning): set `JKIT_HOME` explicitly, so
  the home doesn't depend on what `HOME` happens to be.

## Sharing one set of JDKs

To save disk space, or to give every user the same approved builds, an admin can install into one
home owned by root that users read but can't change.

As root (or with sudo), install into the shared home:

```bash
sudo env JKIT_HOME=/opt/jkit jkit install jdk@21
sudo env JKIT_HOME=/opt/jkit jkit install jdk@17
sudo env JKIT_HOME=/opt/jkit jkit install maven
sudo chmod 755 /opt/jkit         # jkit made it readable only by root
```

The first install of each tool becomes the global default for everyone using that home. Each
user then adds, before the jkit hook in their shell startup file:

```bash
export JKIT_HOME=/opt/jkit
```

What users can do with a home they can't write to:

- **Works:** `jkit list`, `status`, `env`, `exec`, the shell hook, `jkit use` in their own
  shell, and `jkit sync` when every pin is already installed.
- **Doesn't work:** `install`, `upgrade`, `remove`, `prune`, `use … --global`, `config set`, and
  `search` (which writes to the version cache). These need to write to the home, so they are for
  the admin.
- **`jkit doctor`** reports `can't write to /opt/jkit, so installs and jkit use --global will fail`
  as a failure and exits `1`. That's expected for a read-only home.
- **Personal settings** can't go in the shared `config.toml`; use the environment variables
  instead, such as `JKIT_JDK_VENDOR=zulu` or `JKIT_AUTO_SWITCH=1`. See
  [configuration](/reference/configuration/).

A user can't mix the shared home with installs of their own: jkit uses one home at a time. A user
who needs a version the admin hasn't installed can use their own `~/.jkit` (unset `JKIT_HOME`), or
ask the admin.

After an `upgrade` by the admin, the global links move to the new builds for everyone.

## Containers

In Docker images, jkit often runs as root at build time and as another user at run time. The same
rules apply; see [Docker](/guides/docker/) for `JKIT_HOME` in images and for bind-mounted project
files.
