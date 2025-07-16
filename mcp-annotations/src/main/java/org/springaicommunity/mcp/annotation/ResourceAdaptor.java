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
		return new McpSchema.Resource(mcpResource.uri(), mcpResource.name(), mcpResource.description(),
				mcpResource.mimeType(), null);
	}

	public static McpSchema.ResourceTemplate asResourceTemplate(McpResource mcpResource) {
		return new McpSchema.ResourceTemplate(mcpResource.uri(), mcpResource.name(), mcpResource.description(),
				mcpResource.mimeType(), null);
	}

}
