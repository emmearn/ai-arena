package com.marnone.ai_arena.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.marnone.ai_arena.ai.DebateAiPort;
import com.marnone.ai_arena.ai.FakeAiAdapter;
import com.marnone.ai_arena.ai.SupervisorAiPort;
import com.marnone.ai_arena.domain.ArenaLimits;
import com.marnone.ai_arena.domain.DebateMessage;
import com.marnone.ai_arena.domain.FinalAnswer;
import com.marnone.ai_arena.domain.MessageType;
import com.marnone.ai_arena.domain.Question;
import com.marnone.ai_arena.domain.OrchestratedAiExpert;
import com.marnone.ai_arena.domain.SupervisorDecision;

class DebateOrchestratorTests {

	private final FakeAiAdapter fakeAiAdapter = new FakeAiAdapter();

	@Test
	void generatesOrderedTurnsUntilSupervisorConvergence() {
		DebateOrchestrator orchestrator = new DebateOrchestrator(fakeAiAdapter, fakeAiAdapter);

		DebateResult result = orchestrator.run(question(), team(), limits(6, 24, Duration.ofSeconds(90)));

		assertThat(result.messages()).hasSize(3);
		assertThat(result.messages()).extracting(DebateMessage::turn).containsExactly(1, 2, 3);
		assertThat(result.messages()).extracting(DebateMessage::expertId).containsExactly("expert-1", "expert-2", "expert-3");
		assertThat(result.stopReason()).contains("convergence");
	}

	@Test
	void stopsWhenMaxMessagesIsReached() {
		DebateOrchestrator orchestrator = new DebateOrchestrator(fakeAiAdapter, fakeAiAdapter);

		DebateResult result = orchestrator.run(question(), team(), limits(6, 2, Duration.ofSeconds(90)));

		assertThat(result.messages()).hasSize(2);
		assertThat(result.stopReason()).contains("maximum message");
	}

	@Test
	void stopsWhenMaxTurnsIsReached() {
		DebateOrchestrator orchestrator = new DebateOrchestrator(fakeAiAdapter, fakeAiAdapter);

		DebateResult result = orchestrator.run(question(), team(), limits(2, 24, Duration.ofSeconds(90)));

		assertThat(result.messages()).hasSize(2);
		assertThat(result.stopReason()).contains("maximum turn");
	}

	@Test
	void stopsWhenTimeoutIsReached() {
		MutableClock clock = new MutableClock(Instant.EPOCH);
		DebateAiPort slowDebatePort = (question, expert, previousMessages, turn) -> {
			clock.advance(Duration.ofSeconds(2));
			return new DebateMessage("message-" + turn, expert.id(), turn, MessageType.PROPOSAL, "slow", clock.instant());
		};
		DebateOrchestrator orchestrator = new DebateOrchestrator(slowDebatePort, new AlwaysContinueSupervisor(), clock);

		DebateResult result = orchestrator.run(question(), team(), limits(6, 24, Duration.ofSeconds(1)));

		assertThat(result.messages()).hasSize(1);
		assertThat(result.stopReason()).contains("timeout");
	}

	@Test
	void stopsWhenSupervisorSelectsUnknownExpert() {
		SupervisorAiPort badSupervisor = new AlwaysContinueSupervisor("expert-999");
		DebateOrchestrator orchestrator = new DebateOrchestrator(fakeAiAdapter, badSupervisor);

		DebateResult result = orchestrator.run(question(), team(), limits(6, 24, Duration.ofSeconds(90)));

		assertThat(result.messages()).hasSize(1);
		assertThat(result.stopReason()).contains("unknown expert");
	}

	@Test
	void stopsWhenTeamIsEmpty() {
		DebateOrchestrator orchestrator = new DebateOrchestrator(fakeAiAdapter, fakeAiAdapter);

		DebateResult result = orchestrator.run(question(), List.of(), limits(6, 24, Duration.ofSeconds(90)));

		assertThat(result.messages()).isEmpty();
		assertThat(result.stopReason()).contains("team is empty");
	}

	@Test
	void stopsWhenDebateMessageDoesNotMatchExpectedTurnOrExpert() {
		DebateAiPort badDebatePort = (question, expert, previousMessages, turn) ->
			new DebateMessage("message-" + turn, "other-expert", turn, MessageType.PROPOSAL, "bad", Instant.EPOCH);
		DebateOrchestrator orchestrator = new DebateOrchestrator(badDebatePort, new AlwaysContinueSupervisor("expert-1"));

		DebateResult result = orchestrator.run(question(), team(), limits(6, 24, Duration.ofSeconds(90)));

		assertThat(result.messages()).isEmpty();
		assertThat(result.stopReason()).contains("inconsistent");
	}

	private static Question question() {
		return new Question("How should AI Arena structure the debate?", Instant.EPOCH);
	}

	private static List<OrchestratedAiExpert> team() {
		return List.of(
			new OrchestratedAiExpert("expert-1", "Prism", "Analyst", "precise", "Map the arena debate.", "#2FB7C8"),
			new OrchestratedAiExpert("expert-2", "Sentinel", "Critic", "careful", "Challenge the arena debate.", "#C84A5D"),
			new OrchestratedAiExpert("expert-3", "Keystone", "Synthesizer", "balanced", "Synthesize the arena debate.", "#D7A84F")
		);
	}

	private static ArenaLimits limits(int maxTurns, int maxMessages, Duration timeout) {
		return new ArenaLimits(4, maxTurns, maxMessages, timeout, 4000);
	}

	private static class AlwaysContinueSupervisor implements SupervisorAiPort {

		private final String nextExpertId;

		private AlwaysContinueSupervisor() {
			this("expert-1");
		}

		private AlwaysContinueSupervisor(String nextExpertId) {
			this.nextExpertId = nextExpertId;
		}

		@Override
		public SupervisorDecision decide(List<DebateMessage> messages, ArenaLimits limits) {
			return SupervisorDecision.continueWith(nextExpertId, "continue");
		}

		@Override
		public FinalAnswer synthesize(Question question, List<DebateMessage> messages, String stopReason) {
			return new FinalAnswer("unused", "unused", stopReason);
		}
	}

	private static class MutableClock extends Clock {

		private Instant instant;

		private MutableClock(Instant instant) {
			this.instant = instant;
		}

		@Override
		public ZoneId getZone() {
			return ZoneId.of("UTC");
		}

		@Override
		public Clock withZone(ZoneId zone) {
			return this;
		}

		@Override
		public Instant instant() {
			return instant;
		}

		private void advance(Duration duration) {
			instant = instant.plus(duration);
		}
	}
}
