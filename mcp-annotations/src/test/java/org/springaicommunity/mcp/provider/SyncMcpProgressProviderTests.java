/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.provider;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpProgress;
import org.springaicommunity.mcp.method.progress.SyncProgressSpecification;

import io.modelcontextprotocol.spec.McpSchema.ProgressNotification;

/**
 * Tests for {@link SyncMcpProgressProvider}.
 *
 * @author Christian Tzolov
 */
public class SyncMcpProgressProviderTests {

	/**
	 * Test class with progress handler methods.
	 */
	static class ProgressHandler {

		private ProgressNotification lastNotification;

		private Double lastProgress;

		private String lastProgressToken;

		private String lastTotal;

		@McpProgress
		public void handleProgressNotification(ProgressNotification notification) {
			this.lastNotification = notification;
		}

		@McpProgress
		public void handleProgressWithParams(Double progress, String progressToken, String total) {
			this.lastProgress = progress;
			this.lastProgressToken = progressToken;
			this.lastTotal = total;
		}

		@McpProgress
		public void handleProgressWithPrimitiveDouble(double progress, String progressToken, String total) {
			this.lastProgress = progress;
			this.lastProgressToken = progressToken;
			this.lastTotal = total;
		}

		// This method is not annotated and should be ignored
		public void notAnnotatedMethod(ProgressNotification notification) {
			// This method should be ignored
		}

		// This method has invalid return type and should be ignored
		@McpProgress
		public String invalidReturnType(ProgressNotification notification) {
			return "Invalid";
		}

	}

	@Test
	void testGetProgressSpecifications() {
		ProgressHandler progressHandler = new ProgressHandler();
		SyncMcpProgressProvider provider = new SyncMcpProgressProvider(List.of(progressHandler));

		List<SyncProgressSpecification> specifications = provider.getProgressSpecifications();
		List<Consumer<ProgressNotification>> consumers = specifications.stream()
			.map(SyncProgressSpecification::progressHandler)
			.toList();

		// Should find 3 valid annotated methods (invalid return type method is filtered
		// out)
		assertThat(consumers).hasSize(3);

		// Test all consumers and verify at least one sets each expected field
		ProgressNotification notification = new ProgressNotification("test-token-123", 0.5, 100.0,
				"Test progress message");

		// Call all consumers
		for (Consumer<ProgressNotification> consumer : consumers) {
			consumer.accept(notification);
		}

		// Verify that at least one method set the notification
		assertThat(progressHandler.lastNotification).isEqualTo(notification);

		// Verify that at least one method set the individual parameters
		assertThat(progressHandler.lastProgress).isEqualTo(notification.progress());
		assertThat(progressHandler.lastProgressToken).isEqualTo(notification.progressToken());
		assertThat(progressHandler.lastTotal).isEqualTo(String.valueOf(notification.total()));
	}

	@Test
	void testEmptyList() {
		SyncMcpProgressProvider provider = new SyncMcpProgressProvider(List.of());

		List<Consumer<ProgressNotification>> consumers = provider.getProgressSpecifications()
			.stream()
			.map(SyncProgressSpecification::progressHandler)
			.toList();

		assertThat(consumers).isEmpty();
	}

	@Test
	void testMultipleObjects() {
		ProgressHandler handler1 = new ProgressHandler();
		ProgressHandler handler2 = new ProgressHandler();
		SyncMcpProgressProvider provider = new SyncMcpProgressProvider(List.of(handler1, handler2));

		List<Consumer<ProgressNotification>> consumers = provider.getProgressSpecifications()
			.stream()
			.map(SyncProgressSpecification::progressHandler)
			.toList();

		// Should find 6 valid annotated methods (3 from each handler)
		assertThat(consumers).hasSize(6);
	}

	@Test
	void testNullProgressObjects() {
		SyncMcpProgressProvider provider = new SyncMcpProgressProvider(null);

		List<Consumer<ProgressNotification>> consumers = provider.getProgressSpecifications()
			.stream()
			.map(SyncProgressSpecification::progressHandler)
			.toList();

		assertThat(consumers).isEmpty();
	}

	@Test
	void testClientIdExtraction() {
		ProgressHandler handler = new ProgressHandler();
		SyncMcpProgressProvider provider = new SyncMcpProgressProvider(List.of(handler));

		List<SyncProgressSpecification> specifications = provider.getProgressSpecifications();

		// All specifications should have empty clientId (default value from annotation)
		assertThat(specifications).allMatch(spec -> spec.clientId().equals(""));
	}

}
