package com.branchinterview.githubprofile;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "github.api.base-url=http://localhost:1")
class GithubProfileApplicationTest {

    @Test
    void contextLoads() {
    }
}
