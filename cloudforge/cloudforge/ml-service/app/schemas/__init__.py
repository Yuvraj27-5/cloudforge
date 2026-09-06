"""Pydantic request and response models.

Filled in Phase 7: the deployment feature schema (files_changed, lines_added,
test_pass_rate, coverage, complexity, vulnerability counts, prior failures) and the
risk response (risk_score, risk_level, decision, confidence, factors).

Validation lives here. A missing or out-of-range feature must fail loudly rather
than default to a low-risk score.
"""
