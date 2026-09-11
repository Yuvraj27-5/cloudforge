package com.cloudforge.backend.risk;

/**
 * Source of a risk score. The decision engine depends on this interface, never on
 * the ML service directly, so the provider can be replaced — heuristic today,
 * gradient boosting now, a hosted service later — without touching decision logic.
 */
public interface RiskAssessmentProvider {

    /**
     * @throws RiskProviderUnavailableException when no score could be obtained.
     *         Implementations must never invent a score on failure: a risk engine
     *         that returns 0 when broken approves everything.
     */
    RiskScore score(RiskFeatures features);

    String name();
}
