# Getting Started (Windows 11 + VS Code)

## 1. Open the project

Extract the archive, then:

```powershell
cd $HOME\Projects\cloudforge
code .
```

Everything below runs in VS Code's integrated terminal (`` Ctrl+` ``).

Recommended extensions: **Extension Pack for Java**, **Spring Boot Extension
Pack**, **Python**, **Docker**. The frontend uses oxlint, which needs no extension.

## 2. Initialise Git

The archive contains no `.git` folder.

```powershell
git init -b main
git add .
git status
```

Confirm `.env`, `node_modules/`, `backend/target/`, and `ml-service/.venv/` are
**not** listed, then:

```powershell
git commit -m "Phase 0: monorepo scaffold, service skeletons, local dev environment"
```

## 3. Start PostgreSQL

Docker Desktop must be running first.

```powershell
Copy-Item .env.example .env
docker compose up -d
Start-Sleep -Seconds 10
docker compose ps
```

Expect `cloudforge-postgres` as `Up (healthy)`. If port 5432 is already in use,
change `POSTGRES_PORT` in `.env` to `5433`, set `CLOUDFORGE_DB_URL` to match, and
re-run `docker compose up -d`.

## 4. Backend

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

First run downloads Maven and the dependency tree, so allow a few minutes. Leave
it running and open a new terminal.

Expected log lines: Flyway reporting zero migrations (correct for Phase 0 — it
still creates `flyway_schema_history`, which proves the connection works), then
Tomcat started on port 8080.

## 5. ML service

```powershell
cd ml-service
Copy-Item .env.example .env
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install --upgrade pip
pip install -r requirements-dev.txt
uvicorn app.main:app --reload --port 8000
```

If `Activate.ps1` is blocked, run
`Set-ExecutionPolicy -Scope CurrentUser RemoteSigned` once.

Then point VS Code at the interpreter: `Ctrl+Shift+P` -> "Python: Select
Interpreter" -> the one under `.\ml-service\.venv\`.

## 6. Frontend

```powershell
cd frontend
Copy-Item .env.example .env
npm install
npm run dev
```

## 7. Verify

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health | ConvertTo-Json -Depth 5
Invoke-RestMethod http://localhost:8080/api/v1/system/info
Invoke-RestMethod http://localhost:8000/health
docker exec -it cloudforge-postgres psql -U cloudforge -d cloudforge -c "\dt"
```

Backend health must show `"status":"UP"` **and** a `db` component that is UP. That
is the line proving the datasource and Flyway actually connected rather than the
application merely starting. The `psql` call should list `flyway_schema_history`.

Then open http://localhost:5173 and http://localhost:8000/docs.

## Phase 0 exit criteria

- [ ] Postgres healthy in Docker
- [ ] Backend on 8080 with a healthy `db` component
- [ ] `backend/` tests pass (`.\mvnw.cmd test`)
- [ ] ML service on 8000, `/docs` renders, `pytest` passes
- [ ] Frontend on 5173 reporting the backend reachable
- [ ] One clean commit with no secrets

## Troubleshooting

**Backend fails with a DataSource error.** Postgres is not up, or `.env` was
created after `docker compose up`. Run `docker compose ps`.

**`mvnw.cmd` is not recognised.** You are not in the `backend/` directory, or the
archive was extracted without the wrapper files. `.\mvnw.cmd -v` should print a
Maven version.

**Spring Boot version fails to resolve.** Regenerate the module scaffolding from
Spring Initializr and copy the sources back in:

```powershell
Invoke-WebRequest "https://start.spring.io/starter.zip?type=maven-project&javaVersion=25&groupId=com.cloudforge&artifactId=backend&packageName=com.cloudforge.backend&dependencies=web,actuator,validation,data-jpa,postgresql,flyway" -OutFile $env:TEMP\init.zip
```

**Frontend shows "Backend unreachable".** The backend is not running, or it is on
a different port than the `vite.config.ts` proxy target.
