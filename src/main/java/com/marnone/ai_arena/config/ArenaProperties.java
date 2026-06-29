package com.marnone.ai_arena.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Runtime configuration for arena limits that constrain planning and debate execution.
 */
@ConfigurationProperties(prefix = "arena")
public class ArenaProperties {

	private Limits limits = new Limits();
	private Http http = new Http();
	private Ai ai = new Ai();

	public Limits getLimits() {
		return limits;
	}

	public void setLimits(Limits limits) {
		if (limits == null) {
			throw new IllegalArgumentException("arena.limits must be configured");
		}
		this.limits = limits;
	}

	public Http getHttp() {
		return http;
	}

	public void setHttp(Http http) {
		if (http == null) {
			throw new IllegalArgumentException("arena.http must be configured");
		}
		this.http = http;
	}

	public Ai getAi() {
		return ai;
	}

	public void setAi(Ai ai) {
		if (ai == null) {
			throw new IllegalArgumentException("arena.ai must be configured");
		}
		this.ai = ai;
	}

	public static class Limits {

		private int maxExperts = 4;
		private int maxTurns = 6;
		private int maxMessages = 24;
		private Duration timeout = Duration.ofSeconds(90);
		private int maxInputCharacters = 4000;

		public int getMaxExperts() {
			return maxExperts;
		}

		public void setMaxExperts(int maxExperts) {
			this.maxExperts = requirePositive("arena.limits.max-experts", maxExperts);
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

	public static class Http {

		private int maxPayloadBytes = 8192;
		private int rateLimitMaxRequests = 20;
		private Duration rateLimitWindow = Duration.ofMinutes(1);

		public int getMaxPayloadBytes() {
			return maxPayloadBytes;
		}

		public void setMaxPayloadBytes(int maxPayloadBytes) {
			this.maxPayloadBytes = requirePositive("arena.http.max-payload-bytes", maxPayloadBytes);
		}

		public int getRateLimitMaxRequests() {
			return rateLimitMaxRequests;
		}

		public void setRateLimitMaxRequests(int rateLimitMaxRequests) {
			this.rateLimitMaxRequests = requirePositive("arena.http.rate-limit-max-requests", rateLimitMaxRequests);
		}

		public Duration getRateLimitWindow() {
			return rateLimitWindow;
		}

		public void setRateLimitWindow(Duration rateLimitWindow) {
			if (rateLimitWindow == null || rateLimitWindow.isZero() || rateLimitWindow.isNegative()) {
				throw new IllegalArgumentException("arena.http.rate-limit-window must be positive");
			}
			this.rateLimitWindow = rateLimitWindow;
		}

		private static int requirePositive(String name, int value) {
			if (value < 1) {
				throw new IllegalArgumentException(name + " must be positive");
			}
			return value;
		}
	}

	public static class Ai {

		private String provider = "openai";
		private String adapter = "fake";
		private String model = "gpt-5-mini";
		private Duration requestTimeout = Duration.ofSeconds(30);

		public String getProvider() {
			return provider;
		}

		public void setProvider(String provider) {
			this.provider = requireText("arena.ai.provider", provider);
		}

		public String getAdapter() {
			return adapter;
		}

		public void setAdapter(String adapter) {
			this.adapter = requireText("arena.ai.adapter", adapter);
		}

		public String getModel() {
			return model;
		}

		public void setModel(String model) {
			this.model = requireText("arena.ai.model", model);
		}

		public Duration getRequestTimeout() {
			return requestTimeout;
		}

		public void setRequestTimeout(Duration requestTimeout) {
			if (requestTimeout == null || requestTimeout.isZero() || requestTimeout.isNegative()) {
				throw new IllegalArgumentException("arena.ai.request-timeout must be positive");
			}
			this.requestTimeout = requestTimeout;
		}

		private static String requireText(String name, String value) {
			if (value == null || value.isBlank()) {
				throw new IllegalArgumentException(name + " must not be blank");
			}
			return value.trim();
		}
	}
}
