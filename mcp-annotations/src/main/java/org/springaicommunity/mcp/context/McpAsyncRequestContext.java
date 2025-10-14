/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.context;

import java.util.function.Consumer;

import com.fasterxml.jackson.core.type.TypeReference;
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
	Mono<Boolean> rootsEnabled();

	Mono<ListRootsResult> roots();

	// --------------------------------------
	// Elicitation
	// --------------------------------------
	Mono<Boolean> elicitEnabled();

	<T> Mono<StructuredElicitResult<T>> elicit(Class<T> type);

	<T> Mono<StructuredElicitResult<T>> elicit(TypeReference<T> type);

	<T> Mono<StructuredElicitResult<T>> elicit(Consumer<ElicitationSpec> spec, TypeReference<T> returnType);

	<T> Mono<StructuredElicitResult<T>> elicit(Consumer<ElicitationSpec> spec, Class<T> returnType);

	Mono<ElicitResult> elicit(ElicitRequest elicitRequest);

	// --------------------------------------
	// Sampling
	// --------------------------------------
	Mono<Boolean> sampleEnabled();

	Mono<CreateMessageResult> sample(String... messages);

	Mono<CreateMessageResult> sample(Consumer<SamplingSpec> samplingSpec);

	Mono<CreateMessageResult> sample(CreateMessageRequest createMessageRequest);

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
