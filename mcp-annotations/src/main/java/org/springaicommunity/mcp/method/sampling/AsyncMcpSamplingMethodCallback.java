/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.sampling;

import java.lang.reflect.Method;
import java.util.function.Function;

import org.springaicommunity.mcp.annotation.McpSampling;

import io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import reactor.core.publisher.Mono;

/**
 * Class for creating Function callbacks around sampling methods that return Mono.
 *
 * This class provides a way to convert methods annotated with {@link McpSampling} into
 * callback functions that can be used to handle sampling requests in a reactive way. It
 * supports methods with a single CreateMessageRequest parameter.
 *
 * @author Christian Tzolov
 */
public final class AsyncMcpSamplingMethodCallback extends AbstractMcpSamplingMethodCallback
		implements Function<CreateMessageRequest, Mono<CreateMessageResult>> {

	private AsyncMcpSamplingMethodCallback(Builder builder) {
		super(builder.method, builder.bean);
	}

	/**
	 * Apply the callback to the given request.
	 * <p>
	 * This method builds the arguments for the method call, invokes the method, and
	 * returns a Mono that completes with the result.
	 * @param request The sampling request, must not be null
	 * @return A Mono that completes with the result of the method invocation
	 * @throws McpSamplingMethodException if there is an error invoking the sampling
	 * method
	 * @throws IllegalArgumentException if the request is null
	 */
	@Override
	public Mono<CreateMessageResult> apply(CreateMessageRequest request) {
		if (request == null) {
			return Mono.error(new IllegalArgumentException("Request must not be null"));
		}

		try {
			// Build arguments for the method call
			Object[] args = this.buildArgs(this.method, null, request);

			// Invoke the method
			this.method.setAccessible(true);
			Object result = this.method.invoke(this.bean, args);

			// If the method returns a Mono, handle it
			if (result instanceof Mono) {
				@SuppressWarnings("unchecked")
				Mono<CreateMessageResult> monoResult = (Mono<CreateMessageResult>) result;
				return monoResult;
			}
			// If the method returns a CreateMessageResult directly, wrap it in a Mono
			else if (result instanceof CreateMessageResult) {
				return Mono.just((CreateMessageResult) result);
			}
			// Otherwise, throw an exception
			else {
				return Mono.error(new McpSamplingMethodException(
						"Method must return Mono<CreateMessageResult> or CreateMessageResult: "
								+ this.method.getName()));
			}
		}
		catch (Exception e) {
			return Mono
				.error(new McpSamplingMethodException("Error invoking sampling method: " + this.method.getName(), e));
		}
	}

	/**
	 * Validates that the method return type is compatible with the sampling callback.
	 * @param method The method to validate
	 * @throws IllegalArgumentException if the return type is not compatible
	 */
	@Override
	protected void validateReturnType(Method method) {
		Class<?> returnType = method.getReturnType();

		if (!Mono.class.isAssignableFrom(returnType) && !CreateMessageResult.class.isAssignableFrom(returnType)) {
			throw new IllegalArgumentException(
					"Method must return Mono<CreateMessageResult> or CreateMessageResult: " + method.getName() + " in "
							+ method.getDeclaringClass().getName() + " returns " + returnType.getName());
		}
	}

	/**
	 * Checks if a parameter type is compatible with the exchange type.
	 * @param paramType The parameter type to check
	 * @return true if the parameter type is compatible with the exchange type, false
	 * otherwise
	 */
	@Override
	protected boolean isExchangeType(Class<?> paramType) {
		// No exchange type for sampling methods
		return false;
	}

	/**
	 * Builder for creating AsyncMcpSamplingMethodCallback instances.
	 * <p>
	 * This builder provides a fluent API for constructing AsyncMcpSamplingMethodCallback
	 * instances with the required parameters.
	 */
	public static class Builder extends AbstractBuilder<Builder, AsyncMcpSamplingMethodCallback> {

		/**
		 * Build the callback.
		 * @return A new AsyncMcpSamplingMethodCallback instance
		 */
		@Override
		public AsyncMcpSamplingMethodCallback build() {
			validate();
			return new AsyncMcpSamplingMethodCallback(this);
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
