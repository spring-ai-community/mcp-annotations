/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.context;

import java.util.Map;

import io.modelcontextprotocol.spec.McpSchema.ElicitResult.Action;

/**
 * A record representing the result of a structured elicit action.
 *
 * @param <T> the type of the structured content
 * @author Christian Tzolov
 */
public record StructuredElicitResult<T>(Action action, T structuredContent, Map<String, Object> meta) {

}
