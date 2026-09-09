# Roadmap

Every phase is a vertical slice: it ends with something you can run and check, not a
layer you have to trust. Do not start a phase before the previous one meets its exit
criterion.

| Phase | Deliverable | Exit criterion | Touches |
|---|---|---|---|
| 0 | Repository, local environment | Three services start; backend reaches Postgres; frontend reaches backend | root, all modules |
| 1 ✅ | Project CRUD end to end | Create a project in the UI, see the row in Postgres | `backend/project`, `frontend/pages`, migrations |
| 2 | Deployment records | Full deployment lifecycle representable before any real deploy exists | `backend/deployment`, `frontend/pages` |
| 3 | Sample application | Runs locally; healthy and unhealthy variants both behave | `sample-app` |
| 4 | Basic CI | Every push builds and tests automatically | `.github/workflows` |
| 5 | Docker | Sample app runs in a container, tagged by commit SHA | `sample-app`, workflows |
| 6 | Security and code analysis | Trivy and static analysis run in CI; metrics stored | workflows, `backend/deployment` |
| 7 | ML risk engine | `POST /predict-risk` returns a scored, explained result the backend consumes | `ml-service`, `backend/risk` |
| 8 | Decision engine | LOW approves, MEDIUM gates, HIGH blocks; every decision persisted | `backend/risk`, `backend/deployment` |
| 9 | Local Kubernetes | Sample app deployed to kind; pods, service, probes verified | `infrastructure/kubernetes` |
| 10 | Monitoring | Prometheus scrapes metrics; Grafana dashboards render | `monitoring` |
| 11 | Automatic rollback | A deliberately failing deployment rolls back on its own and records why | `backend/cloud`, workflows |
| 12 | AWS | ECR to EKS deployment through the same abstraction | `infrastructure/aws`, `backend/cloud` |
| 13 | Azure | ACR to AKS, reusing the abstraction with no duplicated logic | `infrastructure/azure`, `backend/cloud` |

## Sequencing rules

**Phase 9 gates 12 and 13.** If deployment and rollback are not solid against a
local cluster, cloud credentials only add expensive, slow failure modes to debug.

**Phase 8 must not depend on Phase 7's internals.** The decision engine reads a risk
score through an interface. Swapping the model must not touch the decision code.

**Phase 11 reuses Phase 10.** Rollback triggers read the metrics that already exist
rather than building a parallel health system.

## Deferred until genuinely needed

Authentication (Spring Security + JWT, then GitHub OAuth) after the core loop works;
Kafka only if event fan-out becomes real. Both are listed in the original design as
explicitly non-blocking for the MVP.
