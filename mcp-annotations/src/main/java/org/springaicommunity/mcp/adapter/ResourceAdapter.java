/*
* Copyright 2025 - 2025 the original author or authors.
*/
package org.springaicommunity.mcp.adapter;

import java.util.List;

import io.modelcontextprotocol.spec.McpSchema;
import org.springaicommunity.mcp.annotation.McpResource;

/**
 * @author Christian Tzolov
 */
public class ResourceAdapter {

	private ResourceAdapter() {
	}

	public static McpSchema.Resource asResource(McpResource mcpResourceAnnotation) {
		String name = mcpResourceAnnotation.name();
		if (name == null || name.isEmpty()) {
			name = "resource"; // Default name when not specified
		}

		var resourceBuilder = McpSchema.Resource.builder()
			.uri(mcpResourceAnnotation.uri())
			.name(name)
			.title(mcpResourceAnnotation.title())
			.description(mcpResourceAnnotation.description())
			.mimeType(mcpResourceAnnotation.mimeType());

		// Only set annotations if not default value is provided
		// This is a workaround since Java annotations do not support null default values
		// and we want to avoid setting empty annotations.
		// The default annotations value is ignored.
		// The user must explicitly set the annotations to get them included.
		var annotations = mcpResourceAnnotation.annotations();
		if (annotations != null && annotations.lastModified() != null && !annotations.lastModified().isEmpty()) {
			resourceBuilder
				.annotations(new McpSchema.Annotations(List.of(annotations.audience()), annotations.priority()));
		}

		return resourceBuilder.build();
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
