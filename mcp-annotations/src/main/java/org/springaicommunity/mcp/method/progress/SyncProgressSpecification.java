/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.progress;

import java.util.Objects;
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

	public SyncProgressSpecification {
		Objects.requireNonNull(clientId, "clientId must not be null");
		if (clientId.trim().isEmpty()) {
			throw new IllegalArgumentException("clientId must not be empty");
		}
		Objects.requireNonNull(progressHandler, "progressHandler must not be null");
	}

}
