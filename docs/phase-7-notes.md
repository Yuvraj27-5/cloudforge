# Phase 7 — ML Risk Engine

FastAPI service that scores a deployment and explains the score. Stateless, and it
decides nothing — the backend owns the decision (Phase 8).

## The synthetic data problem, stated plainly

The model is trained on data generated from rules written by hand in
`app/data/generate.py`. It learns those rules. Reported metrics — ROC AUC 0.760,
Brier 0.111 — measure how well gradient boosting recovered hand-typed assumptions.
They are a check that the training pipeline works. They are not evidence that
CloudForge predicts real deployment failures.

Three things protect against that number being misread:

1. `trained_on: "synthetic"` is on every single response
2. `/health` reports it too, so an operator sees it without scoring anything
3. `ASSUMPTIONS` lists the encoded beliefs in plain English, so they can be argued
   with instead of being buried in coefficients

When real deployment outcomes exist, `generate.py` is replaced by a query and that
field changes. Nothing else has to.

## Calibrating the generator

The first version produced a 37% failure rate, against a real-world change-failure
rate closer to 15%. A near-balanced dataset trains a model that predicts failure far
too readily.

It also had `lines_added` outweighing every other signal, so a large clean change
scored higher than a small one shipping a critical CVE. Rebalancing brought the rate
to 15.8%.

Worth noting what was **not** changed: even after rebalancing, one critical CVE
(8.7%) scores below a large clean change (14.5%). The instinct is to crank the
coefficient. That would be wrong. This model predicts *deployment failure*, and a
CVE is a security concern rather than a failure predictor. Phase 6 already gave it a
separate hard gate via `hasBlockingVulnerabilities`. Two different risks, two
different mechanisms — forcing one model to express both would corrupt the thing it
is actually good at.

## Model choice

Gradient boosting, per the original spec. The features are tabular and few, the
relationships mostly monotonic, and the attribution has to be explainable to
whoever is being told their deployment is blocked. A neural network would be worse
at all three.

Brier score is reported alongside accuracy because the output is a probability shown
to a human. Calibration matters more than whether the argmax is right.

## Explanation by occlusion

Each feature is replaced with its training-set median and the model re-run; the
change in predicted risk is that feature's contribution.

Two limitations, documented rather than hidden:

- Contributions **do not sum to the score**. Features interact; occlusion measures
  each alone. `baseline_score` is returned so contributions are interpretable
  relative to a typical deployment.
- A contribution can point counterintuitively when a value sits near the median.
  That is the model being honest about what it learned.

SHAP would attribute better and costs a heavy dependency. Occlusion answers the
question actually being asked: how much of this score comes from this value being
what it is rather than typical.

## Failure behaviour

**No model loaded returns 503, never a default score.** This is the single most
important line in the service. A risk engine that returns 0 when broken approves
every deployment, silently, and looks healthy while doing it.

`/health` reports `model_loaded` separately from `status` for the same reason:
reachable is not the same as able to score.

Validation is strict — missing, out-of-range and unknown fields all return 422.
`extra="forbid"` means a typo'd field name fails rather than being dropped, which
would otherwise score a deployment on eight features while claiming nine.

## Thresholds are policy, not model

0–30 LOW, 31–70 MEDIUM, 71–100 HIGH live in `risk_model.py`. Changing where the
line sits must not require retraining. The service recommends; Phase 8's backend
decides.

## Verified

Ten tests pass. Generator calibration checked empirically across intercepts.
Predictions and explanations exercised against three scenarios. Tests deliberately
assert on **ordering** — a clean change scores below a risky one — never on absolute
values, which would enshrine synthetic assumptions as requirements.

## Not done here

The backend does not yet call this service. That wiring lands with Phase 8, where
the response has to be stored and acted on anyway.
