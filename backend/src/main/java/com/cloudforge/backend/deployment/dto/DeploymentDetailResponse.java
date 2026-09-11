package com.cloudforge.backend.deployment.dto;

import com.cloudforge.backend.deployment.Deployment;
import com.cloudforge.backend.deployment.DeploymentEvent;
import com.cloudforge.backend.deployment.DeploymentMetrics;
import com.cloudforge.backend.deployment.DeploymentStatus;
import com.cloudforge.backend.risk.dto.RiskAssessmentResponse;

import java.util.List;
import java.util.Set;

/**
 * Detail view: the deployment, its event timeline, and which transitions are legal
 * from here. The UI renders action buttons from allowedTransitions rather than
 * duplicating the state machine in TypeScript.
 */
public record DeploymentDetailResponse(
        DeploymentResponse deployment,
        List<DeploymentEventResponse> events,
        Set<DeploymentStatus> allowedTransitions,
        /** Null until the pipeline reports. Absent is not the same as all-zero. */
        DeploymentMetricsResponse metrics,
        /** Null until assessed. Absence is what blocks the deployment from starting. */
        RiskAssessmentResponse riskAssessment
) {
    public static DeploymentDetailResponse from(Deployment deployment,
                                                List<DeploymentEvent> events,
                                                DeploymentMetrics metrics,
                                                RiskAssessmentResponse riskAssessment) {
        return new DeploymentDetailResponse(
                DeploymentResponse.from(deployment),
                events.stream().map(DeploymentEventResponse::from).toList(),
                deployment.getStatus().allowedNext(),
                metrics == null ? null : DeploymentMetricsResponse.from(metrics),
                riskAssessment
        );
    }
}
