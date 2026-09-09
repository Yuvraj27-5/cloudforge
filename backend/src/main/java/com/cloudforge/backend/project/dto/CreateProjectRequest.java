package com.cloudforge.backend.project.dto;

import com.cloudforge.backend.project.CloudProvider;
import com.cloudforge.backend.project.Environment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateProjectRequest(

        @NotBlank(message = "name is required")
        @Size(max = 120, message = "name must be at most 120 characters")
        String name,

        @NotBlank(message = "repositoryUrl is required")
        @Pattern(
                regexp = "^https?://.+|^git@.+:.+\\.git$",
                message = "repositoryUrl must be an http(s) or git SSH URL"
        )
        @Size(max = 500)
        String repositoryUrl,

        @Size(max = 120)
        String defaultBranch,

        @NotNull(message = "cloudProvider is required")
        CloudProvider cloudProvider,

        @NotNull(message = "environment is required")
        Environment environment,

        @Size(max = 500)
        String description
) {
    /** defaultBranch is optional on create; most repositories use main. */
    public String defaultBranchOrMain() {
        return (defaultBranch == null || defaultBranch.isBlank()) ? "main" : defaultBranch;
    }
}
