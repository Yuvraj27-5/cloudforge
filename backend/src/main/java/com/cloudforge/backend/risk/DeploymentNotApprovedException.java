package com.cloudforge.backend.risk;

public class DeploymentNotApprovedException extends RuntimeException {

    public DeploymentNotApprovedException(String message) {
        super(message);
    }
}
