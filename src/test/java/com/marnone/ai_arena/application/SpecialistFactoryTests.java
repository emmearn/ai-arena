package com.marnone.ai_arena.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.marnone.ai_arena.ai.FakeAiAdapter;
import com.marnone.ai_arena.ai.SpecialistAiPort;
import com.marnone.ai_arena.domain.Specialist;
import com.marnone.ai_arena.domain.TeamPlan;

class SpecialistFactoryTests {

	private final SpecialistFactory factory = new SpecialistFactory(new FakeAiAdapter());

	@Test
	void createsCompleteAndDistinctSpecialistsFromPlan() {
		TeamPlan plan = new TeamPlan(
			List.of("architecture", "risk analysis", "implementation synthesis"),
			3,
			List.of("Architect", "Risk Reviewer", "Synthesizer"),
			"Frame the technical trade-offs."
		);

		List<Specialist> specialists = factory.createTeam(plan);

		assertThat(specialists).hasSize(3);
		assertThat(specialists).extracting(Specialist::id).containsExactly("agent-1", "agent-2", "agent-3");
		assertThat(specialists).extracting(Specialist::name).containsExactly("Blueprint", "Guardrail", "Keystone");
		assertThat(specialists).extracting(Specialist::role).containsExactlyElementsOf(plan.roles());
		assertThat(specialists).extracting(Specialist::personality).doesNotHaveDuplicates();
		assertThat(specialists).extracting(Specialist::mission)
			.allMatch(mission -> mission.contains("arena debate"))
			.doesNotHaveDuplicates();
		assertThat(specialists).extracting(Specialist::uiAccent).doesNotHaveDuplicates();
	}

	@Test
	void keepsUiAccentsStableForSamePlan() {
		TeamPlan plan = new TeamPlan(
			List.of("analysis", "critique", "synthesis"),
			3,
			List.of("Analyst", "Critic", "Synthesizer"),
			"Explore the question."
		);

		List<String> firstRun = factory.createTeam(plan).stream().map(Specialist::uiAccent).toList();
		List<String> secondRun = factory.createTeam(plan).stream().map(Specialist::uiAccent).toList();

		assertThat(secondRun).containsExactlyElementsOf(firstRun);
	}

	@Test
	void rejectsTeamSizeDifferentFromPlan() {
		SpecialistAiPort badPort = plan -> List.of(
			new Specialist("agent-1", "Analyst", "Analyst", "precise", "inspect", "#2FB7C8")
		);
		SpecialistFactory badFactory = new SpecialistFactory(badPort);
		TeamPlan plan = new TeamPlan(
			List.of("analysis", "critique"),
			2,
			List.of("Analyst", "Critic"),
			"Explore the question."
		);

		assertThatThrownBy(() -> badFactory.createTeam(plan))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("size");
	}

	@Test
	void rejectsDuplicateUiAccents() {
		SpecialistAiPort badPort = plan -> List.of(
			new Specialist("agent-1", "Analyst", "Analyst", "precise", "inspect", "#2FB7C8"),
			new Specialist("agent-2", "Critic", "Critic", "careful", "challenge", "#2FB7C8")
		);
		SpecialistFactory badFactory = new SpecialistFactory(badPort);
		TeamPlan plan = new TeamPlan(
			List.of("analysis", "critique"),
			2,
			List.of("Analyst", "Critic"),
			"Explore the question."
		);

		assertThatThrownBy(() -> badFactory.createTeam(plan))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("accents");
	}
}
