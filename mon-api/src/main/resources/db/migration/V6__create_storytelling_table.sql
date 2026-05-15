CREATE TYPE story_type AS ENUM ('CONFESSION', 'TESTIMONY', 'EXPERIENCE', 'ANONYMOUS');

CREATE TABLE storytelling (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     VARCHAR(36)     REFERENCES users(id) ON DELETE SET NULL,
    title       VARCHAR(200)    NOT NULL,
    content     TEXT,
    type        story_type      NOT NULL DEFAULT 'TESTIMONY',
    audio_url   TEXT,
    cover_url   TEXT,
    anonymous   BOOLEAN         NOT NULL DEFAULT FALSE,
    status      VARCHAR(20)     NOT NULL DEFAULT 'DRAFT',
    views       INTEGER         NOT NULL DEFAULT 0,
    likes       INTEGER         NOT NULL DEFAULT 0,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_storytelling_user_id ON storytelling(user_id);
CREATE INDEX idx_storytelling_type    ON storytelling(type);
CREATE INDEX idx_storytelling_status  ON storytelling(status);