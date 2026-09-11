package com.cloudforge.backend.risk;

import com.cloudforge.backend.deployment.Deployment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "risk_assessments")
public class RiskAssessment {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deployment_id", nullable = false)
    private Deployment deployment;

    /** Null when the provider was unavailable: no score was obtained, not a score of zero. */
    @Column(name = "risk_score")
    private Integer riskScore;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", length = 20)
    private RiskLevel riskLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DeploymentDecision decision;

    @Column(precision = 4, scale = 3)
    private BigDecimal confidence;

    @Column(columnDefinition = "text")
    private String factors;

    @Column(name = "model_version", length = 60)
    private String modelVersion;

    @Column(name = "trained_on", length = 40)
    private String trainedOn;

    @Column(nullable = false, length = 60)
    private String provider;

    @Column(name = "override_reason", length = 500)
    private String overrideReason;

    @Column(name = "assessed_at", nullable = false)
    private Instant assessedAt;

    protected RiskAssessment() {
        // required by JPA
    }

    private RiskAssessment(Deployment deployment, String provider) {
        this.id = UUID.randomUUID();
        this.deployment = deployment;
        this.provider = provider;
        this.assessedAt = Instant.now();
    }

    public static RiskAssessment scored(Deployment deployment, String provider,
                                        RiskScore risk, DeploymentDecision decision,
                                        String factorsJson, String overrideReason) {
        RiskAssessment assessment = new RiskAssessment(deployment, provider);
        assessment.riskScore = risk.score();
        assessment.riskLevel = risk.level();
        assessment.confidence = BigDecimal.valueOf(risk.confidence());
        assessment.factors = factorsJson;
        assessment.modelVersion = risk.modelVersion();
        assessment.trainedOn = risk.trainedOn();
        assessment.decision = decision;
        assessment.overrideReason = overrideReason;
        return assessment;
    }

    /** No score obtained. Score and level stay null so this is never mistaken for a low score. */
    public static RiskAssessment unavailable(Deployment deployment, String provider,
                                             DeploymentDecision decision, String reason) {
        RiskAssessment assessment = new RiskAssessment(deployment, provider);
        assessment.decision = decision;
        assessment.overrideReason = reason;
        return assessment;
    }

    public UUID getId() {
        return id;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public DeploymentDecision getDecision() {
        return decision;
    }

    public BigDecimal getConfidence() {
        return confidence;
    }

    public String getFactors() {
        return factors;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public String getTrainedOn() {
        return trainedOn;
    }

    public String getProvider() {
        return provider;
    }

    public String getOverrideReason() {
        return overrideReason;
    }

    public Instant getAssessedAt() {
        return assessedAt;
    }

    public Deployment getDeployment() {
        return deployment;
    }
}
