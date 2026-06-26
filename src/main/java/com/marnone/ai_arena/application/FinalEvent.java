package com.marnone.ai_arena.application;

import java.util.Objects;

public record FinalEvent(String content, String rationale, String stopReason) {

	public FinalEvent {
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
