/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.provider.changed.prompt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpPromptListChanged;
import org.springaicommunity.mcp.method.changed.prompt.AsyncPromptListChangedSpecification;

import io.modelcontextprotocol.spec.McpSchema;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Tests for {@link AsyncMcpPromptListChangedProvider}.
 *
 * @author Christian Tzolov
 */
public class AsyncMcpPromptListChangedProviderTests {

	private static final List<McpSchema.Prompt> TEST_PROMPTS = List.of(
			new McpSchema.Prompt("test-prompt-1", "Test Prompt 1", List.of()),
			new McpSchema.Prompt("test-prompt-2", "Test Prompt 2", List.of()));

	/**
	 * Test class with prompt list changed consumer methods.
	 */
	static class PromptListChangedHandler {

		private List<McpSchema.Prompt> lastUpdatedPrompts;

		@McpPromptListChanged
		public Mono<Void> handlePromptListChanged(List<McpSchema.Prompt> updatedPrompts) {
			return Mono.fromRunnable(() -> {
				this.lastUpdatedPrompts = updatedPrompts;
			});
		}

		@McpPromptListChanged(clientId = "test-client")
		public Mono<Void> handlePromptListChangedWithClientId(List<McpSchema.Prompt> updatedPrompts) {
			return Mono.fromRunnable(() -> {
				this.lastUpdatedPrompts = updatedPrompts;
			});
		}

		@McpPromptListChanged
		public void handlePromptListChangedVoid(List<McpSchema.Prompt> updatedPrompts) {
			this.lastUpdatedPrompts = updatedPrompts;
		}

		// This method is not annotated and should be ignored
		public Mono<Void> notAnnotatedMethod(List<McpSchema.Prompt> updatedPrompts) {
			return Mono.empty();
		}

	}

	@Test
	void testGetPromptListChangedSpecifications() {
		PromptListChangedHandler handler = new PromptListChangedHandler();
		AsyncMcpPromptListChangedProvider provider = new AsyncMcpPromptListChangedProvider(List.of(handler));

		List<AsyncPromptListChangedSpecification> specifications = provider.getPromptListChangedSpecifications();
		List<Function<List<McpSchema.Prompt>, Mono<Void>>> consumers = specifications.stream()
			.map(AsyncPromptListChangedSpecification::promptListChangeHandler)
			.toList();

		// Should find 3 annotated methods (2 Mono<Void> + 1 void)
		assertThat(consumers).hasSize(3);
		assertThat(specifications).hasSize(3);

		// Test the first consumer
		StepVerifier.create(consumers.get(0).apply(TEST_PROMPTS)).verifyComplete();

		// Verify that the method was called
		assertThat(handler.lastUpdatedPrompts).isEqualTo(TEST_PROMPTS);
		assertThat(handler.lastUpdatedPrompts).hasSize(2);
		assertThat(handler.lastUpdatedPrompts.get(0).name()).isEqualTo("test-prompt-1");
		assertThat(handler.lastUpdatedPrompts.get(1).name()).isEqualTo("test-prompt-2");

		// Test the second consumer
		StepVerifier.create(consumers.get(1).apply(TEST_PROMPTS)).verifyComplete();

		// Verify that the method was called
		assertThat(handler.lastUpdatedPrompts).isEqualTo(TEST_PROMPTS);

		// Test the third consumer (void method)
		StepVerifier.create(consumers.get(2).apply(TEST_PROMPTS)).verifyComplete();

		// Verify that the method was called
		assertThat(handler.lastUpdatedPrompts).isEqualTo(TEST_PROMPTS);
	}

	@Test
	void testClientIdSpecifications() {
		PromptListChangedHandler handler = new PromptListChangedHandler();
		AsyncMcpPromptListChangedProvider provider = new AsyncMcpPromptListChangedProvider(List.of(handler));

		List<AsyncPromptListChangedSpecification> specifications = provider.getPromptListChangedSpecifications();

		// Should find 3 specifications
		assertThat(specifications).hasSize(3);

		// Check client IDs
		List<String> clientIds = specifications.stream().map(AsyncPromptListChangedSpecification::clientId).toList();

		assertThat(clientIds).containsExactlyInAnyOrder("", "test-client", "");
	}

	@Test
	void testEmptyList() {
		AsyncMcpPromptListChangedProvider provider = new AsyncMcpPromptListChangedProvider(List.of());

		List<Function<List<McpSchema.Prompt>, Mono<Void>>> consumers = provider.getPromptListChangedSpecifications()
			.stream()
			.map(AsyncPromptListChangedSpecification::promptListChangeHandler)
			.toList();

		assertThat(consumers).isEmpty();
	}

	@Test
	void testMultipleObjects() {
		PromptListChangedHandler handler1 = new PromptListChangedHandler();
		PromptListChangedHandler handler2 = new PromptListChangedHandler();
		AsyncMcpPromptListChangedProvider provider = new AsyncMcpPromptListChangedProvider(List.of(handler1, handler2));

		List<Function<List<McpSchema.Prompt>, Mono<Void>>> consumers = provider.getPromptListChangedSpecifications()
			.stream()
			.map(AsyncPromptListChangedSpecification::promptListChangeHandler)
			.toList();

		// Should find 6 annotated methods (3 from each handler)
		assertThat(consumers).hasSize(6);
	}

	@Test
	void testConsumerFunctionality() {
		PromptListChangedHandler handler = new PromptListChangedHandler();
		AsyncMcpPromptListChangedProvider provider = new AsyncMcpPromptListChangedProvider(List.of(handler));

		List<AsyncPromptListChangedSpecification> specifications = provider.getPromptListChangedSpecifications();
		Function<List<McpSchema.Prompt>, Mono<Void>> consumer = specifications.get(0).promptListChangeHandler();

		// Test with empty list
		List<McpSchema.Prompt> emptyList = List.of();
		StepVerifier.create(consumer.apply(emptyList)).verifyComplete();
		assertThat(handler.lastUpdatedPrompts).isEqualTo(emptyList);
		assertThat(handler.lastUpdatedPrompts).isEmpty();

		// Test with test prompts
		StepVerifier.create(consumer.apply(TEST_PROMPTS)).verifyComplete();
		assertThat(handler.lastUpdatedPrompts).isEqualTo(TEST_PROMPTS);
		assertThat(handler.lastUpdatedPrompts).hasSize(2);
	}

	@Test
	void testNonAnnotatedMethodsIgnored() {
		PromptListChangedHandler handler = new PromptListChangedHandler();
		AsyncMcpPromptListChangedProvider provider = new AsyncMcpPromptListChangedProvider(List.of(handler));

		List<AsyncPromptListChangedSpecification> specifications = provider.getPromptListChangedSpecifications();

		// Should only find annotated methods, not the non-annotated one
		assertThat(specifications).hasSize(3);
	}

	/**
	 * Test class with methods that should be filtered out (non-reactive return types).
	 */
	static class InvalidReturnTypeHandler {

		@McpPromptListChanged
		public String invalidReturnType(List<McpSchema.Prompt> updatedPrompts) {
			return "Invalid";
		}

		@McpPromptListChanged
		public int anotherInvalidReturnType(List<McpSchema.Prompt> updatedPrompts) {
			return 42;
		}

	}

	@Test
	void testInvalidReturnTypesFiltered() {
		InvalidReturnTypeHandler handler = new InvalidReturnTypeHandler();
		AsyncMcpPromptListChangedProvider provider = new AsyncMcpPromptListChangedProvider(List.of(handler));

		List<AsyncPromptListChangedSpecification> specifications = provider.getPromptListChangedSpecifications();

		// Should find no methods since they have invalid return types
		assertThat(specifications).isEmpty();
	}

	/**
	 * Test class with mixed valid and invalid methods.
	 */
	static class MixedHandler {

		private List<McpSchema.Prompt> lastUpdatedPrompts;

		@McpPromptListChanged
		public Mono<Void> validMethod(List<McpSchema.Prompt> updatedPrompts) {
			return Mono.fromRunnable(() -> {
				this.lastUpdatedPrompts = updatedPrompts;
			});
		}

		@McpPromptListChanged
		public void validVoidMethod(List<McpSchema.Prompt> updatedPrompts) {
			this.lastUpdatedPrompts = updatedPrompts;
		}

		@McpPromptListChanged
		public String invalidMethod(List<McpSchema.Prompt> updatedPrompts) {
			return "Invalid";
		}

	}

	@Test
	void testMixedValidAndInvalidMethods() {
		MixedHandler handler = new MixedHandler();
		AsyncMcpPromptListChangedProvider provider = new AsyncMcpPromptListChangedProvider(List.of(handler));

		List<AsyncPromptListChangedSpecification> specifications = provider.getPromptListChangedSpecifications();

		// Should find only the 2 valid methods (Mono<Void> and void)
		assertThat(specifications).hasSize(2);

		// Test that the valid methods work
		Function<List<McpSchema.Prompt>, Mono<Void>> consumer = specifications.get(0).promptListChangeHandler();
		StepVerifier.create(consumer.apply(TEST_PROMPTS)).verifyComplete();
		assertThat(handler.lastUpdatedPrompts).isEqualTo(TEST_PROMPTS);
	}

}
