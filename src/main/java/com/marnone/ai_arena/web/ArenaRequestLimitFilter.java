package com.marnone.ai_arena.web;

import java.io.IOException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.marnone.ai_arena.config.ArenaProperties;

/**
 * Applies coarse HTTP resource limits before arena sessions can allocate AI work.
 */
@Component
public class ArenaRequestLimitFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(ArenaRequestLimitFilter.class);
	private static final String ARENA_SESSION_PATH = "/api/arena/sessions";

	private final ArenaProperties arenaProperties;
	private final Clock clock;
	private final ConcurrentMap<String, RateWindow> windows = new ConcurrentHashMap<>();

	@Autowired
	public ArenaRequestLimitFilter(ArenaProperties arenaProperties) {
		this(arenaProperties, Clock.systemUTC());
	}

	ArenaRequestLimitFilter(ArenaProperties arenaProperties, Clock clock) {
		this.arenaProperties = Objects.requireNonNull(arenaProperties, "arenaProperties must not be null");
		this.clock = Objects.requireNonNull(clock, "clock must not be null");
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		return !("POST".equalsIgnoreCase(request.getMethod()) && ARENA_SESSION_PATH.equals(request.getRequestURI()));
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		if (payloadTooLarge(request)) {
			log.warn(
				"Arena request rejected because payload is too large remoteAddress={} contentLength={} maxPayloadBytes={}",
				request.getRemoteAddr(),
				request.getContentLengthLong(),
				arenaProperties.getHttp().getMaxPayloadBytes()
			);
			writeError(response, HttpStatus.CONTENT_TOO_LARGE, "PAYLOAD_TOO_LARGE", "Request payload is too large.");
			return;
		}
		if (rateLimited(request)) {
			log.warn("Arena request rejected by rate limit remoteAddress={}", request.getRemoteAddr());
			writeError(response, HttpStatus.TOO_MANY_REQUESTS, "RATE_LIMITED", "Too many requests. Please try again later.");
			return;
		}
		filterChain.doFilter(request, response);
	}

	private boolean payloadTooLarge(HttpServletRequest request) {
		long contentLength = request.getContentLengthLong();
		return contentLength > arenaProperties.getHttp().getMaxPayloadBytes();
	}

	private boolean rateLimited(HttpServletRequest request) {
		ArenaProperties.Http http = arenaProperties.getHttp();
		Instant now = clock.instant();
		Duration windowLength = http.getRateLimitWindow();
		RateWindow window = windows.compute(request.getRemoteAddr(), (remoteAddress, current) -> {
			if (current == null || !current.startedAt().plus(windowLength).isAfter(now)) {
				return new RateWindow(now, 1);
			}
			return new RateWindow(current.startedAt(), current.requests() + 1);
		});
		return window.requests() > http.getRateLimitMaxRequests();
	}

	private static void writeError(HttpServletResponse response, HttpStatus status, String code, String message)
		throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.getWriter().write("{\"code\":\"" + code + "\",\"message\":\"" + message + "\"}");
	}

	private record RateWindow(Instant startedAt, int requests) {
	}
}
