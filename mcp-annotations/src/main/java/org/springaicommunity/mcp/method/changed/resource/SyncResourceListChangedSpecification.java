/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.changed.resource;

import java.util.List;
import java.util.function.Consumer;

import io.modelcontextprotocol.spec.McpSchema;

public record SyncResourceListChangedSpecification(String clientId,
		Consumer<List<McpSchema.Resource>> resourceListChangeHandler) {
}
