/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.provider.logging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpLogging;
import org.springaicommunity.mcp.method.logging.SyncLoggingSpecification;

import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;

/**
 * Tests for {@link SyncMcpLogginProvider}.
 *
 * @author Christian Tzolov
 */
public class SyncMcpLoggingProviderTests {

	/**
	 * Test class with logging consumer methods.
	 */
	static class LoggingHandler {

		private LoggingMessageNotification lastNotification;

		private LoggingLevel lastLevel;

		private String lastLogger;

		private String lastData;

		@McpLogging(clients = "test-client")
		public void handleLoggingMessage(LoggingMessageNotification notification) {
			System.out.println("1");
			this.lastNotification = notification;
		}

		@McpLogging(clients = "test-client")
		public void handleLoggingMessageWithParams(LoggingLevel level, String logger, String data) {
			System.out.println("2");
			this.lastLevel = level;
			this.lastLogger = logger;
			this.lastData = data;
		}

		// This method is not annotated and should be ignored
		public void notAnnotatedMethod(LoggingMessageNotification notification) {
			// This method should be ignored
		}

	}

	@Test
	void testGetLoggingConsumers() {
		LoggingHandler loggingHandler = new LoggingHandler();
		SyncMcpLogginProvider provider = new SyncMcpLogginProvider(List.of(loggingHandler));

		List<SyncLoggingSpecification> specifications = provider.getLoggingSpecifications();
		List<Consumer<LoggingMessageNotification>> consumers = specifications.stream()
			.map(SyncLoggingSpecification::loggingHandler)
			.toList();

		// Should find 2 annotated methods
		assertThat(consumers).hasSize(2);

		// Test the first consumer
		LoggingMessageNotification notification = new LoggingMessageNotification(LoggingLevel.INFO, "test-logger",
				"This is a test message");
		consumers.get(0).accept(notification);

		// Verify that the method was called
		assertThat(loggingHandler.lastNotification).isEqualTo(notification);

		// Test the second consumer
		consumers.get(1).accept(notification);

		// Verify that the method was called
		assertThat(loggingHandler.lastLevel).isEqualTo(notification.level());
		assertThat(loggingHandler.lastLogger).isEqualTo(notification.logger());
		assertThat(loggingHandler.lastData).isEqualTo(notification.data());
	}

	@Test
	void testEmptyList() {
		SyncMcpLogginProvider provider = new SyncMcpLogginProvider(List.of());

		List<Consumer<LoggingMessageNotification>> consumers = provider.getLoggingSpecifications()
			.stream()
			.map(SyncLoggingSpecification::loggingHandler)
			.toList();

		assertThat(consumers).isEmpty();
	}

	@Test
	void testMultipleObjects() {
		LoggingHandler handler1 = new LoggingHandler();
		LoggingHandler handler2 = new LoggingHandler();
		SyncMcpLogginProvider provider = new SyncMcpLogginProvider(List.of(handler1, handler2));

		List<Consumer<LoggingMessageNotification>> consumers = provider.getLoggingSpecifications()
			.stream()
			.map(SyncLoggingSpecification::loggingHandler)
			.toList();

		// Should find 4 annotated methods (2 from each handler)
		assertThat(consumers).hasSize(4);
	}

}
