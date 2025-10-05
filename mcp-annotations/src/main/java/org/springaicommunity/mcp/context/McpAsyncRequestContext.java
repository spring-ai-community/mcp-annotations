/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.context;

import java.lang.reflect.Type;
import java.util.function.Consumer;

import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import io.modelcontextprotocol.spec.McpSchema.ElicitRequest;
import io.modelcontextprotocol.spec.McpSchema.ElicitResult;
import io.modelcontextprotocol.spec.McpSchema.ListRootsResult;
import io.modelcontextprotocol.spec.McpSchema.ProgressNotification;
import reactor.core.publisher.Mono;

/**
 * Async (Reactor) version of McpSyncRequestContext that returns Mono of value types.
 * 
 * @author Christian Tzolov
 */
public interface McpAsyncRequestContext extends McpRequestContextTypes<McpAsyncServerExchange> {

	// --------------------------------------
	// Roots
	// --------------------------------------
	Mono<ListRootsResult> roots();

	// --------------------------------------
	// Elicitation
	// --------------------------------------
	Mono<ElicitResult> elicitation(Consumer<ElicitationSpec> elicitationSpec);

	Mono<ElicitResult> elicitation(String message, Type type);

	Mono<ElicitResult> elicitation(ElicitRequest elicitRequest);

	// --------------------------------------
	// Sampling
	// --------------------------------------
	Mono<CreateMessageResult> sampling(String... messages);

	Mono<CreateMessageResult> sampling(Consumer<SamplingSpec> samplingSpec);

	Mono<CreateMessageResult> sampling(CreateMessageRequest createMessageRequest);

	// --------------------------------------
	// Progress
	// --------------------------------------
	Mono<Void> progress(int progress);

	Mono<Void> progress(Consumer<ProgressSpec> progressSpec);

	Mono<Void> progress(ProgressNotification progressNotification);

	// --------------------------------------
	// Ping
	// --------------------------------------
	Mono<Object> ping();

	// --------------------------------------
	// Logging
	// --------------------------------------
	Mono<Void> log(Consumer<LoggingSpec> logSpec);

	Mono<Void> debug(String message);

	Mono<Void> info(String message);

	Mono<Void> warn(String message);

	Mono<Void> error(String message);

}
