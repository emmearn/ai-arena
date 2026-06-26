package com.marnone.ai_arena.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.marnone.ai_arena.ai.AiClientPort;
import com.marnone.ai_arena.ai.FakeAiAdapter;
import com.marnone.ai_arena.application.DebateOrchestrator;
import com.marnone.ai_arena.application.FinalAnswerService;
import com.marnone.ai_arena.application.PlanningService;
import com.marnone.ai_arena.application.RunArenaSessionUseCase;
import com.marnone.ai_arena.application.SessionEventMapper;
import com.marnone.ai_arena.application.SpecialistFactory;
import com.marnone.ai_arena.application.ValidationService;

@Configuration(proxyBeanMethods = false)
public class ApplicationConfiguration {

	@Bean
	AiClientPort aiClientPort() {
		return new FakeAiAdapter();
	}

	@Bean
	ValidationService validationService(AiClientPort aiClientPort, ArenaProperties arenaProperties) {
		return new ValidationService(aiClientPort, arenaProperties);
	}

	@Bean
	PlanningService planningService(AiClientPort aiClientPort, ArenaProperties arenaProperties) {
		return new PlanningService(aiClientPort, arenaProperties);
	}

	@Bean
	SpecialistFactory specialistFactory(AiClientPort aiClientPort) {
		return new SpecialistFactory(aiClientPort);
	}

	@Bean
	DebateOrchestrator debateOrchestrator(AiClientPort aiClientPort) {
		return new DebateOrchestrator(aiClientPort, aiClientPort);
	}

	@Bean
	FinalAnswerService finalAnswerService(AiClientPort aiClientPort) {
		return new FinalAnswerService(aiClientPort);
	}

	@Bean
	RunArenaSessionUseCase runArenaSessionUseCase(
		ValidationService validationService,
		PlanningService planningService,
		SpecialistFactory specialistFactory,
		DebateOrchestrator debateOrchestrator,
		FinalAnswerService finalAnswerService,
		ArenaProperties arenaProperties
	) {
		return new RunArenaSessionUseCase(
			validationService,
			planningService,
			specialistFactory,
			debateOrchestrator,
			finalAnswerService,
			arenaProperties
		);
	}

	@Bean
	SessionEventMapper sessionEventMapper() {
		return new SessionEventMapper();
	}
}
