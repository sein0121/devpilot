package com.devpilot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.devpilot.global.config.GithubProperties;
import com.devpilot.global.config.GeminiProperties;
import com.devpilot.global.config.CareerAnalysisProperties;

@EnableConfigurationProperties({GithubProperties.class, GeminiProperties.class, CareerAnalysisProperties.class})
@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
