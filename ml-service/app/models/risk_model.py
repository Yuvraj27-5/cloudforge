"""Model loading, prediction and per-prediction explanation."""

from __future__ import annotations

import logging
from dataclasses import dataclass
from pathlib import Path

import joblib
import numpy as np

from app.schemas.risk import (
    FEATURE_ORDER,
    Decision,
    DeploymentFeatures,
    RiskFactor,
    RiskLevel,
    RiskResponse,
)

logger = logging.getLogger(__name__)

MODEL_PATH = Path(__file__).parent / "risk_model.joblib"

# Thresholds live here, not in the model. The model outputs a probability; turning
# that into LOW/MEDIUM/HIGH is a policy decision that must be changeable without
# retraining. The backend owns the final deployment decision (Phase 8) — these
# values are the service's recommendation.
LOW_MAX = 30
MEDIUM_MAX = 70

READABLE = {
    "files_changed": "files changed",
    "lines_added": "lines added",
    "lines_deleted": "lines deleted",
    "test_pass_rate": "test pass rate",
    "test_coverage": "test coverage",
    "code_complexity": "code complexity",
    "critical_vulnerabilities": "critical vulnerabilities",
    "high_vulnerabilities": "high vulnerabilities",
    "previous_deployment_failures": "previous deployment failures",
}


@dataclass(frozen=True)
class LoadedModel:
    estimator: object
    baseline: np.ndarray
    version: str
    trained_on: str


_model: LoadedModel | None = None


def load_model() -> LoadedModel | None:
    """Loads once at startup. Returns None when no model file exists.

    A missing model is reported honestly through /health rather than being papered
    over with a default score. A risk engine that silently returns 0 would approve
    everything.
    """
    global _model

    if _model is not None:
        return _model

    if not MODEL_PATH.exists():
        logger.warning("No model at %s. Run: python -m app.models.train", MODEL_PATH)
        return None

    bundle = joblib.load(MODEL_PATH)

    if tuple(bundle["feature_order"]) != FEATURE_ORDER:
        # Feature order drift silently scores the wrong numbers against the wrong
        # features, producing plausible nonsense.
        raise RuntimeError(
            "Model feature order does not match the current schema. Retrain the model."
        )

    _model = LoadedModel(
        estimator=bundle["estimator"],
        baseline=np.asarray(bundle["baseline"], dtype=float),
        version=bundle["version"],
        trained_on=bundle["trained_on"],
    )
    logger.info("Loaded model version=%s trained_on=%s", _model.version, _model.trained_on)
    return _model


def is_loaded() -> bool:
    return _model is not None


def _level_and_decision(score: int) -> tuple[RiskLevel, Decision]:
    if score <= LOW_MAX:
        return RiskLevel.LOW, Decision.APPROVE_DEPLOYMENT
    if score <= MEDIUM_MAX:
        return RiskLevel.MEDIUM, Decision.EXTRA_VALIDATION
    return RiskLevel.HIGH, Decision.BLOCK_DEPLOYMENT


def _explain(model: LoadedModel, row: np.ndarray, score: int) -> list[RiskFactor]:
    """Per-prediction contributions by occlusion.

    Each feature is replaced with its training-set median and the model re-run; the
    change in predicted risk is that feature's contribution for this deployment.

    Chosen over SHAP deliberately. SHAP gives theoretically better attributions but
    adds a heavy dependency, and occlusion is exact for the question actually being
    asked here: how much of this score is attributable to this value being what it
    is rather than typical.
    """
    factors: list[RiskFactor] = []

    for index, name in enumerate(FEATURE_ORDER):
        occluded = row.copy()
        occluded[index] = model.baseline[index]

        without = model.estimator.predict_proba(occluded.reshape(1, -1))[0][1] * 100
        contribution = round(score - without, 1)

        if abs(contribution) < 0.5:
            continue

        value = float(row[index])
        label = READABLE[name]
        direction = "raises" if contribution > 0 else "lowers"

        factors.append(
            RiskFactor(
                feature=name,
                value=value,
                contribution=contribution,
                explanation=(
                    f"{label} of {value:g} {direction} risk by "
                    f"{abs(contribution):.1f} points versus a typical deployment"
                ),
            )
        )

    factors.sort(key=lambda factor: abs(factor.contribution), reverse=True)
    return factors[:5]


def predict(features: DeploymentFeatures) -> RiskResponse:
    model = load_model()
    if model is None:
        raise RuntimeError("No model loaded")

    row = np.asarray(features.as_row(), dtype=float)
    probability = float(model.estimator.predict_proba(row.reshape(1, -1))[0][1])
    score = int(round(probability * 100))

    baseline_score = int(round(
        model.estimator.predict_proba(model.baseline.reshape(1, -1))[0][1] * 100
    ))

    level, decision = _level_and_decision(score)

    # Confidence is how far the model is from an even split, not how correct it is.
    # A model trained on synthetic data can be highly confident and entirely wrong.
    confidence = round(abs(probability - 0.5) * 2, 3)

    return RiskResponse(
        risk_score=score,
        risk_level=level,
        decision=decision,
        confidence=confidence,
        factors=_explain(model, row, score),
        baseline_score=baseline_score,
        model_version=model.version,
        trained_on=model.trained_on,
    )
