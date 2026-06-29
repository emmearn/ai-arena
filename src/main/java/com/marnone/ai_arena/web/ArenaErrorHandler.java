package com.marnone.ai_arena.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Maps web-layer failures to stable public error responses without exposing internals.
 */
@RestControllerAdvice
public class ArenaErrorHandler {

	private static final Logger log = LoggerFactory.getLogger(ArenaErrorHandler.class);

	@ExceptionHandler(HttpMessageNotReadableException.class)
	ResponseEntity<ArenaErrorResponse> invalidRequest(HttpMessageNotReadableException ex) {
		log.warn("Arena request rejected because JSON body is not readable exceptionType={}", ex.getClass().getName());
		return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "Request body must be valid JSON.");
	}

	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	ResponseEntity<ArenaErrorResponse> unsupportedMediaType(HttpMediaTypeNotSupportedException ex) {
		log.warn("Arena request rejected because media type is unsupported exceptionType={}", ex.getClass().getName());
		return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "UNSUPPORTED_MEDIA_TYPE", "Request content type is not supported.");
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	ResponseEntity<ArenaErrorResponse> methodNotAllowed(HttpRequestMethodNotSupportedException ex) {
		log.warn("Arena request rejected because method is unsupported exceptionType={}", ex.getClass().getName());
		return error(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", "Request method is not supported.");
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ArenaErrorResponse> internalError(Exception ex) {
		log.error("Arena web request failed exceptionType={}", ex.getClass().getName());
		return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unable to process request.");
	}

	private static ResponseEntity<ArenaErrorResponse> error(HttpStatus status, String code, String message) {
		return ResponseEntity.status(status)
			.contentType(MediaType.APPLICATION_JSON)
			.body(new ArenaErrorResponse(code, message));
	}
}
