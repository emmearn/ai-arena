package com.marnone.ai_arena.ai;

import java.util.List;

import com.marnone.ai_arena.domain.ArenaLimits;
import com.marnone.ai_arena.domain.DebateMessage;
import com.marnone.ai_arena.domain.FinalAnswer;
import com.marnone.ai_arena.domain.Question;
import com.marnone.ai_arena.domain.SupervisorDecision;

public interface SupervisorAiPort {

	SupervisorDecision decide(List<DebateMessage> messages, ArenaLimits limits);

	FinalAnswer synthesize(Question question, List<DebateMessage> messages, String stopReason);
}
