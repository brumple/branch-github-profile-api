# Data Model: GitHub Profile API

## GitHubProfile

Client-facing profile summary returned for a successful username lookup.

### Fields

- `userName`: required string; maps to response field `user_name`
- `displayName`: nullable string; maps to response field `display_name`
- `avatar`: nullable absolute URL string; maps to response field `avatar`
- `geoLocation`: nullable string; maps to response field `geo_location`
- `email`: nullable email string; maps to response field `email`
- `url`: required absolute URL string; maps to response field `url`
- `createdAt`: required HTTP-date string; maps to response field `created_at`
- `repos`: required list of `RepositorySummary`; may be empty

### Validation Rules

- `userName` must match the accepted GitHub username format.
- `url` and repository URLs must be absolute URLs.
- `repos` must never be `null`.
- Optional GitHub profile values remain `null` when unavailable.

## RepositorySummary

Client-facing summary of one public repository.

### Fields

- `name`: required string
- `url`: required absolute URL string

### Relationships

- Belongs to exactly one `GitHubProfile` response.

## LookupError

Client-facing error response for validation and lookup failures.

### Fields

- `code`: required stable machine-readable error code
- `message`: required human-readable message safe for clients
- `status`: required numeric response status
- `requestId`: optional correlation identifier when available

### Error Codes

- `INVALID_USERNAME`: username is blank, malformed, or outside allowed length.
- `PROFILE_NOT_FOUND`: GitHub reports no public user for the supplied username.
- `UPSTREAM_RATE_LIMITED`: GitHub rate limiting prevents lookup completion.
- `UPSTREAM_TIMEOUT`: GitHub does not respond within the configured timeout.
- `UPSTREAM_UNAVAILABLE`: GitHub data cannot be retrieved for another temporary reason.
- `INTERNAL_ERROR`: unexpected service failure with details hidden from clients.

## CachedLookupResult

Short-lived successful lookup result retained to reduce duplicate external requests.

### Fields

- `usernameKey`: required normalized username key
- `profile`: required `GitHubProfile`
- `cachedAt`: required timestamp
- `expiresAt`: required timestamp

### State Rules

- Fresh cached entries may be returned without external GitHub calls.
- Expired cached entries must not be returned as successful current results.
- Failed lookups are not cached for the initial implementation unless tasks later justify negative caching.

## GitHubUserPayload

Internal representation of public GitHub user data retrieved from GitHub.

### Relevant Fields

- `login`
- `name`
- `avatar_url`
- `location`
- `email`
- `url`
- `created_at`

### Mapping Rules

- `login` maps to `GitHubProfile.userName`.
- `name` maps to `GitHubProfile.displayName`.
- `avatar_url` maps to `GitHubProfile.avatar`.
- `location` maps to `GitHubProfile.geoLocation`.
- `email` maps to `GitHubProfile.email`.
- `url` maps to `GitHubProfile.url`.
- `created_at` is converted to the required HTTP-date output.

## GitHubRepositoryPayload

Internal representation of public GitHub repository data retrieved from GitHub.

### Relevant Fields

- `name`
- `url`

### Mapping Rules

- Each payload item maps to one `RepositorySummary`.
- Repository payloads are retrieved from GitHub using paginated requests with `per_page=100`.
- Pagination stops when GitHub omits a `rel="next"` link or the configured maximum page count is reached.
- Missing required repository fields cause the affected upstream response to be treated as invalid upstream data.

## GitHubAuthContext

Request-scoped authentication context for optional GitHub token passthrough.

### Fields

- `token`: optional raw GitHub token from `X-GitHub-Token`; used only to set outbound GitHub `Authorization: Bearer ...`.

### State Rules

- Blank or missing token is treated as unauthenticated.
- Raw tokens are not logged, stored, returned, or used directly in cache keys.
- Authenticated cache keys use a SHA-256 token fingerprint plus normalized username.
- Unauthenticated cache keys use a public namespace plus normalized username.
