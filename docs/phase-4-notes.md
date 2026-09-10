# Phase 4 — Continuous Integration

Four jobs on every push and pull request to `main`.

| Job | Checks |
|---|---|
| `backend-tests` | `./mvnw test` — the unit suite |
| `backend-smoke` | The application actually starts against real Postgres |
| `sample-app-tests` | `npm test` — Node's built-in runner |
| `frontend-build` | `oxlint`, then `tsc -b && vite build` |

## Why a smoke job exists

Every unit test in this project passes with mocks, and they all passed during two
separate incidents where the application could not start at all: Flyway silently
not running under Spring Boot 4, and a datasource pointed at the wrong port.

`backend-smoke` boots the packaged jar against a Postgres service container and
asserts three things:

1. `/actuator/health` responds within 45 seconds
2. `.components.db.status == "UP"` — checked with `jq`, not a grep. Overall status
   being UP is not enough: if the datasource were missing entirely, the endpoint
   would still report UP with no `db` indicator at all
3. `flyway_schema_history`, `projects`, `deployments` and `deployment_events` all
   exist in the database

Point 3 is the one that would have caught the Flyway bug in 90 seconds.

## Action versions

`checkout@v7`, `setup-java@v6`, `setup-node@v7`. GitHub removes Node 20 from
runners on 16 September 2026, so older action majors stop working. `setup-java`
v1 through v4 are already deprecated.

## Details worth keeping

**`chmod +x ./mvnw`.** Git on Windows does not reliably preserve the executable
bit, so the wrapper is made executable in the job rather than assumed. The
permanent fix is `git update-index --chmod=+x backend/mvnw` committed once.

**`package-manager-cache: false` for the sample app.** It has no lockfile because
it has no dependencies. Caching would fail looking for one, and `npm ci` would fail
outright — so the job runs `npm test` directly.

**Jar name is globbed, not hard-coded.** `ls target/*.jar` survives a version bump
in the pom.

**`concurrency` with `cancel-in-progress`.** A new push supersedes the run it
replaced instead of queueing behind it.

**`permissions: contents: read`.** Nothing in this workflow writes to the
repository. Least privilege from the first workflow rather than retrofitted when
deployment credentials arrive in Phase 12.

**The Postgres password is `ci_only_not_a_secret`,** in plain text, deliberately. It
belongs to a container that exists for 90 seconds and is unreachable from outside
the runner. Putting it in GitHub Secrets would imply it protects something.

## Not here yet

Docker build and image tagging (Phase 5), Trivy and static analysis (Phase 6),
feature extraction and the risk engine call (Phase 7), the deployment decision
(Phase 8), and deploy plus rollback (Phases 9 and 11). Each phase adds its stage to
this file.
