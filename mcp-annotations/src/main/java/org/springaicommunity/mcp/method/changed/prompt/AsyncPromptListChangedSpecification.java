/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.changed.prompt;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import io.modelcontextprotocol.spec.McpSchema;
import reactor.core.publisher.Mono;

public record AsyncPromptListChangedSpecification(String[] clients,
		Function<List<McpSchema.Prompt>, Mono<Void>> promptListChangeHandler) {

	public AsyncPromptListChangedSpecification {
		Objects.requireNonNull(clients, "clients must not be null");
		if (clients.length == 0) {
			throw new IllegalArgumentException("At least one client Id must be specified");
		}
		Objects.requireNonNull(promptListChangeHandler, "promptListChangeHandler must not be null");
	}

}
