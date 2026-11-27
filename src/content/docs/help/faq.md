---
title: FAQ
description: Short answers to common questions about jkit, from cost and privacy to vendors, other version managers, platforms, projects and agents.
sidebar:
  order: 2
---

Short answers to the questions people ask most, for newcomers and experienced Java developers
alike. Each answer links to the page with the details.

## Basics

### Is jkit free?

Yes. jkit is open source under the MIT licence, and there is no paid version. The JDKs it
installs come from their vendors under the vendors' own licences; see
[the Oracle question](#is-oracle-jdk-free-for-my-use) below.

### Does jkit need Java to run?

No. jkit is a single native program built with GraalVM native image, so it starts without a JDK.
You can install it on a machine with no Java at all and use it to get your first JDK. See
[install](/start/install/).

### What does jkit manage?

JDKs from 20+ vendors, GraalVM, Maven, Gradle and sbt. It doesn't install Kotlin, Groovy, Scala,
JBang or other tools. Those either run on the JDK jkit gives you, or come through your build
(the Kotlin and Scala compilers through Gradle, Maven or sbt). See
[versions and vendors](/reference/versions-and-vendors/).

### Where does it put things?

Everything goes in `~/.jkit`: installs in `~/.jkit/tools/`, the global defaults as links in
`~/.jkit/global/`, cached version lists in `~/.jkit/index/`, and settings in
`~/.jkit/config.toml`. `JKIT_HOME` moves all of it. Outside it, jkit changes your shell's startup
file, where `jkit hook --install` adds a short marked block, a project's `jkit.toml` when you run
`jkit pin`, and on Windows your user environment variables when you run `jkit env --install`. See
[files and folders](/reference/files-and-folders/).

### Does it touch the Java my system already has?

No. jkit never changes, replaces or removes a JDK it didn't install, and doesn't touch
`/usr/lib/jvm`, `update-alternatives` or `/Library/Java`. In a shell with the hook, jkit's version
comes first on `PATH`, so `java` means jkit's. Without the hook, or in a program that doesn't load
your shell's startup files, the system Java is still what you get. See
[OS packages](/why/vs-os-packages/).

### Do I need `sudo`?

Only to install jkit itself with a system package manager (`apt`, `dnf`). jkit's own commands never
need it: everything goes in your home folder. Don't run jkit with `sudo`; files it creates as root
can leave parts of `~/.jkit` you can't change later. For machines shared by several people, see
[shared machines](/guides/shared-machines/).

### Why `jdk@21` and not `21.0.5-tem`?

jkit names the tool, then the version, then the vendor as a separate word:
`jkit install jdk@21 zulu`. The version is a prefix, so `21` means the newest 21 and `21.0.5` the
newest 21.0.5 build; the vendor is optional and defaults to Temurin. The same form works for every
tool (`maven@3.9`, `gradle@8.14`), and `@lts` and `@latest` work too. jkit still reads SDKMAN's
`21.0.5-tem` ids in `.sdkmanrc` files. See [versions and vendors](/reference/versions-and-vendors/).

### Why doesn't jkit ask before it does things?

So the same command works the same way in a terminal, a script, CI and an AI agent. No command
prompts or reads from the keyboard. Instead, jkit prints every change it makes (folders created,
links set, variables changed), and the risky ones need a flag: `--force` to remove a default,
`--allow-unverified` to install a download with no checksum, or one whose publisher's signature
can't be checked. Commands that change a lot have
`--dry-run`.

## Choosing a JDK

### Which vendor should I use?

If you have no reason to choose, use the default, Temurin (Eclipse Adoptium): a free, widely used
OpenJDK build for every common platform. Pick another vendor when you need something it offers:
a build from your cloud provider (Corretto, Microsoft), JavaFX included (`--fx`), GraalVM's
native image, or a vendor your company has a support contract with. See
[choosing a JDK](/guides/choosing-a-jdk/).

### What does LTS mean?

Long-term support. A new Java feature release comes out every six months; some of them (8, 11, 17,
21, 25) are LTS releases that vendors keep updating with fixes for years. Most projects use an LTS
release. `jkit install jdk@lts` installs the newest one. See [Java basics](/start/java-basics/).

### Is Oracle JDK free for my use?

That depends on Oracle's licence for the version you install and on how you use it, and Oracle
has changed those terms over time. jkit can install Oracle's builds (`jkit install jdk@21 oracle`),
but it doesn't accept a licence for you or check that your use is covered. Read the current terms
on [Oracle's Java download pages](https://www.oracle.com/java/technologies/downloads/) before you
use Oracle JDK or Oracle GraalVM at work. If you'd rather not think about it, the OpenJDK builds
from Temurin, Corretto, Zulu, Microsoft and others are free to use.

### Does it work on Apple Silicon?

Yes. jkit runs natively on Apple Silicon and Intel Macs (macOS 14 Sonoma or newer) and installs the
arm64 build of each JDK when the vendor has one. `jkit search jdk` shows what exists for your
machine.

### Does it work on Windows?

Yes, as a preview: it's less tested than macOS and Linux. `jkit hook --install` doesn't edit
PowerShell profiles yet (add one line by hand), PowerShell doesn't switch versions on `cd`, and
Windows on ARM runs the x64 build under emulation. `jkit env --install` gives programs started
outside PowerShell (cmd.exe, IDEs) the global versions too. See [Windows](/guides/windows/) and
[WSL](/guides/wsl/).

### Does it work on Alpine Linux?

Yes, on x64, from jkit 0.5.0. Each release has a static Linux x64 build linked against musl,
`jkit-<version>-linux-x64-musl.tar.gz`, which needs nothing from the system. There is no arm64
musl build yet; on ARM, use a Debian- or Ubuntu-based image for the steps that run jkit. See
[Docker](/guides/docker/).

## Living with other tools

### Can jkit coexist with SDKMAN, asdf, mise or Homebrew's Java?

Yes, they don't share files. What matters is which one puts its `java` first on `PATH` and sets
`JAVA_HOME`: the one whose shell setup runs last wins. `jkit doctor` tells you when another tool's
`java` or `JAVA_HOME` wins over jkit's. jkit also reads `.sdkmanrc`, `.tool-versions` and
`.java-version` files, so a project set up for those tools works with jkit unchanged. See
[migrating](/guides/migrating/), [jkit and SDKMAN](/why/vs-sdkman/) and
[jkit, mise and asdf](/why/vs-mise-asdf/).

### Should I keep using the Maven or Gradle wrapper?

Yes. A project's `mvnw` or `gradlew` pins the build tool version, and jkit provides the JDK it
runs on: `jkit exec -- ./gradlew build`. Install Maven or Gradle with jkit for projects that have
no wrapper. See [build tools](/guides/build-tools/).

### What does my IDE use?

Whatever JDK you point it at. Point it at `~/.jkit/global/jdk` and it follows
`jkit use ... --global`. On Windows, `jkit env --install` sets `JAVA_HOME` in your user
environment to that link, for IDEs that read it. See [IDEs](/guides/ides/).

## Projects and teams

### How do I make a project use a given JDK?

`jkit pin jdk@21` writes it to the project's `jkit.toml`. Anyone with jkit runs `jkit sync` to
install and switch to it, and with auto-switch on, the shell switches on `cd`. See
[projects](/guides/projects/).

### How do I pin an exact build?

`jkit pin jdk@21 --exact` writes the full version and vendor of the build it resolves to, such as
`21.0.13.1+2` from Temurin, so every machine and CI run gets the same build. There is no separate
lock file. See [jkit pin](/reference/commands/pin/).

### Does it work in a monorepo?

Yes. jkit looks for a project file in the current folder, then each parent, and uses the first
folder that has one. Put a `jkit.toml` at the root for the whole repository, and another in a
subfolder that needs something different. A nested file replaces the outer one completely; the
two are never merged. See [project files](/reference/project-files/).

### Can I use it in CI and Docker?

Yes. Without the shell hook, run commands through `jkit exec -- mvn verify`, or load the
environment with `eval "$(jkit env)"`. See [CI](/guides/ci/) and [Docker](/guides/docker/).

### Can AI agents use it?

Yes, it was designed with them in mind. Each agent command runs in a new shell, so agents should
use `jkit exec` rather than `jkit use`. Nothing prompts, `--json` prints one JSON document even on
failure, and exit codes are stable (`3` means the version isn't installed). See
[AI agents](/guides/ai-agents/).

## Security and privacy

### How are downloads verified?

Downloads come over HTTPS only and are checked against the checksum their source publishes
(SHA-256 or stronger counts as verified). A download with no checksum is refused unless you pass
`--allow-unverified`. Temurin, Microsoft and Corretto JDKs, Maven 3.9.0 and later, and sbt are
also checked against their publisher's OpenPGP signature, with keys built into jkit. A signature
that doesn't match stops the install; one that is missing or isn't by the publisher's key stops
it too, unless you pass `--allow-unverified`. JDKs from vendors jkit has no key for (Zulu, Liberica
and others) must come from the vendor's own download server. See [security](/reference/security/).

### Does jkit phone home or collect telemetry?

No. jkit has no telemetry, analytics or crash reporting, and doesn't check for updates on its
own. It uses the network only when a command needs it: `search`, `install`, `upgrade`, `sync`,
`pin` (to check a version exists), `config set` for a vendor, `doctor --network` and
`self-update`. It then talks only to the sources it downloads from: the foojay API
(`api.foojay.io`) for the list of JDKs, the vendors' download servers, `services.gradle.org`,
Maven Central (`repo.maven.apache.org`), and GitHub for sbt and jkit's own releases. Requests
carry a `jkit/<version>` user agent. `list`, `status`, `env` and `exec` never use the network.

This website is separate from the tool: it counts visits with Cloudflare Web Analytics, which uses
no cookies and collects no personal data.

### Can I use it offline?

Partly. Everything that works with what's installed (`use`, `exec`, `env`, `status`, `list`)
works offline. `--offline` (or `JKIT_NETWORK_OFFLINE=1`) makes `search` and other lookups answer
from the version lists cached by earlier commands. Installing needs a download, from the internet
or a mirror you point jkit at. See [corporate networks](/guides/corporate-networks/).

## Keeping up to date

### How do I update jkit?

`jkit self-update`. For a jkit installed from a `.tar.gz` or `.zip`, it replaces the binary after
checking the download against the release's `SHA256SUMS`, which must be signed by jkit's release
key. For Homebrew, apt, dnf and winget it
prints that tool's upgrade command instead. `jkit self-update --check` only says whether a newer
release is out.

### How do I update my JDKs?

`jkit upgrade` installs the newest build within each release you have (21.0.5 to 21.0.9), and
moves a global default along with it. Old builds stay until you run `jkit prune`. See
[keeping up to date](/guides/keeping-up-to-date/).

### How do I uninstall jkit?

`jkit hook --uninstall`, then delete `~/.jkit`, then remove the binary. See
[uninstall](/guides/uninstall/).

## Help and contributing

### Where can I ask a question?

In [GitHub Discussions](https://github.com/adriandeleon/jkit/discussions). Ideas and feedback are
welcome there too. See [getting help](/help/getting-help/#ask-a-question).

### How do I report a bug?

Open an [issue on GitHub](https://github.com/adriandeleon/jkit/issues) with `jkit --version`,
your OS, and the output of `jkit doctor`. See [getting help](/help/getting-help/).

### How do I report a security problem?

Privately, not in a public issue: use
[Report a vulnerability](https://github.com/adriandeleon/jkit/security/advisories/new) on the
repository's Security tab. See [security](/reference/security/).

### What licence is jkit under?

MIT. Bundled components and their licences are listed in the `THIRD-PARTY-NOTICES` file that
comes with every release.
