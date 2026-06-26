# Research: GitHub Profile API

## Decision: Use a Single Spring Boot Web Service

**Rationale**: The assignment asks for a server with one username lookup endpoint and the repository guidance already targets Java 21 and Spring Boot. A single service keeps the project easy to run and review while allowing clean enterprise-style package boundaries.

**Alternatives considered**:

- Multi-module service: rejected because it adds build and navigation overhead for a 3-5 hour take-home.
- CLI or library-first project: rejected because the assignment explicitly requires a server endpoint.

## Decision: No Durable Database for the Initial Feature

**Rationale**: The required data is retrieved from public GitHub APIs and returned to the caller. Durable storage is not part of the business requirement. Avoiding a database keeps setup fast for evaluators and avoids unnecessary migrations.

**Alternatives considered**:

- PostgreSQL-backed profile cache: rejected because it adds infrastructure that does not improve the required assignment behavior.
- File-based persistence: rejected because cache persistence across restarts is not required.

## Decision: Add Short-Lived In-Memory Caching

**Rationale**: The assignment explicitly notes GitHub rate limits and suggests caching. A short-lived cache for successful profile summaries reduces duplicate GitHub calls during evaluation without changing the source of truth or requiring external infrastructure.

**Alternatives considered**:

- No caching: rejected because repeated evaluator requests could unnecessarily consume GitHub rate limit.
- Long-lived cache: rejected because stale public profile data would be harder to reason about and is unnecessary for a take-home.
- Distributed cache: rejected because single-instance local evaluation does not need it.

## Decision: Use a Dedicated GitHub Client Boundary

**Rationale**: A client abstraction isolates external URL construction, outbound request handling, status translation, JSON deserialization, and timeout behavior from business logic. This improves testability and keeps the service layer focused on workflow and mapping.

**Alternatives considered**:

- Call GitHub directly from the controller: rejected because it mixes transport, validation, orchestration, and mapping.
- Call GitHub directly from the service without an abstraction: rejected because it makes upstream failure tests and future client configuration harder.

## Decision: Map GitHub Payloads into Internal Domain Models Before Response DTOs

**Rationale**: GitHub field names and formats differ from the required response. Separating upstream payloads, domain values, and client-facing DTOs makes field mapping explicit and independently testable.

**Alternatives considered**:

- Reuse GitHub payload DTOs as response DTOs: rejected because response names and date format differ from GitHub.
- Build response JSON with maps: rejected because stringly typed response construction is easier to break and harder to test.

## Decision: Validate GitHub Usernames at the Boundary

**Rationale**: GitHub usernames have a constrained public format. Rejecting blank, overly long, or unsupported values before outbound calls improves security, reduces unnecessary network requests, and gives clients predictable feedback.

**Alternatives considered**:

- Let GitHub validate every username: rejected because it wastes outbound calls and makes validation errors dependent on upstream behavior.
- Accept arbitrary path text: rejected because it increases malformed URL and injection risk.

## Decision: Use Stable Error Categories

**Rationale**: Clients and evaluators should see predictable errors for validation, not-found, rate-limit, timeout, and upstream failure scenarios. Responses must not expose stack traces, class names, secrets, or internal implementation details.

**Alternatives considered**:

- Return raw upstream errors: rejected because upstream formats are not controlled by this service and may expose irrelevant implementation details.
- Collapse all failures into one generic error: rejected because clients need to distinguish validation, missing user, and temporary upstream problems.

## Decision: Use Focused Automated Tests Instead of Live GitHub-Dependent Tests

**Rationale**: Tests should be repeatable without network availability or GitHub rate-limit sensitivity. Stubbed upstream responses can prove field mapping, status translation, timeouts, and endpoint behavior deterministically.

**Alternatives considered**:

- Live GitHub integration tests as the primary verification: rejected because they can fail due to external network or rate-limit conditions.
- Unit-only tests: rejected because the HTTP endpoint contract and error serialization also need coverage.
