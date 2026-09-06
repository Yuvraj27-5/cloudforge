# API Conventions

Decided in Phase 0, enforced from Phase 1 onward. Changing these later is expensive.

## Base path and versioning

All endpoints live under `/api/v1`. The version is in the path, not a header.

## Resource naming

Plural nouns, kebab-case:

```
GET    /api/v1/projects
POST   /api/v1/projects
GET    /api/v1/projects/{id}
GET    /api/v1/deployments/{id}/events
```

Verbs only for genuine actions that are not CRUD:

```
POST   /api/v1/deployments/{id}/rollback
```

## Status codes

| Code | Use |
|------|-----|
| 200 | Successful read or update |
| 201 | Resource created (include `Location` header) |
| 204 | Successful delete |
| 400 | Malformed request or failed bean validation |
| 404 | Resource does not exist |
| 409 | Conflict with current state (e.g. deleting a project mid-deployment) |
| 422 | Well-formed but semantically invalid |
| 502 | Downstream dependency failed (e.g. ML service unreachable) |

`502` matters for CloudForge specifically: if the risk engine is down, that is not
a client error and must never be silently treated as "low risk".

## Errors: RFC 9457 Problem Details

Spring's `ProblemDetail` is the wire format. Stack traces are never returned.

```json
{
  "type": "https://cloudforge.dev/errors/validation",
  "title": "Validation failed",
  "status": 400,
  "detail": "Request contains invalid fields",
  "instance": "/api/v1/projects",
  "correlationId": "b3f1c2d4-...",
  "errors": [
    { "field": "repositoryUrl", "message": "must be a valid URL" }
  ]
}
```

## Pagination

`?page=0&size=20&sort=createdAt,desc`. Responses carry `content`, `page`, `size`,
`totalElements`, `totalPages`. Collections are never returned unbounded.

## Correlation IDs

Every request carries `X-Correlation-Id`. The backend generates one when absent,
places it in the SLF4J MDC, propagates it to the ML service, and stores it on the
deployment record.

One deployment must be traceable across backend logs, ML service logs, Kubernetes
events, and monitoring alerts using a single ID. Retrofitting this after Phase 10
is painful, so the log pattern already reserves the field.

## Timestamps

UTC, ISO-8601 with offset. Every persisted entity carries `createdAt` and `updatedAt`.

## DTOs

Controllers accept and return DTOs (Java records). JPA entities never cross the web
boundary. That coupling turns every schema change into a breaking API change and
leaks lazy-loading behaviour into serialization.
