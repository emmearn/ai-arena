package com.marnone.ai_arena.application;

import java.util.Objects;

import com.marnone.ai_arena.domain.FinalAnswer;
import com.marnone.ai_arena.domain.Judgement;

public record JudgedFinalAnswer(FinalAnswer finalAnswer, Judgement judgement, boolean fallbackApplied) {

	public JudgedFinalAnswer {
		Objects.requireNonNull(finalAnswer, "finalAnswer must not be null");
		Objects.requireNonNull(judgement, "judgement must not be null");
	}
}
