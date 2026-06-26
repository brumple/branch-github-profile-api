package com.branchinterview.githubprofile.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record GitHubProfile(
        @JsonProperty("user_name") String userName,
        @JsonProperty("display_name") String displayName,
        String avatar,
        @JsonProperty("geo_location") String geoLocation,
        String email,
        String url,
        @JsonProperty("created_at") String createdAt,
        List<RepositorySummary> repos) {
}
