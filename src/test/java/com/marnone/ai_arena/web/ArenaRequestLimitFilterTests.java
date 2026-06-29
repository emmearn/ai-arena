package com.marnone.ai_arena.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest(properties = {
	"arena.http.max-payload-bytes=96",
	"arena.http.rate-limit-max-requests=1",
	"arena.http.rate-limit-window=1m"
})
@AutoConfigureMockMvc
class ArenaRequestLimitFilterTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void rejectsPayloadThatExceedsHttpLimit() throws Exception {
		String body = mockMvc.perform(arenaRequest("{\"question\":\"" + "x".repeat(120) + "\"}"))
			.andExpect(status().is(413))
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andReturn()
			.getResponse()
			.getContentAsString();

		assertThat(body).contains("\"code\":\"PAYLOAD_TOO_LARGE\"");
		assertThat(body).contains("\"message\":\"Request payload is too large.\"");
		assertSafePublicError(body);
	}

	@Test
	void rejectsRequestsAfterRateLimitIsExceeded() throws Exception {
		MvcResult asyncResult = mockMvc.perform(arenaRequest("{\"question\":\"How should AI Arena handle limits?\"}"))
			.andExpect(request().asyncStarted())
			.andReturn();
		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch(asyncResult))
			.andExpect(status().isOk());

		String body = mockMvc.perform(arenaRequest("{\"question\":\"How should AI Arena handle another request?\"}"))
			.andExpect(status().isTooManyRequests())
			.andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
			.andReturn()
			.getResponse()
			.getContentAsString();

		assertThat(body).contains("\"code\":\"RATE_LIMITED\"");
		assertThat(body).contains("\"message\":\"Too many requests. Please try again later.\"");
		assertSafePublicError(body);
	}

	private MockHttpServletRequestBuilder arenaRequest(String json) {
		return post("/api/arena/sessions")
			.contentType(MediaType.APPLICATION_JSON)
			.accept(MediaType.TEXT_EVENT_STREAM)
			.content(json);
	}

	private static void assertSafePublicError(String body) {
		assertThat(body).doesNotContain("Exception");
		assertThat(body).doesNotContain("java.");
		assertThat(body).doesNotContain("com.marnone");
		assertThat(body).doesNotContain("stack");
		assertThat(body).doesNotContain("trace");
	}
}
