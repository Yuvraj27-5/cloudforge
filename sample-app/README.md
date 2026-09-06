# sample-app

**Phase 3.** The workload CloudForge deploys, so the platform can be exercised end
to end.

- `GET /` returns a static payload
- `GET /health` is the target for Kubernetes liveness and readiness probes
- A deliberately unhealthy variant proves automatic rollback (Phase 11)

Intentionally trivial. Complexity here would test the sample app, not CloudForge.
