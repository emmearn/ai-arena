package com.marnone.ai_arena.domain;

public record JudgeRubric(int relevance, int correctness, int completeness, int clarity, int safety, int overall) {

	public JudgeRubric {
		validateScore("relevance", relevance);
		validateScore("correctness", correctness);
		validateScore("completeness", completeness);
		validateScore("clarity", clarity);
		validateScore("safety", safety);
		validateScore("overall", overall);
	}

	private static void validateScore(String name, int score) {
		if (score < 1 || score > 5) {
			throw new IllegalArgumentException(name + " must be between 1 and 5");
		}
	}
}
