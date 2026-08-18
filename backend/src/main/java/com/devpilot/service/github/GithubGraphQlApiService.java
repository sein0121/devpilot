package com.devpilot.service.github;

import com.devpilot.dto.github.GithubContributionResponse;
import com.devpilot.dto.github.GithubGraphQlRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class GithubGraphQlApiService {

    private final RestClient githubGraphQlRestClient;

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

        GithubContributionResponse response = githubGraphQlRestClient.post()
                .body(request)
                .retrieve()
                .body(GithubContributionResponse.class);

        return response.data().user().contributionsCollection()
                .contributionCalendar().weeks().stream()
                .flatMap(week -> week.contributionDays().stream())
                .toList();
    }
}