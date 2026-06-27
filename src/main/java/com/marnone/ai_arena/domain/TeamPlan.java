package com.marnone.ai_arena.domain;

import java.util.List;
import java.util.Objects;

/**
 * Domain-agnostic plan that describes which runtime specialists should be created.
 */
public record TeamPlan(List<String> skills, int specialistCount, List<String> roles, String initialStrategy) {

	public TeamPlan {
		skills = List.copyOf(Objects.requireNonNull(skills, "skills must not be null"));
		roles = List.copyOf(Objects.requireNonNull(roles, "roles must not be null"));
		Objects.requireNonNull(initialStrategy, "initialStrategy must not be null");
		if (specialistCount < 1) {
			throw new IllegalArgumentException("specialistCount must be positive");
		}
	}
}
