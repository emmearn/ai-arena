package com.marnone.ai_arena.domain;

import java.util.List;
import java.util.Objects;

public record Judgement(JudgeVerdict verdict, JudgeRubric rubric, String reason, List<String> revisionHints) {

	public Judgement {
		Objects.requireNonNull(verdict, "verdict must not be null");
		Objects.requireNonNull(rubric, "rubric must not be null");
		reason = requireText("reason", reason);
		revisionHints = List.copyOf(Objects.requireNonNull(revisionHints, "revisionHints must not be null"));
		revisionHints.forEach(hint -> requireText("revisionHints", hint));
		if (verdict == JudgeVerdict.ACCEPT && !revisionHints.isEmpty()) {
			throw new IllegalArgumentException("accepted judgement must not include revision hints");
		}
		if (verdict != JudgeVerdict.ACCEPT && revisionHints.isEmpty()) {
			throw new IllegalArgumentException("non-accepted judgement must include revision hints");
		}
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
