# Phase 2 — Deployment Management

## The status model

```
PENDING ──► RUNNING ──┬─► SUCCEEDED ──► ROLLED_BACK
                      ├─► FAILED
                      └─► CANCELLED
PENDING ──► BLOCKED | CANCELLED
```

Three decisions the rest of the project depends on:

**BLOCKED is not FAILED.** A deployment the risk engine refuses is the product
working correctly, not a broken build. Folding them together would make the
dashboard's success rate meaningless in Phase 8.

**ROLLED_BACK follows SUCCEEDED only.** A rollback means it went live and then
degraded. Never deploying at all is a different story, and Phase 11 has to tell
them apart.

**Terminal states are terminal.** FAILED, BLOCKED, ROLLED_BACK and CANCELLED go
nowhere. You do not resurrect a failed deployment; you create a new one. Enforced
in `DeploymentService`, not just documented.

The machine lives in `DeploymentStatus.allowedNext()` and is unit tested directly,
because every later phase calls it.

## Deployment events

One row per state change: from, to, reason, actor, timestamp. This is what renders
the timeline, and in Phase 11 it answers "why did this roll back at 3am". Without
it you only ever see current status and lose the history.

`allowedTransitions` is returned on the detail endpoint so the UI renders action
buttons from the backend's state machine instead of reimplementing it in
TypeScript. One source of truth.

## Correlation ID

Every deployment gets one at creation. Phase 7 passes it to the ML service, Phase 9
attaches it to Kubernetes events, Phase 10 to monitoring alerts. Retrofitting this
later would mean backfilling every table that references a deployment.

## Idempotent status updates

Setting a deployment to the status it already has returns 200 and records no event.
A pipeline retrying a webhook callback must not produce an error or a duplicate
timeline entry.

## API

| Method | Path | Notes |
|---|---|---|
| GET | `/api/v1/deployments` | Paged. `?projectId=&status=` |
| GET | `/api/v1/deployments/{id}` | Deployment, events, allowed transitions |
| POST | `/api/v1/projects/{projectId}/deployments` | Nested: needs a parent |
| PATCH | `/api/v1/deployments/{id}/status` | 409 on an illegal transition |

Creation is nested because a deployment cannot exist without a project. Everything
else is flat, because after creation you look it up by its own id.

`ON DELETE CASCADE` on both foreign keys: deleting a project removes its
deployments and their events. There is no orphan deployment worth keeping.
