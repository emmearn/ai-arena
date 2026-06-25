package com.marnone.ai_arena.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.marnone.ai_arena.ai.FakeAiAdapter;
import com.marnone.ai_arena.ai.PlanningAiPort;
import com.marnone.ai_arena.config.ArenaProperties;
import com.marnone.ai_arena.domain.ArenaLimits;
import com.marnone.ai_arena.domain.Question;
import com.marnone.ai_arena.domain.TeamPlan;

class PlanningServiceTests {

	private final ArenaProperties arenaProperties = new ArenaProperties();
	private final PlanningService planningService = new PlanningService(new FakeAiAdapter(), arenaProperties);

	@Test
	void plansSoftwareQuestionWithDomainSpecificRoles() {
		TeamPlan plan = planningService.plan(question("How should we improve this Spring software?"));

		assertThat(plan.specialistCount()).isEqualTo(3);
		assertThat(plan.roles()).containsExactly("Architect", "Risk Reviewer", "Synthesizer");
		assertThat(plan.skills()).containsExactly("architecture", "risk analysis", "implementation synthesis");
	}

	@Test
	void plansWellnessQuestionWithDifferentRoles() {
		TeamPlan plan = planningService.plan(question("How can I improve my fitness and nutrition routine?"));

		assertThat(plan.roles()).containsExactly("Coach", "Risk Reviewer", "Planner");
		assertThat(plan.initialStrategy()).contains("sustainable action");
	}

	@Test
	void plansUnknownDomainWithGeneralRoles() {
		TeamPlan plan = planningService.plan(question("How should I think about this unusual situation?"));

		assertThat(plan.roles()).containsExactly("Analyst", "Critic", "Synthesizer");
		assertThat(plan.skills()).containsExactly("analysis", "critique", "synthesis");
	}

	@Test
	void respectsConfiguredMaxSpecialists() {
		arenaProperties.getLimits().setMaxSpecialists(2);

		TeamPlan plan = planningService.plan(question("Plan a travel itinerary with constraints"));

		assertThat(plan.specialistCount()).isEqualTo(2);
		assertThat(plan.roles()).containsExactly("Planner", "Risk Reviewer");
		assertThat(plan.skills()).containsExactly("itinerary planning", "constraint analysis");
	}

	@Test
	void rejectsAiPlanThatExceedsMaxSpecialists() {
		PlanningAiPort badPort = (question, limits) -> new TeamPlan(
			List.of("one", "two"),
			2,
			List.of("One", "Two"),
			"bad plan"
		);
		arenaProperties.getLimits().setMaxSpecialists(1);
		PlanningService service = new PlanningService(badPort, arenaProperties);

		assertThatThrownBy(() -> service.plan(question("Any valid question")))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("max specialists");
	}

	private static Question question(String text) {
		return new Question(text, Instant.EPOCH);
	}
}
