/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.logging;

import java.util.function.Consumer;

import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;

public record SyncLoggingSpecification(String clientId, Consumer<LoggingMessageNotification> loggingHandler) {
}