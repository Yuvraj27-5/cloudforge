"""Request and response contracts for the risk endpoint.

Validation lives here. A feature that is out of range or missing must fail loudly:
a risk engine that quietly substitutes a default would score a deployment it never
actually measured.
"""

from enum import StrEnum
from typing import Annotated

from pydantic import BaseModel, Field


class RiskLevel(StrEnum):
    LOW = "LOW"
    MEDIUM = "MEDIUM"
    HIGH = "HIGH"


class Decision(StrEnum):
    APPROVE_DEPLOYMENT = "APPROVE_DEPLOYMENT"
    EXTRA_VALIDATION = "EXTRA_VALIDATION"
    BLOCK_DEPLOYMENT = "BLOCK_DEPLOYMENT"


Count = Annotated[int, Field(ge=0, le=100_000)]
Percent = Annotated[float, Field(ge=0.0, le=100.0)]


class DeploymentFeatures(BaseModel):
    """One deployment, as measured by the pipeline in Phase 6.

    Every field is required. The backend knows which metrics it recorded and which
    it did not; sending a guess here would be indistinguishable from a measurement.
    """

    model_config = {"extra": "forbid"}

    files_changed: Count
    lines_added: Count
    lines_deleted: Count
    test_pass_rate: Percent
    test_coverage: Percent
    code_complexity: Annotated[int, Field(ge=0, le=1000)]
    critical_vulnerabilities: Count
    high_vulnerabilities: Count
    previous_deployment_failures: Count

    def as_row(self) -> list[float]:
        """Feature order must match training. FEATURE_ORDER is the single source."""
        return [float(getattr(self, name)) for name in FEATURE_ORDER]


FEATURE_ORDER: tuple[str, ...] = (
    "files_changed",
    "lines_added",
    "lines_deleted",
    "test_pass_rate",
    "test_coverage",
    "code_complexity",
    "critical_vulnerabilities",
    "high_vulnerabilities",
    "previous_deployment_failures",
)


class RiskFactor(BaseModel):
    """One feature's contribution to this specific prediction."""

    feature: str
    value: float
    contribution: float = Field(
        description="Change in risk score attributable to this feature, in points"
    )
    explanation: str


class RiskResponse(BaseModel):
    risk_score: Annotated[int, Field(ge=0, le=100)]
    risk_level: RiskLevel
    decision: Decision
    confidence: Annotated[float, Field(ge=0.0, le=1.0)]
    factors: list[RiskFactor]
    baseline_score: Annotated[int, Field(
        ge=0, le=100,
        description="Score a deployment with entirely typical values would receive. "
                    "Factor contributions are measured against this, and do not sum "
                    "exactly to risk_score because features interact."
    )]
    model_version: str
    trained_on: str = Field(
        description="Which dataset produced the model. 'synthetic' means the score "
        "reflects encoded assumptions, not observed production outcomes."
    )
