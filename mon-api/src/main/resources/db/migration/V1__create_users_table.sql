CREATE TABLE users (
    id          VARCHAR(36)         PRIMARY KEY,
    name        VARCHAR(100)        NOT NULL,
    prenom      VARCHAR(100)        NOT NULL,
    username    VARCHAR(150)        NOT NULL,
    email       VARCHAR(255)        NOT NULL UNIQUE,
    password    VARCHAR(255)        NOT NULL,
    role        VARCHAR(20)         NOT NULL DEFAULT 'USER',
    avatar_url  TEXT,
    bio         TEXT,
    anonymous   BOOLEAN             NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP           NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);