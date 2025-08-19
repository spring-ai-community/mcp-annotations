/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.prompt;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.BiFunction;

import org.springaicommunity.mcp.annotation.McpPrompt;

import io.modelcontextprotocol.server.McpTransportContext;
import io.modelcontextprotocol.spec.McpSchema.GetPromptRequest;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import reactor.core.publisher.Mono;

/**
 * Class for creating BiFunction callbacks around prompt methods with asynchronous
 * processing for stateless contexts.
 *
 * This class provides a way to convert methods annotated with {@link McpPrompt} into
 * callback functions that can be used to handle prompt requests asynchronously in
 * stateless environments. It supports various method signatures and return types.
 *
 * @author Christian Tzolov
 */
public final class AsyncStatelessMcpPromptMethodCallback extends AbstractMcpPromptMethodCallback
		implements BiFunction<McpTransportContext, GetPromptRequest, Mono<GetPromptResult>> {

	private AsyncStatelessMcpPromptMethodCallback(Builder builder) {
		super(builder.method, builder.bean, builder.prompt);
	}

	/**
	 * Apply the callback to the given context and request.
	 * <p>
	 * This method builds the arguments for the method call, invokes the method, and
	 * converts the result to a GetPromptResult.
	 * @param context The transport context, may be null if the method doesn't require it
	 * @param request The prompt request, must not be null
	 * @return A Mono that emits the prompt result
	 * @throws McpPromptMethodException if there is an error invoking the prompt method
	 * @throws IllegalArgumentException if the request is null
	 */
	@Override
	public Mono<GetPromptResult> apply(McpTransportContext context, GetPromptRequest request) {
		if (request == null) {
			return Mono.error(new IllegalArgumentException("Request must not be null"));
		}

		return Mono.defer(() -> {
			try {
				// Build arguments for the method call
				Object[] args = this.buildArgs(this.method, context, request);

				// Invoke the method
				this.method.setAccessible(true);
				Object result = this.method.invoke(this.bean, args);

				// Handle the result based on its type
				if (result instanceof Mono<?>) {
					// If the result is already a Mono, map it to a GetPromptResult
					return ((Mono<?>) result).map(r -> convertToGetPromptResult(r));
				}
				else {
					// Otherwise, convert the result to a GetPromptResult and wrap in a
					// Mono
					return Mono.just(convertToGetPromptResult(result));
				}
			}
			catch (Exception e) {
				return Mono
					.error(new McpPromptMethodException("Error invoking prompt method: " + this.method.getName(), e));
			}
		});
	}

	@Override
	protected boolean isExchangeOrContextType(Class<?> paramType) {
		return McpTransportContext.class.isAssignableFrom(paramType);
	}

	@Override
	protected void validateReturnType(Method method) {
		Class<?> returnType = method.getReturnType();

		boolean validReturnType = GetPromptResult.class.isAssignableFrom(returnType)
				|| List.class.isAssignableFrom(returnType) || PromptMessage.class.isAssignableFrom(returnType)
				|| String.class.isAssignableFrom(returnType) || Mono.class.isAssignableFrom(returnType);

		if (!validReturnType) {
			throw new IllegalArgumentException("Method must return either GetPromptResult, List<PromptMessage>, "
					+ "List<String>, PromptMessage, String, or Mono<T>: " + method.getName() + " in "
					+ method.getDeclaringClass().getName() + " returns " + returnType.getName());
		}
	}

	/**
	 * Builder for creating AsyncStatelessMcpPromptMethodCallback instances.
	 * <p>
	 * This builder provides a fluent API for constructing
	 * AsyncStatelessMcpPromptMethodCallback instances with the required parameters.
	 */
	public static class Builder extends AbstractBuilder<Builder, AsyncStatelessMcpPromptMethodCallback> {

		/**
		 * Build the callback.
		 * @return A new AsyncStatelessMcpPromptMethodCallback instance
		 */
		@Override
		public AsyncStatelessMcpPromptMethodCallback build() {
			validate();
			return new AsyncStatelessMcpPromptMethodCallback(this);
		}

	}

	/**
	 * Create a new builder.
	 * @return A new builder instance
	 */
	public static Builder builder() {
		return new Builder();
	}

}
