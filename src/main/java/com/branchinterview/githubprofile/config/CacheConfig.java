package com.branchinterview.githubprofile.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String PROFILE_CACHE = "githubProfiles";

    @Bean
    CacheManager cacheManager(@Value("${github.cache.ttl}") Duration ttl) {
        var cacheManager = new CaffeineCacheManager(PROFILE_CACHE);
        cacheManager.setCaffeine(Caffeine.newBuilder().expireAfterWrite(ttl).maximumSize(1_000));
        return cacheManager;
    }
}
