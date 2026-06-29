package com.marnone.ai_arena.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.ai.chat.model.ChatModel;

import com.marnone.ai_arena.ai.AiClientPort;
import com.marnone.ai_arena.ai.FakeAiAdapter;
import com.marnone.ai_arena.ai.SpringAiAdapter;
import com.marnone.ai_arena.application.DebateOrchestrator;
import com.marnone.ai_arena.application.FinalAnswerService;
import com.marnone.ai_arena.application.PlanningService;
import com.marnone.ai_arena.application.RunArenaSessionUseCase;
import com.marnone.ai_arena.application.SessionEventMapper;
import com.marnone.ai_arena.application.OrchestratedAiExpertFactory;
import com.marnone.ai_arena.application.ValidationService;

/**
 * Wires the application services around the currently selected AI adapter.
 */
@Configuration(proxyBeanMethods = false)
public class ApplicationConfiguration {

	@Bean
	@ConditionalOnProperty(prefix = "arena.ai", name = "adapter", havingValue = "openai")
	AiClientPort springAiClientPort(ChatModel chatModel, ArenaProperties arenaProperties) {
		return new SpringAiAdapter(chatModel, arenaProperties.getAi().getRequestTimeout());
	}

	@Bean
	@ConditionalOnMissingBean(AiClientPort.class)
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
	OrchestratedAiExpertFactory expertFactory(AiClientPort aiClientPort) {
		return new OrchestratedAiExpertFactory(aiClientPort);
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
		OrchestratedAiExpertFactory expertFactory,
		DebateOrchestrator debateOrchestrator,
		FinalAnswerService finalAnswerService,
		ArenaProperties arenaProperties
	) {
		return new RunArenaSessionUseCase(
			validationService,
			planningService,
			expertFactory,
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
