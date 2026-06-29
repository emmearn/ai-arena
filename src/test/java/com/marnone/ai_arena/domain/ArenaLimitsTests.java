package com.marnone.ai_arena.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;

import org.junit.jupiter.api.Test;

class ArenaLimitsTests {

	@Test
	void acceptsPositiveLimits() {
		ArenaLimits limits = new ArenaLimits(4, 6, 24, Duration.ofSeconds(90), 4000);

		assertThat(limits.maxExperts()).isEqualTo(4);
		assertThat(limits.maxTurns()).isEqualTo(6);
		assertThat(limits.maxMessages()).isEqualTo(24);
		assertThat(limits.timeout()).isEqualTo(Duration.ofSeconds(90));
		assertThat(limits.maxInputCharacters()).isEqualTo(4000);
	}

	@Test
	void rejectsNonPositiveValues() {
		assertThatThrownBy(() -> new ArenaLimits(0, 6, 24, Duration.ofSeconds(90), 4000))
			.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> new ArenaLimits(4, 0, 24, Duration.ofSeconds(90), 4000))
			.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> new ArenaLimits(4, 6, 0, Duration.ofSeconds(90), 4000))
			.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> new ArenaLimits(4, 6, 24, Duration.ZERO, 4000))
			.isInstanceOf(IllegalArgumentException.class);
		assertThatThrownBy(() -> new ArenaLimits(4, 6, 24, Duration.ofSeconds(90), 0))
			.isInstanceOf(IllegalArgumentException.class);
	}
}
