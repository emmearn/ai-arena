package com.marnone.ai_arena.application;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.marnone.ai_arena.domain.DebateMessage;
import com.marnone.ai_arena.domain.FinalAnswer;
import com.marnone.ai_arena.domain.Specialist;
import com.marnone.ai_arena.domain.SupervisorAction;
import com.marnone.ai_arena.domain.TeamPlan;
import com.marnone.ai_arena.domain.ValidationResult;
import com.marnone.ai_arena.domain.ValidationStatus;

public class SessionEventMapper {

	public List<SessionEvent> map(ArenaSessionResult result) {
		Objects.requireNonNull(result, "result must not be null");
		List<SessionEvent> events = new ArrayList<>();
		events.add(SessionEvent.validationStarted(toValidationEvent(result.validation())));

		if (result.validation().status() == ValidationStatus.REJECTED) {
			events.add(SessionEvent.validationRejected(toValidationEvent(result.validation())));
			return List.copyOf(events);
		}
		if (result.validation().status() == ValidationStatus.ERROR) {
			events.add(SessionEvent.error(new ErrorEvent(reasonOrDefault(result.validation(), "Request rejected."))));
			return List.copyOf(events);
		}

		events.add(SessionEvent.validationAccepted(toValidationEvent(result.validation())));
		events.add(SessionEvent.teamPlanned(toTeamEvent(result.plan())));
		result.team().stream()
			.map(SessionEventMapper::toSpecialistEvent)
			.map(SessionEvent::specialistCreated)
			.forEach(events::add);
		result.debate().messages().stream()
			.map(SessionEventMapper::toMessageEvent)
			.map(SessionEvent::debateMessage)
			.forEach(events::add);
		events.add(SessionEvent.supervisorDecision(new DecisionEvent(SupervisorAction.STOP, result.debate().stopReason(), null)));
		events.add(SessionEvent.finalAnswer(toFinalEvent(result.finalAnswer())));
		return List.copyOf(events);
	}

	private static ValidationEvent toValidationEvent(ValidationResult validation) {
		return new ValidationEvent(validation.status(), validation.reason(), validation.classificationHint());
	}

	private static TeamEvent toTeamEvent(TeamPlan plan) {
		Objects.requireNonNull(plan, "plan must not be null");
		return new TeamEvent(plan.specialistCount(), plan.roles(), plan.initialStrategy());
	}

	private static SpecialistEvent toSpecialistEvent(Specialist specialist) {
		return new SpecialistEvent(
			specialist.id(),
			specialist.name(),
			specialist.role(),
			specialist.personality(),
			specialist.mission(),
			specialist.uiAccent()
		);
	}

	private static MessageEvent toMessageEvent(DebateMessage message) {
		return new MessageEvent(message.id(), message.specialistId(), message.turn(), message.type(), message.content());
	}

	private static FinalEvent toFinalEvent(FinalAnswer finalAnswer) {
		Objects.requireNonNull(finalAnswer, "finalAnswer must not be null");
		return new FinalEvent(finalAnswer.content(), finalAnswer.rationale(), finalAnswer.stopReason());
	}

	private static String reasonOrDefault(ValidationResult validation, String defaultReason) {
		if (validation.reason() == null || validation.reason().isBlank()) {
			return defaultReason;
		}
		return validation.reason();
	}
}
