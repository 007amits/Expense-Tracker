-- Add createdAt column to users table
ALTER TABLE users ADD COLUMN created_at TIMESTAMP;

-- Set default value for existing users
UPDATE users SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL;
