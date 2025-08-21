package org.springaicommunity.mcp.method.sampling;

import java.util.function.Function;

import io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import reactor.core.publisher.Mono;

public record AsyncSamplingSpecification(String clientId,
		Function<CreateMessageRequest, Mono<CreateMessageResult>> samplingHandler) {

}