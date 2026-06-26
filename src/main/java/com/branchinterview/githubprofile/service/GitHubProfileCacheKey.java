package com.branchinterview.githubprofile.service;

import com.branchinterview.githubprofile.client.GitHubAuthContext;
import com.branchinterview.githubprofile.config.CoverageGenerated;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Builds cache keys that separate public GitHub lookups from token-authenticated lookups.
 * Token values are fingerprinted so raw credentials are never stored as cache keys.
 */
final class GitHubProfileCacheKey {

    private static final String PUBLIC_CACHE_PREFIX = "public";
    private static final String TOKEN_CACHE_PREFIX = "github-token";

    @CoverageGenerated
    private GitHubProfileCacheKey() {
    }

    static String forLookup(String username, GitHubAuthContext authContext) {
        if (!authContext.hasToken()) {
            return PUBLIC_CACHE_PREFIX + ":" + username;
        }
        return TOKEN_CACHE_PREFIX + ":" + fingerprint(authContext.token()) + ":" + username;
    }

    private static String fingerprint(String token) {
        var digest = sha256Digest().digest(token.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest);
    }

    @CoverageGenerated
    private static MessageDigest sha256Digest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required for GitHub token cache partitioning.", exception);
        }
    }
}
