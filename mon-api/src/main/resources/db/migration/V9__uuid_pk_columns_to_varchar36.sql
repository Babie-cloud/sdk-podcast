-- Hibernate maps @Id String + GenerationType.UUID to VARCHAR, not PostgreSQL UUID.
ALTER TABLE password_reset_tokens ALTER COLUMN id DROP DEFAULT;
ALTER TABLE password_reset_tokens ALTER COLUMN id TYPE VARCHAR(36) USING id::text;

ALTER TABLE writings ALTER COLUMN id DROP DEFAULT;
ALTER TABLE writings ALTER COLUMN id TYPE VARCHAR(36) USING id::text;

ALTER TABLE storytelling ALTER COLUMN id DROP DEFAULT;
ALTER TABLE storytelling ALTER COLUMN id TYPE VARCHAR(36) USING id::text;

ALTER TABLE refresh_tokens ALTER COLUMN id DROP DEFAULT;
ALTER TABLE refresh_tokens ALTER COLUMN id TYPE VARCHAR(36) USING id::text;
