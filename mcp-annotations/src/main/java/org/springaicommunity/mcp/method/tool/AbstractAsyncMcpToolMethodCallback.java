/*
 * Copyright 2025-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springaicommunity.mcp.method.tool;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.stream.Stream;

import org.reactivestreams.Publisher;
import org.springaicommunity.mcp.annotation.McpMeta;
import org.springaicommunity.mcp.annotation.McpProgressToken;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.method.tool.utils.JsonParser;

import com.fasterxml.jackson.core.type.TypeReference;

import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Abstract base class for creating Function callbacks around async tool methods.
 *
 * This class provides common functionality for converting methods annotated with
 * {@link McpTool} into callback functions that can be used to handle tool requests
 * asynchronously.
 *
 * @param <T> The type of the context parameter (e.g., McpAsyncServerExchange or
 * McpTransportContext)
 * @author Christian Tzolov
 */
public abstract class AbstractAsyncMcpToolMethodCallback<T> {

	private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE = new TypeReference<Map<String, Object>>() {
		// No implementation needed
	};

	protected final Method toolMethod;

	protected final Object toolObject;

	protected final ReturnMode returnMode;

	protected AbstractAsyncMcpToolMethodCallback(ReturnMode returnMode, Method toolMethod, Object toolObject) {
		this.toolMethod = toolMethod;
		this.toolObject = toolObject;
		this.returnMode = returnMode;
	}

	/**
	 * Invokes the tool method with the provided arguments.
	 * @param methodArguments The arguments to pass to the method
	 * @return The result of the method invocation
	 * @throws IllegalStateException if the method cannot be accessed
	 * @throws RuntimeException if there's an error invoking the method
	 */
	protected Object callMethod(Object[] methodArguments) {
		this.toolMethod.setAccessible(true);

		Object result;
		try {
			result = this.toolMethod.invoke(this.toolObject, methodArguments);
		}
		catch (IllegalAccessException ex) {
			throw new IllegalStateException("Could not access method: " + ex.getMessage(), ex);
		}
		catch (InvocationTargetException ex) {
			throw new RuntimeException("Error invoking method: " + this.toolMethod.getName(), ex);
		}
		return result;
	}

	/**
	 * Builds the method arguments from the context, tool input arguments, and optionally
	 * the full request.
	 * @param exchangeOrContext The exchange or context object (e.g.,
	 * McpAsyncServerExchange or McpTransportContext)
	 * @param toolInputArguments The input arguments from the tool request
	 * @param request The full CallToolRequest (optional, can be null)
	 * @return An array of method arguments
	 */
	protected Object[] buildMethodArguments(T exchangeOrContext, Map<String, Object> toolInputArguments,
			CallToolRequest request) {
		return Stream.of(this.toolMethod.getParameters()).map(parameter -> {
			// Check if parameter is annotated with @McpProgressToken
			if (parameter.isAnnotationPresent(McpProgressToken.class)) {
				// Return the progress token from the request
				return request != null ? request.progressToken() : null;
			}

			// Check if parameter is McpMeta type
			if (McpMeta.class.isAssignableFrom(parameter.getType())) {
				// Return the meta from the request wrapped in McpMeta
				return request != null ? new McpMeta(request.meta()) : new McpMeta(null);
			}

			// Check if parameter is CallToolRequest type
			if (CallToolRequest.class.isAssignableFrom(parameter.getType())) {
				return request;
			}

			if (isExchangeOrContextType(parameter.getType())) {
				return exchangeOrContext;
			}

			Object rawArgument = toolInputArguments.get(parameter.getName());
			return buildTypedArgument(rawArgument, parameter.getParameterizedType());
		}).toArray();
	}

	/**
	 * Builds a typed argument from a raw value and type information.
	 * @param value The raw value
	 * @param type The target type
	 * @return The typed argument
	 */
	protected Object buildTypedArgument(Object value, Type type) {
		if (value == null) {
			return null;
		}

		if (type instanceof Class<?>) {
			return JsonParser.toTypedObject(value, (Class<?>) type);
		}

		// For generic types, use the fromJson method that accepts Type
		String json = JsonParser.toJson(value);
		return JsonParser.fromJson(json, type);
	}

	/**
	 * Convert reactive types to Mono<CallToolResult>
	 * @param result The result from the method invocation
	 * @return A Mono<CallToolResult> representing the processed result
	 */
	protected Mono<CallToolResult> convertToCallToolResult(Object result) {
		// Handle Mono types
		if (result instanceof Mono) {

			Mono<?> monoResult = (Mono<?>) result;

			// Check if the Mono contains CallToolResult
			if (ReactiveUtils.isReactiveReturnTypeOfCallToolResult(this.toolMethod)) {
				return (Mono<CallToolResult>) monoResult;
			}

			// Handle Mono<Void> for VOID return type
			if (ReactiveUtils.isReactiveReturnTypeOfVoid(this.toolMethod)) {
				return monoResult
					.then(Mono.just(CallToolResult.builder().addTextContent(JsonParser.toJson("Done")).build()));
			}

			// Handle other Mono types - map the emitted value to CallToolResult
			return monoResult.map(this::mapValueToCallToolResult)
				.onErrorResume(e -> Mono.just(CallToolResult.builder()
					.isError(true)
					.addTextContent("Error invoking method: %s".formatted(e.getMessage()))
					.build()));
		}

		// Handle Flux by taking the first element
		if (result instanceof Flux) {
			Flux<?> fluxResult = (Flux<?>) result;

			// Check if the Flux contains CallToolResult
			if (ReactiveUtils.isReactiveReturnTypeOfCallToolResult(this.toolMethod)) {
				return ((Flux<CallToolResult>) fluxResult).next();
			}

			// Handle Mono<Void> for VOID return type
			if (ReactiveUtils.isReactiveReturnTypeOfVoid(this.toolMethod)) {
				return fluxResult
					.then(Mono.just(CallToolResult.builder().addTextContent(JsonParser.toJson("Done")).build()));
			}

			// Handle other Flux types by taking the first element and mapping
			return fluxResult.next()
				.map(this::mapValueToCallToolResult)
				.onErrorResume(e -> Mono.just(CallToolResult.builder()
					.isError(true)
					.addTextContent("Error invoking method: %s".formatted(e.getMessage()))
					.build()));
		}

		// Handle other Publisher types
		if (result instanceof Publisher) {
			Publisher<?> publisherResult = (Publisher<?>) result;
			Mono<?> monoFromPublisher = Mono.from(publisherResult);

			// Check if the Publisher contains CallToolResult
			if (ReactiveUtils.isReactiveReturnTypeOfCallToolResult(this.toolMethod)) {
				return (Mono<CallToolResult>) monoFromPublisher;
			}

			// Handle Mono<Void> for VOID return type
			if (ReactiveUtils.isReactiveReturnTypeOfVoid(this.toolMethod)) {
				return monoFromPublisher
					.then(Mono.just(CallToolResult.builder().addTextContent(JsonParser.toJson("Done")).build()));
			}

			// Handle other Publisher types by mapping the emitted value
			return monoFromPublisher.map(this::mapValueToCallToolResult)
				.onErrorResume(e -> Mono.just(CallToolResult.builder()
					.isError(true)
					.addTextContent("Error invoking method: %s".formatted(e.getMessage()))
					.build()));
		}

		// This should not happen in async context, but handle as fallback
		throw new IllegalStateException(
				"Expected reactive return type but got: " + (result != null ? result.getClass().getName() : "null"));
	}

	/**
	 * Map individual values to CallToolResult
	 * @param value The value to map
	 * @return A CallToolResult representing the mapped value
	 */
	protected CallToolResult mapValueToCallToolResult(Object value) {
		if (value instanceof CallToolResult) {
			return (CallToolResult) value;
		}

		if (returnMode == ReturnMode.VOID) {
			return CallToolResult.builder().addTextContent(JsonParser.toJson("Done")).build();
		}
		else if (this.returnMode == ReturnMode.STRUCTURED) {
			String jsonOutput = JsonParser.toJson(value);
			Map<String, Object> structuredOutput = JsonParser.fromJson(jsonOutput, MAP_TYPE_REFERENCE);
			return CallToolResult.builder().structuredContent(structuredOutput).build();
		}

		// Default to text output
		return CallToolResult.builder().addTextContent(value != null ? value.toString() : "null").build();
	}

	/**
	 * Creates an error result for exceptions that occur during method invocation.
	 * @param e The exception that occurred
	 * @return A Mono<CallToolResult> representing the error
	 */
	protected Mono<CallToolResult> createErrorResult(Exception e) {
		return Mono.just(CallToolResult.builder()
			.isError(true)
			.addTextContent("Error invoking method: %s".formatted(e.getMessage()))
			.build());
	}

	/**
	 * Validates that the request is not null.
	 * @param request The request to validate
	 * @return A Mono error if the request is null, otherwise Mono.empty()
	 */
	protected Mono<Void> validateRequest(CallToolRequest request) {
		if (request == null) {
			return Mono.error(new IllegalArgumentException("Request must not be null"));
		}
		return Mono.empty();
	}

	/**
	 * Determines if the given parameter type is an exchange or context type that should
	 * be injected. Subclasses must implement this method to specify which types are
	 * considered exchange or context types.
	 * @param paramType The parameter type to check
	 * @return true if the parameter type is an exchange or context type, false otherwise
	 */
	protected abstract boolean isExchangeOrContextType(Class<?> paramType);

}
