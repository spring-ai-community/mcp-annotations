/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.logging;

import java.util.function.Function;

import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import reactor.core.publisher.Mono;

public record AsyncLoggingSpecification(String clientId,
		Function<LoggingMessageNotification, Mono<Void>> loggingHandler) {
}