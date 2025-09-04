/*
* Copyright 2025 - 2025 the original author or authors.
*/
package org.springaicommunity.mcp.adapter;

import io.modelcontextprotocol.spec.McpSchema;
import org.springaicommunity.mcp.annotation.McpResource;

/**
 * @author Christian Tzolov
 */
public class ResourceAdapter {

	private ResourceAdapter() {
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
