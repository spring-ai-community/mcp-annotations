/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.context;

import java.util.Map;
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
	Optional<ListRootsResult> roots();

	// --------------------------------------
	// Elicitation
	// --------------------------------------
	<T> Optional<T> elicitation(TypeReference<T> type);

	<T> Optional<StructuredElicitResult<T>> elicitation(TypeReference<T> type, String message,
			Map<String, Object> meta);

	Optional<ElicitResult> elicitation(ElicitRequest elicitRequest);

	// --------------------------------------
	// Sampling
	// --------------------------------------
	Optional<CreateMessageResult> sampling(String... messages);

	Optional<CreateMessageResult> sampling(Consumer<SamplingSpec> samplingSpec);

	Optional<CreateMessageResult> sampling(CreateMessageRequest createMessageRequest);

	// --------------------------------------
	// Progress
	// --------------------------------------
	void progress(int progress);

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
