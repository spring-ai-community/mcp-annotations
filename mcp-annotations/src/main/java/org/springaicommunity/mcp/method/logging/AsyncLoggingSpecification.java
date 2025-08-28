/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.logging;

import java.util.Objects;
import java.util.function.Function;

import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import reactor.core.publisher.Mono;

public record AsyncLoggingSpecification(String clientId,
		Function<LoggingMessageNotification, Mono<Void>> loggingHandler) {

	public AsyncLoggingSpecification {
		Objects.requireNonNull(clientId, "clientId must not be null");
		if (clientId.trim().isEmpty()) {
			throw new IllegalArgumentException("clientId must not be empty");
		}
		Objects.requireNonNull(loggingHandler, "loggingHandler must not be null");
	}

}