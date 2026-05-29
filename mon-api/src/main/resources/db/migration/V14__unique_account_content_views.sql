CREATE TABLE writing_account_views (
    writing_id VARCHAR(36) NOT NULL REFERENCES writings(id) ON DELETE CASCADE,
    user_id    VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    viewed_at  TIMESTAMP   NOT NULL DEFAULT NOW(),
    PRIMARY KEY (writing_id, user_id)
);

CREATE TABLE storytelling_account_views (
    storytelling_id VARCHAR(36) NOT NULL REFERENCES storytelling(id) ON DELETE CASCADE,
    user_id         VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    viewed_at       TIMESTAMP   NOT NULL DEFAULT NOW(),
    PRIMARY KEY (storytelling_id, user_id)
);
