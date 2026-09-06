# scripts

Developer convenience scripts. Kept thin — anything that CI depends on belongs in a
workflow, not here.

| Script | Phase |
|---|---|
| `dev-up.ps1` — start Postgres, backend, ML service, frontend | 1 |
| `kind-up.ps1` — create the local cluster and load images | 9 |
| `smoke-test.ps1` — hit every health endpoint and report | 9 |
