# Tasks: GitHub Profile API

**Input**: Design documents from `/specs/001-github-profile-api/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/openapi.yaml, quickstart.md

**Tests**: Required by FR-013 and SC-003. Test tasks are included before implementation tasks for each user story.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2, US3)
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Initialize the Spring Boot project, build configuration, and source tree.

- [X] T001 Create Maven project descriptor with Java 21, Spring Boot Web, Validation, Cache, Caffeine, Actuator, test, and HTTP-stub dependencies in pom.xml
- [X] T002 Create Maven Wrapper files for repeatable evaluator commands in mvnw, mvnw.cmd, and .mvn/wrapper/maven-wrapper.properties
- [X] T003 [P] Create application entry point in src/main/java/com/branchinterview/githubprofile/GithubProfileApplication.java
- [X] T004 [P] Create runtime configuration defaults for GitHub base URL, timeout, cache TTL, and logging in src/main/resources/application.yml
- [X] T005 [P] Create package directories with placeholder package-info files in src/main/java/com/branchinterview/githubprofile/client/package-info.java, src/main/java/com/branchinterview/githubprofile/config/package-info.java, src/main/java/com/branchinterview/githubprofile/controller/package-info.java, src/main/java/com/branchinterview/githubprofile/domain/package-info.java, and src/main/java/com/branchinterview/githubprofile/service/package-info.java
- [X] T006 [P] Create mirrored test package directories with placeholder package-info files in src/test/java/com/branchinterview/githubprofile/client/package-info.java, src/test/java/com/branchinterview/githubprofile/controller/package-info.java, and src/test/java/com/branchinterview/githubprofile/service/package-info.java

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Shared domain, configuration, and error primitives required before user stories can be implemented.

**CRITICAL**: No user story work can begin until this phase is complete.

- [X] T007 [P] Create GitHubProfile domain record with JSON field names from the OpenAPI contract in src/main/java/com/branchinterview/githubprofile/domain/GitHubProfile.java
- [X] T008 [P] Create RepositorySummary domain record with JSON field names from the OpenAPI contract in src/main/java/com/branchinterview/githubprofile/domain/RepositorySummary.java
- [X] T009 [P] Create GitHubUserResponse record for upstream user fields in src/main/java/com/branchinterview/githubprofile/client/GitHubUserResponse.java
- [X] T010 [P] Create GitHubRepositoryResponse record for upstream repository fields in src/main/java/com/branchinterview/githubprofile/client/GitHubRepositoryResponse.java
- [X] T011 [P] Create ErrorResponse record matching the OpenAPI error schema in src/main/java/com/branchinterview/githubprofile/controller/ErrorResponse.java
- [X] T012 [P] Create GitHubClientException with error categories for not-found, rate-limit, timeout, unavailable, and invalid upstream data in src/main/java/com/branchinterview/githubprofile/client/GitHubClientException.java
- [X] T013 [P] Configure RestClient or WebClient bean with base URL and timeout properties in src/main/java/com/branchinterview/githubprofile/config/GitHubClientConfig.java
- [X] T014 [P] Configure short-lived Caffeine cache manager for successful profile lookups in src/main/java/com/branchinterview/githubprofile/config/CacheConfig.java
- [X] T015 [P] Create username validation component for GitHub username format rules in src/main/java/com/branchinterview/githubprofile/service/UsernameValidator.java
- [X] T016 [P] Create global exception handler skeleton for validation and lookup errors in src/main/java/com/branchinterview/githubprofile/controller/RestExceptionHandler.java

**Checkpoint**: Foundation ready - user story implementation can now begin in priority order.

---

## Phase 3: User Story 1 - Retrieve a GitHub Profile Summary (Priority: P1) MVP

**Goal**: A client can request a valid GitHub username and receive the required merged profile summary JSON.

**Independent Test**: Request `GET /users/octocat` with stubbed GitHub user and repo responses and verify all required top-level fields, repository fields, null optional fields, and date formatting.

### Tests for User Story 1

- [X] T017 [P] [US1] Add mapper unit tests for required field mapping, null optional fields, empty repos, and HTTP-date formatting in src/test/java/com/branchinterview/githubprofile/service/GitHubProfileMapperTest.java
- [X] T018 [P] [US1] Add GitHub client success-path tests for user and repo retrieval with stubbed upstream JSON in src/test/java/com/branchinterview/githubprofile/client/GitHubClientTest.java
- [X] T019 [P] [US1] Add controller contract test for GET /users/{username} success response shape in src/test/java/com/branchinterview/githubprofile/controller/GitHubProfileControllerTest.java
- [X] T020 [P] [US1] Add service aggregation test proving profile and repository data are merged once per lookup in src/test/java/com/branchinterview/githubprofile/service/GitHubProfileServiceTest.java

### Implementation for User Story 1

- [X] T021 [P] [US1] Implement GitHubProfileMapper for field translation and created_at HTTP-date formatting in src/main/java/com/branchinterview/githubprofile/service/GitHubProfileMapper.java
- [X] T022 [US1] Implement GitHubClient methods to fetch /users/{username} and /users/{username}/repos in src/main/java/com/branchinterview/githubprofile/client/GitHubClient.java
- [X] T023 [US1] Implement GitHubProfileService orchestration for validation, GitHub client calls, and mapping in src/main/java/com/branchinterview/githubprofile/service/GitHubProfileService.java
- [X] T024 [US1] Implement GitHubProfileController GET /users/{username} endpoint in src/main/java/com/branchinterview/githubprofile/controller/GitHubProfileController.java
- [X] T025 [US1] Complete success-path serialization annotations to match user_name, display_name, geo_location, created_at, and repos in src/main/java/com/branchinterview/githubprofile/domain/GitHubProfile.java
- [X] T026 [US1] Run User Story 1 tests and fix failures in src/test/java/com/branchinterview/githubprofile/service/GitHubProfileMapperTest.java, src/test/java/com/branchinterview/githubprofile/client/GitHubClientTest.java, src/test/java/com/branchinterview/githubprofile/controller/GitHubProfileControllerTest.java, and src/test/java/com/branchinterview/githubprofile/service/GitHubProfileServiceTest.java

**Checkpoint**: User Story 1 is fully functional and independently testable as the MVP.

---

## Phase 4: User Story 2 - Handle Invalid and Unavailable Usernames (Priority: P2)

**Goal**: Clients receive clear, stable, non-sensitive errors for invalid input, missing users, rate limiting, timeouts, and upstream failures.

**Independent Test**: Request blank or malformed usernames, nonexistent users, and stubbed upstream failure responses; verify the expected status and error code without stack traces or internal details.

### Tests for User Story 2

- [X] T027 [P] [US2] Add username validator tests for blank, too-long, hyphen-boundary, unsupported-character, and valid usernames in src/test/java/com/branchinterview/githubprofile/service/UsernameValidatorTest.java
- [X] T028 [P] [US2] Add GitHub client failure tests for 404, 429, timeout, 5xx, and malformed upstream payloads in src/test/java/com/branchinterview/githubprofile/client/GitHubClientFailureTest.java
- [X] T029 [P] [US2] Add controller error contract tests for INVALID_USERNAME, PROFILE_NOT_FOUND, UPSTREAM_RATE_LIMITED, UPSTREAM_TIMEOUT, and UPSTREAM_UNAVAILABLE in src/test/java/com/branchinterview/githubprofile/controller/GitHubProfileControllerErrorTest.java
- [X] T030 [P] [US2] Add service tests proving failed lookups are translated and not returned as successful profile responses in src/test/java/com/branchinterview/githubprofile/service/GitHubProfileServiceErrorTest.java

### Implementation for User Story 2

- [X] T031 [US2] Implement full GitHub username validation and exception behavior in src/main/java/com/branchinterview/githubprofile/service/UsernameValidator.java
- [X] T032 [US2] Implement upstream status, timeout, and malformed-payload translation in src/main/java/com/branchinterview/githubprofile/client/GitHubClient.java
- [X] T033 [US2] Implement stable sanitized error responses and status mapping in src/main/java/com/branchinterview/githubprofile/controller/RestExceptionHandler.java
- [X] T034 [US2] Integrate validation and error propagation in src/main/java/com/branchinterview/githubprofile/service/GitHubProfileService.java
- [X] T035 [US2] Run User Story 2 tests and fix failures in src/test/java/com/branchinterview/githubprofile/service/UsernameValidatorTest.java, src/test/java/com/branchinterview/githubprofile/client/GitHubClientFailureTest.java, src/test/java/com/branchinterview/githubprofile/controller/GitHubProfileControllerErrorTest.java, and src/test/java/com/branchinterview/githubprofile/service/GitHubProfileServiceErrorTest.java

**Checkpoint**: User Stories 1 and 2 work independently and provide predictable success and failure behavior.

---

## Phase 5: User Story 3 - Demonstrate Production Readiness in Phases (Priority: P3)

**Goal**: The repository demonstrates evaluator-ready documentation, repeatable verification, cache behavior, and clear implementation phases.

**Independent Test**: Follow README and quickstart instructions from a clean checkout, run the automated suite, and verify the documented architecture and phased scope.

### Tests for User Story 3

- [X] T036 [P] [US3] Add cache behavior test proving repeated successful lookups avoid duplicate upstream calls in src/test/java/com/branchinterview/githubprofile/service/GitHubProfileCacheTest.java
- [X] T037 [P] [US3] Add application context smoke test for production wiring in src/test/java/com/branchinterview/githubprofile/GithubProfileApplicationTest.java

### Implementation for User Story 3

- [X] T038 [US3] Apply caching to successful profile lookups with normalized username keys in src/main/java/com/branchinterview/githubprofile/service/GitHubProfileService.java
- [X] T039 [US3] Add operational health exposure and safe logging defaults in src/main/resources/application.yml
- [X] T040 [US3] Write evaluator README with setup, run, curl examples, architecture, testing, cache, error handling, and tradeoffs in README.md
- [X] T041 [US3] Add implementation phase notes mapping Phase 1 baseline, Phase 2 reliability, and Phase 3 readiness to completed work in README.md
- [X] T042 [US3] Run User Story 3 tests and fix failures in src/test/java/com/branchinterview/githubprofile/service/GitHubProfileCacheTest.java and src/test/java/com/branchinterview/githubprofile/GithubProfileApplicationTest.java

**Checkpoint**: The project is evaluator-ready and documents its production-readiness decisions.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Final verification and cleanup across all user stories.

- [X] T043 [P] Verify OpenAPI contract examples and error codes remain aligned with implementation in specs/001-github-profile-api/contracts/openapi.yaml
- [X] T044 [P] Verify quickstart commands and expected outcomes remain accurate in specs/001-github-profile-api/quickstart.md
- [X] T045 Run ./mvnw clean compile and resolve failures in pom.xml and src/main/java/com/branchinterview/githubprofile/GithubProfileApplication.java
- [X] T046 Run ./mvnw test and resolve failures in src/test/java/com/branchinterview/githubprofile
- [X] T047 Run ./mvnw verify and resolve failures in pom.xml, src/main/java/com/branchinterview/githubprofile, and src/test/java/com/branchinterview/githubprofile
- [X] T048 Review response payloads for no stack traces, secrets, class names, or internal details in src/main/java/com/branchinterview/githubprofile/controller/RestExceptionHandler.java
- [X] T049 Review README instructions by following them from a clean terminal session and update README.md with any missing steps

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately.
- **Foundational (Phase 2)**: Depends on Setup completion - blocks all user stories.
- **User Story 1 (Phase 3)**: Depends on Foundational completion - provides MVP.
- **User Story 2 (Phase 4)**: Depends on Foundational completion and may reuse US1 endpoint/service files.
- **User Story 3 (Phase 5)**: Depends on Foundational completion and builds on US1/US2 behavior for cache, documentation, and verification.
- **Polish (Phase 6)**: Depends on all desired user stories being complete.

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Phase 2 and should be completed first for MVP.
- **User Story 2 (P2)**: Can start after Phase 2, but sequential delivery after US1 is recommended because it hardens the same endpoint and service flow.
- **User Story 3 (P3)**: Can start after Phase 2, but documentation and cache verification are most accurate after US1 and US2 behavior exists.

### Within Each User Story

- Write tests before implementation and confirm they fail for missing behavior.
- Domain and upstream DTOs before mapper and client usage.
- Client and mapper before service orchestration.
- Service orchestration before controller endpoint behavior.
- Run the story-specific test set before moving to the next story.

---

## Parallel Opportunities

- Setup tasks T003-T006 can run in parallel after T001-T002 are defined.
- Foundational records, exceptions, config, validator, and exception-handler skeleton tasks T007-T016 can run in parallel after project setup.
- User Story 1 tests T017-T020 can run in parallel, then mapper T021 can run while client T022 is implemented.
- User Story 2 tests T027-T030 can run in parallel, then validator T031 and client failure handling T032 can run in parallel.
- User Story 3 tests T036-T037 can run in parallel, while README drafting T040-T041 can proceed after behavior decisions are stable.
- Polish checks T043-T044 can run in parallel before final Maven verification.

## Parallel Example: User Story 1

```text
Task: "T017 [P] [US1] Add mapper unit tests for required field mapping, null optional fields, empty repos, and HTTP-date formatting in src/test/java/com/branchinterview/githubprofile/service/GitHubProfileMapperTest.java"
Task: "T018 [P] [US1] Add GitHub client success-path tests for user and repo retrieval with stubbed upstream JSON in src/test/java/com/branchinterview/githubprofile/client/GitHubClientTest.java"
Task: "T019 [P] [US1] Add controller contract test for GET /users/{username} success response shape in src/test/java/com/branchinterview/githubprofile/controller/GitHubProfileControllerTest.java"
Task: "T020 [P] [US1] Add service aggregation test proving profile and repository data are merged once per lookup in src/test/java/com/branchinterview/githubprofile/service/GitHubProfileServiceTest.java"
```

## Parallel Example: User Story 2

```text
Task: "T027 [P] [US2] Add username validator tests for blank, too-long, hyphen-boundary, unsupported-character, and valid usernames in src/test/java/com/branchinterview/githubprofile/service/UsernameValidatorTest.java"
Task: "T028 [P] [US2] Add GitHub client failure tests for 404, 429, timeout, 5xx, and malformed upstream payloads in src/test/java/com/branchinterview/githubprofile/client/GitHubClientFailureTest.java"
Task: "T029 [P] [US2] Add controller error contract tests for INVALID_USERNAME, PROFILE_NOT_FOUND, UPSTREAM_RATE_LIMITED, UPSTREAM_TIMEOUT, and UPSTREAM_UNAVAILABLE in src/test/java/com/branchinterview/githubprofile/controller/GitHubProfileControllerErrorTest.java"
Task: "T030 [P] [US2] Add service tests proving failed lookups are translated and not returned as successful profile responses in src/test/java/com/branchinterview/githubprofile/service/GitHubProfileServiceErrorTest.java"
```

## Parallel Example: User Story 3

```text
Task: "T036 [P] [US3] Add cache behavior test proving repeated successful lookups avoid duplicate upstream calls in src/test/java/com/branchinterview/githubprofile/service/GitHubProfileCacheTest.java"
Task: "T037 [P] [US3] Add application context smoke test for production wiring in src/test/java/com/branchinterview/githubprofile/GithubProfileApplicationTest.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 setup.
2. Complete Phase 2 foundational files.
3. Complete Phase 3 User Story 1 tests and implementation.
4. Stop and validate `GET /users/octocat` against stubbed tests and a manual live call if network access is available.

### Incremental Delivery

1. Deliver User Story 1 as the functional baseline.
2. Add User Story 2 to make the endpoint production-safe for invalid and unavailable data.
3. Add User Story 3 to demonstrate cache behavior, documentation quality, and final verification.
4. Run Phase 6 verification before submission.

### Take-Home Time Management

If time is constrained, complete through User Story 1 plus README basics first, then add User Story 2 error hardening, then add User Story 3 cache and final documentation polish.

## Notes

- All task descriptions include exact file paths for LLM execution.
- `[P]` tasks target different files or independent checks and can be run concurrently after their phase prerequisites.
- User story labels map directly to the prioritized stories in spec.md.
- Tests are required by the feature specification, so they are intentionally included.
