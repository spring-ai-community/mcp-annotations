/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.changed.resource;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;

import org.springaicommunity.mcp.annotation.McpResourceListChanged;

import io.modelcontextprotocol.spec.McpSchema;

/**
 * Class for creating Consumer callbacks around resource list changed consumer methods.
 *
 * This class provides a way to convert methods annotated with
 * {@link McpResourceListChanged} into callback functions that can be used to handle
 * resource list change notifications. It supports methods with a single
 * List&lt;McpSchema.Resource&gt; parameter.
 *
 * @author Christian Tzolov
 */
public final class SyncMcpResourceListChangedMethodCallback extends AbstractMcpResourceListChangedMethodCallback
		implements Consumer<List<McpSchema.Resource>> {

	private SyncMcpResourceListChangedMethodCallback(Builder builder) {
		super(builder.method, builder.bean);
	}

	/**
	 * Accept the resource list change notification and process it.
	 * <p>
	 * This method builds the arguments for the method call and invokes the method.
	 * @param updatedResources The updated list of resources, must not be null
	 * @throws McpResourceListChangedConsumerMethodException if there is an error invoking
	 * the resource list changed consumer method
	 * @throws IllegalArgumentException if the updatedResources is null
	 */
	@Override
	public void accept(List<McpSchema.Resource> updatedResources) {
		if (updatedResources == null) {
			throw new IllegalArgumentException("Updated resources list must not be null");
		}

		try {
			// Build arguments for the method call
			Object[] args = this.buildArgs(this.method, null, updatedResources);

			// Invoke the method
			this.method.setAccessible(true);
			this.method.invoke(this.bean, args);
		}
		catch (Exception e) {
			throw new McpResourceListChangedConsumerMethodException(
					"Error invoking resource list changed consumer method: " + this.method.getName(), e);
		}
	}

	/**
	 * Validates that the method return type is compatible with the resource list changed
	 * consumer callback.
	 * @param method The method to validate
	 * @throws IllegalArgumentException if the return type is not compatible
	 */
	@Override
	protected void validateReturnType(Method method) {
		Class<?> returnType = method.getReturnType();

		if (returnType != void.class) {
			throw new IllegalArgumentException("Method must have void return type: " + method.getName() + " in "
					+ method.getDeclaringClass().getName() + " returns " + returnType.getName());
		}
	}

	/**
	 * Builder for creating SyncMcpResourceListChangedMethodCallback instances.
	 * <p>
	 * This builder provides a fluent API for constructing
	 * SyncMcpResourceListChangedMethodCallback instances with the required parameters.
	 */
	public static class Builder extends AbstractBuilder<Builder, SyncMcpResourceListChangedMethodCallback> {

		/**
		 * Build the callback.
		 * @return A new SyncMcpResourceListChangedMethodCallback instance
		 */
		@Override
		public SyncMcpResourceListChangedMethodCallback build() {
			validate();
			return new SyncMcpResourceListChangedMethodCallback(this);
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
