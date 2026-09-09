# Upgrading your working copy to Phase 1

Extract this archive **over** `C:\Users\ASUS\Projects\cloudforge`.

It contains no `.git`, no `.env`, no `node_modules`, no `.venv`, no `target`, so
your repository history and local secrets are untouched.

```powershell
cd C:\Users\ASUS\Projects\cloudforge
Expand-Archive -Path "$HOME\Downloads\cloudforge-phase1.zip" -DestinationPath . -Force
```

## What changed since your last working state

| File | Change |
|---|---|
| `docker-compose.yml` | Postgres 18 mount corrected to `/var/lib/postgresql` |
| `.env.example` | `POSTGRES_PORT=5433` |
| `backend/.../application.yml` | JDBC URL default now 5433; no explicit dialect |
| `backend/.../project/*` | Entity, enums, repository, service, controller, DTOs |
| `backend/.../common/*` | `PageResponse`, exceptions, `GlobalExceptionHandler` |
| `backend/.../db/migration/V1__create_projects.sql` | Projects table |
| `frontend/package.json` | Adds react-router-dom 7, TanStack Query 5 |
| `frontend/src/*` | API client, query hooks, list page, detail page, styles |
| `docs/phase-1-notes.md` | Design decisions for this phase |

Your `.env` already has `POSTGRES_PORT=5433`, so nothing to change there.

## Run

```powershell
docker compose up -d
docker compose ps
```

Terminal 1:

```powershell
cd backend
.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

Watch for `Found 1 JPA repository interfaces` and Flyway applying V1.

Terminal 2:

```powershell
cd ml-service
.\.venv\Scripts\Activate.ps1
uvicorn app.main:app --reload --port 8000
```

Terminal 3:

```powershell
cd frontend
npm install
npm run dev
```

`npm install` is required — two new dependencies.

## Test

Open http://localhost:5173. You should get the projects page with an empty state.

1. Fill in name `payments-api` and repository URL
   `https://github.com/acme/payments-api`, click **Create project**. The row
   appears in the table.
2. Click the project name for the detail page.
3. Submit the form with an empty name and a repository URL of `notaurl`. Inline
   errors appear under both fields, sent by the backend, not by the browser.
4. Create `payments-api` a second time. A 409 message appears above the table.
5. Confirm the row is real:

```powershell
docker exec cloudforge-postgres psql -U cloudforge -d cloudforge -c "SELECT name, cloud_provider, environment, created_at FROM projects;"
```

Step 3 is the one worth doing carefully: it proves validation, Problem Details
serialization, and the frontend's field-level error mapping all line up.

## Verification status

The frontend is compile-verified: `tsc -b && vite build` and `oxlint` both pass
clean. The backend is **not** — no Maven Central access in the environment where
this was built. If javac complains, send the error.
