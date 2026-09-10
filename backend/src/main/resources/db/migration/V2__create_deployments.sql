CREATE TABLE deployments (
    id                UUID         PRIMARY KEY,
    project_id        UUID         NOT NULL,
    commit_sha        VARCHAR(40)  NOT NULL,
    commit_message    VARCHAR(500),
    triggered_by      VARCHAR(120),
    status            VARCHAR(30)  NOT NULL,
    correlation_id    UUID         NOT NULL,
    started_at        TIMESTAMPTZ,
    completed_at      TIMESTAMPTZ,
    created_at        TIMESTAMPTZ  NOT NULL,
    updated_at        TIMESTAMPTZ  NOT NULL,

    CONSTRAINT fk_deployments_project
        FOREIGN KEY (project_id) REFERENCES projects (id) ON DELETE CASCADE
);

-- The two queries the UI actually makes: a project's history, and the global
-- feed. Both are newest-first.
CREATE INDEX idx_deployments_project_created ON deployments (project_id, created_at DESC);
CREATE INDEX idx_deployments_created_at ON deployments (created_at DESC);
CREATE INDEX idx_deployments_status ON deployments (status);
CREATE INDEX idx_deployments_correlation ON deployments (correlation_id);

CREATE TABLE deployment_events (
    id                UUID         PRIMARY KEY,
    deployment_id     UUID         NOT NULL,
    event_type        VARCHAR(30)  NOT NULL,
    from_status       VARCHAR(30),
    to_status         VARCHAR(30),
    reason            VARCHAR(500),
    actor             VARCHAR(120),
    occurred_at       TIMESTAMPTZ  NOT NULL,

    CONSTRAINT fk_deployment_events_deployment
        FOREIGN KEY (deployment_id) REFERENCES deployments (id) ON DELETE CASCADE
);

CREATE INDEX idx_deployment_events_deployment ON deployment_events (deployment_id, occurred_at);
