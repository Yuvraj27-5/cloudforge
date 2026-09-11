package com.cloudforge.backend.risk;

import java.util.UUID;

public class MetricsMissingException extends RuntimeException {

    public MetricsMissingException(UUID deploymentId) {
        super("No pipeline metrics recorded for deployment %s. Nothing to score.".formatted(deploymentId));
    }
}
