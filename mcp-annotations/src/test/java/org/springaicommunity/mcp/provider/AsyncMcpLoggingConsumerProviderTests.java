/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.provider;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpLoggingConsumer;
import org.springaicommunity.mcp.provider.AsyncMcpLoggingConsumerProvider;

import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Tests for {@link AsyncMcpLoggingConsumerProvider}.
 *
 * @author Christian Tzolov
 */
public class AsyncMcpLoggingConsumerProviderTests {

	/**
	 * Test class with logging consumer methods.
	 */
	static class AsyncLoggingHandler {

		private LoggingMessageNotification lastNotification;

		private LoggingLevel lastLevel;

		private String lastLogger;

		private String lastData;

		@McpLoggingConsumer
		public Mono<Void> handleLoggingMessage(LoggingMessageNotification notification) {
			return Mono.fromRunnable(() -> {
				this.lastNotification = notification;
			});
		}

		@McpLoggingConsumer
		public Mono<Void> handleLoggingMessageWithParams(LoggingLevel level, String logger, String data) {
			return Mono.fromRunnable(() -> {
				this.lastLevel = level;
				this.lastLogger = logger;
				this.lastData = data;
			});
		}

		@McpLoggingConsumer
		public void handleLoggingMessageVoid(LoggingMessageNotification notification) {
			this.lastNotification = notification;
		}

		// This method is not annotated and should be ignored
		public Mono<Void> notAnnotatedMethod(LoggingMessageNotification notification) {
			return Mono.empty();
		}

	}

	@Test
	void testGetLoggingConsumers() {
		AsyncLoggingHandler loggingHandler = new AsyncLoggingHandler();
		AsyncMcpLoggingConsumerProvider provider = new AsyncMcpLoggingConsumerProvider(List.of(loggingHandler));

		List<Function<LoggingMessageNotification, Mono<Void>>> consumers = provider.getLoggingConsumers();

		// Should find 3 annotated methods
		assertThat(consumers).hasSize(3);

		// Test the first consumer (Mono return type)
		LoggingMessageNotification notification = new LoggingMessageNotification(LoggingLevel.INFO, "test-logger",
				"This is a test message");

		StepVerifier.create(consumers.get(0).apply(notification)).verifyComplete();

		// Verify that the method was called
		assertThat(loggingHandler.lastNotification).isEqualTo(notification);

		// Reset the state
		loggingHandler.lastNotification = null;

		// Test the second consumer (Mono return type with parameters)
		StepVerifier.create(consumers.get(1).apply(notification)).verifyComplete();

		// Verify that the method was called
		assertThat(loggingHandler.lastLevel).isEqualTo(notification.level());
		assertThat(loggingHandler.lastLogger).isEqualTo(notification.logger());
		assertThat(loggingHandler.lastData).isEqualTo(notification.data());

		// Test the third consumer (void return type)
		StepVerifier.create(consumers.get(2).apply(notification)).verifyComplete();

		// Verify that the method was called
		assertThat(loggingHandler.lastNotification).isEqualTo(notification);
	}

	@Test
	void testEmptyList() {
		AsyncMcpLoggingConsumerProvider provider = new AsyncMcpLoggingConsumerProvider(List.of());

		List<Function<LoggingMessageNotification, Mono<Void>>> consumers = provider.getLoggingConsumers();

		assertThat(consumers).isEmpty();
	}

	@Test
	void testMultipleObjects() {
		AsyncLoggingHandler handler1 = new AsyncLoggingHandler();
		AsyncLoggingHandler handler2 = new AsyncLoggingHandler();
		AsyncMcpLoggingConsumerProvider provider = new AsyncMcpLoggingConsumerProvider(List.of(handler1, handler2));

		List<Function<LoggingMessageNotification, Mono<Void>>> consumers = provider.getLoggingConsumers();

		// Should find 6 annotated methods (3 from each handler)
		assertThat(consumers).hasSize(6);
	}

}
