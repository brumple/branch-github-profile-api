package com.branchinterview.githubprofile.service;

import com.branchinterview.githubprofile.client.GitHubRepositoryResponse;
import com.branchinterview.githubprofile.client.GitHubUserResponse;
import com.branchinterview.githubprofile.domain.GitHubProfile;
import com.branchinterview.githubprofile.domain.RepositorySummary;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GitHubProfileMapper {

    public GitHubProfile toProfile(GitHubUserResponse user, List<GitHubRepositoryResponse> repositories) {
        var repos = repositories.stream()
                .map(repository -> new RepositorySummary(repository.name(), repository.url()))
                .toList();

        return new GitHubProfile(
                user.login(),
                user.name(),
                user.avatarUrl(),
                user.location(),
                user.email(),
                user.url(),
                DateTimeFormatter.RFC_1123_DATE_TIME.format(user.createdAt().atOffset(ZoneOffset.UTC)),
                repos);
    }
}
