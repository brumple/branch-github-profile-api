package com.branchinterview.githubprofile.client;

import com.branchinterview.githubprofile.client.GitHubClientException.ErrorCategory;
import com.branchinterview.githubprofile.config.CoverageGenerated;
import java.net.SocketTimeoutException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

/**
 * Centralizes GitHub upstream error classification shared by user and repository requests.
 */
final class GitHubErrorMapper {

    @CoverageGenerated
    private GitHubErrorMapper() {
    }

    static GitHubClientException rateLimited() {
        return new GitHubClientException(
                ErrorCategory.UPSTREAM_RATE_LIMITED,
                "GitHub rate limit prevents this lookup right now.");
    }

    static boolean isRateLimitResponse(HttpClientErrorException.Forbidden exception) {
        var remaining = exception.getResponseHeaders() == null
                ? null
                : exception.getResponseHeaders().getFirst("X-RateLimit-Remaining");
        var body = exception.getResponseBodyAsString().toLowerCase();
        return "0".equals(remaining) || body.contains("rate limit");
    }

    static GitHubClientException mapResourceAccess(ResourceAccessException exception) {
        if (isTimeout(exception)) {
            return new GitHubClientException(ErrorCategory.UPSTREAM_TIMEOUT, "GitHub lookup timed out.");
        }
        return new GitHubClientException(ErrorCategory.UPSTREAM_UNAVAILABLE, "GitHub data could not be retrieved.");
    }

    static GitHubClientException mapRestClientException(RestClientException exception) {
        if (isTimeout(exception)) {
            return new GitHubClientException(ErrorCategory.UPSTREAM_TIMEOUT, "GitHub lookup timed out.");
        }
        return new GitHubClientException(ErrorCategory.UPSTREAM_UNAVAILABLE, "GitHub data could not be retrieved.");
    }

    private static boolean isTimeout(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof SocketTimeoutException) {
                return true;
            }
            var message = current.getMessage();
            if (message != null && message.toLowerCase().contains("timed out")) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
