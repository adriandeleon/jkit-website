---
title: Files and folders
description: The layout of ~/.jkit, what each file and folder holds, JKIT_HOME and its safety rules, what jkit touches outside its home, and the Windows layout.
---

Everything jkit installs and remembers lives in one folder, `~/.jkit`. This page describes what is
in it, so you can point an IDE at the right path, back it up, clean it, or move it. Outside that
folder, jkit only touches the marked block `jkit hook --install` adds to your shell's startup file.

## Layout

```text
~/.jkit/
├── config.toml                          settings (only once you set one)
├── tools/                               every install
│   ├── jdk/
│   │   ├── 21.0.13.1+2-temurin/         a JDK: <version>-<vendor>
│   │   │   ├── bin/java …
│   │   │   └── jkit-install.properties  jkit's record of the install
│   │   ├── 21.0.9+10-zulu-fx/           a JavaFX build: -fx at the end
│   │   └── 25.0.2-graalvm-ce/           GraalVM builds live with the JDKs
│   ├── maven/
│   │   └── 3.9.16/                      <version>, no vendor
│   ├── gradle/
│   │   └── 8.14.3/
│   └── sbt/
│       └── 1.12.15/
├── global/                              the global defaults, as links
│   ├── jdk → ../tools/jdk/21.0.13.1+2-temurin
│   ├── maven → ../tools/maven/3.9.16
│   ├── gradle, sbt, graalvm             when set
├── index/                               cached version lists
│   └── 04a1be13e0e3f799864b70a3.cache
├── tmp/                                 work folders of installs in progress
├── .lock                                held while an install or removal changes tools/
└── .links.lock                          held while a global link changes
```

### `tools/`

One folder per install, named after what's in it:

| Tool | Folder | Example |
| --- | --- | --- |
| JDK, GraalVM | `tools/jdk/<version>-<vendor>` | `tools/jdk/21.0.13.1+2-temurin` |
| JavaFX build | `tools/jdk/<version>-<vendor>-fx` | `tools/jdk/21.0.9+10-zulu-fx` |
| Maven | `tools/maven/<version>` | `tools/maven/3.9.16` |
| Gradle | `tools/gradle/<version>` | `tools/gradle/8.14.3` |
| sbt | `tools/sbt/<version>` | `tools/sbt/1.12.15` |

The install folder is the tool's home, the folder `JAVA_HOME` (or `MAVEN_HOME`, …) should point
at: `bin/java` is directly inside. On macOS, jkit installs the `Contents/Home` folder of a JDK
bundle, so the layout is the same as on Linux. The folder name is also the `id` in
[`--json` output](/reference/json/#the-install-object).

Each install has a `jkit-install.properties` file, which is how jkit knows a folder is a finished
install. A folder without it isn't listed, and `jkit doctor` reports it as an unfinished install.

```properties title="jkit-install.properties"
#written by jkit
fx=false
signedBy=Adoptium
tool=jdk
url=https\://github.com/adoptium/temurin21-binaries/releases/download/jdk-21.0.13.1%2B2/OpenJDK21U-jdk_x64_linux_hotspot_21.0.13.1_2.tar.gz
vendor=temurin
verified=true
version=21.0.13.1+2
```

| Key | Meaning |
| --- | --- |
| `tool` | `jdk`, `maven`, `gradle` or `sbt` (GraalVM builds are `jdk`) |
| `version` | The version as the source published it |
| `vendor` | foojay's distribution name (`graalvm_community`, `sap_machine`, …); empty for Maven, Gradle and sbt |
| `fx` | `true` for a JavaFX build |
| `url` | Where it was downloaded from |
| `verified` | `true` when the download matched a SHA-256 or stronger checksum, or a good signature |
| `signedBy` | Whose OpenPGP signature was checked, when one was (`Adoptium`, `Microsoft`, `Amazon`, `Apache Maven`, `sbt`) |

Installs made by older jkit releases may lack `verified` and `signedBy`.

Don't edit files in `tools/`. To get rid of an install, use [`jkit remove`](/reference/commands/remove/)
or [`jkit prune`](/reference/commands/prune/): they also take care of global links that point at it.

### `global/`

One link per tool with a global default: `jdk`, `maven`, `gradle`, `sbt` and `graalvm`. The hook
puts `global/<tool>/bin` on `PATH` and sets `JAVA_HOME` (and the others) to `global/<tool>`, so
anything that follows the link follows `jkit use … --global` without a restart.

- On Linux and macOS these are relative symbolic links, so moving the whole `~/.jkit` folder
  keeps them working.
- The first install of each tool sets its link, unless you pass `--no-default`.
- `jkit reset <tool> --global` removes the link; `jkit remove` removes it along with its install
  (only with `--force`).

Point an IDE or a tool that needs a fixed path at `~/.jkit/global/jdk`. See
[IDEs](/guides/ides/).

### `index/`

Cached answers from the version sources (foojay, Maven Central, services.gradle.org), one file per
request, named after a hash of its URL.

- An answer is used for 24 hours, then fetched again. `--refresh` fetches again now; `--offline`
  (or `JKIT_NETWORK_OFFLINE=1`) uses cached answers of any age and never goes online.
- When a source can't be reached, answers with an error, or returns a web page (a captive portal),
  an older cached answer stands in, with a warning.
- The cache keeps at most 32 MB and nothing older than 30 days: writing to it deletes the oldest
  answers first. `jkit prune` also deletes answers older than 30 days.
- Checksums and signatures are never cached; they are fetched fresh for every download.

Commands report the cache files they write (`cache  ~/.jkit/index: 2 files saved from
api.foojay.io`). Deleting `index/` is always safe.

### `tmp/`

Each download is unpacked and checked in a folder of its own, `tmp/install-<random>`, then moved
into `tools/` in one step, so an interrupted install never leaves half a JDK in `tools/`.
`jkit remove` moves an install to `tmp/remove-<random>` before deleting it, and
`jkit self-update` works in `tmp/install-self-<random>`.

Folders left behind by an interrupted install or removal are deleted by the next install and by
`jkit prune`. When `tools/` is on another disk than `tmp/`, the same steps happen next to the
install instead, in `tools/<tool>/.<name>.partial` and `tools/<tool>/.remove-<random>`; these are
cleaned up the same way. `jkit doctor` reports any of these it finds.

### Locks

`.lock` and `.links.lock` are empty files jkit locks so that two jkit processes don't change the
same thing at once. A second `jkit install` waits for the first, with a message; `jkit use --global`
only waits for another link change, never for a download. Deleting them while no jkit runs is
harmless.

### `config.toml`

Your settings. Created by `jkit config set` or `jkit hook --install --auto-switch`. See
[Configuration](/reference/configuration/).

## `JKIT_HOME`

`JKIT_HOME` moves all of it:

```bash
export JKIT_HOME=/opt/jkit              # must be absolute
export JKIT_HOME="$(mktemp -d)"         # a throwaway home, to try something
```

- It must be an absolute path. A relative one (or a quoted `~`) is an error, since it would mean a
  different folder in every directory you `cd` to.
- jkit creates the folder readable only by you (mode 700) on first use.
- The hook, `jkit env` and `jkit exec` use `$JKIT_HOME/global` and `$JKIT_HOME/tools`, so set
  `JKIT_HOME` in your startup file before the hook block.

The jkit home holds the programs jkit puts first on `PATH`, so jkit refuses to run when someone
else could put programs there:

- the home, or its `tools` or `global` folder, belongs to another user (root is fine) or is
  writable by every user;
- a folder above it (following symbolic links) belongs to another user, or is writable by every
  user without the sticky bit `/tmp` has.

These checks also run on the first run, before anything is created. Group-writable folders are
accepted, because many distributions give every user a private group. See
[Shared machines](/guides/shared-machines/) for setups with several users, and
[Security](/reference/security/#the-jkit-home).

When `HOME` isn't set (a container run with `-u 1001`, say), jkit uses the home folder from the
system's user list; if there is none, it asks you to set `HOME` or `JKIT_HOME`.

## Outside `~/.jkit`

jkit changes nothing outside its home except:

| What | When |
| --- | --- |
| A marked block in `~/.bashrc`, `~/.zshrc` (or `$ZDOTDIR/.zshrc`), or the file `~/.config/fish/conf.d/jkit.fish` | `jkit hook --install`; `--uninstall` takes it out |
| `jkit.toml` in your project | `jkit pin`, `jkit unpin` |
| The jkit binary itself | `jkit self-update`, for a jkit installed from a release archive |
| A temporary file in `$TMPDIR` (or `/tmp`), deleted right after | Each run of `jkit` through the shell hook's function |

Everything else (the hook's variables, `jkit use` in a shell) lives in the shell's environment and
ends with the shell. jkit writes nothing to `/etc`, the registry, or system `PATH` settings.

To remove jkit completely, see [Uninstall](/guides/uninstall/).

## Windows

The layout is the same, under `%USERPROFILE%\.jkit` (or `JKIT_HOME`):

```text
%USERPROFILE%\.jkit\
├── tools\jdk\21.0.13.1+2-temurin\
├── global\
│   ├── jdk                 a directory junction to tools\jdk\…
│   └── .jdk.target         the junction's target, as text
├── index\
└── config.toml
```

- Global links are directory junctions (made with `mklink /J`), which need no administrator
  rights or Developer Mode. Next to each, a `.<tool>.target` file records its target, so jkit can
  report a junction whose target is gone.
- JDK downloads are `.zip` files rather than `.tar.gz`.
- There are no POSIX owners on Windows, so the ownership checks above don't apply; your user
  profile's default permissions protect the folder.
- `jkit self-update` can't replace a running `jkit.exe`, so it renames the old one to
  `jkit.exe.old`, which the next self-update deletes.

Programs started outside a hooked PowerShell (cmd.exe, IDEs) see the global JDK only if you set
`JAVA_HOME` to `%USERPROFILE%\.jkit\global\jdk` once and add `%JAVA_HOME%\bin` to your user
`PATH`. See [Windows](/guides/windows/).

## See also

- [`jkit list`](/reference/commands/list/) shows what is in `tools/` and which links point where.
- [`jkit doctor`](/reference/commands/doctor/) checks the links, leftovers and the cache.
- [`jkit prune`](/reference/commands/prune/) frees space.
