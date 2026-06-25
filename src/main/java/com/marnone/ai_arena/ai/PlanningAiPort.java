package com.marnone.ai_arena.ai;

import com.marnone.ai_arena.domain.ArenaLimits;
import com.marnone.ai_arena.domain.Question;
import com.marnone.ai_arena.domain.TeamPlan;

public interface PlanningAiPort {

	TeamPlan planTeam(Question question, ArenaLimits limits);
}
