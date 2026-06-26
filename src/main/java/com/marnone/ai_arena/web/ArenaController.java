package com.marnone.ai_arena.web;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.marnone.ai_arena.application.ErrorEvent;
import com.marnone.ai_arena.application.RunArenaSessionUseCase;
import com.marnone.ai_arena.application.SessionEvent;

@RestController
@RequestMapping("/api/arena")
public class ArenaController {

	private final RunArenaSessionUseCase runArenaSessionUseCase;

	public ArenaController(RunArenaSessionUseCase runArenaSessionUseCase) {
		this.runArenaSessionUseCase = Objects.requireNonNull(runArenaSessionUseCase, "runArenaSessionUseCase must not be null");
	}

	@PostMapping(
		value = "/sessions",
		consumes = MediaType.APPLICATION_JSON_VALUE,
		produces = MediaType.TEXT_EVENT_STREAM_VALUE
	)
	public SseEmitter runSession(@RequestBody ArenaQuestionRequest request) {
		SseEmitter emitter = new SseEmitter();
		CompletableFuture.runAsync(() -> {
			try {
				runArenaSessionUseCase.run(request.question(), event -> sendUnchecked(emitter, event));
				emitter.complete();
			}
			catch (RuntimeException ex) {
				try {
					send(emitter, SessionEvent.error(new ErrorEvent("Unable to run arena session.")));
					emitter.complete();
				}
				catch (IOException sendError) {
					emitter.completeWithError(sendError);
				}
			}
		});
		return emitter;
	}

	private static void sendUnchecked(SseEmitter emitter, SessionEvent event) {
		try {
			send(emitter, event);
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	private static void send(SseEmitter emitter, SessionEvent event) throws IOException {
		emitter.send(SseEmitter.event().name(event.type().name()).data(event.payload()));
	}
}
