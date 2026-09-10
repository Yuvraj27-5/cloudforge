"""Synthetic deployment dataset generator, for development only.

READ THIS BEFORE TRUSTING ANY METRIC PRODUCED FROM IT.

The labels below come from rules written by hand. A model trained on this data
learns those rules. Its accuracy measures how well it recovered the assumptions
encoded here — it says nothing about whether those assumptions predict real
deployment failures.

The generator exists so the whole pipeline (schema, training, serving, explaining,
the backend client, the UI) can be built and tested before any real deployment
history exists. When Phase 8 starts recording actual outcomes, this file is
replaced by a query, not tuned.
"""

from __future__ import annotations

import numpy as np

from app.schemas.risk import FEATURE_ORDER

# Kept explicit so the encoded beliefs are auditable rather than buried in code.
ASSUMPTIONS = {
    "large_changes_are_riskier": "Failure probability rises with files changed and lines added",
    "coverage_protects": "Higher test coverage lowers failure probability",
    "failing_tests_dominate": "A pass rate below 100% is a strong signal",
    "critical_cves_dominate": "Any critical vulnerability sharply raises risk",
    "complexity_compounds": "High cyclomatic complexity raises risk, more so on large changes",
    "history_repeats": "Projects that failed before fail again",
}


# Solved numerically so the synthetic failure rate lands near the 12-18% seen in
# published DORA-style change-failure figures. A near-balanced dataset would train a
# model that predicts failure far too readily.
BASE_RATE_LOGIT = -4.0


def _failure_probability(row: dict[str, float]) -> float:
    """The hand-written rule the model will learn. This is the assumption, not truth."""
    logit = BASE_RATE_LOGIT

    # Change size matters, but must not outweigh a security finding: a large,
    # well-tested, clean change is not more dangerous than a small one shipping a
    # critical CVE.
    logit += 0.012 * min(row["files_changed"], 120)
    logit += 0.0004 * min(row["lines_added"], 3000)
    logit += 0.0002 * min(row["lines_deleted"], 3000)

    # Failing tests are the strongest single signal available before deploying.
    logit += 0.16 * (100.0 - row["test_pass_rate"])
    logit += 0.022 * (80.0 - row["test_coverage"])

    logit += 0.030 * row["code_complexity"]
    logit += 0.0004 * row["code_complexity"] * min(row["files_changed"], 120)

    logit += 1.60 * row["critical_vulnerabilities"]
    logit += 0.30 * row["high_vulnerabilities"]

    logit += 0.45 * row["previous_deployment_failures"]

    return 1.0 / (1.0 + np.exp(-logit))


def generate(n: int = 6000, seed: int = 42) -> tuple[np.ndarray, np.ndarray]:
    """Returns (features, labels) with labels drawn from the rule above.

    Labels are sampled, not thresholded, so the model sees genuinely noisy targets
    rather than a deterministic function it can memorise perfectly.
    """
    rng = np.random.default_rng(seed)

    files_changed = rng.gamma(shape=2.0, scale=6.0, size=n).round()
    lines_added = (files_changed * rng.gamma(2.0, 40.0, n)).round()
    lines_deleted = (lines_added * rng.beta(2.0, 5.0, n)).round()

    test_pass_rate = np.clip(100.0 - rng.exponential(1.2, n), 60.0, 100.0)
    test_coverage = np.clip(rng.normal(72.0, 14.0, n), 0.0, 100.0)
    code_complexity = np.clip(rng.gamma(3.0, 3.0, n), 1, 60).round()

    critical = rng.poisson(0.12, n)
    high = rng.poisson(1.1, n)
    previous_failures = rng.poisson(0.6, n)

    columns = {
        "files_changed": files_changed,
        "lines_added": lines_added,
        "lines_deleted": lines_deleted,
        "test_pass_rate": test_pass_rate,
        "test_coverage": test_coverage,
        "code_complexity": code_complexity,
        "critical_vulnerabilities": critical,
        "high_vulnerabilities": high,
        "previous_deployment_failures": previous_failures,
    }

    features = np.column_stack([columns[name] for name in FEATURE_ORDER])

    probabilities = np.array([
        _failure_probability({name: columns[name][i] for name in FEATURE_ORDER})
        for i in range(n)
    ])
    labels = rng.binomial(1, probabilities)

    return features, labels


if __name__ == "__main__":
    x, y = generate()
    print(f"{len(x)} rows, {y.mean() * 100:.1f}% labelled as failures")
    print("Features:", ", ".join(FEATURE_ORDER))
