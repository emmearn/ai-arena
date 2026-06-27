package com.marnone.ai_arena.domain;

import java.time.Duration;
import java.util.Objects;

/**
 * Execution limits applied before and during a single arena session.
 */
public record ArenaLimits(int maxSpecialists, int maxTurns, int maxMessages, Duration timeout, int maxInputCharacters) {

	public ArenaLimits {
		requirePositive("maxSpecialists", maxSpecialists);
		requirePositive("maxTurns", maxTurns);
		requirePositive("maxMessages", maxMessages);
		requirePositive("maxInputCharacters", maxInputCharacters);
		Objects.requireNonNull(timeout, "timeout must not be null");
		if (timeout.isZero() || timeout.isNegative()) {
			throw new IllegalArgumentException("timeout must be positive");
		}
	}

	private static void requirePositive(String name, int value) {
		if (value < 1) {
			throw new IllegalArgumentException(name + " must be positive");
		}
	}
}
