/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.context;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import io.modelcontextprotocol.util.Assert;
import org.springaicommunity.mcp.context.McpRequestContextTypes.ElicitationSpec;

/**
 * @author Christian Tzolov
 */
public class DefaultElicitationSpec implements ElicitationSpec {

	protected String message;

	protected Type responseType;

	protected Map<String, Object> meta = new HashMap<>();

	@Override
	public McpSyncRequestContext.ElicitationSpec message(String message) {
		Assert.hasText(message, "Message must not be empty");
		this.message = message;
		return this;
	}

	@Override
	public McpSyncRequestContext.ElicitationSpec responseType(Type type) {
		Assert.notNull(type, "Response type must not be null");
		this.responseType = type;
		return this;
	}

	@Override
	public McpSyncRequestContext.ElicitationSpec meta(Map<String, Object> m) {
		Assert.notNull(m, "Meta map must not be null");
		this.meta.putAll(m);
		return this;
	}

	@Override
	public McpSyncRequestContext.ElicitationSpec meta(String k, Object v) {
		if (k != null && v != null) {
			if (this.meta == null) {
				this.meta = new java.util.HashMap<>();
			}
			this.meta.put(k, v);
		}
		return this;
	}

}
