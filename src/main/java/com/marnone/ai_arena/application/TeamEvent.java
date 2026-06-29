package com.marnone.ai_arena.application;

import java.util.List;
import java.util.Objects;

public record TeamEvent(int expertCount, List<String> roles, String initialStrategy) {

	public TeamEvent {
		if (expertCount < 1) {
			throw new IllegalArgumentException("expertCount must be positive");
		}
		roles = List.copyOf(Objects.requireNonNull(roles, "roles must not be null"));
		Objects.requireNonNull(initialStrategy, "initialStrategy must not be null");
	}
}
