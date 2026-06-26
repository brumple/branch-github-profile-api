# Implementation Plan: GitHub Profile API

**Branch**: `[no-git-branch]` | **Date**: 2026-06-24 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-github-profile-api/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Build a production-quality Spring Boot web service that accepts one GitHub username, retrieves public GitHub user and repository data, maps it to the assignment response shape, and returns predictable JSON success or error responses. The technical approach is a single Maven service with layered controller/service/client/domain code, Java 21 records for immutable DTOs, a GitHub client abstraction around outbound HTTP calls, short-lived in-memory caching for successful lookups, and focused automated tests for mapping, validation, endpoint behavior, and upstream failure handling.

## Technical Context

**Language/Version**: Java 21 LTS

**Primary Dependencies**: Spring Boot 3.x, Spring Web, Spring Validation, Spring Cache with Caffeine, Jackson, Spring Boot Actuator

**Storage**: N/A for durable storage; short-lived in-memory cache only

**Testing**: JUnit 5, Spring Boot Test, Mockito, WireMock or MockWebServer for GitHub HTTP stubs

**Target Platform**: Local or containerized Linux-compatible server runtime

**Project Type**: Web service

**Performance Goals**: Successful cached lookups should be effectively immediate for interactive use; uncached lookups should complete within normal evaluator expectations when GitHub is responsive

**Constraints**: Avoid requiring a GitHub token; do not log secrets or PII; return non-sensitive errors; keep assignment-completable scope within 3-5 hours; avoid durable infrastructure unless a later requirement demands it

**Scale/Scope**: One public GitHub username per request, public GitHub profile data plus public repositories, take-home evaluation traffic rather than high-volume production traffic

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

The constitution file is still placeholder-only and contains no concrete mandatory gates. Repository guidance from `AGENTS.md` is applied as the effective governance baseline:

- **Layered architecture**: PASS. Plan uses controller, service, client, domain, and DTO boundaries.
- **Constructor injection**: PASS. Planned for Spring-managed components.
- **DTO records**: PASS. Planned for immutable request/response and GitHub payload DTOs.
- **Minimal dependencies**: PASS. Dependencies support web serving, validation, cache, observability, JSON, and tests.
- **Database migrations**: PASS by non-applicability. No durable schema changes are planned.
- **Security and error hygiene**: PASS. Plan requires input validation and non-sensitive client-facing errors.
- **Automated verification**: PASS. Plan includes unit, integration, and contract-style API validation.

## Project Structure

### Documentation (this feature)

```text
specs/001-github-profile-api/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── openapi.yaml
└── tasks.md
```

### Source Code (repository root)

```text
pom.xml
README.md
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── branchinterview/
│   │           └── githubprofile/
│   │               ├── GithubProfileApplication.java
│   │               ├── client/
│   │               │   ├── GitHubClient.java
│   │               │   ├── GitHubClientException.java
│   │               │   ├── GitHubUserResponse.java
│   │               │   └── GitHubRepositoryResponse.java
│   │               ├── config/
│   │               │   ├── CacheConfig.java
│   │               │   └── GitHubClientConfig.java
│   │               ├── controller/
│   │               │   ├── ErrorResponse.java
│   │               │   ├── GitHubProfileController.java
│   │               │   └── RestExceptionHandler.java
│   │               ├── domain/
│   │               │   ├── GitHubProfile.java
│   │               │   └── RepositorySummary.java
│   │               └── service/
│   │                   ├── GitHubProfileMapper.java
│   │                   ├── GitHubProfileService.java
│   │                   └── UsernameValidator.java
│   └── resources/
│       └── application.yml
└── test/
    └── java/
        └── com/
            └── branchinterview/
                └── githubprofile/
                    ├── client/
                    ├── controller/
                    └── service/
```

**Structure Decision**: Use a single Spring Boot service at repository root. This matches the assignment's server requirement, avoids unnecessary multi-module overhead, and still preserves enterprise layering through package boundaries.

## Complexity Tracking

No constitution violations require tracking.

## Phase 0: Research Summary

Research decisions are documented in [research.md](./research.md). All technical context questions were resolved without open clarification markers.

## Phase 1: Design Summary

Design artifacts created for this plan:

- [data-model.md](./data-model.md) defines client-facing profile, repository, error, cache, and upstream payload models.
- [contracts/openapi.yaml](./contracts/openapi.yaml) defines the external HTTP contract for the username lookup.
- [quickstart.md](./quickstart.md) defines setup, run, test, and validation scenarios.

## Post-Design Constitution Check

- **Layered architecture**: PASS. Data model and contract keep external payload mapping out of controllers.
- **Constructor injection**: PASS. To be enforced during tasks.
- **DTO records**: PASS. Data model identifies immutable DTO candidates.
- **Minimal dependencies**: PASS. No database, persistence, security server, or container dependency is planned for the MVP.
- **Security and error hygiene**: PASS. Contract includes sanitized validation, not-found, upstream, timeout, and rate-limit errors.
- **Automated verification**: PASS. Quickstart and future tasks will require tests for success path, validation, not-found, upstream failures, and cache behavior.
