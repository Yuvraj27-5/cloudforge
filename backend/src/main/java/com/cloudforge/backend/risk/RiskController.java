package com.cloudforge.backend.risk;

import com.cloudforge.backend.risk.dto.RiskAssessmentResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/deployments/{id}/risk-assessment")
public class RiskController {

    private final RiskAssessmentService service;

    public RiskController(RiskAssessmentService service) {
        this.service = service;
    }

    /** Scores the deployment from its recorded metrics and stores the verdict. */
    @PostMapping
    public RiskAssessmentResponse assess(@PathVariable UUID id) {
        return service.assess(id);
    }

    @GetMapping
    public ResponseEntity<RiskAssessmentResponse> latest(@PathVariable UUID id) {
        RiskAssessmentResponse latest = service.latestFor(id);
        return latest == null ? ResponseEntity.noContent().build() : ResponseEntity.ok(latest);
    }
}
