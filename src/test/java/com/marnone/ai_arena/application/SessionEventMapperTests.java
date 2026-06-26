package com.marnone.ai_arena.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.marnone.ai_arena.ai.FakeAiAdapter;
import com.marnone.ai_arena.config.ArenaProperties;
import com.marnone.ai_arena.domain.ValidationResult;

class SessionEventMapperTests {

	private static final Clock FIXED_CLOCK = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);

	private final SessionEventMapper mapper = new SessionEventMapper();

	@Test
	void mapsCompletedSessionToOrderedProgressiveEvents() {
		ArenaSessionResult result = useCaseWith(new ArenaProperties())
			.run("How should AI Arena present a software architecture decision?");

		List<SessionEvent> events = mapper.map(result);

		assertThat(events).extracting(SessionEvent::type).containsExactly(
			SessionEventType.VALIDATION_STARTED,
			SessionEventType.VALIDATION_ACCEPTED,
			SessionEventType.TEAM_PLANNED,
			SessionEventType.SPECIALIST_CREATED,
			SessionEventType.SPECIALIST_CREATED,
			SessionEventType.SPECIALIST_CREATED,
			SessionEventType.DEBATE_MESSAGE,
			SessionEventType.DEBATE_MESSAGE,
			SessionEventType.DEBATE_MESSAGE,
			SessionEventType.SUPERVISOR_DECISION,
			SessionEventType.FINAL_ANSWER
		);
		assertThat(events.get(1).payload()).isInstanceOf(ValidationEvent.class);
		assertThat(((ValidationEvent) events.get(1).payload()).classificationHint()).isNotNull();
		assertThat(events.get(2).payload()).isInstanceOf(TeamEvent.class);
		assertThat(events.get(3).payload()).isInstanceOf(SpecialistEvent.class);
		assertThat(events.get(6).payload()).isInstanceOf(MessageEvent.class);
		assertThat(events.get(9).payload()).isInstanceOf(DecisionEvent.class);
		assertThat(events.get(10).payload()).isInstanceOf(FinalEvent.class);
	}

	@Test
	void mapsRejectedSessionWithoutTeamOrErrorEvent() {
		ArenaSessionResult result = ArenaSessionResult.rejected(ValidationResult.rejected("Request rejected."));

		List<SessionEvent> events = mapper.map(result);

		assertThat(events).extracting(SessionEvent::type).containsExactly(
			SessionEventType.VALIDATION_STARTED,
			SessionEventType.VALIDATION_REJECTED
		);
		assertThat(((ValidationEvent) events.get(1).payload()).reason()).isEqualTo("Request rejected.");
	}

	@Test
	void mapsValidationErrorToErrorEvent() {
		ArenaSessionResult result = ArenaSessionResult.rejected(ValidationResult.error("Validation unavailable."));

		List<SessionEvent> events = mapper.map(result);

		assertThat(events).extracting(SessionEvent::type).containsExactly(
			SessionEventType.VALIDATION_STARTED,
			SessionEventType.ERROR
		);
		assertThat(((ErrorEvent) events.get(1).payload()).message()).isEqualTo("Validation unavailable.");
	}

	private static RunArenaSessionUseCase useCaseWith(ArenaProperties properties) {
		FakeAiAdapter fakeAiAdapter = new FakeAiAdapter();
		ValidationService validationService = new ValidationService(fakeAiAdapter, properties, FIXED_CLOCK);
		PlanningService planningService = new PlanningService(fakeAiAdapter, properties);
		SpecialistFactory specialistFactory = new SpecialistFactory(fakeAiAdapter);
		DebateOrchestrator debateOrchestrator = new DebateOrchestrator(fakeAiAdapter, fakeAiAdapter, FIXED_CLOCK);
		FinalAnswerService finalAnswerService = new FinalAnswerService(fakeAiAdapter);
		return new RunArenaSessionUseCase(
			validationService,
			planningService,
			specialistFactory,
			debateOrchestrator,
			finalAnswerService,
			properties,
			FIXED_CLOCK
		);
	}
}
