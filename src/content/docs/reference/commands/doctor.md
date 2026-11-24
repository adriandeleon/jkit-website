---
title: jkit doctor
description: Check the jkit setup for problems (hook, PATH, JAVA_HOME set elsewhere, broken links, leftovers, project file, cache) and say how to fix each one.
sidebar:
  order: 13
---

Checks your setup for problems and says how to fix them. It changes nothing, and uses the network
only with `--network`.

## Usage

```text
jkit doctor [-hV] [--json] [--network]
```

## What it does

It runs a fixed list of checks and prints one line per finding, with a level and, for anything
that isn't `ok`, a `fix:` line with what to do:

| Level | Meaning |
| --- | --- |
| `ok` | Fine. |
| `info` | Worth knowing; nothing to fix. |
| `warn` | Something that probably isn't what you want. |
| `fail` | Something broken. `jkit doctor` exits 1. |

The checks, in order:

| Id | Checks |
| --- | --- |
| `home` | jkit's version, platform and home; that the home is writable. |
| `hook` | The shell hook is loaded in this shell, and the `jkit` function is defined (not only inherited by a subshell or script). |
| `binary` | `jkit` is on `PATH`, and only one copy of it. |
| `config` | `config.toml` reads; your default vendors are ones foojay knows (checked against the cached vendor list, if there is one). |
| `links` | Global links and this shell's `jkit use` choices point at installs that exist. |
| `outside-vars` | `JAVA_HOME`, `MAVEN_HOME`, `GRADLE_HOME`, `SBT_HOME` or `GRAALVM_HOME` set outside jkit, and jkit installs nothing chooses any more. |
| `path-order` | `java`, `mvn`, `gradle` and `sbt` on `PATH` are the ones jkit chose, not another copy earlier on `PATH`. |
| `store` | Unfinished installs and leftovers of interrupted downloads. |
| `project` | The project file here: whether it reads, was ignored (and why), its warnings, and pins that aren't installed. |
| `index` | How many version lists are cached and how old the newest is; whether offline mode is on. |
| `java-runs` | The active JDK's `java -version` runs and finishes within 10 seconds. |
| `network` | Only with `--network`: foojay, services.gradle.org, Maven Central and GitHub (sbt's downloads) can be reached, honouring your proxy and mirror settings. |

A check that crashes becomes a `fail` line of its own; the other checks still run. The last line
counts warnings and problems, or says `no problems found`.

## Options

| Option | Description |
| --- | --- |
| `--network` | Also check that foojay, services.gradle.org, Maven Central and GitHub (sbt) can be reached. |
| `--json` | Print one JSON document on stdout instead of text. |
| `-h`, `--help` | Show help and exit. |
| `-V`, `--version` | Print jkit's version and exit. |

## Examples

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

A pin that isn't installed:

```console
$ jkit doctor
…
warn  jkit.toml wants jdk 25, not installed
      fix: jkit install jdk@25
…
```

As a CI gate:

```bash
jkit doctor || exit 1          # fails the step only on fail-level findings
jkit doctor --json | jq '.checks[] | select(.level == "fail")'
```

## The usual culprits

- **`JAVA_HOME` set in your shell profile** (`~/.bashrc`, `~/.profile`, `/etc/environment`): it
  wins over jkit's choice. Remove the export.
- **The hook loads before something else changes `PATH`** (SDKMAN, a distribution's `java`
  alternatives): move the jkit block to the end of the startup file.
- **No hook at all**: `jkit hook --install`, then open a new shell.

## Exit codes

| Code | When |
| --- | --- |
| `0` | No check failed (warnings are fine). |
| `1` | At least one check failed, or jkit can't run at all (an unsafe jkit home). |

The report, or the JSON document, is printed either way.

## JSON

`--json` prints one object per finding (`id`, `level`, `message`, `fix`) and a `summary` with the
count per level. Filter on `id` and `level`; messages may change. See
[`jkit doctor` in JSON output](/reference/json/#jkit-doctor).

## See also

- [`jkit status`](/reference/commands/status/) for which version is active and why
- [Troubleshooting](/help/troubleshooting/)
- [Exit codes](/reference/exit-codes/#jkit-doctor)
