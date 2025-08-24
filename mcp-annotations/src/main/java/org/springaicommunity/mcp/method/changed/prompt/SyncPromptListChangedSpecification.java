/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.changed.prompt;

import java.util.List;
import java.util.function.Consumer;

import io.modelcontextprotocol.spec.McpSchema;

public record SyncPromptListChangedSpecification(String clientId,
		Consumer<List<McpSchema.Prompt>> promptListChangeHandler) {
}
