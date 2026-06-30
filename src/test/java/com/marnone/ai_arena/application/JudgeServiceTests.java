package com.marnone.ai_arena.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import com.marnone.ai_arena.ai.JudgeAiPort;
import com.marnone.ai_arena.domain.DebateMessage;
import com.marnone.ai_arena.domain.FinalAnswer;
import com.marnone.ai_arena.domain.JudgeRequest;
import com.marnone.ai_arena.domain.JudgeRubric;
import com.marnone.ai_arena.domain.JudgeVerdict;
import com.marnone.ai_arena.domain.Judgement;
import com.marnone.ai_arena.domain.MessageType;
import com.marnone.ai_arena.domain.Question;

@ExtendWith(OutputCaptureExtension.class)
class JudgeServiceTests {

	private static final Question QUESTION = new Question("How should AI Arena judge final answers?", Instant.EPOCH);
	private static final DebateResult DEBATE = new DebateResult(
		List.of(new DebateMessage("message-1", "expert-1", 1, MessageType.PROPOSAL, "Use structured review.", Instant.EPOCH)),
		"Converged."
	);
	private static final FinalAnswer FINAL_ANSWER = new FinalAnswer(
		"Use a structured Judge after synthesis.",
		"Debate converged on post-synthesis review.",
		"Converged."
	);

	@Test
	void acceptKeepsFinalAnswerUnchanged() {
		JudgeService service = new JudgeService(returning(new Judgement(
			JudgeVerdict.ACCEPT,
			rubric(5),
			"Answer is ready.",
			List.of()
		)));

		JudgedFinalAnswer result = service.judge(QUESTION, DEBATE, FINAL_ANSWER);

		assertThat(result.finalAnswer()).isEqualTo(FINAL_ANSWER);
		assertThat(result.judgement().verdict()).isEqualTo(JudgeVerdict.ACCEPT);
		assertThat(result.fallbackApplied()).isFalse();
	}

	@Test
	void reviseAddsControlledRevisionNote() {
		JudgeService service = new JudgeService(returning(new Judgement(
			JudgeVerdict.REVISE,
			rubric(3),
			"Needs clearer caveat.",
			List.of("Clarify the risk caveat.")
		)));

		JudgedFinalAnswer result = service.judge(QUESTION, DEBATE, FINAL_ANSWER);

		assertThat(result.finalAnswer().content()).contains("Quality review note: Clarify the risk caveat.");
		assertThat(result.finalAnswer().rationale()).contains("Judge requested revision");
		assertThat(result.judgement().verdict()).isEqualTo(JudgeVerdict.REVISE);
	}

	@Test
	void rejectReplacesFinalAnswerWithControlledMessage() {
		JudgeService service = new JudgeService(returning(new Judgement(
			JudgeVerdict.REJECT,
			rubric(2),
			"Answer is not reliable enough.",
			List.of("Regenerate with better evidence.")
		)));

		JudgedFinalAnswer result = service.judge(QUESTION, DEBATE, FINAL_ANSWER);

		assertThat(result.finalAnswer().content()).isEqualTo("The arena could not produce a reliable final answer for this question.");
		assertThat(result.finalAnswer().rationale()).contains("Judge rejected");
		assertThat(result.judgement().verdict()).isEqualTo(JudgeVerdict.REJECT);
	}

	@Test
	void unavailableJudgeFallsBackWithoutLoggingQuestionOrAnswer(CapturedOutput output) {
		JudgeService service = new JudgeService(request -> {
			throw new IllegalStateException("provider failed with hidden prompt payload");
		});

		JudgedFinalAnswer result = service.judge(QUESTION, DEBATE, FINAL_ANSWER);

		assertThat(result.finalAnswer()).isEqualTo(FINAL_ANSWER);
		assertThat(result.judgement().verdict()).isEqualTo(JudgeVerdict.ACCEPT);
		assertThat(result.fallbackApplied()).isTrue();
		assertThat(output).contains("Judge unavailable; applying fallback judgement");
		assertThat(output).doesNotContain("How should AI Arena judge");
		assertThat(output).doesNotContain("Use a structured Judge after synthesis");
		assertThat(output).doesNotContain("hidden prompt payload");
	}

	private static JudgeAiPort returning(Judgement judgement) {
		return request -> {
			assertJudgeRequest(request);
			return judgement;
		};
	}

	private static void assertJudgeRequest(JudgeRequest request) {
		assertThat(request.question()).isEqualTo(QUESTION);
		assertThat(request.messages()).containsExactlyElementsOf(DEBATE.messages());
		assertThat(request.finalAnswer()).isEqualTo(FINAL_ANSWER);
		assertThat(request.evaluationTarget()).isEqualTo("final-answer");
	}

	private static JudgeRubric rubric(int overall) {
		return new JudgeRubric(overall, overall, overall, overall, overall, overall);
	}
}
