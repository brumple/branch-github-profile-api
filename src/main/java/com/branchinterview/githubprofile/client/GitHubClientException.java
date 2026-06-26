package com.branchinterview.githubprofile.client;

import org.springframework.http.HttpStatus;

public class GitHubClientException extends RuntimeException {

    private final ErrorCategory category;

    public GitHubClientException(ErrorCategory category, String message) {
        super(message);
        this.category = category;
    }

    public ErrorCategory category() {
        return category;
    }

    public enum ErrorCategory {
        INVALID_USERNAME(HttpStatus.BAD_REQUEST, "INVALID_USERNAME"),
        PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, "PROFILE_NOT_FOUND"),
        UPSTREAM_RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "UPSTREAM_RATE_LIMITED"),
        UPSTREAM_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "UPSTREAM_TIMEOUT"),
        UPSTREAM_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "UPSTREAM_UNAVAILABLE");

        private final HttpStatus status;
        private final String code;

        ErrorCategory(HttpStatus status, String code) {
            this.status = status;
            this.code = code;
        }

        public HttpStatus status() {
            return status;
        }

        public String code() {
            return code;
        }
    }
}
