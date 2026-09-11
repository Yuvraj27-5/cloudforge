package com.cloudforge.backend.deployment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DeploymentRepository extends JpaRepository<Deployment, UUID> {

    Page<Deployment> findByProjectId(UUID projectId, Pageable pageable);

    Page<Deployment> findByStatus(DeploymentStatus status, Pageable pageable);

    Page<Deployment> findByProjectIdAndStatus(UUID projectId, DeploymentStatus status, Pageable pageable);

    /** Feeds the previous_deployment_failures feature. CI cannot know this; CloudForge can. */
    long countByProjectIdAndStatusIn(UUID projectId, List<DeploymentStatus> statuses);
}
