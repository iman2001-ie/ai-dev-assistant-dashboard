---
stepsCompleted: ["converted-sprint1-tasks"]
inputDocuments: ["docs/sprint1-tasks.md"]
---

# ai-dev-assistant-dashboard - Epic Breakdown

## Overview

This document converts the existing Sprint 1 ticket list into BMAD-compatible
epics and stories. It is intentionally scoped to the current MVP work already
tracked in `docs/sprint1-tasks.md`.

## Requirements Inventory

### Functional Requirements

- Users can authenticate with backend-issued tokens.
- The frontend supports login, logout, registration, and protected routes.
- The app can expose agent/dashboard actions through backend APIs.
- Agent run logs can be persisted and retrieved.
- CI and smoke checks cover the core development flow.

### NonFunctional Requirements

- Authentication and ownership changes must use Flyway migrations.
- Existing mock/OpenAI fallback behavior must remain safe.
- The implementation should stay beginner-friendly and focused on the MVP.
- CI checks should be reproducible for local and GitHub workflows.

### Additional Requirements

- Keep sprint tracking aligned with the current Sprint 1 ticket table.
- Prefer small stories that map cleanly to existing backend, frontend, and docs
  boundaries.

### UX Design Requirements

- Authentication pages and protected navigation should match the existing React
  app structure.
- Dashboard controls should remain clear and task-oriented.

### FR Coverage Map

| Requirement | Covered By |
| --- | --- |
| Authentication backend | Story 1.1, Story 1.2, Story 1.4 |
| Authentication frontend | Story 1.3 |
| Agent dashboard APIs | Story 2.1, Story 2.3 |
| Agent dashboard UI | Story 2.2 |
| CI and validation | Story 3.1, Story 3.3 |
| Observability | Story 3.2 |

## Epic List

1. Authentication Foundation
2. Agent Dashboard Operations
3. Quality, CI, and Observability

## Epic 1: Authentication Foundation

Build the MVP authentication path across backend, database, frontend, and tests.

### Story 1.1: Decide Authentication Approach

As a project maintainer,
I want to choose and document the JWT versus session approach,
So that the implementation follows one clear authentication model.

**Acceptance Criteria:**

**Given** the project needs user login and protected routes
**When** the authentication approach is selected
**Then** the decision is documented in project docs or implementation notes
**And** backend and frontend work follows the selected approach.

### Story 1.2: Implement Auth Endpoints and JWT Handling

As an app user,
I want backend endpoints for authentication and token handling,
So that I can securely register, log in, refresh, and log out.

**Acceptance Criteria:**

**Given** the backend API is running
**When** a valid user registers or logs in
**Then** the backend returns the expected authentication response
**And** invalid credentials receive a clear error response.

### Story 1.3: Build Frontend Login Logout and Protected Routes

As an app user,
I want login, logout, registration, and protected frontend routes,
So that authenticated screens are only available after signing in.

**Acceptance Criteria:**

**Given** the frontend app is running
**When** I authenticate successfully
**Then** protected routes become accessible
**And** logging out clears auth state and returns me to an unauthenticated flow.

### Story 1.4: Add Flyway Migrations for Users and Ownership

As a developer,
I want user and ownership database changes managed through Flyway,
So that authentication data is reproducible across environments.

**Acceptance Criteria:**

**Given** a fresh PostgreSQL database
**When** Flyway migrations run
**Then** user-related tables and ownership columns are created successfully
**And** existing sample data remains compatible.

### Story 1.5: Add Auth Unit and Integration Tests

As a developer,
I want auth-focused backend tests,
So that login, refresh, logout, and per-user data behavior remain reliable.

**Acceptance Criteria:**

**Given** the backend test suite runs
**When** auth scenarios are exercised
**Then** successful and failing paths are covered
**And** per-user task behavior is validated.

## Epic 2: Agent Dashboard Operations

Add the backend and frontend pieces needed to list agents, run them, and inspect
run history in the dashboard.

### Story 2.1: Add Agent List and Run Endpoints

As a dashboard user,
I want backend APIs for listing agents and starting runs,
So that the frontend can trigger agent workflows from the dashboard.

**Acceptance Criteria:**

**Given** the backend API is running
**When** the frontend requests available agents
**Then** the backend returns agent metadata
**And** run requests receive a clear success or error response.

### Story 2.2: Add Agent List UI and Run Controls

As a dashboard user,
I want frontend controls for viewing agents and starting runs,
So that I can operate workflows without leaving the app.

**Acceptance Criteria:**

**Given** agent metadata is available from the backend
**When** I open the dashboard
**Then** I can see the available agents
**And** I can start a run through clear UI controls.

### Story 2.3: Persist Agent Run Logs and Retrieve Them

As a dashboard user,
I want agent run logs saved and retrievable,
So that I can review what happened after a workflow runs.

**Acceptance Criteria:**

**Given** an agent run is started
**When** the run produces status or output records
**Then** those records are persisted
**And** the dashboard can retrieve them later.

## Epic 3: Quality, CI, and Observability

Make the MVP easier to validate, monitor, and keep stable during continued work.

### Story 3.1: Finalize CI Checks for Pull Requests

As a maintainer,
I want CI checks for backend and frontend validation,
So that pull requests catch basic regressions before merge.

**Acceptance Criteria:**

**Given** a pull request is opened
**When** CI runs
**Then** frontend and backend checks execute
**And** failures are visible in the pull request.

### Story 3.2: Add Structured Logging and Metrics Endpoint

As a maintainer,
I want basic structured logs and a metrics endpoint,
So that local debugging and operational checks are easier.

**Acceptance Criteria:**

**Given** the backend is running
**When** normal API requests occur
**Then** useful structured log output is produced
**And** a basic metrics endpoint can be checked locally.

### Story 3.3: Add Smoke E2E Tests for Core Flows

As a maintainer,
I want smoke end-to-end coverage for core flows,
So that the MVP can be validated quickly after changes.

**Acceptance Criteria:**

**Given** the local stack is running
**When** smoke tests execute
**Then** the main dashboard, task, log, assistant, and auth flows are checked
**And** failures point to the affected workflow.
