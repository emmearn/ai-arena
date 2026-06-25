package com.marnone.ai_arena.domain;

import java.util.Objects;

public record RequestClassification(String domain, String intent, double confidence, String notes) {

	public RequestClassification {
		Objects.requireNonNull(domain, "domain must not be null");
		Objects.requireNonNull(intent, "intent must not be null");
		if (confidence < 0 || confidence > 1) {
			throw new IllegalArgumentException("confidence must be between 0 and 1");
		}
	}
}
