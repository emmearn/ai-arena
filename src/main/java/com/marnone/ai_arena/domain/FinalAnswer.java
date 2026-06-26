package com.marnone.ai_arena.domain;

import java.util.Objects;

public record FinalAnswer(String content, String rationale, String stopReason) {

	public FinalAnswer {
		content = requireText("content", content);
		rationale = requireText("rationale", rationale);
		stopReason = requireText("stopReason", stopReason);
	}

	private static String requireText(String name, String value) {
		Objects.requireNonNull(value, name + " must not be null");
		String trimmed = value.trim();
		if (trimmed.isEmpty()) {
			throw new IllegalArgumentException(name + " must not be blank");
		}
		return trimmed;
	}
}
