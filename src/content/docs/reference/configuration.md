---
title: Configuration
description: Every setting in ~/.jkit/config.toml, the jkit config command, and every environment variable jkit reads or sets.
---

This page lists everything that changes how jkit behaves: the three settings in `config.toml`, the
environment variables jkit reads, and the variables it sets in your shell. Most people never need
more than `jkit config set jdk.vendor <vendor>`.

## `config.toml`

`~/.jkit/config.toml` (or `$JKIT_HOME/config.toml`) holds your settings. It doesn't exist until you
set something; every setting has a default.

```toml title="~/.jkit/config.toml"
[jdk]
vendor = "temurin"        # default JDK vendor
[graalvm]
vendor = "graalvm-ce"     # default GraalVM vendor
[shell]
auto_switch = true        # switch to a project's versions on cd (jkit hook --install --auto-switch)
```

| Key | Type | Default | Environment variable | Meaning |
| --- | --- | --- | --- | --- |
| `jdk.vendor` | string | `temurin` | `JKIT_JDK_VENDOR` | The vendor `jdk@…` uses when none is named |
| `graalvm.vendor` | string | `graalvm-ce` | `JKIT_GRAALVM_VENDOR` | The vendor `graalvm@…` uses when none is named |
| `shell.auto_switch` | boolean | `false` | `JKIT_AUTO_SWITCH` | Switch to a project's versions on `cd` |

- **`jdk.vendor`** is any [vendor name](/reference/versions-and-vendors/#vendors). It decides what
  `jkit install jdk@21` downloads, and which install `jkit use jdk@21` picks. Changing it never
  changes what is installed or your global default.
- **`graalvm.vendor`** must be GraalVM-based: `graalvm-ce`, `graalvm`, `liberica-nik` or `mandrel`.
- **`shell.auto_switch`** only matters with the [shell hook](/reference/shell-hook/) in bash, zsh or
  fish. It takes effect in new shells; `export JKIT_AUTO_SWITCH=0` stops switching in a shell
  that's already open.

A value of the wrong type (`vendor = 21`, `auto_switch = "yes"`) or broken TOML is an error for
most commands. `jkit status`, `list`, `env`, `exec`, `config` (and `config get`) and `hook` warn
instead and use the defaults, and `jkit doctor` reports it as a failed check, so you can still see
what is going on and new shells still get the hook.

The file is read as UTF-8; a byte-order mark (as Notepad and PowerShell 5 write) is fine.
`jkit config set` and `unset` write it atomically and keep its permissions and encoding.

### Precedence

For each setting, the first of these that is set wins:

1. The environment variable (`JKIT_JDK_VENDOR`, …), if set and not empty.
2. `config.toml`.
3. The built-in default.

`jkit config` shows which one each value comes from.

## `jkit config`

```bash
jkit config                              # every setting, its value and where it comes from
jkit config get jdk.vendor               # only the value, as jkit uses it
jkit config set jdk.vendor zulu          # write it to config.toml
jkit config set shell.auto_switch true
jkit config unset jdk.vendor             # back to the default
```

```console
$ jkit config
jdk.vendor         zulu        from ~/.jkit/config.toml
graalvm.vendor     graalvm-ce  default
shell.auto_switch  false       default
$ JKIT_JDK_VENDOR=corretto jkit config
jdk.vendor         corretto    from JKIT_JDK_VENDOR (~/.jkit/config.toml says zulu)
graalvm.vendor     graalvm-ce  default
shell.auto_switch  false       default
```

`set` and `unset` keep the file's comments and order, create the file (and `~/.jkit`) when needed,
and say when the change takes effect. See [`jkit config`](/reference/commands/config/) for
details.

You can also edit the file by hand; jkit reads it fresh on every run.

## Environment variables jkit reads

### Settings

| Variable | Values | Effect |
| --- | --- | --- |
| `JKIT_HOME` | an absolute path | Where jkit keeps everything, instead of `~/.jkit`. See [Files and folders](/reference/files-and-folders/#jkit_home). |
| `JKIT_JDK_VENDOR` | a vendor name | Overrides `jdk.vendor`. |
| `JKIT_GRAALVM_VENDOR` | a vendor name | Overrides `graalvm.vendor`. |
| `JKIT_AUTO_SWITCH` | `1`, `true`, `yes`, `on` or `0`, `false`, `no`, `off` | Overrides `shell.auto_switch`. `0` also stops switching in a shell that's already open. Any other value is an error. |
| `JKIT_PROJECT_FILE` | a path, or empty | Use this project file instead of searching; set but empty turns project files off. See [Project files](/reference/project-files/#jkit_project_file). |

### Network

| Variable | Effect |
| --- | --- |
| `JKIT_NETWORK_OFFLINE` | `1`, `true`, `yes` or `on`: like `--offline` on every command. Only cached version lists are used, nothing is downloaded, and `jkit doctor --network` skips its check. `JKIT_OFFLINE` isn't read; jkit warns when it is set. |
| `JKIT_NETWORK_PROXY` | The HTTP proxy for jkit's requests. Wins over `HTTPS_PROXY`. |
| `HTTPS_PROXY`, `https_proxy` | The proxy, when `JKIT_NETWORK_PROXY` isn't set (in that order). |
| `NO_PROXY`, `no_proxy` | Hosts that bypass the proxy: comma- or space-separated names, matched exactly or as a domain suffix; `*` bypasses it for everything. |

- A proxy is written `http://host:port`, `host:port` or `http://user:password@host:port`; the port
  defaults to 8080, and percent-encoded characters in the user name and password are decoded. Only
  HTTP proxies are supported (an `https://` scheme is accepted and means the same); SOCKS isn't.
- `HTTP_PROXY` isn't read: every request jkit makes is HTTPS.
- A mirror on this machine (`http://localhost…`) is reached directly, never through the proxy.
- A proxy setting jkit can't parse fails the first request that needs the network, with the
  password left out of the message.

When a lookup fails (the server is down, answers 5xx or 429, or a captive portal answers), jkit
falls back to its cached answer only if it is at most 7 days old; an older one fails, with a hint
to use it on purpose with `--offline`, which still uses any cached copy.

See [Corporate networks](/guides/corporate-networks/) for proxies, mirrors and offline use.

### Sources (mirrors)

Each source can point at a mirror. The value must be an `https://` URL, or `http://` to this
machine; anything else is an error.

| Variable | Default | Used for |
| --- | --- | --- |
| `JKIT_SOURCES_FOOJAY_URL` | `https://api.foojay.io/disco/v3.0/` | JDK and GraalVM version lists, download links and checksums |
| `JKIT_SOURCES_GRADLE_URL` | `https://services.gradle.org/` | Gradle versions, downloads and checksums |
| `JKIT_SOURCES_MAVEN_URL` | `https://repo.maven.apache.org/maven2/` | Maven versions, downloads, checksums and signatures; also the sbt version list |
| `JKIT_SOURCES_SBT_URL` | `https://github.com/sbt/sbt/releases/download/` | sbt downloads, checksums and signatures |
| `JKIT_SOURCES_RELEASES_URL` | `https://github.com/adriandeleon/jkit/releases/` | `jkit self-update` |

A mirror must have the same layout as the source it replaces. JDK downloads come from wherever
foojay's answer points (usually the vendor's own site), not from the foojay mirror. A foojay
mirror must keep the vendors' own download links for Temurin, Microsoft, Corretto, Zulu, Liberica,
SapMachine, Semeru, Oracle, GraalVM CE, Oracle GraalVM and Mandrel, since jkit refuses those
builds from anywhere else. An `https` mirror may serve the builds these vendors signed itself,
with each `.sig` next to its file, as the signature and version are still checked; a mirror on
this machine (`http://localhost`) may serve the unsigned ones too. See
[Security](/reference/security/).

### Shell and system

| Variable | Read by | Effect |
| --- | --- | --- |
| `HOME` | everything | Your home folder: where `~/.jkit` and the startup files are, and where the project-file search stops. Falls back to the user's home from the system's user list when unset or relative. |
| `SHELL` | `hook --install`, `hook --uninstall`, `doctor` | Your login shell, when you don't name one. Must end in `bash`, `zsh` or `fish`. |
| `ZDOTDIR` | `hook --install` (zsh) | The hook goes in `$ZDOTDIR/.zshrc` instead of `~/.zshrc`. |
| `XDG_CONFIG_HOME` | `hook --install` (fish) | The hook goes in `$XDG_CONFIG_HOME/fish/conf.d/jkit.fish` instead of `~/.config/fish/conf.d/jkit.fish`. |
| `PATH` | `status`, `doctor`, `env`, `exec`, the hook | jkit adds, removes and moves only its own entries (and a `*_HOME` you set yourself puts its `bin` first); everything else stays in order, empty entries included. |
| `JAVA_HOME`, `MAVEN_HOME`, `GRADLE_HOME`, `SBT_HOME`, `GRAALVM_HOME` | `status`, `doctor`, the hook | Which version this shell has. A value set outside jkit wins over jkit's global default, and `jkit status` and `jkit doctor` say so. A relative value is ignored. |
| `JKIT_DEBUG` | everything | Set to anything: when a command fails with an error, also print its stack trace on stderr. For reporting a bug. |

jkit doesn't read `NO_COLOR` or detect CI: it prints no colors, never prompts, and shows the
download progress bar only when attached to a terminal. Without one, download progress is a line
at each quarter and at least every 30 seconds.

## Variables jkit sets

### For your tools

| Variable | Set to |
| --- | --- |
| `JAVA_HOME` | The chosen JDK |
| `MAVEN_HOME` | The chosen Maven |
| `GRADLE_HOME` | The chosen Gradle |
| `SBT_HOME` | The chosen sbt |
| `GRAALVM_HOME` | The chosen GraalVM; with none chosen, a GraalVM-based JDK when that is the JDK |
| `PATH` | The `bin` folders of the chosen JDK, Maven, Gradle and sbt first (not GraalVM's) |

The [shell hook](/reference/shell-hook/), `jkit use`, `jkit sync`, `jkit pin`, `jkit unpin`,
`jkit reset`, `jkit env` and `jkit exec` set these. Global defaults point at
`~/.jkit/global/<tool>`, so they follow `jkit use … --global` without a new shell; choices for one
shell or project point at the install folder itself.

### jkit's own bookkeeping

The hook and the commands that change a shell keep track of what they did in these variables.
They are internal: don't set them yourself, but it's safe to read them.

| Variable | Meaning |
| --- | --- |
| `JKIT_SHELL` | The hook is loaded, and for which shell: `bash`, `zsh`, `fish` or `pwsh`. |
| `JKIT_ENV_FILE` | Set only while the hook's `jkit` function runs a command: the file the command writes shell code to, which the function then sources. Without it, `jkit use` and `jkit reset` refuse to run. |
| `JKIT_SHELL_JDK`, `JKIT_SHELL_MAVEN`, `JKIT_SHELL_GRADLE`, `JKIT_SHELL_SBT`, `JKIT_SHELL_GRAALVM` | The install this shell chose with `jkit use`. |
| `JKIT_PROJECT_JDK`, `JKIT_PROJECT_MAVEN`, `JKIT_PROJECT_GRADLE`, `JKIT_PROJECT_SBT`, `JKIT_PROJECT_GRAALVM` | The install a project file applied to this shell. |
| `JKIT_PROJECT` | The project file applied to this shell, with its modification time, so the `cd` hook can tell nothing changed. |
| `JKIT_GRAALVM_FOLLOWS_JDK` | `GRAALVM_HOME` follows a GraalVM-based JDK chosen with `jkit use`. |
| `JKIT_USER_JDK`, `JKIT_USER_MAVEN`, … | A `JAVA_HOME` (or other `*_HOME`) you set yourself that a project or `jkit use` replaced in this shell. jkit gives it back when you leave the project or run `jkit reset`. |

On Windows, [`jkit env --install`](/reference/commands/env/#windows-the-user-environment) sets
`JAVA_HOME` and the other `*_HOME` variables, and `PATH`, in your user environment, and keeps a
`*_HOME` of your own that it replaces in `JKIT_SAVED_<TOOL>` (`JKIT_SAVED_JDK`, …), which
`jkit env --uninstall` puts back.

`jkit exec` removes the `JKIT_SHELL_*`, `JKIT_PROJECT*` and `JKIT_ENV_FILE` variables from the
command's environment, so a `jkit` run inside it can't change the calling shell.

## See also

- [`jkit config`](/reference/commands/config/)
- [Shell hook](/reference/shell-hook/)
- [Files and folders](/reference/files-and-folders/)
- [Corporate networks](/guides/corporate-networks/)
