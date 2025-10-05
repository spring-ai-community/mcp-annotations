/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.context;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Consumer;

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
	Optional<ElicitResult> elicitation(Consumer<ElicitationSpec> elicitationSpec);

	Optional<ElicitResult> elicitation(String message, Type type);

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
