# workflows

**Phase 4 onward.** Built up one stage at a time:

| Phase | Added |
|---|---|
| 4 | checkout, build, unit tests |
| 5 | Docker build and commit-SHA tagging |
| 6 | Trivy scan, static analysis, metric collection |
| 7 | feature extraction, call the ML risk engine |
| 8 | evaluate the decision: approve, gate, or block |
| 9 | push image, deploy to Kubernetes |
| 11 | post-deploy health watch and automatic rollback |

Every stage produces useful logs and a meaningful failure message. Credentials come
from GitHub Actions Secrets and OIDC, never from the repository.
