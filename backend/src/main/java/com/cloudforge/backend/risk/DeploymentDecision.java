package com.cloudforge.backend.risk;

public enum DeploymentDecision {

    /** Low risk. The pipeline proceeds. */
    APPROVE_DEPLOYMENT,

    /** Medium risk. The pipeline proceeds only after a human approves. */
    EXTRA_VALIDATION,

    /** High risk or a hard gate. The deployment cannot start. */
    BLOCK_DEPLOYMENT;

    public boolean allowsDeployment() {
        return this == APPROVE_DEPLOYMENT;
    }
}
