package com.branchinterview.githubprofile.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.branchinterview.githubprofile.client.GitHubClient;
import com.branchinterview.githubprofile.client.GitHubClientException;
import com.branchinterview.githubprofile.client.GitHubClientException.ErrorCategory;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

class GitHubProfileServiceErrorTest {

    private final GitHubClient client = org.mockito.Mockito.mock(GitHubClient.class);
    private final GitHubProfileService service = new GitHubProfileService(
            client,
            new GitHubProfileMapper(),
            new UsernameValidator(),
            new ConcurrentMapCacheManager("githubProfiles"));

    @Test
    void rejectsInvalidUsernameBeforeCallingGitHub() {
        assertThatThrownBy(() -> service.getProfile("-"))
                .isInstanceOfSatisfying(GitHubClientException.class, exception ->
                        assertThat(exception.category()).isEqualTo(ErrorCategory.INVALID_USERNAME));

        verify(client, never()).getUser(org.mockito.ArgumentMatchers.anyString());
        verify(client, never()).getRepositories(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void propagatesLookupFailureWithoutSuccessfulProfile() {
        when(client.getUser(org.mockito.ArgumentMatchers.eq("missing"), org.mockito.ArgumentMatchers.any())).thenThrow(new GitHubClientException(
                ErrorCategory.PROFILE_NOT_FOUND,
                "GitHub profile was not found."));

        assertThatThrownBy(() -> service.getProfile("missing"))
                .isInstanceOfSatisfying(GitHubClientException.class, exception ->
                        assertThat(exception.category()).isEqualTo(ErrorCategory.PROFILE_NOT_FOUND));

        verify(client, never()).getRepositories(org.mockito.ArgumentMatchers.eq("missing"), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void propagatesRepositoryFailureWithoutCachingPartialProfile() {
        when(client.getUser(org.mockito.ArgumentMatchers.eq("octocat"), org.mockito.ArgumentMatchers.any())).thenReturn(GitHubProfileServiceTest.user());
        when(client.getRepositories(org.mockito.ArgumentMatchers.eq("octocat"), org.mockito.ArgumentMatchers.any())).thenThrow(new GitHubClientException(
                ErrorCategory.UPSTREAM_UNAVAILABLE,
                "GitHub data could not be retrieved."));

        assertThatThrownBy(() -> service.getProfile("octocat"))
                .isInstanceOfSatisfying(GitHubClientException.class, exception ->
                        assertThat(exception.category()).isEqualTo(ErrorCategory.UPSTREAM_UNAVAILABLE));

        doReturn(List.of()).when(client).getRepositories(org.mockito.ArgumentMatchers.eq("octocat"), org.mockito.ArgumentMatchers.any());

        var profile = service.getProfile("octocat");

        assertThat(profile.repos()).isEmpty();
    }
}
