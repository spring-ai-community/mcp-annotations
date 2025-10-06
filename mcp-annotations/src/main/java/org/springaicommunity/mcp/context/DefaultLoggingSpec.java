/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.context;

import java.util.HashMap;
import java.util.Map;

import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import org.springaicommunity.mcp.context.McpRequestContextTypes.LoggingSpec;

/**
 * @author Christian Tzolov
 */
public class DefaultLoggingSpec implements LoggingSpec {

	protected String message;

	protected String logger;

	protected LoggingLevel level = LoggingLevel.INFO;

	protected Map<String, Object> meta = new HashMap<>();

	@Override
	public LoggingSpec message(String message) {
		this.message = message;
		return this;
	}

	@Override
	public LoggingSpec logger(String logger) {
		this.logger = logger;
		return this;
	}

	@Override
	public LoggingSpec level(LoggingLevel level) {
		this.level = level;
		return this;
	}

	@Override
	public LoggingSpec meta(Map<String, Object> m) {
		if (m != null) {
			this.meta.putAll(m);
		}
		return this;
	}

	@Override
	public LoggingSpec meta(String k, Object v) {
		if (k != null && v != null) {
			this.meta.put(k, v);
		}
		return this;
	}

}
