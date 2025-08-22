package org.springaicommunity.mcp.method.sampling;

import java.util.function.Function;

import io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;

public record SyncSamplingSpecification(String clientId,
		Function<CreateMessageRequest, CreateMessageResult> samplingHandler) {

}