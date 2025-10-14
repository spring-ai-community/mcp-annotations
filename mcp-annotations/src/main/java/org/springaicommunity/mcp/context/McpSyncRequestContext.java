/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.context;

import java.util.Optional;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import io.modelcontextprotocol.spec.McpSchema.ElicitRequest;
import io.modelcontextprotocol.spec.McpSchema.ElicitResult;
import io.modelcontextprotocol.spec.McpSchema.ListRootsResult;
import io.modelcontextprotocol.spec.McpSchema.ProgressNotification;

/**
 * @author Christian Tzolov
 */
public interface McpSyncRequestContext extends McpRequestContextTypes<McpSyncServerExchange> {

	// --------------------------------------
	// Roots
	// --------------------------------------
	boolean rootsEnabled();

	ListRootsResult roots();

	// --------------------------------------
	// Elicitation
	// --------------------------------------
	boolean elicitEnabled();

	<T> StructuredElicitResult<T> elicit(Class<T> type);

	<T> StructuredElicitResult<T> elicit(TypeReference<T> type);

	<T> StructuredElicitResult<T> elicit(Consumer<ElicitationSpec> params, Class<T> returnType);

	<T> StructuredElicitResult<T> elicit(Consumer<ElicitationSpec> params, TypeReference<T> returnType);

	ElicitResult elicit(ElicitRequest elicitRequest);

	// --------------------------------------
	// Sampling
	// --------------------------------------
	boolean sampleEnabled();

	CreateMessageResult sample(String... messages);

	CreateMessageResult sample(Consumer<SamplingSpec> samplingSpec);

	CreateMessageResult sample(CreateMessageRequest createMessageRequest);

	// --------------------------------------
	// Progress
	// --------------------------------------
	void progress(int percentage);

	void progress(Consumer<ProgressSpec> progressSpec);

	void progress(ProgressNotification progressNotification);

	// --------------------------------------
	// Ping
	// --------------------------------------
	void ping();

	// --------------------------------------
	// Logging
	// --------------------------------------
	void log(Consumer<LoggingSpec> logSpec);

	void debug(String message);

	void info(String message);

	void warn(String message);

	void error(String message);

}
