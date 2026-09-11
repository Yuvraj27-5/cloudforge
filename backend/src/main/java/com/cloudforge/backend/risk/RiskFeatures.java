package com.cloudforge.backend.risk;

/**
 * The feature vector sent to the risk provider. Built from stored deployment
 * metrics; every value must have been measured.
 */
public record RiskFeatures(
        int filesChanged,
        int linesAdded,
        int linesDeleted,
        double testPassRate,
        double testCoverage,
        int codeComplexity,
        int criticalVulnerabilities,
        int highVulnerabilities,
        int previousDeploymentFailures
) {
}
