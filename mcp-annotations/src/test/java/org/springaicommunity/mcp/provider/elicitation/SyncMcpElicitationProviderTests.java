/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.provider.elicitation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpElicitation;
import org.springaicommunity.mcp.method.elicitation.SyncElicitationSpecification;

import io.modelcontextprotocol.spec.McpSchema.ElicitRequest;
import io.modelcontextprotocol.spec.McpSchema.ElicitResult;

/**
 * Tests for {@link SyncMcpElicitationProvider}.
 *
 * @author Christian Tzolov
 */
public class SyncMcpElicitationProviderTests {

	@Test
	public void testGetElicitationHandler() {
		var provider = new SyncMcpElicitationProvider(List.of(new TestElicitationHandler()));
		SyncElicitationSpecification specification = provider.getElicitationSpecifications().get(0);
		Function<ElicitRequest, ElicitResult> handler = specification.elicitationHandler();

		assertNotNull(handler);

		ElicitRequest request = new ElicitRequest("Please provide your name",
				Map.of("type", "object", "properties", Map.of("name", Map.of("type", "string"))));
		ElicitResult result = handler.apply(request);

		assertNotNull(result);
		assertEquals(ElicitResult.Action.ACCEPT, result.action());
		assertNotNull(result.content());
		assertEquals("Test User", result.content().get("name"));
	}

	public static class TestElicitationHandler {

		@McpElicitation(clientId = "my-client-id")
		public ElicitResult handleElicitation(ElicitRequest request) {
			return new ElicitResult(ElicitResult.Action.ACCEPT,
					Map.of("name", "Test User", "message", request.message()));
		}

	}

	public static class MultipleElicitationHandler {

		@McpElicitation(clientId = "my-client-id")
		public ElicitResult handleElicitation1(ElicitRequest request) {
			return new ElicitResult(ElicitResult.Action.ACCEPT, Map.of("handler", "1"));
		}

		@McpElicitation(clientId = "my-client-id")
		public ElicitResult handleElicitation2(ElicitRequest request) {
			return new ElicitResult(ElicitResult.Action.ACCEPT, Map.of("handler", "2"));
		}

	}

}
