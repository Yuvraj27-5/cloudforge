package com.cloudforge.backend.risk;

import com.cloudforge.backend.deployment.DeploymentMetrics;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Turns a risk score into a deployment decision.
 *
 * <p>Deliberately independent of the model. Thresholds are configuration, hard gates
 * are code, and neither requires retraining to change.
 */
@Component
public class DecisionEngine {

    private final int lowMax;
    private final int mediumMax;

    public DecisionEngine(
            @Value("${cloudforge.risk.low-max:30}") int lowMax,
            @Value("${cloudforge.risk.medium-max:70}") int mediumMax) {
        this.lowMax = lowMax;
        this.mediumMax = mediumMax;
    }

    /**
     * Hard gate, evaluated before the score.
     *
     * <p>A critical vulnerability blocks regardless of how the model scored the
     * change. Shipping a known critical CVE is not a probability question, and the
     * model is not trained to answer it — it predicts deployment failure, which is a
     * different risk entirely.
     */
    public DeploymentDecision decide(RiskScore risk, DeploymentMetrics metrics) {
        if (metrics != null && metrics.hasBlockingVulnerabilities()) {
            return DeploymentDecision.BLOCK_DEPLOYMENT;
        }

        if (risk.score() <= lowMax) {
            return DeploymentDecision.APPROVE_DEPLOYMENT;
        }
        if (risk.score() <= mediumMax) {
            return DeploymentDecision.EXTRA_VALIDATION;
        }
        return DeploymentDecision.BLOCK_DEPLOYMENT;
    }

    /**
     * Applied when no score could be obtained.
     *
     * <p>Fail closed. An unreachable risk engine means the deployment has not been
     * assessed, and an unassessed deployment is not an approved one. This will be
     * inconvenient every time the ML service is not running, which is the correct
     * trade: the alternative is a platform that silently stops checking anything and
     * keeps saying yes.
     */
    public DeploymentDecision decideWithoutScore() {
        return DeploymentDecision.BLOCK_DEPLOYMENT;
    }

    public String explainBlockedByVulnerabilities(DeploymentMetrics metrics) {
        return "Blocked by hard gate: %d critical vulnerabilities. Security findings are not scored."
                .formatted(metrics.getCriticalVulnerabilities());
    }
}
