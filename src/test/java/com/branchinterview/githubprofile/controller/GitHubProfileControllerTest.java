package com.branchinterview.githubprofile.controller;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.branchinterview.githubprofile.domain.GitHubProfile;
import com.branchinterview.githubprofile.domain.RepositorySummary;
import com.branchinterview.githubprofile.service.GitHubProfileService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GitHubProfileController.class)
@Import(RestExceptionHandler.class)
class GitHubProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GitHubProfileService service;

    @Test
    void returnsProfileSummary() throws Exception {
        when(service.getProfile("octocat", null)).thenReturn(new GitHubProfile(
                "octocat",
                "The Octocat",
                "https://avatars.githubusercontent.com/u/583231?v=4",
                "San Francisco",
                null,
                "https://api.github.com/users/octocat",
                "Tue, 25 Jan 2011 18:44:36 GMT",
                List.of(new RepositorySummary(
                        "boysenberry-repo-1",
                        "https://api.github.com/repos/octocat/boysenberry-repo-1"))));

        mockMvc.perform(get("/users/octocat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.user_name").value("octocat"))
                .andExpect(jsonPath("$.display_name").value("The Octocat"))
                .andExpect(jsonPath("$.avatar").value("https://avatars.githubusercontent.com/u/583231?v=4"))
                .andExpect(jsonPath("$.geo_location").value("San Francisco"))
                .andExpect(jsonPath("$.email").value(nullValue()))
                .andExpect(jsonPath("$.url").value("https://api.github.com/users/octocat"))
                .andExpect(jsonPath("$.created_at").value("Tue, 25 Jan 2011 18:44:36 GMT"))
                .andExpect(jsonPath("$.repos[0].name").value("boysenberry-repo-1"))
                .andExpect(jsonPath("$.repos[0].url").value("https://api.github.com/repos/octocat/boysenberry-repo-1"));
    }

    @Test
    void passesGitHubTokenHeaderToService() throws Exception {
        when(service.getProfile("octocat", "github-token")).thenReturn(new GitHubProfile(
                "octocat",
                "The Octocat",
                "https://avatars.githubusercontent.com/u/583231?v=4",
                "San Francisco",
                "octocat@example.com",
                "https://api.github.com/users/octocat",
                "Tue, 25 Jan 2011 18:44:36 GMT",
                List.of()));

        mockMvc.perform(get("/users/octocat").header("X-GitHub-Token", "github-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("octocat@example.com"));

        verify(service).getProfile("octocat", "github-token");
    }

}
