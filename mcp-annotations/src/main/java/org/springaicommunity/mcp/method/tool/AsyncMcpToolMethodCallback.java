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
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.reactivestreams.Publisher;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.method.tool.utils.JsonParser;

import com.fasterxml.jackson.core.type.TypeReference;

import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Class for creating Function callbacks around tool methods.
 *
 * This class provides a way to convert methods annotated with {@link McpTool} into
 * callback functions that can be used to handle tool requests.
 *
 * @author Christian Tzolov
 */
public final class AsyncMcpToolMethodCallback
		implements BiFunction<McpAsyncServerExchange, CallToolRequest, Mono<CallToolResult>> {

	private static final TypeReference<Map<String, Object>> MAP_TYPE_REFERENCE = new TypeReference<Map<String, Object>>() {
		// No implementation needed
	};

	private final Method toolMethod;

	private final Object toolObject;

	private ReturnMode returnMode;

	public AsyncMcpToolMethodCallback(ReturnMode returnMode, Method toolMethod, Object toolObject) {
		this.toolMethod = toolMethod;
		this.toolObject = toolObject;
		this.returnMode = returnMode;
	}

	/**
	 * Apply the callback to the given request.
	 * <p>
	 * This method builds the arguments for the method call, invokes the method, and
	 * returns the result.
	 * @param request The tool call request, must not be null
	 * @return The result of the method invocation
	 */
	@Override
	public Mono<CallToolResult> apply(McpAsyncServerExchange exchange, CallToolRequest request) {

		if (request == null) {
			return Mono.error(new IllegalArgumentException("Request must not be null"));
		}

		return Mono.defer(() -> {
			try {
				// Build arguments for the method call
				Object[] args = this.buildMethodArguments(exchange, request.arguments());

				// Invoke the method
				Object result = this.callMethod(args);

				// Handle reactive types - method return types should always be reactive
				return this.convertToCallToolResult(result);

			}
			catch (Exception e) {
				return Mono.just(CallToolResult.builder()
					.isError(true)
					.addTextContent("Error invoking method: %s".formatted(e.getMessage()))
					.build());
			}
		});
	}

	/**
	 * Convert reactive types to Mono<CallToolResult>
	 */
	private Mono<CallToolResult> convertToCallToolResult(Object result) {
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
	 */
	private CallToolResult mapValueToCallToolResult(Object value) {
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

	private Object callMethod(Object[] methodArguments) {

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
			// throw new ToolExecutionException(this.toolDefinition, ex.getCause());
		}
		return result;
	}

	private Object[] buildMethodArguments(McpAsyncServerExchange exchange, Map<String, Object> toolInputArguments) {
		return Stream.of(this.toolMethod.getParameters()).map(parameter -> {
			Object rawArgument = toolInputArguments.get(parameter.getName());
			if (isExchangeType(parameter.getType())) {
				return exchange;
			}
			return buildTypedArgument(rawArgument, parameter.getParameterizedType());
		}).toArray();
	}

	private Object buildTypedArgument(Object value, Type type) {
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

	protected boolean isExchangeType(Class<?> paramType) {
		return McpAsyncServerExchange.class.isAssignableFrom(paramType);
	}

}
