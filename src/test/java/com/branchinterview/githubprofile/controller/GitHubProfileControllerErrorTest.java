package com.branchinterview.githubprofile.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.branchinterview.githubprofile.client.GitHubClientException;
import com.branchinterview.githubprofile.client.GitHubClientException.ErrorCategory;
import com.branchinterview.githubprofile.service.GitHubProfileService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GitHubProfileController.class)
@Import(RestExceptionHandler.class)
class GitHubProfileControllerErrorTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GitHubProfileService service;

    @Test
    void returnsInvalidUsernameError() throws Exception {
        when(service.getProfile("-", null)).thenThrow(new GitHubClientException(ErrorCategory.INVALID_USERNAME, "Invalid username."));

        mockMvc.perform(get("/users/-"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_USERNAME"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid username."));
    }

    @Test
    void returnsNotFoundError() throws Exception {
        assertError("missing", ErrorCategory.PROFILE_NOT_FOUND, 404, "PROFILE_NOT_FOUND");
    }

    @Test
    void returnsRateLimitError() throws Exception {
        assertError("octocat", ErrorCategory.UPSTREAM_RATE_LIMITED, 429, "UPSTREAM_RATE_LIMITED");
    }

    @Test
    void returnsTimeoutError() throws Exception {
        assertError("octocat", ErrorCategory.UPSTREAM_TIMEOUT, 504, "UPSTREAM_TIMEOUT");
    }

    @Test
    void returnsUnavailableError() throws Exception {
        assertError("octocat", ErrorCategory.UPSTREAM_UNAVAILABLE, 502, "UPSTREAM_UNAVAILABLE");
    }

    @Test
    void returnsNotFoundForUnknownRoutes() throws Exception {
        mockMvc.perform(get("/unknown/octocat"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void handlesMissingPathVariableDirectly() {
        var request = mock(HttpServletRequest.class);
        when(request.getRequestId()).thenReturn("request-1");
        var response = new RestExceptionHandler().handleMissingPathVariable(request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isEqualTo(new ErrorResponse(
                "INVALID_USERNAME",
                "Username is required.",
                400,
                "request-1"));
    }

    @Test
    void handlesUnexpectedExceptionsDirectly() {
        var request = mock(HttpServletRequest.class);
        when(request.getRequestId()).thenReturn("request-2");
        var response = new RestExceptionHandler().handleUnexpected(new RuntimeException("boom"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isEqualTo(new ErrorResponse(
                "INTERNAL_ERROR",
                "An unexpected error occurred.",
                500,
                "request-2"));
    }

    private void assertError(String username, ErrorCategory category, int status, String code) throws Exception {
        when(service.getProfile(username, null)).thenThrow(new GitHubClientException(category, "Safe message."));

        mockMvc.perform(get("/users/{username}", username))
                .andExpect(status().is(status))
                .andExpect(jsonPath("$.code").value(code))
                .andExpect(jsonPath("$.status").value(status))
                .andExpect(jsonPath("$.message").value("Safe message."))
                .andExpect(jsonPath("$.requestId").exists());
    }
}
