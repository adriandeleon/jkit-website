---
title: Exit codes
description: What each jkit exit code means, the codes jkit exec passes through, doctor's exit code, and the JSON error document.
---

jkit's exit codes are stable, so scripts, CI jobs and AI agents can branch on them. This page lists
every code and what causes it.

| Code | Meaning |
| --- | --- |
| `0` | Success. |
| `1` | Failure: jkit tried and couldn't, or refused. |
| `2` | Bad arguments: the command line itself is wrong. |
| `3` | The version you asked for isn't installed. |
| `126`, `127`, other | [`jkit exec`](#jkit-exec) only: the command's own exit code, or why it couldn't start. |

Every failure prints `error: <message>` on stderr, then indented hint lines with commands to try:

```console
$ jkit use jdk@17
error: no installed JDK matches 17 from temurin (default vendor)
  install it:          jkit install jdk@17
$ echo $?
3
```

No jkit command prompts or waits for input, so a failure always ends with one of these codes.

## `0`: success

The command did what you asked, or found nothing to do (`jkit install` for a version that is
already installed, `jkit prune` with nothing to prune, `jkit hook --install` when the hook is
already there).

## `1`: failure

Anything that isn't a bad argument or a missing install, for example:

- A download failed, or a source couldn't be reached and nothing cached could stand in.
- A download didn't match its checksum or signature, or has no checksum or a signature jkit can't
  check, and you didn't pass `--allow-unverified`.
- The vendor has no build matching the version for your platform.
- `jkit remove` refused because the build is global or chosen in this shell (without `--force`),
  or the version matches several builds (without `--all`).
- `jkit use` or `jkit reset` without `--global`, when the shell hook isn't active.
- `jkit sync` found no project file, or a project file is broken.
- `jkit upgrade` failed on at least one release line (the other lines still ran).
- `jkit doctor` found a check that failed (see [below](#jkit-doctor)).
- `jkit config` was given an unknown key or a value it doesn't accept.
- The jkit home is unsafe (another user could write to it) or can't be written.
- `jkit self-update` couldn't download, check or replace the binary.

## `2`: bad arguments

The command line is wrong; nothing was attempted.

- An unknown command or option, or a missing argument: `jkit instal jdk`, `jkit install`.
- An unknown tool, or a version that isn't one: `jkit install foo`, `jkit install jdk@abc`,
  `jkit install 21`.
- `@lts` on a tool without LTS releases: `jkit install maven@lts`.
- An unknown vendor: `jkit install jdk@21 zuluu` (with a "did you mean").
- A vendor on a tool that has none: `jkit install maven@3.9 zulu`.
- A non-GraalVM vendor for `graalvm`: `jkit install graalvm@25 temurin`.
- An unknown shell: `jkit env --shell tcsh`, `jkit completion fish`.
- `jkit exec` without `--`, with nothing after `--`, or with a word before `--` that is neither a
  tool nor a vendor.

```console
$ jkit instal jdk
error: Unmatched arguments from index 0: 'instal', 'jdk'
  did you mean: install?
  more: jkit --help
```

## `3`: not installed

The version you named, or the one a project file pins, has no matching install. These commands
never download, so they stop instead:

- `jkit use`, `jkit exec` and `jkit remove` for a version that isn't installed (or isn't installed
  from that vendor).
- `jkit env` and `jkit exec` when the project file pins a version that isn't installed, so a CI
  step never runs with half an environment.
- `jkit use jdk@lts` and the like when no LTS release is installed.

The hint names the command that installs it, and other vendors you have it from:

```console
$ jkit use jdk@17
error: no installed JDK matches 17 from temurin (default vendor)
  install it:          jkit install jdk@17
  or use installed:    jkit use jdk@17 zulu
```

## `jkit exec`

`jkit exec` exits with the command's own exit code, so it can stand in for the command in scripts
and CI:

```console
$ jkit exec -- sh -c 'exit 7'
$ echo $?
7
```

| Code | Meaning |
| --- | --- |
| The command's code | The command ran. A command ended by a signal gives 128 + the signal number, such as `143` for SIGTERM. |
| `126` | The command was found but couldn't be started. |
| `127` | The command wasn't found on the `PATH` jkit gives it. |
| `2` | The `jkit exec` command line itself was wrong (no `--`, say). |
| `3` | A version you named, or the project pins, isn't installed. |
| `1` | Any other jkit error before the command started. |

A command can exit with 1, 2 or 3 itself. When it matters which side failed, look for jkit's
`error:` line on stderr: jkit prints one only for its own errors.

Ctrl-C goes to the command, not to jkit, so a REPL keeps running and its exit code comes through.
To stop the command from another process, send jkit SIGTERM (or SIGHUP): jkit passes SIGTERM on,
waits for the command, and exits with its code; a second SIGTERM kills it.

The shell hook's `jkit` function returns jkit's exit code unchanged, or `127` with
`jkit: not found on PATH` when it can't find the jkit binary.

## `jkit doctor`

`jkit doctor` exits `1` when any check has level `fail`, and `0` otherwise: warnings alone don't
fail it, so it can gate a CI step. It prints the full report (or the JSON document) either way.

## JSON error documents

With `--json`, a failure also prints one JSON document on stdout, with the same exit code in it:

```console
$ jkit list --bogus --json
error: Unknown option: '--bogus'
  usage: jkit list [-hV] [--json]
  more: jkit list --help
{
  "schemaVersion" : 1,
  "error" : {
    "message" : "Unknown option: '--bogus'",
    "hints" : [ "usage: jkit list [-hV] [--json]", "more: jkit list --help" ],
    "exitCode" : 2
  }
}
```

When a command had already changed something before it failed, the document also lists those
`changes`. See [JSON output](/reference/json/#errors).

## See also

- [JSON output](/reference/json/)
- [AI agents](/guides/ai-agents/) and [Scripts](/guides/scripts/)
- [`jkit exec`](/reference/commands/exec/)
