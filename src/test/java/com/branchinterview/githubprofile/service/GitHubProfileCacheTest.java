package com.branchinterview.githubprofile.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.branchinterview.githubprofile.client.GitHubClient;
import com.branchinterview.githubprofile.client.GitHubRepositoryResponse;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.support.NoOpCacheManager;

class GitHubProfileCacheTest {

    @Test
    void repeatedSuccessfulLookupUsesCache() {
        var client = org.mockito.Mockito.mock(GitHubClient.class);
        var service = new GitHubProfileService(
                client,
                new GitHubProfileMapper(),
                new UsernameValidator(),
                new ConcurrentMapCacheManager("githubProfiles"));
        when(client.getUser(org.mockito.ArgumentMatchers.eq("octocat"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(GitHubProfileServiceTest.user());
        when(client.getRepositories(org.mockito.ArgumentMatchers.eq("octocat"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(new GitHubRepositoryResponse("repo", "https://api.github.com/repos/octocat/repo")));

        var first = service.getProfile("OctoCat");
        var second = service.getProfile("octocat");

        assertThat(second).isSameAs(first);
        verify(client, times(1)).getUser(org.mockito.ArgumentMatchers.eq("octocat"), org.mockito.ArgumentMatchers.any());
        verify(client, times(1)).getRepositories(org.mockito.ArgumentMatchers.eq("octocat"), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void cacheSeparatesPublicAndGitHubTokenLookups() {
        var client = org.mockito.Mockito.mock(GitHubClient.class);
        var service = new GitHubProfileService(
                client,
                new GitHubProfileMapper(),
                new UsernameValidator(),
                new ConcurrentMapCacheManager("githubProfiles"));
        when(client.getUser(org.mockito.ArgumentMatchers.eq("octocat"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(GitHubProfileServiceTest.user());
        when(client.getRepositories(org.mockito.ArgumentMatchers.eq("octocat"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(new GitHubRepositoryResponse("repo", "https://api.github.com/repos/octocat/repo")));

        var publicProfile = service.getProfile("octocat");
        var tokenProfile = service.getProfile("octocat", "github-token");
        var repeatedTokenProfile = service.getProfile("octocat", "github-token");

        assertThat(tokenProfile).isSameAs(repeatedTokenProfile);
        assertThat(tokenProfile).isNotSameAs(publicProfile);
        verify(client, times(2)).getUser(org.mockito.ArgumentMatchers.eq("octocat"), org.mockito.ArgumentMatchers.any());
        verify(client, times(2)).getRepositories(org.mockito.ArgumentMatchers.eq("octocat"), org.mockito.ArgumentMatchers.any());
        verify(client, never()).getUser("octocat");
        verify(client, never()).getRepositories("octocat");
    }

    @Test
    void worksWhenProfileCacheIsUnavailable() {
        var client = org.mockito.Mockito.mock(GitHubClient.class);
        var service = new GitHubProfileService(
                client,
                new GitHubProfileMapper(),
                new UsernameValidator(),
                new NoOpCacheManager());
        when(client.getUser(org.mockito.ArgumentMatchers.eq("octocat"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(GitHubProfileServiceTest.user());
        when(client.getRepositories(org.mockito.ArgumentMatchers.eq("octocat"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(new GitHubRepositoryResponse("repo", "https://api.github.com/repos/octocat/repo")));

        var first = service.getProfile("octocat");
        var second = service.getProfile("octocat");

        assertThat(first).isNotSameAs(second);
        verify(client, times(2)).getUser(org.mockito.ArgumentMatchers.eq("octocat"), org.mockito.ArgumentMatchers.any());
        verify(client, times(2)).getRepositories(org.mockito.ArgumentMatchers.eq("octocat"), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void worksWhenProfileCacheIsNotConfigured() {
        var client = org.mockito.Mockito.mock(GitHubClient.class);
        var service = new GitHubProfileService(
                client,
                new GitHubProfileMapper(),
                new UsernameValidator(),
                new MissingCacheManager());
        when(client.getUser(org.mockito.ArgumentMatchers.eq("octocat"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(GitHubProfileServiceTest.user());
        when(client.getRepositories(org.mockito.ArgumentMatchers.eq("octocat"), org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of(new GitHubRepositoryResponse("repo", "https://api.github.com/repos/octocat/repo")));

        var first = service.getProfile("octocat");
        var second = service.getProfile("octocat");

        assertThat(first).isNotSameAs(second);
        verify(client, times(2)).getUser(org.mockito.ArgumentMatchers.eq("octocat"), org.mockito.ArgumentMatchers.any());
        verify(client, times(2)).getRepositories(org.mockito.ArgumentMatchers.eq("octocat"), org.mockito.ArgumentMatchers.any());
    }

    private static final class MissingCacheManager implements CacheManager {
        @Override
        public Cache getCache(String name) {
            return null;
        }

        @Override
        public java.util.Collection<String> getCacheNames() {
            return List.of();
        }
    }
}

