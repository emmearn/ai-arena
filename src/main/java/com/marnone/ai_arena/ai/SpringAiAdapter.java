package com.marnone.ai_arena.ai;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.model.ChatModel;

import com.marnone.ai_arena.domain.ArenaLimits;
import com.marnone.ai_arena.domain.DebateMessage;
import com.marnone.ai_arena.domain.FinalAnswer;
import com.marnone.ai_arena.domain.JudgeRequest;
import com.marnone.ai_arena.domain.JudgeRubric;
import com.marnone.ai_arena.domain.JudgeVerdict;
import com.marnone.ai_arena.domain.Judgement;
import com.marnone.ai_arena.domain.MessageType;
import com.marnone.ai_arena.domain.OrchestratedAiExpert;
import com.marnone.ai_arena.domain.Question;
import com.marnone.ai_arena.domain.RequestClassification;
import com.marnone.ai_arena.domain.SupervisorAction;
import com.marnone.ai_arena.domain.SupervisorDecision;
import com.marnone.ai_arena.domain.TeamPlan;
import com.marnone.ai_arena.domain.ValidationResult;

/**
 * Spring AI adapter that requests strict JSON outputs and maps them into validated domain types.
 */
public class SpringAiAdapter implements AiClientPort, JudgeAiPort {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	private static final List<String> FALLBACK_ACCENTS = List.of("#42BFD0", "#D85B55", "#FFC21A", "#58C3A6", "#8F7AF5");
	private static final String MALFORMED_OUTPUT = "AI provider returned malformed output.";

	private final LlmClient llmClient;
	private final Duration requestTimeout;
	private final Clock clock;

	public SpringAiAdapter(ChatModel chatModel, Duration requestTimeout) {
		this(new ChatModelLlmClient(chatModel), requestTimeout, Clock.systemUTC());
	}

	SpringAiAdapter(LlmClient llmClient, Duration requestTimeout, Clock clock) {
		this.llmClient = Objects.requireNonNull(llmClient, "llmClient must not be null");
		if (requestTimeout == null || requestTimeout.isZero() || requestTimeout.isNegative()) {
			throw new IllegalArgumentException("requestTimeout must be positive");
		}
		this.requestTimeout = requestTimeout;
		this.clock = Objects.requireNonNull(clock, "clock must not be null");
	}

	@Override
	public ValidationResult validate(Question question) {
		Objects.requireNonNull(question, "question must not be null");
		try {
			ValidationResponse response = ask(
				"""
				You validate an AI Arena user question.
				Return only JSON with this schema:
				{"status":"VALID|REJECTED","reason":"short user-safe reason or null","classification":{"domain":"string","intent":"string","confidence":0.0,"notes":"string"}}
				Reject only if the question is unsafe, hostile, impossible to process, or not a real question.
				Question: %s
				""".formatted(question.text()),
				ValidationResponse.class
			);
			if ("REJECTED".equalsIgnoreCase(response.status())) {
				return ValidationResult.rejected(reasonOrDefault(response.reason(), "Question rejected by AI validation."));
			}
			if (!"VALID".equalsIgnoreCase(response.status()) || response.classification() == null) {
				return ValidationResult.error("Unable to validate request.");
			}
			return ValidationResult.valid(toClassification(response.classification()));
		}
		catch (RuntimeException ex) {
			return ValidationResult.error("Unable to validate request.");
		}
	}

	@Override
	public TeamPlan planTeam(Question question, ArenaLimits limits) {
		Objects.requireNonNull(question, "question must not be null");
		Objects.requireNonNull(limits, "limits must not be null");
		TeamPlanResponse response = ask(
			"""
			Plan a small domain-agnostic AI Arena expert team.
			Return only JSON with this schema:
			{"skills":["string"],"expertCount":1,"roles":["string"],"initialStrategy":"string"}
			Rules: expertCount must be between 1 and %d. roles length must equal expertCount. skills length must be at least expertCount.
			Question: %s
			""".formatted(limits.maxExperts(), question.text()),
			TeamPlanResponse.class
		);
		TeamPlan plan = new TeamPlan(nonEmptyList(response.skills(), "skills"), response.expertCount(), nonEmptyList(response.roles(), "roles"), requireText("initialStrategy", response.initialStrategy()));
		if (plan.expertCount() > limits.maxExperts() || plan.roles().size() != plan.expertCount() || plan.skills().size() < plan.expertCount()) {
			throw malformed();
		}
		return plan;
	}

	@Override
	public List<OrchestratedAiExpert> createExperts(TeamPlan plan) {
		Objects.requireNonNull(plan, "plan must not be null");
		ExpertTeamResponse response = ask(
			"""
			Create identities for the planned AI Arena experts.
			Return only JSON with this schema:
			{"experts":[{"id":"expert-1","name":"string","role":"exact planned role","personality":"string","mission":"string","uiAccent":"#RRGGBB"}]}
			Rules: return exactly these roles, in this order: %s
			Initial strategy: %s
			""".formatted(plan.roles(), plan.initialStrategy()),
			ExpertTeamResponse.class
		);
		List<ExpertResponse> expertResponses = Objects.requireNonNull(response.experts(), "experts must not be null");
		List<OrchestratedAiExpert> experts = new ArrayList<>();
		for (int index = 0; index < expertResponses.size(); index++) {
			ExpertResponse expert = expertResponses.get(index);
			experts.add(new OrchestratedAiExpert(
				defaultText(expert.id(), "expert-" + (index + 1)),
				requireText("name", expert.name()),
				requireText("role", expert.role()),
				requireText("personality", expert.personality()),
				requireText("mission", expert.mission()),
				safeAccent(expert.uiAccent(), index)
			));
		}
		return List.copyOf(experts);
	}

	@Override
	public DebateMessage createMessage(Question question, OrchestratedAiExpert expert, List<DebateMessage> previousMessages, int turn) {
		Objects.requireNonNull(question, "question must not be null");
		Objects.requireNonNull(expert, "expert must not be null");
		List<DebateMessage> messages = List.copyOf(Objects.requireNonNull(previousMessages, "previousMessages must not be null"));
		MessageResponse response = ask(
			"""
			Write one concise AI Arena debate message.
			Return only JSON with this schema:
			{"messageType":"PROPOSAL|CRITIQUE|CORRECTION|CONVERGENCE|INFO","content":"string"}
			Question: %s
			Current expert: %s, role: %s, personality: %s, mission: %s
			Turn: %d
			Previous messages: %s
			""".formatted(question.text(), expert.name(), expert.role(), expert.personality(), expert.mission(), turn, summarizeMessages(messages)),
			MessageResponse.class
		);
		return new DebateMessage(
			"message-" + turn,
			expert.id(),
			turn,
			parseMessageType(response.messageType()),
			requireText("content", response.content()),
			Instant.now(clock)
		);
	}

	@Override
	public SupervisorDecision decide(List<DebateMessage> messages, ArenaLimits limits) {
		List<DebateMessage> debateMessages = List.copyOf(Objects.requireNonNull(messages, "messages must not be null"));
		Objects.requireNonNull(limits, "limits must not be null");
		DecisionResponse response = ask(
			"""
			Decide whether the AI Arena debate should continue.
			Return only JSON with this schema:
			{"action":"CONTINUE|STOP","reason":"short reason","nextExpertId":"expert id or null"}
			Rules: stop if there is enough convergence, if quality is sufficient, or if another turn would be repetitive.
			Limits: maxMessages=%d, maxTurns=%d.
			Messages: %s
			""".formatted(limits.maxMessages(), limits.maxTurns(), summarizeMessages(debateMessages)),
			DecisionResponse.class
		);
		SupervisorAction action = parseSupervisorAction(response.action());
		String reason = reasonOrDefault(response.reason(), "Supervisor decision completed.");
		if (action == SupervisorAction.STOP) {
			return SupervisorDecision.stop(reason);
		}
		return SupervisorDecision.continueWith(requireText("nextExpertId", response.nextExpertId()), reason);
	}

	@Override
	public FinalAnswer synthesize(Question question, List<DebateMessage> messages, String stopReason) {
		Objects.requireNonNull(question, "question must not be null");
		List<DebateMessage> debateMessages = List.copyOf(Objects.requireNonNull(messages, "messages must not be null"));
		FinalAnswerResponse response = ask(
			"""
			Synthesize the final AI Arena answer.
			Return only JSON with this schema:
			{"content":"final user-facing answer","rationale":"brief explanation of how debate informed it","stopReason":"string"}
			Question: %s
			Stop reason: %s
			Debate messages: %s
			""".formatted(question.text(), stopReason, summarizeMessages(debateMessages)),
			FinalAnswerResponse.class
		);
		return new FinalAnswer(
			requireText("content", response.content()),
			requireText("rationale", response.rationale()),
			reasonOrDefault(response.stopReason(), stopReason)
		);
	}

	@Override
	public Judgement judge(JudgeRequest request) {
		Objects.requireNonNull(request, "request must not be null");
		JudgementResponse response = ask(
			"""
			Evaluate an AI Arena output as a separate LLM-as-a-Judge signal.
			Return only JSON with this schema:
			{"verdict":"ACCEPT|REVISE|REJECT","rubric":{"relevance":1,"correctness":1,"completeness":1,"clarity":1,"safety":1,"overall":1},"reason":"brief reason","revisionHints":["string"]}
			Rules: scores are integers from 1 to 5. ACCEPT must use an empty revisionHints array. REVISE or REJECT must include at least one actionable revision hint.
			Evaluation target: %s
			Question: %s
			Final answer: %s
			Rationale: %s
			Debate messages: %s
			""".formatted(
				request.evaluationTarget(),
				request.question().text(),
				request.finalAnswer().content(),
				request.finalAnswer().rationale(),
				summarizeMessages(request.messages())
			),
			JudgementResponse.class
		);
		try {
			return new Judgement(
				parseJudgeVerdict(response.verdict()),
				toJudgeRubric(Objects.requireNonNull(response.rubric(), "rubric must not be null")),
				requireText("reason", response.reason()),
				List.copyOf(Objects.requireNonNull(response.revisionHints(), "revisionHints must not be null"))
			);
		}
		catch (RuntimeException ex) {
			if (MALFORMED_OUTPUT.equals(ex.getMessage())) {
				throw ex;
			}
			throw malformed();
		}
	}

	private <T> T ask(String prompt, Class<T> responseType) {
		try {
			String raw = CompletableFuture
				.supplyAsync(() -> llmClient.call(prompt))
				.orTimeout(requestTimeout.toMillis(), TimeUnit.MILLISECONDS)
				.join();
			return OBJECT_MAPPER.readValue(extractJson(raw), responseType);
		}
		catch (CompletionException ex) {
			throw new IllegalStateException("AI provider request failed.", ex);
		}
		catch (JsonProcessingException ex) {
			throw malformed();
		}
	}

	private static RequestClassification toClassification(ClassificationResponse response) {
		return new RequestClassification(
			requireText("domain", response.domain()),
			requireText("intent", response.intent()),
			response.confidence(),
			defaultText(response.notes(), "AI classification")
		);
	}

	private static String extractJson(String raw) {
		String value = requireText("raw", raw).trim();
		if (value.startsWith("```")) {
			value = value.replaceFirst("^```(?:json)?\\s*", "").replaceFirst("\\s*```$", "").trim();
		}
		int objectStart = value.indexOf('{');
		int objectEnd = value.lastIndexOf('}');
		if (objectStart < 0 || objectEnd < objectStart) {
			throw malformed();
		}
		return value.substring(objectStart, objectEnd + 1);
	}

	private static List<String> nonEmptyList(List<String> values, String name) {
		if (values == null || values.isEmpty()) {
			throw malformed();
		}
		return values.stream().map(value -> requireText(name, value)).toList();
	}

	private static String summarizeMessages(List<DebateMessage> messages) {
		if (messages.isEmpty()) {
			return "[]";
		}
		return messages.stream()
			.map(message -> "turn %d %s %s".formatted(message.turn(), message.expertId(), message.content()))
			.limit(8)
			.toList()
			.toString();
	}

	private static MessageType parseMessageType(String value) {
		try {
			return MessageType.valueOf(requireText("messageType", value).toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException ex) {
			throw malformed();
		}
	}

	private static SupervisorAction parseSupervisorAction(String value) {
		try {
			return SupervisorAction.valueOf(requireText("action", value).toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException ex) {
			throw malformed();
		}
	}

	private static JudgeVerdict parseJudgeVerdict(String value) {
		try {
			return JudgeVerdict.valueOf(requireText("verdict", value).toUpperCase(Locale.ROOT));
		}
		catch (IllegalArgumentException ex) {
			throw malformed();
		}
	}

	private static JudgeRubric toJudgeRubric(JudgeRubricResponse response) {
		try {
			return new JudgeRubric(
				response.relevance(),
				response.correctness(),
				response.completeness(),
				response.clarity(),
				response.safety(),
				response.overall()
			);
		}
		catch (RuntimeException ex) {
			throw malformed();
		}
	}

	private static String safeAccent(String value, int index) {
		if (value != null && value.matches("^#[0-9a-fA-F]{6}$")) {
			return value;
		}
		return FALLBACK_ACCENTS.get(Math.floorMod(index, FALLBACK_ACCENTS.size()));
	}

	private static String reasonOrDefault(String reason, String defaultReason) {
		if (reason == null || reason.isBlank()) {
			return defaultReason;
		}
		return reason.trim();
	}

	private static String defaultText(String value, String defaultValue) {
		if (value == null || value.isBlank()) {
			return defaultValue;
		}
		return value.trim();
	}

	private static String requireText(String name, String value) {
		if (value == null || value.isBlank()) {
			throw new IllegalArgumentException(name + " must not be blank");
		}
		return value.trim();
	}

	private static IllegalStateException malformed() {
		return new IllegalStateException(MALFORMED_OUTPUT);
	}

	private record ValidationResponse(String status, String reason, ClassificationResponse classification) {
	}

	private record ClassificationResponse(String domain, String intent, double confidence, String notes) {
	}

	private record TeamPlanResponse(List<String> skills, int expertCount, List<String> roles, String initialStrategy) {
	}

	private record ExpertTeamResponse(List<ExpertResponse> experts) {
	}

	private record ExpertResponse(String id, String name, String role, String personality, String mission, String uiAccent) {
	}

	private record MessageResponse(String messageType, String content) {
	}

	private record DecisionResponse(String action, String reason, String nextExpertId) {
	}

	private record FinalAnswerResponse(String content, String rationale, String stopReason) {
	}

	private record JudgementResponse(String verdict, JudgeRubricResponse rubric, String reason, List<String> revisionHints) {
	}

	private record JudgeRubricResponse(int relevance, int correctness, int completeness, int clarity, int safety, int overall) {
	}
}

@FunctionalInterface
interface LlmClient {

	String call(String prompt);
}

final class ChatModelLlmClient implements LlmClient {

	private final ChatModel chatModel;

	ChatModelLlmClient(ChatModel chatModel) {
		this.chatModel = Objects.requireNonNull(chatModel, "chatModel must not be null");
	}

	@Override
	public String call(String prompt) {
		return chatModel.call(prompt);
	}
}
