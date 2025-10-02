/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.provider.changed.resource;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpResourceListChanged;
import org.springaicommunity.mcp.method.changed.resource.AsyncResourceListChangedSpecification;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link AsyncMcpResourceListChangedProvider}.
 *
 * @author Christian Tzolov
 */
public class AsyncMcpResourceListChangedProviderTests {

	private static final List<McpSchema.Resource> TEST_RESOURCES = List.of(
			McpSchema.Resource.builder()
				.uri("file:///test1.txt")
				.name("test-resource-1")
				.description("Test Resource 1")
				.mimeType("text/plain")
				.build(),
			McpSchema.Resource.builder()
				.uri("file:///test2.txt")
				.name("test-resource-2")
				.description("Test Resource 2")
				.mimeType("text/plain")
				.build());

	/**
	 * Test class with resource list changed consumer methods.
	 */
	static class ResourceListChangedHandler {

		private List<McpSchema.Resource> lastUpdatedResources;

		@McpResourceListChanged(clients = "client1")
		public Mono<Void> handleResourceListChanged(List<McpSchema.Resource> updatedResources) {
			return Mono.fromRunnable(() -> {
				this.lastUpdatedResources = updatedResources;
			});
		}

		@McpResourceListChanged(clients = "test-client")
		public Mono<Void> handleResourceListChangedWithClientId(List<McpSchema.Resource> updatedResources) {
			return Mono.fromRunnable(() -> {
				this.lastUpdatedResources = updatedResources;
			});
		}

		@McpResourceListChanged(clients = "client1")
		public void handleResourceListChangedVoid(List<McpSchema.Resource> updatedResources) {
			this.lastUpdatedResources = updatedResources;
		}

		// This method is not annotated and should be ignored
		public Mono<Void> notAnnotatedMethod(List<McpSchema.Resource> updatedResources) {
			return Mono.empty();
		}

	}

	@Test
	void testGetResourceListChangedSpecifications() {
		ResourceListChangedHandler handler = new ResourceListChangedHandler();
		AsyncMcpResourceListChangedProvider provider = new AsyncMcpResourceListChangedProvider(List.of(handler));

		List<AsyncResourceListChangedSpecification> specifications = provider.getResourceListChangedSpecifications();
		List<Function<List<McpSchema.Resource>, Mono<Void>>> consumers = specifications.stream()
			.map(AsyncResourceListChangedSpecification::resourceListChangeHandler)
			.toList();

		// Should find 2 annotated methods (2 Mono<Void>. Ignores the void method)
		assertThat(consumers).hasSize(2);
		assertThat(specifications).hasSize(2);

		// Test the first consumer
		StepVerifier.create(consumers.get(0).apply(TEST_RESOURCES)).verifyComplete();

		// Verify that the method was called
		assertThat(handler.lastUpdatedResources).isEqualTo(TEST_RESOURCES);
		assertThat(handler.lastUpdatedResources).hasSize(2);
		assertThat(handler.lastUpdatedResources.get(0).name()).isEqualTo("test-resource-1");
		assertThat(handler.lastUpdatedResources.get(1).name()).isEqualTo("test-resource-2");

		// Test the second consumer
		StepVerifier.create(consumers.get(0).apply(TEST_RESOURCES)).verifyComplete();

		// Verify that the method was called
		assertThat(handler.lastUpdatedResources).isEqualTo(TEST_RESOURCES);

		// Test the third consumer (void method)
		StepVerifier.create(consumers.get(1).apply(TEST_RESOURCES)).verifyComplete();

		// Verify that the method was called
		assertThat(handler.lastUpdatedResources).isEqualTo(TEST_RESOURCES);
	}

	@Test
	void testClientIdSpecifications() {
		ResourceListChangedHandler handler = new ResourceListChangedHandler();
		AsyncMcpResourceListChangedProvider provider = new AsyncMcpResourceListChangedProvider(List.of(handler));

		List<AsyncResourceListChangedSpecification> specifications = provider.getResourceListChangedSpecifications();

		// Should find 3 specifications
		assertThat(specifications).hasSize(2);

		// Check client IDs
		List<String> clientIds = specifications.stream().map(spec -> spec.clients()).flatMap(Stream::of).toList();

		assertThat(clientIds).containsExactlyInAnyOrder("client1", "test-client");
	}

	@Test
	void testEmptyList() {
		AsyncMcpResourceListChangedProvider provider = new AsyncMcpResourceListChangedProvider(List.of());

		List<Function<List<McpSchema.Resource>, Mono<Void>>> consumers = provider.getResourceListChangedSpecifications()
			.stream()
			.map(AsyncResourceListChangedSpecification::resourceListChangeHandler)
			.toList();

		assertThat(consumers).isEmpty();
	}

	@Test
	void testMultipleObjects() {
		ResourceListChangedHandler handler1 = new ResourceListChangedHandler();
		ResourceListChangedHandler handler2 = new ResourceListChangedHandler();
		AsyncMcpResourceListChangedProvider provider = new AsyncMcpResourceListChangedProvider(
				List.of(handler1, handler2));

		List<Function<List<McpSchema.Resource>, Mono<Void>>> consumers = provider.getResourceListChangedSpecifications()
			.stream()
			.map(AsyncResourceListChangedSpecification::resourceListChangeHandler)
			.toList();

		// Should find 4 annotated methods (2 from each handler) drops the non-reactive
		// ones
		assertThat(consumers).hasSize(4);
	}

	@Test
	void testConsumerFunctionality() {
		ResourceListChangedHandler handler = new ResourceListChangedHandler();
		AsyncMcpResourceListChangedProvider provider = new AsyncMcpResourceListChangedProvider(List.of(handler));

		List<AsyncResourceListChangedSpecification> specifications = provider.getResourceListChangedSpecifications();
		Function<List<McpSchema.Resource>, Mono<Void>> consumer = specifications.get(0).resourceListChangeHandler();

		// Test with empty list
		List<McpSchema.Resource> emptyList = List.of();
		StepVerifier.create(consumer.apply(emptyList)).verifyComplete();
		assertThat(handler.lastUpdatedResources).isEqualTo(emptyList);
		assertThat(handler.lastUpdatedResources).isEmpty();

		// Test with test resources
		StepVerifier.create(consumer.apply(TEST_RESOURCES)).verifyComplete();
		assertThat(handler.lastUpdatedResources).isEqualTo(TEST_RESOURCES);
		assertThat(handler.lastUpdatedResources).hasSize(2);
	}

	@Test
	void testNonAnnotatedMethodsIgnored() {
		ResourceListChangedHandler handler = new ResourceListChangedHandler();
		AsyncMcpResourceListChangedProvider provider = new AsyncMcpResourceListChangedProvider(List.of(handler));

		List<AsyncResourceListChangedSpecification> specifications = provider.getResourceListChangedSpecifications();

		// Should only find annotated methods, not the non-annotated one and drops the
		// non-reactive ones
		assertThat(specifications).hasSize(2);
	}

	/**
	 * Test class with methods that should be filtered out (non-reactive return types).
	 */
	static class InvalidReturnTypeHandler {

		@McpResourceListChanged(clients = "client1")
		public String invalidReturnType(List<McpSchema.Resource> updatedResources) {
			return "Invalid";
		}

		@McpResourceListChanged(clients = "client1")
		public int anotherInvalidReturnType(List<McpSchema.Resource> updatedResources) {
			return 42;
		}

	}

	@Test
	void testInvalidReturnTypesFiltered() {
		InvalidReturnTypeHandler handler = new InvalidReturnTypeHandler();
		AsyncMcpResourceListChangedProvider provider = new AsyncMcpResourceListChangedProvider(List.of(handler));

		List<AsyncResourceListChangedSpecification> specifications = provider.getResourceListChangedSpecifications();

		// Should find no methods since they have invalid return types
		assertThat(specifications).isEmpty();
	}

	/**
	 * Test class with mixed valid and invalid methods.
	 */
	static class MixedHandler {

		private List<McpSchema.Resource> lastUpdatedResources;

		@McpResourceListChanged(clients = "client1")
		public Mono<Void> validMethod(List<McpSchema.Resource> updatedResources) {
			return Mono.fromRunnable(() -> {
				this.lastUpdatedResources = updatedResources;
			});
		}

		@McpResourceListChanged(clients = "client1")
		public void validVoidMethod(List<McpSchema.Resource> updatedResources) {
			this.lastUpdatedResources = updatedResources;
		}

		@McpResourceListChanged(clients = "client1")
		public String invalidMethod(List<McpSchema.Resource> updatedResources) {
			return "Invalid";
		}

	}

	@Test
	void testMixedValidAndInvalidMethods() {
		MixedHandler handler = new MixedHandler();
		AsyncMcpResourceListChangedProvider provider = new AsyncMcpResourceListChangedProvider(List.of(handler));

		List<AsyncResourceListChangedSpecification> specifications = provider.getResourceListChangedSpecifications();

		// Should find only 1 valid method (Mono<Void> and drop the non-reactive void)
		assertThat(specifications).hasSize(1);

		// Test that the valid methods work
		Function<List<McpSchema.Resource>, Mono<Void>> consumer = specifications.get(0).resourceListChangeHandler();
		StepVerifier.create(consumer.apply(TEST_RESOURCES)).verifyComplete();
		assertThat(handler.lastUpdatedResources).isEqualTo(TEST_RESOURCES);
	}

}
