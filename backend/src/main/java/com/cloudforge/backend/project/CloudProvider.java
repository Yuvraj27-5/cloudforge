package com.cloudforge.backend.project;

/**
 * Deployment target. Stored as a string, never an ordinal — see Project.
 * Adding a provider here is a code change, not a database migration.
 */
public enum CloudProvider {
    LOCAL_KUBERNETES,
    AWS,
    AZURE
}
