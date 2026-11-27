---
title: jkit reset
description: Undo a jkit use in this shell, or clear a tool's global default with --global.
sidebar:
  order: 5
---

Clears a choice made in this shell with `jkit use`, so the project's version or the global one
applies again. With `--global`, clears the global default instead.

## Usage

```text
jkit reset [-hV] [--global] [--json] <tool>
```

## What it does

### Without `--global`

It drops this shell's own choice for the tool (`JKIT_SHELL_<TOOL>`), and sets the tool's
`*_HOME` and `PATH` to what applies without it:

1. the project's install, if a project file here still pins the tool and an earlier
   `jkit sync` (or the `cd` hook) applied it to this shell;
2. otherwise a `*_HOME` you set yourself before jkit's choice replaced it (jkit kept it in
   `JKIT_USER_<TOOL>`), with its `bin` first on `PATH`;
3. otherwise the global default, `~/.jkit/global/<tool>`;
4. otherwise nothing: the variable is unset.

The `bin` folder that comes back is moved to the front of `PATH`, and jkit says so.

Like `jkit use`, it needs the [shell hook](/reference/shell-hook/); without it, it refuses with
exit code 1. If the shell had no choice of its own for the tool (from `jkit use` or a project), it
says `nothing to reset` and changes nothing; a `JAVA_HOME` you set yourself stays as it is.

### With `--global`

It removes the link `~/.jkit/global/<tool>`. The installs stay; new shells get no version of
that tool from jkit until you set one with `jkit use <tool>@<version> --global`. Works without the
hook. If this shell's `*_HOME` pointed at that link, jkit tells you to open a new shell or choose
another version.

`jkit reset` never removes an install and never edits a project file.

## Options

| Option | Description |
| --- | --- |
| `<tool>` | `jdk`, `graalvm`, `maven`, `gradle` or `sbt`. No version. |
| `--global` | Clear the global default; installs stay. |
| `--json` | Print one JSON document on stdout instead of text. |
| `-h`, `--help` | Show help and exit. |
| `-V`, `--version` | Print jkit's version and exit. |

## Examples

Back to the project's or the global JDK in this shell:

```bash
jkit use jdk@17 zulu      # try something
jkit reset jdk            # back to what jkit.toml (or the global default) says
```

Clear a global default:

```console
$ jkit reset maven --global
cleared the global maven
  link   ~/.jkit/global/maven removed
  installs stay; new shells get no maven from jkit until: jkit use maven@<version> --global
$ jkit reset maven --global
no global maven to clear
```

Nothing of jkit's to take away:

```console
$ jkit reset jdk
nothing to reset: this shell has no jdk from jkit use or a project
  JAVA_HOME stays as you set it: /opt/jdk-21
```

With no tool named, it lists them:

```console
$ jkit reset
error: name a tool: jkit reset jdk
  tools: jdk, maven, gradle, sbt, graalvm
```

## Exit codes

| Code | When |
| --- | --- |
| `0` | Reset, or nothing to reset. |
| `1` | Without `--global`: the shell hook isn't active. |
| `2` | Unknown tool, or a missing tool name. |

## JSON

`--json` prints the `scope` (`global` or `shell`), the `tool`, `changed` (false when there was
nothing to reset), `changes` and `notes`; the text lines go to stderr. See
[`jkit reset` in JSON output](/reference/json/#jkit-reset).

## See also

- [`jkit use`](/reference/commands/use/)
- [`jkit status`](/reference/commands/status/) to see what applies now
- [Project files](/reference/project-files/#which-version-wins)
