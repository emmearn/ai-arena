package com.marnone.ai_arena.ai;

import java.util.List;

import com.marnone.ai_arena.domain.OrchestratedAiExpert;
import com.marnone.ai_arena.domain.TeamPlan;

public interface OrchestratedAiExpertAiPort {

	List<OrchestratedAiExpert> createExperts(TeamPlan plan);
}
