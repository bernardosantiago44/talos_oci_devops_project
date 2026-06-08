# Backend Test Plan

## Purpose

This test plan defines a small, maintainable testing foundation for the Spring Boot backend. The goal is to protect the core Work Item and assignment behavior without requiring Oracle Autonomous DB credentials or production infrastructure.

Tests should be readable for new contributors. Prefer small focused tests that clearly show the behavior being checked.

## Test Layers

### Unit Tests

Use JUnit 5 and Mockito.

Unit tests should cover service classes by mocking repositories and collaborator services. They should not start Spring and should not connect to any database.

Current priority:

- `WorkItemService`
- `WorkItemAssignmentService`
- Validation and business-rule failures
- Not-found failures

Examples:

- Creating a work item saves it when the creator and sprint are valid.
- Creating a work item rejects an unknown creator.
- Updating a missing work item throws `WorkItemNotFoundException`.
- Replacing assignees rejects duplicate user ids before deleting existing assignments.

### Controller Tests

Use Spring MockMvc.

Controller tests should verify HTTP behavior by mocking the service layer. They should not load JPA repositories or connect to Oracle.

Current priority:

- Main `WorkItemController` endpoints
- Assignment endpoints that already exist under `/workitems/{id}/assignees`
- Request validation errors
- Not-found and business-rule responses from `GlobalExceptionHandler`

Examples:

- `GET /workitems` returns a JSON list.
- `GET /workitems/{id}` returns a single work item.
- `POST /workitems` returns `201 Created` for a valid request.
- `POST /workitems` returns `400 Bad Request` for missing required fields.
- `GET /workitems/{id}` returns `404 Not Found` when the service throws `WorkItemNotFoundException`.
- `PATCH /workitems/{id}/assignees/{userId}` returns `201 Created`.

### Test Data Builders

Shared test data belongs under `src/test/java/.../testdata`.

Use these helpers for common entities and DTOs so each test can focus on the behavior under test. Keep the helpers simple and explicit. A test may still modify returned objects when it needs a specific edge case.

Current shared factory:

- Work item entity
- App user entity
- Work item assignment entity
- Create and update work item requests
- Work item response DTOs
- Assignment DTOs

## What To Test Now

Focus on the behavior that changes most often and is central to the product:

- Work item creation, update, lookup, and deletion decisions in the service layer
- Assignment add, remove, replace, and lookup decisions
- Validation failures for required request fields
- Business-rule failures such as invalid estimates, missing creator, missing sprint, duplicate assignees, and missing users
- HTTP status codes and response shapes for WorkItem controller endpoints

## What To Test Later

Add these only when the related behavior stabilizes:

- Repository integration tests with an isolated test database
- Sprint service and sprint controller behavior
- User management endpoints
- Time entry endpoints
- KPI and analytics computation
- Telegram Bot interaction flows
- Web Portal end-to-end flows
- Performance, load, and deployment checks

## Database Integration Tests

Do not use Oracle Autonomous DB for local tests.

If database integration tests are added later, isolate them from unit and controller tests:

- Put them in a clearly named package such as `integration`.
- Use a test-only database or Testcontainers if Oracle compatibility is required.
- Keep credentials out of the repository.
- Make sure regular `mvn test` can run without production database credentials.

## Local Commands

Run all current tests:

```bash
./mvnw test
```

This also creates a JaCoCo coverage report at:

```text
target/site/jacoco/index.html
```

Run one service test:

```bash
./mvnw -Dtest=WorkItemServiceTest test
```

Run one controller test:

```bash
./mvnw -Dtest=WorkItemControllerTest test
```
