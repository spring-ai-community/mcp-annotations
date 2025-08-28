/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.elicitation;

import java.util.Objects;
import java.util.function.Function;

import io.modelcontextprotocol.spec.McpSchema.ElicitRequest;
import io.modelcontextprotocol.spec.McpSchema.ElicitResult;

public record SyncElicitationSpecification(String clientId, Function<ElicitRequest, ElicitResult> elicitationHandler) {
	public SyncElicitationSpecification {
		Objects.requireNonNull(clientId, "clientId must not be null");
		if (clientId.trim().isEmpty()) {
			throw new IllegalArgumentException("clientId must not be empty");
		}
		Objects.requireNonNull(elicitationHandler, "elicitationHandler must not be null");
	}

}
