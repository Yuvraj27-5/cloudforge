package com.cloudforge.backend.deployment;

import com.cloudforge.backend.common.exception.ResourceNotFoundException;
import com.cloudforge.backend.deployment.dto.CreateDeploymentRequest;
import com.cloudforge.backend.deployment.dto.DeploymentDetailResponse;
import com.cloudforge.backend.deployment.dto.DeploymentResponse;
import com.cloudforge.backend.deployment.dto.UpdateDeploymentStatusRequest;
import com.cloudforge.backend.project.Project;
import com.cloudforge.backend.project.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DeploymentService {

    private static final Logger log = LoggerFactory.getLogger(DeploymentService.class);

    private final DeploymentRepository deployments;
    private final DeploymentEventRepository events;
    private final ProjectRepository projects;

    public DeploymentService(DeploymentRepository deployments,
                             DeploymentEventRepository events,
                             ProjectRepository projects) {
        this.deployments = deployments;
        this.events = events;
        this.projects = projects;
    }

    public Page<DeploymentResponse> find(UUID projectId, DeploymentStatus status, Pageable pageable) {
        Page<Deployment> page;

        if (projectId != null && status != null) {
            page = deployments.findByProjectIdAndStatus(projectId, status, pageable);
        } else if (projectId != null) {
            page = deployments.findByProjectId(projectId, pageable);
        } else if (status != null) {
            page = deployments.findByStatus(status, pageable);
        } else {
            page = deployments.findAll(pageable);
        }

        return page.map(DeploymentResponse::from);
    }

    public DeploymentDetailResponse findById(UUID id) {
        Deployment deployment = getOrThrow(id);
        return DeploymentDetailResponse.from(
                deployment, events.findByDeploymentIdOrderByOccurredAtAsc(id));
    }

    @Transactional
    public DeploymentResponse create(UUID projectId, CreateDeploymentRequest request) {
        Project project = projects.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", projectId));

        Deployment deployment = new Deployment(
                project,
                request.commitSha(),
                request.commitMessage(),
                request.triggeredBy()
        );

        Deployment saved = deployments.save(deployment);
        events.save(DeploymentEvent.created(saved, request.triggeredBy()));

        log.info("Created deployment id={} project={} sha={} correlationId={}",
                saved.getId(), project.getName(), saved.getShortSha(), saved.getCorrelationId());

        return DeploymentResponse.from(saved);
    }

    @Transactional
    public DeploymentDetailResponse updateStatus(UUID id, UpdateDeploymentStatusRequest request) {
        Deployment deployment = getOrThrow(id);
        DeploymentStatus from = deployment.getStatus();
        DeploymentStatus to = request.status();

        if (from == to) {
            // Idempotent: a pipeline retrying the same callback should not fail.
            return DeploymentDetailResponse.from(
                    deployment, events.findByDeploymentIdOrderByOccurredAtAsc(id));
        }

        if (!from.canTransitionTo(to)) {
            throw new InvalidStatusTransitionException(from, to);
        }

        deployment.applyStatus(to, Instant.now());
        events.save(DeploymentEvent.statusChanged(deployment, from, to, request.reason(), request.actor()));

        log.info("Deployment id={} correlationId={} moved {} -> {} reason={}",
                id, deployment.getCorrelationId(), from, to, request.reason());

        return DeploymentDetailResponse.from(
                deployment, events.findByDeploymentIdOrderByOccurredAtAsc(id));
    }

    private Deployment getOrThrow(UUID id) {
        return deployments.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Deployment", id));
    }
}
