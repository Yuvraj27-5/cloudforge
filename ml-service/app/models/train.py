"""Trains the risk model and writes it to app/models/risk_model.joblib.

    python -m app.models.train

The reported metrics describe how well the model recovered the rules in
app/data/generate.py. They are a check that training works, not evidence that the
model predicts real deployment failures.
"""

from __future__ import annotations

import json
from datetime import datetime, timezone

import joblib
import numpy as np
from sklearn.ensemble import GradientBoostingClassifier
from sklearn.metrics import brier_score_loss, classification_report, roc_auc_score
from sklearn.model_selection import train_test_split

from app.data.generate import ASSUMPTIONS, generate
from app.models.risk_model import MODEL_PATH
from app.schemas.risk import FEATURE_ORDER


def main() -> None:
    features, labels = generate()

    x_train, x_test, y_train, y_test = train_test_split(
        features, labels, test_size=0.2, random_state=42, stratify=labels
    )

    # Gradient boosting over a neural network: the features are tabular and few,
    # the relationships are mostly monotonic, and feature attribution has to be
    # explainable to whoever is being told their deployment is blocked.
    estimator = GradientBoostingClassifier(
        n_estimators=200,
        learning_rate=0.05,
        max_depth=3,
        subsample=0.9,
        random_state=42,
    )
    estimator.fit(x_train, y_train)

    probabilities = estimator.predict_proba(x_test)[:, 1]

    print(classification_report(y_test, probabilities > 0.5, digits=3))
    print(f"ROC AUC:     {roc_auc_score(y_test, probabilities):.3f}")
    # Brier score matters more than accuracy here: the risk score is a probability
    # shown to a human, so calibration is the property that counts.
    print(f"Brier score: {brier_score_loss(y_test, probabilities):.4f}  (lower is better)")

    print("\nFeature importance:")
    for name, importance in sorted(
        zip(FEATURE_ORDER, estimator.feature_importances_),
        key=lambda pair: -pair[1],
    ):
        print(f"  {name:32s} {importance:.3f}")

    bundle = {
        "estimator": estimator,
        # Medians from training data, used as the occlusion baseline at serve time.
        "baseline": np.median(x_train, axis=0),
        "feature_order": list(FEATURE_ORDER),
        "version": datetime.now(timezone.utc).strftime("%Y.%m.%d.%H%M"),
        "trained_on": "synthetic",
        "assumptions": ASSUMPTIONS,
    }

    MODEL_PATH.parent.mkdir(parents=True, exist_ok=True)
    joblib.dump(bundle, MODEL_PATH)

    print(f"\nWrote {MODEL_PATH}")
    print(f"Version {bundle['version']}, trained on {bundle['trained_on']}")
    print("\nEncoded assumptions:")
    print(json.dumps(ASSUMPTIONS, indent=2))


if __name__ == "__main__":
    main()
