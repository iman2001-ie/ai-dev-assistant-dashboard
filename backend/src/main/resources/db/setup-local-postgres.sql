-- Run these first commands while connected to the default "postgres" database.
CREATE USER dashboard_user WITH PASSWORD 'dashboard_password';
CREATE DATABASE ai_dev_dashboard OWNER dashboard_user;
GRANT ALL PRIVILEGES ON DATABASE ai_dev_dashboard TO dashboard_user;

-- Run these commands while connected to the "ai_dev_dashboard" database.
GRANT ALL ON SCHEMA public TO dashboard_user;
GRANT CREATE ON SCHEMA public TO dashboard_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO dashboard_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO dashboard_user;
