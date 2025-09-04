/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.changed.tool;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import io.modelcontextprotocol.spec.McpSchema;

public record SyncToolListChangedSpecification(String[] clients, Consumer<List<McpSchema.Tool>> toolListChangeHandler) {

	public SyncToolListChangedSpecification {
		Objects.requireNonNull(clients, "clients must not be null");
		if (clients.length == 0 || Arrays.stream(clients).map(String::trim).anyMatch(String::isEmpty)) {
			throw new IllegalArgumentException("clients must not be empty");
		}
		Objects.requireNonNull(toolListChangeHandler, "toolListChangeHandler must not be null");
	}

}