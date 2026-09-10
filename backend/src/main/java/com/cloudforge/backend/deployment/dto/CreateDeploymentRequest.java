package com.cloudforge.backend.deployment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateDeploymentRequest(

        @NotBlank(message = "commitSha is required")
        @Pattern(regexp = "^[0-9a-fA-F]{7,40}$", message = "commitSha must be 7 to 40 hex characters")
        String commitSha,

        @Size(max = 500)
        String commitMessage,

        @Size(max = 120)
        String triggeredBy
) {
}
