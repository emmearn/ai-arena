package com.marnone.ai_arena.application;

import java.util.Objects;

import com.marnone.ai_arena.domain.MessageType;

public record MessageEvent(String id, String expertId, int turn, MessageType messageType, String content) {

	public MessageEvent {
		Objects.requireNonNull(id, "id must not be null");
		Objects.requireNonNull(expertId, "expertId must not be null");
		Objects.requireNonNull(messageType, "messageType must not be null");
		Objects.requireNonNull(content, "content must not be null");
		if (turn < 1) {
			throw new IllegalArgumentException("turn must be positive");
		}
	}
}
