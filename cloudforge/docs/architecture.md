# Architecture

## Call flow (MVP)

```
React (5173)  ->  Spring Boot (8080)  ->  FastAPI (8000)
                        |
                        v
                  PostgreSQL (5432)
```

Synchronous REST. No message broker. Only the backend talks to the database.

Kafka is deliberately deferred until there is genuine asynchronous fan-out
(pipeline, deployment, monitoring, and alert events with multiple consumers).
Introducing it during the MVP buys distributed-systems debugging with no benefit.

## Responsibility split

- **Backend** owns persistence, orchestration, and the deployment *decision*.
- **ML service** owns feature validation, scoring, and explanation. It is stateless
  and knows nothing about projects or deployments.
- **Frontend** renders state. No business rules.

The backend will call the ML service through a `RiskAssessmentProvider` interface.
The threshold mapping (LOW / MEDIUM / HIGH -> APPROVE / EXTRA_VALIDATION / BLOCK)
lives in the backend, not the model. That keeps the risk provider swappable —
heuristic today, gradient boosting tomorrow, a hosted service later — without
touching the decision engine.

## Multi-cloud abstraction

```java
public interface CloudDeploymentProvider {
    DeploymentHandle deploy(DeploymentRequest request);
    void rollback(DeploymentHandle handle);
    DeploymentStatus getDeploymentStatus(DeploymentHandle handle);
    List<LogEntry> getLogs(DeploymentHandle handle);
}
```

Implementations: `LocalKubernetesProvider` (Phase 9), `AwsDeploymentProvider`
(Phase 12), `AzureDeploymentProvider` (Phase 13). Strategy pattern, selected per
project by configured cloud provider.

The orchestration service must contain zero provider-specific branches. If a
`if (provider == AWS)` appears in the orchestrator, the abstraction has failed.

## Backend package layout

Feature-first, not layer-first:

```
com.cloudforge.backend
├── common/       cross-cutting: config, exception handling, shared DTOs
├── system/       meta endpoints
├── project/      Phase 1
├── deployment/   Phase 2
└── risk/         Phase 7
```

Each feature package holds its own controller, service, repository, entity, and
DTOs. Cross-feature access goes through service interfaces only.

A `controllers/` + `services/` + `repositories/` split looks tidy at five classes
and becomes unnavigable at fifty. It also makes accidental coupling between the
deployment engine and the risk engine easy, which the design explicitly forbids.

## Deliberately deferred

| Thing | Arrives in |
|---|---|
| Authentication (Spring Security + JWT) | after the core loop works |
| Containerising the CloudForge services themselves | Phase 5 |
| `infrastructure/` and `monitoring/` directories | Phases 9 and 10 |
| Kafka | only if event fan-out becomes real |

Empty directories that sit unused for ten phases train people to ignore the
repository structure, so they are created when first needed.
