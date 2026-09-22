package com.devpilot.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "career-analysis")
public record CareerAnalysisProperties(int lookbackDays) {
}