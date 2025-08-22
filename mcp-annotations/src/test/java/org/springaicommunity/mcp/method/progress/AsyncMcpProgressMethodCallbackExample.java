/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.progress;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.springaicommunity.mcp.annotation.McpProgress;

import io.modelcontextprotocol.spec.McpSchema.ProgressNotification;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Example demonstrating the usage of {@link AsyncMcpProgressMethodCallback}.
 *
 * @author Christian Tzolov
 */
public class AsyncMcpProgressMethodCallbackExample {

	/**
	 * Example async service that handles progress notifications.
	 */
	public static class AsyncProgressService {

		private final AtomicInteger notificationCount = new AtomicInteger(0);

		/**
		 * Handle progress notification asynchronously with the full notification object.
		 * @param notification the progress notification
		 * @return Mono completing when processing is done
		 */
		@McpProgress
		public Mono<Void> handleProgressNotificationAsync(ProgressNotification notification) {
			return Mono.fromRunnable(() -> {
				int count = notificationCount.incrementAndGet();
				System.out.printf("[Async] Progress Update #%d: Token=%s, Progress=%.2f%%, Total=%.0f, Message=%s%n",
						count, notification.progressToken(), notification.progress() * 100, notification.total(),
						notification.message());
			})
				.delayElement(Duration.ofMillis(100)) // Simulate async processing
				.then();
		}

		/**
		 * Handle progress notification with individual parameters returning void.
		 * @param progress the progress value (0.0 to 1.0)
		 * @param progressToken the progress token identifier
		 * @param total the total value as string
		 */
		@McpProgress
		public void handleProgressWithParams(Double progress, String progressToken, String total) {
			System.out.printf("[Sync in Async] Progress: %.2f%% for token %s (Total: %s)%n", progress * 100,
					progressToken, total);
		}

		/**
		 * Handle progress asynchronously with individual parameters.
		 * @param progress the progress value (0.0 to 1.0)
		 * @param progressToken the progress token identifier
		 * @param total the total value as string
		 * @return Mono completing when processing is done
		 */
		@McpProgress
		public Mono<Void> handleProgressWithParamsAsync(Double progress, String progressToken, String total) {
			return Mono.fromRunnable(() -> {
				System.out.printf("[Async Params] Progress: %.2f%% for token %s (Total: %s)%n", progress * 100,
						progressToken, total);
			}).delayElement(Duration.ofMillis(50)).then();
		}

		/**
		 * Handle progress with primitive double.
		 * @param progress the progress value (0.0 to 1.0)
		 * @param progressToken the progress token identifier
		 * @param total the total value as string
		 */
		@McpProgress
		public void handleProgressPrimitive(double progress, String progressToken, String total) {
			System.out.printf("[Primitive] Processing: %.1f%% complete (Token: %s)%n", progress * 100, progressToken);
		}

		public int getNotificationCount() {
			return notificationCount.get();
		}

	}

	public static void main(String[] args) throws Exception {
		// Create the service instance
		AsyncProgressService service = new AsyncProgressService();

		// Build the async callback for the notification method
		Function<ProgressNotification, Mono<Void>> asyncNotificationCallback = AsyncMcpProgressMethodCallback.builder()
			.method(AsyncProgressService.class.getMethod("handleProgressNotificationAsync", ProgressNotification.class))
			.bean(service)
			.build();

		// Build the callback for the sync params method
		Function<ProgressNotification, Mono<Void>> syncParamsCallback = AsyncMcpProgressMethodCallback.builder()
			.method(AsyncProgressService.class.getMethod("handleProgressWithParams", Double.class, String.class,
					String.class))
			.bean(service)
			.build();

		// Build the async callback for the params method
		Function<ProgressNotification, Mono<Void>> asyncParamsCallback = AsyncMcpProgressMethodCallback.builder()
			.method(AsyncProgressService.class.getMethod("handleProgressWithParamsAsync", Double.class, String.class,
					String.class))
			.bean(service)
			.build();

		// Build the callback for the primitive method
		Function<ProgressNotification, Mono<Void>> primitiveCallback = AsyncMcpProgressMethodCallback.builder()
			.method(AsyncProgressService.class.getMethod("handleProgressPrimitive", double.class, String.class,
					String.class))
			.bean(service)
			.build();

		System.out.println("=== Async Progress Notification Example ===");

		// Create a flux of progress notifications
		Flux<ProgressNotification> progressFlux = Flux.just(
				new ProgressNotification("async-task-001", 0.0, 100.0, "Starting async operation..."),
				new ProgressNotification("async-task-001", 0.25, 100.0, "Processing batch 1..."),
				new ProgressNotification("async-task-001", 0.5, 100.0, "Halfway through..."),
				new ProgressNotification("async-task-001", 0.75, 100.0, "Processing batch 3..."),
				new ProgressNotification("async-task-001", 1.0, 100.0, "Operation completed successfully!"));

		// Process notifications with different callbacks
		Mono<Void> processing = progressFlux.index().flatMap(indexed -> {
			Long index = indexed.getT1();
			ProgressNotification notification = indexed.getT2();

			// Use different callbacks based on index
			if (index == 0) {
				return asyncNotificationCallback.apply(notification);
			}
			else if (index == 1) {
				return syncParamsCallback.apply(notification);
			}
			else if (index == 2) {
				return asyncParamsCallback.apply(notification);
			}
			else if (index == 3) {
				return primitiveCallback.apply(notification);
			}
			else {
				return asyncNotificationCallback.apply(notification);
			}
		}).then();

		// Block and wait for all processing to complete
		System.out.println("Processing notifications asynchronously...");
		processing.block();

		System.out.printf("%nTotal async notifications handled: %d%n", service.getNotificationCount());

		// Demonstrate concurrent processing
		System.out.println("\n=== Concurrent Progress Processing ===");

		Flux<ProgressNotification> concurrentNotifications = Flux.range(1, 5)
			.map(i -> new ProgressNotification("concurrent-task-" + i, i * 0.2, 100.0, "Processing task " + i));

		concurrentNotifications
			.flatMap(notification -> asyncNotificationCallback.apply(notification)
				.doOnSubscribe(s -> System.out.println("Starting: " + notification.progressToken()))
				.doOnSuccess(v -> System.out.println("Completed: " + notification.progressToken())))
			.blockLast();

		System.out.println("\nAll async operations completed!");
	}

}
