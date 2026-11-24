---
title: jkit completion
description: Print the bash tab-completion script for jkit, which also works in zsh through bashcompinit.
sidebar:
  order: 19
---

Prints the bash completion script. zsh uses it through `bashcompinit`. You rarely need this
command: the shell hook and the Linux packages already load it.

## Usage

```text
jkit completion [-hV] [<shell>]
```

## What it does

It prints a bash script that completes jkit's commands, options, tool names (`jdk`, `maven`,
`gradle`, `sbt`, `graalvm`), common vendor names, shell names and `jkit config` keys. Completion
runs in your shell without asking foojay, so it offers a fixed list of common vendors (`temurin`,
`corretto`, `zulu`, `liberica`, `oracle`, `openjdk`, `graalvm-ce`, `graalvm`, `liberica-nik`,
`mandrel`, `sapmachine`, `semeru`, `microsoft`, `dragonwell`, `kona`, `jetbrains`, `bisheng`,
`redhat`); any other vendor still works when typed in full. It doesn't complete version numbers.

Where completion comes from:

| Setup | Completion |
| --- | --- |
| bash or zsh with the [shell hook](/reference/shell-hook/) | Loaded by the hook |
| The `.deb` and `.rpm` packages | Installed system-wide |
| No hook, bash | `source <(jkit completion)` in `~/.bashrc` |
| No hook, zsh | `autoload -U +X bashcompinit && bashcompinit`, then `source <(jkit completion zsh)` |
| fish, PowerShell | Not available yet |

## Options

| Option | Description |
| --- | --- |
| `<shell>` | `bash` or `zsh` (default: `bash`). Both print the same script. |
| `-h`, `--help` | Show help and exit. |
| `-V`, `--version` | Print jkit's version and exit. |

## Examples

```bash
source <(jkit completion)                                          # bash, this shell
jkit completion > ~/.local/share/bash-completion/completions/jkit  # bash, every shell
```

```zsh
autoload -U +X bashcompinit && bashcompinit
source <(jkit completion zsh)
```

## Exit codes

| Code | When |
| --- | --- |
| `0` | The script was printed. |
| `2` | Any shell other than bash or zsh: `no completion script for fish yet`. |

## See also

- [`jkit hook`](/reference/commands/hook/)
- [Shells](/guides/shells/)
