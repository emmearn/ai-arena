package com.marnone.ai_arena.application;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import com.marnone.ai_arena.config.ArenaProperties;
import com.marnone.ai_arena.domain.ArenaLimits;
import com.marnone.ai_arena.domain.FinalAnswer;
import com.marnone.ai_arena.domain.Question;
import com.marnone.ai_arena.domain.Specialist;
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
		ValidationResult validation = validationService.validate(input);
		if (validation.status() != ValidationStatus.VALID) {
			return ArenaSessionResult.rejected(validation);
		}

		Question question = new Question(input, Instant.now(clock));
		TeamPlan plan = planningService.plan(question);
		List<Specialist> team = specialistFactory.createTeam(plan);
		DebateResult debate = debateOrchestrator.run(question, team, toArenaLimits(arenaProperties.getLimits()));
		FinalAnswer finalAnswer = finalAnswerService.synthesize(question, debate);
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
}
