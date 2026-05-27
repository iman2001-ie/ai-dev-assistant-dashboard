---
title: 'TEA Test Design → BMAD Handoff Document'
version: '1.0'
workflowType: 'testarch-test-design-handoff'
inputDocuments: ['test-design-architecture.md', 'test-design-qa.md']
sourceWorkflow: 'testarch-test-design'
generatedBy: 'TEA Master Test Architect'
generatedAt: '{date}'
projectName: 'AI Dev Assistant Dashboard'
---

# TEA → BMAD Integration Handoff

## Purpose
This document bridges TEA's test design outputs with BMAD's epic/story decomposition workflow. It provides structured integration guidance so that quality requirements, risk assessments, and test strategies flow into implementation planning.

## TEA Artifacts Inventory
| Artifact             | Path                                    | BMAD Integration Point                               |
| -------------------- | --------------------------------------- | ---------------------------------------------------- |
| Test Design Document | test-design-architecture.md             | Epic quality requirements, story acceptance criteria |
| Risk Assessment      | (embedded in test design)               | Epic risk classification, story priority             |
| Coverage Strategy    | (embedded in test design)               | Story test requirements                              |

## Epic-Level Integration Guidance
### Risk References
- P0: DB migrations, AI assistant, chat persistence, error logging
- P1: Task/log CRUD, dashboard summary

### Quality Gates
- All epics must have integration and E2E test coverage for P0/P1 risks
- Mock AI assistant in test environments

## Story-Level Integration Guidance
### P0/P1 Test Scenarios → Story Acceptance Criteria
- API endpoints must be covered by integration tests
- DB migrations must be tested for idempotency
- Chat and log linkage must be tested for persistence and error handling
