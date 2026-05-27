---
project_name: "AI Dev Assistant Dashboard"
user_name: "iman.entezari"
date: 2026-05-27
sections_completed: ["technology_stack","critical_rules"]
existing_patterns_found: 6
---

# Project Context for AI Agents

_This file captures unobvious rules and patterns agents must follow when implementing code in this repository._

## Technology Stack & Versions

- Frontend: React ^18.3.1, TypeScript ^5.6.3, Vite. Node.js 20+ recommended.
- Backend: Java 21, Spring Boot 3.3.5 (parent in pom.xml), Maven build.
- Database: PostgreSQL (local via docker-compose). Flyway for migrations.
- Tooling: npm (frontend), Maven (backend), ESLint, TypeScript compiler (tsc).

## Critical Implementation Rules (high priority)

1. Database changes MUST use Flyway migrations. Add SQL migration files under backend/src/main/resources/db/migration and test locally before PR.
2. Do NOT commit secrets or API keys. Use .env.local (gitignored) or CI secret store. Verify .gitignore before adding local env files.
3. Backend behavior: when OPENAI_API_KEY is not set the service returns mock assistant responses — new features that depend on OpenAI must gate on config and include a graceful fallback.
4. Validate language/runtime versions before major changes (Java 21, Node 20+). Document any upgrade rationale in PR description.
5. Run linters and type checks before creating PRs: frontend `npm run lint` and `npm run build` (tsc -b); backend `mvn test`.
6. Use provided start/stop scripts for local stacks: `scripts\start-dev.ps1`, `scripts\stop-dev.ps1`, and `docker compose` for DB consistency.
7. Prefer small, focused PRs with clear acceptance criteria and automated tests where practical. Include migration and data-migration notes when altering schemas.
8. Avoid time estimates in messages; instead state scope and required follow-ups.

## Code Organization Notes

- Frontend code lives in `frontend/`; backend code in `backend/`. Place shared docs in `docs/`.
- Follow existing patterns: component-driven UI, Spring Boot REST controllers, Flyway migrations for schema.

## Agent Interaction Guidelines

- Show reasoning and tests before committing code changes.
- Run lint/type-check and unit tests locally; include commands used in PR checklist.
- When adding third-party services or keys, include explicit rollback and disablement steps.

## Next recommended actions

- Index docs for LLM consumption: run `bmad-index-docs`.
- Create or update PRD using `bmad-prd` once planning artifacts are ready.

