/*
 * Copyright 2025-2025 the original author or authors.
 */

package com.logaritex.mcp.method.logging;

import java.lang.reflect.Method;
import java.util.function.Function;

import com.logaritex.mcp.annotation.McpLoggingConsumer;
import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import reactor.core.publisher.Mono;

/**
 * Example class demonstrating the use of {@link AsyncMcpLoggingConsumerMethodCallback}.
 *
 * This class shows how to create and use an asynchronous logging consumer method
 * callback. It provides examples of methods annotated with {@link McpLoggingConsumer}
 * that can be used to handle logging message notifications in a reactive way.
 *
 * @author Christian Tzolov
 */
public class AsyncMcpLoggingConsumerMethodCallbackExample {

	/**
	 * Example method that accepts a LoggingMessageNotification and returns Mono<Void>.
	 * @param notification The logging message notification
	 * @return A Mono that completes when the processing is done
	 */
	@McpLoggingConsumer
	public Mono<Void> handleLoggingMessage(LoggingMessageNotification notification) {
		return Mono.fromRunnable(() -> {
			System.out.println("Received logging message: " + notification.level() + " - " + notification.logger()
					+ " - " + notification.data());
		});
	}

	/**
	 * Example method that accepts individual parameters (LoggingLevel, String, String)
	 * and returns Mono<Void>.
	 * @param level The logging level
	 * @param logger The logger name
	 * @param data The log message data
	 * @return A Mono that completes when the processing is done
	 */
	@McpLoggingConsumer
	public Mono<Void> handleLoggingMessageWithParams(LoggingLevel level, String logger, String data) {
		return Mono.fromRunnable(() -> {
			System.out.println("Received logging message with params: " + level + " - " + logger + " - " + data);
		});
	}

	/**
	 * Example method that accepts a LoggingMessageNotification with void return type.
	 * @param notification The logging message notification
	 */
	@McpLoggingConsumer
	public void handleLoggingMessageVoid(LoggingMessageNotification notification) {
		System.out.println("Received logging message (void): " + notification.level() + " - " + notification.logger()
				+ " - " + notification.data());
	}

	/**
	 * Example of how to create and use an AsyncMcpLoggingConsumerMethodCallback.
	 * @param args Command line arguments
	 * @throws Exception If an error occurs
	 */
	public static void main(String[] args) throws Exception {
		// Create an instance of the example class
		AsyncMcpLoggingConsumerMethodCallbackExample example = new AsyncMcpLoggingConsumerMethodCallbackExample();

		// Create a callback for the handleLoggingMessage method
		Method method1 = AsyncMcpLoggingConsumerMethodCallbackExample.class.getMethod("handleLoggingMessage",
				LoggingMessageNotification.class);
		Function<LoggingMessageNotification, Mono<Void>> callback1 = AsyncMcpLoggingConsumerMethodCallback.builder()
			.method(method1)
			.bean(example)
			.build();

		// Create a callback for the handleLoggingMessageWithParams method
		Method method2 = AsyncMcpLoggingConsumerMethodCallbackExample.class.getMethod("handleLoggingMessageWithParams",
				LoggingLevel.class, String.class, String.class);
		Function<LoggingMessageNotification, Mono<Void>> callback2 = AsyncMcpLoggingConsumerMethodCallback.builder()
			.method(method2)
			.bean(example)
			.build();

		// Create a callback for the handleLoggingMessageVoid method
		Method method3 = AsyncMcpLoggingConsumerMethodCallbackExample.class.getMethod("handleLoggingMessageVoid",
				LoggingMessageNotification.class);
		Function<LoggingMessageNotification, Mono<Void>> callback3 = AsyncMcpLoggingConsumerMethodCallback.builder()
			.method(method3)
			.bean(example)
			.build();

		// Create a sample logging message notification
		LoggingMessageNotification notification = new LoggingMessageNotification(LoggingLevel.INFO, "test-logger",
				"This is a test message");

		// Use the callbacks
		System.out.println("Using callback1:");
		callback1.apply(notification).block();

		System.out.println("\nUsing callback2:");
		callback2.apply(notification).block();

		System.out.println("\nUsing callback3 (void method):");
		callback3.apply(notification).block();
	}

}
