---
title: Getting help
description: Where to look when jkit doesn't do what you expect, where to ask questions, what to include in a bug report, and how to report a security problem privately.
sidebar:
  order: 3
---

This page says where to look first when something goes wrong, and how to ask for help or report a
bug so it can be fixed quickly.

## Ask jkit first

Every command explains itself:

```bash
jkit --help                  # every command, one line each
jkit install --help          # one command's options, with examples
```

When a version isn't the one you expected, or something seems broken, run the read-only
diagnostics. They change nothing and don't use the network:

```bash
jkit status                  # what's active here, and why
jkit doctor                  # what's wrong with the setup, with a fix for each problem
jkit doctor --network        # also checks that the download sources can be reached
```

jkit's errors end with hints: indented lines with the command to run next. Most problems are
solved by running one of them. The [troubleshooting](/help/troubleshooting/) page lists the common
messages by symptom, and the [FAQ](/help/faq/) answers the common questions.

## Ask a question

Questions, ideas and show-and-tell go in
[GitHub Discussions](https://github.com/adriandeleon/jkit/discussions). Search there first; the
answer may already be in a thread. Every release and news post is also announced in the
[Announcements](https://github.com/adriandeleon/jkit/discussions/categories/announcements)
category, where you can comment on it or subscribe to hear about the next one.

Something that's clearly broken is a bug: open an issue instead, as below.

## Report a bug

Open an issue at [github.com/adriandeleon/jkit/issues](https://github.com/adriandeleon/jkit/issues).
Search the open issues first; someone may have reported it already.

Include:

- **The jkit version**: the output of `jkit --version`.
- **Your OS and how you installed jkit**: for example "Ubuntu 24.04, from the `.deb`" or
  "macOS 15 on Apple Silicon, Homebrew". The first line of `jkit doctor` also shows the platform.
- **Your shell**: bash, zsh, fish or PowerShell.
- **What you ran, what you expected, and what happened**, with the full output.
- **The output of `jkit doctor`**, and of `jkit status` if the problem is about which version is
  active.
- For a failing command, its **`--json` output** if it has the flag. It includes the exact
  message, the hints and the exit code.
- For `error: unexpected error: ...`, run the command again with `JKIT_DEBUG=1` set and include
  what it prints:

  ```bash
  JKIT_DEBUG=1 jkit install jdk@21
  ```

Before you paste output, look it over: paths show your user name, and environment output
(`jkit env`) may include other variables from your `PATH`. Remove anything you'd rather not share.

## Report a security problem privately

Don't open a public issue for a vulnerability. Use
[Report a vulnerability](https://github.com/adriandeleon/jkit/security/advisories/new) on the
repository's Security tab (GitHub private vulnerability reporting). Include the jkit version, your
OS, and the steps to reproduce. You should get a reply within a week. Once a fix is released, the
advisory is published with credit to you, unless you'd rather stay anonymous.

Only the latest release gets security fixes, so upgrade (`jkit self-update`) before reporting if
you can. What jkit checks, and what it doesn't, is described in [security](/reference/security/).

## Contribute

jkit is MIT-licensed and developed in the open at
[github.com/adriandeleon/jkit](https://github.com/adriandeleon/jkit). The README there explains
how to build it (it needs JDK 25; Maven comes through the wrapper) and how the modules fit
together, and the `docs/design/` folder has design notes for contributors.
