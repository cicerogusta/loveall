ALTER TABLE users ADD COLUMN pcd_opt_in INTEGER NOT NULL DEFAULT 0 CHECK (pcd_opt_in IN (0, 1));
ALTER TABLE users ADD COLUMN pcd_consent_at TEXT;
ALTER TABLE users ADD COLUMN pcd_consent_version TEXT;
CREATE INDEX users_pcd_discovery ON users(pcd_opt_in, is_active, is_banned);
