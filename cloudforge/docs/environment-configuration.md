# Environment and Secret Configuration

## Rules

1. No secret is ever committed. `.env` is gitignored; `.env.example` documents the keys.
2. Configuration is read from environment variables, with safe local defaults only.
3. Local defaults are non-production values by construction.

## Per service

| Service | Mechanism | Keys |
|---|---|---|
| Backend | `application.yml` placeholders `${VAR:default}` | `CLOUDFORGE_DB_URL`, `CLOUDFORGE_DB_USER`, `CLOUDFORGE_DB_PASSWORD` |
| ML service | pydantic-settings, prefix `ML_`, optional local `.env` | `ML_HOST`, `ML_PORT`, `ML_LOG_LEVEL` |
| Frontend | Vite, prefix `VITE_` | `VITE_API_BASE_URL` |
| Docker Compose | root `.env` | `POSTGRES_*` |

**`VITE_` variables are compiled into the JavaScript bundle and are public.**
Anything secret behind a `VITE_` name is published to every visitor.

## Per environment

| Environment | Source of truth |
|---|---|
| Local | `.env` files, gitignored |
| CI | GitHub Actions Secrets |
| AWS | IAM roles for service accounts + Secrets Manager. No static access keys. |
| Azure | Managed identity + Key Vault. No static credentials. |

## Logging

Never log credentials, tokens, connection strings, or full request bodies that may
carry them. Before adding a logging statement to a code path that touches
configuration, check what the object's `toString()` actually prints.
