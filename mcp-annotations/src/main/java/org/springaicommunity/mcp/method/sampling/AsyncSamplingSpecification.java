package org.springaicommunity.mcp.method.sampling;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Function;

import io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import reactor.core.publisher.Mono;

public record AsyncSamplingSpecification(String[] clients,
		Function<CreateMessageRequest, Mono<CreateMessageResult>> samplingHandler) {

	public AsyncSamplingSpecification {
		Objects.requireNonNull(clients, "clients must not be null");
		if (clients.length == 0 || Arrays.stream(clients).map(String::trim).anyMatch(String::isEmpty)) {
			throw new IllegalArgumentException("clients must not be empty");
		}
		Objects.requireNonNull(samplingHandler, "samplingHandler must not be null");
	}

}