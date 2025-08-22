/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.progress;

import java.util.function.Consumer;

import io.modelcontextprotocol.spec.McpSchema.ProgressNotification;

/**
 * Specification for synchronous progress handlers.
 *
 * @param clientId The client ID for the progress handler
 * @param progressHandler The consumer that handles progress notifications
 * @author Christian Tzolov
 */
public record SyncProgressSpecification(String clientId, Consumer<ProgressNotification> progressHandler) {
}
