CREATE TABLE episodes (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    podcast_id      UUID            NOT NULL REFERENCES podcasts(id) ON DELETE CASCADE,
    title           VARCHAR(200)    NOT NULL,
    description     TEXT,
    audio_url       TEXT,
    duration        INTEGER         DEFAULT 0,
    file_size       BIGINT          DEFAULT 0,
    episode_number  INTEGER         DEFAULT 1,
    status          VARCHAR(20)     NOT NULL DEFAULT 'DRAFT',
    published_at    TIMESTAMP,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_episodes_podcast_id ON episodes(podcast_id);
CREATE INDEX idx_episodes_status     ON episodes(status);