/*
* Copyright 2025 - 2025 the original author or authors.
*/
package org.springaicommunity.mcp.annotation;

import io.modelcontextprotocol.spec.McpSchema;

/**
 * @author Christian Tzolov
 */
public class ResourceAdaptor {

	private ResourceAdaptor() {
	}

	public static McpSchema.Resource asResource(McpResource mcpResource) {
		String name = mcpResource.name();
		if (name == null || name.isEmpty()) {
			name = "resource"; // Default name when not specified
		}
		return McpSchema.Resource.builder()
			.uri(mcpResource.uri())
			.name(name)
			.description(mcpResource.description())
			.mimeType(mcpResource.mimeType())
			.build();
	}

	public static McpSchema.ResourceTemplate asResourceTemplate(McpResource mcpResource) {
		String name = mcpResource.name();
		if (name == null || name.isEmpty()) {
			name = "resource"; // Default name when not specified
		}
		return new McpSchema.ResourceTemplate(mcpResource.uri(), name, mcpResource.description(),
				mcpResource.mimeType(), null);
	}

}
