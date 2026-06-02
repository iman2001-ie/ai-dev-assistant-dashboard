---
project_name: "AI Dev Assistant Dashboard"
generated_by: "bmad-document-project"
updated_by: "Codex wrap-up pass"
date: 2026-05-27
last_updated: 2026-06-02
sections_completed: ["summary","tech_stack","quickstart","notes_for_agents","current_status"]
---

# Project Overview

A concise summary to orient contributors and AI agents.

## Summary

Full-stack developer productivity dashboard: React + TypeScript frontend (Vite), Spring Boot 3.3.5 backend (Java 21), PostgreSQL persistence with Flyway migrations, and JWT authentication. Optional OpenAI integration; mock assistant responses are used when OPENAI_API_KEY is not set.

## Current Status

The local MVP is functional:

- Users can register, log in, refresh tokens, log out, edit profile details, change passwords, and delete their account.
- Tasks, error logs, and chat history are scoped per authenticated user.
- Seeded sample data can be claimed by a local development user through `scripts\create-dev-user.ps1`.
- The assistant supports general chat and error-log-specific chat history.
- GitHub CI runs backend tests, frontend lint, and TypeScript checks.

## Technology Stack (discovered)

- Frontend: React 18, TypeScript 5, Vite, React Router
- Backend: Java 21, Spring Boot 3.3.5, Spring Web, Spring Security, Spring Data JPA
- Database: PostgreSQL (recommended via docker-compose)
- Migrations: Flyway
- Auth: JWT access tokens, server-side refresh tokens, BCrypt password hashing
- Dev tooling: npm (frontend), Maven (backend)

## Quick start / local run summary

- Recommended single-command start: `scripts\start-dev.ps1` (starts local PostgreSQL, backend, frontend)
- Frontend: `cd frontend && npm install && npm run dev` (serves at http://127.0.0.1:5173)
- Backend: `cd backend && mvn spring-boot:run` (API at http://localhost:8080)
- Default development user: run `scripts\create-dev-user.ps1`, then log in as `testuser` / `Password123!`
- Check status: `scripts\status-dev.ps1`; stop: `scripts\stop-dev.ps1`

## Notes for AI agents / implementers

- Use database migrations (Flyway) for schema changes — do not modify DB directly.
- The app falls back to mock AI responses when OPENAI_API_KEY is absent — check backend config before adding OpenAI-dependent features.
- Follow repository structure: `backend/`, `frontend/`, `docs/`, `scripts/`, `docker-compose.yml`.
- Confirm language/tooling versions (Java, Node) against README and ci/build configs before making breaking changes.
- Keep user-owned data isolated by `user_id` when adding task, log, or chat behavior.
- Treat `_bmad-output/` as project planning history, not runtime output.

## Next recommended docs

- Refresh README screenshots and demo assets when the UI is ready for a new public walkthrough.
- Add deployment notes when a production target is selected.

