package com.marnone.ai_arena.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;

import org.junit.jupiter.api.Test;

class QuestionTests {

	@Test
	void trimsQuestionText() {
		Question question = new Question("  What should we do?  ", Instant.EPOCH);

		assertThat(question.text()).isEqualTo("What should we do?");
	}

	@Test
	void rejectsBlankQuestionText() {
		assertThatThrownBy(() -> new Question("", Instant.EPOCH))
			.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> new Question("   ", Instant.EPOCH))
			.isInstanceOf(IllegalArgumentException.class);
	}
}
