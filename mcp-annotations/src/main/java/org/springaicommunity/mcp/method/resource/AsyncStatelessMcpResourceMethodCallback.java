/*
 * Copyright 2025-2025 the original author or authors.
 */
package org.springaicommunity.mcp.method.resource;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.springaicommunity.mcp.annotation.McpResource;

import io.modelcontextprotocol.server.McpTransportContext;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.ResourceContents;
import reactor.core.publisher.Mono;

/**
 * Class for creating BiFunction callbacks around resource methods with asynchronous
 * processing for stateless contexts.
 *
 * This class provides a way to convert methods annotated with {@link McpResource} into
 * callback functions that can be used to handle resource requests asynchronously in
 * stateless environments. It supports various method signatures and return types, and
 * handles URI template variables.
 *
 * @author Christian Tzolov
 */
public final class AsyncStatelessMcpResourceMethodCallback extends AbstractMcpResourceMethodCallback
		implements BiFunction<McpTransportContext, ReadResourceRequest, Mono<ReadResourceResult>> {

	private AsyncStatelessMcpResourceMethodCallback(Builder builder) {
		super(builder.method, builder.bean, builder.uri, builder.name, builder.description, builder.mimeType,
				builder.resultConverter, builder.uriTemplateManagerFactory, builder.contentType);
		this.validateMethod(this.method);
	}

	/**
	 * Apply the callback to the given context and request.
	 * <p>
	 * This method extracts URI variable values from the request URI, builds the arguments
	 * for the method call, invokes the method, and converts the result to a
	 * ReadResourceResult.
	 * @param context The transport context, may be null if the method doesn't require it
	 * @param request The resource request, must not be null
	 * @return A Mono that emits the resource result
	 * @throws McpResourceMethodException if there is an error invoking the resource
	 * method
	 * @throws IllegalArgumentException if the request is null or if URI variable
	 * extraction fails
	 */
	@Override
	public Mono<ReadResourceResult> apply(McpTransportContext context, ReadResourceRequest request) {
		if (request == null) {
			return Mono.error(new IllegalArgumentException("Request must not be null"));
		}

		return Mono.defer(() -> {
			try {
				// Extract URI variable values from the request URI
				Map<String, String> uriVariableValues = this.uriTemplateManager.extractVariableValues(request.uri());

				// Verify all URI variables were extracted if URI variables are expected
				if (!this.uriVariables.isEmpty() && uriVariableValues.size() != this.uriVariables.size()) {
					return Mono
						.error(new IllegalArgumentException("Failed to extract all URI variables from request URI: "
								+ request.uri() + ". Expected variables: " + this.uriVariables + ", but found: "
								+ uriVariableValues.keySet()));
				}

				// Build arguments for the method call
				Object[] args = this.buildArgs(this.method, context, request, uriVariableValues);

				// Invoke the method
				this.method.setAccessible(true);
				Object result = this.method.invoke(this.bean, args);

				// Handle the result based on its type
				if (result instanceof Mono<?>) {
					// If the result is already a Mono, use it
					return ((Mono<?>) result).map(r -> this.resultConverter.convertToReadResourceResult(r,
							request.uri(), this.mimeType, this.contentType));
				}
				else {
					// Otherwise, convert the result to a ReadResourceResult and wrap in a
					// Mono
					return Mono.just(this.resultConverter.convertToReadResourceResult(result, request.uri(),
							this.mimeType, this.contentType));
				}
			}
			catch (Exception e) {
				return Mono.error(
						new McpResourceMethodException("Error invoking resource method: " + this.method.getName(), e));
			}
		});
	}

	/**
	 * Builder for creating AsyncStatelessMcpResourceMethodCallback instances.
	 * <p>
	 * This builder provides a fluent API for constructing
	 * AsyncStatelessMcpResourceMethodCallback instances with the required parameters.
	 */
	public static class Builder extends AbstractBuilder<Builder, AsyncStatelessMcpResourceMethodCallback> {

		/**
		 * Constructor for Builder.
		 */
		public Builder() {
			this.resultConverter = new DefaultMcpReadResourceResultConverter();
		}

		/**
		 * Build the callback.
		 * @return A new AsyncStatelessMcpResourceMethodCallback instance
		 */
		@Override
		public AsyncStatelessMcpResourceMethodCallback build() {
			validate();
			return new AsyncStatelessMcpResourceMethodCallback(this);
		}

	}

	/**
	 * Create a new builder.
	 * @return A new builder instance
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Validates that the method return type is compatible with the resource callback.
	 * @param method The method to validate
	 * @throws IllegalArgumentException if the return type is not compatible
	 */
	@Override
	protected void validateReturnType(Method method) {
		Class<?> returnType = method.getReturnType();

		boolean validReturnType = ReadResourceResult.class.isAssignableFrom(returnType)
				|| List.class.isAssignableFrom(returnType) || ResourceContents.class.isAssignableFrom(returnType)
				|| String.class.isAssignableFrom(returnType) || Mono.class.isAssignableFrom(returnType);

		if (!validReturnType) {
			throw new IllegalArgumentException(
					"Method must return either ReadResourceResult, List<ResourceContents>, List<String>, "
							+ "ResourceContents, String, or Mono<T>: " + method.getName() + " in "
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
	protected boolean isExchangeOrContextType(Class<?> paramType) {
		return McpTransportContext.class.isAssignableFrom(paramType);
	}

}
