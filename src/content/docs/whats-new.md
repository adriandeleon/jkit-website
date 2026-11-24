---
title: What's new
description: Every jkit release and what changed in it, newest first.
tableOfContents:
  maxHeadingLevel: 2
---

Every jkit release, newest first. The [releases page](https://github.com/adriandeleon/jkit/releases)
has the downloads and the full list of changes; [News](/news/) has the longer stories. To update,
run `jkit self-update` (see [Keeping up to date](/guides/keeping-up-to-date/)).

## 0.4.0

Settings, disk space, self-updates and signature checks.

- **`jkit config`** lists every setting, its value and where that value comes from (an environment
  variable, `config.toml` or the default). `jkit config set jdk.vendor zulu` and
  `jkit config unset jdk.vendor` change the file and keep its comments.
  [Reference](/reference/commands/config/)
- **`jkit prune`** frees disk space: older builds in a release line once a newer one is installed
  (never one that is global, used in this shell or pinned here), folders left by interrupted
  installs, and cached version lists older than 30 days. `--dry-run` shows what would go and how
  much space it frees. [Reference](/reference/commands/prune/)
- **`jkit self-update`** updates jkit itself. A jkit from a release archive is replaced in place
  after checking the download against the release's `SHA256SUMS`, which must be signed by jkit's
  release key (`SHA256SUMS.asc`, built into jkit); for Homebrew, apt, dnf and winget it prints
  that tool's upgrade command. `--check` only says whether a newer jkit is out.
  [Reference](/reference/commands/self-update/)
- **OpenPGP signature checks.** Temurin, Microsoft and Corretto JDKs, Maven 3.9.0 and later, and
  sbt are now checked against their publisher's signature, with keys built into jkit, on top of
  the checksum. A signature that doesn't match stops the install. So does one jkit can't fetch,
  read, or tie to the publisher's keys, unless you pass `--allow-unverified`, which installs on
  the checksum alone, marked unverified. Expect that with a Maven release by a new Apache release
  manager, until a jkit release adds their key. Corretto publishes no checksum, so its signature
  is the check, and Corretto installs no longer need `--allow-unverified`.
  [Security](/reference/security/)
- **`eval "$(jkit env)"` fails when `jkit env` does.** On failure it prints `false` on stdout
  (`throw '…'` for PowerShell), so a `set -e` script or CI step stops there.
  [Reference](/reference/commands/env/)
- Smaller fixes: `jkit upgrade jdk zuluu` says `unknown JDK vendor` with a suggestion (exit 2)
  instead of finding nothing to upgrade; `jkit config set jdk.vendor "SAP Machine"` accepts
  foojay's other names for a vendor; `config set` and `config unset` can repair a `config.toml`
  with a wrong value; `jkit prune` carries on past a build it can't remove.

## 0.3.0

*24 November 2026.* The first public release.

- **Project files and switching on `cd`.** `jkit pin`, `jkit unpin` and `jkit sync` write and
  apply a `jkit.toml`. With auto-switch on, bash, zsh and fish switch to a project's versions when
  you `cd` into it, and back when you leave. [Projects](/guides/projects/)
- **`jkit hook --install`** adds the hook to `~/.bashrc`, `~/.zshrc` or fish's `conf.d` for you,
  and `--uninstall` takes it out.
- **sbt**, next to the JDKs, GraalVM, Maven and Gradle.
- **`jkit exec`** runs one command with the right versions and no shell change, for scripts, CI
  and AI agents. [AI agents](/guides/ai-agents/)
- **`jkit status`, `jkit doctor` and `jkit env`** say which version is active here and why, check
  the setup and say how to fix each problem, and print the environment for CI and IDEs.
  [Troubleshooting](/help/troubleshooting/)
- **Every change is printed**: each file, folder, link and variable a command touches.
- **Packages**: Homebrew, apt (through the Vitrina repository), winget, `.deb`, `.rpm`, and zips
  for every platform. macOS binaries are signed and notarized.
- Many fixes from security and correctness reviews: archive extraction, installs, network
  handling, shells, and the release pipeline.

## 0.2.2

*22 November 2026.*

- Reads project files: `jkit.toml`, `.sdkmanrc`, `.tool-versions` and `.java-version`.
- `--json` output for editors and scripts. [JSON output](/reference/json/)
- Reports environment and link changes after each command.
- Signed and notarized macOS binaries for arm64 and x64 (macOS 15 or newer).

## 0.2.0

*20 November 2026.*

- `jkit upgrade`: the newest build of each installed major version.
- `jkit search jdk` shows every vendor against every major version as a grid.
- JavaFX builds and plain builds are kept apart.
- Bash tab completion.

## 0.1.1

*20 November 2026.* Clear errors for unknown vendors and network failures.

## 0.1.0

*20 November 2026.* The first build: install and switch JDKs, Maven and Gradle.
