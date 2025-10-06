/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.context;

import java.util.HashMap;
import java.util.Map;

import org.springaicommunity.mcp.context.McpRequestContextTypes.ProgressSpec;

/**
 * @author Christian Tzolov
 */
public class DefaultProgressSpec implements ProgressSpec {

	protected double progress = 0.0;

	protected double total = 1.0;

	protected String message;

	protected Map<String, Object> meta = new HashMap<>();

	@Override
	public ProgressSpec progress(double progress) {
		this.progress = progress;
		return this;
	}

	@Override
	public ProgressSpec total(double total) {
		this.total = total;
		return this;
	}

	@Override
	public ProgressSpec message(String message) {
		this.message = message;
		return this;
	}

	@Override
	public ProgressSpec meta(Map<String, Object> m) {
		if (m != null) {
			this.meta.putAll(m);
		}
		return this;
	}

	@Override
	public ProgressSpec meta(String k, Object v) {
		if (k != null && v != null) {
			this.meta.put(k, v);
		}
		return this;
	}

}
