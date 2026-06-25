package com.marnone.ai_arena.ai;

import com.marnone.ai_arena.domain.Question;
import com.marnone.ai_arena.domain.ValidationResult;

public interface ValidationAiPort {

	ValidationResult validate(Question question);
}
