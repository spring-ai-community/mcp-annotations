/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.sampling;

import org.springaicommunity.mcp.annotation.McpSampling;

import io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import reactor.core.publisher.Mono;

/**
 * Example class with methods annotated with {@link McpSampling} for testing the
 * asynchronous sampling method callback.
 *
 * @author Christian Tzolov
 */
public class AsyncMcpSamplingMethodCallbackExample {

	/**
	 * Example method that handles a sampling request and returns a Mono result.
	 * @param request The sampling request
	 * @return The sampling result as a Mono
	 */
	@McpSampling(clientId = "test-client")
	public Mono<CreateMessageResult> handleAsyncSamplingRequest(CreateMessageRequest request) {
		// Process the request asynchronously and return a result
		return Mono.just(CreateMessageResult.builder()
			.role(Role.ASSISTANT)
			.content(new TextContent("This is an async response to the sampling request"))
			.model("test-model")
			.build());
	}

	/**
	 * Example method that returns a direct result (not wrapped in Mono).
	 * @param request The sampling request
	 * @return The sampling result directly
	 */
	@McpSampling(clientId = "test-client")
	public CreateMessageResult handleDirectSamplingRequest(CreateMessageRequest request) {
		// Process the request and return a direct result
		return CreateMessageResult.builder()
			.role(Role.ASSISTANT)
			.content(new TextContent("This is a direct response to the sampling request"))
			.model("test-model")
			.build();
	}

	/**
	 * Example method with an invalid return type.
	 * @param request The sampling request
	 * @return A Mono with an invalid type
	 */
	@McpSampling(clientId = "test-client")
	public Mono<String> invalidMonoReturnType(CreateMessageRequest request) {
		return Mono.just("This method has an invalid return type");
	}

	/**
	 * Example method with an invalid parameter type.
	 * @param invalidParam An invalid parameter type
	 * @return The sampling result as a Mono
	 */
	@McpSampling(clientId = "test-client")
	public Mono<CreateMessageResult> invalidParameterType(String invalidParam) {
		return Mono.just(CreateMessageResult.builder()
			.role(Role.ASSISTANT)
			.content(new TextContent("This method has an invalid parameter type"))
			.model("test-model")
			.build());
	}

	/**
	 * Example method with no parameters.
	 * @return The sampling result as a Mono
	 */
	@McpSampling(clientId = "test-client")
	public Mono<CreateMessageResult> noParameters() {
		return Mono.just(CreateMessageResult.builder()
			.role(Role.ASSISTANT)
			.content(new TextContent("This method has no parameters"))
			.model("test-model")
			.build());
	}

	/**
	 * Example method with too many parameters.
	 * @param request The sampling request
	 * @param extraParam An extra parameter
	 * @return The sampling result as a Mono
	 */
	@McpSampling(clientId = "test-client")
	public Mono<CreateMessageResult> tooManyParameters(CreateMessageRequest request, String extraParam) {
		return Mono.just(CreateMessageResult.builder()
			.role(Role.ASSISTANT)
			.content(new TextContent("This method has too many parameters"))
			.model("test-model")
			.build());
	}

}
