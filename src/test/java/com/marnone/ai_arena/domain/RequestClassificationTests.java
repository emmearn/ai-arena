package com.marnone.ai_arena.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class RequestClassificationTests {

	@Test
	void rejectsConfidenceOutsideZeroToOneRange() {
		assertThatThrownBy(() -> new RequestClassification("software", "answer", -0.01, null))
			.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> new RequestClassification("software", "answer", 1.01, null))
			.isInstanceOf(IllegalArgumentException.class);
	}
}
