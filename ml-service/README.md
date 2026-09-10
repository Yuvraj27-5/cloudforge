# ml-service

Scores deployment risk and explains the score. Stateless: it knows nothing about
projects or deployments, and it decides nothing.

## Run

```powershell
Copy-Item .env.example .env
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements-dev.txt

python -m app.models.train      # writes app/models/risk_model.joblib
uvicorn app.main:app --reload --port 8000
pytest
```

The model file is gitignored. Train it once after cloning.

## Endpoints

| Path | Purpose |
|---|---|
| `GET /health` | Liveness, plus `model_loaded` and which dataset trained it |
| `POST /predict-risk` | Score and explanation |
| `GET /docs` | OpenAPI UI |

## Read this before quoting any accuracy number

The model is trained on **synthetic data generated from rules written by hand** in
`app/data/generate.py`. It has learned those rules. Its ROC AUC measures how well it
recovered assumptions someone typed in — it is not evidence that the model predicts
real deployment failures.

Every response carries `trained_on: "synthetic"` for exactly this reason. When real
deployment outcomes exist, the generator is replaced by a query and that field
changes.

The encoded assumptions are listed explicitly in `ASSUMPTIONS` so they can be
argued with rather than being buried in coefficients.

## Explanation

Contributions are computed by **occlusion**: each feature is replaced with its
training-set median and the model re-run. The change in predicted risk is that
feature's contribution for this deployment.

Two honest limitations:

- Contributions **do not sum to the score**, because features interact and occlusion
  measures each one alone. `baseline_score` is returned so the numbers are
  interpretable relative to a typical deployment.
- A contribution can point the counterintuitive way — "1 previous failure lowers
  risk" — when a value sits near the median and interacts with others. That is the
  model being honest about what it learned, not a display bug.

SHAP would give better attributions and adds a heavy dependency. Occlusion is exact
for the question actually asked: how much of this score comes from this value being
what it is rather than typical.

## Thresholds

| Score | Level | Recommendation |
|---|---|---|
| 0–30 | LOW | APPROVE_DEPLOYMENT |
| 31–70 | MEDIUM | EXTRA_VALIDATION |
| 71–100 | HIGH | BLOCK_DEPLOYMENT |

These live in `risk_model.py`, not in the model, so policy changes without
retraining. The service **recommends**; the backend decides (Phase 8).

## Failure behaviour

No model loaded returns **503**, never a default score. An unavailable risk engine
must stop the pipeline, not quietly approve the deployment. `/health` reports
`model_loaded` separately from `status` so a caller cannot mistake reachable for
able to score.

Validation is strict: missing, out-of-range, or unknown fields are rejected with
422. Substituting a default would score a deployment that was never measured.
