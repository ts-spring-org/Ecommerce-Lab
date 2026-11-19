--liquibase formatted sql

--changeset identity-team:003-create-auth-rate-limits
CREATE TABLE auth_rate_limits (
    bucket_key VARCHAR(255) PRIMARY KEY,
    window_started_at TIMESTAMPTZ NOT NULL,
    request_count INTEGER NOT NULL,
    blocked_until TIMESTAMPTZ,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT ck_auth_rate_limits_count CHECK (request_count >= 0)
);

CREATE INDEX idx_auth_rate_limits_updated_at ON auth_rate_limits(updated_at);

--rollback DROP TABLE auth_rate_limits;
