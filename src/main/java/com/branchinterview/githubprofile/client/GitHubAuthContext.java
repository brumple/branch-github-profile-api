package com.branchinterview.githubprofile.client;

/**
 * Optional upstream GitHub authentication context for a lookup.
 * This is not service-level authentication; tokens are only forwarded to GitHub.
 */
public record GitHubAuthContext(String token) {

    public static GitHubAuthContext fromHeader(String tokenHeader) {
        if (tokenHeader == null || tokenHeader.isBlank()) {
            return new GitHubAuthContext(null);
        }
        return new GitHubAuthContext(tokenHeader.trim());
    }

    public boolean hasToken() {
        return token != null;
    }
}
