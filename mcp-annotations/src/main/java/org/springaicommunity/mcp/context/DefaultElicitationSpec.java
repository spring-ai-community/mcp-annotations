/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.context;

import java.util.HashMap;
import java.util.Map;

import org.springaicommunity.mcp.context.McpRequestContextTypes.ElicitationSpec;

public class DefaultElicitationSpec implements ElicitationSpec {

	protected String message;

	protected Map<String, Object> meta = new HashMap<>();

	protected String message() {
		return message;
	}

	protected Map<String, Object> meta() {
		return meta;
	}

	@Override
	public ElicitationSpec message(String message) {
		this.message = message;
		return this;
	}

	@Override
	public ElicitationSpec meta(Map<String, Object> m) {
		if (m != null) {
			this.meta.putAll(m);
		}
		return this;
	}

	@Override
	public ElicitationSpec meta(String k, Object v) {
		if (k != null && v != null) {
			this.meta.put(k, v);
		}
		return this;
	}

}
