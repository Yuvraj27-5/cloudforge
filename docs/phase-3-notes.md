# Phase 3 — Sample Application

A Node service with no dependencies, used as the workload for every later phase.

## Decisions

**Zero dependencies.** Built-in `http` and `node:test`. Phase 6 runs Trivy against
this image, and a dependency-free app makes that scan meaningful rather than a wall
of transitive CVEs from a web framework. It also keeps the image small, which
matters when Phase 11 reloads it into kind on every rollback test.

**Failure is configuration, not a fork.** `FAILURE_MODE` switches behaviour on one
codebase. A separate broken copy drifts from the good one, and config-driven
failure is closer to how real incidents happen.

**The failure is delayed, not immediate.** `FAILURE_DELAY_SECONDS` defaults to 30 so
the app deploys, passes its first probes, reaches `SUCCEEDED`, and only then
degrades. A version that fails instantly would test deployment failure, not
rollback — the two are different code paths in Phase 11.

**Liveness and readiness are separate endpoints.** Kubernetes restarts a pod that
fails liveness but only depools one that fails readiness. Pointing both probes at
one endpoint turns a slow start into a CrashLoopBackOff.

**Graceful SIGTERM handling.** Readiness goes false, in-flight requests drain, then
exit. Without it, every rolling update and every rollback drops connections, and
Phase 10's error-rate metric would show spikes caused by the platform itself.

## Failure modes

| Mode | Behaviour | Used by |
|---|---|---|
| `none` | Healthy | Phase 9 happy path |
| `unhealthy` | `/health` 503 after the delay | Phase 11 health-based rollback |
| `crash` | Process exits, CrashLoopBackOff | Phase 11 pod-status rollback |
| `slow` | Responses delayed | Phase 10 latency and error-rate alerts |

Invalid values throw at startup rather than being silently ignored. A rollback test
that quietly ran in healthy mode would look like a passing test.

## Verified

Seven tests pass. All three failure modes exercised by hand: healthy stays 200,
`unhealthy` flips to 503 on schedule while the process stays alive, `crash` exits
and refuses connections.
