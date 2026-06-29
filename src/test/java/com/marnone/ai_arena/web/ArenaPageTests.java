package com.marnone.ai_arena.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ArenaPageTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void servesSinglePageArenaShell() throws Exception {
		String body = mockMvc.perform(get("/index.html"))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
			.andReturn()
			.getResponse()
			.getContentAsString();

		assertThat(body).contains("AI Arena");
		assertThat(body).contains("Ask the arena");
		assertThat(body).contains("Orchestrated experts");
		assertThat(body).contains("Progressive exchange");
		assertThat(body).contains("Final answer");
		assertThat(body).contains("/assets/logo.png");
		assertThat(body).contains("/app.js");
		assertThat(body).contains("id=\"question-form\"");
		assertThat(body).contains("id=\"question-error\"");
		assertThat(body).contains("id=\"validation-status\"");
		assertThat(body).contains("id=\"expert-list\"");
		assertThat(body).contains("id=\"team-status\"");
		assertThat(body).contains("id=\"message-list\"");
		assertThat(body).contains("id=\"debate-status\"");
		assertThat(body).contains("id=\"final-answer\"");
		assertThat(body).contains("id=\"final-rationale\"");
		assertThat(body).contains("id=\"final-stop-reason\"");
		assertThat(body).contains("Accepted questions will assemble the expert team here.");
		assertThat(body).contains("Accepted questions will stream debate messages here.");
		assertThat(body).doesNotContain("<h3>Analyst</h3>");
		assertThat(body).doesNotContain("Live reasoning arena");
		assertThat(body).doesNotContain("<h1>AI Arena</h1>");
	}

	@Test
	void servesArenaInteractionScript() throws Exception {
		String body = mockMvc.perform(get("/app.js"))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith("text/javascript"))
			.andReturn()
			.getResponse()
			.getContentAsString();

		assertThat(body).contains("Enter a question before starting the arena.");
		assertThat(body).contains("VALIDATION_ACCEPTED");
		assertThat(body).contains("VALIDATION_REJECTED");
		assertThat(body).contains("EXPERT_CREATED");
		assertThat(body).contains("DEBATE_MESSAGE");
		assertThat(body).contains("FINAL_ANSWER");
		assertThat(body).contains("renderFinalAnswer");
		assertThat(body).contains("renderStreamError");
		assertThat(body).contains("/api/arena/sessions");
		assertThat(body).contains("document.createElement(\"article\")");
		assertThat(body).contains("textContent = payload.mission");
		assertThat(body).contains("textContent = payload.content");
		assertThat(body).contains("is-active");
		assertThat(body).contains("safeAccent");
	}

	@Test
	void rendersGeneratedOutputWithTextContentOnly() throws Exception {
		String body = mockMvc.perform(get("/app.js"))
			.andExpect(status().isOk())
			.andExpect(content().contentTypeCompatibleWith("text/javascript"))
			.andReturn()
			.getResponse()
			.getContentAsString();

		assertThat(body).contains("mission.textContent = payload.mission");
		assertThat(body).contains("personality.textContent = payload.personality");
		assertThat(body).contains("content.textContent = payload.content");
		assertThat(body).contains("finalAnswer.textContent = payload.content");
		assertThat(body).contains("finalRationale.textContent = payload.rationale");
		assertThat(body).contains("finalStopReason.textContent = payload.stopReason");
		assertThat(body).doesNotContain(".innerHTML");
		assertThat(body).doesNotContain("insertAdjacentHTML");
	}
}
