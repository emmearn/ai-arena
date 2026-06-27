package com.marnone.ai_arena.application;

import java.util.Objects;

import com.marnone.ai_arena.ai.SupervisorAiPort;
import com.marnone.ai_arena.domain.FinalAnswer;
import com.marnone.ai_arena.domain.Question;

/**
 * Produces the final answer from the completed or stopped debate.
 */
public class FinalAnswerService {

	private final SupervisorAiPort supervisorAiPort;

	public FinalAnswerService(SupervisorAiPort supervisorAiPort) {
		this.supervisorAiPort = Objects.requireNonNull(supervisorAiPort, "supervisorAiPort must not be null");
	}

	public FinalAnswer synthesize(Question question, DebateResult debateResult) {
		Objects.requireNonNull(question, "question must not be null");
		Objects.requireNonNull(debateResult, "debateResult must not be null");
		return supervisorAiPort.synthesize(question, debateResult.messages(), debateResult.stopReason());
	}
}
