package com.marnone.ai_arena.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class JudgeModelTests {

	@Test
	void createsValidJudgeRequestAndDefensivelyCopiesMessages() {
		List<DebateMessage> messages = new ArrayList<>();
		messages.add(message());

		JudgeRequest request = new JudgeRequest(question(), messages, finalAnswer(), "final-answer");

		messages.clear();
		assertThat(request.question()).isEqualTo(question());
		assertThat(request.messages()).hasSize(1);
		assertThat(request.finalAnswer()).isEqualTo(finalAnswer());
		assertThat(request.evaluationTarget()).isEqualTo("final-answer");
		assertThatThrownBy(() -> request.messages().add(message()))
			.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void rejectsInvalidJudgeRequestFields() {
		assertThatThrownBy(() -> new JudgeRequest(null, List.of(message()), finalAnswer(), "final-answer"))
			.isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> new JudgeRequest(question(), null, finalAnswer(), "final-answer"))
			.isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> new JudgeRequest(question(), List.of(message()), null, "final-answer"))
			.isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> new JudgeRequest(question(), List.of(message()), finalAnswer(), " "))
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void createsValidJudgeRubric() {
		JudgeRubric rubric = new JudgeRubric(5, 4, 3, 4, 5, 4);

		assertThat(rubric.relevance()).isEqualTo(5);
		assertThat(rubric.correctness()).isEqualTo(4);
		assertThat(rubric.completeness()).isEqualTo(3);
		assertThat(rubric.clarity()).isEqualTo(4);
		assertThat(rubric.safety()).isEqualTo(5);
		assertThat(rubric.overall()).isEqualTo(4);
	}

	@Test
	void rejectsRubricScoresOutsideOneToFive() {
		assertThatThrownBy(() -> new JudgeRubric(0, 4, 4, 4, 4, 4))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("relevance");
		assertThatThrownBy(() -> new JudgeRubric(4, 4, 4, 4, 4, 6))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("overall");
	}

	@Test
	void createsAcceptJudgementWithoutRevisionHints() {
		Judgement judgement = new Judgement(JudgeVerdict.ACCEPT, rubric(), "Answer is ready.", List.of());

		assertThat(judgement.verdict()).isEqualTo(JudgeVerdict.ACCEPT);
		assertThat(judgement.rubric()).isEqualTo(rubric());
		assertThat(judgement.reason()).isEqualTo("Answer is ready.");
		assertThat(judgement.revisionHints()).isEmpty();
	}

	@Test
	void createsReviseJudgementWithRevisionHints() {
		Judgement judgement = new Judgement(
			JudgeVerdict.REVISE,
			rubric(),
			"Needs a clearer caveat.",
			List.of("Clarify the main trade-off.")
		);

		assertThat(judgement.verdict()).isEqualTo(JudgeVerdict.REVISE);
		assertThat(judgement.revisionHints()).containsExactly("Clarify the main trade-off.");
		assertThatThrownBy(() -> judgement.revisionHints().add("another"))
			.isInstanceOf(UnsupportedOperationException.class);
	}

	@Test
	void rejectsJudgementInconsistentWithVerdict() {
		assertThatThrownBy(() -> new Judgement(JudgeVerdict.ACCEPT, rubric(), "ok", List.of("revise it")))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("accepted");
		assertThatThrownBy(() -> new Judgement(JudgeVerdict.REJECT, rubric(), "unsafe", List.of()))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("non-accepted");
		assertThatThrownBy(() -> new Judgement(null, rubric(), "reason", List.of()))
			.isInstanceOf(NullPointerException.class);
		assertThatThrownBy(() -> new Judgement(JudgeVerdict.ACCEPT, rubric(), " ", List.of()))
			.isInstanceOf(IllegalArgumentException.class);
	}

	private static Question question() {
		return new Question("How should AI Arena judge an answer?", Instant.EPOCH);
	}

	private static DebateMessage message() {
		return new DebateMessage("message-1", "expert-1", 1, MessageType.PROPOSAL, "Initial point.", Instant.EPOCH);
	}

	private static FinalAnswer finalAnswer() {
		return new FinalAnswer("Final answer.", "Reasoned from debate.", "Converged.");
	}

	private static JudgeRubric rubric() {
		return new JudgeRubric(5, 4, 4, 4, 5, 4);
	}
}
