package com.marnone.ai_arena.ai;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.marnone.ai_arena.domain.ArenaLimits;
import com.marnone.ai_arena.domain.DebateMessage;
import com.marnone.ai_arena.domain.FinalAnswer;
import com.marnone.ai_arena.domain.MessageType;
import com.marnone.ai_arena.domain.Question;
import com.marnone.ai_arena.domain.RequestClassification;
import com.marnone.ai_arena.domain.Specialist;
import com.marnone.ai_arena.domain.SupervisorDecision;
import com.marnone.ai_arena.domain.TeamPlan;
import com.marnone.ai_arena.domain.ValidationResult;

public class FakeAiAdapter implements AiClientPort {

	@Override
	public ValidationResult validate(Question question) {
		String text = Objects.requireNonNull(question, "question must not be null").text().toLowerCase(Locale.ROOT);
		if (text.contains("jailbreak") || text.contains("ignore previous instructions")) {
			return ValidationResult.rejected("Request rejected by fake safety policy.");
		}
		return ValidationResult.valid(new RequestClassification(classifyDomain(text), "answer", 0.80, "fake classification"));
	}

	@Override
	public TeamPlan planTeam(Question question, ArenaLimits limits) {
		Objects.requireNonNull(question, "question must not be null");
		Objects.requireNonNull(limits, "limits must not be null");
		int specialistCount = Math.min(limits.maxSpecialists(), 3);
		List<String> roles = List.of("Analyst", "Critic", "Synthesizer").subList(0, specialistCount);
		return new TeamPlan(
			List.of("analysis", "critique", "synthesis").subList(0, specialistCount),
			specialistCount,
			roles,
			"Explore the question, challenge assumptions, then converge."
		);
	}

	@Override
	public List<Specialist> createSpecialists(TeamPlan plan) {
		Objects.requireNonNull(plan, "plan must not be null");
		List<Specialist> specialists = new ArrayList<>();
		for (int index = 0; index < plan.roles().size(); index++) {
			String role = plan.roles().get(index);
			specialists.add(new Specialist(
				"agent-" + (index + 1),
				role,
				role,
				personalityFor(role),
				"Contribute as " + role + " to the arena debate."
			));
		}
		return List.copyOf(specialists);
	}

	@Override
	public DebateMessage createMessage(
		Question question,
		Specialist specialist,
		List<DebateMessage> previousMessages,
		int turn
	) {
		Objects.requireNonNull(question, "question must not be null");
		Objects.requireNonNull(specialist, "specialist must not be null");
		Objects.requireNonNull(previousMessages, "previousMessages must not be null");
		MessageType type = messageTypeFor(turn);
		String content = specialist.role() + " turn " + turn + ": " + deterministicContent(type, question.text());
		return new DebateMessage("message-" + turn, specialist.id(), turn, type, content, Instant.EPOCH.plusSeconds(turn));
	}

	@Override
	public SupervisorDecision decide(List<DebateMessage> messages, ArenaLimits limits) {
		Objects.requireNonNull(messages, "messages must not be null");
		Objects.requireNonNull(limits, "limits must not be null");
		if (messages.size() >= Math.min(3, limits.maxMessages())) {
			return SupervisorDecision.stop("Fake supervisor reached deterministic convergence.");
		}
		return SupervisorDecision.continueWith("agent-" + (messages.size() + 1), "Fake supervisor requests another view.");
	}

	@Override
	public FinalAnswer synthesize(Question question, List<DebateMessage> messages, String stopReason) {
		Objects.requireNonNull(question, "question must not be null");
		Objects.requireNonNull(messages, "messages must not be null");
		return new FinalAnswer(
			"Fake final answer for: " + question.text(),
			"Synthesized from " + messages.size() + " deterministic debate messages.",
			stopReason
		);
	}

	private static String classifyDomain(String text) {
		if (text.contains("code") || text.contains("software") || text.contains("spring")) {
			return "software";
		}
		if (text.contains("fitness") || text.contains("nutrition")) {
			return "wellness";
		}
		return "general";
	}

	private static String personalityFor(String role) {
		return switch (role) {
			case "Analyst" -> "precise and evidence-oriented";
			case "Critic" -> "careful and challenging";
			case "Synthesizer" -> "balanced and decision-oriented";
			default -> "professional and focused";
		};
	}

	private static MessageType messageTypeFor(int turn) {
		return switch (Math.floorMod(turn - 1, 4)) {
			case 0 -> MessageType.PROPOSAL;
			case 1 -> MessageType.CRITIQUE;
			case 2 -> MessageType.CORRECTION;
			default -> MessageType.CONVERGENCE;
		};
	}

	private static String deterministicContent(MessageType type, String questionText) {
		return switch (type) {
			case PROPOSAL -> "proposes an initial angle on \"" + questionText + "\".";
			case CRITIQUE -> "challenges weak assumptions before convergence.";
			case CORRECTION -> "refines the proposal into a clearer answer.";
			case CONVERGENCE -> "aligns the debate toward a final synthesis.";
			case INFO -> "adds neutral context.";
		};
	}
}
