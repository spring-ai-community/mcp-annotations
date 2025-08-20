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

/**
 * Class for creating BiFunction callbacks around resource methods for stateless contexts.
 *
 * This class provides a way to convert methods annotated with {@link McpResource} into
 * callback functions that can be used to handle resource requests in stateless
 * environments. It supports various method signatures and return types, and handles URI
 * template variables.
 *
 * @author Christian Tzolov
 */
public final class SyncStatelessMcpResourceMethodCallback extends AbstractMcpResourceMethodCallback
		implements BiFunction<McpTransportContext, ReadResourceRequest, ReadResourceResult> {

	private SyncStatelessMcpResourceMethodCallback(Builder builder) {
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
	 * @return The resource result
	 * @throws McpResourceMethodException if there is an error invoking the resource
	 * method
	 * @throws IllegalArgumentException if the request is null or if URI variable
	 * extraction fails
	 */
	@Override
	public ReadResourceResult apply(McpTransportContext context, ReadResourceRequest request) {
		if (request == null) {
			throw new IllegalArgumentException("Request must not be null");
		}

		try {
			// Extract URI variable values from the request URI
			Map<String, String> uriVariableValues = this.uriTemplateManager.extractVariableValues(request.uri());

			// Verify all URI variables were extracted if URI variables are expected
			if (!this.uriVariables.isEmpty() && uriVariableValues.size() != this.uriVariables.size()) {
				throw new IllegalArgumentException("Failed to extract all URI variables from request URI: "
						+ request.uri() + ". Expected variables: " + this.uriVariables + ", but found: "
						+ uriVariableValues.keySet());
			}

			// Build arguments for the method call
			Object[] args = this.buildArgs(this.method, context, request, uriVariableValues);

			// Invoke the method
			this.method.setAccessible(true);
			Object result = this.method.invoke(this.bean, args);

			// Convert the result to a ReadResourceResult using the converter
			return this.resultConverter.convertToReadResourceResult(result, request.uri(), this.mimeType,
					this.contentType);
		}
		catch (Exception e) {
			throw new McpResourceMethodException("Access error invoking resource method: " + this.method.getName(), e);
		}
	}

	/**
	 * Builder for creating SyncStatelessMcpResourceMethodCallback instances.
	 * <p>
	 * This builder provides a fluent API for constructing
	 * SyncStatelessMcpResourceMethodCallback instances with the required parameters.
	 */
	public static class Builder extends AbstractBuilder<Builder, SyncStatelessMcpResourceMethodCallback> {

		/**
		 * Constructor for Builder.
		 */
		private Builder() {
			this.resultConverter = new DefaultMcpReadResourceResultConverter();
		}

		@Override
		public SyncStatelessMcpResourceMethodCallback build() {
			validate();
			return new SyncStatelessMcpResourceMethodCallback(this);
		}

	}

	/**
	 * Create a new builder.
	 * @return A new builder instance
	 */
	public static Builder builder() {
		return new Builder();
	}

	@Override
	protected void validateReturnType(Method method) {
		Class<?> returnType = method.getReturnType();

		boolean validReturnType = ReadResourceResult.class.isAssignableFrom(returnType)
				|| List.class.isAssignableFrom(returnType) || ResourceContents.class.isAssignableFrom(returnType)
				|| String.class.isAssignableFrom(returnType);

		if (!validReturnType) {
			throw new IllegalArgumentException(
					"Method must return either ReadResourceResult, List<ResourceContents>, List<String>, "
							+ "ResourceContents, or String: " + method.getName() + " in "
							+ method.getDeclaringClass().getName() + " returns " + returnType.getName());
		}
	}

	@Override
	protected boolean isExchangeOrContextType(Class<?> paramType) {
		return McpTransportContext.class.isAssignableFrom(paramType);
	}

}
