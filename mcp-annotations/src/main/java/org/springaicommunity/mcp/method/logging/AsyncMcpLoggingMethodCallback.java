/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.logging;

import java.lang.reflect.Method;
import java.util.function.Function;

import org.springaicommunity.mcp.annotation.McpLogging;

import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import reactor.core.publisher.Mono;

/**
 * Class for creating Function callbacks around logging consumer methods that return Mono.
 *
 * This class provides a way to convert methods annotated with {@link McpLogging} into
 * callback functions that can be used to handle logging message notifications in a
 * reactive way. It supports methods with either a single LoggingMessageNotification
 * parameter or three parameters (LoggingLevel, String, String).
 *
 * @author Christian Tzolov
 */
public final class AsyncMcpLoggingMethodCallback extends AbstractMcpLoggingMethodCallback
		implements Function<LoggingMessageNotification, Mono<Void>> {

	private AsyncMcpLoggingMethodCallback(Builder builder) {
		super(builder.method, builder.bean);
	}

	/**
	 * Apply the callback to the given notification.
	 * <p>
	 * This method builds the arguments for the method call, invokes the method, and
	 * returns a Mono that completes when the method execution is done.
	 * @param notification The logging message notification, must not be null
	 * @return A Mono that completes when the method execution is done
	 * @throws McpLoggingConsumerMethodException if there is an error invoking the logging
	 * consumer method
	 * @throws IllegalArgumentException if the notification is null
	 */
	@Override
	public Mono<Void> apply(LoggingMessageNotification notification) {
		if (notification == null) {
			return Mono.error(new IllegalArgumentException("Notification must not be null"));
		}

		try {
			// Build arguments for the method call
			Object[] args = this.buildArgs(this.method, null, notification);

			// Invoke the method
			this.method.setAccessible(true);
			Object result = this.method.invoke(this.bean, args);

			// If the method returns a Mono, handle it
			if (result instanceof Mono) {
				// We need to handle the case where the Mono is not a Mono<Void>
				// This is expected by the test testInvalidMonoReturnType
				Mono<?> monoResult = (Mono<?>) result;

				// Convert the Mono to a Mono<Void> by checking the value
				// If the value is not null (i.e., not Void), throw a ClassCastException
				return monoResult.flatMap(value -> {
					if (value != null) {
						// This will be caught by the test testInvalidMonoReturnType
						throw new ClassCastException(
								"Expected Mono<Void> but got Mono<" + value.getClass().getName() + ">");
					}
					return Mono.empty();
				}).then();
			}
			// If the method returns void, return an empty Mono
			return Mono.empty();
		}
		catch (Exception e) {
			return Mono.error(new McpLoggingConsumerMethodException(
					"Error invoking logging consumer method: " + this.method.getName(), e));
		}
	}

	/**
	 * Validates that the method return type is compatible with the logging consumer
	 * callback.
	 * @param method The method to validate
	 * @throws IllegalArgumentException if the return type is not compatible
	 */
	@Override
	protected void validateReturnType(Method method) {
		Class<?> returnType = method.getReturnType();

		if (returnType != void.class && !Mono.class.isAssignableFrom(returnType)) {
			throw new IllegalArgumentException("Method must have void or Mono<Void> return type: " + method.getName()
					+ " in " + method.getDeclaringClass().getName() + " returns " + returnType.getName());
		}
	}

	/**
	 * Builder for creating AsyncMcpLoggingConsumerMethodCallback instances.
	 * <p>
	 * This builder provides a fluent API for constructing
	 * AsyncMcpLoggingConsumerMethodCallback instances with the required parameters.
	 */
	public static class Builder extends AbstractBuilder<Builder, AsyncMcpLoggingMethodCallback> {

		/**
		 * Build the callback.
		 * @return A new AsyncMcpLoggingConsumerMethodCallback instance
		 */
		@Override
		public AsyncMcpLoggingMethodCallback build() {
			validate();
			return new AsyncMcpLoggingMethodCallback(this);
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
