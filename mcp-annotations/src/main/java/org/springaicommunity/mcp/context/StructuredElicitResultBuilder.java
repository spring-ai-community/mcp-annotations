/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.context;

import java.util.HashMap;
import java.util.Map;

import io.modelcontextprotocol.spec.McpSchema.ElicitResult.Action;
import io.modelcontextprotocol.util.Assert;

/**
 * Builder for {@link StructuredElicitResult}.
 *
 * @param <T> the type of the structured content
 * @author Christian Tzolov
 */
public class StructuredElicitResultBuilder<T> {

	private Action action = Action.ACCEPT;

	private T structuredContent;

	private Map<String, Object> meta = new HashMap<>();

	/**
	 * Private constructor to enforce builder pattern usage.
	 */
	private StructuredElicitResultBuilder() {
		this.meta = new HashMap<>();
	}

	/**
	 * Creates a new builder instance.
	 * @param <T> the type of the structured content
	 * @return a new builder instance
	 */
	public static <T> StructuredElicitResultBuilder<T> builder() {
		return new StructuredElicitResultBuilder<>();
	}

	/**
	 * Sets the action.
	 * @param action the action to set
	 * @return this builder instance
	 */
	public StructuredElicitResultBuilder<T> action(Action action) {
		Assert.notNull(action, "Action must not be null");
		this.action = action;
		return this;
	}

	/**
	 * Sets the structured content.
	 * @param structuredContent the structured content to set
	 * @return this builder instance
	 */
	public StructuredElicitResultBuilder<T> structuredContent(T structuredContent) {
		this.structuredContent = structuredContent;
		return this;
	}

	/**
	 * Sets the meta map.
	 * @param meta the meta map to set
	 * @return this builder instance
	 */
	public StructuredElicitResultBuilder<T> meta(Map<String, Object> meta) {
		this.meta = meta != null ? new HashMap<>(meta) : new HashMap<>();
		return this;
	}

	/**
	 * Adds a single meta entry.
	 * @param key the meta key
	 * @param value the meta value
	 * @return this builder instance
	 */
	public StructuredElicitResultBuilder<T> addMeta(String key, Object value) {
		this.meta.put(key, value);
		return this;
	}

	/**
	 * Builds the {@link StructuredElicitResult} instance.
	 * @return a new StructuredElicitResult instance
	 */
	public StructuredElicitResult<T> build() {
		return new StructuredElicitResult<>(this.action, this.structuredContent, this.meta);
	}

}
