package com.marnone.ai_arena.application;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.marnone.ai_arena.ai.SpecialistAiPort;
import com.marnone.ai_arena.domain.Specialist;
import com.marnone.ai_arena.domain.TeamPlan;

public class SpecialistFactory {

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
			throw new IllegalStateException("specialist team size must match plan");
		}
		List<String> roles = specialists.stream().map(Specialist::role).toList();
		if (!roles.equals(plan.roles())) {
			throw new IllegalStateException("specialist roles must match plan roles");
		}
	}

	private static void ensureDistinct(List<Specialist> specialists) {
		Set<String> ids = new HashSet<>();
		Set<String> personalities = new HashSet<>();
		Set<String> accents = new HashSet<>();
		for (Specialist specialist : specialists) {
			if (!ids.add(specialist.id())) {
				throw new IllegalStateException("specialist ids must be unique");
			}
			if (!personalities.add(specialist.personality())) {
				throw new IllegalStateException("specialist personalities must be distinct");
			}
			if (!accents.add(specialist.uiAccent())) {
				throw new IllegalStateException("specialist UI accents must be distinct");
			}
		}
	}
}
