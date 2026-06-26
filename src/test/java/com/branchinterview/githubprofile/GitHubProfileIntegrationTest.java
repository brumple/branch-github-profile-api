package com.branchinterview.githubprofile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class GitHubProfileIntegrationTest {

    private static MockWebServer gitHub;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CacheManager cacheManager;

    @BeforeAll
    static void startGitHub() throws IOException {
        gitHub = new MockWebServer();
        gitHub.start();
    }

    @AfterAll
    static void stopGitHub() throws IOException {
        gitHub.shutdown();
    }

    @DynamicPropertySource
    static void gitHubProperties(DynamicPropertyRegistry registry) {
        registry.add("github.api.base-url", () -> gitHub.url("/").toString());
        registry.add("github.cache.ttl", () -> "5m");
        registry.add("github.api.max-repository-pages", () -> "2");
    }

    @Test
    void forwardsGitHubTokenThroughHttpEndpointAndCachesByToken() throws Exception {
        clearCache();
        gitHub.enqueue(gitHubUser("token@example.com"));
        gitHub.enqueue(gitHubRepos("token-repo"));

        mockMvc.perform(get("/users/octocat").header("X-GitHub-Token", "github-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("token@example.com"))
                .andExpect(jsonPath("$.repos[0].name").value("token-repo"));

        assertThat(gitHub.takeRequest().getHeader("Authorization")).isEqualTo("Bearer github-token");
        assertThat(gitHub.takeRequest().getHeader("Authorization")).isEqualTo("Bearer github-token");

        mockMvc.perform(get("/users/octocat").header("X-GitHub-Token", "github-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("token@example.com"));

        assertThat(gitHub.takeRequest(200, TimeUnit.MILLISECONDS)).isNull();
    }

    @Test
    void separatesUnauthenticatedAndGitHubTokenCacheEntries() throws Exception {
        clearCache();
        gitHub.enqueue(gitHubUser(null));
        gitHub.enqueue(gitHubRepos("public-repo"));
        gitHub.enqueue(gitHubUser("token@example.com"));
        gitHub.enqueue(gitHubRepos("token-repo"));

        mockMvc.perform(get("/users/octocat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").doesNotExist())
                .andExpect(jsonPath("$.repos[0].name").value("public-repo"));

        mockMvc.perform(get("/users/octocat").header("X-GitHub-Token", "github-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("token@example.com"))
                .andExpect(jsonPath("$.repos[0].name").value("token-repo"));

        assertThat(gitHub.takeRequest().getHeader("Authorization")).isNull();
        assertThat(gitHub.takeRequest().getHeader("Authorization")).isNull();
        assertThat(gitHub.takeRequest().getHeader("Authorization")).isEqualTo("Bearer github-token");
        assertThat(gitHub.takeRequest().getHeader("Authorization")).isEqualTo("Bearer github-token");
    }

    @Test
    void treatsBlankGitHubTokenHeaderAsUnauthenticated() throws Exception {
        clearCache();
        gitHub.enqueue(gitHubUser(null));
        gitHub.enqueue(gitHubRepos("public-repo"));

        mockMvc.perform(get("/users/octocat").header("X-GitHub-Token", "   "))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.repos[0].name").value("public-repo"));

        assertThat(gitHub.takeRequest().getHeader("Authorization")).isNull();
        assertThat(gitHub.takeRequest().getHeader("Authorization")).isNull();
    }

    private void clearCache() {
        var cache = cacheManager.getCache("githubProfiles");
        if (cache != null) {
            cache.clear();
        }
    }

    private MockResponse gitHubUser(String email) {
        return jsonResponse("""
                {
                  "login": "octocat",
                  "name": "The Octocat",
                  "avatar_url": "https://avatars.githubusercontent.com/u/583231?v=4",
                  "location": "San Francisco",
                  "email": %s,
                  "url": "https://api.github.com/users/octocat",
                  "created_at": "2011-01-25T18:44:36Z"
                }
                """.formatted(email == null ? "null" : "\"" + email + "\""));
    }

    private MockResponse gitHubRepos(String repositoryName) {
        return jsonResponse("""
                [
                  {
                    "name": "%s",
                    "url": "https://api.github.com/repos/octocat/%s"
                  }
                ]
                """.formatted(repositoryName, repositoryName));
    }

    private MockResponse jsonResponse(String body) {
        return new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(body);
    }
}
