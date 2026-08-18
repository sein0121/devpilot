package com.devpilot.service.github;

public class GithubContributionQuery {

    public static final String QUERY = """
            query($username: String!, $from: DateTime!, $to: DateTime!) {
                user(login: $username) {
                    contributionsCollection(from: $from, to: $to) {
                        contributionCalendar {
                            weeks {
                                contributionDays {
                                    date
                                    contributionCount
                                }
                            }
                        }
                    }
                }
            }
            """;

    private GithubContributionQuery() {
    }
}