---
title: jkit hook
description: Print the jkit shell hook for bash, zsh, fish or PowerShell, or add it to (and remove it from) your shell's startup file.
sidebar:
  order: 18
---

Prints the shell hook, or adds it to your shell's startup file. The hook puts the global versions
on `PATH` and lets `jkit use` change this shell; with auto-switch on, it also switches to a
project's versions on `cd`.

## Usage

```text
jkit hook [-hV] [--auto-switch] [--install | --uninstall] [<shell>]
```

## What it does

### Without `--install` or `--uninstall`

It prints the hook script for the shell, for your startup file to run. It changes nothing. The
default shell is `bash` (`pwsh` on Windows). With auto-switch on, the script includes the `cd`
hook; in bash and zsh it also includes tab completion. See [Shell hook](/reference/shell-hook/) for
what the script does.

```bash
eval "$(jkit hook bash)"                              # bash
eval "$(jkit hook zsh)"                               # zsh
jkit hook fish | source                               # fish
jkit hook pwsh | Out-String | Invoke-Expression       # PowerShell
```

### `--install`

Adds a short marked block, between `# >>> jkit >>>` and `# <<< jkit <<<`, to the end of the
shell's startup file:

| Shell | File |
| --- | --- |
| bash | `~/.bashrc` |
| zsh | `$ZDOTDIR/.zshrc`, else `~/.zshrc` |
| fish | `$XDG_CONFIG_HOME/fish/conf.d/jkit.fish`, else `~/.config/fish/conf.d/jkit.fish` |

Without a shell name it uses your login shell, from `$SHELL`. It prints the exact lines it wrote.
It does nothing to the file if the block is already there, or if another line already loads the
hook. PowerShell isn't supported yet: add `jkit hook pwsh | Out-String | Invoke-Expression` to
your `$PROFILE` by hand. See [`jkit hook --install`](/reference/shell-hook/#jkit-hook---install)
for every rule it follows.

### `--auto-switch`

With `--install`, also sets `auto_switch = true` under `[shell]` in `~/.jkit/config.toml`, so
bash, zsh and fish switch to a project's versions on `cd`. If the hook is already installed, only
the setting changes. `JKIT_AUTO_SWITCH=1` does the same without the file.

### `--uninstall`

Takes out what `--install` added to the startup file, and nothing else. It deletes the file only if
jkit created it and nothing else is in it. It doesn't change `auto_switch` in `config.toml`. Open
shells keep the hook until they exit.

## Options

| Option | Description |
| --- | --- |
| `<shell>` | `bash`, `zsh`, `fish` or `pwsh`. With `--install` or `--uninstall`, defaults to your login shell (`$SHELL`); otherwise `bash` (`pwsh` on Windows). |
| `--install` | Add the hook to the shell's startup file. Does nothing if it's already there. |
| `--uninstall` | Take out what `--install` added. |
| `--auto-switch` | With `--install`: also turn on switching to a project's versions on `cd`. |
| `-h`, `--help` | Show help and exit. |
| `-V`, `--version` | Print jkit's version and exit. |

`--install` and `--uninstall` can't be combined. `jkit hook` has no `--json` option.

## Examples

```console
$ jkit hook --install
added the jkit hook to ~/.bashrc:
    # >>> jkit >>>
    # added by jkit hook bash --install; remove with jkit hook bash --uninstall
    # jkit created this file: --uninstall deletes it if nothing else is in it
    if command -v jkit >/dev/null 2>&1; then eval "$(jkit hook bash)"; fi
    # <<< jkit <<<
  file   ~/.bashrc created
  new bash shells load it; for this one: eval "$(jkit hook bash)"
  to also switch to a project's versions on cd: jkit hook bash --install --auto-switch
```

The native jkit writes its own absolute path into the block instead of relying on `PATH`; see
[Shell hook](/reference/shell-hook/#jkit-hook---install).

Turning on auto-switch when the hook is already there:

```console
$ jkit hook --install --auto-switch
the jkit hook is already in ~/.bashrc; nothing changed there
turned on switching to a project's versions on cd: auto_switch = true under [shell] in ~/.jkit/config.toml
  file   ~/.jkit/config.toml created
  new bash shells switch on cd; for this one: eval "$(jkit hook bash)"
```

Taking it out:

```console
$ jkit hook --uninstall
removed the jkit hook from ~/.bashrc
  file   ~/.bashrc deleted
  open shells keep the hook until they exit
```

More:

```bash
jkit hook zsh --install         # name the shell instead of using $SHELL
jkit hook fish --install
```

## Exit codes

| Code | When |
| --- | --- |
| `0` | Printed, added, removed, or already there. |
| `1` | An unsupported shell (`jkit hook tcsh`), `$SHELL` isn't bash, zsh or fish and no shell was named, `--install` for PowerShell, `--auto-switch` without `--install`, or the startup file couldn't be written. |
| `2` | `--install` with `--uninstall`, or an unknown option. |

A broken `config.toml` only prints a warning, so new shells still get the hook.

## See also

- [Shell hook](/reference/shell-hook/) for what the hook does, per shell
- [Shells](/guides/shells/)
- [`jkit use`](/reference/commands/use/), [`jkit completion`](/reference/commands/completion/)
