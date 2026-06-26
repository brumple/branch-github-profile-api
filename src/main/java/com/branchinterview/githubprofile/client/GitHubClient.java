package com.branchinterview.githubprofile.client;

import com.branchinterview.githubprofile.client.GitHubAuthContext;
import com.branchinterview.githubprofile.client.GitHubClientException.ErrorCategory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class GitHubClient {

    private static final int REPOSITORIES_PER_PAGE = 100;

    private final RestClient restClient;
    private final int maxRepositoryPages;

    @Autowired
    public GitHubClient(
            RestClient gitHubRestClient,
            @Value("${github.api.max-repository-pages:10}") int maxRepositoryPages) {
        this.restClient = gitHubRestClient;
        this.maxRepositoryPages = Math.max(1, maxRepositoryPages);
    }

    public GitHubClient(RestClient gitHubRestClient) {
        this(gitHubRestClient, 10);
    }

    public GitHubUserResponse getUser(String username) {
        return getUser(username, GitHubAuthContext.fromHeader(null));
    }

    public GitHubUserResponse getUser(String username, GitHubAuthContext authContext) {
        try {
            var response = restClient.get()
                    .uri("/users/{username}", username)
                    .headers(headers -> applyGitHubToken(headers, authContext))
                    .retrieve()
                    .body(GitHubUserResponse.class);
            if (response == null || response.login() == null || response.url() == null || response.createdAt() == null) {
                throw new GitHubClientException(ErrorCategory.UPSTREAM_UNAVAILABLE, "GitHub returned incomplete profile data.");
            }
            return response;
        } catch (HttpClientErrorException.NotFound exception) {
            throw new GitHubClientException(ErrorCategory.PROFILE_NOT_FOUND, "GitHub profile was not found.");
        } catch (HttpClientErrorException.TooManyRequests exception) {
            throw GitHubErrorMapper.rateLimited();
        } catch (HttpClientErrorException.Forbidden exception) {
            if (GitHubErrorMapper.isRateLimitResponse(exception)) {
                throw GitHubErrorMapper.rateLimited();
            }
            throw new GitHubClientException(ErrorCategory.UPSTREAM_UNAVAILABLE, "GitHub data could not be retrieved.");
        } catch (ResourceAccessException exception) {
            throw GitHubErrorMapper.mapResourceAccess(exception);
        } catch (HttpServerErrorException exception) {
            throw new GitHubClientException(ErrorCategory.UPSTREAM_UNAVAILABLE, "GitHub is temporarily unavailable.");
        } catch (RestClientException exception) {
            throw GitHubErrorMapper.mapRestClientException(exception);
        }
    }

    public List<GitHubRepositoryResponse> getRepositories(String username) {
        return getRepositories(username, GitHubAuthContext.fromHeader(null));
    }

    public List<GitHubRepositoryResponse> getRepositories(String username, GitHubAuthContext authContext) {
        try {
            var repositories = new ArrayList<GitHubRepositoryResponse>();
            var page = 1;
            boolean hasNextPage;
            do {
                var response = getRepositoryPage(username, page, authContext);
                var body = response.getBody();
                if (body == null) {
                    throw new GitHubClientException(ErrorCategory.UPSTREAM_UNAVAILABLE, "GitHub returned incomplete repository data.");
                }
                repositories.addAll(Arrays.asList(body));
                // GitHub returns repository pages at per_page=100; the page cap prevents unbounded upstream calls.
                hasNextPage = hasNextPage(response) && page < maxRepositoryPages;
                page++;
            } while (hasNextPage);

            if (repositories.stream().anyMatch(repository -> repository == null || repository.name() == null || repository.url() == null)) {
                throw new GitHubClientException(ErrorCategory.UPSTREAM_UNAVAILABLE, "GitHub returned incomplete repository data.");
            }
            return List.copyOf(repositories);
        } catch (HttpClientErrorException.NotFound exception) {
            throw new GitHubClientException(ErrorCategory.PROFILE_NOT_FOUND, "GitHub profile was not found.");
        } catch (HttpClientErrorException.TooManyRequests exception) {
            throw GitHubErrorMapper.rateLimited();
        } catch (HttpClientErrorException.Forbidden exception) {
            if (GitHubErrorMapper.isRateLimitResponse(exception)) {
                throw GitHubErrorMapper.rateLimited();
            }
            throw new GitHubClientException(ErrorCategory.UPSTREAM_UNAVAILABLE, "GitHub data could not be retrieved.");
        } catch (ResourceAccessException exception) {
            throw GitHubErrorMapper.mapResourceAccess(exception);
        } catch (HttpServerErrorException exception) {
            throw new GitHubClientException(ErrorCategory.UPSTREAM_UNAVAILABLE, "GitHub is temporarily unavailable.");
        } catch (RestClientException exception) {
            throw GitHubErrorMapper.mapRestClientException(exception);
        }
    }

    private ResponseEntity<GitHubRepositoryResponse[]> getRepositoryPage(
            String username, int page, GitHubAuthContext authContext) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/users/{username}/repos")
                        .queryParam("per_page", REPOSITORIES_PER_PAGE)
                        .queryParam("page", page)
                        .build(username))
                .headers(headers -> applyGitHubToken(headers, authContext))
                .retrieve()
                .toEntity(GitHubRepositoryResponse[].class);
    }

    private void applyGitHubToken(HttpHeaders headers, GitHubAuthContext authContext) {
        if (authContext.hasToken()) {
            headers.setBearerAuth(authContext.token());
        }
    }

    private boolean hasNextPage(ResponseEntity<?> response) {
        return response.getHeaders().getOrEmpty("Link").stream()
                .anyMatch(link -> link.contains("rel=\"next\""));
    }
}
