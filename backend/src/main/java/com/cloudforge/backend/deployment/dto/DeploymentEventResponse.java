package com.cloudforge.backend.deployment.dto;

import com.cloudforge.backend.deployment.DeploymentEvent;
import com.cloudforge.backend.deployment.DeploymentEventType;
import com.cloudforge.backend.deployment.DeploymentStatus;

import java.time.Instant;
import java.util.UUID;

public record DeploymentEventResponse(
        UUID id,
        DeploymentEventType eventType,
        DeploymentStatus fromStatus,
        DeploymentStatus toStatus,
        String reason,
        String actor,
        Instant occurredAt
) {
    public static DeploymentEventResponse from(DeploymentEvent event) {
        return new DeploymentEventResponse(
                event.getId(),
                event.getEventType(),
                event.getFromStatus(),
                event.getToStatus(),
                event.getReason(),
                event.getActor(),
                event.getOccurredAt()
        );
    }
}
