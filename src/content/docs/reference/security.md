---
title: Security
description: What jkit checks before it runs a download (HTTPS, checksums, OpenPGP signatures, archive safety), which project files it trusts, how it protects its home, how to verify a jkit release, and how to report a vulnerability.
---

jkit downloads JDKs, Maven, Gradle and sbt and puts them first on your `PATH`, so the trust it
places in each download matters. This page describes every check jkit makes, and its limits. It
follows jkit's [SECURITY.md](https://github.com/adriandeleon/jkit/blob/main/SECURITY.md).

## Summary

| Check | What it catches | Applies to |
| --- | --- | --- |
| HTTPS only | Tampering on the network | Every download, checksum and version list |
| Checksum | A broken or altered download | Every download whose source publishes one |
| OpenPGP signature | A download not built by its publisher, even from a compromised source | Temurin, Microsoft, Corretto, Maven 3.9.0+, sbt |
| Vendor download servers | A compromised foojay sending a JDK download somewhere else | Temurin, Microsoft, Corretto, Zulu, Liberica, SapMachine, Semeru, Oracle, GraalVM CE, Oracle GraalVM, Mandrel |
| Archive checks | Archives that try to escape their folder or exhaust the disk | Every download |
| Project-file trust | Another user choosing your JDK through a file in a shared folder | Every project file |
| Home checks | Another user putting programs on your `PATH` | `~/.jkit` |

## Transport

- Downloads, checksums and version lists are fetched over HTTPS only. The one exception is a
  mirror on this machine (`JKIT_SOURCES_*_URL` pointing at `http://localhost`), which is reached
  directly, never through `HTTPS_PROXY`.
- Every redirect is checked before it is followed: HTTPS stays HTTPS, and plain HTTP only goes to
  this machine.
- A mirror set with a `JKIT_SOURCES_*_URL` variable must be `https://` (or `http://` to this
  machine); anything else is refused. See [Configuration](/reference/configuration/#sources-mirrors).
- Download links from a remote source must be HTTPS. An HTTPS link may still name a host on your
  network: jkit only sends it a plain GET, and checks what comes back.

## Checksums

- When the source publishes a SHA-256, SHA-384 or SHA-512 checksum, jkit checks the download
  against it, and the install counts as verified.
- A checksum file named for one algorithm (`.sha512`) must hold a hash of that kind.
- That checksum comes from the same source as the download (for example the vendor's release site
  or the foojay API), so it catches corruption and tampering in transit or on a mirror, not a
  compromised source. Signatures cover that case, for the downloads that have them.
- A download whose only checksum is SHA-1 (or MD5, where the source lists one) is still checked,
  but installed marked unverified, with a warning. For Maven, jkit asks the repository for
  `.sha512` and, for releases older than 3.9.1, `.sha1`; a mirror that has only `.md5` counts as
  having no checksum.
- A download with no checksum at all is refused unless you pass `--allow-unverified`, or unless its
  publisher's signature checks out (Corretto, below). The installer itself refuses it, not only the
  commands that call it.
- Checksums are fetched fresh for every download, never from the cache.
- A build checked by its checksum alone says so (`checksum checked (sha256)`), as a signed one
  says `signature checked: …`.

A refused download says `… has no published checksum, so jkit can't check the download; nothing
installed`, followed by the same command with `--allow-unverified` added. `jkit sync --dry-run`
marks such a pin `would not install`.

`jkit list --json` shows how each install was checked: `verified` and `signedBy` in the
[install object](/reference/json/#the-install-object).

## Signatures

Some downloads are also checked against their publisher's detached OpenPGP signature, using keys
built into jkit. Each key was taken from where the publisher's own documentation points and
confirmed by verifying real downloads with it:

| Downloads | Signature | Key (primary fingerprint) |
| --- | --- | --- |
| Temurin 8u342, 11.0.16, 17.0.4, 18.0.2, 19 and later | `.sig` next to the download on Adoptium's GitHub releases | Adoptium `3B04 D753 C905 0D9A 5D34 3F39 843C 48A5 65F8 F04B` |
| Microsoft Build of OpenJDK | `.sig` next to the download on aka.ms | Microsoft `B602 4333 84B8 9913 0292 4D82 3553 1D31 5B21 C189` |
| Amazon Corretto 8u242, 11.0.6 and later (January 2020 on) | `.sig` next to the download on corretto.aws | Amazon `6DC3 636D AE53 4049 C8B9 4623 A122 542A B04F 24E3` |
| Apache Maven 3.9.0 and later | `.asc` next to the zip on Maven Central (or your mirror) | the release managers who signed them, from Maven's [KEYS](https://downloads.apache.org/maven/KEYS) file: Tamas Cservenak `29BE A2A6 45F2 D6CE D7FB 12E0 2B17 2E3E 1564 66E8` and `88BE 34F9 4BDB 2B53 5704 4E2E 3A38 7D43 9641 43E3`, Slawomir Jaranowski `8478 9D24 DF77 A324 33CE 1F07 9EB8 0E92 EB21 35B1`, Guillaume Nodet `EA23 DB13 60D9 0294 81E7 F2EF ECDF EA3C B449 3B94` |
| sbt | `.asc` on sbt's GitHub releases | sbt build tool `2EE0 EA64 E40A 89B8 4B2D F734 99E8 2A75 642A C823` (its subkey `05B3 70DC 1261 4519 62FF E35A CD4E AA59 06F9 BF46` signs 1.12.2 on) |

Keys are tied to what they sign: a Temurin download signed by Amazon's key, or a Maven zip signed
by sbt's, doesn't count. Unlike a checksum, a signature shows who built the file, so it catches a
compromised download source, mirror or foojay entry, not only tampering in transit.

Only a key that may sign counts: its self-signature (for a subkey, its binding signature) must
mark it for signing, it must be RSA of at least 2048 bits, EdDSA or ECDSA, and it must not have
expired when it signed. So Guillaume Nodet's DSA-1024 primary key, which is in Maven's key file
and signed his releases before 3.9.0, isn't accepted; his signing subkeys are.

```console
$ jkit install jdk@21
downloading jdk 21.0.13.1+2 temurin from github.com
installed jdk 21.0.13.1+2 temurin → ~/.jkit/tools/jdk/21.0.13.1+2-temurin
  signature checked: signed by Adoptium (key 843C48A565F8F04B)
```

What happens in each case:

- **A good signature** by the publisher's key: the install goes ahead, and records who signed it
  (`signedBy`).
- **A signature by the publisher's key that doesn't match** the download stops the install.
  Nothing is installed.
- **A signature jkit can't fetch, read, or tie to the publisher's keys** (a new key, a weak hash,
  a key not marked for signing, a DSA or short RSA key, a key that had expired when it signed)
  also stops the install. The checksum usually comes from the same server as the download, so
  whoever can replace one can replace both, and a missing signature is exactly what they would
  serve. `--allow-unverified` installs it anyway, on the checksum alone, marked unverified, and
  says why. Expect this most from Maven: each release is signed by whichever Apache release
  manager cut it, so a release by someone new is refused until a jkit release adds their key
  (`jkit self-update`). A Maven mirror (`JKIT_SOURCES_MAVEN_URL`) that doesn't serve the `.asc`
  files is refused too. jkit pins the keys rather than fetching the KEYS file, because a file
  fetched at install time is only as trustworthy as the server it comes from.
- **Temurin, Microsoft and Corretto** builds are always checked against the `.sig` the vendor
  publishes next to the download, whether or not foojay lists one; jkit doesn't read the signature
  link foojay lists, if any (foojay leaves it out for some builds that have one). A download link
  of these vendors with no file name, or one in its query string, is refused rather than installed
  without a signature.
- **Where there is a checksum**, the signature is checked as well, never instead of it.
- **Corretto** publishes no per-version checksum (foojay lists none, and corretto.aws has only one
  for the latest build), so for Corretto the signature is the check: a good signature by Amazon's
  key installs it, counted as verified.
- **A signed build must be the version asked for.** A signature covers the file, not its name, so
  any build the publisher signed would pass, an older one included. After a good signature jkit
  checks that the unpacked tool is the version it asked for: every part of a JDK's version in its
  `release` file (`version.txt` for Corretto 8, which has no `release` file), Maven's
  `lib/maven-core-<version>.jar`, and the version sbt's launcher (`bin/sbt-launch.jar`) starts by
  default.
- **Amazon's 2019 Corretto builds** (8u222, 8u232, 11.0.4, 11.0.5), which foojay still lists, were
  each signed with a key of its own, since retired (SHA-1 signatures, keys that expired in 2024),
  and have no checksum. jkit says so, and installs one only with `--allow-unverified`. `jkit search`
  marks them ○, as it does builds with nothing to check.
- A signature file is read only up to 64 KB; a link that serves more is cut off there.

Not checked:

- Maven releases before 3.9.0: they were signed with SHA-1, DSA-1024 keys, or keys that have since
  expired.
- Temurin builds from before Adoptium started signing (July 2022: before 8u342, 11.0.16, 17.0.4,
  18.0.2). jkit installs these on their checksum and says only the checksum was checked.
- Other JDK vendors: they publish no signatures, or none with a key jkit has.
- Gradle, which publishes none.

## foojay

JDK builds are found through the [foojay](https://foojay.io) API, and jkit trusts it as little as
it can:

- **Only the vendor and major release you asked for.** jkit takes only builds foojay lists under
  that vendor and major version, so an answer can't call a Temurin build "Zulu", or a Temurin 21
  build 17.0.3, to have it installed without the signature check.
- **Vendor names.** Vendor names are checked against foojay's list of distributions, but foojay's
  synonyms can only add names: Temurin (and `adoptium`), Microsoft and Corretto are known without
  asking foojay, a distribution's own name or a name jkit reads as an alias always means that
  vendor, and a synonym two distributions claim is ignored.
- **The vendor's own server.** A build of Temurin, Microsoft, Corretto, Zulu, Liberica, SapMachine,
  Semeru, Oracle, GraalVM CE, Oracle GraalVM or Mandrel must download from the vendor's own
  server, at a link of the vendor's own shape ending in the build's file name, or it is refused:
  a release asset of Adoptium's `temurin<N>-binaries` repositories on GitHub (GitHub serves a
  fork's commits from the original repository's other paths, so a host check alone isn't enough),
  `aka.ms/download-jdk/<file>`, `corretto.aws/downloads/resources/<version>/<file>` (or Amazon's
  CloudFront host for its 2019 builds), `cdn.azul.com/zulu/bin/<file>` (or `zulu-embedded/bin/`),
  a release asset of `bell-sw/Liberica`, `SAP/SapMachine`, `ibmruntimes/semeru<N>-binaries` (or
  `-certified-binaries`), `graalvm/graalvm-ce-builds` or `graalvm/mandrel` on GitHub,
  `download.oracle.com/java/` and `/graalvm/`, and Oracle's `gds.oracle.com`. That is where foojay
  lists every one of them. jkit checks the address foojay names; the redirects the vendor's server
  answers with are checked only to stay on HTTPS.
- **A foojay mirror of your own** (`JKIT_SOURCES_FOOJAY_URL`) may serve the builds these vendors
  signed itself, with each `.sig` next to its file: the signature, which is still required, and
  the version check after it are what make those safe, not the host. It must keep the vendor's
  links for the builds they didn't sign (Temurin before July 2022, and every build of the other
  vendors above). A mirror on this machine (`http://localhost`) may serve those too.
- **A failed lookup** (the server is down, answers 5xx or 429, or a captive portal answers) falls
  back to a cached answer only if it is at most 7 days old; an older one fails, and `--offline`
  uses it on purpose. So a network that only blocks foojay can't keep jkit on an old version list
  for long. The same goes for the other version lists, Maven Central's included.

What that leaves a compromised foojay: it can choose which build of the vendor and major you asked
for is installed, an older one included (with no version given, also which major), and for a
Temurin build from before July 2022 it supplies the checksum. For a signed build it can only make
the install fail. For Zulu, Liberica, SapMachine, Semeru, Oracle, GraalVM CE, Oracle GraalVM and
Mandrel it also supplies the checksum, but the file still comes from the vendor's server. For
other vendors (Kona, Dragonwell, JetBrains, Liberica NIK, jdk.java.net's OpenJDK and the rest) it
chooses the link and the checksum: install those only if you trust foojay as much as the vendor.

## Archive safety

Version and vendor names from a source must be plain text (letters, digits, `.`, `+`, `_`, `~`,
`-`); anything else is skipped, so a source can't put a path or `PATH` separator into an install
folder name.

Limits on what jkit fetches and unpacks:

| What | Limit |
| --- | --- |
| Version lists and checksum files | 8 MB and two minutes; JSON at most a million tokens |
| Downloads | 2 GB and three hours; given up on when they average under 32 KB/s |
| Signature files | 64 KB; a link that serves more is cut off there |
| An unpacked archive | 3 GB and 50,000 entries |

A request given up on for time or speed isn't tried again. Network failures and server errors
(5xx) are tried up to three times.

Archives are checked before they are unpacked:

- Only ordinary files, folders, links, GNU long names and PAX headers are accepted: no sparse
  files, devices or other entry types.
- Names stay under 4,096 bytes, and symlink entries are read only that far.
- Entries can't escape their folder, and links inside an install can't point outside it.
- What an archive unpacks is never left writable by anyone but you: not by your group either,
  whatever the archive or the umask says. jkit's own files and folders, and the startup files
  `hook --install` creates, get explicit permissions whatever the umask.
- An archive with colliding entries (a file and a folder of the same name), or an entry name the
  file system can't hold, is refused as broken or unsafe to unpack.

Each archive is unpacked and checked in `~/.jkit/tmp/`, and moved into `tools/` in one step only
after all checks pass, so a failed or interrupted install leaves nothing in `tools/`.

## Project files

jkit reads project files (`jkit.toml`, `.jkit.toml`, `.sdkmanrc`, `.tool-versions`,
`.java-version`) from the current directory and its parents, and they decide which JDK and tools
run. To keep another user from choosing them for you:

- jkit ignores a project file that is not owned by you or by root, or that is world-writable, or
  that sits in such a directory. The search stops there, like git's `safe.directory`.
- A project file that is a symbolic link counts only if it points to a file in its own folder or
  below.
- `jkit status`, `jkit sync` and `jkit doctor` say when they ignored one, and why.
- `jkit pin` and `jkit unpin` refuse to write through a project file that is a symbolic link.
- Project files over 256 KiB are refused, and values that aren't plain text are skipped, since
  they are printed on every `cd`.
- `JKIT_PROJECT_FILE` set to an absolute path is your own choice and is used as is.

Entering a project never downloads anything, even with auto-switch on: only `jkit sync` installs
what a project asks for, and it refuses downloads it can't check unless you pass
`--allow-unverified`. See [Project files](/reference/project-files/#which-file-applies).

## The jkit home

`~/.jkit` (or `JKIT_HOME`, which must be an absolute path) holds the programs jkit puts first on
`PATH`.

- jkit creates it readable only by you.
- It refuses to run when the home, or its `tools` and `global` folders, belong to another user
  (root is fine) or are world-writable.
- It also refuses when a folder above the home (followed through symbolic links) belongs to
  another user or is world-writable without the sticky bit.
- This is checked on the first run too, before anything is created.

Limits: group-writable folders are accepted, because many distributions give every user a private
group and a umask that makes new folders group-writable (the JDKs and tools unpacked into them are
not group-writable). On Windows there are no POSIX owners, so
none of the ownership checks above apply there; rely on the default ACLs of your user profile.

See [Files and folders](/reference/files-and-folders/#jkit_home) and
[Shared machines](/guides/shared-machines/).

## `jkit self-update`

`jkit self-update` downloads jkit's own release archive over HTTPS from GitHub and checks it
against that release's `SHA256SUMS`, then checks the new binary runs and reports the expected
version before it replaces the old one. `SHA256SUMS` comes from the same place as the archive, so
jkit first checks `SHA256SUMS.asc`, the release workflow's OpenPGP signature of it, against jkit's
release key, built into jkit (below): someone who could replace the files on the releases page
couldn't sign them. A release without a good signature is refused. Releases before 0.4.0 have no
`SHA256SUMS.asc`.

- **A release never moves onto a pre-release.** A pre-release replaces only a pre-release, even if
  it is marked as the latest release.
- **Homebrew, the apt repository and winget**: the command `jkit self-update` prints downloads
  nothing, so it needs no check. A jkit installed with Homebrew, apt, dnf or winget is left to that
  tool.
- **The Windows `.msi`** (from 0.6.0): `jkit self-update` checks `SHA256SUMS.asc` first, and prints
  a PowerShell command that downloads the newer `.msi` into your `%TEMP%` and runs it only if
  `Get-FileHash` matches the SHA-256 the signed `SHA256SUMS` lists.
- **An `.rpm` or a `.deb` installed from a file**: dnf and apt don't check the signature of a
  package given as a file or URL, so `jkit self-update` (and `--check`) checks `SHA256SUMS.asc`
  first, and prints a command that downloads the package into a new `mktemp -d` folder and
  installs it only if `sha256sum -c` matches the SHA-256 the signed `SHA256SUMS` lists.
- **On Windows**, the Visual C++ runtime DLLs next to `jkit.exe` are replaced from the same checked
  archive; a folder without them doesn't get them.

## Verifying a jkit release

Each release on [GitHub](https://github.com/adriandeleon/jkit/releases/latest) has:

- **`SHA256SUMS`**, the SHA-256 checksum of every file:

  ```bash
  sha256sum -c SHA256SUMS --ignore-missing
  ```

- **`SHA256SUMS.asc`** (from 0.4.0), the OpenPGP signature of `SHA256SUMS` by jkit's release key,
  `A944 4E8F 5048 FEBC B942 E110 FC89 EBE6 8C45 39E6`. The public key is
  [`jkit-release.asc`](https://github.com/adriandeleon/jkit/blob/main/jkit-core/src/main/resources/dev/jkit/core/keys/jkit-release.asc)
  in jkit's repository. Import it, check its fingerprint matches, then:

  ```bash
  gpg --verify SHA256SUMS.asc SHA256SUMS
  ```

- **A build provenance attestation** from GitHub Actions, showing which workflow built the file
  from which commit:

  ```bash
  gh attestation verify jkit-0.6.0-linux-x64.tar.gz --repo adriandeleon/jkit
  ```

- **A CycloneDX SBOM**, listing the libraries compiled in. It is made from the Linux x64 build: the
  other builds compile in the same libraries, and the macOS x64 binary is built with Liberica NIK
  instead of GraalVM CE.
- **Signed macOS binaries**, signed and notarized by Apple.
- **Signed `.deb` and `.rpm` packages** (from 0.5.0), signed with the key the Vitrina apt
  repository signs its index with, `8486 A7AF 4FC8 FEE9 3D83 CDB4 EA5F A138 C2C0 9C4A` (at
  https://vitrina-project.dev/vitrina.asc), so apt and dnf users trust one key. After
  `sudo rpm --import https://vitrina-project.dev/vitrina.asc`, `rpm -K` checks an `.rpm`; `dnf`
  checks it when it installs from a repository, but by default not a package given as a file or
  URL (`localpkg_gpgcheck`). The `.deb`'s signature is a `_gpgorigin` member, which
  `debsig-verify` checks; apt and dpkg don't check a downloaded `.deb`. See
  [Install](/start/install/#verify-a-deb-or-rpm).
- **The Windows `.zip`** (from 0.5.0) carries Microsoft's `vcruntime140.dll` and
  `vcruntime140_1.dll`, which Windows loads before any system-wide copy. The release workflow takes
  them from the build machine's Visual Studio and checks they are signed by Microsoft. Windows
  Update doesn't patch a copy next to a program, so a fix to them reaches you with the next jkit
  release and `jkit self-update`; with the Visual C++ Redistributable installed, deleting the two
  DLLs leaves the runtime to Windows Update. The `.msi` (from 0.6.0) installs the same two DLLs
  next to `jkit.exe`, and a newer `.msi` replaces them.
- **No Authenticode signature on Windows.** Neither `jkit.exe` nor the `.msi` is Authenticode-signed,
  so SmartScreen may warn when you open a downloaded `.msi` ("Windows protected your PC"). Check
  it against `SHA256SUMS` and its attestation before choosing *Run anyway*.

## Reporting a vulnerability

Please report vulnerabilities privately, not in a public issue: use **Report a vulnerability** on
the [Security tab](https://github.com/adriandeleon/jkit/security/advisories/new) of
adriandeleon/jkit (GitHub private vulnerability reporting). Include the jkit version
(`jkit --version`), your OS, and steps to reproduce.

You should get a reply within a week. Once a fix is released, the advisory is published with
credit to you unless you'd rather stay anonymous.

Only the latest release gets security fixes. Upgrade before reporting if you can
([`jkit self-update`](/reference/commands/self-update/)).

## See also

- [`jkit install`](/reference/commands/install/) and `--allow-unverified`
- [Corporate networks](/guides/corporate-networks/) for mirrors and proxies
- [JSON output](/reference/json/#the-install-object) for `verified` and `signedBy`
