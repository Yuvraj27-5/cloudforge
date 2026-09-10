package com.cloudforge.backend.deployment.dto;

import com.cloudforge.backend.deployment.DeploymentMetrics;

import java.math.BigDecimal;
import java.time.Instant;

public record DeploymentMetricsResponse(
        Integer filesChanged,
        Integer linesAdded,
        Integer linesDeleted,
        BigDecimal testPassRate,
        BigDecimal testCoverage,
        Integer codeComplexity,
        Integer codeSmells,
        Integer bugs,
        Integer securityHotspots,
        Integer criticalVulnerabilities,
        Integer highVulnerabilities,
        Integer mediumVulnerabilities,
        Integer lowVulnerabilities,
        boolean hasBlockingVulnerabilities,
        String source,
        Instant recordedAt
) {
    public static DeploymentMetricsResponse from(DeploymentMetrics metrics) {
        return new DeploymentMetricsResponse(
                metrics.getFilesChanged(),
                metrics.getLinesAdded(),
                metrics.getLinesDeleted(),
                metrics.getTestPassRate(),
                metrics.getTestCoverage(),
                metrics.getCodeComplexity(),
                metrics.getCodeSmells(),
                metrics.getBugs(),
                metrics.getSecurityHotspots(),
                metrics.getCriticalVulnerabilities(),
                metrics.getHighVulnerabilities(),
                metrics.getMediumVulnerabilities(),
                metrics.getLowVulnerabilities(),
                metrics.hasBlockingVulnerabilities(),
                metrics.getSource(),
                metrics.getRecordedAt()
        );
    }
}
