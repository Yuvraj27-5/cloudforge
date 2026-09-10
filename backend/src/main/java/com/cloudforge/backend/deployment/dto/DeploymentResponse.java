package com.cloudforge.backend.deployment.dto;

import com.cloudforge.backend.deployment.Deployment;
import com.cloudforge.backend.deployment.DeploymentStatus;

import java.time.Instant;
import java.util.UUID;

/** List view. Carries the project name so the global feed needs no second lookup. */
public record DeploymentResponse(
        UUID id,
        UUID projectId,
        String projectName,
        String commitSha,
        String shortSha,
        String commitMessage,
        String triggeredBy,
        DeploymentStatus status,
        UUID correlationId,
        Instant startedAt,
        Instant completedAt,
        Instant createdAt
) {
    public static DeploymentResponse from(Deployment deployment) {
        return new DeploymentResponse(
                deployment.getId(),
                deployment.getProject().getId(),
                deployment.getProject().getName(),
                deployment.getCommitSha(),
                deployment.getShortSha(),
                deployment.getCommitMessage(),
                deployment.getTriggeredBy(),
                deployment.getStatus(),
                deployment.getCorrelationId(),
                deployment.getStartedAt(),
                deployment.getCompletedAt(),
                deployment.getCreatedAt()
        );
    }
}
