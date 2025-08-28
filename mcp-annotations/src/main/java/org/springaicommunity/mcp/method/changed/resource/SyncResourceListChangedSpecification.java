/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.changed.resource;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import io.modelcontextprotocol.spec.McpSchema;

public record SyncResourceListChangedSpecification(String[] clients,
		Consumer<List<McpSchema.Resource>> resourceListChangeHandler) {

	public SyncResourceListChangedSpecification {
		Objects.requireNonNull(clients, "clients must not be null");
		if (clients.length == 0 || Arrays.stream(clients).map(String::trim).anyMatch(String::isEmpty)) {
			throw new IllegalArgumentException("clients must not be empty");
		}
		Objects.requireNonNull(resourceListChangeHandler, "resourceListChangeHandler must not be null");
	}

}
