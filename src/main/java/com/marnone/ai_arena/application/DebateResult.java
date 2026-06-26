package com.marnone.ai_arena.application;

import java.util.List;
import java.util.Objects;

import com.marnone.ai_arena.domain.DebateMessage;

public record DebateResult(List<DebateMessage> messages, String stopReason) {

	public DebateResult {
		messages = List.copyOf(Objects.requireNonNull(messages, "messages must not be null"));
		Objects.requireNonNull(stopReason, "stopReason must not be null");
		if (stopReason.isBlank()) {
			throw new IllegalArgumentException("stopReason must not be blank");
		}
	}
}
