package com.marnone.ai_arena.ai;

import com.marnone.ai_arena.domain.JudgeRequest;
import com.marnone.ai_arena.domain.Judgement;

public interface JudgeAiPort {

	Judgement judge(JudgeRequest request);
}
