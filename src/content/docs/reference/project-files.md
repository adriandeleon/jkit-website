---
title: Project files
description: The complete format of jkit.toml, the .sdkmanrc, .tool-versions and .java-version files jkit also reads, how jkit finds them, and which version wins.
---

A project file records the JDK, GraalVM, Maven, Gradle and sbt a repository builds with. This page
is the complete reference for its format and the rules jkit follows when reading it. For a
walkthrough, see [Projects](/guides/projects/) and [Teams](/guides/teams/).

`jkit sync` installs what the file asks for and switches the current shell to it. With auto-switch
on, every shell with the jkit hook switches when you `cd` into the project and back when you leave.

```console
$ jkit pin jdk@21
$ jkit pin maven@3.9
$ cat jkit.toml
# Versions jkit uses in this project: jkit sync installs them.
[tools]
jdk = "21"
maven = "3.9"
$ jkit sync
```

## `jkit.toml`

```toml title="jkit.toml"
[tools]
jdk = "21"                                        # newest installed 21, any vendor
maven = "3.9"
gradle = "8.14.3"                                 # exact
graalvm = { version = "25", vendor = "graalvm-ce" }
# jdk = { version = "21.0.9+10", vendor = "zulu", fx = true }   # the long form
```

Each line under `[tools]` pins one tool, in one of two forms:

- **Short:** `tool = "version"`.
- **Long:** `tool = { version = "…", vendor = "…", fx = true }`. Every field is optional.

### Keys

| Key | Tool |
| --- | --- |
| `jdk` or `java` | A JDK |
| `graalvm` | A GraalVM-based JDK, for `GRAALVM_HOME` |
| `maven` or `mvn` | Apache Maven |
| `gradle` | Gradle |
| `sbt` | The sbt launcher |

Setting the same tool twice (`jdk` and `java`) is an error. Keys aren't case-sensitive.

### Fields

| Field | Type | Meaning |
| --- | --- | --- |
| `version` | string | A version prefix: `"21"` is any 21, `"21.0.9+10"` one build. Left out, empty or `"latest"` means the newest. See [prefix matching](/reference/versions-and-vendors/#prefix-matching). |
| `vendor` | string | JDK and GraalVM only. The same names as on the command line (`zulu`, `corretto`, …). |
| `fx` | boolean | `true` asks for a JavaFX build. JDK and GraalVM only. |

- **Version.** A prefix, matched as on the command line. `"1.8"` reads as `8`. Write an exact build
  (or run `jkit pin --exact`) for reproducible builds; there is no separate lock file. A version
  with a pre-release tag (`"4.0.0-rc-4"`) installs that pre-release without `--pre`.
- **Vendor.** Left out, jkit prefers your default vendor and otherwise takes the newest matching
  install from any vendor, so the file works on machines with different defaults. When nothing is
  installed, `jkit sync` downloads from your default vendor. A `graalvm` vendor must be
  GraalVM-based (`graalvm-ce`, `graalvm`, `liberica-nik`, `mandrel`).
- **sbt** picks the launcher only: the sbt version a build runs on still comes from its
  `project/build.properties`.

### Quote versions

A version is a string. A whole number (`jdk = 21`, a TOML number) is read as `"21"` and pins
JDK 21. A decimal is refused with a warning to quote it, since TOML has already made `maven = 3.10`
into 3.1:

```console
warning: ~/src/app/jkit.toml:3: maven = 3.10 is a TOML number, which drops trailing zeros (3.10 reads as 3.1); quote it: maven = "3.10", skipped maven
```

A decimal inside a table (`version = 3.9` under `[tools.maven]`) is reported on its own line.

### What jkit tolerates

A file written for a newer jkit, or edited by hand, still works. These give a warning naming the
file and line, and the rest of the file applies:

- An unknown top-level key or table, or an unknown tool under `[tools]`.
- An unknown field in a long-form entry (ignored; the entry still applies).
- A value that isn't a string, a whole number or a table, or a field of the wrong type (that tool
  is skipped).
- A version that isn't one, such as `"twenty-one"`, or a decimal such as `3.10` (skipped).
- A vendor on `maven`, `gradle` or `sbt`, or `fx` on them (ignored).
- A `graalvm` vendor that isn't GraalVM-based (skipped).
- Versions and vendors that aren't plain text. Versions may hold letters, digits and `. + _ -`,
  vendors letters, digits and `. _ -`, up to 64 characters; anything else is skipped.

At most 20 warnings are shown per file. Broken TOML is an error, with the line number.

A skipped pin doesn't fall back to anything: `jkit sync` fails, installing nothing, when a project
file pins a tool it can't read (a malformed version, a decimal, an unknown SDKMAN suffix, …), so CI
doesn't go on with the wrong JDK. An unknown tool or key only warns.

```console
$ jkit sync
warning: ~/src/app/jkit.toml:3: maven = 3.10 is a TOML number, which drops trailing zeros (3.10 reads as 3.1); quote it: maven = "3.10", skipped maven
error: jkit.toml pins maven, but jkit can't read that pin (see the warning above); nothing was installed
  fix the entry in ~/src/app, then run jkit sync again
```

The file is read as UTF-8, or as UTF-16 when it starts with a byte-order mark (as PowerShell 5's
`echo 21 > .java-version` writes).

### `.jkit.toml`

`.jkit.toml` works too, for those who want it hidden. When a folder has both, `jkit.toml` wins and
`.jkit.toml` is ignored with a warning.

## Other files jkit reads

jkit reads these so existing repositories work unchanged, and never writes them. Several in one
folder are read together: the first file in this list that mentions a tool decides it, even when
jkit can't read that entry (a later file doesn't stand in for it, and `jkit sync` fails). Within
`.sdkmanrc` the last line for a tool decides, as `sdk env` runs `sdk use` for each line in turn;
within `.tool-versions` the first one does, as in asdf. The other is ignored with a warning. Hints
and warnings name the file that set each tool. A `jkit.toml` (or `.jkit.toml`) in the same folder
replaces all of them.

| File | Example | Notes |
|---|---|---|
| `.sdkmanrc` | `java=21.0.5-tem`, `maven=3.9.9` | SDKMAN's vendor suffixes map to jkit's vendors (`tem` temurin, `zulu`, `amzn` corretto, `librca` liberica, `ms` microsoft, `sapmchn` sapmachine, `sem` semeru, `graalce` graalvm-ce, `graal` graalvm, `nik` liberica-nik, `mandrel`, `oracle`, `open` openjdk, `kona`, `albba` dragonwell, `jbr` jetbrains, `trava`, `bsg` bisheng). `21.0.5.fx-zulu` means a JavaFX build. Other candidates are skipped with one warning. |
| `.tool-versions` (asdf, mise) | `java temurin-21.0.5+11.0.LTS`, `maven 3.9.9` | asdf names map to jkit's (`adoptopenjdk` temurin, `graalvm-community` graalvm-ce, `oracle-graalvm` graalvm, `semeru-openj9` semeru). Only the first version on a line counts. `system`, `ref:` and `path:` are skipped. See the details below for Corretto, Semeru, Mandrel, Zulu and JavaFX ids. |
| `.java-version` | `21`, `17.0.2`, `temurin64-21.0.5` | jdk only; `1.8` reads as 8, and a plain number means any vendor. jEnv's names pin that vendor (see below). |

Details per format:

- **`.sdkmanrc`.** One `candidate=version` per line; `#` starts a comment. jkit reads the `java`,
  `maven`, `gradle` and `sbt` candidates. When a candidate is set twice, the last line wins, as in
  `sdk env`. A `java` id must end in a vendor suffix
  (`21.0.5-tem`); SDKMAN ids carry no build number, so the version is used as a prefix.
  Early-access ids (`26.ea.20-open`) read as `26-ea+20`. Old GraalVM and Mandrel ids that name the
  distribution release (`22.3.r17-grl`) are skipped with a warning. A GraalVM `java` id stays a
  `jdk` pin, as in SDKMAN, where it becomes `JAVA_HOME`.
- **`.tool-versions`.** `<name> <version> [fallbacks…]` per line; jkit reads `java`, `maven`,
  `gradle` and `sbt`, and other tools are skipped with one warning. A `java` version is
  `<vendor>-<version>` (`temurin-21`, `graalvm-community-21`) or a plain number (`21`, any vendor).
  A `-javafx` or `-fx` suffix on the vendor asks for a JavaFX build. `latest` means the newest.
  When a tool is set twice, the first line wins, as in asdf. Vendor-specific ids:
  - Corretto's own numbering reads as the Java version: `21.0.5.11.1` is 21.0.5, `8.432.06.1` is
    8.0.432.
  - Semeru's `_openj9-…` suffix is dropped.
  - `mandrel-23.1.5.0-Final-java21` is Mandrel for Java 21.
  - `-jre`, `-lite` and `-musl` builds are pinned as the vendor's full JDK, with a warning (the C
    library follows the machine).
  - A Zulu release number (`zulu-21.38.21`) pins only the major, with a warning: write the Java
    version (`zulu-21.0.5`) to be exact.
  - `semeru-certified` is pinned as open Semeru, with a warning.
  - Old GraalVM release ids (`graalvm-22.3.0+java17`) are skipped.
  - An unknown vendor is warned about as soon as the file is read; jkit then looks for a vendor of
    that name.
- **`.java-version`.** The first line that isn't empty or a `#` comment. Numbers such as `21` or
  `17.0.2` pin that version from any vendor. jEnv's names (`<vendor>64-<version>`) pin that vendor,
  read like `.tool-versions` ids: `temurin64-21.0.5` is Temurin 21.0.5, `corretto64-21` is
  Corretto 21. `openjdk64-17.0.2` pins 17.0.2 from any vendor, since jEnv gives that name to any
  build that only says OpenJDK. `oracle64-1.8.0.202` and other Oracle JDK 8 and 11 names are
  skipped with a warning: Oracle offers those only after a sign-in, so jkit can't install them.
  Anything else (`system`) is skipped with a warning.

## Which file applies

jkit starts in the current folder and walks up. The first folder with any of these files is the
project; files from different folders are never merged, so a nested `jkit.toml` replaces the outer
one completely.

- Below your home folder the search stops at `$HOME` (still checked, so `~/jkit.toml` can be a
  personal default). Elsewhere, such as a CI workspace, it goes up to `/`.
- It stops at a folder that belongs to another user (other than root) or that every user can write
  to, such as `/tmp`: someone else's `/tmp/jkit.toml` must not change your versions. A project
  file that is itself world-writable, or owned by another user, is ignored the same way, as is a
  project file that is a symbolic link to a file outside its own folder.
  `jkit status`, `jkit sync` and `jkit doctor` say when they ignored one, and why. This often
  affects WSL's `/mnt/c`, SMB shares and Docker bind mounts, where every file looks world-writable;
  fix the mount options or permissions, or name the file with `JKIT_PROJECT_FILE`.
- A folder whose name isn't valid UTF-8 is skipped, and `jkit status` says so.
- On Windows there are no POSIX owners, so the owner and permission checks don't apply.

### `JKIT_PROJECT_FILE`

```bash
JKIT_PROJECT_FILE=/path/to/jkit.toml jkit sync   # use this file, wherever you are
JKIT_PROJECT_FILE=ci/jkit.toml jkit exec -- mvn verify
JKIT_PROJECT_FILE= jkit status                   # set but empty: no project files at all
```

- An absolute path names the file directly and skips the owner and permission checks (it's your
  own choice). The file is read by its name: `.sdkmanrc`, `.tool-versions` and `.java-version` in
  their own format, anything else as `jkit.toml`. If it doesn't exist, commands that read it fail.
- A relative path is looked up from each folder, and applies only where it exists. The owner and
  permission checks still apply to it.
- Set but empty, it turns project files off.
- `jkit pin` writes to the file it names, creating it if it ends in `.toml` and its folder exists.

### Limits

- Project files over 256 KiB are refused, as is anything that isn't a regular file (a named pipe).
- `jkit pin` and `jkit unpin` won't write through a `jkit.toml` that is a symbolic link.

## Which version wins

Per tool, from highest to lowest:

1. `jkit use` in this shell (without `--global`).
2. The project file.
3. The global default (`jkit use … --global`).

A project that pins only `jdk` leaves Maven and Gradle on their shell or global choice. A `jkit use`
keeps winning after you `cd` into a project, and the hook says so; `jkit reset jdk` goes back to the
project's version.

`jkit env` and `jkit exec` describe a new process, so they skip step 1: project pins over global
defaults. `jkit exec` then applies the tools you name before `--` on top.

## Switching on `cd`

Off by default. Turn it on with:

```bash
jkit hook --install --auto-switch
```

That sets `auto_switch = true` under `[shell]` in `~/.jkit/config.toml` (`JKIT_AUTO_SWITCH=1` works
too). bash, zsh and fish then switch when you enter a project and go back to the global versions
when you leave, printing every variable they change:

```console
$ cd ~/src/app
jkit: using ~/src/app/jkit.toml: jdk 21.0.13.1+2 temurin
  set    JKIT_PROJECT=~/src/app/jkit.toml:1795537120159
  set    JKIT_PROJECT_JDK=~/.jkit/tools/jdk/21.0.13.1+2-temurin
  set    JAVA_HOME=~/.jkit/tools/jdk/21.0.13.1+2-temurin
  PATH   + ~/.jkit/tools/jdk/21.0.13.1+2-temurin/bin
$ cd ..
jkit: left the project in ~/src/app: back to the global versions
  unset  JKIT_PROJECT
  unset  JKIT_PROJECT_JDK
  set    JAVA_HOME=~/.jkit/global/jdk
  PATH   - ~/.jkit/tools/jdk/21.0.13.1+2-temurin/bin
```

PowerShell doesn't switch on `cd` yet.

- Entering a project never downloads anything: a missing version stays on its shell or global
  choice, with a note to run `jkit sync`.
- bash switches when the next prompt is drawn, so `cd app && java -version` on one line still runs
  the old JDK. In scripts and one-liners, use `jkit exec -- java -version` instead.
- Editing the project file takes effect on the next `cd`, `jkit sync` or `jkit pin`.
- A broken project file is reported once and leaves the shell's versions alone. A file whose pins
  jkit can't read (a `.java-version` saying `system`) is reported as "it pins nothing jkit can
  read", with a pointer to `jkit status`, which says why.
- A change to `auto_switch` in `config.toml` applies to new shells. To stop switching in a shell
  that is already open, `export JKIT_AUTO_SWITCH=0` there (`set -gx JKIT_AUTO_SWITCH 0` in fish).
- A `JAVA_HOME` (or other `*_HOME`) you set yourself is kept in `JKIT_USER_JDK` (`JKIT_USER_MAVEN`,
  …) when a project or `jkit use` first replaces it, and comes back, with its `bin` first on
  `PATH`, when neither applies any more (you leave the project, `jkit reset jdk`); jkit says so each
  time. A `JAVA_HOME` you change by hand while a project's JDK applies is left as you set it.
- Leaving a project, a tool with no global version is unset (`maven unset (no global maven)`), and
  the `bin` that comes back moves to the front of `PATH` if it is further back
  (`PATH   moved … ahead of …`).

See [Shell hook](/reference/shell-hook/) for how this works.

## Commands

- [`jkit pin <tool>[@version] [vendor] [--exact] [--here]`](/reference/commands/pin/) writes or
  updates one line of the nearest `jkit.toml` (or creates one here with `--here`), keeping comments,
  order, line endings and the file's encoding (UTF-8 with or without a byte-order mark, UTF-16 with
  one, or Latin-1). The file is written atomically, so an interrupted write never leaves half a
  file; `jkit doctor` reports a `*.jkit-tmp` file one leaves behind. The version must be installed or downloadable. It won't edit `.sdkmanrc`,
  `.tool-versions` or `.java-version`. It writes the short form, or the long form when the pin has
  a vendor or `fx`.
- [`jkit unpin <tool>`](/reference/commands/unpin/) removes that line, the same way.
- [`jkit sync [--dry-run]`](/reference/commands/sync/) installs what the project asks for and
  switches this shell to it. A download with no published checksum is refused unless you add
  `--allow-unverified`.
- [`jkit status`](/reference/commands/status/) shows what the project wants and what this shell has.

jkit only edits plain `tool = …` lines under a `[tools]` header. A file that sets its tools another
way (an inline `tools = { … }` table, dotted keys, a value over several lines) has to be edited by
hand; `jkit pin` says so instead of guessing.

In CI, either load the hook and sync:

```bash
eval "$(jkit hook bash)"
jkit sync
mvn -B verify
```

or skip the hook: `jkit sync` then `jkit exec -- mvn -B verify`. See [CI](/guides/ci/) and
[AI agents](/guides/ai-agents/).

## See also

- [Versions and vendors](/reference/versions-and-vendors/) for version syntax and vendor names.
- [Configuration](/reference/configuration/) for the default vendor and auto-switch.
- [Security](/reference/security/#project-files) for why some project files are ignored.
