package com.marnone.ai_arena.application;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.marnone.ai_arena.ai.ValidationAiPort;
import com.marnone.ai_arena.config.ArenaProperties;
import com.marnone.ai_arena.domain.Question;
import com.marnone.ai_arena.domain.ValidationResult;

public class ValidationService {

	private static final List<String> HOSTILE_PATTERNS = List.of(
		"ignore previous instructions",
		"jailbreak",
		"system prompt",
		"developer message",
		"reveal your instructions",
		"bypass safety"
	);

	private final ValidationAiPort validationAiPort;
	private final ArenaProperties arenaProperties;
	private final Clock clock;

	public ValidationService(ValidationAiPort validationAiPort, ArenaProperties arenaProperties) {
		this(validationAiPort, arenaProperties, Clock.systemUTC());
	}

	ValidationService(ValidationAiPort validationAiPort, ArenaProperties arenaProperties, Clock clock) {
		this.validationAiPort = Objects.requireNonNull(validationAiPort, "validationAiPort must not be null");
		this.arenaProperties = Objects.requireNonNull(arenaProperties, "arenaProperties must not be null");
		this.clock = Objects.requireNonNull(clock, "clock must not be null");
	}

	public ValidationResult validate(String input) {
		if (input == null || input.trim().isEmpty()) {
			return ValidationResult.rejected("Question must not be empty.");
		}
		String text = input.trim();
		if (text.length() > arenaProperties.getLimits().getMaxInputCharacters()) {
			return ValidationResult.rejected("Question exceeds the maximum allowed length.");
		}
		if (containsHostilePattern(text)) {
			return ValidationResult.rejected("Question rejected by local safety checks.");
		}
		return validationAiPort.validate(new Question(text, Instant.now(clock)));
	}

	private static boolean containsHostilePattern(String text) {
		String normalized = text.toLowerCase(Locale.ROOT);
		return HOSTILE_PATTERNS.stream().anyMatch(normalized::contains);
	}
}
