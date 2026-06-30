package com.marnone.ai_arena.application;

import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.marnone.ai_arena.ai.JudgeAiPort;
import com.marnone.ai_arena.ai.SupervisorAiPort;
import com.marnone.ai_arena.domain.DebateMessage;
import com.marnone.ai_arena.domain.FinalAnswer;
import com.marnone.ai_arena.domain.JudgeRequest;
import com.marnone.ai_arena.domain.JudgeVerdict;
import com.marnone.ai_arena.domain.Judgement;
import com.marnone.ai_arena.domain.Question;

/**
 * Uses the Judge as a consultative quality signal when the Supervisor is ready to stop.
 */
public class SupervisorJudgementAdvisor {

	private static final Logger log = LoggerFactory.getLogger(SupervisorJudgementAdvisor.class);

	private final SupervisorAiPort supervisorAiPort;
	private final JudgeAiPort judgeAiPort;

	public SupervisorJudgementAdvisor(SupervisorAiPort supervisorAiPort, JudgeAiPort judgeAiPort) {
		this.supervisorAiPort = Objects.requireNonNull(supervisorAiPort, "supervisorAiPort must not be null");
		this.judgeAiPort = Objects.requireNonNull(judgeAiPort, "judgeAiPort must not be null");
	}

	public SupervisorAdvice adviseStop(Question question, List<DebateMessage> messages, String stopReason) {
		Objects.requireNonNull(question, "question must not be null");
		List<DebateMessage> debateMessages = List.copyOf(Objects.requireNonNull(messages, "messages must not be null"));
		try {
			FinalAnswer draft = supervisorAiPort.synthesize(question, debateMessages, stopReason);
			Judgement judgement = judgeAiPort.judge(new JudgeRequest(question, debateMessages, draft, "supervisor-stop"));
			if (judgement.verdict() == JudgeVerdict.ACCEPT) {
				return SupervisorAdvice.acceptStop(judgement.reason());
			}
			return SupervisorAdvice.requestMoreDebate(judgement.reason());
		}
		catch (RuntimeException ex) {
			log.warn("Supervisor judgement unavailable; keeping supervisor decision exceptionType={}", ex.getClass().getName());
			return SupervisorAdvice.acceptStop("Judge advice unavailable; supervisor decision kept.");
		}
	}

	public record SupervisorAdvice(boolean requestMoreDebate, String reason) {

		public SupervisorAdvice {
			reason = requireText("reason", reason);
		}

		public static SupervisorAdvice acceptStop(String reason) {
			return new SupervisorAdvice(false, reason);
		}

		public static SupervisorAdvice requestMoreDebate(String reason) {
			return new SupervisorAdvice(true, reason);
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
}
