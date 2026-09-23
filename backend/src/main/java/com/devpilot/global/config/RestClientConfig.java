package com.devpilot.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class RestClientConfig {

    private static final String DEFAULT_GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/v1beta";

    private final GithubProperties githubProperties;

    private SimpleClientHttpRequestFactory githubRequestFactory() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(10));
        return requestFactory;
    }

    @Bean
    public RestClient githubRestClient() {
        return RestClient.builder()
                .baseUrl("https://api.github.com")
                .defaultHeader("Authorization", "Bearer " + githubProperties.token())
                .defaultHeader("Accept", "application/vnd.github+json")
                .requestFactory(githubRequestFactory())
                .build();
    }

    @Bean
    public RestClient githubGraphQlRestClient() {
        return RestClient.builder()
                .baseUrl("https://api.github.com/graphql")
                .defaultHeader("Authorization", "Bearer " + githubProperties.token())
                .defaultHeader("Content-Type", "application/json")
                .requestFactory(githubRequestFactory())
                .build();
    }

    @Bean
    public RestClient geminiRestClient(GeminiProperties geminiProperties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(30));

        String baseUrl = geminiProperties.baseUrl() != null
                ? geminiProperties.baseUrl()
                : DEFAULT_GEMINI_BASE_URL;

        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("x-goog-api-key", geminiProperties.key())
                .defaultHeader("Content-Type", "application/json")
                .requestFactory(requestFactory)
                .build();
    }
}