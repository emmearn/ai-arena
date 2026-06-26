package com.marnone.ai_arena.application;

import java.util.Objects;

import com.marnone.ai_arena.domain.RequestClassification;
import com.marnone.ai_arena.domain.ValidationStatus;

public record ValidationEvent(ValidationStatus status, String reason, RequestClassification classificationHint) {

	public ValidationEvent {
		Objects.requireNonNull(status, "status must not be null");
	}
}
