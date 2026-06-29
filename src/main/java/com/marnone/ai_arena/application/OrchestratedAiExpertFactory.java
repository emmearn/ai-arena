package com.marnone.ai_arena.application;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.marnone.ai_arena.ai.OrchestratedAiExpertAiPort;
import com.marnone.ai_arena.domain.OrchestratedAiExpert;
import com.marnone.ai_arena.domain.TeamPlan;

/**
 * Creates the orchestrated AI expert team and checks that generated identities match the plan.
 */
public class OrchestratedAiExpertFactory {

	private static final Logger log = LoggerFactory.getLogger(OrchestratedAiExpertFactory.class);

	private final OrchestratedAiExpertAiPort expertAiPort;

	public OrchestratedAiExpertFactory(OrchestratedAiExpertAiPort expertAiPort) {
		this.expertAiPort = Objects.requireNonNull(expertAiPort, "expertAiPort must not be null");
	}

	public List<OrchestratedAiExpert> createTeam(TeamPlan plan) {
		Objects.requireNonNull(plan, "plan must not be null");
		List<OrchestratedAiExpert> experts = List.copyOf(expertAiPort.createExperts(plan));
		ensureCompleteTeam(plan, experts);
		ensureDistinct(experts);
		return experts;
	}

	private static void ensureCompleteTeam(TeamPlan plan, List<OrchestratedAiExpert> experts) {
		if (experts.size() != plan.expertCount()) {
			log.warn(
				"AI expert team rejected because size does not match plan teamSize={} expectedExpertCount={}",
				experts.size(),
				plan.expertCount()
			);
			throw new IllegalStateException("expert team size must match plan");
		}
		List<String> roles = experts.stream().map(OrchestratedAiExpert::role).toList();
		if (!roles.equals(plan.roles())) {
			log.warn(
				"AI expert team rejected because roles do not match plan roleCount={} expectedRoleCount={}",
				roles.size(),
				plan.roles().size()
			);
			throw new IllegalStateException("expert roles must match plan roles");
		}
	}

	private static void ensureDistinct(List<OrchestratedAiExpert> experts) {
		Set<String> ids = new HashSet<>();
		Set<String> personalities = new HashSet<>();
		Set<String> accents = new HashSet<>();
		for (OrchestratedAiExpert expert : experts) {
			if (!ids.add(expert.id())) {
				log.warn("AI expert team rejected because expert ids are not unique teamSize={}", experts.size());
				throw new IllegalStateException("expert ids must be unique");
			}
			if (!personalities.add(expert.personality())) {
				log.warn("AI expert team rejected because expert personalities are not distinct teamSize={}", experts.size());
				throw new IllegalStateException("expert personalities must be distinct");
			}
			if (!accents.add(expert.uiAccent())) {
				log.warn("AI expert team rejected because UI accents are not distinct teamSize={}", experts.size());
				throw new IllegalStateException("expert UI accents must be distinct");
			}
		}
	}
}
