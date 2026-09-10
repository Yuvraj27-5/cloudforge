package com.cloudforge.backend.deployment;

import com.cloudforge.backend.common.dto.PageResponse;
import com.cloudforge.backend.deployment.dto.CreateDeploymentRequest;
import com.cloudforge.backend.deployment.dto.DeploymentDetailResponse;
import com.cloudforge.backend.deployment.dto.DeploymentResponse;
import com.cloudforge.backend.deployment.dto.UpdateDeploymentStatusRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;
import java.util.function.Function;

@RestController
@RequestMapping("/api/v1")
public class DeploymentController {

    private final DeploymentService service;

    public DeploymentController(DeploymentService service) {
        this.service = service;
    }

    @GetMapping("/deployments")
    public PageResponse<DeploymentResponse> list(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) DeploymentStatus status,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable) {

        Page<DeploymentResponse> page = service.find(projectId, status, pageable);
        return PageResponse.from(page, Function.identity());
    }

    @GetMapping("/deployments/{id}")
    public DeploymentDetailResponse get(@PathVariable UUID id) {
        return service.findById(id);
    }

    /**
     * Nested under the project: a deployment cannot exist without one. Every other
     * route is flat, because after creation you look a deployment up by its own id.
     */
    @PostMapping("/projects/{projectId}/deployments")
    public ResponseEntity<DeploymentResponse> create(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateDeploymentRequest request,
            UriComponentsBuilder uriBuilder) {

        DeploymentResponse created = service.create(projectId, request);

        return ResponseEntity
                .created(uriBuilder.path("/api/v1/deployments/{id}").build(created.id()))
                .body(created);
    }

    @PatchMapping("/deployments/{id}/status")
    public DeploymentDetailResponse updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDeploymentStatusRequest request) {

        return service.updateStatus(id, request);
    }
}
