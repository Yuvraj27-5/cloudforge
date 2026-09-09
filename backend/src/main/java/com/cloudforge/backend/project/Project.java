package com.cloudforge.backend.project;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * A repository CloudForge builds, scores and deploys.
 *
 * <p>No public setters: the entity is built through its constructor and changed
 * as a whole through {@link #update}. A bag of setters lets any caller leave the
 * object in a half-valid state.
 */
@Entity
@Table(name = "projects")
public class Project {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Column(name = "repository_url", nullable = false, length = 500)
    private String repositoryUrl;

    @Column(name = "default_branch", nullable = false, length = 120)
    private String defaultBranch;

    @Enumerated(EnumType.STRING)
    @Column(name = "cloud_provider", nullable = false, length = 30)
    private CloudProvider cloudProvider;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Environment environment;

    @Column(length = 500)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Project() {
        // required by JPA
    }

    public Project(String name, String repositoryUrl, String defaultBranch,
                   CloudProvider cloudProvider, Environment environment, String description) {
        this.id = UUID.randomUUID();
        this.name = name;
        this.repositoryUrl = repositoryUrl;
        this.defaultBranch = defaultBranch;
        this.cloudProvider = cloudProvider;
        this.environment = environment;
        this.description = description;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public void update(String name, String repositoryUrl, String defaultBranch,
                       CloudProvider cloudProvider, Environment environment, String description) {
        this.name = name;
        this.repositoryUrl = repositoryUrl;
        this.defaultBranch = defaultBranch;
        this.cloudProvider = cloudProvider;
        this.environment = environment;
        this.description = description;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRepositoryUrl() {
        return repositoryUrl;
    }

    public String getDefaultBranch() {
        return defaultBranch;
    }

    public CloudProvider getCloudProvider() {
        return cloudProvider;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public String getDescription() {
        return description;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
