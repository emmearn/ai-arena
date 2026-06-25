package com.marnone.ai_arena.domain;

import java.time.Instant;
import java.util.Objects;

public record Question(String text, Instant submittedAt) {

	public Question {
		Objects.requireNonNull(text, "text must not be null");
		Objects.requireNonNull(submittedAt, "submittedAt must not be null");
		text = text.trim();
		if (text.isEmpty()) {
			throw new IllegalArgumentException("text must not be blank");
		}
	}
}
