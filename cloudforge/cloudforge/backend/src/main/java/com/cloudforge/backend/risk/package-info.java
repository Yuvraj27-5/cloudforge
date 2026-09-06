/**
 * Deployment risk: the client for the FastAPI ML service and the decision engine.
 *
 * <p>Filled in <strong>Phases 7 and 8</strong>.
 *
 * <p>The {@code RiskAssessmentProvider} interface lives here so the risk source stays
 * swappable. The threshold mapping (LOW/MEDIUM/HIGH to APPROVE/EXTRA_VALIDATION/BLOCK)
 * belongs to the backend, never to the model.
 */
package com.cloudforge.backend.risk;
