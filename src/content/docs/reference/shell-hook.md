---
title: Shell hook
description: What the jkit shell hook does in bash, zsh, fish and PowerShell, the block jkit hook --install writes, and how switching on cd works.
---

The shell hook is a short script your shell runs at startup. It puts the global versions on `PATH`
and lets `jkit use`, `jkit sync` and `jkit reset` change the current shell. This page describes
exactly what it does, per shell. To set it up, run `jkit hook --install`; see
[Shells](/guides/shells/) for shell-specific advice.

You don't need the hook for `jkit exec`, `jkit env` or `jkit use … --global`, so CI jobs, scripts
and AI agents usually go without it.

## What the hook does

`jkit hook <shell>` prints the hook; your startup file runs it with `eval "$(jkit hook bash)"` (or
`jkit hook fish | source`). It changes nothing on disk. When it runs, it:

1. **Marks the shell** by exporting `JKIT_SHELL` (`bash`, `zsh`, `fish` or `pwsh`).
2. **Puts the global `bin` folders first on `PATH`:** `~/.jkit/global/jdk/bin`, then `maven`,
   `gradle` and `sbt`. An entry already on `PATH` isn't added twice: when it is there but further
   back, it moves to the front. That matters where something else ran first and put `/usr/bin`
   ahead of it: macOS's `path_helper` in a nested login zsh, or Windows, which puts the user `PATH`
   (where `jkit env --install` puts jkit's folders) after the system one. Loading the hook again
   (a nested shell, `source ~/.bashrc`) changes nothing. For a tool whose `*_HOME` you set
   yourself, that home's `bin` goes first instead of the global one (see
   [below](#a-java_home-you-set-yourself)). GraalVM's `bin` is never added, so there is only one
   `java` on `PATH`.
3. **Sets the `*_HOME` variables** to the global links: `JAVA_HOME=~/.jkit/global/jdk`,
   `MAVEN_HOME`, `GRADLE_HOME`, `SBT_HOME` and `GRAALVM_HOME` likewise. Each only when that
   global link exists and the variable isn't already set.
4. **Defines a `jkit` function** that wraps the binary, so a command can change this shell (below).
5. **With auto-switch on**, adds the `cd` hook and runs it once for the folder you start in.
6. **In bash and zsh**, loads tab completion (the same script as `jkit completion`).

Because the `*_HOME` variables and `PATH` point at the links in `~/.jkit/global/`, a
`jkit use … --global` reaches every open shell without reloading anything.

The start of the bash and zsh hook looks like this (comments left out):

```bash
# jkit shell hook (zsh)
export JKIT_SHELL=zsh
__jkit_g='/home/me/.jkit/global'
for __jkit_t in sbt gradle maven jdk; do
  case $__jkit_t in sbt) __jkit_h=${SBT_HOME-} ;; gradle) __jkit_h=${GRADLE_HOME-} ;;
    maven) __jkit_h=${MAVEN_HOME-} ;; *) __jkit_h=${JAVA_HOME-} ;; esac
  __jkit_h=${__jkit_h%/}
  __jkit_b="$__jkit_g/$__jkit_t/bin"
  case "$__jkit_h" in ""|"$__jkit_g/$__jkit_t") ;; *) [ -d "$__jkit_h/bin" ] && __jkit_b="$__jkit_h/bin" ;; esac
  __jkit_p=":$PATH:"
  while :; do
    case $__jkit_p in
      *":$__jkit_b:"*) __jkit_p="${__jkit_p%%":$__jkit_b:"*}:${__jkit_p#*":$__jkit_b:"}" ;;
      *":$__jkit_b/:"*) __jkit_p="${__jkit_p%%":$__jkit_b/:"*}:${__jkit_p#*":$__jkit_b/:"}" ;;
      *) break ;;
    esac
  done
  case $__jkit_p in :) PATH=$__jkit_b ;; *) __jkit_p=${__jkit_p#:}; PATH="$__jkit_b:${__jkit_p%:}" ;; esac
done
export PATH
[ -z "${JAVA_HOME-}" ] && [ -e "$__jkit_g/jdk" ] && export JAVA_HOME="$__jkit_g/jdk"
[ -z "${MAVEN_HOME-}" ] && [ -e "$__jkit_g/maven" ] && export MAVEN_HOME="$__jkit_g/maven"
[ -z "${GRADLE_HOME-}" ] && [ -e "$__jkit_g/gradle" ] && export GRADLE_HOME="$__jkit_g/gradle"
[ -z "${SBT_HOME-}" ] && [ -e "$__jkit_g/sbt" ] && export SBT_HOME="$__jkit_g/sbt"
[ -z "${GRAALVM_HOME-}" ] && [ -e "$__jkit_g/graalvm" ] && export GRAALVM_HOME="$__jkit_g/graalvm"
```

### A `JAVA_HOME` you set yourself

The hook never overwrites a `*_HOME` variable that is already set when it loads. If your
`~/.bashrc`, `~/.profile` or `/etc/environment` exports `JAVA_HOME`, that value stays, and it wins
over jkit's global default. `jkit status` shows the tool as `outside`, and `jkit doctor` warns:

```console
$ jkit status
jdk      17.0.2                  outside   JAVA_HOME=/usr/lib/jvm/java-17
         JAVA_HOME is set outside jkit and wins over the global jdk; remove it from your shell profile to use jkit's
```

Remove the export to let jkit manage it. While your own `JAVA_HOME` applies, the hook puts its
`bin` first on `PATH` instead of the global JDK's, so `java` is `$JAVA_HOME/bin/java`, also when it
points at one of jkit's installs under `~/.jkit/tools`. A trailing slash on it doesn't put its
`bin` on `PATH` twice.

An explicit `jkit use`, `jkit sync`, or a project on `cd` still sets the variable, since you asked
for that version. jkit keeps your value in `JKIT_USER_JDK` (`JKIT_USER_MAVEN`, and so on), says so,
and gives it back, with its `bin` first on `PATH`, when you leave the project or run
`jkit reset jdk`:

```console
$ jkit use jdk@21
...
  your own JAVA_HOME is kept in JKIT_USER_JDK: it comes back when jkit's jdk leaves this shell
$ jkit reset jdk
reset jdk in this shell
  unset  JKIT_SHELL_JDK
  set    JAVA_HOME=/usr/lib/jvm/java-17
  unset  JKIT_USER_JDK
  ...
  JAVA_HOME is your own again: /usr/lib/jvm/java-17
```

A `JAVA_HOME` you change by hand while a project or `jkit use` applies is yours from then on: when
jkit's choice goes, jkit leaves it as you set it, and says so. In a shell where jkit chose no JDK,
`jkit reset jdk` changes nothing and says there was nothing to reset.

"Already set" means non-empty in bash, zsh and PowerShell; in fish, set at all.

### The `jkit` function

A program can't change the environment of the shell that started it, so the hook defines `jkit`
as a shell function:

1. It creates an empty temporary file (`mktemp` in `$TMPDIR`, else `/tmp`, named `jkit-env.*`).
2. It runs the jkit binary with `JKIT_ENV_FILE` pointing at that file.
3. Commands that change this shell (`use`, `reset`, `sync`, `pin`, `unpin`) write shell code to it:
   `export`/`unset` lines in bash and zsh, `set -gx`/`set -e` in fish, `$env:` assignments in
   PowerShell. Every other command leaves it empty.
4. The function sources the file if it isn't empty, deletes it, and returns jkit's exit code.

A run that is interrupted before the function deletes the file (Ctrl-C, `kill -9`) can leave it
behind; in bash, zsh and fish the next `jkit` run removes it (fish also when it exits), and
PowerShell always removes it.

Without the function (no hook, a script, a subshell), `JKIT_ENV_FILE` isn't set: `jkit use` and
`jkit reset` refuse with a hint, and `jkit sync`, `pin` and `unpin` still do their work but leave
the shell unchanged and say so.

```console
$ jkit use jdk@21
using jdk 21.0.13.1+2 temurin in this shell
  set    JKIT_SHELL_JDK=~/.jkit/tools/jdk/21.0.13.1+2-temurin
  set    JAVA_HOME=~/.jkit/tools/jdk/21.0.13.1+2-temurin
  PATH   + ~/.jkit/tools/jdk/21.0.13.1+2-temurin/bin
  only this shell; for new shells too: jkit use jdk@21.0.13.1+2 temurin --global
```

When jkit changes this shell's `PATH`, it only adds or removes its own entries (under `~/.jkit`);
everything you put on `PATH` stays, empty entries included. When `jkit reset` or leaving a project
brings back the global version (or your own `*_HOME`), its `bin` moves to the front if it is
further back, and jkit says so, since a move changes which `java` runs:

```console
  PATH   moved ~/.jkit/global/jdk/bin ahead of /usr/bin
```

In `--json` that is a change with action `move`. The variables jkit keeps for itself
(`JKIT_SHELL_<TOOL>`, `JKIT_PROJECT_<TOOL>`, `JKIT_PROJECT`, `JKIT_USER_<TOOL>`,
`JKIT_GRAALVM_FOLLOWS_JDK`) are listed in
[Configuration](/reference/configuration/#jkits-own-bookkeeping).

The first install of a tool becomes its global version. In a shell with the hook where that tool's
`*_HOME` isn't set, jkit sets it right away and puts its `bin` on `PATH`, and says so: no new shell
needed. `jkit use … --global` does the same when the variable was unset.

### Which jkit the hook runs

Once a project's JDK is active, its `bin` folder is first on `PATH`, and a file named `jkit` there
must never be what runs. So the native jkit writes its own absolute path into the hook and calls
that. If that file is gone (a package manager moved it), the hook takes the first `jkit` on `PATH`
that isn't inside `~/.jkit`, and prints `jkit: not found on PATH` (exit code 127) if there is none.
It skips relative `PATH` entries (`.`, `bin`), so a `./jkit` or `bin/jkit` in the folder you `cd`
into never runs.

## Per shell

| | bash | zsh | fish | PowerShell |
| --- | --- | --- | --- | --- |
| Load by hand | `eval "$(jkit hook bash)"` | `eval "$(jkit hook zsh)"` | `jkit hook fish \| source` | `jkit hook pwsh \| Out-String \| Invoke-Expression` |
| `jkit hook --install` | `~/.bashrc` | `$ZDOTDIR/.zshrc`, else `~/.zshrc` | `$XDG_CONFIG_HOME/fish/conf.d/jkit.fish`, else `~/.config/fish/conf.d/jkit.fish` | Not yet: add the line to `$PROFILE` by hand |
| `jkit use` in this shell | yes | yes | yes | yes (Windows PowerShell 5.1: with scripts allowed) |
| Switching on `cd` | yes, when the prompt is drawn | yes, on `cd` | yes, interactive shells | not yet |
| Tab completion | yes | yes (through `bashcompinit`) | no | no |

- **bash.** The `cd` hook runs from `PROMPT_COMMAND`, before each prompt, and only acts when the
  folder changed. It keeps any `PROMPT_COMMAND` you already have, and the exit code `$?` you see
  is unchanged. Because it runs at the prompt, `cd app && java -version` on one line still uses
  the old JDK.
- **zsh.** The `cd` hook is a `chpwd` hook (`add-zsh-hook chpwd`), so it runs on every folder
  change, including inside one command line. It doesn't run in `$( )` or `( )` subshells, such as
  a prompt theme's, so jkit's messages don't end up in your prompt. The hook loads with
  `setopt nounset` (`set -u`) and with `warn_create_global` on without errors or warnings, and
  loading its completion doesn't turn on `COMPLETE_ALIASES` or define a `compopt` alias, so your
  aliases complete as before.
- **fish.** The `cd` hook runs when `PWD` changes, in interactive shells only: `conf.d` files are
  read by every fish, scripts too. `conf.d` runs before `config.fish`, where a line such as
  Homebrew's `fish_add_path --move` can put its `bin` ahead of jkit's again, so in interactive
  shells the hook puts the `PATH` order back once more at the first prompt.
- **PowerShell.** `jkit hook pwsh` (also the default when you run `jkit hook` on Windows) sets up
  `PATH`, the `*_HOME` variables and the `jkit` function inside a script block of its own, so your
  profile's variables are untouched. On Windows, folders on `PATH` match whatever their case,
  slash direction or trailing slash. The hook's output is ASCII only, so a path such as
  `C:\Users\José` arrives intact. The `jkit` function reads its temporary file (UTF-8 with a
  byte-order mark) and runs its text, rather than running it as a script file, so the execution
  policy doesn't block `use`, `reset`, `pin`, `sync` and `exec`. PowerShell drops a bare `--` from
  a function's arguments; the function puts it back, so `jkit exec jdk@21 -- java -version` works.
  With auto-switch on it prints a warning that PowerShell doesn't switch on `cd` yet; `jkit sync`
  switches by hand. Windows PowerShell 5.1 still needs scripts allowed to load `$PROFILE` at all:
  `Set-ExecutionPolicy -Scope CurrentUser RemoteSigned`.
- **Git Bash, MSYS2 and Cygwin** (Windows) have no jkit hook. On Windows, PowerShell is the shell
  jkit has a hook for.

For any other shell, `jkit env` prints the variables to set, and `jkit exec` runs a command with
them.

## `jkit hook --install`

`jkit hook --install` adds a marked block to the end of your shell's startup file. It picks the
shell from `$SHELL` (it must be bash, zsh or fish); name one to override: `jkit hook zsh --install`.

```console
$ jkit hook --install
added the jkit hook to ~/.bashrc:
    # >>> jkit >>>
    # added by jkit hook bash --install; remove with jkit hook bash --uninstall
    __jkit='/usr/local/bin/jkit'
    if [ ! -x "$__jkit" ]; then __jkit=; IFS=: read -r -a __jkit_p <<< "$PATH"; for __jkit_d in "${__jkit_p[@]}"; do case "$__jkit_d/" in '/home/me/.jkit/'*) ;; *) if [ -x "$__jkit_d/jkit" ]; then __jkit=$__jkit_d/jkit; break; fi ;; esac; done; unset __jkit_p __jkit_d; fi
    if [ -n "$__jkit" ]; then eval "$("$__jkit" hook bash)"; fi; unset __jkit
    # <<< jkit <<<
  file   ~/.bashrc updated
  new bash shells load it; for this one: eval "$('/usr/local/bin/jkit' hook bash)"
  to also switch to a project's versions on cd: jkit hook bash --install --auto-switch
```

The block:

- starts with `# >>> jkit >>>` and ends with `# <<< jkit <<<`, the markers `--uninstall` looks for;
- names the jkit binary by its absolute path, and falls back to the first `jkit` on `PATH` outside
  `~/.jkit` if that path is gone. When jkit can't tell its own path, the block is a plain
  `if command -v jkit >/dev/null 2>&1; then eval "$(jkit hook bash)"; fi`;
- is guarded, so a shell still starts normally after jkit is uninstalled;
- for fish, runs only in interactive shells;
- has one more comment line, `# jkit created this file: --uninstall deletes it if nothing else is in it`,
  when the startup file didn't exist before.

Rules `--install` follows:

- It does nothing to the startup file if the block is already there, or if a line outside the
  block already loads the hook (`eval "$(jkit hook bash)"` and similar, not commented out). It
  says which line.
- It creates the startup file (mode 644) and any missing folders (mode 755) when needed, and lists
  each one.
- If the startup file is a symbolic link, the change goes to the file it points to, and jkit says
  so; the link stays a link.
- The file is written atomically and synced to disk, keeping its permissions, owner and encoding
  (UTF-8 with or without a byte-order mark, UTF-16, or Latin-1 for a file that isn't valid UTF-8),
  so nothing jkit doesn't change is altered. A read-only file is refused.
- The block always has LF line endings, also in a file with Windows (CRLF) line endings, so bash
  and zsh still read it.
- It finds jkit's block also when it is indented or has trailing spaces. A start marker with no
  end marker (one deleted by hand) makes it stop with the line number and change nothing.
- For bash, it checks `~/.bash_profile`, `~/.bash_login` and `~/.profile` (the first that exists,
  as bash does). If that file doesn't load `~/.bashrc`, it tells you to add
  `[ -f ~/.bashrc ] && . ~/.bashrc` to it, since login shells (macOS Terminal, ssh) read only that
  one.
- PowerShell isn't supported yet: it fails and prints the line to add to `$PROFILE` by hand. On
  Windows it edits no startup file at all, Git Bash's `~/.bashrc` included (jkit has no hook for
  Git Bash), and prints that `$PROFILE` line instead.

### `--auto-switch`

`jkit hook --install --auto-switch` also sets `auto_switch = true` under `[shell]` in
`~/.jkit/config.toml`. Run it again when the hook is already installed to turn auto-switch on
without touching the startup file. If `JKIT_AUTO_SWITCH` is set to an "off" value, it warns that
the variable wins. `--auto-switch` without `--install` is an error.

### `--uninstall`

`jkit hook --uninstall` removes the marked block, and the blank line before it if jkit added one,
and nothing else. It deletes the startup file only when jkit created it and nothing else is left
in it (fish's `conf.d/jkit.fish` is always jkit's own). Every other byte of the file stays as it
was, line endings included. A line that loads the hook outside the block is left alone, and jkit
tells you its line number. A stray `# >>> jkit >>>` line before jkit's block, or a start marker
with no end marker, makes it refuse and change nothing, rather than guess which lines to delete.
Open shells keep the hook until they exit.

On Windows, `jkit hook bash --uninstall` takes out a block an older jkit added to Git Bash's
`~/.bashrc`.

`--uninstall` doesn't change `config.toml`: `auto_switch` stays as it was. Use
`jkit config unset shell.auto_switch` for that.

## Switching on `cd`

With `shell.auto_switch` on (in `config.toml`, or `JKIT_AUTO_SWITCH=1`), the hook calls
`jkit hook-env <shell>` when the current folder changes. `hook-env` is internal and hidden from
`jkit --help`; it exists only for the hook. It:

1. Looks for a [project file](/reference/project-files/#which-file-applies) from the current folder.
   This is only a few file checks; if the file and its modification time are the same as the
   project already applied (`JKIT_PROJECT`), it prints nothing and stops.
2. Otherwise, for each pinned tool, picks the matching installed build and prints the shell code
   that sets its `*_HOME` and `PATH`, recording it in `JKIT_PROJECT_<TOOL>`.
3. Leaving a project, it sets each tool the project applied back to the global version, with its
   `bin` moved to the front of `PATH` if it is further back. A `*_HOME` of your own that the
   project replaced comes back instead, and a tool with no global version is unset
   (`maven unset (no global maven)`).
4. Tells you on stderr what changed, with the same lines as any other jkit command. A project file
   whose pins jkit can't read (a `.java-version` saying `system`) gets "it pins nothing jkit can
   read" and a pointer to `jkit status`, which says why.

A change to `auto_switch` in `config.toml` applies to new shells. To stop switching in a shell
that is already open, run `export JKIT_AUTO_SWITCH=0` there (`set -gx JKIT_AUTO_SWITCH 0` in fish).

```console
$ cd ~/src/app
jkit: using ~/src/app/jkit.toml: jdk 21.0.13.1+2 temurin
  set    JKIT_PROJECT=~/src/app/jkit.toml:1795537120159
  set    JKIT_PROJECT_JDK=~/.jkit/tools/jdk/21.0.13.1+2-temurin
  set    JAVA_HOME=~/.jkit/tools/jdk/21.0.13.1+2-temurin
  PATH   + ~/.jkit/tools/jdk/21.0.13.1+2-temurin/bin
$ cd ~
jkit: left the project in ~/src/app: back to the global versions
  unset  JKIT_PROJECT
  unset  JKIT_PROJECT_JDK
  set    JAVA_HOME=~/.jkit/global/jdk
  PATH   - ~/.jkit/tools/jdk/21.0.13.1+2-temurin/bin
```

What it never does:

- **Download.** A pin with nothing installed to match stays on the shell or global version, with
  a note to run `jkit sync`. It looks again on the next `cd` into the project, quietly.
- **Override `jkit use`.** A tool chosen in this shell with `jkit use` keeps winning, and the hook
  says so; `jkit reset <tool>` follows the project again.
- **Fail.** A broken project file is reported once (until it changes) and leaves the versions as
  they were. Any other problem prints one `jkit:` line, and `cd` still works.
- **Use the network.**

## See also

- [`jkit hook`](/reference/commands/hook/), [`jkit use`](/reference/commands/use/),
  [`jkit sync`](/reference/commands/sync/)
- [Shells](/guides/shells/) for bash, zsh, fish and PowerShell specifics
- [Configuration](/reference/configuration/) for `shell.auto_switch` and the variables jkit sets
