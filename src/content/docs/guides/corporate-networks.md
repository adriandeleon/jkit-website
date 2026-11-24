---
title: Corporate networks
description: Use jkit behind an HTTP proxy, with internal mirrors, offline, or on air-gapped machines.
sidebar:
  order: 16
---

This page is for developers and admins on networks that don't allow direct internet access:
behind a proxy, with internal mirrors of Maven Central and other sources, or with no network at
all. Commands that only read what is installed (`list`, `status`, `env`, `exec`, `doctor`) never
use the network, so they work the same everywhere.

## What jkit connects to

| What | Default source |
| --- | --- |
| JDK and GraalVM version lists and download links | `api.foojay.io` |
| JDK downloads | wherever the vendor hosts them, for example `github.com` (Temurin), `cdn.azul.com` (Zulu) or `corretto.aws` |
| Maven, and the list of sbt versions | `repo.maven.apache.org` |
| Gradle | `services.gradle.org` |
| sbt downloads | `github.com` (sbt's releases) |
| `jkit self-update` | `github.com` (jkit's releases) |

`jkit install` names the host before it downloads:

```console
$ jkit install jdk@21
downloading jdk 21.0.9+10 temurin from github.com
```

Everything goes over https. The one exception is a mirror on the same machine
(`http://localhost`), described below.

## Proxies

jkit reads the proxy from the environment:

```bash
export HTTPS_PROXY=http://proxy.example.com:3128
export NO_PROXY=localhost,.internal.example.com
```

- `HTTPS_PROXY` (or `https_proxy`) is the proxy for every request. `JKIT_NETWORK_PROXY` wins over
  both, for when jkit needs a different proxy from your other tools.
- `HTTP_PROXY` isn't read: jkit makes no plain-http requests to the internet.
- The proxy must be an HTTP proxy, written `http://host:port`. SOCKS proxies aren't supported.
- Credentials go in the URL: `http://user:password@proxy.example.com:3128`. Encode special
  characters in the password with `%` (`@` is `%40`). jkit hides the password in its messages.
- `NO_PROXY` (or `no_proxy`) lists hosts to reach directly, separated by commas or spaces: an
  exact name, a domain (`example.com` or `.example.com` covers its subdomains too), or `*` for
  everything. A port after a name is ignored.
- A mirror on `localhost` is always reached directly, never through the proxy.

A malformed proxy setting doesn't stop commands that don't use the network; the first command
that does reports the error with a hint.

To check the connection: `jkit doctor --network` tries to reach foojay, services.gradle.org,
Maven Central and GitHub (or your mirrors) and reports each.

### Proxies that inspect HTTPS

Some company proxies decrypt and re-sign HTTPS traffic with their own certificate authority. jkit
has no setting for an extra CA certificate, so the connection fails:

```console
error: secure connection to api.foojay.io failed: …
  a company proxy that inspects HTTPS can cause this; its certificate is not trusted yet
```

Ask your network team to exempt the hosts above from inspection, or use internal mirrors (next
section) that have a certificate jkit trusts.

## Mirrors

Each source can be replaced with a mirror through an environment variable. The value is the base
URL; jkit adds the paths below it.

| Variable | Replaces | What it must serve |
| --- | --- | --- |
| `JKIT_SOURCES_FOOJAY_URL` | `https://api.foojay.io/disco/v3.0/` | The foojay Disco API |
| `JKIT_SOURCES_MAVEN_URL` | `https://repo.maven.apache.org/maven2/` | A Maven repository layout (Nexus, Artifactory and other repository managers proxy Maven Central this way) |
| `JKIT_SOURCES_GRADLE_URL` | `https://services.gradle.org/` | `versions/all` and the files it lists |
| `JKIT_SOURCES_SBT_URL` | `https://github.com/sbt/sbt/releases/download/` | sbt's release zips and their `.sha256` and `.asc` files |
| `JKIT_SOURCES_RELEASES_URL` | `https://github.com/adriandeleon/jkit/releases/` | jkit's release files, for `jkit self-update` |

```bash
export JKIT_SOURCES_MAVEN_URL=https://nexus.example.com/repository/maven-central/
jkit install maven@3.9
```

- **https only.** A mirror URL must start with `https://`, or jkit refuses it:

  ```console
  error: JKIT_SOURCES_MAVEN_URL must be an https URL (or http to localhost), not http://repo.example.com/m2
    unset it to use the default source
  ```

  The one exception is plain `http://` to this machine (`localhost`, `127.0.0.1` or `[::1]`), for
  a mirror or cache running locally. Redirects are checked too: https stays https.
- **Download links.** For Maven and sbt, the downloads come from the mirror itself. For JDKs and
  Gradle, the version list names the download links, so a foojay or Gradle mirror must also
  rewrite those links to internal https addresses if downloads have to stay inside your network.
- **Checksums and signatures still apply.** jkit checks every download against the checksum its
  source publishes. Maven 3.9.0 and later and sbt are also checked against their publisher's
  signature: a mirror that doesn't serve the `.asc` files gives a warning that only the checksum
  was checked. A Maven mirror with only `.md5` files counts as having no checksum, and the install is
  refused unless you pass `--allow-unverified`. See [security](/reference/security/).

## Offline use

Version lists are cached in `~/.jkit/index/` for 24 hours, so `jkit search` and `jkit install`
don't ask the network again within that time. `--refresh` fetches them again.

`--offline` (on `search`, `install`, `upgrade`, `pin` and `sync`), or `JKIT_NETWORK_OFFLINE=1`
for every command, never contacts the network and uses only cached version lists, however old:

```bash
JKIT_NETWORK_OFFLINE=1 jkit search jdk@25      # from the cache
jkit sync --offline                            # checks the pins are installed
```

Offline, jkit can tell you what is installed or out of date, but it can't download anything:

```console
$ jkit install gradle --offline
error: --offline, so gradle 9.9.2 can't be downloaded
  run the command without --offline
```

A lookup that was never cached says so and asks you to run it once online. The cache keeps at
most 32 MB and nothing older than 30 days; `jkit prune` removes cached lists older than 30 days.

## Air-gapped machines

On a machine with no network at all, copy a prepared jkit home from a connected machine of the
same operating system and CPU (for example both Linux x64).

1. On the connected machine, install what you need into a fresh home:

   ```bash
   export JKIT_HOME=$HOME/jkit-bundle
   jkit install jdk@21
   jkit install maven@3.9
   jkit search jdk@21 >/dev/null       # optional: cache version lists for search --offline
   tar -czf jkit-bundle.tar.gz -C "$HOME" jkit-bundle
   ```

2. Copy `jkit-bundle.tar.gz` and the jkit binary (or package) to the air-gapped machine.

3. Unpack it as the user who will use it, and point jkit at it:

   ```bash
   tar -xzf jkit-bundle.tar.gz -C "$HOME"
   mv "$HOME/jkit-bundle" "$HOME/.jkit"         # or keep the name and set JKIT_HOME
   export JKIT_NETWORK_OFFLINE=1                # put this in your shell startup file
   jkit list
   ```

The global links in `~/.jkit/global/` are relative on Linux and macOS, so the home still works
after the move. Each install folder keeps a `jkit-install.properties` file recording where it
came from and whether it was verified, so that information travels with it.

The home must belong to the user who runs jkit (or to root) and not be writable by other users;
see [Shared machines](/guides/shared-machines/) for a home many users share.
