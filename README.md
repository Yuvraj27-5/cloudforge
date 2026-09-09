# CloudForge

Intelligent multi-cloud CI/CD and deployment risk management platform.

CloudForge inserts a decision layer between "tests passed" and "deploy". It extracts
features from a change (diff size, coverage, complexity, vulnerability counts,
deployment history), scores deployment risk with a machine learning model, and
approves, gates, or blocks the deployment. After deploying it watches the workload
and rolls back automatically when health degrades.

> **Status: Phase 1 complete.** Project management works end to end. No risk model,
> no Kubernetes, no cloud providers, no authentication yet. See the roadmap below.

> **Local note:** Postgres is mapped to host port **5433**, not the default 5432,
> because another service already holds 5432 on the primary dev machine. Both
> `.env` (`POSTGRES_PORT`) and `application.yml` (`CLOUDFORGE_DB_URL`) reflect this.

## Architecture

| Component | Stack | Port |
|---|---|---|
| `frontend` | React 19 + TypeScript + Vite 8 | 5173 |
| `backend` | Java 25 + Spring Boot 4.1 | 8080 |
| `ml-service` | Python 3.13+ + FastAPI | 8000 |
| `postgres` | PostgreSQL 18 (Docker) | 5432 |
| `sample-app` | Deployment target (Phase 3) | — |

Calls flow one way: frontend to backend to ML service. Only the backend touches the
database. See [docs/architecture.md](docs/architecture.md).

## Prerequisites (Windows 11)

Git, JDK 25, Node.js 22.12+ or 24 LTS, Python 3.13+, Docker Desktop with the WSL2
backend. Maven is not needed — the repository ships the Maven wrapper.

## Getting started

Full walkthrough in [GETTING-STARTED.md](GETTING-STARTED.md). Short version:

```powershell
Copy-Item .env.example .env
docker compose up -d

# Terminal 1
cd backend
.\mvnw.cmd spring-boot:run

# Terminal 2
cd ml-service
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements-dev.txt
uvicorn app.main:app --reload --port 8000

# Terminal 3
cd frontend
Copy-Item .env.example .env
npm install
npm run dev
```

Open http://localhost:5173. It should report the backend as reachable.

## Verification

| Check | URL | Expected |
|---|---|---|
| Backend health | http://localhost:8080/actuator/health | `"status":"UP"` with a `db` component UP |
| Backend API | http://localhost:8080/api/v1/system/info | JSON, phase `0` |
| Projects API | http://localhost:8080/api/v1/projects | Paged project list |
| ML health | http://localhost:8000/health | `"status":"UP"`, `"model_loaded":false` |
| ML docs | http://localhost:8000/docs | Swagger UI |
| Frontend | http://localhost:5173 | Backend reachable |

## Tests

```powershell
cd backend;    .\mvnw.cmd test
cd ml-service; pytest
cd frontend;   npm run build
```

## Repository layout

```
cloudforge/
├── backend/            Spring Boot API and orchestration
├── frontend/           React dashboard
├── ml-service/         FastAPI deployment risk engine
├── sample-app/         Workload CloudForge deploys        (Phase 3)
├── infrastructure/     Kubernetes manifests, AWS, Azure   (Phases 9, 12, 13)
├── monitoring/         Prometheus and Grafana             (Phase 10)
├── scripts/            Developer convenience scripts
├── docs/               Architecture, API conventions, roadmap
├── .github/workflows/  CI pipelines                       (Phase 4)
└── docker-compose.yml  Local Postgres
```

Every directory that is empty today carries a README naming the phase that fills it
and what goes in it. See [docs/roadmap.md](docs/roadmap.md) for the full sequence
and the exit criterion for each phase.

## Security

No credentials live in this repository. `.env` is gitignored; `.env.example`
documents the required keys. Cloud credentials come from IAM roles and managed
identity, never from committed files. See
[docs/environment-configuration.md](docs/environment-configuration.md).

## Roadmap

| Phase | Deliverable |
|---|---|
| 0 | Repository and local environment ✅ |
| 1 | Project management CRUD, end to end ✅ |
| 2 | Deployment records and lifecycle |
| 3 | Sample application |
| 4 | GitHub Actions CI |
| 5 | Docker build and tagging |
| 6 | Trivy and static analysis |
| 7 | ML risk engine |
| 8 | Deployment decision engine |
| 9 | Local Kubernetes (kind) |
| 10 | Prometheus and Grafana |
| 11 | Automatic rollback |
| 12 | AWS (ECR + EKS) |
| 13 | Azure (ACR + AKS) |
