package com.marnone.ai_arena.domain;

import java.time.Instant;
import java.util.Objects;

public record DebateMessage(String id, String specialistId, int turn, MessageType type, String content, Instant createdAt) {

	public DebateMessage {
		Objects.requireNonNull(id, "id must not be null");
		Objects.requireNonNull(specialistId, "specialistId must not be null");
		Objects.requireNonNull(type, "type must not be null");
		Objects.requireNonNull(content, "content must not be null");
		Objects.requireNonNull(createdAt, "createdAt must not be null");
		if (turn < 1) {
			throw new IllegalArgumentException("turn must be positive");
		}
	}
}
