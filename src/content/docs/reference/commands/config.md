---
title: jkit config
description: Show and change jkit's settings in ~/.jkit/config.toml (jdk.vendor, graalvm.vendor, shell.auto_switch), and see where each value comes from.
sidebar:
  order: 16
---

Shows and changes the settings in `~/.jkit/config.toml`. With no subcommand, it lists every
setting, its value and where that comes from.

## Usage

```text
jkit config [-hV] [--json] [COMMAND]
jkit config list [--json]
jkit config get [--json] <key>
jkit config set [--json] <key> <value>
jkit config unset [--json] <key>
```

## Keys

| Key | Values | Default | Overridden by |
| --- | --- | --- | --- |
| `jdk.vendor` | a [vendor name](/reference/versions-and-vendors/#vendors) | `temurin` | `JKIT_JDK_VENDOR` |
| `graalvm.vendor` | `graalvm-ce`, `graalvm`, `liberica-nik` or `mandrel` | `graalvm-ce` | `JKIT_GRAALVM_VENDOR` |
| `shell.auto_switch` | `true` or `false` | `false` | `JKIT_AUTO_SWITCH` |

An environment variable wins over `config.toml`, which wins over the default. See
[Configuration](/reference/configuration/).

## Subcommands

### `list` (or no subcommand)

Lists every setting with its value and its source: `default`, `from ~/.jkit/config.toml`, or
`from JKIT_…` (with what the file says, when it says something else). If `config.toml` can't be
read, it says so and shows the defaults.

### `get <key>`

Prints the value jkit uses, and nothing else, for scripts: `vendor=$(jkit config get jdk.vendor)`.

### `set <key> <value>`

Writes the value to `config.toml`, keeping the file's comments and the order of its settings. It
creates the file, and `~/.jkit`, if needed. Then it says when the change takes effect.

- `jdk.vendor` is checked against foojay's vendor list (which may go online), and written as jkit's
  name for it: `jkit config set jdk.vendor sap_machine` and
  `jkit config set jdk.vendor "SAP Machine"` both write `sapmachine`. An unknown vendor is refused
  with a suggestion. If foojay can't be reached, a plain name is written anyway, with a note; one
  that only foojay can make sense of (with a space, say) is refused.
- `graalvm.vendor` must be GraalVM-based.
- `shell.auto_switch` takes `true`/`false`, and also `1`/`0`, `yes`/`no`, `on`/`off`; it is written
  as `true` or `false`.

Setting a value the file already has changes nothing and says so. If the environment variable for
that key is set, jkit warns that it wins over the file.

`set` and `unset` work on a `config.toml` that holds a wrong value (`auto_switch = "yes"`, say),
so they can repair it; a file that isn't TOML at all is still refused.

Changing `jdk.vendor` never changes installed JDKs or your global default; it changes what
`jdk@…` means from now on. Changing `shell.auto_switch` takes effect in new shells.

### `unset <key>`

Removes the setting from `config.toml`, so the default applies. A table left empty goes too;
comments and the other settings stay.

## Options

| Option | Description |
| --- | --- |
| `--json` | Print one JSON document on stdout instead of text (on `config` and each subcommand). |
| `-h`, `--help` | Show help and exit. |
| `-V`, `--version` | Print jkit's version and exit. |

## Examples

```console
$ jkit config
jdk.vendor         temurin     default
graalvm.vendor     graalvm-ce  default
shell.auto_switch  false       default
$ jkit config set jdk.vendor zulu
set jdk.vendor = "zulu" in ~/.jkit/config.toml
  file   ~/.jkit/config.toml created
  jdk@… without a vendor now means zulu; installed JDKs and the global default are unchanged
$ jkit config get jdk.vendor
zulu
$ jkit config unset jdk.vendor
removed jdk.vendor from ~/.jkit/config.toml; the default, temurin, applies
  file   ~/.jkit/config.toml updated
  jdk@… without a vendor now means temurin; installed JDKs and the global default are unchanged
```

An environment variable wins:

```console
$ JKIT_JDK_VENDOR=corretto jkit config
jdk.vendor         corretto    from JKIT_JDK_VENDOR (~/.jkit/config.toml says zulu)
graalvm.vendor     graalvm-ce  default
shell.auto_switch  false       default
```

Switching on `cd`:

```bash
jkit config set shell.auto_switch true    # same as jkit hook --install --auto-switch, minus the hook
```

## Exit codes

| Code | When |
| --- | --- |
| `0` | Shown or changed, or nothing to change. |
| `1` | An unknown key, a value it doesn't accept (a non-GraalVM vendor for `graalvm.vendor`, a switch that isn't on or off), or a `config.toml` it can't read or write. |
| `2` | A vendor foojay doesn't know, a missing key or value, or an unknown subcommand. |

```console
$ jkit config set shell.auto_switch maybe
error: shell.auto_switch must be true or false, not 'maybe'
```

## JSON

`--json` prints one object per setting: `key`, `value`, `source` (`env`, `file` or `default`), the
raw `file` and `env` values, `envVariable`, `default` and `description`. `error` says why
`config.toml` can't be used, or is null; while it is set, the file's values don't apply, so their
`source` is `default`. `set` and `unset` add
`changed`, `changes` and `notes`. See [`jkit config` in JSON output](/reference/json/#jkit-config).

## See also

- [Configuration](/reference/configuration/)
- [`jkit hook`](/reference/commands/hook/) for `--auto-switch`
