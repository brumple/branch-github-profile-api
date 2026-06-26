package com.branchinterview.githubprofile.controller;

import com.branchinterview.githubprofile.domain.GitHubProfile;
import com.branchinterview.githubprofile.service.GitHubProfileService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GitHubProfileController {

    private final GitHubProfileService service;

    public GitHubProfileController(GitHubProfileService service) {
        this.service = service;
    }

    @GetMapping("/users/{username}")
    GitHubProfile getProfile(
            @PathVariable String username,
            @RequestHeader(name = "X-GitHub-Token", required = false) String gitHubToken) {
        return service.getProfile(username, gitHubToken);
    }
}
