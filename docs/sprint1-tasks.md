# Sprint 1 - Ticket List

This file summarizes the Sprint 1 tickets, current status, owners, and estimates.


| ID | Title | Status | Owner | Estimate |
| --- | --- | --- | --- | --- |
| sprint1-auth-backend | Implement auth endpoints and JWT handling | done | Backend | medium |
| sprint1-auth-decision | Decide auth approach (JWT vs session) | done | Architect | small |
| sprint1-auth-frontend | Frontend login/logout and protected routes | in_progress | Frontend | medium |
| sprint1-auth-migrations | Add Flyway migrations for users and ownership | in_progress | Backend | small |
| sprint1-auth-tests | Auth unit and integration tests | in_progress | QA | small |
| sprint1-ci | Finalize CI checks for PRs | in_progress | DevOps | small |
| sprint1-dashboard-api | Backend: agent list and run endpoints | in_progress | Backend | medium |
| sprint1-dashboard-frontend | Frontend: agent list UI and run controls | in_progress | Frontend | medium |
| sprint1-dashboard-logs | Persist agent run logs and retrieve | in_progress | Backend | small |
| sprint1-observability | Add structured logging and metrics endpoint | in_progress | DevOps | medium |
| sprint1-smoke-tests | Add smoke E2E tests for core flows | in_progress | QA | small |


Files changed by this PR:
- backend/src/main/resources/db/migration/V3__create_users_table.sql
- docs/sprint1-tasks.md

Notes:
- Migration V3 creates users table and adds nullable user_id references to tasks, error_logs, and chat_messages. Foreign keys are not enforced initially to keep rollout safe.
- All sprint1 tickets are now marked in_progress in the session tracker.
