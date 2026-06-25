package com.marnone.ai_arena.domain;

import java.util.Objects;

public record ValidationResult(ValidationStatus status, String reason, RequestClassification classificationHint) {

	public ValidationResult {
		Objects.requireNonNull(status, "status must not be null");
	}

	public static ValidationResult valid(RequestClassification classificationHint) {
		return new ValidationResult(ValidationStatus.VALID, null, classificationHint);
	}

	public static ValidationResult rejected(String reason) {
		return new ValidationResult(ValidationStatus.REJECTED, reason, null);
	}

	public static ValidationResult error(String reason) {
		return new ValidationResult(ValidationStatus.ERROR, reason, null);
	}
}
