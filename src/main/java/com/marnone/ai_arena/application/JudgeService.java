package com.marnone.ai_arena.application;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.marnone.ai_arena.ai.JudgeAiPort;
import com.marnone.ai_arena.domain.FinalAnswer;
import com.marnone.ai_arena.domain.JudgeRequest;
import com.marnone.ai_arena.domain.JudgeRubric;
import com.marnone.ai_arena.domain.JudgeVerdict;
import com.marnone.ai_arena.domain.Judgement;
import com.marnone.ai_arena.domain.Question;

/**
 * Applies a post-synthesis quality gate without giving the Judge control over arena flow.
 */
public class JudgeService {

	private static final Logger log = LoggerFactory.getLogger(JudgeService.class);
	private static final JudgeRubric FALLBACK_RUBRIC = new JudgeRubric(3, 3, 3, 3, 3, 3);

	private final JudgeAiPort judgeAiPort;

	public JudgeService(JudgeAiPort judgeAiPort) {
		this.judgeAiPort = Objects.requireNonNull(judgeAiPort, "judgeAiPort must not be null");
	}

	public JudgedFinalAnswer judge(Question question, DebateResult debateResult, FinalAnswer finalAnswer) {
		Objects.requireNonNull(question, "question must not be null");
		Objects.requireNonNull(debateResult, "debateResult must not be null");
		Objects.requireNonNull(finalAnswer, "finalAnswer must not be null");
		try {
			Judgement judgement = judgeAiPort.judge(new JudgeRequest(
				question,
				debateResult.messages(),
				finalAnswer,
				"final-answer"
			));
			return new JudgedFinalAnswer(applyJudgement(finalAnswer, judgement), judgement, false);
		}
		catch (RuntimeException ex) {
			log.warn("Judge unavailable; applying fallback judgement exceptionType={}", ex.getClass().getName());
			Judgement fallback = new Judgement(
				JudgeVerdict.ACCEPT,
				FALLBACK_RUBRIC,
				"Judge unavailable; final answer delivered with fallback review.",
				List.of()
			);
			return new JudgedFinalAnswer(finalAnswer, fallback, true);
		}
	}

	private static FinalAnswer applyJudgement(FinalAnswer finalAnswer, Judgement judgement) {
		return switch (judgement.verdict()) {
			case ACCEPT -> finalAnswer;
			case REVISE -> revisedAnswer(finalAnswer, judgement);
			case REJECT -> rejectedAnswer(finalAnswer, judgement);
		};
	}

	private static FinalAnswer revisedAnswer(FinalAnswer finalAnswer, Judgement judgement) {
		String hints = String.join("; ", judgement.revisionHints());
		return new FinalAnswer(
			finalAnswer.content() + "\n\nQuality review note: " + hints,
			finalAnswer.rationale() + " Judge requested revision: " + judgement.reason(),
			finalAnswer.stopReason()
		);
	}

	private static FinalAnswer rejectedAnswer(FinalAnswer finalAnswer, Judgement judgement) {
		return new FinalAnswer(
			"The arena could not produce a reliable final answer for this question.",
			"Judge rejected the synthesized answer: " + judgement.reason(),
			finalAnswer.stopReason()
		);
	}
}
