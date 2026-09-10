package com.cloudforge.backend.deployment;

import com.cloudforge.backend.project.Project;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "deployments")
public class Deployment {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "commit_sha", nullable = false, length = 40)
    private String commitSha;

    @Column(name = "commit_message", length = 500)
    private String commitMessage;

    @Column(name = "triggered_by", length = 120)
    private String triggeredBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DeploymentStatus status;

    /**
     * Traces this deployment across backend logs, the ML service, Kubernetes events
     * and monitoring alerts. See docs/api-conventions.md.
     */
    @Column(name = "correlation_id", nullable = false)
    private UUID correlationId;

    @Column(name = "started_at")
    private Instant startedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Deployment() {
        // required by JPA
    }

    public Deployment(Project project, String commitSha, String commitMessage, String triggeredBy) {
        this.id = UUID.randomUUID();
        this.correlationId = UUID.randomUUID();
        this.project = project;
        this.commitSha = commitSha;
        this.commitMessage = commitMessage;
        this.triggeredBy = triggeredBy;
        this.status = DeploymentStatus.PENDING;
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

    /**
     * Applies a status change. The transition is validated by the service before
     * this is called; the entity owns the side effects of the change.
     */
        void applyStatus(DeploymentStatus next, Instant when) {
        this.status = next;

        if (next == DeploymentStatus.RUNNING && this.startedAt == null) {
            this.startedAt = when;
        }

        // Not isTerminal(): SUCCEEDED is not terminal, because a rollback can
        // follow it, but the deployment run has finished. Completion is about the
        // run ending, not about the state machine having no exits.
        //
        // First write wins: a rollback happening later must not overwrite when the
        // original deployment actually completed.
        if (!next.isActive() && this.completedAt == null) {
            this.completedAt = when;
        }
    }

    public UUID getId() {
        return id;
    }

    public Project getProject() {
        return project;
    }

    public String getCommitSha() {
        return commitSha;
    }

    public String getShortSha() {
        return commitSha.length() > 7 ? commitSha.substring(0, 7) : commitSha;
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    public String getTriggeredBy() {
        return triggeredBy;
    }

    public DeploymentStatus getStatus() {
        return status;
    }

    public UUID getCorrelationId() {
        return correlationId;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
