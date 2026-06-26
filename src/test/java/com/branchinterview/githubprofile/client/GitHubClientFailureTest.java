package com.branchinterview.githubprofile.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.branchinterview.githubprofile.client.GitHubClientException.ErrorCategory;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClient;

class GitHubClientFailureTest {

    private MockWebServer server;
    private GitHubClient client;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
        client = client(Duration.ofSeconds(2));
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    void mapsNotFound() {
        server.enqueue(new MockResponse().setResponseCode(404));

        assertFailure(() -> client.getUser("missing"), ErrorCategory.PROFILE_NOT_FOUND);
    }

    @Test
    void mapsRateLimit() {
        server.enqueue(new MockResponse().setResponseCode(429));

        assertFailure(() -> client.getUser("octocat"), ErrorCategory.UPSTREAM_RATE_LIMITED);
    }

    @Test
    void mapsForbiddenRateLimitHeader() {
        server.enqueue(new MockResponse()
                .setResponseCode(403)
                .setHeader("X-RateLimit-Remaining", "0")
                .setBody("{\"message\":\"API rate limit exceeded\"}"));

        assertFailure(() -> client.getUser("octocat"), ErrorCategory.UPSTREAM_RATE_LIMITED);
    }

    @Test
    void mapsForbiddenSecondaryRateLimitBody() {
        server.enqueue(new MockResponse()
                .setResponseCode(403)
                .setBody("{\"message\":\"You have exceeded a secondary rate limit\"}"));

        assertFailure(() -> client.getRepositories("octocat"), ErrorCategory.UPSTREAM_RATE_LIMITED);
    }

    @Test
    void mapsForbiddenNonRateLimitToUnavailableForUser() {
        server.enqueue(new MockResponse()
                .setResponseCode(403)
                .setBody("{\"message\":\"Forbidden\"}"));

        assertFailure(() -> client.getUser("octocat"), ErrorCategory.UPSTREAM_UNAVAILABLE);
    }

    @Test
    void mapsForbiddenNonRateLimitToUnavailableForRepositories() {
        server.enqueue(new MockResponse()
                .setResponseCode(403)
                .setBody("{\"message\":\"Forbidden\"}"));

        assertFailure(() -> client.getRepositories("octocat"), ErrorCategory.UPSTREAM_UNAVAILABLE);
    }

    @Test
    void treatsForbiddenWithoutHeadersAsNonRateLimit() {
        var exception = (HttpClientErrorException.Forbidden) HttpClientErrorException.create(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                null,
                "{\"message\":\"Forbidden\"}".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8);

        assertThat(GitHubErrorMapper.isRateLimitResponse(exception)).isFalse();
    }

    @Test
    void mapsRepositoryNotFound() {
        server.enqueue(new MockResponse().setResponseCode(404));

        assertFailure(() -> client.getRepositories("missing"), ErrorCategory.PROFILE_NOT_FOUND);
    }

    @Test
    void mapsRepositoryRateLimit() {
        server.enqueue(new MockResponse().setResponseCode(429));

        assertFailure(() -> client.getRepositories("octocat"), ErrorCategory.UPSTREAM_RATE_LIMITED);
    }

    @Test
    void mapsServerFailure() {
        server.enqueue(new MockResponse().setResponseCode(503));

        assertFailure(() -> client.getUser("octocat"), ErrorCategory.UPSTREAM_UNAVAILABLE);
    }

    @Test
    void mapsRepositoryServerFailure() {
        server.enqueue(new MockResponse().setResponseCode(503));

        assertFailure(() -> client.getRepositories("octocat"), ErrorCategory.UPSTREAM_UNAVAILABLE);
    }

    @Test
    void mapsRepositoryFailureOnSecondPage() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setHeader("Link", "<https://api.github.com/users/octocat/repos?per_page=100&page=2>; rel=\"next\"")
                .setBody("[]"));
        server.enqueue(new MockResponse().setResponseCode(503));

        assertFailure(() -> client.getRepositories("octocat"), ErrorCategory.UPSTREAM_UNAVAILABLE);
    }

    @Test
    void mapsInvalidProfileJson() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{"));

        assertFailure(() -> client.getUser("octocat"), ErrorCategory.UPSTREAM_UNAVAILABLE);
    }

    @Test
    void mapsInvalidRepositoryJson() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{"));

        assertFailure(() -> client.getRepositories("octocat"), ErrorCategory.UPSTREAM_UNAVAILABLE);
    }

    @Test
    void mapsNullRepositoryPayload() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json"));

        assertFailure(() -> client.getRepositories("octocat"), ErrorCategory.UPSTREAM_UNAVAILABLE);
    }

    @Test
    void mapsMalformedProfilePayload() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"login\":\"octocat\"}"));

        assertFailure(() -> client.getUser("octocat"), ErrorCategory.UPSTREAM_UNAVAILABLE);
    }

    @Test
    void mapsNullProfilePayload() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json"));

        assertFailure(() -> client.getUser("octocat"), ErrorCategory.UPSTREAM_UNAVAILABLE);
    }

    @Test
    void mapsProfilePayloadWithMissingLogin() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"url\":\"https://api.github.com/users/octocat\",\"created_at\":\"2011-01-25T18:44:36Z\"}"));

        assertFailure(() -> client.getUser("octocat"), ErrorCategory.UPSTREAM_UNAVAILABLE);
    }

    @Test
    void mapsProfilePayloadWithMissingCreatedAt() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"login\":\"octocat\",\"url\":\"https://api.github.com/users/octocat\"}"));

        assertFailure(() -> client.getUser("octocat"), ErrorCategory.UPSTREAM_UNAVAILABLE);
    }

    @Test
    void mapsMalformedRepositoryPayload() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("[{\"name\":\"repo\"}]"));

        assertFailure(() -> client.getRepositories("octocat"), ErrorCategory.UPSTREAM_UNAVAILABLE);
    }

    @Test
    void mapsNullRepositoryEntry() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("[null]"));

        assertFailure(() -> client.getRepositories("octocat"), ErrorCategory.UPSTREAM_UNAVAILABLE);
    }

    @Test
    void mapsRepositoryPayloadWithMissingName() {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("[{\"url\":\"https://api.github.com/repos/octocat/repo\"}]"));

        assertFailure(() -> client.getRepositories("octocat"), ErrorCategory.UPSTREAM_UNAVAILABLE);
    }

    @Test
    void mapsConnectionFailureToUnavailableForUser() {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(100));
        requestFactory.setReadTimeout(Duration.ofMillis(100));
        client = new GitHubClient(RestClient.builder()
                .baseUrl("http://127.0.0.1:1")
                .requestFactory(requestFactory)
                .build());

        assertFailure(() -> client.getUser("octocat"), ErrorCategory.UPSTREAM_UNAVAILABLE);
    }

    @Test
    void mapsConnectionFailureToUnavailableForRepositories() {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofMillis(100));
        requestFactory.setReadTimeout(Duration.ofMillis(100));
        client = new GitHubClient(RestClient.builder()
                .baseUrl("http://127.0.0.1:1")
                .requestFactory(requestFactory)
                .build());

        assertFailure(() -> client.getRepositories("octocat"), ErrorCategory.UPSTREAM_UNAVAILABLE);
    }

    @Test
    void mapsResourceAccessTimeoutCause() {
        var exception = GitHubErrorMapper.mapResourceAccess(
                new ResourceAccessException("I/O error", new SocketTimeoutException("read timed out")));

        assertThat(exception.category()).isEqualTo(ErrorCategory.UPSTREAM_TIMEOUT);
    }

    @Test
    void mapsResourceAccessTimeoutMessage() {
        var exception = GitHubErrorMapper.mapResourceAccess(new ResourceAccessException("Read timed out"));

        assertThat(exception.category()).isEqualTo(ErrorCategory.UPSTREAM_TIMEOUT);
    }

    @Test
    void mapsRestClientExceptionWithNullMessage() {
        var exception = GitHubErrorMapper.mapRestClientException(new RestClientException(null));

        assertThat(exception.category()).isEqualTo(ErrorCategory.UPSTREAM_UNAVAILABLE);
    }

    @Test
    void mapsTimeout() {
        client = client(Duration.ofMillis(100));
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBodyDelay(1, TimeUnit.SECONDS)
                .setBody("{}"));

        assertFailure(() -> client.getUser("octocat"), ErrorCategory.UPSTREAM_TIMEOUT);
    }

    private GitHubClient client(Duration timeout) {
        var requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(timeout);
        requestFactory.setReadTimeout(timeout);
        return new GitHubClient(RestClient.builder()
                .baseUrl(server.url("/").toString())
                .requestFactory(requestFactory)
                .build());
    }

    private void assertFailure(Runnable operation, ErrorCategory category) {
        assertThatThrownBy(operation::run)
                .isInstanceOfSatisfying(GitHubClientException.class, exception ->
                        assertThat(exception.category()).isEqualTo(category));
    }
}
