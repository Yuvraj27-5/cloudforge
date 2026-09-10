package com.cloudforge.backend.deployment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/**
 * One row per state change. This is the audit trail that renders the timeline,
 * and in Phase 11 it is where "why did this roll back at 3am" gets answered.
 * Without it you only ever see current status and lose the history.
 */
@Entity
@Table(name = "deployment_events")
public class DeploymentEvent {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "deployment_id", nullable = false)
    private Deployment deployment;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30)
    private DeploymentEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "from_status", length = 30)
    private DeploymentStatus fromStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "to_status", length = 30)
    private DeploymentStatus toStatus;

    @Column(length = 500)
    private String reason;

    @Column(length = 120)
    private String actor;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected DeploymentEvent() {
        // required by JPA
    }

    private DeploymentEvent(Deployment deployment, DeploymentEventType eventType,
                            DeploymentStatus fromStatus, DeploymentStatus toStatus,
                            String reason, String actor) {
        this.id = UUID.randomUUID();
        this.deployment = deployment;
        this.eventType = eventType;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.reason = reason;
        this.actor = actor;
        this.occurredAt = Instant.now();
    }

    static DeploymentEvent created(Deployment deployment, String actor) {
        return new DeploymentEvent(deployment, DeploymentEventType.CREATED,
                null, DeploymentStatus.PENDING, "Deployment recorded", actor);
    }

    static DeploymentEvent statusChanged(Deployment deployment, DeploymentStatus from,
                                         DeploymentStatus to, String reason, String actor) {
        return new DeploymentEvent(deployment, DeploymentEventType.STATUS_CHANGED,
                from, to, reason, actor);
    }

    public UUID getId() {
        return id;
    }

    public DeploymentEventType getEventType() {
        return eventType;
    }

    public DeploymentStatus getFromStatus() {
        return fromStatus;
    }

    public DeploymentStatus getToStatus() {
        return toStatus;
    }

    public String getReason() {
        return reason;
    }

    public String getActor() {
        return actor;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }
}
