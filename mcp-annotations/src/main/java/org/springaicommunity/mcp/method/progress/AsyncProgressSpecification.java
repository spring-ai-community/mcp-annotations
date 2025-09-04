/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.progress;

import java.util.Arrays;
import java.util.Objects;
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
public record AsyncProgressSpecification(String[] clients, Function<ProgressNotification, Mono<Void>> progressHandler) {
	public AsyncProgressSpecification {
		Objects.requireNonNull(clients, "clients must not be null");
		if (clients.length == 0 || Arrays.stream(clients).map(String::trim).anyMatch(String::isEmpty)) {
			throw new IllegalArgumentException("At least one client Id must be specified");
		}
		Objects.requireNonNull(progressHandler, "progressHandler must not be null");
	}

}
