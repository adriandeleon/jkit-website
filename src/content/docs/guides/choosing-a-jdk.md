---
title: Choosing a JDK
description: Which Java version and which vendor to install, what LTS means, how @lts and @latest resolve, and how to set your default vendor.
sidebar:
  order: 5
---

This guide helps you decide which JDK to install: which Java version, and which vendor's build of
it. It's for newcomers who see twenty vendors in `jkit search jdk` and wonder which one, and for
anyone setting a team default. If you want something that works without choosing, run
`jkit install jdk@lts` and skip the rest.

## Versions: LTS and feature releases

A new Java version (a **feature release**: 23, 24, 25, …) comes out every six months, in March
and September. Most vendors update a feature release only until the next one is out, so after six
months it stops getting security fixes.

Every two years one release is a **long-term-support (LTS)** release: currently 8, 11, 17, 21 and
25. Vendors keep fixing those for years. `jkit search jdk` underlines them:

```console
$ jkit search jdk --maintained
                  maintained
                  27 25 21 17 11 8 7
                     ‾‾ ‾‾ ‾‾ ‾‾ ‾ ‾
temurin *          ✓  ✓  ✓  ✓  ✓ ✓
corretto           ✓  ✓  ✓  ✓  ✓ ✓
microsoft             ✓  ✓  ✓  ✓
zulu               ✓  ✓  ✓  ✓  ✓ ✓ ✓
...
✓ available   ● installed   ‾‾ LTS   * default vendor
```

Which to pick:

- **For applications and new projects: the newest LTS** (25 at the time of writing), or the LTS
  your framework or servers require. This is what `jdk@lts` gives you.
- **To try new language features: the newest feature release** (`jdk@latest`, the default when
  you leave the version out). Plan to move to the next one every six months.
- **For an existing project: whatever it builds with.** If it has a `jkit.toml`, `.sdkmanrc`,
  `.tool-versions` or `.java-version`, `jkit sync` installs that.

Newer JDKs run code compiled for older ones, so having 25 installed doesn't stop you building a
project that targets 17; but a build tool may need a JDK it supports (see
[Build tools](/guides/build-tools/)).

## Writing versions

```bash
jkit install jdk@lts           # the newest LTS release (online: the newest GA LTS foojay lists)
jkit install jdk@lts zulu      # Zulu's newest LTS release
jkit install jdk               # the newest release; same as jdk@latest
jkit install jdk@21            # the newest 21 build
jkit install jdk@21.0.5        # the newest 21.0.5 build
```

With a vendor, `@lts` is that vendor's newest LTS release, not the newest LTS of any vendor
(the same for `pin`, and `search` with a vendor). The version is a prefix: `21` matches every 21
build and jkit takes the newest. Where jkit only
looks at what's installed (`use`, `exec`, `remove`, `upgrade`), `@lts` means the newest LTS
release you have installed.

Search flags:

```bash
jkit search jdk                 # a grid of vendors × releases, maintained and end of life
jkit search jdk --maintained    # hide versions that are no longer maintained
jkit search jdk --pre           # include early-access builds of the next release
jkit search jdk@21              # each vendor's newest 21 build
jkit search jdk@21 zulu         # every Zulu 21 build
jkit search jdk --fx            # builds with JavaFX
```

Early-access builds are installed only when you ask: `jkit install jdk@28 --pre`. Version lists
are cached for 24 hours; `--refresh` fetches them again. More detail in
[Versions and vendors](/reference/versions-and-vendors/).

## Vendors

Java is open source (OpenJDK), and many companies build and ship it. For the same version, the
builds behave the same for almost every program; they differ in who supports them, how long and
how fast they publish fixes, which platforms they cover, and extras (JavaFX bundled, a different
VM, native-image). jkit gets the list from the [foojay](https://foojay.io) Disco API, so any
vendor it lists works under its foojay name.

| Name | Distribution | Notes |
|---|---|---|
| `temurin` | Eclipse Temurin (Adoptium) | jkit's default. Vendor-neutral, widely used. |
| `corretto` | Amazon Corretto | Common on AWS. |
| `zulu` | Azul Zulu | Many versions and platforms, also JavaFX builds. |
| `liberica` | BellSoft Liberica | Also JavaFX builds. |
| `microsoft` | Microsoft Build of OpenJDK | Common on Azure. |
| `oracle` | Oracle JDK | Check Oracle's license terms for your use. |
| `openjdk` | OpenJDK builds from jdk.java.net | Updated only while a release is current: its newest 21 is 21.0.2, which `jkit search` marks as behind. |
| `sapmachine` | SapMachine (SAP) | |
| `semeru` | IBM Semeru Runtime | Uses the OpenJ9 VM instead of HotSpot. |
| `semeru-certified` | IBM Semeru Runtime Certified Edition | |
| `dragonwell` | Alibaba Dragonwell | |
| `kona` | Tencent Kona | |
| `bisheng` | Huawei BiSheng JDK | |
| `jetbrains` | JetBrains Runtime | The runtime JetBrains IDEs ship. |
| `openlogic` | OpenLogic builds of OpenJDK | |
| `trava` | Trava OpenJDK | 8 and 11 only. |
| `graalvm-ce`, `graalvm`, `liberica-nik`, `mandrel` | GraalVM-based | Include native-image; see [GraalVM](/guides/graalvm/). |

`jkit search jdk` shows every vendor for your platform, and which releases each has. The list
changes as vendors come and go; what's shown there is what you can install.

Some vendors publish only the first updates of each release (openjdk from jdk.java.net, GraalVM
CE). `jkit search` marks a vendor's newest build that is two or more updates behind the newest
update at least two vendors have (for JDK 8, two quarterly releases), and `jkit install` warns
and says where the newest build is:

```console
$ jkit search jdk@21 openjdk
21.0.2+13  ↓ behind: newest 21 is 21.0.13.1+5
21.0.1+12
21+35
↓ behind: the vendor stopped updating this release; other vendors have newer builds
```

If you have no reason to choose, keep Temurin. Reasons to choose otherwise: your company has
support from a vendor, your servers or cloud run a particular one and you want to match it, or
you need something only some builds have (JavaFX, native-image, OpenJ9).

## Your default vendor

Everywhere you leave the vendor out, jkit uses your default vendor: Temurin, unless you change
it.

```console
$ jkit config set jdk.vendor zulu
set jdk.vendor = "zulu" in ~/.jkit/config.toml
  file   ~/.jkit/config.toml created
  jdk@… without a vendor now means zulu; installed JDKs and the global default are unchanged
$ jkit config
jdk.vendor         zulu        from ~/.jkit/config.toml
graalvm.vendor     graalvm-ce  default
shell.auto_switch  false       default
```

- `jkit config unset jdk.vendor` goes back to Temurin.
- `JKIT_JDK_VENDOR=corretto` sets it for one shell or CI job, and wins over `config.toml`.
- `jkit config set` checks the name against foojay's vendor list, so a typo is caught.
- Changing it doesn't touch what's installed or your global default; it changes what
  `jdk@21` means from now on (for `install`, `use`, `exec`, `remove`, and for project pins that
  name no vendor).

jkit never quietly swaps to another vendor. If yours has no build for a version on your platform,
the install stops and lists the vendors that do.

The default GraalVM vendor is a separate setting, `graalvm.vendor` (`graalvm-ce` by default).
