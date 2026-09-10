# syntax=docker/dockerfile:1

# node:24-alpine, pinned to a major that matches package.json engines.
#
# Distroless would cut the attack surface further and is worth revisiting once
# Phase 6 shows what Trivy actually reports here. Alpine is the conservative
# choice for now: a shell is available for debugging a misbehaving pod, which
# matters more while Phase 9 is being built than a marginally cleaner scan.
FROM node:24-alpine

# Metadata consumed by registries and by CloudForge itself. Values are supplied at
# build time; the defaults make an unlabelled local build obvious rather than
# silently wrong.
ARG COMMIT_SHA=unknown
ARG APP_VERSION=0.0.0-local
ARG BUILD_TIME=unknown

LABEL org.opencontainers.image.title="cloudforge-sample-app" \
      org.opencontainers.image.description="The workload CloudForge deploys, scores and rolls back" \
      org.opencontainers.image.revision="${COMMIT_SHA}" \
      org.opencontainers.image.version="${APP_VERSION}" \
      org.opencontainers.image.created="${BUILD_TIME}" \
      org.opencontainers.image.source="https://github.com/cloudforge/sample-app"

ENV NODE_ENV=production \
    PORT=3000 \
    COMMIT_SHA=${COMMIT_SHA} \
    APP_VERSION=${APP_VERSION}

WORKDIR /app

# No npm install: this application has zero dependencies by design, so there is
# no dependency layer to cache and nothing for a supply-chain attack to enter
# through.
COPY package.json ./
COPY src/ ./src/

# node:alpine ships a "node" user. Running as root inside a container means a
# container escape starts with root on the node.
USER node

EXPOSE 3000

# Docker-level health, distinct from the Kubernetes probes added in Phase 9.
# Uses node rather than curl or wget so the image needs no extra tooling.
HEALTHCHECK --interval=15s --timeout=3s --start-period=5s --retries=3 \
    CMD ["node", "-e", "fetch('http://127.0.0.1:3000/health').then(r => process.exit(r.ok ? 0 : 1)).catch(() => process.exit(1))"]

# Exec form, so the process is PID 1 and receives SIGTERM directly. Shell form
# would put /bin/sh at PID 1, swallow the signal, and defeat the graceful
# shutdown written in Phase 3.
CMD ["node", "src/server.js"]
