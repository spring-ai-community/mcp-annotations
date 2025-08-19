/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.elicitation;

import java.lang.reflect.Method;
import java.util.function.Function;

import org.springaicommunity.mcp.annotation.McpElicitation;

import io.modelcontextprotocol.spec.McpSchema.ElicitRequest;
import io.modelcontextprotocol.spec.McpSchema.ElicitResult;

/**
 * Class for creating Function callbacks around elicitation methods.
 *
 * This class provides a way to convert methods annotated with {@link McpElicitation} into
 * callback functions that can be used to handle elicitation requests. It supports methods
 * with a single ElicitRequest parameter.
 *
 * @author Christian Tzolov
 */
public final class SyncMcpElicitationMethodCallback extends AbstractMcpElicitationMethodCallback
		implements Function<ElicitRequest, ElicitResult> {

	private SyncMcpElicitationMethodCallback(Builder builder) {
		super(builder.method, builder.bean);
	}

	/**
	 * Apply the callback to the given request.
	 * <p>
	 * This method builds the arguments for the method call, invokes the method, and
	 * returns the result.
	 * @param request The elicitation request, must not be null
	 * @return The result of the method invocation
	 * @throws McpElicitationMethodException if there is an error invoking the elicitation
	 * method
	 * @throws IllegalArgumentException if the request is null
	 */
	@Override
	public ElicitResult apply(ElicitRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Request must not be null");
		}

		try {
			// Build arguments for the method call
			Object[] args = this.buildArgs(this.method, null, request);

			// Invoke the method
			this.method.setAccessible(true);
			Object result = this.method.invoke(this.bean, args);

			// Return the result
			return (ElicitResult) result;
		}
		catch (Exception e) {
			throw new McpElicitationMethodException("Error invoking elicitation method: " + this.method.getName(), e);
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

		if (!ElicitResult.class.isAssignableFrom(returnType)) {
			throw new IllegalArgumentException("Method must return ElicitResult: " + method.getName() + " in "
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
	 * Builder for creating SyncMcpElicitationMethodCallback instances.
	 * <p>
	 * This builder provides a fluent API for constructing
	 * SyncMcpElicitationMethodCallback instances with the required parameters.
	 */
	public static class Builder extends AbstractBuilder<Builder, SyncMcpElicitationMethodCallback> {

		/**
		 * Build the callback.
		 * @return A new SyncMcpElicitationMethodCallback instance
		 */
		@Override
		public SyncMcpElicitationMethodCallback build() {
			validate();
			return new SyncMcpElicitationMethodCallback(this);
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
