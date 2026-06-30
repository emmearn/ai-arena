package com.marnone.ai_arena.application;

import java.util.Objects;

public record SessionEvent(SessionEventType type, Object payload) {

	public SessionEvent {
		Objects.requireNonNull(type, "type must not be null");
		Objects.requireNonNull(payload, "payload must not be null");
	}

	public static SessionEvent validationStarted(ValidationEvent payload) {
		return new SessionEvent(SessionEventType.VALIDATION_STARTED, payload);
	}

	public static SessionEvent validationAccepted(ValidationEvent payload) {
		return new SessionEvent(SessionEventType.VALIDATION_ACCEPTED, payload);
	}

	public static SessionEvent validationRejected(ValidationEvent payload) {
		return new SessionEvent(SessionEventType.VALIDATION_REJECTED, payload);
	}

	public static SessionEvent teamPlanned(TeamEvent payload) {
		return new SessionEvent(SessionEventType.TEAM_PLANNED, payload);
	}

	public static SessionEvent expertCreated(ExpertEvent payload) {
		return new SessionEvent(SessionEventType.EXPERT_CREATED, payload);
	}

	public static SessionEvent debateMessage(MessageEvent payload) {
		return new SessionEvent(SessionEventType.DEBATE_MESSAGE, payload);
	}

	public static SessionEvent supervisorDecision(DecisionEvent payload) {
		return new SessionEvent(SessionEventType.SUPERVISOR_DECISION, payload);
	}

	public static SessionEvent judgement(JudgementEvent payload) {
		return new SessionEvent(SessionEventType.JUDGEMENT, payload);
	}

	public static SessionEvent finalAnswer(FinalEvent payload) {
		return new SessionEvent(SessionEventType.FINAL_ANSWER, payload);
	}

	public static SessionEvent error(ErrorEvent payload) {
		return new SessionEvent(SessionEventType.ERROR, payload);
	}
}
