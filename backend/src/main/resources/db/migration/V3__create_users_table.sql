-- Flyway migration: create users table
-- V3__create_users_table.sql

CREATE TABLE IF NOT EXISTS users (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(100) NOT NULL UNIQUE,
  email VARCHAR(255) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  refresh_token VARCHAR(255),
  created_at TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- Add user_id columns to existing domain tables where appropriate. These are nullable for initial rollout.
-- If tables do not exist yet, these ALTER statements will be ignored by Flyway at runtime; adjust as needed.

ALTER TABLE IF EXISTS tasks ADD COLUMN IF NOT EXISTS user_id BIGINT;
ALTER TABLE IF EXISTS error_logs ADD COLUMN IF NOT EXISTS user_id BIGINT;
ALTER TABLE IF EXISTS chat_messages ADD COLUMN IF NOT EXISTS user_id BIGINT;

-- Optional: add foreign key constraints if the target tables exist and you want enforcement.
-- ALTER TABLE IF EXISTS tasks ADD CONSTRAINT fk_tasks_user FOREIGN KEY (user_id) REFERENCES users(id);
-- ALTER TABLE IF EXISTS error_logs ADD CONSTRAINT fk_logs_user FOREIGN KEY (user_id) REFERENCES users(id);
-- ALTER TABLE IF EXISTS chat_messages ADD CONSTRAINT fk_chat_user FOREIGN KEY (user_id) REFERENCES users(id);

