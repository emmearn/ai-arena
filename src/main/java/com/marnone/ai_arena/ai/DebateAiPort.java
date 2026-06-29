package com.marnone.ai_arena.ai;

import java.util.List;

import com.marnone.ai_arena.domain.DebateMessage;
import com.marnone.ai_arena.domain.Question;
import com.marnone.ai_arena.domain.OrchestratedAiExpert;

public interface DebateAiPort {

	DebateMessage createMessage(Question question, OrchestratedAiExpert expert, List<DebateMessage> previousMessages, int turn);
}
