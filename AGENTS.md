# AGENTS.md

## Purpose

This repository follows Spec-Driven Development (SDD) using GitHub Spec Kit.

All work should follow the established workflow:

1. Define requirements in `spec.md`
2. Create technical design in `plan.md`
3. Break work into `tasks.md`
4. Implement tasks
5. Validate with automated tests

The approved specification is the source of truth for all implementation work.

---

# Technology Stack

## Runtime

- Java 21 LTS

## Frameworks

- Spring Boot 3.x
- Spring Web
- Spring Data JPA
- Spring Security

## Database

- PostgreSQL
- Liquibase

## HTTP Client Guidelines

- Prefer Spring `RestClient` for synchronous HTTP communication.
- Prefer Spring HTTP Interfaces (`@HttpExchange`) for declarative REST clients.
- Use `WebClient` only when reactive or asynchronous behavior is required.
- Do not introduce new `RestTemplate` implementations.

## Build

- Maven 3.9+
- Maven Wrapper (`./mvnw`)

## Testing

- JUnit 5
- Mockito
- Testcontainers

---

# Project Structure

```text
/my-enterprise-app
├── .specify/
├── specs/
│   └── NNN-feature-name/
│       ├── spec.md
│       ├── plan.md
│       └── tasks.md
├── api-gateway/
└── core-services/
    └── customer-service/
        ├── src/main/java/com/enterprise/customer/
        │   ├── config/
        │   ├── controller/
        │   ├── service/
        │   ├── domain/
        │   ├── repository/
        │   └── db/changelog/
        └── src/test/java/
```

Agents should follow the existing project structure and avoid introducing alternative patterns without justification.

---

# Architecture Guidelines

## Layer Responsibilities

### Controllers

- Handle HTTP requests and responses.
- Validate incoming requests.
- Delegate business operations to services.

### Services

- Contain business logic.
- Coordinate application workflows.

### Domain

- Represent business entities and business rules.

### Repositories

- Handle data persistence.

Business logic should reside in the service and domain layers, not in controllers or repositories.

---

# Coding Guidelines

## Dependency Injection

Prefer constructor injection for Spring-managed components.

Avoid field injection unless there is a compelling reason.

## DTOs

Prefer Java records for immutable request and response DTOs.

Use traditional classes when mutable state, inheritance, or framework requirements make them more appropriate.

## Code Style

- Follow existing project conventions.
- Favor readability over cleverness.
- Keep methods focused on a single responsibility.
- Reuse existing utilities and abstractions before creating new ones.

---

# Database Guidelines

## Migrations

All schema changes should be managed through Liquibase migrations.

Migration files should be additive whenever practical.

## Persistence

- Design schemas with appropriate indexes and constraints.
- Use transactions where required to maintain data consistency.
- Consider performance implications of entity relationships and queries.

---

# API Guidelines

## Design

- Follow RESTful conventions where appropriate.
- Use meaningful resource names.
- Maintain backward compatibility whenever practical.

## Validation

- Validate incoming requests.
- Return clear and consistent error responses.

## Documentation

Maintain API documentation for externally exposed endpoints.

---

# Security Guidelines

- Never log secrets, credentials, tokens, or personally identifiable information (PII).
- Validate authentication and authorization using existing Spring Security patterns.
- Treat all external input as untrusted.
- Avoid exposing internal implementation details in API responses.

---

# Transaction Guidelines

- Apply transactions at the service layer where appropriate.
- Keep transactions short.
- Avoid external network calls within database transactions.
- Design write operations to be idempotent when retries are possible.

---

# Observability Guidelines

- Log meaningful business events and failures.
- Avoid excessive logging.
- Include correlation or request identifiers when available.
- Add metrics or tracing for significant workflows when supported by the project.

---

# Dependency Guidelines

- Prefer existing project libraries before introducing new dependencies.
- Do not add dependencies unless required by the approved plan or implementation.
- Keep the dependency footprint minimal.

---

# Compatibility Guidelines

- Preserve existing API behavior unless the specification explicitly requires a change.
- Avoid breaking database schemas, API contracts, or messaging contracts without documenting the change in the technical plan.

---

# Testing Guidelines

## Unit Tests

Create unit tests for business logic and validation rules.

## Integration Tests

Create integration tests for:

- Repository behavior
- API endpoints
- Database interactions

Use Testcontainers when infrastructure dependencies are required.

---

# Quality Verification

Before completing implementation, verify:

```bash
./mvnw clean compile
./mvnw test
./mvnw verify
```

Resolve build and test failures before considering work complete.

---

# Spec-Driven Development

Before implementing changes:

1. Read `spec.md`.
2. Read `plan.md`.
3. Read `tasks.md`.

Implementation should align with the approved specification and technical plan.

If requirements are unclear or conflicting, seek clarification rather than making assumptions.

---

# AI Agent Guidance

- Read this document before making changes.
- Prefer existing project patterns over introducing new abstractions.
- Keep changes focused on the requested feature.
- Minimize unnecessary refactoring.
- Avoid adding dependencies without justification.
- Do not implement behavior that is not defined in the specification.
- Preserve existing code style and architectural conventions.
- If multiple approaches are reasonable, choose the simplest solution consistent with the current codebase.
- When in doubt, follow the specification and existing project conventions.

<!-- SPECKIT START -->
For additional context about technologies to be used, project structure,
shell commands, and other important information, read the current plan
at specs/001-github-profile-api/plan.md
<!-- SPECKIT END -->
