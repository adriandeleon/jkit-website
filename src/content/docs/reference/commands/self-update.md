---
title: jkit self-update
description: Update jkit itself to the newest release, or print the upgrade command when Homebrew, apt, dnf or winget installed it.
sidebar:
  order: 17
---

Updates jkit itself to the newest release. A jkit from Homebrew, apt, dnf or winget is updated by
that tool, and this tells you the command.

## Usage

```text
jkit self-update [-hV] [--check] [--json]
```

## What it does

1. **Finds the newest release** by reading the `SHA256SUMS` file of the latest release on GitHub
   (always fetched fresh). Pre-releases are on the releases page only, never offered here.
2. **Works out how this jkit was installed**, from where the binary is:

   | Installed with | How jkit tells | Update |
   | --- | --- | --- |
   | Homebrew | the binary is in a `Cellar/jkit/` folder (or under `.linuxbrew`) | `brew upgrade jkit` |
   | apt (`.deb`) | `/usr/bin/jkit`, and dpkg lists the `jkit` package | `sudo apt update && sudo apt install --only-upgrade jkit` |
   | dnf (`.rpm`) | `/usr/bin/jkit` on a system with an rpm database | `sudo dnf install <the new release's .rpm URL>` |
   | winget | the binary is in winget's `Packages` folder | `winget upgrade AdrianDeLeon.jkit` |
   | a release archive | anything else | jkit replaces itself |

3. **If it's already the newest**, it says so.
4. **For a package manager's copy**, it prints the command above and changes nothing.
5. **For a copy from a release archive**, it:
   1. downloads the `.tar.gz` (`.zip` on Windows) for your platform into `~/.jkit/tmp/`;
   2. checks it against the release's `SHA256SUMS`;
   3. unpacks it and runs the new binary's `jkit --version`, which must report the new version;
   4. copies it next to the old binary and renames it over the old one in one step, keeping the
      old file's permissions.

   If any step fails, the old binary is untouched. Windows can't replace a running `jkit.exe`, so
   there the old one is renamed to `jkit.exe.old`, which the next self-update deletes.

`SHA256SUMS` comes from the same place as the archive, so the check catches a broken or altered
download, not a compromised release. To check a release's build provenance by hand, see
[Verifying a jkit release](/reference/security/#verifying-a-jkit-release).

You need write access to the folder holding jkit. There is no jkit build for musl Linux (Alpine).
A jkit run on a JVM (a development build) can't update itself.

## Options

| Option | Description |
| --- | --- |
| `--check` | Only say whether a newer release is out; change nothing. |
| `--json` | Print one JSON document on stdout instead of text. |
| `-h`, `--help` | Show help and exit. |
| `-V`, `--version` | Print jkit's version and exit. |

The download honours `HTTPS_PROXY` and `JKIT_NETWORK_PROXY`, and `JKIT_SOURCES_RELEASES_URL`
points it at a mirror of the releases page. See [Configuration](/reference/configuration/).

## Examples

```bash
jkit self-update --check     # is there a newer jkit?
jkit self-update             # update it, or print the command that does
```

What you see:

| Case | Output |
| --- | --- |
| Up to date | `jkit 0.4.0 is the newest release` |
| `--check`, newer out | `jkit 0.4.1 is out (this is 0.4.0)`, then the command that updates it |
| Package manager | `jkit 0.4.1 is out (this is 0.4.0); jkit came from Homebrew, so update it there:` and the command |
| Release archive | `updated jkit 0.4.0 → 0.4.1 at ~/.local/bin/jkit`, and the file changed |

## Exit codes

| Code | When |
| --- | --- |
| `0` | Up to date, updated, or (package manager, `--check`) told you how. |
| `1` | GitHub couldn't be reached, the download failed its checksum, the new binary didn't run, the folder isn't writable, no build for this platform, or jkit runs on a JVM. |

## JSON

`--json` prints `current`, `latest`, `status` (`up-to-date`, `available`, `updated` or `managed`),
`installedWith`, the `binary` path and, for a package manager, the `command`. See
[`jkit self-update` in JSON output](/reference/json/#jkit-self-update).

## See also

- [Keeping up to date](/guides/keeping-up-to-date/)
- [Install](/start/install/)
- [Security](/reference/security/#jkit-self-update)
