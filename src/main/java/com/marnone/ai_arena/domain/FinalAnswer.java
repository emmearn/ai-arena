package com.marnone.ai_arena.domain;

import java.util.Objects;

public record FinalAnswer(String content, String rationale, String stopReason) {

	public FinalAnswer {
		Objects.requireNonNull(content, "content must not be null");
		Objects.requireNonNull(rationale, "rationale must not be null");
	}
}
