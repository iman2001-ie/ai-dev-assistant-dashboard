# Agent Instructions

These instructions are for AI coding agents working in this repository.

## Scope

- Only access files inside this project directory.
- Do not inspect parent folders.
- Do not access unrelated files.
- Do not request or use company/internal information.
- Use only mock or sample data.
- Ask before running destructive commands.

## Project Context

This is a learning-oriented full-stack app:

- `frontend/`: React, TypeScript, Vite
- `backend/`: Java 21, Spring Boot, Spring Data JPA, Flyway
- `docker-compose.yml`: local PostgreSQL only

Keep the implementation beginner-friendly. Prefer clear, direct code over clever abstractions.

## Local Runtime Notes

Common local URLs:

- Frontend: `http://localhost:5173`
- Backend API: `http://localhost:8080/api`
- PostgreSQL: `localhost:5432`

The backend can run without `OPENAI_API_KEY`; it returns mock assistant responses.

## Verification

Use the narrowest useful checks:

```bash
cd frontend
npm run lint
npx tsc -b
```

```bash
cd backend
mvn test
```

If Maven or Java are not visible in the agent shell, ask the user for their local paths before installing anything.
