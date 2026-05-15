CREATE TYPE writing_type AS ENUM ('POEM', 'STORY', 'CONFESSION', 'TESTIMONY');

CREATE TABLE writings (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     VARCHAR(36)     NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title       VARCHAR(200)    NOT NULL,
    content     TEXT            NOT NULL,
    type        writing_type    NOT NULL DEFAULT 'POEM',
    audio_url   TEXT,
    cover_url   TEXT,
    status      VARCHAR(20)     NOT NULL DEFAULT 'DRAFT',
    views       INTEGER         NOT NULL DEFAULT 0,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_writings_user_id ON writings(user_id);
CREATE INDEX idx_writings_type    ON writings(type);
CREATE INDEX idx_writings_status  ON writings(status);