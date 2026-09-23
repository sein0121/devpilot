package com.devpilot.service.github;

import com.devpilot.dto.github.GithubContributionResponse;
import com.devpilot.dto.github.GithubGraphQlRequest;
import com.devpilot.global.exception.GithubApiAuthException;
import com.devpilot.global.exception.GithubApiClientException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GithubGraphQlApiService {

    private final RestClient githubGraphQlRestClient;

    @Retry(name = "github")
    @CircuitBreaker(name = "github")
    public List<GithubContributionResponse.Day> fetchContributions(
            String username, LocalDate from, LocalDate to
    ) {
        GithubGraphQlRequest request = new GithubGraphQlRequest(
                GithubContributionQuery.QUERY,
                Map.of(
                        "username", username,
                        "from", from.atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME),
                        "to", to.atStartOfDay().format(DateTimeFormatter.ISO_DATE_TIME)
                )
        );

        GithubContributionResponse response;
        try {
            response = githubGraphQlRestClient.post()
                    .body(request)
                    .retrieve()
                    .body(GithubContributionResponse.class);
        } catch (HttpClientErrorException.Unauthorized e) {
            throw new GithubApiAuthException();
        } catch (HttpClientErrorException e) {
            throw new GithubApiClientException("GitHub GraphQL API 요청이 거부되었습니다: " + e.getStatusCode());
        }

        return response.data().user().contributionsCollection()
                .contributionCalendar().weeks().stream()
                .flatMap(week -> week.contributionDays().stream())
                .toList();
    }
}