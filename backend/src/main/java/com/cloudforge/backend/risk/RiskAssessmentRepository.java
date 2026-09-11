package com.cloudforge.backend.risk;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, UUID> {

    /** Latest assessment wins: a deployment may be rescored after metrics change. */
    Optional<RiskAssessment> findFirstByDeploymentIdOrderByAssessedAtDesc(UUID deploymentId);
}
