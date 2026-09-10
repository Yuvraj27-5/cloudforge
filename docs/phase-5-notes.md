# Phase 5 — Docker

The sample app is containerised. CloudForge's own services stay native: adding a
rebuild cycle to every backend code change buys nothing until Phase 9.

## Tagging

Every build is tagged with the **full commit SHA**. That tag is immutable, and it
is what Kubernetes will reference in Phase 9.

This is the decision that makes rollback possible. `latest` is a moving pointer —
if a deployment references it, there is no previous artifact to return to, and
Phase 11 has nothing to roll back *to*. Rollback is not a code path you can add
later if the image naming scheme forbids it.

`latest` is still written, but only for a clean working tree on `main`. An image
built from uncommitted changes gets `-dirty` appended and never claims to be
current.

| Tag | Purpose |
|---|---|
| `<full-sha>` | Immutable. Referenced by manifests and rollbacks |
| `<short-sha>` | For humans reading `docker images` |
| `latest` | Only from a clean `main`. Convenience, never referenced by a deployment |

## Dockerfile decisions

**Exec-form CMD.** `CMD ["node", "src/server.js"]` makes node PID 1 so it receives
SIGTERM directly. Shell form would put `/bin/sh` at PID 1, which swallows the
signal and silently defeats the graceful shutdown written in Phase 3. Rolling
updates would then drop connections, and Phase 10's error-rate metric would show
spikes caused by the platform itself.

**`USER node`.** Running as root inside a container means a container escape starts
with root on the node.

**No `npm install` layer.** Zero dependencies by design, so there is no dependency
layer to cache and no transitive package for a supply-chain attack to arrive
through. Phase 6's Trivy scan will therefore report on the base image only, which
is the point.

**`node:24-alpine`, not distroless.** Distroless has a smaller attack surface and
is worth revisiting once Phase 6 shows what Trivy actually finds. Alpine keeps a
shell available for debugging a misbehaving pod, which matters more while Phase 9
is being built.

**HEALTHCHECK uses `node -e`,** not curl or wget, so the image needs no extra
tooling. This is Docker-level health, separate from the Kubernetes probes added in
Phase 9.

**OCI labels** carry the commit, version and build time, so an image found on a
node can be traced back to the commit that produced it.

## CI

The `docker-build` job builds the image, runs it, and asserts four things:

1. The container becomes healthy within 30 seconds
2. `GET /` reports the same commit SHA the image is tagged with — proving the build
   arg reached the running process, so an image cannot be tagged with one SHA while
   containing another
3. The process runs as `node`, not root
4. The image size is reported, so growth is visible in the log

Nothing is pushed: there is no registry until Phase 12. GitHub Actions cache
(`type=gha`) keeps rebuilds fast.

## Local use

```powershell
.\scripts\build-image.ps1
docker run --rm -p 3000:3000 cloudforge-sample-app:<short-sha>
```

Or through Compose, behind a profile so the default `docker compose up -d` still
starts only the database:

```powershell
docker compose --profile sample up -d --build
docker compose --profile sample down
```

Set `SAMPLE_FAILURE_MODE=unhealthy` in `.env` to run the degrading variant in a
container, which is the shape Phase 11 will roll back.

## Verification status

The Dockerfile and the CI job are **not** locally verified — no Docker daemon in
the environment where they were written. The Compose file and the workflow YAML
both parse. The action versions (`setup-buildx-action@v4`, `build-push-action@v7`)
were checked against current documentation rather than assumed.
