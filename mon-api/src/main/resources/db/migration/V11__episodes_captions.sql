-- Paroles synchronisées (JSON) — optionnel, servi tel quel au frontend.
ALTER TABLE episodes ADD COLUMN IF NOT EXISTS captions TEXT;
