package com.marnone.ai_arena.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "arena")
public class ArenaProperties {

	private Limits limits = new Limits();

	public Limits getLimits() {
		return limits;
	}

	public void setLimits(Limits limits) {
		if (limits == null) {
			throw new IllegalArgumentException("arena.limits must be configured");
		}
		this.limits = limits;
	}

	public static class Limits {

		private int maxSpecialists = 4;
		private int maxTurns = 6;
		private int maxMessages = 24;
		private Duration timeout = Duration.ofSeconds(90);
		private int maxInputCharacters = 4000;

		public int getMaxSpecialists() {
			return maxSpecialists;
		}

		public void setMaxSpecialists(int maxSpecialists) {
			this.maxSpecialists = requirePositive("arena.limits.max-specialists", maxSpecialists);
		}

		public int getMaxTurns() {
			return maxTurns;
		}

		public void setMaxTurns(int maxTurns) {
			this.maxTurns = requirePositive("arena.limits.max-turns", maxTurns);
		}

		public int getMaxMessages() {
			return maxMessages;
		}

		public void setMaxMessages(int maxMessages) {
			this.maxMessages = requirePositive("arena.limits.max-messages", maxMessages);
		}

		public Duration getTimeout() {
			return timeout;
		}

		public void setTimeout(Duration timeout) {
			if (timeout == null || timeout.isZero() || timeout.isNegative()) {
				throw new IllegalArgumentException("arena.limits.timeout must be positive");
			}
			this.timeout = timeout;
		}

		public int getMaxInputCharacters() {
			return maxInputCharacters;
		}

		public void setMaxInputCharacters(int maxInputCharacters) {
			this.maxInputCharacters = requirePositive("arena.limits.max-input-characters", maxInputCharacters);
		}

		private static int requirePositive(String name, int value) {
			if (value < 1) {
				throw new IllegalArgumentException(name + " must be positive");
			}
			return value;
		}
	}
}
