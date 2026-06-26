package com.branchinterview.githubprofile.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.branchinterview.githubprofile.client.GitHubClientException;
import com.branchinterview.githubprofile.client.GitHubClientException.ErrorCategory;
import org.junit.jupiter.api.Test;

class UsernameValidatorTest {

    private final UsernameValidator validator = new UsernameValidator();

    @Test
    void acceptsValidUsernames() {
        assertThat(validator.validate("octocat")).isEqualTo("octocat");
        assertThat(validator.validate("branch-123")).isEqualTo("branch-123");
        assertThat(validator.validate("a")).isEqualTo("a");
    }

    @Test
    void rejectsInvalidUsernames() {
        assertInvalid(null);
        assertInvalid("");
        assertInvalid(" ");
        assertInvalid("-octocat");
        assertInvalid("octocat-");
        assertInvalid("octo_cat");
        assertInvalid("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
    }

    @Test
    void normalizesToLowercase() {
        assertThat(validator.normalize("OctoCat")).isEqualTo("octocat");
    }

    private void assertInvalid(String username) {
        assertThatThrownBy(() -> validator.validate(username))
                .isInstanceOfSatisfying(GitHubClientException.class, exception ->
                        assertThat(exception.category()).isEqualTo(ErrorCategory.INVALID_USERNAME));
    }
}
