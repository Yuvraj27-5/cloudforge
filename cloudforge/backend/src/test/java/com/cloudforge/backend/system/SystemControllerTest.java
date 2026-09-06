package com.cloudforge.backend.system;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Standalone MockMvc: no Spring context, no database, runs in milliseconds.
 * Context-loading tests arrive in Phase 1 with Testcontainers.
 */
class SystemControllerTest {

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new SystemController())
            .build();

    @Test
    void infoReturnsServiceMetadata() throws Exception {
        mockMvc.perform(get("/api/v1/system/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("cloudforge-backend"))
                .andExpect(jsonPath("$.phase").value("0"));
    }
}
