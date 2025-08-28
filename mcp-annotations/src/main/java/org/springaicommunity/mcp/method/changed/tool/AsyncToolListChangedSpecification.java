/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.changed.tool;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import io.modelcontextprotocol.spec.McpSchema;
import reactor.core.publisher.Mono;

public record AsyncToolListChangedSpecification(String[] clients,
		Function<List<McpSchema.Tool>, Mono<Void>> toolListChangeHandler) {

	public AsyncToolListChangedSpecification {
		Objects.requireNonNull(clients, "clients must not be null");
		if (clients.length == 0 || Arrays.stream(clients).map(String::trim).anyMatch(String::isEmpty)) {
			throw new IllegalArgumentException("clients must not be empty");
		}
		Objects.requireNonNull(toolListChangeHandler, "toolListChangeHandler must not be null");
	}

}