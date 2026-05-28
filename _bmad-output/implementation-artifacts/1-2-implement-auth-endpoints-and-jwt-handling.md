---
baseline_commit: 690e6e86563b20ba104438ccdbbc1466ba474f9e
---

# Story 1.2: Implement Auth Endpoints and JWT Handling

Status: done

## Story

As an app user,
I want backend endpoints for authentication and token handling,
so that I can securely register, log in, refresh, and log out.

## Acceptance Criteria

1. Given the backend API is running, when a valid user registers, then the backend creates the user, hashes the password, returns an access token, username, and refresh token.
2. Given an existing user, when valid login credentials are submitted, then the backend returns an access token, username, and a newly rotated refresh token.
3. Given invalid login credentials, when login is attempted, then the backend returns a clear error response without issuing tokens.
4. Given a valid refresh token, when refresh is requested, then the backend rotates the refresh token and returns a new access token and refresh token.
5. Given an authenticated user, when logout is requested, then the stored refresh token is cleared and future refresh with that token fails.
6. Given protected API routes, when requests omit a valid Bearer token, then those routes reject unauthenticated access.

## Tasks / Subtasks

- [x] Verify and tighten auth request/response behavior. (AC: #1, #2, #3)
  - [x] Confirm `POST /api/auth/register` rejects duplicate usernames with a clear error response.
  - [x] Confirm `POST /api/auth/login` rejects invalid credentials with a clear error response.
  - [x] Confirm successful register/login responses include `token`, `username`, and `refreshToken`.
- [x] Verify refresh and logout token lifecycle. (AC: #4, #5)
  - [x] Confirm refresh token rotation invalidates the old refresh token.
  - [x] Confirm logout clears the stored refresh token for the authenticated user.
  - [x] Confirm refresh with a logged-out token fails clearly.
- [x] Verify JWT authentication for protected routes. (AC: #6)
  - [x] Confirm protected routes require `Authorization: Bearer <jwt>`.
  - [x] Confirm invalid or expired JWTs do not authenticate the request.
  - [x] Confirm public auth routes remain accessible without a token.
- [x] Add or update focused backend tests for any uncovered behavior. (AC: #1-#6)
  - [x] Run `mvn test` from `backend`.
  - [x] Record any added or updated tests in the Dev Agent Record.

### Review Findings

- [x] [Review][Patch] Missing refresh-token null guard can reissue tokens after logout [backend/src/main/java/com/example/aidevdashboard/controller/AuthController.java:32]
- [x] [Review][Patch] Duplicate email registration falls through to database error [backend/src/main/java/com/example/aidevdashboard/service/AuthService.java:27]
- [x] [Review][Patch] Broad IllegalArgumentException handler can misclassify server bugs as client errors [backend/src/main/java/com/example/aidevdashboard/config/GlobalExceptionHandler.java:40]

## Dev Notes

- Story 1.1 selected stateless JWT access tokens plus server-side refresh tokens as the MVP auth approach.
- Existing implementation already includes `AuthController`, `AuthService`, `JwtUtil`, `JwtAuthenticationFilter`, and `SecurityConfig`.
- Treat this as a verification-and-hardening story unless a gap is found. Do not replace the existing auth architecture without a specific failing acceptance criterion.
- Keep the implementation beginner-friendly. Prefer direct service/controller code and focused tests over new abstractions.
- Do not add new dependencies without approval.
- Keep schema work in Flyway migrations. If auth persistence changes are needed, coordinate with Story 1.4 rather than changing schema directly here.
- Do not expose secrets. `app.jwt.secret` has a default for local development, but production configuration should use environment-specific secret management.

### Current Implementation Snapshot

- `AuthController` exposes `POST /api/auth/register`, `/login`, `/refresh`, and `/logout`.
- `AuthService.register` rejects duplicate usernames, hashes passwords with `BCryptPasswordEncoder`, stores a UUID refresh token, and returns a JWT.
- `AuthService.login` validates credentials, rotates the UUID refresh token, and returns a JWT.
- `AuthService.refresh` looks up the submitted refresh token, rotates it, and returns a new JWT and refresh token.
- `AuthService.logout` clears the stored refresh token for the authenticated username.
- `JwtUtil` signs HMAC JWTs with `app.jwt.secret` and applies `app.jwt.expiration-seconds`.
- `JwtAuthenticationFilter` reads `Authorization: Bearer <token>`, validates the JWT, finds the user, and sets request authentication.
- `SecurityConfig` is stateless, disables CSRF for the API flow, permits auth register/login/refresh plus actuator health/info, protects logout, and authenticates all other routes.

### Existing Test Coverage

- `AuthControllerIntegrationTest.java`
- `AuthRefreshLogoutIntegrationTest.java`
- `AuthTokenExpiryIntegrationTest.java`
- `AuthPerUserTaskIntegrationTest.java`

Review these before adding tests to avoid duplicate coverage.

### Project Structure Notes

- Backend auth code lives under `backend/src/main/java/com/example/aidevdashboard/`.
- Backend tests live under `backend/src/test/java/com/example/aidevdashboard/`.
- Keep controller behavior consistent with `GlobalExceptionHandler`.

### References

- [Source: _bmad-output/planning-artifacts/epics.md#Story-1.2-Implement-Auth-Endpoints-and-JWT-Handling]
- [Source: _bmad-output/implementation-artifacts/1-1-decide-authentication-approach.md]
- [Source: docs/auth-decision.md]
- [Source: backend/src/main/java/com/example/aidevdashboard/controller/AuthController.java]
- [Source: backend/src/main/java/com/example/aidevdashboard/service/AuthService.java]
- [Source: backend/src/main/java/com/example/aidevdashboard/service/JwtUtil.java]
- [Source: backend/src/main/java/com/example/aidevdashboard/config/JwtAuthenticationFilter.java]
- [Source: backend/src/main/java/com/example/aidevdashboard/config/SecurityConfig.java]
- [Source: backend/src/test/java/com/example/aidevdashboard/controller/AuthControllerIntegrationTest.java]
- [Source: backend/src/test/java/com/example/aidevdashboard/controller/AuthRefreshLogoutIntegrationTest.java]
- [Source: backend/src/test/java/com/example/aidevdashboard/controller/AuthTokenExpiryIntegrationTest.java]
- [Source: backend/src/test/java/com/example/aidevdashboard/controller/AuthPerUserTaskIntegrationTest.java]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Created from BMAD sprint backlog entry `1-2-implement-auth-endpoints-and-jwt-handling`.
- Loaded Epic 1 context, completed Story 1.1, auth decision doc, current auth controller/service/JWT/filter code, and existing auth tests.
- Started dev-story implementation from baseline commit `690e6e86563b20ba104438ccdbbc1466ba474f9e`.
- Red phase: focused auth tests failed because duplicate registration and invalid login returned `500 INTERNAL_SERVER_ERROR` instead of `400 BAD_REQUEST`.
- Green phase: added `IllegalArgumentException` handling and reran focused auth tests successfully.
- Regression gate: ran full backend `mvn test` successfully.
- Code-review patch gate: reran focused auth tests with `mvn "-Dtest=AuthControllerIntegrationTest,AuthRefreshLogoutIntegrationTest" test` successfully.
- Code-review regression gate: reran full backend `mvn test` successfully.

### Completion Notes List

- Story file created with repo-specific implementation context and acceptance criteria.
- Sprint status updated from `backlog` to `ready-for-dev` for Story 1.2.
- Added backend coverage for duplicate username registration, invalid login, refresh token rotation invalidating the old token, public auth route access, invalid bearer token rejection, and auth response refresh-token fields.
- Added a focused `IllegalArgumentException` handler so expected auth validation failures return `400 BAD_REQUEST` with the service message.
- Full backend test suite passed: 14 tests, 0 failures.
- Code review patches applied: added explicit auth exception handling, duplicate email validation, refresh token blank/null rejection, and focused regression coverage.
- Full backend test suite passed after review patches: 15 tests, 0 failures.

### File List

- _bmad-output/implementation-artifacts/1-2-implement-auth-endpoints-and-jwt-handling.md
- _bmad-output/implementation-artifacts/sprint-status.yaml
- backend/src/main/java/com/example/aidevdashboard/config/GlobalExceptionHandler.java
- backend/src/main/java/com/example/aidevdashboard/service/AuthException.java
- backend/src/main/java/com/example/aidevdashboard/service/AuthService.java
- backend/src/test/java/com/example/aidevdashboard/controller/AuthControllerIntegrationTest.java
- backend/src/test/java/com/example/aidevdashboard/controller/AuthRefreshLogoutIntegrationTest.java
- docs/sprint1-tasks.md

### Change Log

- 2026-05-28: Created Story 1.2 and marked it ready for development.
- 2026-05-28: Implemented auth endpoint hardening, expanded backend auth tests, and marked Story 1.2 ready for review.
- 2026-05-28: Applied code-review patches and marked Story 1.2 done.
