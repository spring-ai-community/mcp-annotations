/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.elicitation;

import java.util.function.Function;

import io.modelcontextprotocol.spec.McpSchema.ElicitRequest;
import io.modelcontextprotocol.spec.McpSchema.ElicitResult;

public record SyncElicitationSpecification(String clientId, Function<ElicitRequest, ElicitResult> elicitationHandler) {

}
