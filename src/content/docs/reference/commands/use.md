---
title: jkit use
description: Switch the current shell, or every new shell with --global, to an installed JDK, GraalVM, Maven, Gradle or sbt version.
sidebar:
  order: 4
---

Activates an installed version, either for this shell or, with `--global`, as the default for new
shells and apps. It never downloads.

## Usage

```text
jkit use [-hV] [--fx] [--global] [--json] <tool>[@version] [<vendor>]
```

## What it does

`jkit use` picks the newest installed build that matches, from the vendor you typed or else your
default vendor. Builds from other vendors don't count, even if they match the version; when
nothing matches, it exits with code 3 and names the other vendors you have it from. Plain builds
win over JavaFX builds unless you pass `--fx`. `@lts` means the newest LTS release you have
installed. See [how a version resolves](/reference/versions-and-vendors/#choosing-an-install-use-exec-remove).

### With `--global`

It points the link `~/.jkit/global/<tool>` at the install, and changes no files. Shells with the
hook already use the link for `JAVA_HOME` (or `MAVEN_HOME`, …) and `PATH`, so open shells follow it
at once, unless they chose their own version with `jkit use` or a project overrides it. IDEs and
tools pointed at `~/.jkit/global/jdk` follow it too. In a shell with the hook where the tool's
variable was unset (no global version when the shell started), it also sets it to the link and
puts the link's `bin` on `PATH`, and says so.

If the install already is the global one, it says so (`the global maven is already 3.9.16,
unchanged`) and changes nothing.

`--global` works without the shell hook. If the project file here pins the tool, jkit reminds you
that the pin wins in this project. On Windows it also says whether programs started outside
PowerShell (cmd.exe, IDEs) follow it, which needs
[`jkit env --install`](/reference/commands/env/#windows-the-user-environment).

### Without `--global`

It changes only the current shell, through the [shell hook](/reference/shell-hook/): it sets the
tool's `*_HOME` variable to the install folder, puts its `bin` folder first on `PATH` (removing
the one it replaces), and records the choice in `JKIT_SHELL_<TOOL>`. Other shells, and new ones,
are unaffected.

A shell choice wins over the project file and the global default until you run
[`jkit reset`](/reference/commands/reset/) or close the shell. If the project here pins the tool,
jkit says that this shell now overrides it.

Without the hook, `jkit use` can't change the shell that started it, so it refuses (exit code 1)
and offers `--global` or `jkit exec` instead. Choosing a GraalVM-based JDK also points
`GRAALVM_HOME` at it, unless you chose a `graalvm` separately.

## Options

| Option | Description |
| --- | --- |
| `<tool>[@version]` | `jdk`, `graalvm`, `maven`, `gradle` or `sbt`, with a version prefix, `@lts` or `@latest` (the newest installed). |
| `<vendor>` | A JDK vendor. Default: your default vendor (`jdk.vendor` or `graalvm.vendor`). |
| `--global` | Make it the default for new shells and apps. |
| `--fx` | Use a JavaFX build. Without it, plain builds win when both are installed. |
| `--json` | Print one JSON document on stdout instead of text. |
| `-h`, `--help` | Show help and exit. |
| `-V`, `--version` | Print jkit's version and exit. |

## Examples

Set the default for new shells:

```console
$ jkit use jdk@17 zulu --global
global jdk → 17.0.21.1+2 zulu
  link   ~/.jkit/global/jdk → ~/.jkit/tools/jdk/17.0.21.1+2-zulu
  new shells with the jkit hook: JAVA_HOME=~/.jkit/global/jdk, PATH includes ~/.jkit/global/jdk/bin
  jkit.toml here pins jdk 21, which wins over the global jdk in this project
```

Switch only this shell:

```console
$ jkit use jdk@21
using jdk 21.0.13.1+2 temurin in this shell
  set    JKIT_SHELL_JDK=~/.jkit/tools/jdk/21.0.13.1+2-temurin
  set    JAVA_HOME=~/.jkit/tools/jdk/21.0.13.1+2-temurin
  PATH   + ~/.jkit/tools/jdk/21.0.13.1+2-temurin/bin
  only this shell; for new shells too: jkit use jdk@21.0.13.1+2 temurin --global
  overrides jkit.toml's jdk 21 in this shell; jkit reset jdk to go back to it
```

Without the hook:

```console
$ jkit use jdk@21
error: the jkit shell hook is not active, so this shell can't be changed
  load it in new shells: jkit hook --install
  or set the default: jkit use jdk@21 --global
  or run one command with it: jkit exec jdk@21 -- <command>
```

Not installed from that vendor:

```console
$ jkit use jdk@17
error: no installed JDK matches 17 from temurin (default vendor)
  install it:          jkit install jdk@17
  or use installed:    jkit use jdk@17 zulu
```

More:

```bash
jkit use maven@3.9 --global
jkit use jdk@lts                 # newest LTS release you have from your default vendor
jkit use jdk@21 zulu --fx        # a JavaFX build
jkit use graalvm@25              # GRAALVM_HOME only; JAVA_HOME stays
```

## Exit codes

| Code | When |
| --- | --- |
| `0` | Switched. |
| `1` | Without `--global`: the shell hook isn't active. Or the install was removed by another jkit meanwhile. |
| `2` | Unknown tool, a version that isn't one, a vendor on Maven, Gradle or sbt, or a non-GraalVM vendor for `graalvm`. |
| `3` | No installed build matches. |

## JSON

`--json` prints the `scope` (`global` or `shell`), the install object, `changed` (`false` when
`--global` names the install that already is the global one), `changes` and `notes`. See
[`jkit use` in JSON output](/reference/json/#jkit-use).

## See also

- [`jkit reset`](/reference/commands/reset/) to undo it
- [`jkit exec`](/reference/commands/exec/) for one command, without a shell change
- [`jkit pin`](/reference/commands/pin/) for a project
- [Switching versions](/guides/switching-versions/)
