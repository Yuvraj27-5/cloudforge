package com.cloudforge.backend.risk;

import com.cloudforge.backend.deployment.Deployment;
import com.cloudforge.backend.deployment.DeploymentMetrics;
import com.cloudforge.backend.project.CloudProvider;
import com.cloudforge.backend.project.Environment;
import com.cloudforge.backend.project.Project;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Policy tests. These encode decisions that are deliberate and arguable, so they
 * are written to fail loudly if someone changes the policy without meaning to.
 */
class DecisionEngineTest {

    private final DecisionEngine engine = new DecisionEngine(30, 70);

    private RiskScore scoreOf(int score, RiskLevel level) {
        return new RiskScore(score, level, 0.9, List.of(), "test", "synthetic");
    }

    private DeploymentMetrics metricsWith(Integer critical) {
        Project project = new Project("p", "https://github.com/a/b", "main",
                CloudProvider.AWS, Environment.PRODUCTION, null);
        DeploymentMetrics metrics = new DeploymentMetrics(new Deployment(project, "abc1234", null, "ci"));
        metrics.record(5, 100, 20, null, null, null, null, null, null,
                critical, 0, 0, 0, "test");
        return metrics;
    }

    @Test
    void lowScoreApproves() {
        assertThat(engine.decide(scoreOf(12, RiskLevel.LOW), metricsWith(0)))
                .isEqualTo(DeploymentDecision.APPROVE_DEPLOYMENT);
    }

    @Test
    void mediumScoreRequiresValidation() {
        assertThat(engine.decide(scoreOf(55, RiskLevel.MEDIUM), metricsWith(0)))
                .isEqualTo(DeploymentDecision.EXTRA_VALIDATION);
    }

    @Test
    void highScoreBlocks() {
        assertThat(engine.decide(scoreOf(88, RiskLevel.HIGH), metricsWith(0)))
                .isEqualTo(DeploymentDecision.BLOCK_DEPLOYMENT);
    }

    @Test
    void bandBoundariesAreInclusiveAtTheLowerLevel() {
        assertThat(engine.decide(scoreOf(30, RiskLevel.LOW), metricsWith(0)))
                .isEqualTo(DeploymentDecision.APPROVE_DEPLOYMENT);
        assertThat(engine.decide(scoreOf(31, RiskLevel.MEDIUM), metricsWith(0)))
                .isEqualTo(DeploymentDecision.EXTRA_VALIDATION);
        assertThat(engine.decide(scoreOf(70, RiskLevel.MEDIUM), metricsWith(0)))
                .isEqualTo(DeploymentDecision.EXTRA_VALIDATION);
        assertThat(engine.decide(scoreOf(71, RiskLevel.HIGH), metricsWith(0)))
                .isEqualTo(DeploymentDecision.BLOCK_DEPLOYMENT);
    }

    @Test
    void aCriticalVulnerabilityBlocksEvenWithAPerfectScore() {
        // The hard gate. Shipping a known critical CVE is not a probability
        // question, and this model is not trained to answer it.
        assertThat(engine.decide(scoreOf(0, RiskLevel.LOW), metricsWith(1)))
                .isEqualTo(DeploymentDecision.BLOCK_DEPLOYMENT);
    }

    @Test
    void unmeasuredVulnerabilitiesDoNotTriggerTheHardGate() {
        // Null means no scan ran. That is handled upstream by refusing to score at
        // all, not by pretending the gate fired.
        assertThat(engine.decide(scoreOf(10, RiskLevel.LOW), metricsWith(null)))
                .isEqualTo(DeploymentDecision.APPROVE_DEPLOYMENT);
    }

    @Test
    void anUnavailableProviderFailsClosed() {
        // Inconvenient by design. The alternative is a platform that stops checking
        // and keeps saying yes.
        assertThat(engine.decideWithoutScore()).isEqualTo(DeploymentDecision.BLOCK_DEPLOYMENT);
    }

    @Test
    void approvalIsTheOnlyDecisionThatAllowsDeployment() {
        assertThat(DeploymentDecision.APPROVE_DEPLOYMENT.allowsDeployment()).isTrue();
        assertThat(DeploymentDecision.EXTRA_VALIDATION.allowsDeployment()).isFalse();
        assertThat(DeploymentDecision.BLOCK_DEPLOYMENT.allowsDeployment()).isFalse();
    }
}
