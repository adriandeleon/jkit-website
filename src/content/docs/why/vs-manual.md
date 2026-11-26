---
title: jkit vs installing by hand
description: Installing JDKs by hand (download, verify, unpack, set JAVA_HOME and PATH) compared step by step with jkit.
sidebar:
  order: 2
  label: vs installing by hand
---

Plenty of developers install JDKs by hand: download an archive from a vendor, unpack it, and set
`JAVA_HOME` in their shell profile. This page compares that with jkit, step by step, for anyone
who does it this way today.

## What installing by hand does well

- Nothing new to learn or trust: you use the vendor's own site and your own shell.
- Full control over where each JDK lives and what your profile says.
- Works anywhere, including machines where you can't install other tools.

For one JDK that you update twice a year, it's a reasonable choice.

## Side by side

| Task | By hand | jkit |
| --- | --- | --- |
| Find a version | Browse the vendor's download page | `jkit search jdk` (all vendors), `jkit search jdk@21 zulu` |
| Download | Pick the right archive for your OS and CPU | `jkit install jdk@21` (picks it for you) |
| Verify | Fetch the checksum, run `sha256sum -c`; check the signature with `gpg` | Done on every install; refused if there's no checksum or signature to check |
| Unpack | `tar -xzf … -C ~/jdks` (macOS: the JDK is inside `Contents/Home`) | Done, into `~/.jkit/tools/jdk/<version>` |
| Make it the default | `export JAVA_HOME=…` and `export PATH="$JAVA_HOME/bin:$PATH"` in `~/.bashrc` | `jkit use jdk@21 --global` |
| Switch in one shell | Re-export `JAVA_HOME` and `PATH` by hand | `jkit use jdk@17` |
| Per project | Not really; a note in the README | `jkit pin jdk@21` writes `jkit.toml` |
| Update to a new build | Repeat everything, then edit the profile | `jkit upgrade` (the global default moves along) |
| Remove an old one | `rm -rf` the folder; check nothing points at it | `jkit remove jdk@17.0.12`, or `jkit prune` |
| Check what's active | `echo $JAVA_HOME; which java; java -version` | `jkit status` |

A typical manual install on Linux:

```bash
curl -LO https://…/OpenJDK21U-jdk_x64_linux_hotspot_21.0.9_10.tar.gz
curl -LO https://…/OpenJDK21U-jdk_x64_linux_hotspot_21.0.9_10.tar.gz.sha256.txt
sha256sum -c OpenJDK21U-jdk_x64_linux_hotspot_21.0.9_10.tar.gz.sha256.txt
mkdir -p ~/jdks && tar -xzf OpenJDK21U-jdk_x64_linux_hotspot_21.0.9_10.tar.gz -C ~/jdks
echo 'export JAVA_HOME="$HOME/jdks/jdk-21.0.9+10"' >> ~/.bashrc
echo 'export PATH="$JAVA_HOME/bin:$PATH"' >> ~/.bashrc
```

With jkit:

```bash
jkit install jdk@21       # downloads, checks the checksum and Adoptium's signature, and makes it the default
```

## The differences that matter

- **Keeping up.** Each JDK release line gets security updates every three months. By hand, each
  one means repeating the download, checks and profile edits. `jkit upgrade` installs the newest
  build of every release you have, and `jkit upgrade --dry-run` shows what it would change first.
- **`JAVA_HOME` and `PATH` agree.** A hand-edited profile easily ends up with `java` from one JDK
  and `JAVA_HOME` pointing at another. jkit sets both from the same choice, and `jkit doctor`
  warns when something else on `PATH`, or a `JAVA_HOME` set outside jkit, gets in the way.
- **Verification isn't optional.** It's easy to skip the checksum when you're in a hurry. jkit
  always checks it, and refuses downloads that have none unless you pass `--allow-unverified`.
- **More than one JDK.** Working on an old project on Java 11 and a new one on 21 means switching
  back and forth. jkit switches per shell and per project, and on `cd` if you turn that on.
- **Nothing hidden.** jkit doesn't take away the control: installs are ordinary folders under
  `~/.jkit/tools`, the global default is a link at `~/.jkit/global/jdk`, and every change is
  printed. You can point an IDE straight at those paths.

## Moving over

Your existing JDKs keep working; jkit doesn't touch them. Install the versions you use with jkit,
then remove the `JAVA_HOME` and `PATH` lines from your profile, so they don't override jkit's.
[Migrating](/guides/migrating/) has the steps.
