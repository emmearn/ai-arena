package com.marnone.ai_arena.application;

import java.util.Objects;

public record ErrorEvent(String message) {

	public ErrorEvent {
		Objects.requireNonNull(message, "message must not be null");
		if (message.isBlank()) {
			throw new IllegalArgumentException("message must not be blank");
		}
	}
}
