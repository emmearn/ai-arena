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
		assertThat(body).doesNotContain("Live reasoning arena");
		assertThat(body).doesNotContain("<h1>AI Arena</h1>");
	}
}
