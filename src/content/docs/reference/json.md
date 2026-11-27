---
title: JSON output
description: The --json output of every jkit command that has it, document by document, with the error shape, the install object and the changes list.
---

For editors, IDEs, scripts and AI agents that drive jkit. With `--json`, a command prints
**exactly one JSON document on stdout**, also when it fails. Progress, download bars and messages
go to stderr, so a program can read stdout whole and show stderr lines as progress.

Commands with `--json`: `list`, `status`, `env`, `doctor`, `search`, `install`, `upgrade`, `use`,
`reset`, `pin`, `unpin`, `sync`, `remove`, `prune`, `config` (and its subcommands) and `self-update`.
(`hook` only makes sense in a shell; `exec`'s stdout belongs to the command it runs;
`completion` prints a script.) For agents and scripts, see also [AI agents](/guides/ai-agents/)
and [Scripts](/guides/scripts/).

`--help` and `--version` with `--json` print `{"schemaVersion": 1, "help": "<usage text>"}` (or
`"version"`):

```console
$ jkit --version --json
{
  "schemaVersion" : 1,
  "version" : "jkit 0.6.0"
}
```

## Versioning

Every document starts with `"schemaVersion": 1`. New fields may appear at any time; a change that
removes or renames a field, or changes a field's meaning, bumps `schemaVersion`. Readers should
ignore fields they don't know.

Paths are absolute. Fields with no value are `null`, not left out, unless noted.

The output is ASCII-safe where it matters: control characters, and characters that can hide text
from a reader (such as Unicode tag characters, U+E0000 to U+E007F), are written as `\u` escapes, as
text output already hides them. Where the console's code page can't hold a character (the Windows
console, `LANG=C`), `--json` writes it as a `\u` escape too, instead of a substitute.

## Errors

Any failure prints this, with the same exit code the process exits with:

```json
{
  "schemaVersion": 1,
  "error": {
    "message": "no installed JDK matches 17 from temurin (default vendor)",
    "hints": ["install it:          jkit install jdk@17"],
    "exitCode": 3
  }
}
```

The same message and hints also go to stderr, as `error: …` followed by indented hint lines.
`hints` are commands or advice for a person; they may change between releases.

When a command had already changed something before it failed (`sync` installed one pin and the
next download failed, `remove --all` removed some builds), the error document also has `changes`,
in the same shape as on success.

Exit codes: `0` success, `1` failure, `2` bad arguments (unknown tool, vendor or shell, a version
that isn't one), `3` no installed match (`use`, `remove`, and `env` for a pin with no install).
See [Exit codes](/reference/exit-codes/).

## The install object

`list`, `install`, `use`, `remove`, `status`, `pin`, `sync` and `prune` describe installed builds
the same way:

```json
{
  "tool": "jdk",
  "id": "21.0.9+10-temurin",
  "version": "21.0.9+10",
  "major": 21,
  "vendor": "temurin",
  "fx": false,
  "graalvm": false,
  "preRelease": false,
  "home": "/home/me/.jkit/tools/jdk/21.0.9+10-temurin",
  "verified": true,
  "signedBy": "Adoptium",
  "global": ["jdk"],
  "activeInShell": []
}
```

| Field | Type | Meaning |
| --- | --- | --- |
| `tool` | string | The store tool: `jdk` (GraalVM builds too, marked `graalvm: true`), `maven`, `gradle` or `sbt`. |
| `id` | string | The install folder's name under `tools/<tool>/`. |
| `version` | string | The version as the source published it. |
| `major` | number | The feature release: 21 for `21.0.9+10`, 8 for `1.8.0_472`, 3 for Maven `3.9.11`. |
| `vendor` | string or null | The vendor name; null for Maven, Gradle and sbt. |
| `fx` | boolean | A JavaFX build. |
| `graalvm` | boolean | A GraalVM-based build (usable as `graalvm`). |
| `preRelease` | boolean | An early-access build, release candidate or milestone. |
| `home` | string | The folder to use as `JAVA_HOME`, `MAVEN_HOME`, `GRADLE_HOME` or `SBT_HOME`. |
| `verified` | boolean | See below. |
| `signedBy` | string or null | See below. |
| `global` | array | The slots (`jdk`, `graalvm`, `maven`, `gradle`, `sbt`) whose global link points here. |
| `activeInShell` | array | The slots the calling shell chose with `jkit use` that point here. |

- `verified` is true when the download matched a sha256, sha384 or sha512 checksum published by its
  source (foojay or the vendor for JDKs, Maven Central, services.gradle.org, sbt's GitHub releases),
  or a good signature (see `signedBy`; Corretto, which publishes no checksum, is verified this
  way). False for md5/sha1-only checksums, for `--allow-unverified` installs whose signature
  couldn't be checked, and for installs made before jkit recorded it.
- `signedBy` names whose OpenPGP signature the download was checked against, using a key built
  into jkit: `"Adoptium"` for Temurin, `"Microsoft"` for Microsoft builds, `"Amazon"` for Corretto,
  `"Apache Maven"` for Maven 3.9.0 and later (the install's `notes` name the release manager), and
  `"sbt"` for sbt. That does say who built it. Null for other vendors, Gradle and older Maven
  releases, when the signature couldn't be fetched or checked (an `--allow-unverified` install;
  its `notes` say why), and
  for installs made before jkit checked signatures.

## Changes

Nothing jkit changes is silent. Every command that changes a file, a folder, a link or a variable
prints a line for each change under its own message, and with `--json` lists them in `changes`
(`install`, `upgrade` per line and for the lookup, `use`, `reset`, `pin`, `unpin`, `sync`,
`remove`, `prune`, `config set`, `config unset`, `self-update`, `env --install`,
`env --uninstall`, and `search` for the cache). The `cd` hook
(`shell.auto_switch`) follows the same rule: whenever it switches versions it prints the same lines
on stderr, under a `jkit: using …` or `jkit: left the project …` line.

```json
"changes": [
  { "kind": "folder", "name": "/home/me/.jkit/tools/jdk/21.0.9+10-temurin", "action": "create", "value": null },
  { "kind": "file", "name": "/home/me/.jkit/index/3f9a….cache", "action": "update",
    "value": "https://api.foojay.io/disco/v3.0/packages?…" }
]
```

Each change has `kind`, `name`, `action` and `value`:

| `kind` | `name` | `action` | `value` |
| --- | --- | --- | --- |
| `folder` | An install folder, the jkit home on first use, a leftover of an interrupted install, or a folder `hook --install` created | `create`, `delete` | null |
| `file` | A cached version list under `index/` | `create`, `update` | The URL it came from |
| `file` | A cached version list the cache dropped (it keeps at most 32 MB and nothing older than 30 days) | `delete` | null |
| `file` | A project file `pin` or `unpin` wrote, `config.toml`, a shell startup file, or the jkit binary (`self-update`) | `create`, `update`, `delete` | null |
| `link` | A global link | `set` (`value` is its target), `remove` | The target, or null |
| `env` | A variable of the calling shell | `set` (with `value`), `unset` | The new value, or null |
| `env` | `PATH`, reported per entry | `add`, `remove`, `move` (an entry already there that now comes before or after another, which changes which program runs) | The entry; `""` for an empty entry (the current folder) |
| `user-env` | Windows: a variable of your user environment (`env --install` and `--uninstall`) | the same as `env` | the same as `env` |

Checksums are fetched fresh, never cached, so they never show up here. Text mode folds the
cached-list lines into one `cache` line, and `install` leaves out that line and the folders its own
message names; `changes` lists them all.

`list`, `status`, `env`, `exec` and `doctor` never change anything; nor do `hook` and `env` without
`--install` or `--uninstall` (`hook`'s edit your shell's startup file and `config.toml`; `env`'s,
the user's environment variables on Windows).

Commands that change something also have a `notes` array: the same advice lines text mode prints
under the changes, such as `"new shells with the jkit hook: JAVA_HOME=…"`. They are for people.

## `jkit list`

`jkit list --json`. Never uses the network.

```json
{
  "schemaVersion": 1,
  "home": "/home/me/.jkit",
  "global": { "jdk": "/home/me/.jkit/tools/jdk/21.0.9+10-temurin", "maven": null, "gradle": null, "sbt": null, "graalvm": null },
  "project": {
    "dir": "/home/me/src/app",
    "files": ["/home/me/src/app/jkit.toml"],
    "tools": { "jdk": "/home/me/.jkit/tools/jdk/21.0.9+10-temurin", "maven": null }
  },
  "installed": [ { "tool": "jdk", "id": "21.0.9+10-temurin", "...": "the install object" } ]
}
```

- `global` maps each tool to the install its global link points at, or `null`.
- `installed` holds JDKs, then Maven, then Gradle, then sbt, each newest first.
- `project` is the project file found from the working directory, or `null`; its `tools` maps each
  pinned tool to the install it picks, or `null` when none is installed (`jkit sync`).

## `jkit status`

`jkit status --json`. Never uses the network. Reads the project file found from the working
directory (see [Project files](/reference/project-files/)).

```json
{
  "schemaVersion": 1,
  "project": { "dir": "/home/me/src/app", "files": ["/home/me/src/app/jkit.toml"], "warnings": [] },
  "projectError": null,
  "projectSkipped": null,
  "tools": [
    {
      "tool": "jdk",
      "source": "global",
      "home": "/home/me/.jkit/global/jdk",
      "installed": { "tool": "jdk", "id": "17.0.13+11-zulu", "...": "the install object" },
      "version": "17.0.13+11",
      "pin": { "version": "21", "vendor": null, "fx": false },
      "pinInstalled": { "tool": "jdk", "id": "21.0.9+10-temurin", "...": "the install object" },
      "wanted": { "source": "project", "home": "/home/me/.jkit/tools/jdk/21.0.9+10-temurin" },
      "global": { "target": "/home/me/.jkit/tools/jdk/17.0.13+11-zulu", "missing": false },
      "matches": false,
      "hint": "jkit.toml wants jdk 21.0.9+10 temurin; this shell has global: jkit sync"
    }
  ]
}
```

- `tools` always has `jdk`, `maven`, `gradle`, `sbt`, `graalvm`, in that order.
- `source` is what this shell has: `shell` (`jkit use`), `project`, `global`, `outside` (a
  `*_HOME` jkit didn't set), `stale` (a jkit install nothing chooses any more) or `none`.
- `wanted.source` is what jkit would pick (shell, then project, then global, else `none`);
  `wanted.home` is null when nothing is picked or a pin has no matching install.
- `home` is the tool's `*_HOME` as set in this environment. `installed` is null when it isn't a
  jkit install; `version` then comes from a JDK's `release` file when it has one.
- `pin` and `pinInstalled` are null without a project pin. `global` is null without a global link;
  `global.missing` is true when the link points at a folder that no longer exists.
- `project` is null without a project file. `projectError` is the error message when the
  file can't be read. `projectSkipped` says which project file was ignored, and why, when its
  folder or the file belongs to another user or every user can write to it; otherwise null.
- `matches` is true when `source` is what jkit would pick; `hint` says what to do when it isn't,
  and is null when there's nothing to do.

## `jkit env`

`jkit env [--no-project] [--dir <path>] --json`. Never uses the network. The environment for a
new process in `dir`: global links, overridden by the project's pins (shell choices don't count).

```json
{
  "schemaVersion": 1,
  "dir": "/home/me/src/app",
  "project": { "dir": "/home/me/src/app", "files": ["/home/me/src/app/jkit.toml"], "warnings": [] },
  "variables": {
    "JAVA_HOME": "/home/me/.jkit/tools/jdk/21.0.9+10-temurin",
    "MAVEN_HOME": "/home/me/.jkit/global/maven"
  },
  "pathPrepend": ["/home/me/.jkit/tools/jdk/21.0.9+10-temurin/bin", "/home/me/.jkit/global/maven/bin"],
  "path": "/home/me/.jkit/tools/jdk/21.0.9+10-temurin/bin:/home/me/.jkit/global/maven/bin:/usr/bin",
  "pathSeparator": ":"
}
```

- `variables` holds only the `*_HOME` variables of tools that are chosen; others are left out,
  not unset. `GRAALVM_HOME` follows a GraalVM JDK when no GraalVM is chosen.
- `pathPrepend` is what to put first on PATH; `path` is the whole PATH jkit would set (the
  current PATH with old jkit install folders removed). `pathSeparator` is `:`, or `;` on Windows.
- `project` is null without a project file (or with `--no-project`).
- A pin with no matching install is an error document with exit code 3.

### `jkit env --install` and `--uninstall`

`jkit env --install --json` and `jkit env --uninstall --json`. Windows only (elsewhere an error
document). They write your user environment variables (`HKCU\Environment`): `--install` sets each
`*_HOME` of a tool with a global version to its global link and puts the links' `bin` folders first
on the user `PATH`; `--uninstall` removes only what `--install` set. The message goes to stderr.

```json
{
  "schemaVersion": 1,
  "action": "install",
  "changed": true,
  "changes": [
    { "kind": "user-env", "name": "JAVA_HOME", "action": "set", "value": "C:\\Users\\me\\.jkit\\global\\jdk" },
    { "kind": "user-env", "name": "PATH", "action": "add", "value": "C:\\Users\\me\\.jkit\\global\\jdk\\bin" }
  ],
  "notes": ["programs started from now on get them (a new terminal, an IDE you restart); running ones, this shell included, keep what they have"]
}
```

- `changed` is false when there was nothing to do; `changes` is then empty.
- When `--install` replaces a `*_HOME` you set yourself, it also sets `JKIT_SAVED_<TOOL>`
  (`JKIT_SAVED_JDK`, …) to your value, a `user-env` `set` change. `--uninstall` sets the `*_HOME`
  back to it (`set`, not `unset`) and unsets `JKIT_SAVED_<TOOL>`.

## `jkit doctor`

`jkit doctor [--network] --json`. Exit code 1 when any check has level `fail`, 0 otherwise; the
document is printed either way.

```json
{
  "schemaVersion": 1,
  "checks": [
    { "id": "home", "level": "ok", "message": "jkit 0.6.0, linux-x64, home ~/.jkit", "fix": null },
    { "id": "links", "level": "fail", "message": "global maven → ~/.jkit/tools/maven/3.9.6, which no longer exists",
      "fix": "jkit use maven@3.9.11 --global, or remove the link: rm ~/.jkit/global/maven" }
  ],
  "summary": { "ok": 9, "info": 2, "warn": 0, "fail": 1 }
}
```

- `level` is `ok`, `info`, `warn` or `fail`; `fix` is null when there is nothing to do.
- A check can report several lines (one per broken link, say). The ids are stable:
  `home`, `hook`, `binary`, `config`, `links`, `outside-vars`, `path-order`, `user-env`
  (Windows), `store`, `temp-files` (only when there are some), `project`, `index`, `java-runs`,
  and `network` (only with `--network`).
- Messages and fixes are for people and may change; filter on `id` and `level`.

## `jkit search`

`jkit search <tool>[@version] [vendor] --json`.

```json
{
  "schemaVersion": 1,
  "tool": "jdk",
  "vendor": "temurin",
  "vendorChosen": false,
  "majors": [ { "major": 25, "lts": true, "maintained": true } ],
  "available": [
    { "tool": "jdk", "version": "25.0.1+8", "major": 25, "vendor": "temurin", "graalvm": false,
      "fx": false, "lts": true, "maintained": true, "preRelease": false, "installed": false,
      "downloadable": true, "verifiable": true, "behind": null }
  ],
  "changes": []
}
```

What `available` holds for a JDK (or `graalvm`):

| Arguments | `available` |
| --- | --- |
| `jdk` | the newest build of every vendor and major |
| `jdk zulu` | the newest build of every major from that vendor |
| `jdk@21` | the newest matching build of every vendor |
| `jdk@21 zulu` | every matching build of that vendor |

- `downloadable` and `verifiable` say what `jkit install` would do with that line:
  `downloadable: false` when no matching build has a direct download (install refuses it),
  `verifiable: false` when the build it would download has no published checksum and no signature
  jkit checks (install refuses it unless `--allow-unverified`). Either is `null` when jkit couldn't
  look it up (offline and not cached); `verifiable` is also `null` when the build isn't
  downloadable.
- `behind` is set on a vendor's newest build of a release when the vendor stopped updating it:
  that build is two or more updates behind the newest update at least two vendors have (20 update
  numbers for JDK 8, whose numbers move by about 10 a quarter). Only OpenJDK update numbers
  count; GraalVM distributions' own release numbers don't. It is
  `{"newest": "21.0.13.1+5", "vendors": ["corretto", "zulu"]}` (the newest build of that release,
  and who has it), and `null` otherwise or when jkit couldn't compare (offline and not cached).
- `vendor` is the default vendor, or the one typed (`vendorChosen: true`).
- `majors` lists every feature release foojay knows, newest first, with whether it is LTS and
  still maintained.
- For Maven, Gradle and sbt, `vendor` and `vendorChosen` are left out, `majors` is empty, and
  `available` holds every matching version (`tool`, `version`, `major`, `preRelease`,
  `installed`).
- Newest first. `--maintained`, `--pre` and `--fx` filter as they do for text output.
- `changes` lists the cached version lists the lookup wrote.

## `jkit install`

`jkit install <tool>@<version> [vendor] --json`.

```json
{ "schemaVersion": 1, "status": "installed", "verified": true, "signedBy": "Adoptium", "behind": null,
  "installed": { "...": "the install object" },
  "changes": [ { "kind": "folder", "name": "…/tools/jdk/21.0.9+10-temurin", "action": "create", "value": null } ],
  "notes": [ "signature checked: signed by Adoptium (key 843C48A565F8F04B)" ] }
```

- `status` is `installed` or `already-installed` (then `verified` and `signedBy` are null; the
  install object's own `verified` and `signedBy` say how it was installed).
- `verified` is false when the source publishes only an md5 or sha1 checksum (jkit still checks
  those, but they don't count as verified), or for an `--allow-unverified` install; a download with
  no checksum at all is refused without that flag, unless a good signature stands in for it
  (Corretto).
- `signedBy` is whose signature was checked, or null (see the install object); when a signature
  couldn't be fetched or checked, `notes` says so, and a build checked by its checksum alone has
  the note `checksum checked (sha256)`.
- `behind` is as in `search`: set when the build is from a vendor that stopped updating that
  release, else null.
- Downloads and checksums come only over https. The first install of a tool also sets its global
  link (in `changes`), unless `--no-default`.

## `jkit upgrade`

`jkit upgrade [tool[@major]] [vendor] [--dry-run] --json`.

```json
{
  "schemaVersion": 1,
  "dryRun": true,
  "lines": [
    { "tool": "jdk", "major": 21, "release": "21", "vendor": "temurin", "fx": false,
      "from": "21.0.5+11", "to": "21.0.9+10", "status": "available" }
  ],
  "changes": [ { "kind": "file", "name": "/home/me/.jkit/index/3f9a….cache", "action": "update",
                 "value": "https://api.foojay.io/disco/v3.0/packages?version=21&…" } ]
}
```

- One entry per release line (tool, line, vendor, JavaFX or not). `release` is the line: the
  major version for JDKs, GraalVM and sbt, major.minor (`3.9`) for Maven and Gradle; `major` is its
  first number. `tool` is `graalvm` for a GraalVM-based line.
- `status`: `up-to-date`, `available` (dry run), `upgraded` (then also `verified`, `signedBy`,
  `notes`, `path` and `changes`: the new folder and any global link moved) or `failed` (then
  `error: {message, hints}`, and `to` may be null).
- Exits `1` when any line failed; the other lines still run.
- The top-level `changes` lists the cached files written to check the lines.
- With nothing installed, `lines` is empty.

## `jkit use`

`jkit use <tool>@<version> [vendor] [--global] --json`.

```json
{ "schemaVersion": 1, "scope": "global", "tool": "jdk", "installed": { "...": "the install object" },
  "changed": true,
  "changes": [
    { "kind": "link", "name": "/home/me/.jkit/global/jdk", "action": "set", "value": "/home/me/.jkit/tools/jdk/25.0.1+8-temurin" }
  ],
  "notes": ["new shells with the jkit hook: JAVA_HOME=~/.jkit/global/jdk, PATH includes ~/.jkit/global/jdk/bin"] }
```

- `scope` is `global` or `shell`. Without `--global` the shell hook must be active, as in text mode.
- `changed` is `false` when `--global` names the install the global link already points at;
  `changes` then has no link entry.
- `--global` changes a link. In a hooked shell with no version of its own for the tool and its
  variable (`JAVA_HOME`, `MAVEN_HOME`, …) unset, it also sets that variable to the link and, if
  missing, adds the link's `bin` to `PATH`: `env` changes. Without `--global`, only variables of
  this shell change.
- `notes` are the same lines text mode prints (also on stderr).

## `jkit reset`

`jkit reset <tool> [--global] --json`.

```json
{
  "schemaVersion": 1, "scope": "global", "tool": "maven", "changed": true,
  "changes": [
    { "kind": "link", "name": "/home/me/.jkit/global/maven", "action": "remove", "value": null }
  ],
  "notes": ["installs stay; new shells get no maven from jkit until: jkit use maven@<version> --global"]
}
```

- `scope` is `global` or `shell`. Without `--global` the shell hook must be active, and `changes`
  lists the shell's variables.
- `changed` is false, with `changes` empty, when there was nothing to reset. The text lines go to
  stderr.

## `jkit pin`

`jkit pin <tool>[@version] [vendor] [--fx] [--exact] [--here] --json`.

```json
{ "schemaVersion": 1, "file": "/home/me/src/app/jkit.toml", "created": false, "tool": "jdk",
  "pin": { "version": "21", "vendor": null, "fx": false },
  "previous": { "version": "17", "vendor": null, "fx": false },
  "installed": { "...": "the install object the pin picks, or null when it isn't installed yet" },
  "changes": [ { "kind": "file", "name": "/home/me/src/app/jkit.toml", "action": "update", "value": null } ],
  "notes": [] }
```

- `created` is true when this command created the file.
- `previous` is null when the file didn't pin the tool.
- A `pin.version` of null is a pin to the latest release (`jkit pin jdk@latest`, written as
  `jdk = "latest"`).
- With the shell hook active, `changes` also lists the variables of this shell that now follow
  the project.

## `jkit unpin`

`jkit unpin <tool> [--here] --json`.

```json
{ "schemaVersion": 1, "file": "/home/me/src/app/jkit.toml", "tool": "jdk", "removed": true,
  "previous": { "version": "21", "vendor": null, "fx": false }, "changes": [], "notes": [] }
```

`removed` is false (and nothing changes) when the file didn't pin the tool. `previous` is null
when it didn't, or when the line was one jkit couldn't use (it is removed anyway).

## `jkit sync`

`jkit sync [--dry-run] --json`.

```json
{
  "schemaVersion": 1,
  "dryRun": false,
  "project": { "dir": "/home/me/src/app", "files": ["/home/me/src/app/jkit.toml"], "warnings": [] },
  "tools": [
    { "tool": "jdk", "pin": { "version": "21", "vendor": null, "fx": false },
      "status": "already-installed", "label": "21.0.9+10 temurin", "installed": { "...": "the install object" } }
  ],
  "changes": [], "notes": []
}
```

- `status` per tool: `already-installed`, `installed`, or in a dry run `would-install` or
  `would-refuse` (the source publishes no checksum or signature jkit can check, so the real sync
  needs `--allow-unverified`).
- `installed` is null in a dry run for a tool that isn't installed yet.
- `changes` lists new install folders and, with the shell hook active, the variables of this
  shell that now follow the project; without the hook `notes` says the shell is unchanged.
- With no project file, it's an error document.

## `jkit remove`

`jkit remove <tool>@<version> [vendor] --json`.

```json
{ "schemaVersion": 1, "removed": [ { "...": "the install object, as it was before removal" } ],
  "changes": [ { "kind": "folder", "name": "…/tools/jdk/17.0.13+11-zulu", "action": "delete", "value": null } ] }
```

`changes` also lists global links removed with `--force`.

## `jkit prune`

`jkit prune [--dry-run] --json`.

```json
{ "schemaVersion": 1, "dryRun": false,
  "builds": [ { "install": { "...": "the install object, as it was before removal" },
                "newer": "21.0.9+10", "bytes": 201326592 } ],
  "inUse": [ { "install": { "...": "the install object" }, "newer": "8.14.3", "bytes": 150994944,
               "pid": 48213, "command": "/home/me/.jkit/tools/jdk/21.0.9+10-temurin/bin/java" } ],
  "leftovers": [ "/home/me/.jkit/tmp/install-4821" ],
  "cache": [ "/home/me/.jkit/index/0a579b56721481afc1486ff0.cache" ],
  "bytes": 201330000, "changes": [], "notes": [] }
```

- `builds` are installs with a newer stable build in the same release line (`newer`), other than
  a global one, one this shell uses, or one the project here pins.
- `inUse` are such builds kept because a running program uses them: `pid` and `command` (null
  when the OS doesn't say) name one, whose executable is in the build or whose arguments name a
  file in it (a Gradle daemon's classpath). A `jkit use` in another shell that runs nothing from
  the build can't be seen.
- `leftovers` are folders interrupted installs and removals left. A work folder jkit can't tell
  is in use (on file systems without locks) is never removed and isn't listed, but named on
  stderr with its age. `cache` holds the cached version lists older than 30 days.
- `bytes` is the space freed (or, in a dry run, that would be). `changes` is empty in a dry run.

## `jkit config`

`jkit config [list] --json`.

```json
{ "schemaVersion": 1, "file": "/home/me/.jkit/config.toml", "error": null,
  "settings": [
    { "key": "jdk.vendor", "value": "zulu", "source": "file", "file": "zulu", "env": null,
      "envVariable": "JKIT_JDK_VENDOR", "default": "temurin",
      "description": "the vendor jdk@… uses when none is named" }
  ] }
```

- `value` is what jkit uses; `source` says where it came from: `env` (the variable named by
  `envVariable`, which wins), `file` (config.toml) or `default`.
- `file` and `env` are the raw values each place holds, null where unset.
- `error` says why config.toml can't be used (a wrong type, say), or is null. While it is set, the
  file's values don't apply, so their `source` is `default`.
- Keys: `jdk.vendor`, `graalvm.vendor`, `shell.auto_switch` (`value` `"true"` or `"false"`).

`jkit config get <key> --json` prints one of those objects as the document. `jkit config set <key>
<value> --json` and `jkit config unset <key> --json` print it as it is after the change, plus
`changed` (false when the file already said so), `changes` and `notes`.

## `jkit self-update`

`jkit self-update [--check] --json`.

```json
{ "schemaVersion": 1, "current": "0.5.0", "latest": "0.6.0", "status": "managed",
  "installedWith": "homebrew", "binary": "/opt/homebrew/Cellar/jkit/0.5.0/bin/jkit",
  "command": "brew upgrade jkit", "changes": [], "notes": [] }
```

- `status`: `up-to-date`, `available` (with `--check`), `updated` (a release-archive install,
  replaced in place: `changes` names the binary), or `managed` (installed with a package manager,
  left alone: `command` is what updates it).
- `installedWith`: `homebrew`, `deb`, `rpm`, `winget`, `msi` (the Windows `.msi`, either
  scope; from 0.6.0) or `archive`.
- `command` is null unless a newer release is out and a package manager installed jkit.
- `latest` is the newest stable release; pre-releases are on the releases page only.

## See also

- [Exit codes](/reference/exit-codes/)
- [AI agents](/guides/ai-agents/) and [Scripts](/guides/scripts/)
- [`jkit status`](/reference/commands/status/) and [`jkit doctor`](/reference/commands/doctor/)
