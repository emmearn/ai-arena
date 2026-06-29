package com.marnone.ai_arena.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

class ArenaSessionStateTests {

	@Test
	void createsInitialSessionState() {
		Question question = new Question("How should we design this?", Instant.EPOCH);
		ArenaLimits limits = new ArenaLimits(4, 6, 24, Duration.ofSeconds(90), 4000);

		ArenaSessionState state = ArenaSessionState.created("session-1", question, limits);

		assertThat(state.sessionId()).isEqualTo("session-1");
		assertThat(state.question()).isEqualTo(question);
		assertThat(state.team()).isEmpty();
		assertThat(state.messages()).isEmpty();
		assertThat(state.status()).isEqualTo(SessionStatus.CREATED);
		assertThat(state.limits()).isEqualTo(limits);
	}

	@Test
	void copiesCollectionsDefensively() {
		List<OrchestratedAiExpert> team = new ArrayList<>();
		List<DebateMessage> messages = new ArrayList<>();
		Question question = new Question("Question", Instant.EPOCH);
		ArenaLimits limits = new ArenaLimits(4, 6, 24, Duration.ofSeconds(90), 4000);

		ArenaSessionState state = new ArenaSessionState("session-1", question, team, messages, SessionStatus.PLANNED, limits);

		team.add(new OrchestratedAiExpert("s1", "Analyst", "Analyst", "precise", "inspect", "#2FB7C8"));
		assertThat(state.team()).isEmpty();
		assertThatThrownBy(() -> state.team().add(new OrchestratedAiExpert("s2", "Critic", "Critic", "careful", "challenge", "#C84A5D")))
			.isInstanceOf(UnsupportedOperationException.class);
	}
}
