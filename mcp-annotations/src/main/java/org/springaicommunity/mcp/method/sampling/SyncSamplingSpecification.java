package org.springaicommunity.mcp.method.sampling;

import java.util.Objects;
import java.util.function.Function;

import io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;

public record SyncSamplingSpecification(String clientId,
		Function<CreateMessageRequest, CreateMessageResult> samplingHandler) {

	public SyncSamplingSpecification {
		Objects.requireNonNull(clientId, "clientId must not be null");
		if (clientId.trim().isEmpty()) {
			throw new IllegalArgumentException("clientId must not be empty");
		}
		Objects.requireNonNull(samplingHandler, "samplingHandler must not be null");
	}

}