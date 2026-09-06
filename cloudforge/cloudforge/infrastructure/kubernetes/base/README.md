# kubernetes/base

**Phase 9.** Provider-neutral manifests for `sample-app`:

- `deployment.yaml` with liveness and readiness probes and resource requests/limits
- `service.yaml`
- `configmap.yaml` for non-secret configuration
- `kustomization.yaml`

Images are tagged by commit SHA, never `latest` — rollback needs an immutable
reference to the previous known-good version.
