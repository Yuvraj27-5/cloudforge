-- One assessment per attempt to score a deployment. Not unique on deployment_id:
-- a deployment may be rescored after metrics change, and the earlier verdict must
-- survive so "why was this blocked at 2am" stays answerable.
CREATE TABLE risk_assessments (
    id                  UUID          PRIMARY KEY,
    deployment_id       UUID          NOT NULL,

    risk_score          INTEGER,
    risk_level          VARCHAR(20),
    decision            VARCHAR(30)   NOT NULL,
    confidence          NUMERIC(4, 3),

    -- Display data from the model. Stored as JSON because the shape belongs to the
    -- risk provider, and normalising it would couple our schema to whichever model
    -- happens to be plugged in.
    factors             TEXT,

    -- Provenance. Without these, a score from a synthetic model is
    -- indistinguishable from a validated one six months later.
    model_version       VARCHAR(60),
    trained_on          VARCHAR(40),
    provider            VARCHAR(60)   NOT NULL,

    -- Set when the decision came from a hard gate or a failure, not the model.
    override_reason     VARCHAR(500),

    assessed_at         TIMESTAMPTZ   NOT NULL,

    CONSTRAINT fk_risk_assessments_deployment
        FOREIGN KEY (deployment_id) REFERENCES deployments (id) ON DELETE CASCADE
);

CREATE INDEX idx_risk_assessments_deployment ON risk_assessments (deployment_id, assessed_at DESC);
CREATE INDEX idx_risk_assessments_assessed_at ON risk_assessments (assessed_at DESC);
