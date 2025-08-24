/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.provider.changed.tool;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpToolListChanged;
import org.springaicommunity.mcp.method.changed.tool.SyncToolListChangedSpecification;

import io.modelcontextprotocol.spec.McpSchema;

/**
 * Tests for {@link SyncMcpToolListChangedProvider}.
 *
 * @author Christian Tzolov
 */
public class SyncMcpToolListChangedProviderTests {

	private static final List<McpSchema.Tool> TEST_TOOLS = List.of(
			McpSchema.Tool.builder().name("test-tool-1").description("Test Tool 1").inputSchema("{}").build(),
			McpSchema.Tool.builder().name("test-tool-2").description("Test Tool 2").inputSchema("{}").build());

	/**
	 * Test class with tool list changed consumer methods.
	 */
	static class ToolListChangedHandler {

		private List<McpSchema.Tool> lastUpdatedTools;

		@McpToolListChanged
		public void handleToolListChanged(List<McpSchema.Tool> updatedTools) {
			this.lastUpdatedTools = updatedTools;
		}

		@McpToolListChanged(clientId = "test-client")
		public void handleToolListChangedWithClientId(List<McpSchema.Tool> updatedTools) {
			this.lastUpdatedTools = updatedTools;
		}

		// This method is not annotated and should be ignored
		public void notAnnotatedMethod(List<McpSchema.Tool> updatedTools) {
			// This method should be ignored
		}

	}

	@Test
	void testGetToolListChangedSpecifications() {
		ToolListChangedHandler handler = new ToolListChangedHandler();
		SyncMcpToolListChangedProvider provider = new SyncMcpToolListChangedProvider(List.of(handler));

		List<SyncToolListChangedSpecification> specifications = provider.getToolListChangedSpecifications();
		List<Consumer<List<McpSchema.Tool>>> consumers = specifications.stream()
			.map(SyncToolListChangedSpecification::toolListChangeHandler)
			.toList();

		// Should find 2 annotated methods
		assertThat(consumers).hasSize(2);
		assertThat(specifications).hasSize(2);

		// Test the first consumer
		consumers.get(0).accept(TEST_TOOLS);

		// Verify that the method was called
		assertThat(handler.lastUpdatedTools).isEqualTo(TEST_TOOLS);
		assertThat(handler.lastUpdatedTools).hasSize(2);
		assertThat(handler.lastUpdatedTools.get(0).name()).isEqualTo("test-tool-1");
		assertThat(handler.lastUpdatedTools.get(1).name()).isEqualTo("test-tool-2");

		// Test the second consumer
		consumers.get(1).accept(TEST_TOOLS);

		// Verify that the method was called
		assertThat(handler.lastUpdatedTools).isEqualTo(TEST_TOOLS);
	}

	@Test
	void testClientIdSpecifications() {
		ToolListChangedHandler handler = new ToolListChangedHandler();
		SyncMcpToolListChangedProvider provider = new SyncMcpToolListChangedProvider(List.of(handler));

		List<SyncToolListChangedSpecification> specifications = provider.getToolListChangedSpecifications();

		// Should find 2 specifications
		assertThat(specifications).hasSize(2);

		// Check client IDs
		List<String> clientIds = specifications.stream().map(SyncToolListChangedSpecification::clientId).toList();

		assertThat(clientIds).containsExactlyInAnyOrder("", "test-client");
	}

	@Test
	void testEmptyList() {
		SyncMcpToolListChangedProvider provider = new SyncMcpToolListChangedProvider(List.of());

		List<Consumer<List<McpSchema.Tool>>> consumers = provider.getToolListChangedSpecifications()
			.stream()
			.map(SyncToolListChangedSpecification::toolListChangeHandler)
			.toList();

		assertThat(consumers).isEmpty();
	}

	@Test
	void testMultipleObjects() {
		ToolListChangedHandler handler1 = new ToolListChangedHandler();
		ToolListChangedHandler handler2 = new ToolListChangedHandler();
		SyncMcpToolListChangedProvider provider = new SyncMcpToolListChangedProvider(List.of(handler1, handler2));

		List<Consumer<List<McpSchema.Tool>>> consumers = provider.getToolListChangedSpecifications()
			.stream()
			.map(SyncToolListChangedSpecification::toolListChangeHandler)
			.toList();

		// Should find 4 annotated methods (2 from each handler)
		assertThat(consumers).hasSize(4);
	}

	@Test
	void testConsumerFunctionality() {
		ToolListChangedHandler handler = new ToolListChangedHandler();
		SyncMcpToolListChangedProvider provider = new SyncMcpToolListChangedProvider(List.of(handler));

		List<SyncToolListChangedSpecification> specifications = provider.getToolListChangedSpecifications();
		Consumer<List<McpSchema.Tool>> consumer = specifications.get(0).toolListChangeHandler();

		// Test with empty list
		List<McpSchema.Tool> emptyList = List.of();
		consumer.accept(emptyList);
		assertThat(handler.lastUpdatedTools).isEqualTo(emptyList);
		assertThat(handler.lastUpdatedTools).isEmpty();

		// Test with test tools
		consumer.accept(TEST_TOOLS);
		assertThat(handler.lastUpdatedTools).isEqualTo(TEST_TOOLS);
		assertThat(handler.lastUpdatedTools).hasSize(2);
	}

	@Test
	void testNonAnnotatedMethodsIgnored() {
		ToolListChangedHandler handler = new ToolListChangedHandler();
		SyncMcpToolListChangedProvider provider = new SyncMcpToolListChangedProvider(List.of(handler));

		List<SyncToolListChangedSpecification> specifications = provider.getToolListChangedSpecifications();

		// Should only find annotated methods, not the non-annotated one
		assertThat(specifications).hasSize(2);
	}

}
