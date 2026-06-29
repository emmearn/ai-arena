package com.marnone.ai_arena.domain;

import java.util.List;
import java.util.Objects;

public record JudgeRequest(Question question, List<DebateMessage> messages, FinalAnswer finalAnswer, String evaluationTarget) {

	public JudgeRequest {
		Objects.requireNonNull(question, "question must not be null");
		messages = List.copyOf(Objects.requireNonNull(messages, "messages must not be null"));
		Objects.requireNonNull(finalAnswer, "finalAnswer must not be null");
		evaluationTarget = requireText("evaluationTarget", evaluationTarget);
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
