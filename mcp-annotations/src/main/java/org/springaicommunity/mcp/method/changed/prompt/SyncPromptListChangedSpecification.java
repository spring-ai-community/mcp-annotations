/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.changed.prompt;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import io.modelcontextprotocol.spec.McpSchema;

public record SyncPromptListChangedSpecification(String[] clients,
		Consumer<List<McpSchema.Prompt>> promptListChangeHandler) {

	public SyncPromptListChangedSpecification {
		Objects.requireNonNull(clients, "clients must not be null");
		if (clients.length == 0) {
			throw new IllegalArgumentException("At least one client Id must be specified");
		}
		Objects.requireNonNull(promptListChangeHandler, "promptListChangeHandler must not be null");
	}

}
