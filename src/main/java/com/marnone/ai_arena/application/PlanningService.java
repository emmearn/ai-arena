package com.marnone.ai_arena.application;

import java.util.Objects;

import com.marnone.ai_arena.ai.PlanningAiPort;
import com.marnone.ai_arena.config.ArenaProperties;
import com.marnone.ai_arena.domain.ArenaLimits;
import com.marnone.ai_arena.domain.Question;
import com.marnone.ai_arena.domain.TeamPlan;

public class PlanningService {

	private final PlanningAiPort planningAiPort;
	private final ArenaProperties arenaProperties;

	public PlanningService(PlanningAiPort planningAiPort, ArenaProperties arenaProperties) {
		this.planningAiPort = Objects.requireNonNull(planningAiPort, "planningAiPort must not be null");
		this.arenaProperties = Objects.requireNonNull(arenaProperties, "arenaProperties must not be null");
	}

	public TeamPlan plan(Question question) {
		Objects.requireNonNull(question, "question must not be null");
		ArenaLimits limits = toArenaLimits(arenaProperties.getLimits());
		TeamPlan plan = planningAiPort.planTeam(question, limits);
		ensureWithinLimits(plan, limits);
		return plan;
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

	private static void ensureWithinLimits(TeamPlan plan, ArenaLimits limits) {
		Objects.requireNonNull(plan, "plan must not be null");
		if (plan.specialistCount() > limits.maxSpecialists()) {
			throw new IllegalStateException("team plan exceeds max specialists");
		}
		if (plan.roles().size() != plan.specialistCount()) {
			throw new IllegalStateException("team plan roles must match specialist count");
		}
		if (plan.skills().size() < plan.specialistCount()) {
			throw new IllegalStateException("team plan must provide skills for each specialist");
		}
	}
}
