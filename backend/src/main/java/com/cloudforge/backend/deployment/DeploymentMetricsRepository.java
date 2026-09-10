package com.cloudforge.backend.deployment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface DeploymentMetricsRepository extends JpaRepository<DeploymentMetrics, UUID> {

    Optional<DeploymentMetrics> findByDeploymentId(UUID deploymentId);
}
