/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.changed.prompt;

import java.util.List;
import java.util.function.Function;

import io.modelcontextprotocol.spec.McpSchema;
import reactor.core.publisher.Mono;

public record AsyncPromptListChangedSpecification(String clientId,
		Function<List<McpSchema.Prompt>, Mono<Void>> promptListChangeHandler) {
}
