package com.cloudforge.backend.deployment.dto;

import com.cloudforge.backend.deployment.Deployment;
import com.cloudforge.backend.deployment.DeploymentEvent;
import com.cloudforge.backend.deployment.DeploymentStatus;

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
        Set<DeploymentStatus> allowedTransitions
) {
    public static DeploymentDetailResponse from(Deployment deployment, List<DeploymentEvent> events) {
        return new DeploymentDetailResponse(
                DeploymentResponse.from(deployment),
                events.stream().map(DeploymentEventResponse::from).toList(),
                deployment.getStatus().allowedNext()
        );
    }
}
