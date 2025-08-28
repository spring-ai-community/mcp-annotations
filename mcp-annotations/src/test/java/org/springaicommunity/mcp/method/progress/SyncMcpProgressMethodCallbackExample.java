/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.progress;

import java.util.function.Consumer;

import org.springaicommunity.mcp.annotation.McpProgress;

import io.modelcontextprotocol.spec.McpSchema.ProgressNotification;

/**
 * Example demonstrating the usage of {@link SyncMcpProgressMethodCallback}.
 *
 * @author Christian Tzolov
 */
public class SyncMcpProgressMethodCallbackExample {

	/**
	 * Example service that handles progress notifications.
	 */
	public static class ProgressService {

		private int notificationCount = 0;

		/**
		 * Handle progress notification with the full notification object.
		 * @param notification the progress notification
		 */
		@McpProgress(clientId = "my-client-id")
		public void handleProgressNotification(ProgressNotification notification) {
			notificationCount++;
			System.out.printf("Progress Update #%d: Token=%s, Progress=%.2f%%, Total=%.0f, Message=%s%n",
					notificationCount, notification.progressToken(), notification.progress() * 100,
					notification.total(), notification.message());
		}

		/**
		 * Handle progress notification with individual parameters.
		 * @param progress the progress value (0.0 to 1.0)
		 * @param progressToken the progress token identifier
		 * @param total the total value as string
		 */
		@McpProgress(clientId = "my-client-id")
		public void handleProgressWithParams(Double progress, String progressToken, String total) {
			System.out.printf("Progress: %.2f%% for token %s (Total: %s)%n", progress * 100, progressToken, total);
		}

		/**
		 * Handle progress with primitive double.
		 * @param progress the progress value (0.0 to 1.0)
		 * @param progressToken the progress token identifier
		 * @param total the total value as string
		 */
		@McpProgress(clientId = "my-client-id")
		public void handleProgressPrimitive(double progress, String progressToken, String total) {
			System.out.printf("Processing: %.1f%% complete (Token: %s)%n", progress * 100, progressToken);
		}

		public int getNotificationCount() {
			return notificationCount;
		}

	}

	public static void main(String[] args) throws Exception {
		// Create the service instance
		ProgressService service = new ProgressService();

		// Build the callback for the notification method
		Consumer<ProgressNotification> notificationCallback = SyncMcpProgressMethodCallback.builder()
			.method(ProgressService.class.getMethod("handleProgressNotification", ProgressNotification.class))
			.bean(service)
			.build();

		// Build the callback for the params method
		Consumer<ProgressNotification> paramsCallback = SyncMcpProgressMethodCallback.builder()
			.method(ProgressService.class.getMethod("handleProgressWithParams", Double.class, String.class,
					String.class))
			.bean(service)
			.build();

		// Build the callback for the primitive method
		Consumer<ProgressNotification> primitiveCallback = SyncMcpProgressMethodCallback.builder()
			.method(ProgressService.class.getMethod("handleProgressPrimitive", double.class, String.class,
					String.class))
			.bean(service)
			.build();

		// Simulate progress notifications
		System.out.println("=== Progress Notification Example ===");

		// Start of operation
		ProgressNotification startNotification = new ProgressNotification("task-001", 0.0, 100.0,
				"Starting operation...");
		notificationCallback.accept(startNotification);

		// Progress updates
		ProgressNotification progressNotification1 = new ProgressNotification("task-001", 0.25, 100.0,
				"Processing batch 1...");
		paramsCallback.accept(progressNotification1);

		ProgressNotification progressNotification2 = new ProgressNotification("task-001", 0.5, 100.0,
				"Halfway through...");
		primitiveCallback.accept(progressNotification2);

		ProgressNotification progressNotification3 = new ProgressNotification("task-001", 0.75, 100.0,
				"Processing batch 3...");
		notificationCallback.accept(progressNotification3);

		// Completion
		ProgressNotification completeNotification = new ProgressNotification("task-001", 1.0, 100.0,
				"Operation completed successfully!");
		notificationCallback.accept(completeNotification);

		System.out.printf("%nTotal notifications handled: %d%n", service.getNotificationCount());
	}

}
