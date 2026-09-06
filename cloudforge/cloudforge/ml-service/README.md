# ml-service

FastAPI deployment risk engine.

```powershell
Copy-Item .env.example .env
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements-dev.txt
uvicorn app.main:app --reload --port 8000
pytest
```

`GET /health` is the only endpoint in Phase 0. `POST /predict-risk`, the feature
schema, the model, and the synthetic dataset generator arrive in Phase 7.

The service is stateless. It knows nothing about projects or deployments, and it
never decides anything — it scores and explains. The LOW/MEDIUM/HIGH to
APPROVE/EXTRA_VALIDATION/BLOCK mapping lives in the backend.
