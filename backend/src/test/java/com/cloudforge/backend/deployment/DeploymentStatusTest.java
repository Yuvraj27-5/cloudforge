package com.cloudforge.backend.deployment;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The state machine is the contract every later phase depends on, so it is tested
 * directly rather than only through the service.
 */
class DeploymentStatusTest {

    @Test
    void pendingCanStartRunOrBeBlockedOrCancelled() {
        assertThat(DeploymentStatus.PENDING.allowedNext())
                .containsExactlyInAnyOrder(
                        DeploymentStatus.RUNNING,
                        DeploymentStatus.BLOCKED,
                        DeploymentStatus.CANCELLED);
    }

    @Test
    void succeededCanOnlyRollBack() {
        assertThat(DeploymentStatus.SUCCEEDED.allowedNext())
                .containsExactly(DeploymentStatus.ROLLED_BACK);
    }

    @Test
    void failedIsTerminalAndNotResurrectable() {
        assertThat(DeploymentStatus.FAILED.isTerminal()).isTrue();
        assertThat(DeploymentStatus.FAILED.canTransitionTo(DeploymentStatus.RUNNING)).isFalse();
        assertThat(DeploymentStatus.FAILED.canTransitionTo(DeploymentStatus.SUCCEEDED)).isFalse();
    }

    @Test
    void blockedIsTerminalAndDistinctFromFailed() {
        assertThat(DeploymentStatus.BLOCKED.isTerminal()).isTrue();
        assertThat(DeploymentStatus.BLOCKED).isNotEqualTo(DeploymentStatus.FAILED);
    }

    @Test
    void aDeploymentCannotRollBackWithoutHavingSucceeded() {
        assertThat(DeploymentStatus.RUNNING.canTransitionTo(DeploymentStatus.ROLLED_BACK)).isFalse();
        assertThat(DeploymentStatus.FAILED.canTransitionTo(DeploymentStatus.ROLLED_BACK)).isFalse();
        assertThat(DeploymentStatus.SUCCEEDED.canTransitionTo(DeploymentStatus.ROLLED_BACK)).isTrue();
    }

    @Test
    void activeStatesAreOnlyPendingAndRunning() {
        assertThat(DeploymentStatus.PENDING.isActive()).isTrue();
        assertThat(DeploymentStatus.RUNNING.isActive()).isTrue();
        assertThat(DeploymentStatus.SUCCEEDED.isActive()).isFalse();
    }
}
