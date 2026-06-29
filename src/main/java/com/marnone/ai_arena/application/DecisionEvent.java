package com.marnone.ai_arena.application;

import java.util.Objects;

import com.marnone.ai_arena.domain.SupervisorAction;

public record DecisionEvent(SupervisorAction action, String reason, String nextExpertId) {

	public DecisionEvent {
		Objects.requireNonNull(action, "action must not be null");
		if (reason == null || reason.isBlank()) {
			throw new IllegalArgumentException("reason must not be blank");
		}
	}
}
