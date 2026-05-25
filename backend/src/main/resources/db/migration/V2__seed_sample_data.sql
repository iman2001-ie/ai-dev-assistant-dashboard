INSERT INTO tasks (title, description, status, priority, created_at, updated_at)
VALUES
    ('Wire dashboard summary cards', 'Show totals for tasks, unresolved logs, and saved AI conversations.', 'IN_PROGRESS', 'HIGH', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Review REST endpoint naming', 'Keep endpoint paths beginner-friendly and consistent.', 'TODO', 'MEDIUM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Document local setup', 'Make README setup steps clear for a first full-stack run.', 'DONE', 'LOW', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO error_logs (title, content, source, resolved, created_at)
VALUES
    ('React state update warning', 'Warning: Cannot update a component while rendering a different component.', 'frontend', FALSE, CURRENT_TIMESTAMP),
    ('Database connection refused', 'org.postgresql.util.PSQLException: Connection to localhost:5432 refused.', 'backend', FALSE, CURRENT_TIMESTAMP);
