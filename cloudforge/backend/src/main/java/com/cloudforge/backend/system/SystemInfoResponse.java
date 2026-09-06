package com.cloudforge.backend.system;

import java.time.Instant;

/**
 * Response DTO. JPA entities are never returned from controllers.
 */
public record SystemInfoResponse(
        String service,
        String version,
        String phase,
        Instant timestamp
) {
}
