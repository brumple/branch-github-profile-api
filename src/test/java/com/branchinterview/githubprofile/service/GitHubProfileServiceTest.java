package com.branchinterview.githubprofile.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.branchinterview.githubprofile.client.GitHubClient;
import com.branchinterview.githubprofile.client.GitHubRepositoryResponse;
import com.branchinterview.githubprofile.client.GitHubUserResponse;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

class GitHubProfileServiceTest {

    private final GitHubClient client = org.mockito.Mockito.mock(GitHubClient.class);
    private final GitHubProfileService service = new GitHubProfileService(
            client,
            new GitHubProfileMapper(),
            new UsernameValidator(),
            new ConcurrentMapCacheManager("githubProfiles"));

    @Test
    void aggregatesUserAndRepositoryData() {
        when(client.getUser(org.mockito.ArgumentMatchers.eq("octocat"), org.mockito.ArgumentMatchers.any())).thenReturn(user());
        when(client.getRepositories(org.mockito.ArgumentMatchers.eq("octocat"), org.mockito.ArgumentMatchers.any())).thenReturn(List.of(
                new GitHubRepositoryResponse("repo", "https://api.github.com/repos/octocat/repo")));

        var profile = service.getProfile("octocat");

        assertThat(profile.userName()).isEqualTo("octocat");
        assertThat(profile.repos()).hasSize(1);
        verify(client).getUser(org.mockito.ArgumentMatchers.eq("octocat"), org.mockito.ArgumentMatchers.any());
        verify(client).getRepositories(org.mockito.ArgumentMatchers.eq("octocat"), org.mockito.ArgumentMatchers.any());
    }

    static GitHubUserResponse user() {
        return new GitHubUserResponse(
                "octocat",
                "The Octocat",
                "https://avatars.githubusercontent.com/u/583231?v=4",
                "San Francisco",
                null,
                "https://api.github.com/users/octocat",
                Instant.parse("2011-01-25T18:44:36Z"));
    }
}
