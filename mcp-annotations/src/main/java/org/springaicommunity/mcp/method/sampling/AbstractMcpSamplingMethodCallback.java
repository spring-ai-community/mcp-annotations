/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.sampling;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import org.springaicommunity.mcp.annotation.McpSampling;

import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import io.modelcontextprotocol.util.Assert;

/**
 * Abstract base class for creating callbacks around sampling methods.
 *
 * This class provides common functionality for both synchronous and asynchronous sampling
 * method callbacks. It contains shared logic for method validation, argument building,
 * and other common operations.
 *
 * @author Christian Tzolov
 */
public abstract class AbstractMcpSamplingMethodCallback {

	protected final Method method;

	protected final Object bean;

	/**
	 * Constructor for AbstractMcpSamplingMethodCallback.
	 * @param method The method to create a callback for
	 * @param bean The bean instance that contains the method
	 */
	protected AbstractMcpSamplingMethodCallback(Method method, Object bean) {
		Assert.notNull(method, "Method can't be null!");
		Assert.notNull(bean, "Bean can't be null!");

		this.method = method;
		this.bean = bean;
		this.validateMethod(this.method);
	}

	/**
	 * Validates that the method signature is compatible with the sampling callback.
	 * <p>
	 * This method checks that the return type is valid and that the parameters match the
	 * expected pattern.
	 * @param method The method to validate
	 * @throws IllegalArgumentException if the method signature is not compatible
	 */
	protected void validateMethod(Method method) {
		if (method == null) {
			throw new IllegalArgumentException("Method must not be null");
		}

		this.validateReturnType(method);
		this.validateParameters(method);
	}

	/**
	 * Validates that the method return type is compatible with the sampling callback.
	 * This method should be implemented by subclasses to handle specific return type
	 * validation.
	 * @param method The method to validate
	 * @throws IllegalArgumentException if the return type is not compatible
	 */
	protected abstract void validateReturnType(Method method);

	/**
	 * Validates method parameters. This method provides common validation logic and
	 * delegates exchange type checking to subclasses.
	 * @param method The method to validate
	 * @throws IllegalArgumentException if the parameters are not compatible
	 */
	protected void validateParameters(Method method) {
		Parameter[] parameters = method.getParameters();

		// Check parameter count - must have at least 1 parameter
		if (parameters.length < 1) {
			throw new IllegalArgumentException(
					"Method must have at least 1 parameter (CreateMessageRequest): " + method.getName() + " in "
							+ method.getDeclaringClass().getName() + " has " + parameters.length + " parameters");
		}

		// Check parameter types
		if (parameters.length == 1) {
			// Single parameter must be CreateMessageRequest
			if (!CreateMessageRequest.class.isAssignableFrom(parameters[0].getType())) {
				throw new IllegalArgumentException("Single parameter must be of type CreateMessageRequest: "
						+ method.getName() + " in " + method.getDeclaringClass().getName() + " has parameter of type "
						+ parameters[0].getType().getName());
			}
		}
		else {
			// TODO: Support for multiple parameters corresponding to CreateMessageRequest
			// fields
			// For now, we only support the single parameter version
			throw new IllegalArgumentException(
					"Currently only methods with a single CreateMessageRequest parameter are supported: "
							+ method.getName() + " in " + method.getDeclaringClass().getName() + " has "
							+ parameters.length + " parameters");
		}
	}

	/**
	 * Builds the arguments array for invoking the method.
	 * <p>
	 * This method constructs an array of arguments based on the method's parameter types
	 * and the available values (exchange, request).
	 * @param method The method to build arguments for
	 * @param exchange The server exchange
	 * @param request The sampling request
	 * @return An array of arguments for the method invocation
	 */
	protected Object[] buildArgs(Method method, Object exchange, CreateMessageRequest request) {
		Parameter[] parameters = method.getParameters();
		Object[] args = new Object[parameters.length];

		if (parameters.length == 1) {
			// Single parameter (CreateMessageRequest)
			args[0] = request;
		}
		else {
			// TODO: Support for multiple parameters corresponding to CreateMessageRequest
			// fields
			// For now, we only support the single parameter version
			throw new IllegalArgumentException(
					"Currently only methods with a single CreateMessageRequest parameter are supported");
		}

		return args;
	}

	/**
	 * Checks if a parameter type is compatible with the exchange type. This method should
	 * be implemented by subclasses to handle specific exchange type checking.
	 * @param paramType The parameter type to check
	 * @return true if the parameter type is compatible with the exchange type, false
	 * otherwise
	 */
	protected abstract boolean isExchangeType(Class<?> paramType);

	/**
	 * Exception thrown when there is an error invoking a sampling method.
	 */
	public static class McpSamplingMethodException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		/**
		 * Constructs a new exception with the specified detail message and cause.
		 * @param message The detail message
		 * @param cause The cause
		 */
		public McpSamplingMethodException(String message, Throwable cause) {
			super(message, cause);
		}

		/**
		 * Constructs a new exception with the specified detail message.
		 * @param message The detail message
		 */
		public McpSamplingMethodException(String message) {
			super(message);
		}

	}

	/**
	 * Abstract builder for creating McpSamplingMethodCallback instances.
	 * <p>
	 * This builder provides a base for constructing callback instances with the required
	 * parameters.
	 *
	 * @param <T> The type of the builder
	 * @param <R> The type of the callback
	 */
	protected abstract static class AbstractBuilder<T extends AbstractBuilder<T, R>, R> {

		protected Method method;

		protected Object bean;

		/**
		 * Set the method to create a callback for.
		 * @param method The method to create a callback for
		 * @return This builder
		 */
		@SuppressWarnings("unchecked")
		public T method(Method method) {
			this.method = method;
			return (T) this;
		}

		/**
		 * Set the bean instance that contains the method.
		 * @param bean The bean instance
		 * @return This builder
		 */
		@SuppressWarnings("unchecked")
		public T bean(Object bean) {
			this.bean = bean;
			return (T) this;
		}

		/**
		 * Set the sampling annotation.
		 * @param sampling The sampling annotation
		 * @return This builder
		 */
		@SuppressWarnings("unchecked")
		public T sampling(McpSampling sampling) {
			// No additional configuration needed from the annotation at this time
			return (T) this;
		}

		/**
		 * Validate the builder state.
		 * @throws IllegalArgumentException if the builder state is invalid
		 */
		protected void validate() {
			if (method == null) {
				throw new IllegalArgumentException("Method must not be null");
			}
			if (bean == null) {
				throw new IllegalArgumentException("Bean must not be null");
			}
		}

		/**
		 * Build the callback.
		 * @return A new callback instance
		 */
		public abstract R build();

	}

}
