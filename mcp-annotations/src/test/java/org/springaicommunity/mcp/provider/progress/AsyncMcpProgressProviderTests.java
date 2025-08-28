/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.provider.progress;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpProgress;
import org.springaicommunity.mcp.method.progress.AsyncProgressSpecification;

import io.modelcontextprotocol.spec.McpSchema.ProgressNotification;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Tests for {@link AsyncMcpProgressProvider}.
 *
 * @author Christian Tzolov
 */
public class AsyncMcpProgressProviderTests {

	/**
	 * Test class with async progress handler methods.
	 */
	static class AsyncProgressHandler {

		private ProgressNotification lastNotification;

		private Double lastProgress;

		private String lastProgressToken;

		private String lastTotal;

		@McpProgress(clientId = "my-client-id")
		public void handleProgressVoid(ProgressNotification notification) {
			this.lastNotification = notification;
		}

		@McpProgress(clientId = "my-client-id")
		public Mono<Void> handleProgressMono(ProgressNotification notification) {
			this.lastNotification = notification;
			return Mono.empty();
		}

		@McpProgress(clientId = "my-client-id")
		public void handleProgressWithParams(Double progress, String progressToken, String total) {
			this.lastProgress = progress;
			this.lastProgressToken = progressToken;
			this.lastTotal = total;
		}

		@McpProgress(clientId = "my-client-id")
		public Mono<Void> handleProgressWithParamsMono(Double progress, String progressToken, String total) {
			this.lastProgress = progress;
			this.lastProgressToken = progressToken;
			this.lastTotal = total;
			return Mono.empty();
		}

		@McpProgress(clientId = "my-client-id")
		public void handleProgressWithPrimitiveDouble(double progress, String progressToken, String total) {
			this.lastProgress = progress;
			this.lastProgressToken = progressToken;
			this.lastTotal = total;
		}

		// This method is not annotated and should be ignored
		public Mono<Void> notAnnotatedMethod(ProgressNotification notification) {
			// This method should be ignored
			return Mono.empty();
		}

		// This method has invalid return type and should be ignored
		@McpProgress(clientId = "my-client-id")
		public String invalidReturnType(ProgressNotification notification) {
			return "Invalid";
		}

		// This method has invalid Mono return type and should be ignored
		@McpProgress(clientId = "my-client-id")
		public Mono<String> invalidMonoReturnType(ProgressNotification notification) {
			return Mono.just("Invalid");
		}

	}

	@Test
	void testGetProgressSpecifications() {
		AsyncProgressHandler progressHandler = new AsyncProgressHandler();
		AsyncMcpProgressProvider provider = new AsyncMcpProgressProvider(List.of(progressHandler));

		List<AsyncProgressSpecification> specifications = provider.getProgressSpecifications();
		List<Function<ProgressNotification, Mono<Void>>> handlers = specifications.stream()
			.map(AsyncProgressSpecification::progressHandler)
			.toList();

		// Should find 2 valid annotated methods (only Mono<Void> methods are valid for
		// async)
		assertThat(handlers).hasSize(2);

		// Test the first handler (Mono<Void> method)
		ProgressNotification notification = new ProgressNotification("test-token-123", 0.5, 100.0,
				"Test progress message");

		StepVerifier.create(handlers.get(0).apply(notification)).verifyComplete();
		assertThat(progressHandler.lastNotification).isEqualTo(notification);

		// Reset
		progressHandler.lastNotification = null;
		progressHandler.lastProgress = null;
		progressHandler.lastProgressToken = null;
		progressHandler.lastTotal = null;

		// Test the second handler (Mono<Void> with params)
		StepVerifier.create(handlers.get(1).apply(notification)).verifyComplete();
		assertThat(progressHandler.lastProgress).isEqualTo(notification.progress());
		assertThat(progressHandler.lastProgressToken).isEqualTo(notification.progressToken());
		assertThat(progressHandler.lastTotal).isEqualTo(String.valueOf(notification.total()));
	}

	@Test
	void testEmptyList() {
		AsyncMcpProgressProvider provider = new AsyncMcpProgressProvider(List.of());

		List<Function<ProgressNotification, Mono<Void>>> handlers = provider.getProgressSpecifications()
			.stream()
			.map(AsyncProgressSpecification::progressHandler)
			.toList();

		assertThat(handlers).isEmpty();
	}

	@Test
	void testMultipleObjects() {
		AsyncProgressHandler handler1 = new AsyncProgressHandler();
		AsyncProgressHandler handler2 = new AsyncProgressHandler();
		AsyncMcpProgressProvider provider = new AsyncMcpProgressProvider(List.of(handler1, handler2));

		List<Function<ProgressNotification, Mono<Void>>> handlers = provider.getProgressSpecifications()
			.stream()
			.map(AsyncProgressSpecification::progressHandler)
			.toList();

		// Should find 4 valid annotated methods (2 from each handler - only Mono<Void>
		// methods)
		assertThat(handlers).hasSize(4);
	}

	@Test
	void testNullProgressObjects() {
		AsyncMcpProgressProvider provider = new AsyncMcpProgressProvider(null);

		List<Function<ProgressNotification, Mono<Void>>> handlers = provider.getProgressSpecifications()
			.stream()
			.map(AsyncProgressSpecification::progressHandler)
			.toList();

		assertThat(handlers).isEmpty();
	}

	@Test
	void testClientIdExtraction() {
		AsyncProgressHandler handler = new AsyncProgressHandler();
		AsyncMcpProgressProvider provider = new AsyncMcpProgressProvider(List.of(handler));

		List<AsyncProgressSpecification> specifications = provider.getProgressSpecifications();

		// All specifications should have non-empty clientId
		assertThat(specifications).allMatch(spec -> !spec.clientId().isEmpty());
	}

	@Test
	void testErrorHandling() {
		// Test class with method that throws an exception
		class ErrorHandler {

			@McpProgress(clientId = "my-client-id")
			public Mono<Void> handleProgressWithError(ProgressNotification notification) {
				return Mono.error(new RuntimeException("Test error"));
			}

		}

		ErrorHandler errorHandler = new ErrorHandler();
		AsyncMcpProgressProvider provider = new AsyncMcpProgressProvider(List.of(errorHandler));

		List<Function<ProgressNotification, Mono<Void>>> handlers = provider.getProgressSpecifications()
			.stream()
			.map(AsyncProgressSpecification::progressHandler)
			.toList();

		assertThat(handlers).hasSize(1);

		ProgressNotification notification = new ProgressNotification("error-token", 0.5, 100.0, "Error test");

		// Verify that the error is propagated correctly
		StepVerifier.create(handlers.get(0).apply(notification)).expectError(RuntimeException.class).verify();
	}

}
