---
title: Troubleshooting
description: Fixes for common jkit problems, organised by symptom, with the error messages jkit prints and what to do about each.
sidebar:
  order: 1
---

This page is for anyone whose JDK, Maven or jkit itself isn't doing what they expect. It's
organised by symptom; each entry quotes what jkit prints, says why, and gives the fix. If your
problem isn't here, see [getting help](/help/getting-help/).

## Start with status, doctor and env

Three read-only commands answer most questions. None of them change anything, and none use the
network unless you ask.

| Command | Answers |
| --- | --- |
| `jkit status` | Which JDK, Maven, Gradle, sbt and GraalVM are active here, and why |
| `jkit doctor` | Whether anything in your setup is broken or fighting jkit, with a fix for each |
| `jkit env` | The environment a new process here should get (for CI, IDEs, Docker) |

`jkit status` prints one line per tool: the version this shell has and where it comes from
(`shell`, `project`, `global`, `outside` when a `*_HOME` variable was set outside jkit, or
`not set`). When that isn't what jkit would pick, an indented second line says why and gives the
command that fixes it:

```console
$ jkit status
jdk      17.0.21.1+2 zulu        shell
         jkit.toml wants jdk 21; this shell chose another with jkit use, to follow it: jkit reset jdk
maven    3.9.16                  global
project: ~/src/app (jkit.toml)
```

`jkit doctor` runs a fixed list of checks and prints `ok`, `warn`, `fail` or `info` for each,
with a `fix:` line under anything that isn't ok. It exits `1` when a check fails, so it can gate a
CI step. `jkit doctor --network` also checks that foojay, services.gradle.org, Maven Central and
GitHub can be reached.

```console
$ jkit doctor
ok    jkit 0.4.0, linux-x64, home ~/.jkit
warn  shell hook not loaded: jkit use can't change this shell, and the global versions aren't on PATH
      fix: jkit hook --install, then open a new shell
ok    jkit on PATH: /usr/bin/jkit
ok    default vendors: temurin (jdk), graalvm-ce (graalvm)
ok    global links and this shell's choices point at existing installs
ok    no *_HOME variable set outside jkit
ok    3 installs, nothing left over
ok    project ~/src/app (jkit.toml): 1 pin, all installed
info  version index: 8 cached lists, newest 0 min old (refetched after 24 h)
ok    active JDK runs: openjdk version "21.0.13.1" 2026-11-17 LTS (~/.jkit/tools/jdk/21.0.13.1+2-temurin)
1 warning, 0 problems
```

`jkit env` prints the environment for a new process in this folder: the global versions,
overridden by the project file. It's what to compare against when a tool started outside your
shell (an IDE, a CI step) sees a different JDK. See [jkit env](/reference/commands/env/).

## `java -version` shows the wrong version

Run `jkit status` first. The second line under `jdk` usually names the cause.

**This shell chose another version with `jkit use`.** A `jkit use` in this shell wins over the
project file and the global default until you reset it:

```console
$ jkit status
jdk      17.0.21.1+2 zulu        shell
         jkit.toml wants jdk 21; this shell chose another with jkit use, to follow it: jkit reset jdk
```

Run `jkit reset jdk`. See [which version wins](/guides/switching-versions/).

**The project wants a version this shell doesn't have yet.**

```console
$ jkit status
jdk      -                       not set
         jkit.toml wants jdk 21.0.13.1+2 temurin; this shell has none: jkit sync
project: ~/src/app (jkit.toml)
```

Run `jkit sync`. It installs what the project file asks for and switches this shell. To have this
happen on every `cd`, turn on [auto-switch](#auto-switch-doesnt-switch).

**Another `java` comes first on PATH.** `jkit doctor` says so:

```console
$ jkit doctor
warn  'java' on PATH is /usr/bin/java, not $JAVA_HOME/bin/java
      fix: load the jkit hook after other PATH changes in your shell profile, or remove the other java
```

Something in your startup files (SDKMAN, a distribution's `java` alternatives, a manual
`export PATH=...`) runs after the jkit hook and puts its own folder first. Move the jkit line to
the end of `~/.bashrc`, `~/.zshrc` or fish's config, then open a new shell.

**`JAVA_HOME` is set somewhere else.** See [`JAVA_HOME` points somewhere else](#java_home-points-somewhere-else).

## `jkit use` says the hook isn't loaded

```console
$ jkit use jdk@17 zulu
error: the jkit shell hook is not active, so this shell can't be changed
  load it in new shells: jkit hook --install
  or set the default: jkit use jdk@17 zulu --global
  or run one command with it: jkit exec jdk@17 zulu -- <command>
```

`jkit use` without `--global` changes the current shell, and a program can't change the shell
that started it. The [shell hook](/reference/shell-hook/) is a small shell function that makes this
possible. Fix:

```bash
jkit hook --install      # adds a marked block to ~/.bashrc, ~/.zshrc or fish's config
# then open a new shell
```

On Windows, `jkit hook --install` doesn't edit PowerShell profiles yet. Add the hook by hand:
`notepad $PROFILE`, then add this line:

```powershell
jkit hook pwsh | Out-String | Invoke-Expression
```

If you've installed the hook but `jkit doctor` says this:

```text
warn  JKIT_SHELL=bash is inherited, but the jkit function isn't defined here (a subshell or script?), so jkit use can't change this shell
```

you're in a script or subshell that inherited the hook's variables but not its function. Run jkit
from your interactive shell, load the hook in that script too (`eval "$(jkit hook bash)"`), or use
`jkit exec` for one command. Scripts and AI agents should use [jkit exec](/reference/commands/exec/)
anyway; see [scripts](/guides/scripts/).

## `JAVA_HOME` points somewhere else

```console
$ jkit status
jdk      ?                       outside   JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
         JAVA_HOME is set outside jkit and wins over the global jdk; remove it from your shell profile to use jkit's
```

```console
$ jkit doctor
warn  JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 is set outside jkit and wins over the global jdk
      fix: remove the export from your shell profile (~/.bashrc, ~/.zshrc, ~/.profile)
```

Something exports `JAVA_HOME` before or after the hook runs, and jkit leaves a value it didn't set
alone. Look for `JAVA_HOME` in `~/.bashrc`, `~/.bash_profile`, `~/.zshrc`, `~/.profile` and
`/etc/environment`:

```bash
grep -n JAVA_HOME ~/.bashrc ~/.bash_profile ~/.zshrc ~/.profile /etc/environment 2>/dev/null
```

Remove the line, then open a new shell. The same applies to `MAVEN_HOME`, `GRADLE_HOME`, `SBT_HOME`
and `GRAALVM_HOME`.

A related warning means a value jkit set earlier is left over in this shell:

```text
JAVA_HOME=~/.jkit/tools/jdk/17.0.21.1+2-zulu is a jkit install nothing chooses any more
```

Open a new shell, or run `jkit reset jdk`.

## `command not found: java` (or `mvn`) after install

After `jkit install`, check its last lines:

```text
  the jkit hook is not loaded in this shell, so nothing sets JAVA_HOME; load it in new shells: jkit hook --install
```

The hook is what puts `~/.jkit/global/jdk/bin` on `PATH`. Run `jkit hook --install` and open a
new shell. A shell that was already open before you installed the hook doesn't have it.

If the hook is loaded, the tool may have no default yet. Only the first build of each tool you
install becomes the default; after that, install prints:

```text
make it the default: jkit use jdk@17 zulu --global
```

Run that command, or check with `jkit list` (the default is marked `global`). `jkit install
--no-default` and `jkit reset jdk --global` also leave a tool with no default.

If the hook is loaded and there is a default but the command is still missing, `jkit doctor` says:

```text
warn  'mvn' isn't on PATH, though MAVEN_HOME is set by jkit
      fix: open a new shell; if that doesn't help, load the jkit hook after other PATH changes
```

In a script, CI job or container with no hook, use `jkit exec -- java -version` or
`eval "$(jkit env)"`. See [CI](/guides/ci/) and [Docker](/guides/docker/).

## Auto-switch doesn't switch

Switching versions when you `cd` into a project has a few conditions.

**It's off by default.** Check with `jkit config`:

```console
$ jkit config
jdk.vendor         temurin     default
graalvm.vendor     graalvm-ce  default
shell.auto_switch  false       default
```

Turn it on, then open a new shell. A shell that's already open keeps the hook it loaded.

```bash
jkit hook --install --auto-switch      # or: jkit config set shell.auto_switch true
```

`JKIT_AUTO_SWITCH=1` turns it on through the environment, and `0` turns it off.

**PowerShell doesn't switch on `cd` yet.** `jkit hook pwsh` says so when auto-switch is on:

```text
warning: shell.auto_switch isn't supported in pwsh yet, so versions won't switch on cd; jkit sync switches this shell by hand
```

Run `jkit sync` after you enter the project.

**bash switches when the next prompt is drawn.** The hook runs from `PROMPT_COMMAND`, so a one-liner
such as `cd app && java -version` still runs the old JDK. Use `jkit exec -- java -version` in
one-liners and scripts. zsh and fish switch during the `cd` itself (fish only in interactive
shells).

**A `jkit use` in this shell wins.** A version chosen with `jkit use` keeps winning after you
enter a project, and the hook says so. `jkit reset jdk` goes back to the project's version.

**The version isn't installed.** Entering a project never downloads anything. A missing version
stays on its shell or global choice, with a note to run `jkit sync`.

**The project file is ignored.** See the next section.

## Project file ignored

```console
$ jkit status
project: none (ignoring ~/src/app/jkit.toml: every user can write to the folder)
```

```console
$ jkit sync
error: ignoring /home/you/src/app/jkit.toml: every user can write to the folder
  jkit ignores project files in folders other users can change; fix the folder's owner or permissions, or set JKIT_PROJECT_FILE to the file's absolute path
```

A project file decides which programs run when you type `java`, so jkit ignores one that someone
else could have written, as git does with `safe.directory`. The reasons it gives:

| Message ends with | Meaning |
| --- | --- |
| `every user can write to the folder` | The folder is world-writable |
| `every user can write to it (chmod o-w to use it)` | The file itself is world-writable |
| `the folder belongs to <user>` / `it belongs to <user>` | Owned by someone other than you or root |
| `it is a link to a file outside its folder` | A symbolic link that points out of the project |

Where this comes up:

- **WSL, on `/mnt/c`.** Windows drives are mounted with every file world-writable by default.
  Keep projects in the Linux file system (`~/src/...`), or change the mount options. See
  [WSL](/guides/wsl/).
- **Docker bind mounts and dev containers.** Files mounted from the host belong to the host's
  user id, which often isn't the container's user. Run the container as the same uid, or set
  `JKIT_PROJECT_FILE`. See [Docker](/guides/docker/).
- **SMB and other network shares**, which often show every file as world-writable.
- **Shared folders** such as `/tmp` or a group checkout.

Fixes, in order of preference:

```bash
chmod o-w ~/src/app ~/src/app/jkit.toml               # stop other users writing to it
export JKIT_PROJECT_FILE="$HOME/src/app/jkit.toml"    # an absolute path is your own choice and skips the checks
```

A folder name that isn't valid UTF-8 gives `its name isn't valid UTF-8`; rename the folder, or set
`JKIT_PROJECT_FILE`. See [project files](/reference/project-files/) and
[shared machines](/guides/shared-machines/).

## No build for this vendor or version

```console
$ jkit install jdk@8 microsoft
error: microsoft has no build matching jdk@8 for linux-x64
  available from: bisheng, corretto, dragonwell, kona, liberica, openjdk, openlogic, semeru, temurin, trava, zulu
```

Not every vendor builds every release for every platform. Pick a vendor from the list, or see
what exists with `jkit search jdk` (a table of vendors and releases) and `jkit search jdk@8`.
For older releases on Apple Silicon or Windows, the list is often short.

A misspelt vendor gives a suggestion:

```console
$ jkit install jdk@21 temurn
error: unknown JDK vendor 'temurn'
  did you mean: temurin?
  vendors: bisheng, corretto, dragonwell, eliya, gluon-graalvm, graalvm, graalvm-ce, ...
```

Other messages in this family:

- `<vendor> <version> has no direct download`: foojay lists the build, but only behind a page on
  the vendor's site. jkit suggests an older build that has one, or download it from the vendor.
- `only pre-releases match maven@4`: add `--pre`, or give the exact version jkit prints.
- `no sbt release matches 1.2`: jkit installs sbt 1.3.0 and later. The sbt launcher still builds
  projects whose `project/build.properties` names an older sbt.
- `<host> no longer has what jkit asked for (HTTP 404)`: the cached version list is out of date.
  Add `--refresh`.

See [versions and vendors](/reference/versions-and-vendors/) and
[choosing a JDK](/guides/choosing-a-jdk/).

## Download refused: no checksum

```text
error: jdk <version> <vendor> from <host> has no published checksum, so jkit can't check the download; nothing installed
  install it anyway: jkit install jdk@<version> <vendor> --allow-unverified
  or pick another version or vendor
```

jkit checks every download against the checksum its source publishes. When there is none, it
refuses rather than install something it can't check. Your choices:

- Pick another build or vendor (`jkit search jdk@21`). Most vendors publish checksums.
- If you trust the source, rerun with `--allow-unverified`, as the hint shows. `jkit sync` and
  `jkit upgrade` take the same flag.

Related warnings that don't stop the install:

- `only a <algorithm> checksum is published for ...: it catches a broken download, not a tampered one`.
  Old Maven releases have only SHA-1.
- For Corretto, which publishes no checksum, jkit checks Amazon's signature instead and says
  `note: ... has no published checksum; checking Amazon's signature instead`. If the signature
  can't be fetched or checked, the install is refused the same way. A signed Corretto build must
  also unpack to the JDK version jkit asked for, or it is refused with
  `the download for ... is signed, but it is JDK <version>; nothing was installed`.

See [security](/reference/security/).

## Signature mismatch

```text
error: the signature of jdk <version> temurin doesn't match the download (<detail>); nothing was installed
  try again; if it keeps happening, the download or the signature is wrong at the source: pick another build or vendor
```

For Temurin, Microsoft and Corretto JDKs, Maven 3.9.0 and later, and sbt, jkit also checks the
publisher's OpenPGP signature with keys built into jkit. A signature by the right key that doesn't
match the file means the download was changed or broken. Nothing was installed. Try again once;
if it repeats, pick another build or vendor, and consider [reporting it](/help/getting-help/).

`checksum mismatch for ...` followed by `expected` and `actual` lines means the file didn't match
its published checksum; treat it the same way as a bad signature.

## Signature missing or by an unknown key

```text
error: couldn't check the signature of maven <version>: made with key <key id>, not Apache Maven's 4 keys (a newer jkit may know a new key: jkit self-update); nothing was installed
  install it anyway: jkit install maven@<version> --allow-unverified
  or pick another version or vendor
```

When a publisher signs its downloads (Temurin and Microsoft when foojay lists a signature,
Corretto, Maven 3.9.0 and later, sbt), jkit needs a good signature, not only the checksum: the
checksum usually comes from the same server as the download, so whoever can replace one can
replace both. A signature jkit can't fetch (`couldn't fetch the signature of ...`), can't read
(not an OpenPGP signature, or a weak hash), or can't tie to the publisher's built-in keys refuses
the install. `jkit sync` and `jkit upgrade` stop the same way.

Expect it most with Maven. Each Maven release is signed by whichever Apache release manager made
it, so a release by someone new is refused until a jkit release adds their key. A Maven mirror
(`JKIT_SOURCES_MAVEN_URL`) that doesn't serve the `.asc` files is refused too. Your choices:

- `jkit self-update`, then try again: a newer jkit may know the key.
- Install an earlier release (`jkit install maven@3.9.11`), or ask your mirror's admins to serve the
  `.asc` files.
- If you trust the source, rerun with `--allow-unverified`, as the hint shows. It installs on the
  checksum alone, marked unverified, and says why:

  ```text
  warning: couldn't check the signature of maven <version>: <detail> (a newer jkit may know a new key: jkit self-update); installed with only its checksum checked
  ```

## Behind a proxy, or offline

jkit reads `HTTPS_PROXY` (or `https_proxy`) and `NO_PROXY`. `JKIT_NETWORK_PROXY` overrides
`HTTPS_PROXY` for jkit only. The proxy must be an `http://` proxy; credentials go in the URL.

```bash
export HTTPS_PROXY=http://proxy.example.com:3128
export NO_PROXY=localhost,.internal.example.com
jkit doctor --network        # checks foojay, services.gradle.org, Maven Central and GitHub
```

Messages and what they mean:

| Message | Fix |
| --- | --- |
| `can't connect to <host>` | Check the connection or `HTTPS_PROXY` |
| `can't find <host> (no DNS answer)` | No network, or DNS blocked; cached lists still work with `--offline` |
| `<host> took too long to answer` | Try again; check the proxy |
| `secure connection to <host> failed: ...` | A proxy that inspects HTTPS presents its own certificate, which jkit doesn't trust |
| `got a web page instead of data from <host> (a captive portal or proxy login?)` | Sign in to the network in a browser |
| `<host> is limiting requests (HTTP 429)` | Wait a few minutes |
| `<host> is having problems (HTTP 5xx)` | Try later; `--offline` uses cached lists |

jkit has no setting for adding a company root certificate. If your proxy inspects HTTPS, ask for
the download hosts to be exempt, or point jkit at an internal mirror with
`JKIT_SOURCES_FOOJAY_URL`, `JKIT_SOURCES_MAVEN_URL`, `JKIT_SOURCES_GRADLE_URL`,
`JKIT_SOURCES_SBT_URL` and `JKIT_SOURCES_RELEASES_URL`.

**Offline.** `--offline` (or `JKIT_NETWORK_OFFLINE=1`) never contacts the network and answers
from the version lists cached by earlier commands. Installing still needs a download:

```text
error: --offline, but this lookup on api.foojay.io isn't cached (the cache only holds what earlier commands asked for)
  run the command once without --offline
```

`jkit list`, `status`, `env`, `exec` and `doctor` (without `--network`) never use the network. See [corporate networks](/guides/corporate-networks/).

## IDE uses a different JDK

IDEs don't load your shell's startup files, so they don't see the hook or a `jkit use` in a
terminal. Point the IDE at the global link instead:

```text
~/.jkit/global/jdk
```

It follows `jkit use ... --global`, so the IDE changes when you change the default. For a project
pinned to another version, point the IDE at that install (`jkit list` shows them; they're under
`~/.jkit/tools/jdk/`), or read `jkit env --json`, which gives the variables and `PATH` folders a
new process in that folder should get. Maven and Gradle settings in the IDE work the same way with
`~/.jkit/global/maven` and `~/.jkit/global/gradle`. See [IDEs](/guides/ides/).

On Windows, programs started outside a hooked PowerShell see the global JDK only if you set
`JAVA_HOME` to `%USERPROFILE%\.jkit\global\jdk` once and add `%JAVA_HOME%\bin` to your user
`PATH`. See [Windows](/guides/windows/).

## `jkit: command not found` after installing jkit

The jkit binary itself isn't on `PATH`.

- **Homebrew**: open a new terminal; check that `brew --prefix` + `/bin` is on `PATH`.
- **`.deb` or `.rpm`**: the package installs to `/usr/bin`, which is always on `PATH`. Check the
  install finished (`dpkg -L jkit` or `rpm -ql jkit`).
- **`.tar.gz` or `.zip`**: you choose where `jkit` goes. Put it in a folder on `PATH`, such as
  `~/.local/bin`, or add its folder to `PATH` in your startup file. In bash, `hash -r` makes an
  open shell look again.

If you have more than one copy, `jkit doctor` warns:

```text
warn  2 different jkit binaries on PATH, the first wins: /usr/local/bin/jkit, /usr/bin/jkit
      fix: remove the ones you don't use, e.g. an old copy in /usr/local/bin
```

See [install](/start/install/).

## glibc too old, or Alpine

jkit's Linux builds need glibc 2.34 or newer and zlib: Debian 12+, Ubuntu 22.04+, RHEL, Rocky or
Alma 9+. On an older system the binary doesn't start, and the system loader prints something like
`version 'GLIBC_2.34' not found (required by jkit)`.

Alpine uses musl instead of glibc, and there is no jkit build for it. In containers, use a glibc-based image such as
Debian or Ubuntu for the steps that run jkit. See [Docker](/guides/docker/).

## macOS blocks jkit

The macOS builds are signed and notarized by Apple, so Gatekeeper lets them run, and they need
macOS 15 or newer. If macOS refuses to open jkit:

- Check your macOS version. On older versions the binary doesn't run.
- Install from Homebrew (`brew install adriandeleon/tap/jkit`) or the
  [releases page](https://github.com/adriandeleon/jkit/releases/latest), not from a copy
  someone passed around.

Don't turn Gatekeeper off for jkit. If an official build is blocked on macOS 15 or newer, please
[open an issue](https://github.com/adriandeleon/jkit/issues).

## Windows: `jkit.exe` won't start

`jkit.exe` needs the Microsoft Visual C++ Redistributable (2015 or newer, x64). winget installs it
along with jkit, and most machines already have it. With the `.zip`, if Windows reports a missing
DLL or jkit exits without output, install it:

```powershell
winget install Microsoft.VCRedist.2015+.x64
```

Windows on ARM runs the x64 build under emulation. Windows support is a preview; see
[Windows](/guides/windows/).

## Windows: PowerShell doesn't change, or `jkit exec` says `Unknown option`

In jkit 0.4.0:

- **`jkit use`, `reset`, `pin` or `sync` print `set JAVA_HOME=…`, but `$env:JAVA_HOME` doesn't
  change** in Windows PowerShell 5.1. Its default execution policy blocks the script file the hook
  applies changes through (and keeps `$PROFILE` from loading at all). Allow scripts for your
  account once, then open a new window:

  ```powershell
  Set-ExecutionPolicy -Scope CurrentUser RemoteSigned
  ```

  PowerShell 7 (`pwsh`) allows them already.
- **`jkit exec jdk@21 -- java -version` fails with `Unknown option: '-version'`** in a PowerShell
  with the hook loaded: PowerShell drops the `--` it passes to the hook's `jkit` function. Run
  `jkit.exe exec jdk@21 -- java -version`, which skips the function.

Both are fixed for the release after 0.4.0.

## `jkit remove` refuses

jkit won't remove a build that's the global default or chosen in this shell, because that would
leave `java` pointing at nothing:

```console
$ jkit remove jdk@17 zulu
error: jdk 17.0.21.1+2 zulu is global; nothing removed
  make another version the default first: jkit use jdk@<version> --global
  or clear the default: jkit reset jdk --global
  or remove it anyway (the link goes too): add --force
```

Follow one of the hints. A version that matches several installed builds is also refused, with
the list and two hints: `remove one:` with the exact version, and `remove all:` with `--all`.
A project file that pins the build gives a warning, not a refusal.

Other messages:

- `permission denied moving ~/.jkit/tools/...`: check the folder's owner with the `ls -ld`
  command jkit prints. A jkit home written to with `sudo` is the usual cause.
- On Windows, `can't remove ...` with `something may have it open (IDE or Gradle daemon?)`: run
  `gradle --stop`, close the IDE or terminal using that JDK, and try again.

See [jkit remove](/reference/commands/remove/).

## Disk filling up

Each JDK is a full copy, and `jkit upgrade` installs new builds next to the old ones without
deleting them. To see and free space:

```bash
jkit list                 # what's installed
jkit prune --dry-run      # what prune would remove, and how much space it frees
jkit prune
```

`jkit prune` removes older builds within a release line once a newer one is installed (21.0.5 once
21.0.9 is in), unless that build is global, used in this shell or pinned here; folders left by
interrupted installs and removals; and cached version lists older than 30 days. A project
elsewhere that pins an exact old build gets it back with `jkit sync`.

To remove a whole release line, use `jkit remove jdk@17 --all`. `jkit doctor` also reports
unfinished installs and leftovers in `~/.jkit/tmp`, with the command to clear them. See
[keeping up to date](/guides/keeping-up-to-date/).

## Other errors

- `error: unexpected error: ...` is a bug. Run the command again with `JKIT_DEBUG=1` set for
  details, and [report it](/help/getting-help/).
- `permission denied: <file>` with `check the folder's owner, or set JKIT_HOME to a folder you can write`:
  part of `~/.jkit` belongs to another user, often after running jkit with `sudo`.
- `/home/you/.jkit is writable by every user, so anyone could put programs on your PATH`: run the
  `chmod o-w` command jkit prints. jkit refuses to run with an unsafe home; see
  [shared machines](/guides/shared-machines/).
- `invalid /home/you/.jkit/config.toml: ...`: fix the file, or move it away to use the defaults.
  `jkit config` shows each setting and where it comes from.

Every error goes to stderr as `error: ...` followed by indented hints, and `--json` puts the same
message and hints in `error.message` and `error.hints`. Exit codes are listed in
[exit codes](/reference/exit-codes/).
