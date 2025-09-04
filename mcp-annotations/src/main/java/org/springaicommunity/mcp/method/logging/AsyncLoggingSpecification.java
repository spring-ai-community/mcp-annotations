/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.logging;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import reactor.core.publisher.Mono;

public record AsyncLoggingSpecification(String[] clients,
		Function<LoggingMessageNotification, Mono<Void>> loggingHandler) {

	public AsyncLoggingSpecification {
		Objects.requireNonNull(clients, "clients must not be null");
		if (clients.length == 0 || Arrays.stream(clients).anyMatch(String::isEmpty)) {
			throw new IllegalArgumentException("clients must not be empty");
		}
		Objects.requireNonNull(loggingHandler, "loggingHandler must not be null");
	}

}