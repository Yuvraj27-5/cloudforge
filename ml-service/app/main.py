import logging
from datetime import datetime, timezone

from fastapi import FastAPI
from pydantic import BaseModel

from app.config import settings

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)s [%(name)s] %(message)s",
)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="CloudForge ML Risk Service",
    version=settings.version,
    description="Deployment risk prediction. No model is loaded yet (arrives in Phase 7).",
)


class HealthResponse(BaseModel):
    status: str
    service: str
    version: str
    model_loaded: bool
    timestamp: datetime


@app.get("/health", response_model=HealthResponse, tags=["system"])
def health() -> HealthResponse:
    """Liveness probe.

    `model_loaded` is intentionally reported separately from `status`: once a
    real model exists, the backend must be able to tell "service reachable"
    apart from "service able to score". A silently model-less risk engine that
    reports UP would approve every deployment.
    """
    return HealthResponse(
        status="UP",
        service=settings.service_name,
        version=settings.version,
        model_loaded=False,
        timestamp=datetime.now(timezone.utc),
    )
