package org.springaicommunity.mcp.context;

import java.util.Map;

import io.modelcontextprotocol.spec.McpSchema.ElicitResult.Action;

public record StructuredElicitResult<T>(Action action, T structuredContent, Map<String, Object> meta) {

}
