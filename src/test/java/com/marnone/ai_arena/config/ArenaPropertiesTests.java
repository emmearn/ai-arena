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
				"arena.limits.max-specialists=3",
				"arena.limits.max-turns=5",
				"arena.limits.max-messages=15",
				"arena.limits.timeout=30s",
				"arena.limits.max-input-characters=1200"
			)
			.run(context -> {
				ArenaProperties.Limits limits = context.getBean(ArenaProperties.class).getLimits();

				assertThat(limits.getMaxSpecialists()).isEqualTo(3);
				assertThat(limits.getMaxTurns()).isEqualTo(5);
				assertThat(limits.getMaxMessages()).isEqualTo(15);
				assertThat(limits.getTimeout()).isEqualTo(Duration.ofSeconds(30));
				assertThat(limits.getMaxInputCharacters()).isEqualTo(1200);
			});
	}

	@Test
	void providesDefaultsWhenLimitsAreNotConfigured() {
		contextRunner.run(context -> {
			ArenaProperties.Limits limits = context.getBean(ArenaProperties.class).getLimits();

			assertThat(limits.getMaxSpecialists()).isEqualTo(4);
			assertThat(limits.getMaxTurns()).isEqualTo(6);
			assertThat(limits.getMaxMessages()).isEqualTo(24);
			assertThat(limits.getTimeout()).isEqualTo(Duration.ofSeconds(90));
			assertThat(limits.getMaxInputCharacters()).isEqualTo(4000);
		});
	}

	@Test
	void rejectsNonPositiveLimits() {
		contextRunner
			.withPropertyValues("arena.limits.max-specialists=0")
			.run(context -> assertThat(context).hasFailed());
	}

	@Configuration(proxyBeanMethods = false)
	@EnableConfigurationProperties(ArenaProperties.class)
	static class PropertiesConfiguration {
	}
}
