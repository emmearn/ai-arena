package com.marnone.ai_arena.ai;

/**
 * Composite AI port used by the current application wiring.
 */
public interface AiClientPort extends ValidationAiPort, PlanningAiPort, SpecialistAiPort, DebateAiPort, SupervisorAiPort {
}
