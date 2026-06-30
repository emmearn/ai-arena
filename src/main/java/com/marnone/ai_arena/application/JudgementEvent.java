package com.marnone.ai_arena.application;

import java.util.List;
import java.util.Objects;

import com.marnone.ai_arena.domain.JudgeRubric;
import com.marnone.ai_arena.domain.JudgeVerdict;

public record JudgementEvent(
	JudgeVerdict verdict,
	JudgeRubric rubric,
	String reason,
	List<String> revisionHints,
	boolean fallbackApplied
) {

	public JudgementEvent {
		Objects.requireNonNull(verdict, "verdict must not be null");
		Objects.requireNonNull(rubric, "rubric must not be null");
		reason = requireText("reason", reason);
		revisionHints = List.copyOf(Objects.requireNonNull(revisionHints, "revisionHints must not be null"));
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
