package com.marnone.ai_arena.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Configuration;

class ArenaPropertiesTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withUserConfiguration(PropertiesConfiguration.class);

	@Test
	void bindsConfiguredLimits() {
		contextRunner
			.withPropertyValues(
				"arena.limits.max-experts=3",
				"arena.limits.max-turns=5",
				"arena.limits.max-messages=15",
				"arena.limits.timeout=30s",
				"arena.limits.max-input-characters=1200",
				"arena.http.max-payload-bytes=2048",
				"arena.http.rate-limit-max-requests=7",
				"arena.http.rate-limit-window=45s",
				"arena.ai.provider=openai",
				"arena.ai.model=gpt-5-mini",
				"arena.ai.request-timeout=20s"
			)
			.run(context -> {
				ArenaProperties properties = context.getBean(ArenaProperties.class);
				ArenaProperties.Limits limits = properties.getLimits();
				ArenaProperties.Http http = properties.getHttp();
				ArenaProperties.Ai ai = properties.getAi();

				assertThat(limits.getMaxExperts()).isEqualTo(3);
				assertThat(limits.getMaxTurns()).isEqualTo(5);
				assertThat(limits.getMaxMessages()).isEqualTo(15);
				assertThat(limits.getTimeout()).isEqualTo(Duration.ofSeconds(30));
				assertThat(limits.getMaxInputCharacters()).isEqualTo(1200);
				assertThat(http.getMaxPayloadBytes()).isEqualTo(2048);
				assertThat(http.getRateLimitMaxRequests()).isEqualTo(7);
				assertThat(http.getRateLimitWindow()).isEqualTo(Duration.ofSeconds(45));
				assertThat(ai.getProvider()).isEqualTo("openai");
				assertThat(ai.getModel()).isEqualTo("gpt-5-mini");
				assertThat(ai.getRequestTimeout()).isEqualTo(Duration.ofSeconds(20));
			});
	}

	@Test
	void providesDefaultsWhenLimitsAreNotConfigured() {
		contextRunner.run(context -> {
			ArenaProperties.Limits limits = context.getBean(ArenaProperties.class).getLimits();

			assertThat(limits.getMaxExperts()).isEqualTo(4);
			assertThat(limits.getMaxTurns()).isEqualTo(6);
			assertThat(limits.getMaxMessages()).isEqualTo(24);
			assertThat(limits.getTimeout()).isEqualTo(Duration.ofSeconds(90));
			assertThat(limits.getMaxInputCharacters()).isEqualTo(4000);
			assertThat(context.getBean(ArenaProperties.class).getHttp().getMaxPayloadBytes()).isEqualTo(8192);
			assertThat(context.getBean(ArenaProperties.class).getHttp().getRateLimitMaxRequests()).isEqualTo(20);
			assertThat(context.getBean(ArenaProperties.class).getHttp().getRateLimitWindow()).isEqualTo(Duration.ofMinutes(1));
			assertThat(context.getBean(ArenaProperties.class).getAi().getProvider()).isEqualTo("openai");
			assertThat(context.getBean(ArenaProperties.class).getAi().getModel()).isEqualTo("gpt-5-mini");
			assertThat(context.getBean(ArenaProperties.class).getAi().getRequestTimeout()).isEqualTo(Duration.ofSeconds(30));
		});
	}

	@Test
	void rejectsNonPositiveLimits() {
		contextRunner
			.withPropertyValues("arena.limits.max-experts=0")
			.run(context -> assertThat(context).hasFailed());
	}

	@Test
	void rejectsNonPositiveHttpLimits() {
		contextRunner
			.withPropertyValues("arena.http.rate-limit-max-requests=0")
			.run(context -> assertThat(context).hasFailed());
	}

	@Test
	void rejectsInvalidAiConfiguration() {
		contextRunner
			.withPropertyValues("arena.ai.request-timeout=0s")
			.run(context -> assertThat(context).hasFailed());
	}

	@Configuration(proxyBeanMethods = false)
	@EnableConfigurationProperties(ArenaProperties.class)
	static class PropertiesConfiguration {
	}
}
