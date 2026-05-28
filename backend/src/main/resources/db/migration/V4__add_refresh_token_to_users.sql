-- Add refresh_token column to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS refresh_token VARCHAR(255);
