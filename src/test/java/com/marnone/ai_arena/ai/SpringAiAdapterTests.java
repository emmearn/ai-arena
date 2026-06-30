package com.marnone.ai_arena.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import org.junit.jupiter.api.Test;

import com.marnone.ai_arena.domain.ArenaLimits;
import com.marnone.ai_arena.domain.DebateMessage;
import com.marnone.ai_arena.domain.FinalAnswer;
import com.marnone.ai_arena.domain.JudgeRequest;
import com.marnone.ai_arena.domain.JudgeVerdict;
import com.marnone.ai_arena.domain.Judgement;
import com.marnone.ai_arena.domain.MessageType;
import com.marnone.ai_arena.domain.OrchestratedAiExpert;
import com.marnone.ai_arena.domain.Question;
import com.marnone.ai_arena.domain.SupervisorAction;
import com.marnone.ai_arena.domain.SupervisorDecision;
import com.marnone.ai_arena.domain.TeamPlan;
import com.marnone.ai_arena.domain.ValidationStatus;

class SpringAiAdapterTests {

	private static final Clock FIXED_CLOCK = Clock.fixed(Instant.EPOCH, ZoneOffset.UTC);
	private static final ArenaLimits LIMITS = new ArenaLimits(4, 6, 24, Duration.ofSeconds(90), 4000);
	private static final Question QUESTION = new Question("How should AI Arena integrate real LLM output?", Instant.EPOCH);

	@Test
	void mapsValidStructuredOutputsAcrossAllPorts() {
		SpringAiAdapter adapter = adapterWith(
			"""
			{"status":"VALID","reason":null,"classification":{"domain":"software","intent":"architecture","confidence":0.82,"notes":"integration question"}}
			""",
			"""
			{"skills":["architecture","risk","synthesis"],"expertCount":3,"roles":["Architect","Risk Reviewer","Synthesizer"],"initialStrategy":"Compare trade-offs and converge."}
			""",
			"""
			{"experts":[
				{"id":"expert-1","name":"Blueprint","role":"Architect","personality":"structural","mission":"Frame the design.","uiAccent":"#42BFD0"},
				{"id":"expert-2","name":"Guardrail","role":"Risk Reviewer","personality":"skeptical","mission":"Find risks.","uiAccent":"#D85B55"},
				{"id":"expert-3","name":"Keystone","role":"Synthesizer","personality":"balanced","mission":"Converge.","uiAccent":"#FFC21A"}
			]}
			""",
			"""
			{"messageType":"PROPOSAL","content":"Start with a narrow adapter and contract tests."}
			""",
			"""
			{"action":"STOP","reason":"The debate has converged.","nextExpertId":null}
			""",
			"""
			{"content":"Use a guarded Spring AI adapter.","rationale":"The experts converged on structured outputs and tests.","stopReason":"The debate has converged."}
			"""
		);

		var validation = adapter.validate(QUESTION);
		TeamPlan plan = adapter.planTeam(QUESTION, LIMITS);
		List<OrchestratedAiExpert> experts = adapter.createExperts(plan);
		DebateMessage message = adapter.createMessage(QUESTION, experts.getFirst(), List.of(), 1);
		SupervisorDecision decision = adapter.decide(List.of(message), LIMITS);
		FinalAnswer finalAnswer = adapter.synthesize(QUESTION, List.of(message), decision.reason());

		assertThat(validation.status()).isEqualTo(ValidationStatus.VALID);
		assertThat(validation.classificationHint().domain()).isEqualTo("software");
		assertThat(plan.roles()).containsExactly("Architect", "Risk Reviewer", "Synthesizer");
		assertThat(experts).extracting(OrchestratedAiExpert::id).containsExactly("expert-1", "expert-2", "expert-3");
		assertThat(message.type()).isEqualTo(MessageType.PROPOSAL);
		assertThat(message.createdAt()).isEqualTo(Instant.EPOCH);
		assertThat(decision.action()).isEqualTo(SupervisorAction.STOP);
		assertThat(finalAnswer.content()).contains("Spring AI adapter");
	}

	@Test
	void mapsRejectedValidationWithoutStartingPlanning() {
		SpringAiAdapter adapter = adapterWith(
			"""
			{"status":"REJECTED","reason":"Question is not safe to process.","classification":null}
			"""
		);

		var validation = adapter.validate(QUESTION);

		assertThat(validation.status()).isEqualTo(ValidationStatus.REJECTED);
		assertThat(validation.reason()).contains("not safe");
	}

	@Test
	void returnsValidationErrorForMalformedValidationOutput() {
		SpringAiAdapter adapter = adapterWith("not-json");

		var validation = adapter.validate(QUESTION);

		assertThat(validation.status()).isEqualTo(ValidationStatus.ERROR);
		assertThat(validation.reason()).contains("Unable to validate");
	}

	@Test
	void rejectsMalformedPlanningOutput() {
		SpringAiAdapter adapter = adapterWith(
			"""
			{"skills":["architecture"],"expertCount":2,"roles":["Architect"],"initialStrategy":"bad"}
			"""
		);

		assertThatThrownBy(() -> adapter.planTeam(QUESTION, LIMITS))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("malformed");
	}

	@Test
	void rejectsMalformedFinalAnswerOutput() {
		SpringAiAdapter adapter = adapterWith(
			"""
			{"content":"","rationale":"missing user-facing content","stopReason":"done"}
			"""
		);

		assertThatThrownBy(() -> adapter.synthesize(QUESTION, List.of(message()), "done"))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("content must not be blank");
	}

	@Test
	void continuesWithNextExpertWhenSupervisorRequestsAnotherTurn() {
		SpringAiAdapter adapter = adapterWith(
			"""
			```json
			{"action":"CONTINUE","reason":"Need a second view.","nextExpertId":"expert-2"}
			```
			"""
		);

		SupervisorDecision decision = adapter.decide(List.of(message()), LIMITS);

		assertThat(decision.action()).isEqualTo(SupervisorAction.CONTINUE);
		assertThat(decision.nextExpertId()).isEqualTo("expert-2");
	}

	@Test
	void wrapsProviderFailureInControlledException() {
		SpringAiAdapter adapter = new SpringAiAdapter(prompt -> {
			throw new IllegalStateException("provider unavailable");
		}, Duration.ofSeconds(3), FIXED_CLOCK);

		assertThatThrownBy(() -> adapter.createMessage(QUESTION, expert(), List.of(), 1))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("AI provider request failed");
	}

	@Test
	void mapsValidJudgeOutput() {
		SpringAiAdapter adapter = adapterWith(
			"""
			{"verdict":"REVISE","rubric":{"relevance":5,"correctness":4,"completeness":3,"clarity":4,"safety":5,"overall":4},"reason":"Needs a sharper caveat.","revisionHints":["Clarify the operational caveat."]}
			"""
		);

		Judgement judgement = adapter.judge(judgeRequest());

		assertThat(judgement.verdict()).isEqualTo(JudgeVerdict.REVISE);
		assertThat(judgement.rubric().overall()).isEqualTo(4);
		assertThat(judgement.reason()).contains("caveat");
		assertThat(judgement.revisionHints()).containsExactly("Clarify the operational caveat.");
	}

	@Test
	void rejectsJudgeOutputWithMissingFields() {
		SpringAiAdapter adapter = adapterWith(
			"""
			{"verdict":"ACCEPT","reason":"Looks fine.","revisionHints":[]}
			"""
		);

		assertThatThrownBy(() -> adapter.judge(judgeRequest()))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("malformed");
	}

	@Test
	void rejectsJudgeOutputWithInvalidVerdict() {
		SpringAiAdapter adapter = adapterWith(
			"""
			{"verdict":"MAYBE","rubric":{"relevance":5,"correctness":4,"completeness":3,"clarity":4,"safety":5,"overall":4},"reason":"Unclear.","revisionHints":["Decide."]}
			"""
		);

		assertThatThrownBy(() -> adapter.judge(judgeRequest()))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("malformed");
	}

	@Test
	void rejectsJudgeReviseWithoutHints() {
		SpringAiAdapter adapter = adapterWith(
			"""
			{"verdict":"REVISE","rubric":{"relevance":5,"correctness":4,"completeness":3,"clarity":4,"safety":5,"overall":4},"reason":"Needs revision.","revisionHints":[]}
			"""
		);

		assertThatThrownBy(() -> adapter.judge(judgeRequest()))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("malformed");
	}

	@Test
	void rejectsJudgeAcceptWithHallucinatedRevisionHints() {
		SpringAiAdapter adapter = adapterWith(
			"""
			{"verdict":"ACCEPT","rubric":{"relevance":5,"correctness":5,"completeness":5,"clarity":5,"safety":5,"overall":5},"reason":"Looks ready.","revisionHints":["Still rewrite it completely."]}
			"""
		);

		assertThatThrownBy(() -> adapter.judge(judgeRequest()))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("malformed");
	}

	@Test
	void wrapsJudgeProviderFailureInControlledException() {
		SpringAiAdapter adapter = new SpringAiAdapter(prompt -> {
			throw new IllegalStateException("judge provider unavailable");
		}, Duration.ofSeconds(3), FIXED_CLOCK);

		assertThatThrownBy(() -> adapter.judge(judgeRequest()))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("AI provider request failed");
	}

	private static DebateMessage message() {
		return new DebateMessage("message-1", "expert-1", 1, MessageType.PROPOSAL, "Initial point.", Instant.EPOCH);
	}

	private static OrchestratedAiExpert expert() {
		return new OrchestratedAiExpert("expert-1", "Blueprint", "Architect", "structural", "Frame the design.", "#42BFD0");
	}

	private static JudgeRequest judgeRequest() {
		return new JudgeRequest(
			QUESTION,
			List.of(message()),
			new FinalAnswer("Use a guarded Spring AI adapter.", "The debate converged on structured outputs.", "Converged."),
			"final-answer"
		);
	}

	private static SpringAiAdapter adapterWith(String... responses) {
		return new SpringAiAdapter(new QueueLlmClient(responses), Duration.ofSeconds(3), FIXED_CLOCK);
	}

	private static class QueueLlmClient implements LlmClient {

		private final Queue<String> responses;

		QueueLlmClient(String... responses) {
			this.responses = new ArrayDeque<>(List.of(responses));
		}

		@Override
		public String call(String prompt) {
			assertThat(prompt).isNotBlank();
			return responses.remove();
		}
	}
}
