/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.elicitation;

import java.util.Map;

import org.springaicommunity.mcp.annotation.McpElicitation;

import io.modelcontextprotocol.spec.McpSchema.ElicitRequest;
import io.modelcontextprotocol.spec.McpSchema.ElicitResult;

/**
 * Example class demonstrating synchronous elicitation method usage.
 *
 * @author Christian Tzolov
 */
public class SyncMcpElicitationMethodCallbackExample {

	@McpElicitation
	public ElicitResult handleElicitationRequest(ElicitRequest request) {
		// Example implementation that accepts the request and returns some content
		return new ElicitResult(ElicitResult.Action.ACCEPT,
				Map.of("userInput", "Example user input", "confirmed", true));
	}

	@McpElicitation
	public ElicitResult handleDeclineElicitationRequest(ElicitRequest request) {
		// Example implementation that declines the request
		return new ElicitResult(ElicitResult.Action.DECLINE, null);
	}

	@McpElicitation
	public ElicitResult handleCancelElicitationRequest(ElicitRequest request) {
		// Example implementation that cancels the request
		return new ElicitResult(ElicitResult.Action.CANCEL, null);
	}

}
