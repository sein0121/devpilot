package com.devpilot.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    private final GithubProperties githubProperties;

    @Bean
    public RestClient githubRestClient() {
        return RestClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Authorization", "Bearer " + githubProperties.token())
                .defaultHeader("Accept", "application/vnd.github+json")
                .build();
    }

    @Bean
    public RestClient githubGraphQlRestClient() {
        return RestClient.builder()
                .baseUrl("https://api.github.com/graphql")
                .defaultHeader("Authorization", "Bearer " + githubProperties.token())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}