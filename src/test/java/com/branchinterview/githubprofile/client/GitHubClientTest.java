package com.branchinterview.githubprofile.client;

import static org.assertj.core.api.Assertions.assertThat;

import com.branchinterview.githubprofile.client.GitHubAuthContext;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class GitHubClientTest {

    private MockWebServer server;
    private GitHubClient client;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        client = new GitHubClient(RestClient.builder().baseUrl(server.url("/").toString()).build());
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    void fetchesUserProfile() throws Exception {
        server.enqueue(jsonResponse("""
                {
                  "login": "octocat",
                  "name": "The Octocat",
                  "avatar_url": "https://avatars.githubusercontent.com/u/583231?v=4",
                  "location": "San Francisco",
                  "email": null,
                  "url": "https://api.github.com/users/octocat",
                  "created_at": "2011-01-25T18:44:36Z"
                }
                """));

        var user = client.getUser("octocat");

        assertThat(user.login()).isEqualTo("octocat");
        assertThat(user.name()).isEqualTo("The Octocat");
        assertThat(user.createdAt().toString()).isEqualTo("2011-01-25T18:44:36Z");
        assertThat(server.takeRequest().getPath()).isEqualTo("/users/octocat");
    }

    @Test
    void forwardsGitHubTokenToUserAndRepositoryRequests() throws Exception {
        server.enqueue(jsonResponse("""
                {
                  "login": "octocat",
                  "name": "The Octocat",
                  "avatar_url": "https://avatars.githubusercontent.com/u/583231?v=4",
                  "location": "San Francisco",
                  "email": "octocat@example.com",
                  "url": "https://api.github.com/users/octocat",
                  "created_at": "2011-01-25T18:44:36Z"
                }
                """));
        server.enqueue(jsonResponse("[]"));
        var authContext = GitHubAuthContext.fromHeader("github-token");

        client.getUser("octocat", authContext);
        client.getRepositories("octocat", authContext);

        assertThat(server.takeRequest().getHeader("Authorization")).isEqualTo("Bearer github-token");
        assertThat(server.takeRequest().getHeader("Authorization")).isEqualTo("Bearer github-token");
    }

    @Test
    void fetchesRepositories() throws Exception {
        server.enqueue(jsonResponse("""
                [
                  {
                    "name": "boysenberry-repo-1",
                    "url": "https://api.github.com/repos/octocat/boysenberry-repo-1"
                  }
                ]
                """));

        var repositories = client.getRepositories("octocat");

        assertThat(repositories).hasSize(1);
        assertThat(repositories.getFirst().name()).isEqualTo("boysenberry-repo-1");
        assertThat(server.takeRequest().getPath()).isEqualTo("/users/octocat/repos?per_page=100&page=1");
    }

    @Test
    void fetchesPaginatedRepositoriesUntilNoNextLink() throws Exception {
        server.enqueue(jsonResponse("""
                [
                  {
                    "name": "repo-1",
                    "url": "https://api.github.com/repos/octocat/repo-1"
                  }
                ]
                """).setHeader("Link", "<https://api.github.com/users/octocat/repos?per_page=100&page=2>; rel=\"next\""));
        server.enqueue(jsonResponse("""
                [
                  {
                    "name": "repo-2",
                    "url": "https://api.github.com/repos/octocat/repo-2"
                  }
                ]
                """));

        var repositories = client.getRepositories("octocat");

        assertThat(repositories).extracting(GitHubRepositoryResponse::name).containsExactly("repo-1", "repo-2");
        assertThat(server.takeRequest().getPath()).isEqualTo("/users/octocat/repos?per_page=100&page=1");
        assertThat(server.takeRequest().getPath()).isEqualTo("/users/octocat/repos?per_page=100&page=2");
    }

    @Test
    void stopsRepositoryPaginationAtConfiguredPageLimit() throws Exception {
        client = new GitHubClient(RestClient.builder().baseUrl(server.url("/").toString()).build(), 1);
        server.enqueue(jsonResponse("""
                [
                  {
                    "name": "repo-1",
                    "url": "https://api.github.com/repos/octocat/repo-1"
                  }
                ]
                """).setHeader("Link", "<https://api.github.com/users/octocat/repos?per_page=100&page=2>; rel=\"next\""));

        var repositories = client.getRepositories("octocat");

        assertThat(repositories).extracting(GitHubRepositoryResponse::name).containsExactly("repo-1");
        assertThat(server.getRequestCount()).isEqualTo(1);
        assertThat(server.takeRequest().getPath()).isEqualTo("/users/octocat/repos?per_page=100&page=1");
    }

    private MockResponse jsonResponse(String body) {
        return new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }
}
