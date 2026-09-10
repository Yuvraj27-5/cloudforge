package com.cloudforge.backend.deployment.dto;

import com.cloudforge.backend.deployment.DeploymentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateDeploymentStatusRequest(

        @NotNull(message = "status is required")
        DeploymentStatus status,

        @Size(max = 500)
        String reason,

        @Size(max = 120)
        String actor
) {
}
