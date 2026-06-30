package com.marnone.ai_arena.ai;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.marnone.ai_arena.domain.ArenaLimits;
import com.marnone.ai_arena.domain.DebateMessage;
import com.marnone.ai_arena.domain.FinalAnswer;
import com.marnone.ai_arena.domain.JudgeRequest;
import com.marnone.ai_arena.domain.JudgeRubric;
import com.marnone.ai_arena.domain.JudgeVerdict;
import com.marnone.ai_arena.domain.Judgement;
import com.marnone.ai_arena.domain.MessageType;
import com.marnone.ai_arena.domain.Question;
import com.marnone.ai_arena.domain.RequestClassification;
import com.marnone.ai_arena.domain.OrchestratedAiExpert;
import com.marnone.ai_arena.domain.SupervisorDecision;
import com.marnone.ai_arena.domain.TeamPlan;
import com.marnone.ai_arena.domain.ValidationResult;

/**
 * Deterministic AI adapter for local development and tests until a real LLM provider is selected.
 */
public class FakeAiAdapter implements AiClientPort, JudgeAiPort {

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
		String text = Objects.requireNonNull(question, "question must not be null").text().toLowerCase(Locale.ROOT);
		Objects.requireNonNull(limits, "limits must not be null");
		PlanningProfile profile = PlanningProfile.forDomain(classifyDomain(text));
		int expertCount = Math.min(limits.maxExperts(), profile.roles().size());
		return new TeamPlan(
			profile.skills().subList(0, expertCount),
			expertCount,
			profile.roles().subList(0, expertCount),
			profile.strategy()
		);
	}

	@Override
	public List<OrchestratedAiExpert> createExperts(TeamPlan plan) {
		Objects.requireNonNull(plan, "plan must not be null");
		List<OrchestratedAiExpert> experts = new ArrayList<>();
		for (int index = 0; index < plan.roles().size(); index++) {
			String role = plan.roles().get(index);
			experts.add(new OrchestratedAiExpert(
				"expert-" + (index + 1),
				nameFor(role),
				role,
				personalityFor(role),
				missionFor(role),
				uiAccentFor(role, index)
			));
		}
		return List.copyOf(experts);
	}

	@Override
	public DebateMessage createMessage(
		Question question,
		OrchestratedAiExpert expert,
		List<DebateMessage> previousMessages,
		int turn
	) {
		Objects.requireNonNull(question, "question must not be null");
		Objects.requireNonNull(expert, "expert must not be null");
		Objects.requireNonNull(previousMessages, "previousMessages must not be null");
		MessageType type = messageTypeFor(turn);
		String content = expert.role() + " turn " + turn + ": " + deterministicContent(type, question.text());
		return new DebateMessage("message-" + turn, expert.id(), turn, type, content, Instant.EPOCH.plusSeconds(turn));
	}

	@Override
	public SupervisorDecision decide(List<DebateMessage> messages, ArenaLimits limits) {
		Objects.requireNonNull(messages, "messages must not be null");
		Objects.requireNonNull(limits, "limits must not be null");
		if (messages.size() >= Math.min(3, limits.maxMessages())) {
			return SupervisorDecision.stop("Fake supervisor reached deterministic convergence.");
		}
		return SupervisorDecision.continueWith("expert-" + (messages.size() + 1), "Fake supervisor requests another view.");
	}

	@Override
	public FinalAnswer synthesize(Question question, List<DebateMessage> messages, String stopReason) {
		Objects.requireNonNull(question, "question must not be null");
		Objects.requireNonNull(messages, "messages must not be null");
		String debateBasis = messages.isEmpty()
			? "No debate messages were available."
			: "Key debate basis: " + messages.stream()
				.map(DebateMessage::content)
				.limit(3)
				.toList();
		return new FinalAnswer(
			"Fake final answer for: " + question.text() + ". " + debateBasis,
			"Synthesized from " + messages.size() + " deterministic debate messages and stop reason: " + stopReason,
			stopReason
		);
	}

	@Override
	public Judgement judge(JudgeRequest request) {
		Objects.requireNonNull(request, "request must not be null");
		return new Judgement(
			JudgeVerdict.ACCEPT,
			new JudgeRubric(4, 4, 4, 4, 5, 4),
			"Fake judge accepted the deterministic final answer.",
			List.of()
		);
	}

	private static String classifyDomain(String text) {
		if (text.contains("code") || text.contains("software") || text.contains("spring")) {
			return "software";
		}
		if (text.contains("fitness") || text.contains("nutrition")) {
			return "wellness";
		}
		if (text.contains("travel") || text.contains("trip") || text.contains("itinerary")) {
			return "travel";
		}
		if (text.contains("finance") || text.contains("budget") || text.contains("investment")) {
			return "finance";
		}
		if (text.contains("study") || text.contains("learn") || text.contains("productivity")) {
			return "learning";
		}
		return "general";
	}

	private static String nameFor(String role) {
		return switch (role) {
			case "Analyst" -> "Prism";
			case "Critic" -> "Sentinel";
			case "Synthesizer" -> "Keystone";
			case "Architect" -> "Blueprint";
			case "Risk Reviewer" -> "Guardrail";
			case "Coach" -> "Momentum";
			case "Planner" -> "Compass";
			case "Budget Analyst" -> "Ledger";
			case "Learning Strategist" -> "Method";
			default -> role + " Expert";
		};
	}

	private static String personalityFor(String role) {
		return switch (role) {
			case "Analyst" -> "precise and evidence-oriented";
			case "Critic" -> "careful and challenging";
			case "Synthesizer" -> "balanced and decision-oriented";
			case "Architect" -> "structural and pragmatic";
			case "Risk Reviewer" -> "skeptical and safety-oriented";
			case "Coach" -> "supportive and behavior-oriented";
			case "Planner" -> "organized and constraint-aware";
			case "Budget Analyst" -> "quantitative and trade-off aware";
			case "Learning Strategist" -> "methodical and goal-oriented";
			default -> "professional and focused";
		};
	}

	private static String missionFor(String role) {
		return switch (role) {
			case "Analyst" -> "Map the facts and assumptions that shape the arena debate.";
			case "Critic" -> "Challenge weak claims and expose blind spots in the arena debate.";
			case "Synthesizer" -> "Connect the strongest arguments into a balanced arena debate conclusion.";
			case "Architect" -> "Structure the solution space and technical trade-offs for the arena debate.";
			case "Risk Reviewer" -> "Identify constraints, failure modes, and safety concerns in the arena debate.";
			case "Coach" -> "Translate goals into sustainable actions for the arena debate.";
			case "Planner" -> "Sequence options and constraints into a practical path for the arena debate.";
			case "Budget Analyst" -> "Compare costs, trade-offs, and risk exposure in the arena debate.";
			case "Learning Strategist" -> "Shape goals, methods, and feedback loops for the arena debate.";
			default -> "Contribute a focused professional perspective to the arena debate.";
		};
	}

	private static String uiAccentFor(String role, int index) {
		return switch (role) {
			case "Analyst", "Architect", "Learning Strategist" -> "#2FB7C8";
			case "Critic", "Risk Reviewer" -> "#C84A5D";
			case "Synthesizer" -> "#D7A84F";
			case "Coach", "Planner" -> "#4FAE7B";
			case "Budget Analyst" -> "#8E7BE8";
			default -> List.of("#2FB7C8", "#C84A5D", "#D7A84F", "#4FAE7B").get(Math.floorMod(index, 4));
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

	private record PlanningProfile(List<String> skills, List<String> roles, String strategy) {

		private static PlanningProfile forDomain(String domain) {
			return switch (domain) {
				case "software" -> new PlanningProfile(
					List.of("architecture", "risk analysis", "implementation synthesis"),
					List.of("Architect", "Risk Reviewer", "Synthesizer"),
					"Frame the technical trade-offs, challenge implementation risk, then converge."
				);
				case "wellness" -> new PlanningProfile(
					List.of("habit analysis", "safety review", "practical planning"),
					List.of("Coach", "Risk Reviewer", "Planner"),
					"Balance ambition, safety, and sustainable action."
				);
				case "travel" -> new PlanningProfile(
					List.of("itinerary planning", "constraint analysis", "experience synthesis"),
					List.of("Planner", "Risk Reviewer", "Synthesizer"),
					"Compare constraints and shape a practical itinerary direction."
				);
				case "finance" -> new PlanningProfile(
					List.of("budget analysis", "risk review", "decision synthesis"),
					List.of("Budget Analyst", "Risk Reviewer", "Synthesizer"),
					"Separate assumptions, risk, and options before a cautious synthesis."
				);
				case "learning" -> new PlanningProfile(
					List.of("goal analysis", "method critique", "learning plan synthesis"),
					List.of("Learning Strategist", "Critic", "Synthesizer"),
					"Clarify the learning goal, test the method, then define next steps."
				);
				default -> new PlanningProfile(
					List.of("analysis", "critique", "synthesis"),
					List.of("Analyst", "Critic", "Synthesizer"),
					"Explore the question, challenge assumptions, then converge."
				);
			};
		}
	}
}
