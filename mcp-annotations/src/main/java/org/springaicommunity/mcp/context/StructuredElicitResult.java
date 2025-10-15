/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.context;

import java.util.HashMap;
import java.util.Map;

import io.modelcontextprotocol.spec.McpSchema.ElicitResult.Action;
import io.modelcontextprotocol.util.Assert;

/**
 * A record representing the result of a structured elicit action.
 *
 * @param <T> the type of the structured content
 * @author Christian Tzolov
 */
public record StructuredElicitResult<T>(Action action, T structuredContent, Map<String, Object> meta) {

	public static Builder<?> builder() {
		return new Builder<>();
	}

	public static class Builder<T> {

		private Action action = Action.ACCEPT;

		private T structuredContent;

		private Map<String, Object> meta = new HashMap<>();

		/**
		 * Private constructor to enforce builder pattern usage.
		 */
		private Builder() {
			this.meta = new HashMap<>();
		}

		/**
		 * Sets the action.
		 * @param action the action to set
		 * @return this builder instance
		 */
		public Builder<T> action(Action action) {
			Assert.notNull(action, "Action must not be null");
			this.action = action;
			return this;
		}

		/**
		 * Sets the structured content.
		 * @param <U> the type of the structured content
		 * @param structuredContent the structured content to set
		 * @return this builder instance with the correct type
		 */
		@SuppressWarnings("unchecked")
		public <U> Builder<U> structuredContent(U structuredContent) {
			Builder<U> typedBuilder = (Builder<U>) this;
			typedBuilder.structuredContent = structuredContent;
			return typedBuilder;
		}

		/**
		 * Sets the meta map.
		 * @param meta the meta map to set
		 * @return this builder instance
		 */
		public Builder<T> meta(Map<String, Object> meta) {
			this.meta = meta != null ? new HashMap<>(meta) : new HashMap<>();
			return this;
		}

		/**
		 * Adds a single meta entry.
		 * @param key the meta key
		 * @param value the meta value
		 * @return this builder instance
		 */
		public Builder<T> addMeta(String key, Object value) {
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

}
