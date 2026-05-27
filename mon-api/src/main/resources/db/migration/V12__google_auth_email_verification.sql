ALTER TABLE users
    ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT TRUE,
    ADD COLUMN auth_provider VARCHAR(30) NOT NULL DEFAULT 'LOCAL',
    ADD COLUMN google_subject VARCHAR(255);

CREATE UNIQUE INDEX idx_users_google_subject
    ON users(google_subject)
    WHERE google_subject IS NOT NULL;

CREATE TABLE email_verification_tokens (
    id          VARCHAR(36) PRIMARY KEY,
    user_id     VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token       VARCHAR(120) NOT NULL UNIQUE,
    expires_at  TIMESTAMP   NOT NULL,
    used_at     TIMESTAMP,
    created_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_email_verification_tokens_user ON email_verification_tokens(user_id);
CREATE INDEX idx_email_verification_tokens_token ON email_verification_tokens(token);
