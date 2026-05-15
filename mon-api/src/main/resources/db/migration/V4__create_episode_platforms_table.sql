CREATE TABLE episode_platforms (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    episode_id  VARCHAR(36)     NOT NULL REFERENCES episodes(id) ON DELETE CASCADE,
    platform    VARCHAR(50)     NOT NULL,
    url         TEXT,
    created_at  TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ep_platforms_episode_id ON episode_platforms(episode_id);