# Sprint 1 - Authentication and Local MVP Wrap-Up

This file summarizes the Sprint 1 work that moved the app from a mostly broken local shell into a functional authenticated MVP.

## Status Summary

| ID | Title | Status | Notes |
| --- | --- | --- | --- |
| sprint1-auth-decision | Decide auth approach (JWT vs session) | done | Documented in `docs/auth-decision.md`. |
| sprint1-auth-backend | Implement auth endpoints and JWT handling | done | Register, login, refresh, logout, `/auth/me`, profile update, account deletion. |
| sprint1-auth-frontend | Frontend login/logout and protected routes | done | Login, registration, protected app routes, sidebar auth state, account page. |
| sprint1-auth-migrations | Add Flyway migrations for users and ownership | done | Users table, refresh tokens, and nullable ownership columns for tasks/logs/chat. |
| sprint1-auth-tests | Auth and user isolation tests | done | Backend integration tests cover auth, refresh/logout, token expiry, and per-user data. |
| sprint1-ci | Finalize CI checks for PRs | done | GitHub Actions runs backend tests, frontend lint, and TypeScript checks. |
| sprint1-local-dev | Local reset and dev user workflow | done | `reset-dev-db.ps1`, `create-dev-user.ps1`, and local docs updated. |
| sprint1-account-management | Account management polish | done | Profile editing, password change verification, account deletion confirmation. |
| sprint1-log-delete-fix | Delete logs with attached chat history | done | Log deletion clears attached chat records after ownership check. |
| sprint1-dashboard-api | Backend: agent list and run endpoints | deferred | Original idea deferred; current app focuses on task/log/assistant MVP. |
| sprint1-dashboard-frontend | Frontend: agent list UI and run controls | deferred | Original idea deferred pending clearer product direction. |
| sprint1-dashboard-logs | Persist agent run logs and retrieve | deferred | Current persisted logs are user-created error logs, not agent run logs. |
| sprint1-observability | Add structured logging and metrics endpoint | deferred | Useful later, not required for the current local MVP. |
| sprint1-smoke-tests | Add smoke E2E tests for core flows | deferred | Recommended next quality step before expanding the app. |

## Completed User-Facing Outcomes

- Users can register and log in with username/password.
- The login page shows clear messages for missing accounts and wrong passwords.
- Protected pages require authentication.
- The sidebar shows the logged-in user clearly and links to the account page.
- Users can update username and email.
- Users can change password only after verifying their current password.
- Users can delete their account after confirming the action.
- Tasks, error logs, and chat history are private per account.
- Local seeded data can be claimed by the development user instead of being shared globally.
- Error logs can be deleted even when assistant chat history is attached.

## BMAD Notes

Sprint planning converted the early ticket table into BMAD-compatible epics and stories under `_bmad-output/planning-artifacts/epics.md`.

The first two formal BMAD stories were completed as story files:

- `_bmad-output/implementation-artifacts/1-1-decide-authentication-approach.md`
- `_bmad-output/implementation-artifacts/1-2-implement-auth-endpoints-and-jwt-handling.md`

Follow-on work was completed through direct implementation and review loops rather than creating a separate BMAD story file for every small UI and bug-fix task. The sprint status file has been reconciled to show the current high-level state.

## Deferred Work

- Add E2E smoke tests for login, task creation, log creation/deletion, and account deletion.
- Add task due dates and tags.
- Improve assistant conversation organization.
- Add streaming assistant responses.
- Decide whether agent-list/run-control features still belong in this product.
