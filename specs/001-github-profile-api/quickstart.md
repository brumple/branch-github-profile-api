# Quickstart: GitHub Profile API

## Prerequisites

- Java 21
- Maven Wrapper included in the repository
- Network access to `api.github.com` for manual live validation

## Setup

From the repository root:

```bash
./mvnw clean compile
```


## Run the Service

```bash
./mvnw spring-boot:run
```

Expected outcome: the service starts locally and exposes the profile lookup endpoint documented in [contracts/openapi.yaml](./contracts/openapi.yaml).

## Validate the Primary Scenario

```bash
curl -i http://localhost:8080/users/octocat
```

Expected outcome:

- HTTP status is `200`.
- Response contains `user_name`, `display_name`, `avatar`, `geo_location`, `email`, `url`, `created_at`, and `repos`.
- `user_name` is `octocat`.
- `repos` is a JSON array.
- Repository items contain `name` and `url`.
- Repository retrieval follows GitHub pagination up to the configured `github.api.max-repository-pages` limit.

## Validate Optional GitHub Authentication Passthrough

```bash
curl -i -H "X-GitHub-Token: $GITHUB_TOKEN" http://localhost:8080/users/octocat
```

Expected outcome: the service still returns the same response shape, while forwarding the token only to GitHub for that lookup. Authenticated GitHub requests may improve rate limits and may return public profile email when GitHub exposes it for the authenticated request.

## Validate Error Scenarios

Blank or malformed username:

```bash
curl -i http://localhost:8080/users/-
```

Expected outcome: HTTP status is `400` with error code `INVALID_USERNAME`.

Missing user:

```bash
curl -i http://localhost:8080/users/this-user-should-not-exist-branch-demo
```

Expected outcome: HTTP status is `404` with error code `PROFILE_NOT_FOUND`.

## Run Automated Tests

```bash
./mvnw test
```

Expected coverage:

- Successful profile aggregation and required field mapping.
- Optional `null` profile fields.
- Empty repository list.
- Username validation failures.
- Missing GitHub user.
- Upstream rate-limit, timeout, and unavailable responses.
- Paginated repository retrieval and page-level upstream failure handling.
- Optional `X-GitHub-Token` passthrough to GitHub.
- Short-lived cache avoids duplicate successful upstream lookups while partitioning public and token-authenticated cache entries.

## Final Verification

Before submitting the take-home:

```bash
./mvnw clean compile
./mvnw test
./mvnw verify
```

The README should explain setup, usage, architectural decisions, test strategy, and known tradeoffs so an evaluator can run the project in under 10 minutes.
