"""Behavioural tests for the risk endpoint.

Deliberately no assertions on model accuracy. The model is trained on synthetic
data, so accuracy measures how well it recovered hand-written rules. Asserting on
it would enshrine those assumptions as requirements.
"""

from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)

CLEAN = {
    "files_changed": 3,
    "lines_added": 40,
    "lines_deleted": 8,
    "test_pass_rate": 100,
    "test_coverage": 88,
    "code_complexity": 4,
    "critical_vulnerabilities": 0,
    "high_vulnerabilities": 0,
    "previous_deployment_failures": 0,
}

RISKY = {
    "files_changed": 95,
    "lines_added": 2800,
    "lines_deleted": 600,
    "test_pass_rate": 91,
    "test_coverage": 42,
    "code_complexity": 38,
    "critical_vulnerabilities": 2,
    "high_vulnerabilities": 6,
    "previous_deployment_failures": 3,
}


def test_health_reports_whether_scoring_is_possible():
    body = client.get("/health").json()

    assert body["status"] == "UP"
    # Separate from status on purpose: reachable is not the same as able to score.
    assert body["model_loaded"] is True
    assert body["trained_on"] == "synthetic"


def test_a_clean_change_scores_lower_than_a_risky_one():
    clean = client.post("/predict-risk", json=CLEAN).json()
    risky = client.post("/predict-risk", json=RISKY).json()

    # Ordering, not absolute values. The absolute numbers reflect synthetic rules.
    assert clean["risk_score"] < risky["risk_score"]


def test_score_maps_to_the_documented_bands():
    body = client.post("/predict-risk", json=CLEAN).json()
    score, level, decision = body["risk_score"], body["risk_level"], body["decision"]

    if score <= 30:
        assert level == "LOW" and decision == "APPROVE_DEPLOYMENT"
    elif score <= 70:
        assert level == "MEDIUM" and decision == "EXTRA_VALIDATION"
    else:
        assert level == "HIGH" and decision == "BLOCK_DEPLOYMENT"


def test_every_response_says_what_it_was_trained_on():
    body = client.post("/predict-risk", json=CLEAN).json()

    # A caller must never be able to mistake a synthetic score for a validated one.
    assert body["trained_on"] == "synthetic"
    assert body["model_version"]


def test_explanation_is_returned_and_ordered_by_impact():
    body = client.post("/predict-risk", json=RISKY).json()
    factors = body["factors"]

    assert factors, "a score with no explanation is not actionable"
    magnitudes = [abs(f["contribution"]) for f in factors]
    assert magnitudes == sorted(magnitudes, reverse=True)
    assert all(f["explanation"] for f in factors)


def test_baseline_score_is_reported_so_contributions_are_interpretable():
    body = client.post("/predict-risk", json=RISKY).json()

    assert 0 <= body["baseline_score"] <= 100


def test_missing_feature_is_rejected():
    payload = {k: v for k, v in CLEAN.items() if k != "test_coverage"}

    # 422, not a default. Substituting a value would score a deployment that was
    # never measured.
    assert client.post("/predict-risk", json=payload).status_code == 422


def test_out_of_range_feature_is_rejected():
    assert client.post("/predict-risk", json={**CLEAN, "test_coverage": 150}).status_code == 422
    assert client.post("/predict-risk", json={**CLEAN, "files_changed": -1}).status_code == 422


def test_unknown_feature_is_rejected():
    # extra="forbid": a typo'd field name must not be silently dropped.
    assert client.post("/predict-risk", json={**CLEAN, "lines_moved": 5}).status_code == 422


def test_correlation_id_is_echoed():
    response = client.post(
        "/predict-risk", json=CLEAN, headers={"x-correlation-id": "cf-abc-123"}
    )

    assert response.headers["x-correlation-id"] == "cf-abc-123"
