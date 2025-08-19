/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.elicitation;

import java.lang.reflect.Method;
import java.util.function.Function;

import org.springaicommunity.mcp.annotation.McpElicitation;

import io.modelcontextprotocol.spec.McpSchema.ElicitRequest;
import io.modelcontextprotocol.spec.McpSchema.ElicitResult;
import reactor.core.publisher.Mono;

/**
 * Class for creating Function callbacks around elicitation methods that return Mono.
 *
 * This class provides a way to convert methods annotated with {@link McpElicitation} into
 * callback functions that can be used to handle elicitation requests in a reactive way.
 * It supports methods with a single ElicitRequest parameter.
 *
 * @author Christian Tzolov
 */
public final class AsyncMcpElicitationMethodCallback extends AbstractMcpElicitationMethodCallback
		implements Function<ElicitRequest, Mono<ElicitResult>> {

	private AsyncMcpElicitationMethodCallback(Builder builder) {
		super(builder.method, builder.bean);
	}

	/**
	 * Apply the callback to the given request.
	 * <p>
	 * This method builds the arguments for the method call, invokes the method, and
	 * returns a Mono that completes with the result.
	 * @param request The elicitation request, must not be null
	 * @return A Mono that completes with the result of the method invocation
	 * @throws McpElicitationMethodException if there is an error invoking the elicitation
	 * method
	 * @throws IllegalArgumentException if the request is null
	 */
	@Override
	public Mono<ElicitResult> apply(ElicitRequest request) {
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
				Mono<ElicitResult> monoResult = (Mono<ElicitResult>) result;
				return monoResult;
			}
			// If the method returns an ElicitResult directly, wrap it in a Mono
			else if (result instanceof ElicitResult) {
				return Mono.just((ElicitResult) result);
			}
			// Otherwise, throw an exception
			else {
				return Mono.error(new McpElicitationMethodException(
						"Method must return Mono<ElicitResult> or ElicitResult: " + this.method.getName()));
			}
		}
		catch (Exception e) {
			return Mono.error(new McpElicitationMethodException(
					"Error invoking elicitation method: " + this.method.getName(), e));
		}
	}

	/**
	 * Validates that the method return type is compatible with the elicitation callback.
	 * @param method The method to validate
	 * @throws IllegalArgumentException if the return type is not compatible
	 */
	@Override
	protected void validateReturnType(Method method) {
		Class<?> returnType = method.getReturnType();

		if (!Mono.class.isAssignableFrom(returnType) && !ElicitResult.class.isAssignableFrom(returnType)) {
			throw new IllegalArgumentException(
					"Method must return Mono<ElicitResult> or ElicitResult: " + method.getName() + " in "
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
		// No exchange type for elicitation methods
		return false;
	}

	/**
	 * Builder for creating AsyncMcpElicitationMethodCallback instances.
	 * <p>
	 * This builder provides a fluent API for constructing
	 * AsyncMcpElicitationMethodCallback instances with the required parameters.
	 */
	public static class Builder extends AbstractBuilder<Builder, AsyncMcpElicitationMethodCallback> {

		/**
		 * Build the callback.
		 * @return A new AsyncMcpElicitationMethodCallback instance
		 */
		@Override
		public AsyncMcpElicitationMethodCallback build() {
			validate();
			return new AsyncMcpElicitationMethodCallback(this);
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
