package com.cloudforge.backend.project.dto;

import com.cloudforge.backend.project.CloudProvider;
import com.cloudforge.backend.project.Environment;
import com.cloudforge.backend.project.Project;

import java.time.Instant;
import java.util.UUID;

public record ProjectResponse(
        UUID id,
        String name,
        String repositoryUrl,
        String defaultBranch,
        CloudProvider cloudProvider,
        Environment environment,
        String description,
        Instant createdAt,
        Instant updatedAt
) {
    public static ProjectResponse from(Project project) {
        return new ProjectResponse(
                project.getId(),
                project.getName(),
                project.getRepositoryUrl(),
                project.getDefaultBranch(),
                project.getCloudProvider(),
                project.getEnvironment(),
                project.getDescription(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
}
