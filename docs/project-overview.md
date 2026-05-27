---
project_name: "AI Dev Assistant Dashboard"
generated_by: "bmad-document-project"
date: 2026-05-27
sections_completed: ["summary","tech_stack","quickstart","notes_for_agents"]
---

# Project Overview

A concise summary to orient contributors and AI agents.

## Summary

Full-stack developer productivity dashboard: React + TypeScript frontend (Vite), Spring Boot 3.3.5 backend (Java 21), PostgreSQL persistence with Flyway migrations. Optional OpenAI integration; mock assistant responses used when OPENAI_API_KEY is not set.

## Technology Stack (discovered)

- Frontend: React (>=18.x), TypeScript (>=5.x), Vite (dev)
- Backend: Java 21, Spring Boot 3.3.5, Spring Web, Spring Data JPA
- Database: PostgreSQL (recommended via docker-compose)
- Migrations: Flyway
- Dev tooling: npm (frontend), Maven (backend)

## Quick start / local run summary

- Recommended single-command start: `scripts\start-dev.ps1` (starts local PostgreSQL, backend, frontend)
- Frontend: `cd frontend && npm install && npm run dev` (serves at http://127.0.0.1:5173)
- Backend: `cd backend && mvn spring-boot:run` (API at http://localhost:8080)
- Check status: `scripts\status-dev.ps1`; stop: `scripts\stop-dev.ps1`

## Notes for AI agents / implementers

- Use database migrations (Flyway) for schema changes — do not modify DB directly.
- The app falls back to mock AI responses when OPENAI_API_KEY is absent — check backend config before adding OpenAI-dependent features.
- Follow repository structure: `backend/`, `frontend/`, `docs/`, `scripts/`, `docker-compose.yml`.
- Confirm language/tooling versions (Java, Node) against README and ci/build configs before making breaking changes.

## Next recommended docs

- Generate `project-context.md` (LLM-optimized rules for agents) using `bmad-generate-project-context` (recommended).
- Create or update a docs index with `bmad-index-docs`.

