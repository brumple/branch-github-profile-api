package com.branchinterview.githubprofile.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ApiContractAlignmentTest {

    @Test
    void publicAndSpecKitOpenApiContractsStayAligned() throws Exception {
        var specKitContract = Files.readString(Path.of("specs/001-github-profile-api/contracts/openapi.yaml"));
        var publicContract = Files.readString(Path.of("docs/openapi.yml"));

        assertThat(publicContract).isEqualTo(specKitContract);
        assertThat(publicContract).contains("/users/{username}:");
        assertThat(publicContract).doesNotContain("/pro" + "files/{username}");
    }
}
