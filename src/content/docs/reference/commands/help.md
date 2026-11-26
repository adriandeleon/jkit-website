---
title: jkit help
description: Show jkit's list of commands, or the usage and options of one command.
sidebar:
  order: 21
---

Shows help. With no command it lists every command with a one-line summary, as `jkit --help`
does; with a command it prints that command's usage and options, as `jkit <command> --help` does.

## Usage

```text
jkit help [-h] [COMMAND]
```

```bash
jkit help             # every command
jkit help install     # the same as jkit install --help
```

It changes nothing and never uses the network.

## See also

- [Getting help](/help/getting-help/)
- [`jkit doctor`](/reference/commands/doctor/)
