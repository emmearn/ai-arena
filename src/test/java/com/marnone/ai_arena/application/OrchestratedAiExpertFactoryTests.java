package com.marnone.ai_arena.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.marnone.ai_arena.ai.FakeAiAdapter;
import com.marnone.ai_arena.ai.OrchestratedAiExpertAiPort;
import com.marnone.ai_arena.domain.OrchestratedAiExpert;
import com.marnone.ai_arena.domain.TeamPlan;

class OrchestratedAiExpertFactoryTests {

	private final OrchestratedAiExpertFactory factory = new OrchestratedAiExpertFactory(new FakeAiAdapter());

	@Test
	void createsCompleteAndDistinctExpertsFromPlan() {
		TeamPlan plan = new TeamPlan(
			List.of("architecture", "risk analysis", "implementation synthesis"),
			3,
			List.of("Architect", "Risk Reviewer", "Synthesizer"),
			"Frame the technical trade-offs."
		);

		List<OrchestratedAiExpert> experts = factory.createTeam(plan);

		assertThat(experts).hasSize(3);
		assertThat(experts).extracting(OrchestratedAiExpert::id).containsExactly("expert-1", "expert-2", "expert-3");
		assertThat(experts).extracting(OrchestratedAiExpert::name).containsExactly("Blueprint", "Guardrail", "Keystone");
		assertThat(experts).extracting(OrchestratedAiExpert::role).containsExactlyElementsOf(plan.roles());
		assertThat(experts).extracting(OrchestratedAiExpert::personality).doesNotHaveDuplicates();
		assertThat(experts).extracting(OrchestratedAiExpert::mission)
			.allMatch(mission -> mission.contains("arena debate"))
			.doesNotHaveDuplicates();
		assertThat(experts).extracting(OrchestratedAiExpert::uiAccent).doesNotHaveDuplicates();
	}

	@Test
	void keepsUiAccentsStableForSamePlan() {
		TeamPlan plan = new TeamPlan(
			List.of("analysis", "critique", "synthesis"),
			3,
			List.of("Analyst", "Critic", "Synthesizer"),
			"Explore the question."
		);

		List<String> firstRun = factory.createTeam(plan).stream().map(OrchestratedAiExpert::uiAccent).toList();
		List<String> secondRun = factory.createTeam(plan).stream().map(OrchestratedAiExpert::uiAccent).toList();

		assertThat(secondRun).containsExactlyElementsOf(firstRun);
	}

	@Test
	void rejectsTeamSizeDifferentFromPlan() {
		OrchestratedAiExpertAiPort badPort = plan -> List.of(
			new OrchestratedAiExpert("expert-1", "Analyst", "Analyst", "precise", "inspect", "#2FB7C8")
		);
		OrchestratedAiExpertFactory badFactory = new OrchestratedAiExpertFactory(badPort);
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
		OrchestratedAiExpertAiPort badPort = plan -> List.of(
			new OrchestratedAiExpert("expert-1", "Analyst", "Analyst", "precise", "inspect", "#2FB7C8"),
			new OrchestratedAiExpert("expert-2", "Critic", "Critic", "careful", "challenge", "#2FB7C8")
		);
		OrchestratedAiExpertFactory badFactory = new OrchestratedAiExpertFactory(badPort);
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
