# sample-app

The workload CloudForge deploys, scores and rolls back. Deliberately trivial:
complexity here would test the sample app, not the platform.

**Zero dependencies.** Node's built-in `http` and `node:test` only. No
`npm install`, no dependency vulnerabilities polluting Phase 6's Trivy scan, and a
small image for kind to reload on every Phase 11 rollback test.

## Run

```powershell
cd sample-app
npm start
```

```powershell
npm test
```

## Endpoints

| Path | Purpose |
|---|---|
| `GET /` | Which build is running: version, commit SHA, uptime |
| `GET /health` | Liveness. 503 once a failure mode trips |
| `GET /ready` | Readiness. 503 during startup and shutdown |

`/health` and `/ready` are separate on purpose. Kubernetes restarts a pod that
fails liveness but only removes one that fails readiness from the load balancer.
Conflating them turns a slow start into a restart loop.

Every response carries `X-Correlation-Id`, echoing the caller's when present. That
is what makes a deployment traceable from CloudForge through to this pod.

## Failure modes

Failure is configuration, not a forked codebase. A separate "broken copy" would
drift from the good one; this way the same artifact misbehaves under different
config, which is also how most real incidents happen.

| `FAILURE_MODE` | Behaviour | Proves |
|---|---|---|
| `none` | Healthy for ever | The happy path |
| `unhealthy` | `/health` returns 503 after `FAILURE_DELAY_SECONDS` | Health-based rollback (Phase 11) |
| `crash` | Process exits, producing CrashLoopBackOff | Pod-status rollback (Phase 11) |
| `slow` | Every response delayed by `SLOW_RESPONSE_MS` | Latency and error-rate thresholds (Phase 10) |

```powershell
$env:FAILURE_MODE="unhealthy"; $env:FAILURE_DELAY_SECONDS="10"; npm start
```

The delay matters: the app must deploy successfully and pass its first probes
before degrading. A version that fails immediately never reaches `SUCCEEDED`, so it
tests deployment failure rather than rollback.

## Graceful shutdown

SIGTERM sets readiness false and drains in-flight requests before exiting. Without
it, every rolling update and rollback drops live connections.

## Not here yet

The Dockerfile arrives in Phase 5, Kubernetes manifests in Phase 9. This phase is
the application only.
