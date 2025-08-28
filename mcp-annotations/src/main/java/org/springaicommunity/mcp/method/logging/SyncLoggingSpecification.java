/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.logging;

import java.util.Objects;
import java.util.function.Consumer;

import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;

public record SyncLoggingSpecification(String clientId, Consumer<LoggingMessageNotification> loggingHandler) {

	public SyncLoggingSpecification {
		Objects.requireNonNull(clientId, "clientId must not be null");
		if (clientId.trim().isEmpty()) {
			throw new IllegalArgumentException("clientId must not be empty");
		}
		Objects.requireNonNull(loggingHandler, "loggingHandler must not be null");
	}
}
