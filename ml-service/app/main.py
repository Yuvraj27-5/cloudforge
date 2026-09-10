import logging
import uuid
from contextlib import asynccontextmanager
from datetime import datetime, timezone

from fastapi import FastAPI, HTTPException, Request, Response
from pydantic import BaseModel

from app.config import settings
from app.models import risk_model
from app.schemas.risk import DeploymentFeatures, RiskResponse

logging.basicConfig(
    level=logging.INFO,
    format='{"timestamp":"%(asctime)s","level":"%(levelname)s","logger":"%(name)s","message":"%(message)s"}',
)
logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(_: FastAPI):
    # Loaded once at startup rather than per request. A failure here is visible
    # immediately through /health instead of on the first scoring call.
    risk_model.load_model()
    yield


app = FastAPI(
    title="CloudForge ML Risk Service",
    version=settings.version,
    description="Scores deployment risk and explains the score. Stateless.",
    lifespan=lifespan,
)


@app.middleware("http")
async def correlation_id(request: Request, call_next) -> Response:
    """Propagates the deployment's correlation ID from the backend into these logs."""
    incoming = request.headers.get("x-correlation-id") or str(uuid.uuid4())
    response = await call_next(request)
    response.headers["x-correlation-id"] = incoming
    return response


class HealthResponse(BaseModel):
    status: str
    service: str
    version: str
    model_loaded: bool
    model_version: str | None
    trained_on: str | None
    timestamp: datetime


@app.get("/health", response_model=HealthResponse, tags=["system"])
def health() -> HealthResponse:
    """Liveness, plus whether scoring is actually possible.

    `model_loaded` is reported separately from `status` on purpose. A reachable
    service with no model cannot score anything, and a caller that only checked
    `status` would treat silence as approval.
    """
    model = risk_model.load_model()

    return HealthResponse(
        status="UP",
        service=settings.service_name,
        version=settings.version,
        model_loaded=model is not None,
        model_version=model.version if model else None,
        trained_on=model.trained_on if model else None,
        timestamp=datetime.now(timezone.utc),
    )


@app.post("/predict-risk", response_model=RiskResponse, tags=["risk"])
def predict_risk(features: DeploymentFeatures, request: Request) -> RiskResponse:
    if not risk_model.is_loaded() and risk_model.load_model() is None:
        # 503, not a default score. An unavailable risk engine must stop the
        # pipeline, never quietly approve the deployment.
        raise HTTPException(
            status_code=503,
            detail="No model loaded. Run: python -m app.models.train",
        )

    result = risk_model.predict(features)

    logger.info(
        "Scored deployment score=%s level=%s decision=%s correlationId=%s",
        result.risk_score,
        result.risk_level,
        result.decision,
        request.headers.get("x-correlation-id", "-"),
    )

    return result
