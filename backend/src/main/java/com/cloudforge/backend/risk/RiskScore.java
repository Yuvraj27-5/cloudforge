package com.cloudforge.backend.risk;

import java.util.List;

/**
 * What a provider returns. Deliberately has no decision field: scoring and deciding
 * are separate responsibilities, and the threshold policy belongs to CloudForge,
 * not to whichever model produced the number.
 */
public record RiskScore(
        int score,
        RiskLevel level,
        double confidence,
        List<Factor> factors,
        String modelVersion,
        String trainedOn
) {
    public record Factor(String feature, double value, double contribution, String explanation) {
    }
}
