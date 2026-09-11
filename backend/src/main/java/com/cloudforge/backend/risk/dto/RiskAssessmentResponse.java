package com.cloudforge.backend.risk.dto;

import com.cloudforge.backend.risk.DeploymentDecision;
import com.cloudforge.backend.risk.RiskAssessment;
import com.cloudforge.backend.risk.RiskLevel;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RiskAssessmentResponse(
        UUID id,
        Integer riskScore,
        RiskLevel riskLevel,
        DeploymentDecision decision,
        BigDecimal confidence,
        List<FactorResponse> factors,
        String modelVersion,
        /**
         * "synthetic" means the score reflects encoded assumptions, not observed
         * production outcomes. Surfaced all the way to the UI on purpose.
         */
        String trainedOn,
        String provider,
        String overrideReason,
        boolean allowsDeployment,
        Instant assessedAt
) {
    public record FactorResponse(String feature, double value, double contribution, String explanation) {
    }

    public static RiskAssessmentResponse from(RiskAssessment assessment, List<FactorResponse> factors) {
        return new RiskAssessmentResponse(
                assessment.getId(),
                assessment.getRiskScore(),
                assessment.getRiskLevel(),
                assessment.getDecision(),
                assessment.getConfidence(),
                factors,
                assessment.getModelVersion(),
                assessment.getTrainedOn(),
                assessment.getProvider(),
                assessment.getOverrideReason(),
                assessment.getDecision().allowsDeployment(),
                assessment.getAssessedAt()
        );
    }
}
