/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.logging;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;

public record SyncLoggingSpecification(String[] clients, Consumer<LoggingMessageNotification> loggingHandler) {

	public SyncLoggingSpecification {
		Objects.requireNonNull(clients, "clients must not be null");
		if (clients.length == 0 || Arrays.stream(clients).anyMatch(String::isEmpty)) {
			throw new IllegalArgumentException("clients must not be empty");
		}
		Objects.requireNonNull(loggingHandler, "loggingHandler must not be null");
	}
}
