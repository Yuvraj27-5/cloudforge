package com.cloudforge.backend.system;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * Meta endpoint used to verify that the API surface is reachable.
 * Deliberately has no service layer: there is no business logic to delegate.
 */
@RestController
@RequestMapping("/api/v1/system")
public class SystemController {

    private static final String SERVICE = "cloudforge-backend";
    private static final String VERSION = "0.1.0";
    private static final String PHASE = "0";

    @GetMapping("/info")
    public ResponseEntity<SystemInfoResponse> info() {
        return ResponseEntity.ok(new SystemInfoResponse(SERVICE, VERSION, PHASE, Instant.now()));
    }
}
