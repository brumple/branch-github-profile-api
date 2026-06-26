package com.branchinterview.githubprofile.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.branchinterview.githubprofile.client.GitHubRepositoryResponse;
import com.branchinterview.githubprofile.client.GitHubUserResponse;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class GitHubProfileMapperTest {

    private final GitHubProfileMapper mapper = new GitHubProfileMapper();

    @Test
    void mapsRequiredFieldsAndFormatsCreatedAt() {
        var user = new GitHubUserResponse(
                "octocat",
                "The Octocat",
                "https://avatars.githubusercontent.com/u/583231?v=4",
                "San Francisco",
                null,
                "https://api.github.com/users/octocat",
                Instant.parse("2011-01-25T18:44:36Z"));
        var repos = List.of(new GitHubRepositoryResponse(
                "boysenberry-repo-1",
                "https://api.github.com/repos/octocat/boysenberry-repo-1"));

        var profile = mapper.toProfile(user, repos);

        assertThat(profile.userName()).isEqualTo("octocat");
        assertThat(profile.displayName()).isEqualTo("The Octocat");
        assertThat(profile.avatar()).isEqualTo("https://avatars.githubusercontent.com/u/583231?v=4");
        assertThat(profile.geoLocation()).isEqualTo("San Francisco");
        assertThat(profile.email()).isNull();
        assertThat(profile.url()).isEqualTo("https://api.github.com/users/octocat");
        assertThat(profile.createdAt()).isEqualTo("Tue, 25 Jan 2011 18:44:36 GMT");
        assertThat(profile.repos()).hasSize(1);
        assertThat(profile.repos().getFirst().name()).isEqualTo("boysenberry-repo-1");
    }

    @Test
    void preservesNullOptionalFieldsAndEmptyRepositories() {
        var user = new GitHubUserResponse(
                "empty",
                null,
                null,
                null,
                null,
                "https://api.github.com/users/empty",
                Instant.parse("2020-01-01T00:00:00Z"));

        var profile = mapper.toProfile(user, List.of());

        assertThat(profile.displayName()).isNull();
        assertThat(profile.avatar()).isNull();
        assertThat(profile.geoLocation()).isNull();
        assertThat(profile.email()).isNull();
        assertThat(profile.repos()).isEmpty();
    }
}
