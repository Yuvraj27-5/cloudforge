package com.cloudforge.backend.deployment;

public class InvalidStatusTransitionException extends RuntimeException {

    public InvalidStatusTransitionException(DeploymentStatus from, DeploymentStatus to) {
        super("Cannot move deployment from %s to %s. Allowed: %s"
                .formatted(from, to, from.allowedNext().isEmpty() ? "none, %s is final".formatted(from) : from.allowedNext()));
    }
}
