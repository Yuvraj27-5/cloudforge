package com.cloudforge.backend.deployment;

import java.util.Set;

/**
 * Deployment lifecycle.
 *
 * <p>Transitions are enforced in {@link DeploymentService}, not merely documented.
 * Terminal states stay terminal: a failed deployment is never resurrected, a new
 * one is created instead.
 *
 * <p>{@code BLOCKED} is deliberately separate from {@code FAILED}. A deployment the
 * risk engine refused is not a broken build — it is the product working — and the
 * dashboard must not fold the two together.
 *
 * <p>{@code ROLLED_BACK} follows {@code SUCCEEDED} only. A rollback means it went
 * live and then degraded, which is a different story from never deploying at all.
 */
public enum DeploymentStatus {

    PENDING,
    RUNNING,
    SUCCEEDED,
    FAILED,
    BLOCKED,
    ROLLED_BACK,
    CANCELLED;

    public Set<DeploymentStatus> allowedNext() {
        return switch (this) {
            case PENDING -> Set.of(RUNNING, BLOCKED, CANCELLED);
            case RUNNING -> Set.of(SUCCEEDED, FAILED, CANCELLED);
            case SUCCEEDED -> Set.of(ROLLED_BACK);
            case FAILED, BLOCKED, ROLLED_BACK, CANCELLED -> Set.of();
        };
    }

    public boolean canTransitionTo(DeploymentStatus next) {
        return allowedNext().contains(next);
    }

    /** True once no further transition is possible. */
    public boolean isTerminal() {
        return allowedNext().isEmpty();
    }

    /** True while the deployment is live and serving. */
    public boolean isActive() {
        return this == PENDING || this == RUNNING;
    }
}
