---
workflowStatus: 'complete'
totalSteps: 5
stepsCompleted: [1,2,3,4,5]
lastStep: 'handoff'
nextStep: ''
lastSaved: '{date}'
workflowType: 'testarch-test-design'
inputDocuments: ['README.md', 'backend/controllers', 'frontend/routes', 'db/migration']
---

# Test Design for QA: AI Dev Assistant Dashboard

**Purpose:** Test execution recipe for QA team. Defines what to test, how to test it, and what QA needs from other teams.

**Date:** {date}
**Author:** Master Test Architect
**Status:** Complete
**Project:** AI Dev Assistant Dashboard

**Related:** See Architecture doc (test-design-architecture.md) for testability concerns and architectural blockers.

---

## Executive Summary

**Scope:** End-to-end, integration, and unit testing for all major flows: tasks, logs, dashboard, AI chat.

**Risk Summary:**
- High: DB migrations, AI assistant flows, chat persistence, error logging
- Medium: Task/log CRUD, dashboard summary
- Low: UI rendering, static content

**Coverage Summary:**
- P0 tests: API endpoint integration, DB migration idempotency, AI chat (mocked)
- P1 tests: Task/log CRUD, dashboard summary, error handling
- P2 tests: UI edge cases, chat context switching
- P3 tests: Exploratory/manual

**Test Data Strategy:**
- Use sample tasks/logs for CRUD
- Use mock OpenAI for chat
- Use migration scripts for DB state

**Test Suites:**
- Backend: JUnit for services/controllers, Flyway migration tests
- Frontend: Lint, type-check, manual E2E (Cypress/Playwright recommended)

**CI Recommendations:**
- Backend: `cd backend && mvn test`
- Frontend: `cd frontend && npm run lint && npx tsc -b`
- Add E2E tests for full coverage
