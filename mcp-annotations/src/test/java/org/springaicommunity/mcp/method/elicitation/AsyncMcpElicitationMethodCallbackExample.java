/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.elicitation;

import java.util.Map;

import org.springaicommunity.mcp.annotation.McpElicitation;

import io.modelcontextprotocol.spec.McpSchema.ElicitRequest;
import io.modelcontextprotocol.spec.McpSchema.ElicitResult;
import reactor.core.publisher.Mono;

/**
 * Example class demonstrating asynchronous elicitation method usage.
 *
 * @author Christian Tzolov
 */
public class AsyncMcpElicitationMethodCallbackExample {

	@McpElicitation(clientId = "my-client-id")
	public Mono<ElicitResult> handleElicitationRequest(ElicitRequest request) {
		// Example implementation that accepts the request and returns some content
		return Mono.just(new ElicitResult(ElicitResult.Action.ACCEPT, Map.of("userInput", "Example async user input",
				"confirmed", true, "timestamp", System.currentTimeMillis())));
	}

	@McpElicitation(clientId = "my-client-id")
	public Mono<ElicitResult> handleDeclineElicitationRequest(ElicitRequest request) {
		// Example implementation that declines the request after a delay
		return Mono.delay(java.time.Duration.ofMillis(100))
			.then(Mono.just(new ElicitResult(ElicitResult.Action.DECLINE, null)));
	}

	@McpElicitation(clientId = "my-client-id")
	public ElicitResult handleSyncElicitationRequest(ElicitRequest request) {
		// Example implementation that returns synchronously but will be wrapped in Mono
		return new ElicitResult(ElicitResult.Action.ACCEPT, Map.of("syncResponse",
				"This was returned synchronously but wrapped in Mono", "requestMessage", request.message()));
	}

	@McpElicitation(clientId = "my-client-id")
	public Mono<ElicitResult> handleCancelElicitationRequest(ElicitRequest request) {
		// Example implementation that cancels the request
		return Mono.just(new ElicitResult(ElicitResult.Action.CANCEL, null));
	}

	// Test methods for invalid scenarios

	@McpElicitation(clientId = "my-client-id")
	public String invalidReturnType(ElicitRequest request) {
		return "Invalid return type";
	}

	@McpElicitation(clientId = "my-client-id")
	public Mono<String> invalidMonoReturnType(ElicitRequest request) {
		return Mono.just("Invalid Mono return type");
	}

	@McpElicitation(clientId = "my-client-id")
	public Mono<ElicitResult> invalidParameterType(String request) {
		return Mono.just(new ElicitResult(ElicitResult.Action.ACCEPT, Map.of("test", "value")));
	}

	@McpElicitation(clientId = "my-client-id")
	public Mono<ElicitResult> noParameters() {
		return Mono.just(new ElicitResult(ElicitResult.Action.ACCEPT, Map.of("test", "value")));
	}

	@McpElicitation(clientId = "my-client-id")
	public Mono<ElicitResult> tooManyParameters(ElicitRequest request, String extra) {
		return Mono.just(new ElicitResult(ElicitResult.Action.ACCEPT, Map.of("test", "value")));
	}

}
