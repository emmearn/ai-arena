package com.marnone.ai_arena.application;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.marnone.ai_arena.config.ArenaProperties;
import com.marnone.ai_arena.domain.ArenaLimits;
import com.marnone.ai_arena.domain.DebateMessage;
import com.marnone.ai_arena.domain.FinalAnswer;
import com.marnone.ai_arena.domain.Question;
import com.marnone.ai_arena.domain.OrchestratedAiExpert;
import com.marnone.ai_arena.domain.SupervisorAction;
import com.marnone.ai_arena.domain.TeamPlan;
import com.marnone.ai_arena.domain.ValidationResult;
import com.marnone.ai_arena.domain.ValidationStatus;

/**
 * Coordinates the full arena session from validation through final answer generation.
 */
public class RunArenaSessionUseCase {

	private static final Logger log = LoggerFactory.getLogger(RunArenaSessionUseCase.class);

	private final ValidationService validationService;
	private final PlanningService planningService;
	private final OrchestratedAiExpertFactory expertFactory;
	private final DebateOrchestrator debateOrchestrator;
	private final FinalAnswerService finalAnswerService;
	private final JudgeService judgeService;
	private final ArenaProperties arenaProperties;
	private final Clock clock;

	public RunArenaSessionUseCase(
		ValidationService validationService,
		PlanningService planningService,
		OrchestratedAiExpertFactory expertFactory,
		DebateOrchestrator debateOrchestrator,
		FinalAnswerService finalAnswerService,
		JudgeService judgeService,
		ArenaProperties arenaProperties
	) {
		this(validationService, planningService, expertFactory, debateOrchestrator, finalAnswerService, judgeService, arenaProperties, Clock.systemUTC());
	}

	RunArenaSessionUseCase(
		ValidationService validationService,
		PlanningService planningService,
		OrchestratedAiExpertFactory expertFactory,
		DebateOrchestrator debateOrchestrator,
		FinalAnswerService finalAnswerService,
		JudgeService judgeService,
		ArenaProperties arenaProperties,
		Clock clock
	) {
		this.validationService = Objects.requireNonNull(validationService, "validationService must not be null");
		this.planningService = Objects.requireNonNull(planningService, "planningService must not be null");
		this.expertFactory = Objects.requireNonNull(expertFactory, "expertFactory must not be null");
		this.debateOrchestrator = Objects.requireNonNull(debateOrchestrator, "debateOrchestrator must not be null");
		this.finalAnswerService = Objects.requireNonNull(finalAnswerService, "finalAnswerService must not be null");
		this.judgeService = Objects.requireNonNull(judgeService, "judgeService must not be null");
		this.arenaProperties = Objects.requireNonNull(arenaProperties, "arenaProperties must not be null");
		this.clock = Objects.requireNonNull(clock, "clock must not be null");
	}

	public ArenaSessionResult run(String input) {
		return run(input, event -> {
		});
	}

	public ArenaSessionResult run(String input, Consumer<SessionEvent> eventConsumer) {
		Objects.requireNonNull(eventConsumer, "eventConsumer must not be null");
		String correlationId = correlationId();
		Instant startedAt = clock.instant();
		String stage = "validation";
		log.info("Arena session started correlationId={} inputLength={}", correlationId, safeLength(input));
		try {
			eventConsumer.accept(SessionEvent.validationStarted(new ValidationEvent(null, null, null)));
			ValidationResult validation = validationService.validate(input);
			if (validation.status() != ValidationStatus.VALID) {
				if (validation.status() == ValidationStatus.REJECTED) {
					eventConsumer.accept(SessionEvent.validationRejected(toValidationEvent(validation)));
				}
				else {
					eventConsumer.accept(SessionEvent.error(new ErrorEvent(reasonOrDefault(validation, "Unable to validate request."))));
				}
				log.warn(
					"Arena session stopped during validation correlationId={} validationStatus={} validationOutcome={} durationMs={}",
					correlationId,
					validation.status(),
					validationOutcome(validation),
					elapsedMillis(startedAt)
				);
				return ArenaSessionResult.rejected(validation);
			}
			eventConsumer.accept(SessionEvent.validationAccepted(toValidationEvent(validation)));
			log.info("Arena validation accepted correlationId={}", correlationId);

			stage = "planning";
			Question question = new Question(input, Instant.now(clock));
			TeamPlan plan = planningService.plan(question);
			eventConsumer.accept(SessionEvent.teamPlanned(new TeamEvent(plan.expertCount(), plan.roles(), plan.initialStrategy())));
			log.info(
				"Arena team planned correlationId={} expertCount={} skillCount={} roleCount={}",
				correlationId,
				plan.expertCount(),
				plan.skills().size(),
				plan.roles().size()
			);

			stage = "expert_factory";
			List<OrchestratedAiExpert> team = expertFactory.createTeam(plan);
			team.stream()
				.map(RunArenaSessionUseCase::toExpertEvent)
				.map(SessionEvent::expertCreated)
				.forEach(eventConsumer);
			log.info("Arena experts created correlationId={} expertCount={}", correlationId, team.size());

			stage = "debate";
			DebateResult debate = debateOrchestrator.run(
				question,
				team,
				toArenaLimits(arenaProperties.getLimits()),
				message -> eventConsumer.accept(SessionEvent.debateMessage(toMessageEvent(message)))
			);
			eventConsumer.accept(SessionEvent.supervisorDecision(new DecisionEvent(SupervisorAction.STOP, debate.stopReason(), null)));
			log.info(
				"Arena debate stopped correlationId={} messageCount={} stopReasonCategory={}",
				correlationId,
				debate.messages().size(),
				stopReasonCategory(debate.stopReason())
			);

			stage = "final_answer";
			FinalAnswer synthesizedAnswer = finalAnswerService.synthesize(question, debate);
			log.info(
				"Arena final answer synthesized correlationId={} contentLength={} rationaleLength={}",
				correlationId,
				safeLength(synthesizedAnswer.content()),
				safeLength(synthesizedAnswer.rationale())
			);

			stage = "judgement";
			JudgedFinalAnswer judgedFinalAnswer = judgeService.judge(question, debate, synthesizedAnswer);
			eventConsumer.accept(SessionEvent.judgement(toJudgementEvent(judgedFinalAnswer)));
			FinalAnswer finalAnswer = judgedFinalAnswer.finalAnswer();
			eventConsumer.accept(SessionEvent.finalAnswer(new FinalEvent(finalAnswer.content(), finalAnswer.rationale(), finalAnswer.stopReason())));
			log.info(
				"Arena final answer judged correlationId={} verdict={} overallScore={} fallbackApplied={}",
				correlationId,
				judgedFinalAnswer.judgement().verdict(),
				judgedFinalAnswer.judgement().rubric().overall(),
				judgedFinalAnswer.fallbackApplied()
			);
			log.info(
				"Arena session completed correlationId={} durationMs={} expertCount={} messageCount={} stopReasonCategory={}",
				correlationId,
				elapsedMillis(startedAt),
				team.size(),
				debate.messages().size(),
				stopReasonCategory(debate.stopReason())
			);
			return ArenaSessionResult.completed(validation, plan, team, debate, judgedFinalAnswer);
		}
		catch (RuntimeException ex) {
			log.error(
				"Arena session failed correlationId={} stage={} durationMs={} exceptionType={}",
				correlationId,
				stage,
				elapsedMillis(startedAt),
				ex.getClass().getName()
			);
			throw ex;
		}
	}

	private static ArenaLimits toArenaLimits(ArenaProperties.Limits limits) {
		return new ArenaLimits(
			limits.getMaxExperts(),
			limits.getMaxTurns(),
			limits.getMaxMessages(),
			limits.getTimeout(),
			limits.getMaxInputCharacters()
		);
	}

	private static ValidationEvent toValidationEvent(ValidationResult validation) {
		return new ValidationEvent(validation.status(), validation.reason(), validation.classificationHint());
	}

	private static ExpertEvent toExpertEvent(OrchestratedAiExpert expert) {
		return new ExpertEvent(
			expert.id(),
			expert.name(),
			expert.role(),
			expert.personality(),
			expert.mission(),
			expert.uiAccent()
		);
	}

	private static MessageEvent toMessageEvent(DebateMessage message) {
		return new MessageEvent(message.id(), message.expertId(), message.turn(), message.type(), message.content());
	}

	private static JudgementEvent toJudgementEvent(JudgedFinalAnswer judgedFinalAnswer) {
		return new JudgementEvent(
			judgedFinalAnswer.judgement().verdict(),
			judgedFinalAnswer.judgement().rubric(),
			judgedFinalAnswer.judgement().reason(),
			judgedFinalAnswer.judgement().revisionHints(),
			judgedFinalAnswer.fallbackApplied()
		);
	}

	private static String reasonOrDefault(ValidationResult validation, String defaultReason) {
		if (validation.reason() == null || validation.reason().isBlank()) {
			return defaultReason;
		}
		return validation.reason();
	}

	private static String correlationId() {
		String requestId = MDC.get("requestId");
		if (requestId == null || requestId.isBlank()) {
			return UUID.randomUUID().toString();
		}
		return requestId;
	}

	private long elapsedMillis(Instant startedAt) {
		return Duration.between(startedAt, clock.instant()).toMillis();
	}

	private static int safeLength(String value) {
		if (value == null) {
			return 0;
		}
		return value.length();
	}

	private static String validationOutcome(ValidationResult validation) {
		if (validation.status() == ValidationStatus.ERROR) {
			return "ERROR";
		}
		return switch (reasonOrDefault(validation, "")) {
			case "Question must not be empty." -> "LOCAL_EMPTY";
			case "Question exceeds the maximum allowed length." -> "LOCAL_TOO_LONG";
			case "Question rejected by local safety checks." -> "LOCAL_SAFETY";
			default -> "REJECTED";
		};
	}

	private static String stopReasonCategory(String stopReason) {
		if (stopReason == null || stopReason.isBlank()) {
			return "UNSPECIFIED";
		}
		if (stopReason.contains("maximum message")) {
			return "MAX_MESSAGES";
		}
		if (stopReason.contains("maximum turn")) {
			return "MAX_TURNS";
		}
		if (stopReason.contains("timeout")) {
			return "TIMEOUT";
		}
		if (stopReason.contains("empty")) {
			return "EMPTY_TEAM";
		}
		if (stopReason.contains("unknown expert")) {
			return "INVALID_SUPERVISOR";
		}
		if (stopReason.contains("inconsistent")) {
			return "INVALID_MESSAGE";
		}
		return "SUPERVISOR";
	}
}
