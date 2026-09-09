CREATE TABLE projects (
    id                UUID         PRIMARY KEY,
    name              VARCHAR(120) NOT NULL,
    repository_url    VARCHAR(500) NOT NULL,
    default_branch    VARCHAR(120) NOT NULL DEFAULT 'main',
    cloud_provider    VARCHAR(30)  NOT NULL,
    environment       VARCHAR(30)  NOT NULL,
    description       VARCHAR(500),
    created_at        TIMESTAMPTZ  NOT NULL,
    updated_at        TIMESTAMPTZ  NOT NULL,

    CONSTRAINT uq_projects_name UNIQUE (name)
);

CREATE INDEX idx_projects_created_at ON projects (created_at DESC);
