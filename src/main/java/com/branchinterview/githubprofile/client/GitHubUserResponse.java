package com.branchinterview.githubprofile.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

public record GitHubUserResponse(
        String login,
        String name,
        @JsonProperty("avatar_url") String avatarUrl,
        String location,
        String email,
        String url,
        @JsonProperty("created_at") Instant createdAt) {
}
