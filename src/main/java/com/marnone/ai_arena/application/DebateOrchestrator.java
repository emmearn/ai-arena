package com.marnone.ai_arena.application;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.marnone.ai_arena.ai.DebateAiPort;
import com.marnone.ai_arena.ai.SupervisorAiPort;
import com.marnone.ai_arena.domain.ArenaLimits;
import com.marnone.ai_arena.domain.DebateMessage;
import com.marnone.ai_arena.domain.Question;
import com.marnone.ai_arena.domain.Specialist;
import com.marnone.ai_arena.domain.SupervisorAction;
import com.marnone.ai_arena.domain.SupervisorDecision;

/**
 * Runs the sequential specialist debate and enforces turn, message, timeout, and supervisor limits.
 */
public class DebateOrchestrator {

	private static final String MAX_MESSAGES_STOP = "Stopped because the maximum message limit was reached.";
	private static final String MAX_TURNS_STOP = "Stopped because the maximum turn limit was reached.";
	private static final String TIMEOUT_STOP = "Stopped because the debate timeout was reached.";
	private static final String EMPTY_TEAM_STOP = "Stopped because the debate team is empty.";
	private static final String INVALID_SUPERVISOR_STOP = "Stopped because the supervisor selected an unknown specialist.";
	private static final String INVALID_MESSAGE_STOP = "Stopped because the debate message was inconsistent.";
	private static final String SUPERVISOR_STOP = "Stopped by supervisor decision.";

	private final DebateAiPort debateAiPort;
	private final SupervisorAiPort supervisorAiPort;
	private final Clock clock;

	public DebateOrchestrator(DebateAiPort debateAiPort, SupervisorAiPort supervisorAiPort) {
		this(debateAiPort, supervisorAiPort, Clock.systemUTC());
	}

	DebateOrchestrator(DebateAiPort debateAiPort, SupervisorAiPort supervisorAiPort, Clock clock) {
		this.debateAiPort = Objects.requireNonNull(debateAiPort, "debateAiPort must not be null");
		this.supervisorAiPort = Objects.requireNonNull(supervisorAiPort, "supervisorAiPort must not be null");
		this.clock = Objects.requireNonNull(clock, "clock must not be null");
	}

	public DebateResult run(Question question, List<Specialist> team, ArenaLimits limits) {
		return run(question, team, limits, message -> {
		});
	}

	public DebateResult run(Question question, List<Specialist> team, ArenaLimits limits, Consumer<DebateMessage> messageConsumer) {
		Objects.requireNonNull(question, "question must not be null");
		Objects.requireNonNull(limits, "limits must not be null");
		Objects.requireNonNull(messageConsumer, "messageConsumer must not be null");
		List<Specialist> specialists = List.copyOf(Objects.requireNonNull(team, "team must not be null"));
		if (specialists.isEmpty()) {
			return new DebateResult(List.of(), EMPTY_TEAM_STOP);
		}

		Instant startedAt = clock.instant();
		List<DebateMessage> messages = new ArrayList<>();
		Specialist nextSpecialist = specialists.getFirst();

		for (int turn = 1; ; turn++) {
			if (isTimedOut(startedAt, limits.timeout())) {
				return new DebateResult(messages, TIMEOUT_STOP);
			}
			if (turn > limits.maxTurns()) {
				return new DebateResult(messages, MAX_TURNS_STOP);
			}
			if (messages.size() >= limits.maxMessages()) {
				return new DebateResult(messages, MAX_MESSAGES_STOP);
			}

			DebateMessage message = debateAiPort.createMessage(question, nextSpecialist, List.copyOf(messages), turn);
			if (!isConsistentMessage(message, nextSpecialist, turn)) {
				return new DebateResult(messages, INVALID_MESSAGE_STOP);
			}
			messages.add(message);
			messageConsumer.accept(message);

			if (isTimedOut(startedAt, limits.timeout())) {
				return new DebateResult(messages, TIMEOUT_STOP);
			}
			if (messages.size() >= limits.maxMessages()) {
				return new DebateResult(messages, MAX_MESSAGES_STOP);
			}
			if (turn >= limits.maxTurns()) {
				return new DebateResult(messages, MAX_TURNS_STOP);
			}

			SupervisorDecision decision = supervisorAiPort.decide(List.copyOf(messages), limits);
			if (decision.action() == SupervisorAction.STOP) {
				return new DebateResult(messages, explicitReason(decision.reason()));
			}
			Specialist selectedSpecialist = specialistById(specialists, decision.nextSpecialistId());
			if (selectedSpecialist == null) {
				return new DebateResult(messages, INVALID_SUPERVISOR_STOP);
			}
			nextSpecialist = selectedSpecialist;
		}
	}

	private boolean isTimedOut(Instant startedAt, Duration timeout) {
		return !Duration.between(startedAt, clock.instant()).minus(timeout).isNegative();
	}

	private static boolean isConsistentMessage(DebateMessage message, Specialist specialist, int turn) {
		Objects.requireNonNull(message, "message must not be null");
		return message.turn() == turn && message.specialistId().equals(specialist.id());
	}

	private static Specialist specialistById(List<Specialist> specialists, String id) {
		return specialists.stream()
			.filter(specialist -> specialist.id().equals(id))
			.findFirst()
			.orElse(null);
	}

	private static String explicitReason(String reason) {
		if (reason == null || reason.isBlank()) {
			return SUPERVISOR_STOP;
		}
		return reason;
	}
}
