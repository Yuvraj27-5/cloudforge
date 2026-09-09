package com.cloudforge.backend.project.dto;

import com.cloudforge.backend.project.CloudProvider;
import com.cloudforge.backend.project.Environment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * PUT replaces the whole resource, so every field is required here even though
 * defaultBranch is optional on create.
 */
public record UpdateProjectRequest(

        @NotBlank(message = "name is required")
        @Size(max = 120)
        String name,

        @NotBlank(message = "repositoryUrl is required")
        @Pattern(
                regexp = "^https?://.+|^git@.+:.+\\.git$",
                message = "repositoryUrl must be an http(s) or git SSH URL"
        )
        @Size(max = 500)
        String repositoryUrl,

        @NotBlank(message = "defaultBranch is required")
        @Size(max = 120)
        String defaultBranch,

        @NotNull(message = "cloudProvider is required")
        CloudProvider cloudProvider,

        @NotNull(message = "environment is required")
        Environment environment,

        @Size(max = 500)
        String description
) {
}
