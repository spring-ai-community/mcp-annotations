/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.provider.changed.prompt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpPromptListChanged;
import org.springaicommunity.mcp.method.changed.prompt.SyncPromptListChangedSpecification;

import io.modelcontextprotocol.spec.McpSchema;

/**
 * Tests for {@link SyncMcpPromptListChangedProvider}.
 *
 * @author Christian Tzolov
 */
public class SyncMcpPromptListChangedProviderTests {

	private static final List<McpSchema.Prompt> TEST_PROMPTS = List.of(
			new McpSchema.Prompt("test-prompt-1", "Test Prompt 1", List.of()),
			new McpSchema.Prompt("test-prompt-2", "Test Prompt 2", List.of()));

	/**
	 * Test class with prompt list changed consumer methods.
	 */
	static class PromptListChangedHandler {

		private List<McpSchema.Prompt> lastUpdatedPrompts;

		@McpPromptListChanged
		public void handlePromptListChanged(List<McpSchema.Prompt> updatedPrompts) {
			this.lastUpdatedPrompts = updatedPrompts;
		}

		@McpPromptListChanged(clientId = "test-client")
		public void handlePromptListChangedWithClientId(List<McpSchema.Prompt> updatedPrompts) {
			this.lastUpdatedPrompts = updatedPrompts;
		}

		// This method is not annotated and should be ignored
		public void notAnnotatedMethod(List<McpSchema.Prompt> updatedPrompts) {
			// This method should be ignored
		}

	}

	@Test
	void testGetPromptListChangedSpecifications() {
		PromptListChangedHandler handler = new PromptListChangedHandler();
		SyncMcpPromptListChangedProvider provider = new SyncMcpPromptListChangedProvider(List.of(handler));

		List<SyncPromptListChangedSpecification> specifications = provider.getPromptListChangedSpecifications();
		List<Consumer<List<McpSchema.Prompt>>> consumers = specifications.stream()
			.map(SyncPromptListChangedSpecification::promptListChangeHandler)
			.toList();

		// Should find 2 annotated methods
		assertThat(consumers).hasSize(2);
		assertThat(specifications).hasSize(2);

		// Test the first consumer
		consumers.get(0).accept(TEST_PROMPTS);

		// Verify that the method was called
		assertThat(handler.lastUpdatedPrompts).isEqualTo(TEST_PROMPTS);
		assertThat(handler.lastUpdatedPrompts).hasSize(2);
		assertThat(handler.lastUpdatedPrompts.get(0).name()).isEqualTo("test-prompt-1");
		assertThat(handler.lastUpdatedPrompts.get(1).name()).isEqualTo("test-prompt-2");

		// Test the second consumer
		consumers.get(1).accept(TEST_PROMPTS);

		// Verify that the method was called
		assertThat(handler.lastUpdatedPrompts).isEqualTo(TEST_PROMPTS);
	}

	@Test
	void testClientIdSpecifications() {
		PromptListChangedHandler handler = new PromptListChangedHandler();
		SyncMcpPromptListChangedProvider provider = new SyncMcpPromptListChangedProvider(List.of(handler));

		List<SyncPromptListChangedSpecification> specifications = provider.getPromptListChangedSpecifications();

		// Should find 2 specifications
		assertThat(specifications).hasSize(2);

		// Check client IDs
		List<String> clientIds = specifications.stream().map(SyncPromptListChangedSpecification::clientId).toList();

		assertThat(clientIds).containsExactlyInAnyOrder("", "test-client");
	}

	@Test
	void testEmptyList() {
		SyncMcpPromptListChangedProvider provider = new SyncMcpPromptListChangedProvider(List.of());

		List<Consumer<List<McpSchema.Prompt>>> consumers = provider.getPromptListChangedSpecifications()
			.stream()
			.map(SyncPromptListChangedSpecification::promptListChangeHandler)
			.toList();

		assertThat(consumers).isEmpty();
	}

	@Test
	void testMultipleObjects() {
		PromptListChangedHandler handler1 = new PromptListChangedHandler();
		PromptListChangedHandler handler2 = new PromptListChangedHandler();
		SyncMcpPromptListChangedProvider provider = new SyncMcpPromptListChangedProvider(List.of(handler1, handler2));

		List<Consumer<List<McpSchema.Prompt>>> consumers = provider.getPromptListChangedSpecifications()
			.stream()
			.map(SyncPromptListChangedSpecification::promptListChangeHandler)
			.toList();

		// Should find 4 annotated methods (2 from each handler)
		assertThat(consumers).hasSize(4);
	}

	@Test
	void testConsumerFunctionality() {
		PromptListChangedHandler handler = new PromptListChangedHandler();
		SyncMcpPromptListChangedProvider provider = new SyncMcpPromptListChangedProvider(List.of(handler));

		List<SyncPromptListChangedSpecification> specifications = provider.getPromptListChangedSpecifications();
		Consumer<List<McpSchema.Prompt>> consumer = specifications.get(0).promptListChangeHandler();

		// Test with empty list
		List<McpSchema.Prompt> emptyList = List.of();
		consumer.accept(emptyList);
		assertThat(handler.lastUpdatedPrompts).isEqualTo(emptyList);
		assertThat(handler.lastUpdatedPrompts).isEmpty();

		// Test with test prompts
		consumer.accept(TEST_PROMPTS);
		assertThat(handler.lastUpdatedPrompts).isEqualTo(TEST_PROMPTS);
		assertThat(handler.lastUpdatedPrompts).hasSize(2);
	}

	@Test
	void testNonAnnotatedMethodsIgnored() {
		PromptListChangedHandler handler = new PromptListChangedHandler();
		SyncMcpPromptListChangedProvider provider = new SyncMcpPromptListChangedProvider(List.of(handler));

		List<SyncPromptListChangedSpecification> specifications = provider.getPromptListChangedSpecifications();

		// Should only find annotated methods, not the non-annotated one
		assertThat(specifications).hasSize(2);
	}

}
