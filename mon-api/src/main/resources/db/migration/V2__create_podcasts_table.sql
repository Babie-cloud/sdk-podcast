CREATE TABLE podcasts (
    id          VARCHAR(36)         PRIMARY KEY,
    user_id     VARCHAR(36)         NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title       VARCHAR(200)        NOT NULL,
    description TEXT,
    cover_url   TEXT,
    category    VARCHAR(100),
    language    VARCHAR(10)         NOT NULL DEFAULT 'fr',
    status      VARCHAR(20)         NOT NULL DEFAULT 'DRAFT',
    created_at  TIMESTAMP           NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP           NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_podcasts_user_id ON podcasts(user_id);
CREATE INDEX idx_podcasts_status  ON podcasts(status);