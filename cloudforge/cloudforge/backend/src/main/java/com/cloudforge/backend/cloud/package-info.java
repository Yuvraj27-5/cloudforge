/**
 * Multi-cloud deployment abstraction.
 *
 * <p>{@code CloudDeploymentProvider} with {@code deploy}, {@code rollback},
 * {@code getDeploymentStatus}, {@code getLogs}.
 *
 * <p>Implementations arrive per phase: LocalKubernetesProvider (Phase 9),
 * AwsDeploymentProvider (Phase 12), AzureDeploymentProvider (Phase 13).
 *
 * <p>The orchestration service must contain zero provider-specific branches. An
 * {@code if (provider == AWS)} in the orchestrator means the abstraction has failed.
 */
package com.cloudforge.backend.cloud;
