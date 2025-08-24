/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.changed.tool;

import java.util.List;
import java.util.function.Function;

import io.modelcontextprotocol.spec.McpSchema;
import reactor.core.publisher.Mono;

public record AsyncToolListChangedSpecification(String clientId,
		Function<List<McpSchema.Tool>, Mono<Void>> toolListChangeHandler) {
}