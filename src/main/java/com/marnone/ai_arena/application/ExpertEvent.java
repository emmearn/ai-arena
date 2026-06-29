package com.marnone.ai_arena.application;

import java.util.Objects;

public record ExpertEvent(String id, String name, String role, String personality, String mission, String uiAccent) {

	public ExpertEvent {
		id = requireText("id", id);
		name = requireText("name", name);
		role = requireText("role", role);
		personality = requireText("personality", personality);
		mission = requireText("mission", mission);
		uiAccent = requireText("uiAccent", uiAccent);
	}

	private static String requireText(String name, String value) {
		Objects.requireNonNull(value, name + " must not be null");
		String trimmed = value.trim();
		if (trimmed.isEmpty()) {
			throw new IllegalArgumentException(name + " must not be blank");
		}
		return trimmed;
	}
}
