package com.cloudforge.backend.deployment.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Every field is optional except the source. A pipeline reports what it measured;
 * absent means "not measured", which is different from zero. Recording zero
 * vulnerabilities when no scan ran would tell the Phase 7 model the change was
 * clean.
 */
public record RecordMetricsRequest(

        @Min(0) Integer filesChanged,
        @Min(0) Integer linesAdded,
        @Min(0) Integer linesDeleted,

        @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal testPassRate,
        @DecimalMin("0.0") @DecimalMax("100.0") BigDecimal testCoverage,

        @Min(0) Integer codeComplexity,
        @Min(0) Integer codeSmells,
        @Min(0) Integer bugs,
        @Min(0) Integer securityHotspots,

        @Min(0) Integer criticalVulnerabilities,
        @Min(0) Integer highVulnerabilities,
        @Min(0) Integer mediumVulnerabilities,
        @Min(0) Integer lowVulnerabilities,

        @NotBlank(message = "source is required")
        @Size(max = 60)
        String source
) {
}
