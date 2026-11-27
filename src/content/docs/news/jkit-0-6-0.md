---
title: "jkit 0.6.0: a Windows installer"
description: jkit 0.6.0 adds a Windows .msi that installs jkit just for you or for all users, puts it on PATH, and upgrades in place.
date: 2026-11-27T20:39:57Z
authors: adrian
tags: [release]
version: "0.6.0"
excerpt: A Windows installer that asks who jkit is for, puts it on PATH, and upgrades in place.
---

jkit 0.6.0 is out. Update with `jkit self-update`, or with your package manager.

## A Windows installer

Each release now has `jkit-<version>-windows-x64.msi` next to the `.zip`. Open it, and it asks
who jkit is for:

- **Just for you**: in `%LOCALAPPDATA%\Programs\jkit`, on your user `PATH`. No administrator
  rights needed.
- **All users**: in `%ProgramFiles%\jkit`, on the system `PATH`. It asks for administrator rights.
  Each user's JDKs and settings still go in their own `%USERPROFILE%\.jkit`.

It installs the same files as the `.zip`, the Visual C++ runtime included, so there's nothing else
to install first. Open a new terminal afterwards and `jkit --version` works.

Unattended, `msiexec /i jkit-0.6.0-windows-x64.msi /qn` installs it just for you, and
`ALLUSERS=1` installs it for all users. Uninstalling it in Settings → Apps takes jkit's folder off
`PATH` again and leaves `%USERPROFILE%\.jkit`. [Windows](/guides/windows/#install)

## Updating an installed jkit

A newer `.msi` upgrades jkit in place. Make the same choice as before: Windows Installer upgrades
an install only for the same users. `jkit self-update` on a jkit from the `.msi` prints a
PowerShell command that downloads the newer `.msi`, checks it against the release's signed
`SHA256SUMS`, and installs it for the same users without asking. With `--json`, `installedWith`
is `msi`. [More](/reference/commands/self-update/)

The `.msi` isn't Authenticode-signed, so SmartScreen may say "Windows protected your PC" when you
open it. Check it against `SHA256SUMS` or its build attestation first
([verify a download](/start/install/#verify-a-download)), then choose *More info* → *Run anyway*.

winget still installs the `.zip`, as before.

All changes are in [What's new](/whats-new/#060).
