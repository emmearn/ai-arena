package com.marnone.ai_arena.domain;

import java.util.Objects;

public record Specialist(String id, String name, String role, String personality, String mission) {

	public Specialist {
		Objects.requireNonNull(id, "id must not be null");
		Objects.requireNonNull(name, "name must not be null");
		Objects.requireNonNull(role, "role must not be null");
		Objects.requireNonNull(personality, "personality must not be null");
		Objects.requireNonNull(mission, "mission must not be null");
	}
}
