package com.marnone.ai_arena.ai;

import java.util.List;

import com.marnone.ai_arena.domain.Specialist;
import com.marnone.ai_arena.domain.TeamPlan;

public interface SpecialistAiPort {

	List<Specialist> createSpecialists(TeamPlan plan);
}
