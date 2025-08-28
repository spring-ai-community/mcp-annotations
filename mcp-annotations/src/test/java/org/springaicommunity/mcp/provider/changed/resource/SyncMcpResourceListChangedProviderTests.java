/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.provider.changed.resource;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpResourceListChanged;
import org.springaicommunity.mcp.method.changed.resource.SyncResourceListChangedSpecification;

import io.modelcontextprotocol.spec.McpSchema;

/**
 * Tests for {@link SyncMcpResourceListChangedProvider}.
 *
 * @author Christian Tzolov
 */
public class SyncMcpResourceListChangedProviderTests {

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
		public void handleResourceListChanged(List<McpSchema.Resource> updatedResources) {
			this.lastUpdatedResources = updatedResources;
		}

		@McpResourceListChanged(clients = "test-client")
		public void handleResourceListChangedWithClientId(List<McpSchema.Resource> updatedResources) {
			this.lastUpdatedResources = updatedResources;
		}

		// This method is not annotated and should be ignored
		public void notAnnotatedMethod(List<McpSchema.Resource> updatedResources) {
			// This method should be ignored
		}

	}

	@Test
	void testGetResourceListChangedSpecifications() {
		ResourceListChangedHandler handler = new ResourceListChangedHandler();
		SyncMcpResourceListChangedProvider provider = new SyncMcpResourceListChangedProvider(List.of(handler));

		List<SyncResourceListChangedSpecification> specifications = provider.getResourceListChangedSpecifications();
		List<Consumer<List<McpSchema.Resource>>> consumers = specifications.stream()
			.map(SyncResourceListChangedSpecification::resourceListChangeHandler)
			.toList();

		// Should find 2 annotated methods
		assertThat(consumers).hasSize(2);
		assertThat(specifications).hasSize(2);

		// Test the first consumer
		consumers.get(0).accept(TEST_RESOURCES);

		// Verify that the method was called
		assertThat(handler.lastUpdatedResources).isEqualTo(TEST_RESOURCES);
		assertThat(handler.lastUpdatedResources).hasSize(2);
		assertThat(handler.lastUpdatedResources.get(0).name()).isEqualTo("test-resource-1");
		assertThat(handler.lastUpdatedResources.get(1).name()).isEqualTo("test-resource-2");

		// Test the second consumer
		consumers.get(1).accept(TEST_RESOURCES);

		// Verify that the method was called
		assertThat(handler.lastUpdatedResources).isEqualTo(TEST_RESOURCES);
	}

	@Test
	void testClientIdSpecifications() {
		ResourceListChangedHandler handler = new ResourceListChangedHandler();
		SyncMcpResourceListChangedProvider provider = new SyncMcpResourceListChangedProvider(List.of(handler));

		List<SyncResourceListChangedSpecification> specifications = provider.getResourceListChangedSpecifications();

		// Should find 2 specifications
		assertThat(specifications).hasSize(2);

		// Check client IDs
		List<String> clientIds = specifications.stream().map(spec -> spec.clients()).flatMap(Stream::of).toList();

		assertThat(clientIds).containsExactlyInAnyOrder("client1", "test-client");
	}

	@Test
	void testEmptyList() {
		SyncMcpResourceListChangedProvider provider = new SyncMcpResourceListChangedProvider(List.of());

		List<Consumer<List<McpSchema.Resource>>> consumers = provider.getResourceListChangedSpecifications()
			.stream()
			.map(SyncResourceListChangedSpecification::resourceListChangeHandler)
			.toList();

		assertThat(consumers).isEmpty();
	}

	@Test
	void testMultipleObjects() {
		ResourceListChangedHandler handler1 = new ResourceListChangedHandler();
		ResourceListChangedHandler handler2 = new ResourceListChangedHandler();
		SyncMcpResourceListChangedProvider provider = new SyncMcpResourceListChangedProvider(
				List.of(handler1, handler2));

		List<Consumer<List<McpSchema.Resource>>> consumers = provider.getResourceListChangedSpecifications()
			.stream()
			.map(SyncResourceListChangedSpecification::resourceListChangeHandler)
			.toList();

		// Should find 4 annotated methods (2 from each handler)
		assertThat(consumers).hasSize(4);
	}

	@Test
	void testConsumerFunctionality() {
		ResourceListChangedHandler handler = new ResourceListChangedHandler();
		SyncMcpResourceListChangedProvider provider = new SyncMcpResourceListChangedProvider(List.of(handler));

		List<SyncResourceListChangedSpecification> specifications = provider.getResourceListChangedSpecifications();
		Consumer<List<McpSchema.Resource>> consumer = specifications.get(0).resourceListChangeHandler();

		// Test with empty list
		List<McpSchema.Resource> emptyList = List.of();
		consumer.accept(emptyList);
		assertThat(handler.lastUpdatedResources).isEqualTo(emptyList);
		assertThat(handler.lastUpdatedResources).isEmpty();

		// Test with test resources
		consumer.accept(TEST_RESOURCES);
		assertThat(handler.lastUpdatedResources).isEqualTo(TEST_RESOURCES);
		assertThat(handler.lastUpdatedResources).hasSize(2);
	}

	@Test
	void testNonAnnotatedMethodsIgnored() {
		ResourceListChangedHandler handler = new ResourceListChangedHandler();
		SyncMcpResourceListChangedProvider provider = new SyncMcpResourceListChangedProvider(List.of(handler));

		List<SyncResourceListChangedSpecification> specifications = provider.getResourceListChangedSpecifications();

		// Should only find annotated methods, not the non-annotated one
		assertThat(specifications).hasSize(2);
	}

}
