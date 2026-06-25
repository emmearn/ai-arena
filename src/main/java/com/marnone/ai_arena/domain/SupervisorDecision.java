package com.marnone.ai_arena.domain;

import java.util.Objects;

public record SupervisorDecision(SupervisorAction action, String reason, String nextSpecialistId) {

	public SupervisorDecision {
		Objects.requireNonNull(action, "action must not be null");
	}

	public static SupervisorDecision continueWith(String nextSpecialistId, String reason) {
		return new SupervisorDecision(SupervisorAction.CONTINUE, reason, Objects.requireNonNull(nextSpecialistId));
	}

	public static SupervisorDecision stop(String reason) {
		return new SupervisorDecision(SupervisorAction.STOP, reason, null);
	}
}
