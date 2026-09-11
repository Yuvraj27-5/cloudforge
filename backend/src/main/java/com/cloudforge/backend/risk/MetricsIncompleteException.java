package com.cloudforge.backend.risk;

import java.util.UUID;

public class MetricsIncompleteException extends RuntimeException {

    public MetricsIncompleteException(UUID deploymentId) {
        super(("Metrics for deployment %s are incomplete. "
                + "filesChanged, testPassRate, testCoverage and criticalVulnerabilities "
                + "must all be measured before scoring.").formatted(deploymentId));
    }
}
