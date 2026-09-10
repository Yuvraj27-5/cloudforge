package com.cloudforge.backend.deployment;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeploymentRepository extends JpaRepository<Deployment, UUID> {

    Page<Deployment> findByProjectId(UUID projectId, Pageable pageable);

    Page<Deployment> findByStatus(DeploymentStatus status, Pageable pageable);

    Page<Deployment> findByProjectIdAndStatus(UUID projectId, DeploymentStatus status, Pageable pageable);
}
