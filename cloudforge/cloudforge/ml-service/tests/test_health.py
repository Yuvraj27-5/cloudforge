from fastapi.testclient import TestClient

from app.main import app

client = TestClient(app)


def test_health_reports_up_and_no_model():
    response = client.get("/health")
    assert response.status_code == 200

    body = response.json()
    assert body["status"] == "UP"
    assert body["service"] == "cloudforge-ml-service"
    assert body["model_loaded"] is False
