package com.marnone.ai_arena.application;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.marnone.ai_arena.config.ArenaProperties;
import com.marnone.ai_arena.domain.ArenaLimits;
import com.marnone.ai_arena.domain.DebateMessage;
import com.marnone.ai_arena.domain.FinalAnswer;
import com.marnone.ai_arena.domain.Question;
import com.marnone.ai_arena.domain.Specialist;
import com.marnone.ai_arena.domain.SupervisorAction;
import com.marnone.ai_arena.domain.TeamPlan;
import com.marnone.ai_arena.domain.ValidationResult;
import com.marnone.ai_arena.domain.ValidationStatus;

public class RunArenaSessionUseCase {

	private final ValidationService validationService;
	private final PlanningService planningService;
	private final SpecialistFactory specialistFactory;
	private final DebateOrchestrator debateOrchestrator;
	private final FinalAnswerService finalAnswerService;
	private final ArenaProperties arenaProperties;
	private final Clock clock;

	public RunArenaSessionUseCase(
		ValidationService validationService,
		PlanningService planningService,
		SpecialistFactory specialistFactory,
		DebateOrchestrator debateOrchestrator,
		FinalAnswerService finalAnswerService,
		ArenaProperties arenaProperties
	) {
		this(validationService, planningService, specialistFactory, debateOrchestrator, finalAnswerService, arenaProperties, Clock.systemUTC());
	}

	RunArenaSessionUseCase(
		ValidationService validationService,
		PlanningService planningService,
		SpecialistFactory specialistFactory,
		DebateOrchestrator debateOrchestrator,
		FinalAnswerService finalAnswerService,
		ArenaProperties arenaProperties,
		Clock clock
	) {
		this.validationService = Objects.requireNonNull(validationService, "validationService must not be null");
		this.planningService = Objects.requireNonNull(planningService, "planningService must not be null");
		this.specialistFactory = Objects.requireNonNull(specialistFactory, "specialistFactory must not be null");
		this.debateOrchestrator = Objects.requireNonNull(debateOrchestrator, "debateOrchestrator must not be null");
		this.finalAnswerService = Objects.requireNonNull(finalAnswerService, "finalAnswerService must not be null");
		this.arenaProperties = Objects.requireNonNull(arenaProperties, "arenaProperties must not be null");
		this.clock = Objects.requireNonNull(clock, "clock must not be null");
	}

	public ArenaSessionResult run(String input) {
		return run(input, event -> {
		});
	}

	public ArenaSessionResult run(String input, Consumer<SessionEvent> eventConsumer) {
		Objects.requireNonNull(eventConsumer, "eventConsumer must not be null");
		eventConsumer.accept(SessionEvent.validationStarted(new ValidationEvent(null, null, null)));
		ValidationResult validation = validationService.validate(input);
		if (validation.status() != ValidationStatus.VALID) {
			if (validation.status() == ValidationStatus.REJECTED) {
				eventConsumer.accept(SessionEvent.validationRejected(toValidationEvent(validation)));
			}
			else {
				eventConsumer.accept(SessionEvent.error(new ErrorEvent(reasonOrDefault(validation, "Unable to validate request."))));
			}
			return ArenaSessionResult.rejected(validation);
		}
		eventConsumer.accept(SessionEvent.validationAccepted(toValidationEvent(validation)));

		Question question = new Question(input, Instant.now(clock));
		TeamPlan plan = planningService.plan(question);
		eventConsumer.accept(SessionEvent.teamPlanned(new TeamEvent(plan.specialistCount(), plan.roles(), plan.initialStrategy())));
		List<Specialist> team = specialistFactory.createTeam(plan);
		team.stream()
			.map(RunArenaSessionUseCase::toSpecialistEvent)
			.map(SessionEvent::specialistCreated)
			.forEach(eventConsumer);
		DebateResult debate = debateOrchestrator.run(
			question,
			team,
			toArenaLimits(arenaProperties.getLimits()),
			message -> eventConsumer.accept(SessionEvent.debateMessage(toMessageEvent(message)))
		);
		eventConsumer.accept(SessionEvent.supervisorDecision(new DecisionEvent(SupervisorAction.STOP, debate.stopReason(), null)));
		FinalAnswer finalAnswer = finalAnswerService.synthesize(question, debate);
		eventConsumer.accept(SessionEvent.finalAnswer(new FinalEvent(finalAnswer.content(), finalAnswer.rationale(), finalAnswer.stopReason())));
		return ArenaSessionResult.completed(validation, plan, team, debate, finalAnswer);
	}

	private static ArenaLimits toArenaLimits(ArenaProperties.Limits limits) {
		return new ArenaLimits(
			limits.getMaxSpecialists(),
			limits.getMaxTurns(),
			limits.getMaxMessages(),
			limits.getTimeout(),
			limits.getMaxInputCharacters()
		);
	}

	private static ValidationEvent toValidationEvent(ValidationResult validation) {
		return new ValidationEvent(validation.status(), validation.reason(), validation.classificationHint());
	}

	private static SpecialistEvent toSpecialistEvent(Specialist specialist) {
		return new SpecialistEvent(
			specialist.id(),
			specialist.name(),
			specialist.role(),
			specialist.personality(),
			specialist.mission(),
			specialist.uiAccent()
		);
	}

	private static MessageEvent toMessageEvent(DebateMessage message) {
		return new MessageEvent(message.id(), message.specialistId(), message.turn(), message.type(), message.content());
	}

	private static String reasonOrDefault(ValidationResult validation, String defaultReason) {
		if (validation.reason() == null || validation.reason().isBlank()) {
			return defaultReason;
		}
		return validation.reason();
	}
}
