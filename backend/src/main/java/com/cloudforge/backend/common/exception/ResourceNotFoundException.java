package com.cloudforge.backend.common.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String resource, Object id) {
        super("%s not found: %s".formatted(resource, id));
    }
}
