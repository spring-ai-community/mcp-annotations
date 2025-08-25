/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.provider.logging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpLogging;
import org.springaicommunity.mcp.method.logging.AsyncLoggingSpecification;

import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import reactor.core.publisher.Mono;

/**
 * Tests for {@link AsyncMcpLoggingProvider}.
 *
 * @author Christian Tzolov
 */
public class AsyncMcpLoggingProviderTests {

	/**
	 * Test class with logging consumer methods.
	 */
	static class TestAsyncLoggingProvider {

		private LoggingMessageNotification lastNotification;

		private LoggingLevel lastLevel;

		private String lastLogger;

		private String lastData;

		@McpLogging
		public Mono<Void> handleLoggingMessage(LoggingMessageNotification notification) {
			return Mono.fromRunnable(() -> {
				this.lastNotification = notification;
			});
		}

		@McpLogging
		public Mono<Void> handleLoggingMessageWithParams(LoggingLevel level, String logger, String data) {
			return Mono.fromRunnable(() -> {
				this.lastLevel = level;
				this.lastLogger = logger;
				this.lastData = data;
			});
		}

		@McpLogging
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
		TestAsyncLoggingProvider loggingHandler = new TestAsyncLoggingProvider();
		AsyncMcpLoggingProvider provider = new AsyncMcpLoggingProvider(List.of(loggingHandler));

		List<AsyncLoggingSpecification> specifications = provider.getLoggingSpecifications();
		List<Function<LoggingMessageNotification, Mono<Void>>> consumers = specifications.stream()
			.map(AsyncLoggingSpecification::loggingHandler)
			.toList();

		// Should find 3 annotated methods
		assertThat(consumers).hasSize(3);

		// Test the first consumer (Mono return type)
		LoggingMessageNotification notification = new LoggingMessageNotification(LoggingLevel.INFO, "test-logger",
				"This is a test message");

		consumers.get(0).apply(notification).block();

		// Verify that the method was called
		assertThat(loggingHandler.lastNotification).isEqualTo(notification);

		// Reset the state
		loggingHandler.lastNotification = null;

		// Test the second consumer (Mono return type with parameters)
		consumers.get(1).apply(notification).block();

		// Verify that the method was called
		assertThat(loggingHandler.lastLevel).isEqualTo(notification.level());
		assertThat(loggingHandler.lastLogger).isEqualTo(notification.logger());
		assertThat(loggingHandler.lastData).isEqualTo(notification.data());

		// Test the third consumer (void return type)
		consumers.get(2).apply(notification).block();

		// Verify that the method was called
		assertThat(loggingHandler.lastNotification).isEqualTo(notification);
	}

	@Test
	void testEmptyList() {
		AsyncMcpLoggingProvider provider = new AsyncMcpLoggingProvider(List.of());

		List<AsyncLoggingSpecification> specifications = provider.getLoggingSpecifications();

		List<Function<LoggingMessageNotification, Mono<Void>>> consumers = specifications.stream()
			.map(AsyncLoggingSpecification::loggingHandler)
			.toList();

		assertThat(consumers).isEmpty();
	}

	@Test
	void testMultipleObjects() {
		TestAsyncLoggingProvider handler1 = new TestAsyncLoggingProvider();
		TestAsyncLoggingProvider handler2 = new TestAsyncLoggingProvider();
		AsyncMcpLoggingProvider provider = new AsyncMcpLoggingProvider(List.of(handler1, handler2));

		List<AsyncLoggingSpecification> specifications = provider.getLoggingSpecifications();

		List<Function<LoggingMessageNotification, Mono<Void>>> consumers = specifications.stream()
			.map(AsyncLoggingSpecification::loggingHandler)
			.toList();

		// Should find 6 annotated methods (3 from each handler)
		assertThat(consumers).hasSize(6);
	}

}
