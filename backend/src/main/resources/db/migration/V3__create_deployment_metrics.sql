-- Pipeline output attached to a deployment. Every column is the raw measurement
-- taken by CI; the risk model in Phase 7 reads this table rather than recomputing
-- anything, so the features it scores are exactly what the pipeline observed.
CREATE TABLE deployment_metrics (
    id                        UUID          PRIMARY KEY,
    deployment_id             UUID          NOT NULL UNIQUE,

    -- Change size
    files_changed             INTEGER,
    lines_added               INTEGER,
    lines_deleted             INTEGER,

    -- Test outcomes
    test_pass_rate            NUMERIC(5, 2),
    test_coverage             NUMERIC(5, 2),

    -- Code quality. Nullable: filled once static analysis is wired up.
    code_complexity           INTEGER,
    code_smells               INTEGER,
    bugs                      INTEGER,
    security_hotspots         INTEGER,

    -- Security scan
    critical_vulnerabilities  INTEGER,
    high_vulnerabilities      INTEGER,
    medium_vulnerabilities    INTEGER,
    low_vulnerabilities       INTEGER,

    source                    VARCHAR(60)   NOT NULL,
    recorded_at               TIMESTAMPTZ   NOT NULL,

    CONSTRAINT fk_deployment_metrics_deployment
        FOREIGN KEY (deployment_id) REFERENCES deployments (id) ON DELETE CASCADE
);
