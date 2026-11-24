---
title: Versions and vendors
description: The tools jkit manages, how to write a version spec, how jkit matches and resolves versions, and every JDK vendor name it accepts.
---

This page is the exact reference for what you type after a command: the tool, the version and the
vendor. It covers matching rules, `latest`, `lts`, pre-releases, JavaFX, and the full list of JDK
vendors. For help choosing a JDK, see [Choosing a JDK](/guides/choosing-a-jdk/).

## The spec

```text
<tool>[@<version>] [<vendor>]
```

```bash
jkit install jdk                 # newest JDK from your default vendor
jkit install jdk@21              # newest 21 from your default vendor
jkit install jdk@21.0.5 zulu     # newest 21.0.5 build from Zulu
jkit install jdk@lts corretto    # newest long-term-support release from Corretto
jkit install maven@3.9           # newest Maven 3.9.x
jkit install gradle@8.14.3       # exactly Gradle 8.14.3
```

- The version goes after `@`, never after a space: `jdk@21`, not `jdk 21`.
- The vendor is a separate word after the version, and only JDKs and GraalVM have one.
  `jkit install maven@3.9 zulu` is an error (exit code 2).
- A version without `@` (`jkit install 21`) or a vendor in the tool's place (`temurin@21`) is an
  error, and the hint shows the right spelling.

## Tools

| Tool | Also accepted | What it installs | Variable it sets |
| --- | --- | --- | --- |
| `jdk` | `java` | A JDK from any [vendor](#vendors) | `JAVA_HOME` |
| `graalvm` | | A GraalVM-based JDK, used for `native-image` | `GRAALVM_HOME` |
| `maven` | `mvn` | Apache Maven, from Maven Central | `MAVEN_HOME` |
| `gradle` | | Gradle, from services.gradle.org | `GRADLE_HOME` |
| `sbt` | | The sbt launcher, from sbt's GitHub releases | `SBT_HOME` |

Tool names aren't case-sensitive. The `bin` folders of `jdk`, `maven`, `gradle` and `sbt` go on
`PATH`; `graalvm` only sets `GRAALVM_HOME`, so there is only ever one `java` on `PATH`.

For sbt, jkit installs the launcher. The sbt version a build runs on still comes from the build's
`project/build.properties`.

## Versions

### Prefix matching

A version is a prefix, matched component by component:

| You write | Matches | Doesn't match |
| --- | --- | --- |
| `21` | `21.0.5+11`, `21.0.13.1+2` | `17.0.13`, `210` |
| `21.0` | `21.0.5+11` | `21.1` |
| `21.0.5` | `21.0.5+11`, `21.0.5.1+2` | `21.0.50` |
| `21.0.5+11` | that build only | `21.0.5+10` |
| `3.9` | `3.9.0` … `3.9.16` | `3.10.0` |
| `4.0.0-rc-4` | `4.0.0-rc-4` | `4.0.0-rc-7`, `4.0.0` |

- Missing components count as 0, so `21.0.0` matches `21+35`.
- A build number after `+` must match as far as you write it.
- A version with a pre-release tag (`-rc-4`, `-ea`) matches only that tag.
- The newest match wins. Versions compare by number, so `21.0.13` is newer than `21.0.9`, and a
  release is newer than its own pre-releases.

### Old Java numbering

For `jdk` and `graalvm`, Java's pre-9 numbering is read as the modern one: `jdk@1.8` is `jdk@8`,
and `1.8.0_432` is `8.0_432`. `8u472` is read as `8.0.472`. This doesn't apply to Gradle, where
`1.8` is a real version.

### `latest`

`@latest`, or no version at all, means the newest stable release:

```bash
jkit install jdk           # same as jdk@latest
jkit install maven@latest
```

In a project file, `"latest"` or a left-out version means the same.

### `lts`

`@lts` means the newest long-term-support (LTS) release. Only `jdk` and `graalvm` have LTS
releases; `maven@lts` is an error.

- Commands that go online (`install`, `search`, `pin`) take the newest release foojay marks as LTS.
- Commands that work offline (`use`, `exec`, `remove`, `upgrade`) take the newest LTS release you
  have installed from that vendor (the one you typed, or your default). jkit counts 8, 11, 17 and
  every fourth release after 17 (21, 25, 29, …) as LTS here.

```bash
jkit install jdk@lts zulu   # newest LTS release Zulu publishes
jkit use jdk@lts zulu       # newest LTS release of Zulu you have installed
```

### Pre-releases

Early-access builds, release candidates and milestones are left out unless you ask for them.
jkit treats a version as a pre-release when it contains `-ea`, `alpha`, `beta`, `-rc`, `rc` followed
by a number, `-m` followed by a number, `milestone` or `snapshot`.

```bash
jkit search jdk@27 --pre          # early-access JDK builds
jkit install gradle@9.9 --pre     # newest 9.9, release candidates included
jkit install maven@4.0.0-rc-4     # an exact pre-release asks for pre-releases by itself
```

- `--pre` works on `search`, `install` and `upgrade`.
- An exact pre-release version (one that carries the tag) needs no `--pre`, on the command line or
  in a project file.
- `jkit install jdk --pre` with no version picks an early-access release above the newest stable
  one, when the vendor has one.
- `jkit upgrade` skips installed pre-releases unless you add `--pre`, and `jkit prune` never
  removes them.

## Version formats per vendor

Each vendor numbers its builds its own way, and jkit uses the version as the vendor publishes it
(as foojay reports it). For JDK 21 on Linux x64, `jkit search jdk@21` shows:

```console
$ jkit search jdk@21
bisheng          21.0.12+15  ○ no checksum
corretto         21.0.13.1+2
dragonwell       21.0.13.1  ○ no checksum
gluon-graalvm    21.1  ○ no checksum
graalvm          21.0.12
graalvm-ce       21.0.2
jetbrains        21.0.11+1163125
kona             21.0.13+1
liberica         21.0.13.1+5
liberica-nik     21.0.13+6
mandrel          21
microsoft        21.0.13.1
openjdk          21.0.2+13
openlogic        21.0.12+8  ○ no checksum
oracle           21.0.12.1
sapmachine       21.0.13.1
semeru           21.0.13
semeru-certified 21.0.13+1
temurin *        21.0.13.1+2
zulu             21.0.13.1+2
* default vendor   ● installed   · all builds of one vendor: jkit search jdk@21 <vendor>
○ no published checksum: install refuses it unless --allow-unverified
```

What to expect:

- Most vendors use `<feature>.<interim>.<update>[.<patch>]+<build>`, such as `21.0.13.1+2`.
  Some leave out the build number (`21.0.13.1`).
- Java 8 builds are listed in the modern form: `8.0.514+1`, not `1.8.0_514`.
- GraalVM and Mandrel builds may carry only the Java version (`21.0.2`, or `21` for Mandrel).
- Early-access builds look like `25.0.6-ea+3`.
- Maven, Gradle and sbt use their own plain versions (`3.9.16`, `8.14`, `9.9.0`, `1.12.15`), as
  each project publishes them. Gradle marks pre-releases `-rc-1` or `-milestone-2`.

Because matching is by prefix, `jdk@21.0.13` matches both `21.0.13+1` and `21.0.13.1+2`. To pin one
build, copy the full version from `jkit search` or `jkit list`, or use `jkit pin --exact`.

## How a version resolves

### Downloading: `install`, `pin`, `sync`, `upgrade`

1. The vendor is the one you typed, or else your default vendor: `jdk.vendor` (Temurin unless you
   changed it) for `jdk`, `graalvm.vendor` (GraalVM Community) for `graalvm`. See
   [Configuration](/reference/configuration/).
2. jkit asks foojay for that vendor's builds for your OS and CPU, keeps those that match the
   version, and leaves out pre-releases unless asked.
3. It takes the newest match that has a direct download. Some vendors list builds that are only
   offered behind a form; jkit skips those and tries the next one (up to 32).

jkit never swaps the vendor for you. When your vendor has no matching build, the install fails
and names the vendors that do have one:

```console
$ jkit install jdk@8 microsoft
error: microsoft has no build matching jdk@8 for linux-x64
  available from: bisheng, corretto, dragonwell, kona, liberica, openjdk, openlogic, semeru, temurin, trava, zulu
```

A typo in a vendor name fails before any download, with a suggestion:

```console
$ jkit install jdk@21 zuluu
error: unknown JDK vendor 'zuluu'
  did you mean: zulu?
  vendors: bisheng, corretto, dragonwell, eliya, gluon-graalvm, graalvm, graalvm-ce, jetbrains, kona, liberica, liberica-nik, mandrel, microsoft, openjdk, openlogic, oracle, redhat, sapmachine, semeru, semeru-certified, temurin, trava, zulu
```

### Choosing an install: `use`, `exec`, `remove`

These commands only look at what is installed and never use the network.

- The vendor is the one you typed, or else your default vendor. An install from another vendor
  doesn't count, even if it matches the version; the error lists the other vendors you have it
  from.
- The newest installed match wins.
- Plain builds win over JavaFX builds. With `--fx`, only JavaFX builds count.

```console
$ jkit use jdk@17
error: no installed JDK matches 17 from temurin (default vendor)
  install it:          jkit install jdk@17
  or use installed:    jkit use jdk@17 zulu
```

A version that isn't installed exits with code 3. See [exit codes](/reference/exit-codes/).

### Project pins

A [project file](/reference/project-files/) pin may leave out the vendor, so the same file works on
machines with different defaults. Then:

- jkit picks your default vendor's newest matching install if you have one, and otherwise the
  newest matching install from any vendor.
- If nothing installed matches, `jkit sync` downloads it from your default vendor.

A pin that names a vendor only ever matches that vendor.

## GraalVM

`graalvm` is a JDK that includes `native-image`. jkit stores GraalVM builds with the other JDKs
(under `~/.jkit/tools/jdk/`), and the `graalvm` tool only sets `GRAALVM_HOME`, so you can build
with one JDK and use GraalVM's `native-image` at the same time.

- `graalvm` accepts only GraalVM-based vendors: `graalvm-ce` (the default), `graalvm`,
  `liberica-nik` and `mandrel`.
- A GraalVM-based vendor also works as a plain JDK: `jkit install jdk@25 graalvm-ce` makes it your
  `JAVA_HOME`. When no `graalvm` is chosen, `GRAALVM_HOME` then follows that JDK.
- `gluon-graalvm` installs as a JDK only; jkit doesn't treat it as GraalVM-based.
- The first `jkit install graalvm` becomes the default `graalvm` (`~/.jkit/global/graalvm`), not
  the default JDK, and it doesn't stop the next plain JDK from becoming the default `jdk`.

See [GraalVM](/guides/graalvm/) for a walkthrough.

## JavaFX

Some vendors publish JDK builds with JavaFX bundled. Ask for them with `--fx`:

```bash
jkit search jdk --fx              # which vendors and releases have JavaFX builds
jkit install jdk@21 zulu --fx
jkit use jdk@21 zulu --fx
```

- A JavaFX build installs next to the plain build of the same version, in a folder ending in
  `-fx` (`21.0.9+10-zulu-fx`), and `jkit list` marks it `fx`.
- Without `--fx`, `use`, `exec` and `remove` prefer plain builds and match JavaFX builds only when no
  plain build matches.
- In `jkit.toml`, write `fx = true`; `.sdkmanrc` ids such as `21.0.5.fx-zulu` and
  `.tool-versions` ids such as `liberica-javafx-21` are read as JavaFX builds too.

See [JavaFX](/guides/javafx/).

## Vendors

The JDK vendors `jkit search jdk` lists on Linux x64. Type the name in the first column; jkit sends
foojay the distribution name in the second. Which vendors and releases exist depends on your OS
and CPU: run `jkit search jdk` to see yours.

| Name you type | foojay distribution | Notes |
| --- | --- | --- |
| `temurin` | `temurin` (Temurin) | Default `jdk` vendor. Signature checked. `adoptium` also works |
| `bisheng` | `bisheng` (Bi Sheng) | |
| `corretto` | `corretto` (Corretto) | Signature checked; it publishes no checksum, so the signature is the check |
| `dragonwell` | `dragonwell` (Dragonwell) | |
| `eliya` | `eliya` (Eliya JDK) | |
| `gluon-graalvm` | `gluon_graalvm` (Gluon GraalVM) | JDK only, not usable as `graalvm` |
| `graalvm` | `graalvm` (GraalVM) | GraalVM-based |
| `graalvm-ce` | `graalvm_community` (GraalVM Community) | GraalVM-based. Default `graalvm` vendor |
| `jetbrains` | `jetbrains` (JetBrains) | |
| `kona` | `kona` (Kona) | |
| `liberica` | `liberica` (Liberica) | |
| `liberica-nik` | `liberica_native` (Liberica Native) | GraalVM-based |
| `mandrel` | `mandrel` (Mandrel) | GraalVM-based |
| `microsoft` | `microsoft` (Microsoft Build of OpenJDK) | Signature checked |
| `openjdk` | `oracle_open_jdk` (Oracle OpenJDK) | |
| `openlogic` | `openlogic` (OpenLogic) | |
| `oracle` | `oracle` (Oracle) | |
| `sapmachine` | `sap_machine` (SapMachine) | |
| `semeru` | `semeru` (Semeru) | |
| `semeru-certified` | `semeru_certified` (Semeru certified) | |
| `trava` | `trava` (Trava) | |
| `zulu` | `zulu` (Zulu) | |

- Names aren't case-sensitive, and `_` and `-` are the same (`liberica_nik` works).
- foojay's own names work too (`sap_machine`, `oracle_open_jdk`). Commands that check the name
  with foojay (`search`, `install`, `pin`, `sync`) also accept its synonyms, such as `"SAP Machine"`.
- foojay also knows `redhat`; `jkit search jdk` lists no builds for it on Linux x64.
- A vendor foojay adds later works as soon as foojay lists it, with no jkit update.
- Retired distributions foojay still lists (AOJ, OJDK Build, the old per-release GraalVM CE
  entries) are hidden.

"Signature checked" means jkit also checks the publisher's OpenPGP signature with a key built
into jkit. See [Security](/reference/security/).

### Names in `.sdkmanrc`

SDKMAN's vendor suffixes map to jkit's names:

| SDKMAN | jkit | SDKMAN | jkit |
| --- | --- | --- | --- |
| `tem` | `temurin` | `nik` | `liberica-nik` |
| `zulu` | `zulu` | `mandrel` | `mandrel` |
| `amzn` | `corretto` | `oracle` | `oracle` |
| `librca` | `liberica` | `open` | `openjdk` |
| `ms` | `microsoft` | `kona` | `kona` |
| `sapmchn` | `sapmachine` | `albba` | `dragonwell` |
| `sem` | `semeru` | `jbr` | `jetbrains` |
| `graalce` | `graalvm-ce` | `trava` | `trava` |
| `graal` | `graalvm` | `bsg` | `bisheng` |

SDKMAN's early-access ids (`26.ea.20-open`) are read as `26-ea+20`. Old GraalVM and Mandrel ids
that name the distribution release (ending in `.r21` and similar) are skipped with a warning; use
the Java version instead (`21.0.5-graalce`).

### Names in `.tool-versions`

asdf and mise write the vendor before the version (`temurin-21.0.5+11.0.LTS`). Names that differ
from jkit's are mapped; all others are used as they are:

| asdf / mise | jkit |
| --- | --- |
| `adoptopenjdk` | `temurin` |
| `graalvm-community` | `graalvm-ce` |
| `oracle-graalvm` | `graalvm` |
| `semeru-openj9` | `semeru` |

A plain number (`java 21`) means any vendor. A `-javafx` or `-fx` suffix on the vendor
(`liberica-javafx-21`) asks for a JavaFX build. asdf's Zulu ids use Zulu's own numbering and don't
match. See [Project files](/reference/project-files/) for the rest of both formats.

## See also

- [`jkit search`](/reference/commands/search/) lists what you can install.
- [`jkit install`](/reference/commands/install/) and [`jkit use`](/reference/commands/use/).
- [Choosing a JDK](/guides/choosing-a-jdk/) for which vendor and release to pick.
