---
title: jkit self-update
description: Update jkit itself to the newest release, or print the upgrade command when Homebrew, apt, dnf, winget or the Windows .msi installed it.
sidebar:
  order: 17
---

Updates jkit itself to the newest release. A jkit from Homebrew, apt, dnf or winget is updated by
that tool, and one from the Windows `.msi` by a newer `.msi`; this tells you the command.

## Usage

```text
jkit self-update [-hV] [--check] [--json]
```

## What it does

1. **Finds the newest release** by reading the `SHA256SUMS` file of the latest release on GitHub
   (always fetched fresh). Pre-releases are on the releases page only: self-update never moves a
   release onto a pre-release, even one marked as the latest release.
2. **Works out how this jkit was installed**, from where the binary is:

   | Installed with | How jkit tells | Update |
   | --- | --- | --- |
   | Homebrew | the binary is in a `Cellar/jkit/` folder (or under `.linuxbrew`) | `brew upgrade jkit` |
   | apt (`.deb`) | `/usr/bin/jkit`, dpkg lists the `jkit` package, and the Vitrina apt repository is set up | `sudo apt update && sudo apt install --only-upgrade jkit` |
   | a `.deb` installed from a file | `/usr/bin/jkit`, dpkg lists the `jkit` package, no Vitrina repository | `d=$(mktemp -d) && curl -fLo "$d/<file>" <URL> && echo '<sha256>  '"$d/<file>" \| sha256sum -c - && sudo apt install "$d/<file>"` |
   | dnf (`.rpm`) | `/usr/bin/jkit` on a system with an rpm database | the same, ending in `sudo dnf install "$d/<file>"` |
   | winget | the binary is in winget's `Packages` folder | `winget upgrade AdrianDeLeon.jkit` |
   | the Windows `.msi` | `INSTALLER.txt`, which the `.msi` installs, is next to `jkit.exe`; just for you if that's in your `AppData`, else for all users | a PowerShell command: `$f = "$env:TEMP\<file>"; Invoke-WebRequest -UseBasicParsing -OutFile $f <URL>; if ((Get-FileHash $f).Hash -eq '<sha256>') { msiexec /i $f /passive \| Out-Null } else { Write-Error … }`, with `ALLUSERS=1` after `/passive` for an all-users install |
   | a release archive | anything else | jkit replaces itself |

3. **If it's already the newest**, it says so.
4. **For a package manager's copy**, it prints the command above and changes nothing. For an
   `.rpm` or a `.deb` installed from a file, it first checks the release's `SHA256SUMS.asc`, and the
   command it prints downloads the package into a private `mktemp -d` folder and checks it against
   that signed `SHA256SUMS` before `sudo dnf install` or `sudo apt install`, since dnf and apt
   don't check the signature of a package given as a file or URL. The `.msi` gets the same
   check: the PowerShell command runs `msiexec` only if `Get-FileHash` matches the signed
   `SHA256SUMS`, and installs for the same users the `.msi` was installed for, since Windows
   Installer upgrades an install only for the same users.
5. **For a copy from a release archive**, it:
   1. checks that `SHA256SUMS` is signed by jkit's release key, built into jkit, using the
      release's `SHA256SUMS.asc`; a release without a good signature is refused, and nothing
      changes;
   2. downloads the `.tar.gz` (`.zip` on Windows) for your platform into `~/.jkit/tmp/`;
   3. checks it against the release's `SHA256SUMS`;
   4. unpacks it and runs the new binary's `jkit --version`, which must report the new version;
   5. copies it next to the old binary and renames it over the old one in one step, keeping the
      old file's permissions.

   On Linux, a musl system (Alpine) gets the static `linux-x64-musl` build. If any step fails, the
   old binary is untouched. Windows can't replace a running `jkit.exe`, so there the old one is
   renamed to `jkit.exe.old` (or `jkit.exe.1.old`, and so on, when an older jkit that is still
   running holds that one), and the next self-update deletes them all. The Visual C++ runtime DLLs
   next to `jkit.exe` (`VCRUNTIME140.dll`, `VCRUNTIME140_1.dll`) are replaced when they are there,
   and never added to a folder without them.

   Self-update doesn't hold the install lock while it downloads and tests the new binary, so
   installs, `remove` and `prune` elsewhere don't wait for it.

`SHA256SUMS` comes from the same place as the archive, so on its own it would catch a broken or
altered download, not a compromised release. Its signature covers that: someone who could replace
the files on the releases page couldn't sign them. Releases before 0.4.0 have no
`SHA256SUMS.asc`. Only replacing the binary and the `.rpm` or `.deb` file command need the
signature: `--check`, and the upgrade command printed for Homebrew, apt and winget, download
nothing. To check a release by hand, see
[Verifying a jkit release](/reference/security/#verifying-a-jkit-release).

You need write access to the folder holding jkit. There is no arm64 musl build. A jkit run on a
JVM (a development build) can't update itself.

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
| Up to date | `jkit 0.6.0 is the newest release` |
| Ahead of the newest release (a pre-release or development build) | `jkit 0.7.0-rc.1 is up to date: the newest release is 0.6.0` |
| The newest is a pre-release, this isn't one | `jkit 0.6.0 is up to date: the newest release, 0.7.0-rc.1, is a pre-release, which self-update installs only over a pre-release` |
| `--check`, newer out | `jkit 0.6.1 is out (this is 0.6.0)`, then the command that updates it |
| Package manager | `jkit 0.6.1 is out (this is 0.6.0); jkit came from Homebrew, so update it there:` and the command |
| The Windows `.msi` | `jkit 0.6.1 is out (this is 0.6.0); jkit came from the .msi, just for you, so a newer .msi updates it (in PowerShell):` and the command (`for all users` for an all-users install) |
| Release archive | `updated jkit 0.6.0 → 0.6.1 at ~/.local/bin/jkit`, and the file changed |

## Exit codes

| Code | When |
| --- | --- |
| `0` | Up to date, updated, or (package manager, `--check`) told you how. |
| `1` | GitHub couldn't be reached, `SHA256SUMS` isn't signed by jkit's release key (also for an `.rpm` or `.deb` installed from a file), the download failed its checksum, the new binary didn't run, the folder isn't writable, no build for this platform, or jkit runs on a JVM. |

## JSON

`--json` prints `current`, `latest`, `status` (`up-to-date`, `available`, `updated` or `managed`),
`installedWith`, the `binary` path and, for a package manager, the `command`. See
[`jkit self-update` in JSON output](/reference/json/#jkit-self-update).

## See also

- [Keeping up to date](/guides/keeping-up-to-date/)
- [Install](/start/install/)
- [Security](/reference/security/#jkit-self-update)
