package com.marnone.ai_arena.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import com.marnone.ai_arena.AiArenaApplication;
import com.marnone.ai_arena.application.RunArenaSessionUseCase;
import com.marnone.ai_arena.application.SessionEvent;

@SpringBootTest(classes = { AiArenaApplication.class, ArenaControllerErrorTests.FailingUseCaseConfiguration.class })
@AutoConfigureMockMvc
class ArenaControllerErrorTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private RunArenaSessionUseCase runArenaSessionUseCase;

	@BeforeEach
	void resetMock() {
		reset(runArenaSessionUseCase);
	}

	@Test
	void mapsInvalidJsonToSafeErrorResponse() throws Exception {
		MvcResult result = mockMvc.perform(arenaRequest("{\"question\":"))
			.andExpect(status().isBadRequest())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andReturn();

		String body = result.getResponse().getContentAsString();

		assertThat(body).contains("\"code\":\"INVALID_REQUEST\"");
		assertThat(body).contains("\"message\":\"Request body must be valid JSON.\"");
		assertSafePublicError(body);
	}

	@Test
	void streamsSafeErrorWhenProviderFails() throws Exception {
		doThrow(new IllegalStateException("provider failed at C:\\secret\\provider.java"))
			.when(runArenaSessionUseCase)
			.run(anyString(), anyConsumer());

		MvcResult asyncResult = mockMvc.perform(arenaRequest("{\"question\":\"How should the provider failure be handled?\"}"))
			.andExpect(request().asyncStarted())
			.andReturn();
		MvcResult result = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch(asyncResult))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM))
			.andReturn();

		String body = result.getResponse().getContentAsString();

		assertThat(body).contains("event:ERROR");
		assertThat(body).contains("Unable to run arena session.");
		assertSafePublicError(body);
	}

	@Test
	void mapsMissingStaticResourceToNotFound() throws Exception {
		MvcResult result = mockMvc.perform(get("/missing-static-resource.png"))
			.andExpect(status().isNotFound())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andReturn();

		String body = result.getResponse().getContentAsString();

		assertThat(body).contains("\"code\":\"NOT_FOUND\"");
		assertThat(body).contains("\"message\":\"Resource not found.\"");
		assertSafePublicError(body);
	}

	private MockHttpServletRequestBuilder arenaRequest(String json) {
		return post("/api/arena/sessions")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.TEXT_EVENT_STREAM)
			.content(json);
	}

	@SuppressWarnings("unchecked")
	private static Consumer<SessionEvent> anyConsumer() {
		return any(Consumer.class);
	}

	private static void assertSafePublicError(String body) {
		assertThat(body).doesNotContain("Exception");
		assertThat(body).doesNotContain("java.");
		assertThat(body).doesNotContain("com.marnone");
		assertThat(body).doesNotContain("stack");
		assertThat(body).doesNotContain("trace");
		assertThat(body).doesNotContain("C:\\");
		assertThat(body).doesNotContain("secret");
	}

	@TestConfiguration(proxyBeanMethods = false)
	static class FailingUseCaseConfiguration {

		@Bean
		@Primary
		RunArenaSessionUseCase failingRunArenaSessionUseCase() {
			return mock(RunArenaSessionUseCase.class);
		}
	}
}
