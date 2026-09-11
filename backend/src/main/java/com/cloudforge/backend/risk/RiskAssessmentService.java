package com.cloudforge.backend.risk;

import com.cloudforge.backend.common.exception.ResourceNotFoundException;
import com.cloudforge.backend.deployment.Deployment;
import com.cloudforge.backend.deployment.DeploymentMetrics;
import com.cloudforge.backend.deployment.DeploymentMetricsRepository;
import com.cloudforge.backend.deployment.DeploymentRepository;
import com.cloudforge.backend.deployment.DeploymentStatus;
import com.cloudforge.backend.risk.dto.RiskAssessmentResponse;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class RiskAssessmentService {

    private static final Logger log = LoggerFactory.getLogger(RiskAssessmentService.class);

    private final RiskAssessmentProvider provider;
    private final DecisionEngine decisionEngine;
    private final RiskAssessmentRepository assessments;
    private final DeploymentRepository deployments;
    private final DeploymentMetricsRepository metrics;
    private final ObjectMapper objectMapper;

    public RiskAssessmentService(RiskAssessmentProvider provider,
                                 DecisionEngine decisionEngine,
                                 RiskAssessmentRepository assessments,
                                 DeploymentRepository deployments,
                                 DeploymentMetricsRepository metrics,
                                 ObjectMapper objectMapper) {
        this.provider = provider;
        this.decisionEngine = decisionEngine;
        this.assessments = assessments;
        this.deployments = deployments;
        this.metrics = metrics;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public RiskAssessmentResponse assess(UUID deploymentId) {
        Deployment deployment = deployments.findById(deploymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Deployment", deploymentId));

        DeploymentMetrics recorded = metrics.findByDeploymentId(deploymentId)
                .orElseThrow(() -> new MetricsMissingException(deploymentId));

        requireComplete(recorded, deploymentId);

        RiskFeatures features = toFeatures(recorded, deployment);

        RiskScore score;
        try {
            score = provider.score(features);
        } catch (RiskProviderUnavailableException ex) {
            // Fail closed. An unassessed deployment is not an approved one.
            DeploymentDecision decision = decisionEngine.decideWithoutScore();
            RiskAssessment blocked = RiskAssessment.unavailable(
                    deployment, provider.name(), decision,
                    "Risk provider unavailable: " + ex.getMessage());

            assessments.save(blocked);
            log.error("Risk provider unavailable for deployment id={}. Failing closed.", deploymentId);

            return RiskAssessmentResponse.from(blocked, List.of());
        }

        DeploymentDecision decision = decisionEngine.decide(score, recorded);

        String override = null;
        if (recorded.hasBlockingVulnerabilities()) {
            override = decisionEngine.explainBlockedByVulnerabilities(recorded);
        }

        RiskAssessment saved = assessments.save(RiskAssessment.scored(
                deployment, provider.name(), score, decision, toJson(score.factors()), override));

        log.info("Assessed deployment id={} correlationId={} score={} level={} decision={} trainedOn={}",
                deploymentId, deployment.getCorrelationId(), score.score(),
                score.level(), decision, score.trainedOn());

        return RiskAssessmentResponse.from(saved, toFactorResponses(score.factors()));
    }

    public RiskAssessmentResponse latestFor(UUID deploymentId) {
        return assessments.findFirstByDeploymentIdOrderByAssessedAtDesc(deploymentId)
                .map(assessment -> RiskAssessmentResponse.from(assessment, parseFactors(assessment.getFactors())))
                .orElse(null);
    }

    /**
     * Gate used before a deployment is allowed to start. Absence of an assessment is
     * treated as refusal: a deployment nobody scored has not been approved.
     */
    public void requireApprovalToStart(UUID deploymentId, DeploymentStatus target) {
        if (target != DeploymentStatus.RUNNING) {
            return;
        }

        RiskAssessment latest = assessments
                .findFirstByDeploymentIdOrderByAssessedAtDesc(deploymentId)
                .orElseThrow(() -> new DeploymentNotApprovedException(
                        "No risk assessment. Assess the deployment before starting it."));

        if (!latest.getDecision().allowsDeployment()) {
            throw new DeploymentNotApprovedException(switch (latest.getDecision()) {
                case BLOCK_DEPLOYMENT -> latest.getOverrideReason() != null
                        ? latest.getOverrideReason()
                        : "Blocked: risk score %d exceeds the approval threshold".formatted(latest.getRiskScore());
                case EXTRA_VALIDATION -> "Requires additional validation before it can start";
                default -> "Not approved";
            });
        }
    }

    private void requireComplete(DeploymentMetrics recorded, UUID deploymentId) {
        // Null means not measured. Substituting a value here would score a
        // deployment on numbers nobody observed.
        if (recorded.getFilesChanged() == null
                || recorded.getTestPassRate() == null
                || recorded.getTestCoverage() == null
                || recorded.getCriticalVulnerabilities() == null) {
            throw new MetricsIncompleteException(deploymentId);
        }
    }

    private RiskFeatures toFeatures(DeploymentMetrics m, Deployment deployment) {
        return new RiskFeatures(
                m.getFilesChanged(),
                orZero(m.getLinesAdded()),
                orZero(m.getLinesDeleted()),
                m.getTestPassRate().doubleValue(),
                m.getTestCoverage().doubleValue(),
                orZero(m.getCodeComplexity()),
                m.getCriticalVulnerabilities(),
                orZero(m.getHighVulnerabilities()),
                previousFailuresFor(deployment)
        );
    }

    /**
     * Counted from this project's history rather than reported by the pipeline. CI
     * does not know what happened on earlier deployments; CloudForge does.
     */
    private int previousFailuresFor(Deployment deployment) {
        return (int) deployments.countByProjectIdAndStatusIn(
                deployment.getProject().getId(),
                List.of(DeploymentStatus.FAILED, DeploymentStatus.ROLLED_BACK));
    }

    private static int orZero(Integer value) {
        return value == null ? 0 : value;
    }

    private String toJson(List<RiskScore.Factor> factors) {
        try {
            return objectMapper.writeValueAsString(factors);
                } catch (JacksonException ex) {
            log.warn("Could not serialise risk factors", ex);
            return null;
        }
    }

    private List<RiskAssessmentResponse.FactorResponse> toFactorResponses(List<RiskScore.Factor> factors) {
        return factors.stream()
                .map(f -> new RiskAssessmentResponse.FactorResponse(
                        f.feature(), f.value(), f.contribution(), f.explanation()))
                .toList();
    }

    private List<RiskAssessmentResponse.FactorResponse> parseFactors(String json) {
        if (json == null || json.isBlank()) {
            return List.of();
        }
        try {
            return List.of(objectMapper.readValue(json, RiskAssessmentResponse.FactorResponse[].class));
                } catch (JacksonException ex) {
            log.warn("Could not parse stored risk factors", ex);
            return List.of();
        }
    }
}
