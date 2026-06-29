package com.marnone.ai_arena.domain;

import java.util.List;
import java.util.Objects;

public record ArenaSessionState(
	String sessionId,
	Question question,
	List<OrchestratedAiExpert> team,
	List<DebateMessage> messages,
	SessionStatus status,
	ArenaLimits limits
) {

	public ArenaSessionState {
		Objects.requireNonNull(sessionId, "sessionId must not be null");
		Objects.requireNonNull(question, "question must not be null");
		team = List.copyOf(Objects.requireNonNull(team, "team must not be null"));
		messages = List.copyOf(Objects.requireNonNull(messages, "messages must not be null"));
		Objects.requireNonNull(status, "status must not be null");
		Objects.requireNonNull(limits, "limits must not be null");
	}

	public static ArenaSessionState created(String sessionId, Question question, ArenaLimits limits) {
		return new ArenaSessionState(sessionId, question, List.of(), List.of(), SessionStatus.CREATED, limits);
	}
}
