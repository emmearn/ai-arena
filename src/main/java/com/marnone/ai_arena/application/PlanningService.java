package com.marnone.ai_arena.application;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.marnone.ai_arena.ai.PlanningAiPort;
import com.marnone.ai_arena.config.ArenaProperties;
import com.marnone.ai_arena.domain.ArenaLimits;
import com.marnone.ai_arena.domain.Question;
import com.marnone.ai_arena.domain.TeamPlan;

/**
 * Builds and validates the domain-agnostic expert plan for an accepted question.
 */
public class PlanningService {

	private static final Logger log = LoggerFactory.getLogger(PlanningService.class);

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
			limits.getMaxExperts(),
			limits.getMaxTurns(),
			limits.getMaxMessages(),
			limits.getTimeout(),
			limits.getMaxInputCharacters()
		);
	}

	private static void ensureWithinLimits(TeamPlan plan, ArenaLimits limits) {
		Objects.requireNonNull(plan, "plan must not be null");
		if (plan.expertCount() > limits.maxExperts()) {
			log.warn(
				"AI team plan rejected because expert count exceeds limit expertCount={} maxExperts={}",
				plan.expertCount(),
				limits.maxExperts()
			);
			throw new IllegalStateException("team plan exceeds max experts");
		}
		if (plan.roles().size() != plan.expertCount()) {
			log.warn(
				"AI team plan rejected because roles do not match expert count roleCount={} expertCount={}",
				plan.roles().size(),
				plan.expertCount()
			);
			throw new IllegalStateException("team plan roles must match expert count");
		}
		if (plan.skills().size() < plan.expertCount()) {
			log.warn(
				"AI team plan rejected because skills are missing skillCount={} expertCount={}",
				plan.skills().size(),
				plan.expertCount()
			);
			throw new IllegalStateException("team plan must provide skills for each expert");
		}
	}
}
