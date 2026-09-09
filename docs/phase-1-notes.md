# Phase 1 — Project Management

Delivered as one vertical slice: migration, entity, repository, service, REST API,
error handling, React pages, tests.

## Decisions worth remembering

**UUID primary keys, not bigserial.** Deployment and project IDs will appear in
logs, URLs and CI output across three clouds. Sequential integers leak how many
projects exist and collide if data ever merges across environments.

**Enums stored as VARCHAR, mapped with `@Enumerated(EnumType.STRING)`.** The JPA
default is ordinal — integers. Reorder the enum constants later and every existing
row silently means something different. Adding a cloud provider is now a code
change, not a type migration.

**TIMESTAMPTZ, not TIMESTAMP.** The dev machine is IST, clusters will be UTC. A
deployment timeline that silently mixes them is a long debugging session.

**Hibernate never owns the schema.** `ddl-auto: validate`; Flyway applies
`V1__create_projects.sql`. Dev and production schemas cannot drift.

**No public setters on `Project`.** Built through its constructor, changed through
`update()`. A setter per field lets any caller leave the entity half-valid.

**No `repository.save()` in `ProjectService.update()`.** The entity is managed
inside the transaction, so the change flushes on commit. Calling save there works
but implies it is required.

**Our own `PageResponse`, not Spring's `Page`.** Returning `Page` publishes Spring
internals as the API contract, and that structure changes between versions.

**Catch-all exception handler.** Logs the stack trace server-side, returns a generic
message. Without it, an unhandled exception leaks class names, SQL fragments and
file paths to the caller.

## API

| Method | Path | Notes |
|---|---|---|
| GET | `/api/v1/projects` | Paged. `?page=0&size=20&sort=createdAt,desc` |
| GET | `/api/v1/projects/{id}` | 404 as Problem Details |
| POST | `/api/v1/projects` | 201 + `Location`. 400 validation, 409 duplicate name |
| PUT | `/api/v1/projects/{id}` | Full replace, so every field required |
| DELETE | `/api/v1/projects/{id}` | 204 |

`defaultBranch` is optional on create and defaults to `main`. It is required on
update, because PUT replaces the whole resource.

## Frontend

- `src/api/client.ts` — the only place `fetch` is called. Parses Problem Details
  into an `ApiError` that carries field-level messages, so the create form can show
  a backend validation error next to the field that caused it.
- `src/api/projects.ts` — TanStack Query hooks. Mutations invalidate the list query
  rather than manually patching cached state.
- Pages hold data fetching, components stay presentational.

Retries are set to 1: against a local backend, a failed request almost always means
"it is not running", and three retries only delay the error message.

## Not done in this phase

Update-from-the-UI (the API supports PUT; no edit form yet), pagination controls
(the API is paged, the UI shows the first page), and any real visual design. The
design pass comes once Phases 7 and 8 give the dashboard real content to lay out.
