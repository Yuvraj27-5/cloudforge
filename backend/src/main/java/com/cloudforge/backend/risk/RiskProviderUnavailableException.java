package com.cloudforge.backend.risk;

public class RiskProviderUnavailableException extends RuntimeException {

    public RiskProviderUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
