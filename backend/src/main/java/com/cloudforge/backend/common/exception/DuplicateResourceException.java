package com.cloudforge.backend.common.exception;

public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String resource, String field, Object value) {
        super("%s with %s '%s' already exists".formatted(resource, field, value));
    }
}
