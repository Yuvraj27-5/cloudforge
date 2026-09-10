package com.cloudforge.backend.deployment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * What the pipeline measured about a deployment. Phase 7 reads this table directly
 * as the risk model's feature vector, so these are raw observations only — no
 * scoring, weighting or interpretation lives here.
 *
 * <p>Fields are nullable on purpose. A pipeline that has not yet been taught to run
 * static analysis should record what it does know rather than reporting zero, which
 * the model would read as "no problems found".
 */
@Entity
@Table(name = "deployment_metrics")
public class DeploymentMetrics {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deployment_id", nullable = false, unique = true)
    private Deployment deployment;

    @Column(name = "files_changed")
    private Integer filesChanged;

    @Column(name = "lines_added")
    private Integer linesAdded;

    @Column(name = "lines_deleted")
    private Integer linesDeleted;

    @Column(name = "test_pass_rate", precision = 5, scale = 2)
    private BigDecimal testPassRate;

    @Column(name = "test_coverage", precision = 5, scale = 2)
    private BigDecimal testCoverage;

    @Column(name = "code_complexity")
    private Integer codeComplexity;

    @Column(name = "code_smells")
    private Integer codeSmells;

    @Column
    private Integer bugs;

    @Column(name = "security_hotspots")
    private Integer securityHotspots;

    @Column(name = "critical_vulnerabilities")
    private Integer criticalVulnerabilities;

    @Column(name = "high_vulnerabilities")
    private Integer highVulnerabilities;

    @Column(name = "medium_vulnerabilities")
    private Integer mediumVulnerabilities;

    @Column(name = "low_vulnerabilities")
    private Integer lowVulnerabilities;

    /** Which pipeline recorded this, for example "github-actions" or "manual". */
    @Column(nullable = false, length = 60)
    private String source;

    @Column(name = "recorded_at", nullable = false)
    private Instant recordedAt;

    protected DeploymentMetrics() {
        // required by JPA
    }

    public DeploymentMetrics(Deployment deployment) {
        this.id = UUID.randomUUID();
        this.deployment = deployment;
    }

    public void record(Integer filesChanged, Integer linesAdded, Integer linesDeleted,
                       BigDecimal testPassRate, BigDecimal testCoverage,
                       Integer codeComplexity, Integer codeSmells, Integer bugs,
                       Integer securityHotspots,
                       Integer criticalVulnerabilities, Integer highVulnerabilities,
                       Integer mediumVulnerabilities, Integer lowVulnerabilities,
                       String source) {
        this.filesChanged = filesChanged;
        this.linesAdded = linesAdded;
        this.linesDeleted = linesDeleted;
        this.testPassRate = testPassRate;
        this.testCoverage = testCoverage;
        this.codeComplexity = codeComplexity;
        this.codeSmells = codeSmells;
        this.bugs = bugs;
        this.securityHotspots = securityHotspots;
        this.criticalVulnerabilities = criticalVulnerabilities;
        this.highVulnerabilities = highVulnerabilities;
        this.mediumVulnerabilities = mediumVulnerabilities;
        this.lowVulnerabilities = lowVulnerabilities;
        this.source = source;
        this.recordedAt = Instant.now();
    }

    /**
     * True when the security scan found something that must be looked at. Phase 8
     * uses this as a hard gate independent of the model's score: a critical CVE is
     * not a probability question.
     */
    public boolean hasBlockingVulnerabilities() {
        return criticalVulnerabilities != null && criticalVulnerabilities > 0;
    }

    public UUID getId() {
        return id;
    }

    public Integer getFilesChanged() {
        return filesChanged;
    }

    public Integer getLinesAdded() {
        return linesAdded;
    }

    public Integer getLinesDeleted() {
        return linesDeleted;
    }

    public BigDecimal getTestPassRate() {
        return testPassRate;
    }

    public BigDecimal getTestCoverage() {
        return testCoverage;
    }

    public Integer getCodeComplexity() {
        return codeComplexity;
    }

    public Integer getCodeSmells() {
        return codeSmells;
    }

    public Integer getBugs() {
        return bugs;
    }

    public Integer getSecurityHotspots() {
        return securityHotspots;
    }

    public Integer getCriticalVulnerabilities() {
        return criticalVulnerabilities;
    }

    public Integer getHighVulnerabilities() {
        return highVulnerabilities;
    }

    public Integer getMediumVulnerabilities() {
        return mediumVulnerabilities;
    }

    public Integer getLowVulnerabilities() {
        return lowVulnerabilities;
    }

    public String getSource() {
        return source;
    }

    public Instant getRecordedAt() {
        return recordedAt;
    }
}
