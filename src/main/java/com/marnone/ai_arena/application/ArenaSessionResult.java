package com.marnone.ai_arena.application;

import java.util.List;
import java.util.Objects;

import com.marnone.ai_arena.domain.FinalAnswer;
import com.marnone.ai_arena.domain.OrchestratedAiExpert;
import com.marnone.ai_arena.domain.TeamPlan;
import com.marnone.ai_arena.domain.ValidationResult;
import com.marnone.ai_arena.domain.ValidationStatus;

public record ArenaSessionResult(
	ValidationResult validation,
	TeamPlan plan,
	List<OrchestratedAiExpert> team,
	DebateResult debate,
	FinalAnswer finalAnswer
) {

	public ArenaSessionResult {
		Objects.requireNonNull(validation, "validation must not be null");
		team = List.copyOf(Objects.requireNonNull(team, "team must not be null"));
	}

	public static ArenaSessionResult rejected(ValidationResult validation) {
		if (validation.status() == ValidationStatus.VALID) {
			throw new IllegalArgumentException("valid validation cannot create a rejected session result");
		}
		return new ArenaSessionResult(validation, null, List.of(), null, null);
	}

	public static ArenaSessionResult completed(
		ValidationResult validation,
		TeamPlan plan,
		List<OrchestratedAiExpert> team,
		DebateResult debate,
		FinalAnswer finalAnswer
	) {
		if (validation.status() != ValidationStatus.VALID) {
			throw new IllegalArgumentException("completed session result requires valid validation");
		}
		return new ArenaSessionResult(
			validation,
			Objects.requireNonNull(plan, "plan must not be null"),
			team,
			Objects.requireNonNull(debate, "debate must not be null"),
			Objects.requireNonNull(finalAnswer, "finalAnswer must not be null")
		);
	}

	public boolean isCompleted() {
		return finalAnswer != null;
	}
}
