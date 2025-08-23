/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.annotation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.modelcontextprotocol.spec.McpSchema;

/**
 * Special object used to represent the {@link McpSchema.Request#meta()},
 * {@link McpSchema.Notification#meta()} and {@link McpSchema.Result#meta()} values as
 * method argument in all client and server MCP request and notification handlers.
 *
 * @author Christian Tzolov
 */
public record McpMeta(Map<String, Object> meta) {

	public McpMeta {
		// Ensure idempotent initialization by creating an immutable copy
		meta = meta == null ? Collections.emptyMap() : Collections.unmodifiableMap(new HashMap<>(meta));
	}

	public Object get(String key) {
		return meta.get(key);
	}
}
