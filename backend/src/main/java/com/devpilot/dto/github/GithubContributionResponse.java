package com.devpilot.dto.github;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GithubContributionResponse(Data data) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Data(User user) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record User(ContributionsCollection contributionsCollection) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ContributionsCollection(ContributionCalendar contributionCalendar) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ContributionCalendar(List<Week> weeks) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Week(List<Day> contributionDays) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Day(String date, Integer contributionCount) {}
}