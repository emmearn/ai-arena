package com.marnone.ai_arena.application;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.marnone.ai_arena.ai.SpecialistAiPort;
import com.marnone.ai_arena.domain.Specialist;
import com.marnone.ai_arena.domain.TeamPlan;

/**
 * Creates the runtime specialist team and checks that generated identities match the plan.
 */
public class SpecialistFactory {

	private static final Logger log = LoggerFactory.getLogger(SpecialistFactory.class);

	private final SpecialistAiPort specialistAiPort;

	public SpecialistFactory(SpecialistAiPort specialistAiPort) {
		this.specialistAiPort = Objects.requireNonNull(specialistAiPort, "specialistAiPort must not be null");
	}

	public List<Specialist> createTeam(TeamPlan plan) {
		Objects.requireNonNull(plan, "plan must not be null");
		List<Specialist> specialists = List.copyOf(specialistAiPort.createSpecialists(plan));
		ensureCompleteTeam(plan, specialists);
		ensureDistinct(specialists);
		return specialists;
	}

	private static void ensureCompleteTeam(TeamPlan plan, List<Specialist> specialists) {
		if (specialists.size() != plan.specialistCount()) {
			log.warn(
				"AI specialist team rejected because size does not match plan teamSize={} expectedSpecialistCount={}",
				specialists.size(),
				plan.specialistCount()
			);
			throw new IllegalStateException("specialist team size must match plan");
		}
		List<String> roles = specialists.stream().map(Specialist::role).toList();
		if (!roles.equals(plan.roles())) {
			log.warn(
				"AI specialist team rejected because roles do not match plan roleCount={} expectedRoleCount={}",
				roles.size(),
				plan.roles().size()
			);
			throw new IllegalStateException("specialist roles must match plan roles");
		}
	}

	private static void ensureDistinct(List<Specialist> specialists) {
		Set<String> ids = new HashSet<>();
		Set<String> personalities = new HashSet<>();
		Set<String> accents = new HashSet<>();
		for (Specialist specialist : specialists) {
			if (!ids.add(specialist.id())) {
				log.warn("AI specialist team rejected because specialist ids are not unique teamSize={}", specialists.size());
				throw new IllegalStateException("specialist ids must be unique");
			}
			if (!personalities.add(specialist.personality())) {
				log.warn("AI specialist team rejected because specialist personalities are not distinct teamSize={}", specialists.size());
				throw new IllegalStateException("specialist personalities must be distinct");
			}
			if (!accents.add(specialist.uiAccent())) {
				log.warn("AI specialist team rejected because UI accents are not distinct teamSize={}", specialists.size());
				throw new IllegalStateException("specialist UI accents must be distinct");
			}
		}
	}
}
