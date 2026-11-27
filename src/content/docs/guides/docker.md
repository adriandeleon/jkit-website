---
title: Docker
description: Install a JDK with jkit in a Docker build stage and copy it into a slim runtime image, or keep jkit in the image.
sidebar:
  order: 13
---

This page shows how to use jkit in container images: install jkit and the project's JDK in a
build stage, build there, and copy only the JDK into the image you ship. It assumes you know
Dockerfiles and multi-stage builds.

:::note[Which jkit build]
The `linux-x64` and `linux-aarch64` builds need glibc 2.34 or newer and zlib: Debian 12
(`debian:12-slim`, also `bookworm`), Ubuntu 22.04 or 24.04, or Red Hat UBI 9 work; Ubuntu 20.04
and RHEL 8 don't. From 0.5.0, the `linux-x64-musl` build is static and needs nothing from the
system, so it runs on Alpine (x64) and on older glibc images too. On Alpine, jkit installs the
musl builds of a JDK. There is no arm64 musl build.
:::

## Build stage, then a slim runtime image

```dockerfile title="Dockerfile"
# ---- build: jkit, the project's JDK and Maven, and the build itself
FROM debian:12-slim AS build
ARG JKIT_VERSION=0.5.0
RUN apt-get update \
 && apt-get install -y --no-install-recommends ca-certificates curl \
 && rm -rf /var/lib/apt/lists/*

# jkit, checked against the release's SHA256SUMS
RUN set -eu; \
    case "$(uname -m)" in x86_64) arch=x64 ;; aarch64) arch=aarch64 ;; esac; \
    file="jkit-$JKIT_VERSION-linux-$arch.tar.gz"; \
    base="https://github.com/adriandeleon/jkit/releases/download/v$JKIT_VERSION"; \
    cd /tmp; \
    curl -fsSLO "$base/$file"; \
    curl -fsSLO "$base/SHA256SUMS"; \
    sha256sum -c SHA256SUMS --ignore-missing; \
    tar -xzf "$file" -C /usr/local/bin jkit; \
    rm "$file" SHA256SUMS

# a fixed home, whatever user or HOME the build runs with
ENV JKIT_HOME=/opt/jkit

WORKDIR /src
# the project file first, so this layer is cached until the pins change
COPY jkit.toml ./
RUN jkit sync

COPY . .
RUN jkit exec -- mvn -B package -DskipTests

# copy the project's JDK to a plain folder for the runtime stage
RUN jkit exec -- sh -c 'cp -a "$JAVA_HOME/." /opt/java/'

# ---- runtime: only the JDK and the application
FROM debian:12-slim
COPY --from=build /opt/java /opt/java
COPY --from=build /src/target/app.jar /app/app.jar
ENV JAVA_HOME=/opt/java
ENV PATH="/opt/java/bin:$PATH"
USER 1000
CMD ["java", "-jar", "/app/app.jar"]
```

Notes:

- The example assumes `jkit.toml` pins both `jdk` and `maven`. A build that uses the Maven or
  Gradle wrapper only needs the `jdk` pin.
- `jkit sync` installs what `jkit.toml` pins, and `jkit exec` runs the build with those versions.
  Without the shell hook, `sync` only installs, which is what you want in a `RUN` line.
- `jkit exec -- sh -c '… $JAVA_HOME …'` reads the JDK folder the project pins, so the runtime
  stage gets exactly the JDK the build used. `cp -a "$JAVA_HOME/."` copies the folder's contents
  and keeps the links inside the JDK as they are.
- Change `mvn -B package` to `./mvnw`, `./gradlew build` or `sbt` as your project needs, and the
  path of the jar to match.
- The runtime image has no jkit and no curl. It only needs what the JDK itself needs, which
  `debian:12-slim` has.

Without a `jkit.toml`, name the version instead: `RUN jkit install jdk@21` and
`jkit exec jdk@21 -- …`. A `.sdkmanrc`, `.tool-versions` or `.java-version` works in place of
`jkit.toml`; copy that file instead.

## Keeping jkit in the image

For a development or CI image, keep jkit and its home in the image and run everything through
`jkit exec`:

```dockerfile
ENV JKIT_HOME=/opt/jkit
RUN jkit install jdk@21 && jkit install maven
RUN chmod 755 /opt/jkit
```

`chmod 755` matters when the image later runs as another user: jkit creates its home readable
only by the user who made it (root here). jkit accepts a home owned by root, and the installs in
it, from any user, so a non-root user can then run `jkit exec`, `jkit env`, `jkit list` and
`jkit status`. Installing more, or `jkit use … --global`, needs a user who can write to the home.

`jkit exec` forwards a `docker stop` correctly when it is the container's main process: it passes
SIGTERM on to the command, waits for it, and exits with its exit code.

## `JKIT_HOME` in images

Set `JKIT_HOME` to an absolute path in images. Otherwise jkit uses `$HOME/.jkit`, and `HOME`
depends on the user: with `docker run -u 1001` and a user that isn't in `/etc/passwd`, `HOME`
may be `/` or not set at all, and jkit can't make a home there.

jkit refuses a home that another user (other than root) owns, or that is writable by every user,
or that sits in a folder anyone could rename it out of. `/opt/jkit` created by root passes these
checks. See [Shared machines](/guides/shared-machines/) for the exact rules.

## Bind mounts and project files

When you mount a project into a container (`docker run -v "$PWD":/src …`), its files keep their
owner from the host, often your user id rather than the container's. jkit ignores a project file
that belongs to another user (other than root) or that every user can write to, so that someone
else's file can't choose which programs you run. Inside the container you then see a warning
from `jkit status` or `jkit sync` and the global versions instead of the pinned ones.

Fixes, from simplest:

- Name the file: `-e JKIT_PROJECT_FILE=/src/jkit.toml`. An absolute `JKIT_PROJECT_FILE` is your
  own choice and is used without the ownership checks.
- Run as your own user: `-u "$(id -u):$(id -g)"` (with `JKIT_HOME` readable by that user, as
  above).
- Some file sharing setups make every file look world-writable; fixing the mount options or the
  permissions also works.

## Caching downloads between builds

`jkit sync` in its own layer, after copying only the project file, is usually enough: Docker
reuses the layer until `jkit.toml` changes. In CI, see [CI](/guides/ci/) for caching
`~/.jkit/tools` across runs.
