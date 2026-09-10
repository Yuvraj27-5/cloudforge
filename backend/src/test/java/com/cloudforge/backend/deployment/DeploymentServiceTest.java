package com.cloudforge.backend.deployment;

import com.cloudforge.backend.common.exception.ResourceNotFoundException;
import com.cloudforge.backend.deployment.dto.CreateDeploymentRequest;
import com.cloudforge.backend.deployment.dto.UpdateDeploymentStatusRequest;
import com.cloudforge.backend.project.CloudProvider;
import com.cloudforge.backend.project.Environment;
import com.cloudforge.backend.project.Project;
import com.cloudforge.backend.project.ProjectRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeploymentServiceTest {

    @Mock
    private DeploymentRepository deployments;

    @Mock
    private DeploymentEventRepository events;

    @Mock
    private ProjectRepository projects;

    @InjectMocks
    private DeploymentService service;

    private Project project;

    @BeforeEach
    void setUp() {
        project = new Project("payments-api", "https://github.com/acme/payments-api", "main",
                CloudProvider.AWS, Environment.PRODUCTION, null);
    }

    @Test
    void createStartsPendingAndRecordsAnEvent() {
        when(projects.findById(any())).thenReturn(Optional.of(project));
        when(deployments.save(any(Deployment.class))).thenAnswer(call -> call.getArgument(0));

        var response = service.create(UUID.randomUUID(),
                new CreateDeploymentRequest("a1b2c3d4e5f6", "Fix rounding", "aush"));

        assertThat(response.status()).isEqualTo(DeploymentStatus.PENDING);
        assertThat(response.shortSha()).isEqualTo("a1b2c3d");
        assertThat(response.correlationId()).isNotNull();
        verify(events).save(any(DeploymentEvent.class));
    }

    @Test
    void createFailsWhenProjectMissing() {
        UUID missing = UUID.randomUUID();
        when(projects.findById(missing)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(missing,
                new CreateDeploymentRequest("a1b2c3d", null, null)))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(deployments, never()).save(any());
    }

    @Test
    void statusChangeIsRejectedWhenNotAllowed() {
        Deployment deployment = new Deployment(project, "a1b2c3d", null, "aush");
        UUID id = deployment.getId();
        when(deployments.findById(id)).thenReturn(Optional.of(deployment));

        // PENDING cannot jump straight to SUCCEEDED.
        assertThatThrownBy(() -> service.updateStatus(id,
                new UpdateDeploymentStatusRequest(DeploymentStatus.SUCCEEDED, null, "aush")))
                .isInstanceOf(InvalidStatusTransitionException.class)
                .hasMessageContaining("PENDING");

        verify(events, never()).save(any());
    }

    @Test
    void runningSetsStartedAtAndTerminalSetsCompletedAt() {
        Deployment deployment = new Deployment(project, "a1b2c3d", null, "aush");
        UUID id = deployment.getId();
        when(deployments.findById(id)).thenReturn(Optional.of(deployment));
        when(events.findByDeploymentIdOrderByOccurredAtAsc(id)).thenReturn(List.of());

        service.updateStatus(id, new UpdateDeploymentStatusRequest(
                DeploymentStatus.RUNNING, "Pipeline started", "github-actions"));
        assertThat(deployment.getStartedAt()).isNotNull();
        assertThat(deployment.getCompletedAt()).isNull();

        service.updateStatus(id, new UpdateDeploymentStatusRequest(
                DeploymentStatus.SUCCEEDED, "Healthy", "github-actions"));
        assertThat(deployment.getCompletedAt()).isNotNull();
    }

    @Test
    void repeatingTheSameStatusIsIdempotent() {
        Deployment deployment = new Deployment(project, "a1b2c3d", null, "aush");
        UUID id = deployment.getId();
        when(deployments.findById(id)).thenReturn(Optional.of(deployment));
        when(events.findByDeploymentIdOrderByOccurredAtAsc(id)).thenReturn(List.of());

        // A pipeline retrying its callback must not produce an error.
        var response = service.updateStatus(id,
                new UpdateDeploymentStatusRequest(DeploymentStatus.PENDING, null, "github-actions"));

        assertThat(response.deployment().status()).isEqualTo(DeploymentStatus.PENDING);
        verify(events, never()).save(any());
    }
}
