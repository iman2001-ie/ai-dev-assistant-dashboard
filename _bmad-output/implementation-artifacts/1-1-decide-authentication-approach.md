---
baseline_commit: 690e6e86563b20ba104438ccdbbc1466ba474f9e
---

# Story 1.1: Decide Authentication Approach

Status: done

## Story

As a project maintainer,
I want to choose and document the JWT versus session approach,
so that the implementation follows one clear authentication model.

## Acceptance Criteria

1. Given the project needs user login and protected routes, when the authentication approach is selected, then the decision is documented in project docs or implementation notes.
2. Given the authentication decision is documented, when backend and frontend work is reviewed, then the implementation follows the selected approach.

## Tasks / Subtasks

- [x] Document the selected authentication model. (AC: #1)
  - [x] State whether the MVP uses JWTs or server-backed sessions.
  - [x] Capture access token, refresh token, logout, and protected API behavior.
- [x] Cross-check the documented decision against existing implementation. (AC: #2)
  - [x] Verify backend Spring Security session policy and JWT filter behavior.
  - [x] Verify frontend token storage and refresh behavior.
- [x] Add the decision document to the docs index. (AC: #1)

### Review Findings

- [x] [Review][Patch] BMAD sprint status marks uncreated stories as in-progress [_bmad-output/implementation-artifacts/sprint-status.yaml:46]
- [x] [Review][Patch] Source sprint tracker still marks Story 1.1 as in_progress [docs/sprint1-tasks.md:9]
- [x] [Review][Defer] README API overview omits auth endpoints and Bearer-token requirements [README.md:148] — deferred, pre-existing
- [x] [Review][Defer] README development notes do not expose the docs index or auth decision [README.md:182] — deferred, pre-existing
- [x] [Review][Defer] README roadmap still frames authentication as future work [README.md:190] — deferred, pre-existing

## Dev Notes

- Existing backend security is stateless and uses `JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter`.
- `SecurityConfig` disables CSRF, sets `SessionCreationPolicy.STATELESS`, permits register/login/refresh/health/info, and protects other routes.
- `AuthService` issues JWT access tokens and rotates opaque refresh tokens on register, login, and refresh.
- `AuthService.logout` clears the stored refresh token for the authenticated user.
- Frontend auth stores `token` and `refreshToken` in `localStorage`, retries a failed authenticated request after refresh, and clears tokens on logout.
- This story is a documentation/decision story; no production code behavior should change.

### Project Structure Notes

- Auth decision docs belong in `docs/` with other contributor-facing project documents.
- `docs/index.md` should list new docs so agents and contributors can find them.

### References

- [Source: _bmad-output/planning-artifacts/epics.md#Story-1.1-Decide-Authentication-Approach]
- [Source: backend/src/main/java/com/example/aidevdashboard/config/SecurityConfig.java]
- [Source: backend/src/main/java/com/example/aidevdashboard/config/JwtAuthenticationFilter.java]
- [Source: backend/src/main/java/com/example/aidevdashboard/service/AuthService.java]
- [Source: backend/src/main/java/com/example/aidevdashboard/service/JwtUtil.java]
- [Source: frontend/src/services/auth.ts]
- [Source: frontend/src/contexts/AuthContext.tsx]

## Dev Agent Record

### Agent Model Used

GPT-5 Codex

### Debug Log References

- Read BMAD dev-story and create-story workflow instructions.
- Confirmed the story file was missing from `_bmad-output/implementation-artifacts`.
- Created the story file from the converted Sprint 1 epic artifact.
- Reviewed backend and frontend auth implementation before documenting the decision.
- Ran backend regression tests: `mvn test` from `backend`.
- Ran frontend lint: `npm run lint` from `frontend`.
- Ran frontend TypeScript validation: `npx tsc -b` from `frontend`.

### Completion Notes List

- Added `docs/auth-decision.md` documenting the MVP decision to use stateless JWT access tokens with server-side refresh tokens.
- Documented the current auth API contract, session policy, token rotation behavior, logout behavior, and follow-up hardening notes.
- Added the new auth decision document to `docs/index.md`.
- No production code was changed for this decision story.
- Validation passed: backend tests, frontend lint, and frontend TypeScript build.
- Code review patches applied: uncreated BMAD stories reset to backlog, source sprint tracker synced, and Story 1.1 marked done.

### File List

- docs/auth-decision.md
- docs/index.md
- _bmad-output/implementation-artifacts/1-1-decide-authentication-approach.md
- _bmad-output/implementation-artifacts/sprint-status.yaml

### Change Log

- 2026-05-28: Created auth decision documentation and marked Story 1.1 ready for review.
- 2026-05-28: Applied code review patches and marked Story 1.1 done.
