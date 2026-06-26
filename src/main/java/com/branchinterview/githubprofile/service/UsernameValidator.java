package com.branchinterview.githubprofile.service;

import com.branchinterview.githubprofile.client.GitHubClientException;
import com.branchinterview.githubprofile.client.GitHubClientException.ErrorCategory;
import org.springframework.stereotype.Component;

@Component
public class UsernameValidator {

    private static final String GITHUB_USERNAME_PATTERN = "^[A-Za-z0-9](?:[A-Za-z0-9-]{0,37}[A-Za-z0-9])?$";

    public String validate(String username) {
        if (username == null || username.isBlank() || !username.matches(GITHUB_USERNAME_PATTERN)) {
            throw new GitHubClientException(
                    ErrorCategory.INVALID_USERNAME,
                    "Username must be 1-39 characters and contain only alphanumeric characters or single hyphens not at the boundaries.");
        }
        return username;
    }

    public String normalize(String username) {
        return validate(username).toLowerCase();
    }
}
