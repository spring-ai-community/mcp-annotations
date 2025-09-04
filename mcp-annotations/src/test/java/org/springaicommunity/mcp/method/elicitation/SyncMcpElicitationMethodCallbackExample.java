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

	@McpElicitation(clients = "my-client-id")
	public ElicitResult handleElicitationRequest(ElicitRequest request) {
		// Example implementation that accepts the request and returns some content
		return new ElicitResult(ElicitResult.Action.ACCEPT,
				Map.of("userInput", "Example user input", "confirmed", true));
	}

	@McpElicitation(clients = "my-client-id")
	public ElicitResult handleDeclineElicitationRequest(ElicitRequest request) {
		// Example implementation that declines the request
		return new ElicitResult(ElicitResult.Action.DECLINE, null);
	}

	@McpElicitation(clients = "my-client-id")
	public ElicitResult handleCancelElicitationRequest(ElicitRequest request) {
		// Example implementation that cancels the request
		return new ElicitResult(ElicitResult.Action.CANCEL, null);
	}

	// Test methods for invalid scenarios

	@McpElicitation(clients = "my-client-id")
	public String invalidReturnType(ElicitRequest request) {
		return "Invalid return type";
	}

	@McpElicitation(clients = "my-client-id")
	public ElicitResult invalidParameterType(String request) {
		return new ElicitResult(ElicitResult.Action.ACCEPT, Map.of("test", "value"));
	}

	@McpElicitation(clients = "my-client-id")
	public ElicitResult noParameters() {
		return new ElicitResult(ElicitResult.Action.ACCEPT, Map.of("test", "value"));
	}

	@McpElicitation(clients = "my-client-id")
	public ElicitResult tooManyParameters(ElicitRequest request, String extra) {
		return new ElicitResult(ElicitResult.Action.ACCEPT, Map.of("test", "value"));
	}

}
