package com.branchinterview.githubprofile.service;

import com.branchinterview.githubprofile.client.GitHubAuthContext;
import com.branchinterview.githubprofile.client.GitHubClient;
import com.branchinterview.githubprofile.config.CacheConfig;
import com.branchinterview.githubprofile.domain.GitHubProfile;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class GitHubProfileService {

    private final GitHubClient gitHubClient;
    private final GitHubProfileMapper mapper;
    private final UsernameValidator usernameValidator;
    private final Cache cache;

    public GitHubProfileService(
            GitHubClient gitHubClient,
            GitHubProfileMapper mapper,
            UsernameValidator usernameValidator,
            CacheManager cacheManager) {
        this.gitHubClient = gitHubClient;
        this.mapper = mapper;
        this.usernameValidator = usernameValidator;
        this.cache = cacheManager.getCache(CacheConfig.PROFILE_CACHE);
    }

    public GitHubProfile getProfile(String username) {
        return getProfile(username, null);
    }

    public GitHubProfile getProfile(String username, String gitHubToken) {
        var lookupUsername = usernameValidator.normalize(username);
        var authContext = GitHubAuthContext.fromHeader(gitHubToken);
        var cacheKey = GitHubProfileCacheKey.forLookup(lookupUsername, authContext);
        if (cache == null) {
            return loadProfile(lookupUsername, authContext);
        }
        try {
            return cache.get(cacheKey, () -> loadProfile(lookupUsername, authContext));
        } catch (Cache.ValueRetrievalException exception) {
            throw (RuntimeException) exception.getCause();
        }
    }

    private GitHubProfile loadProfile(String username, GitHubAuthContext authContext) {
        var user = gitHubClient.getUser(username, authContext);
        var repositories = gitHubClient.getRepositories(username, authContext);
        return mapper.toProfile(user, repositories);
    }
}
