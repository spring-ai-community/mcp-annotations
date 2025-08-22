/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.progress;

import java.util.function.Function;

import io.modelcontextprotocol.spec.McpSchema.ProgressNotification;
import reactor.core.publisher.Mono;

/**
 * Specification for asynchronous progress handlers.
 *
 * @param clientId The client ID for the progress handler
 * @param progressHandler The function that handles progress notifications asynchronously
 * @author Christian Tzolov
 */
public record AsyncProgressSpecification(String clientId, Function<ProgressNotification, Mono<Void>> progressHandler) {
}
