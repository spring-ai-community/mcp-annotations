/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpLogging;

import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Tests for {@link AsyncMcpLoggingMethodCallback}.
 *
 * @author Christian Tzolov
 */
public class AsyncMcpLoggingMethodCallbackTests {

	private static final LoggingMessageNotification TEST_NOTIFICATION = new LoggingMessageNotification(
			LoggingLevel.INFO, "test-logger", "This is a test message");

	/**
	 * Test class with valid methods.
	 */
	static class ValidMethods {

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

	}

	/**
	 * Test class with invalid methods.
	 */
	static class InvalidMethods {

		@McpLogging
		public String invalidReturnType(LoggingMessageNotification notification) {
			return "Invalid";
		}

		@McpLogging
		public Mono<String> invalidMonoReturnType(LoggingMessageNotification notification) {
			return Mono.just("Invalid");
		}

		@McpLogging
		public Mono<Void> invalidParameterCount(LoggingMessageNotification notification, String extra) {
			return Mono.empty();
		}

		@McpLogging
		public Mono<Void> invalidParameterType(String invalidType) {
			return Mono.empty();
		}

		@McpLogging
		public Mono<Void> invalidParameterTypes(String level, int logger, boolean data) {
			return Mono.empty();
		}

	}

	@Test
	void testValidMethodWithNotification() throws Exception {
		ValidMethods bean = new ValidMethods();
		Method method = ValidMethods.class.getMethod("handleLoggingMessage", LoggingMessageNotification.class);

		Function<LoggingMessageNotification, Mono<Void>> callback = AsyncMcpLoggingMethodCallback.builder()
			.method(method)
			.bean(bean)
			.build();

		StepVerifier.create(callback.apply(TEST_NOTIFICATION)).verifyComplete();

		assertThat(bean.lastNotification).isEqualTo(TEST_NOTIFICATION);
	}

	@Test
	void testValidMethodWithParams() throws Exception {
		ValidMethods bean = new ValidMethods();
		Method method = ValidMethods.class.getMethod("handleLoggingMessageWithParams", LoggingLevel.class, String.class,
				String.class);

		Function<LoggingMessageNotification, Mono<Void>> callback = AsyncMcpLoggingMethodCallback.builder()
			.method(method)
			.bean(bean)
			.build();

		StepVerifier.create(callback.apply(TEST_NOTIFICATION)).verifyComplete();

		assertThat(bean.lastLevel).isEqualTo(TEST_NOTIFICATION.level());
		assertThat(bean.lastLogger).isEqualTo(TEST_NOTIFICATION.logger());
		assertThat(bean.lastData).isEqualTo(TEST_NOTIFICATION.data());
	}

	@Test
	void testValidVoidMethod() throws Exception {
		ValidMethods bean = new ValidMethods();
		Method method = ValidMethods.class.getMethod("handleLoggingMessageVoid", LoggingMessageNotification.class);

		Function<LoggingMessageNotification, Mono<Void>> callback = AsyncMcpLoggingMethodCallback.builder()
			.method(method)
			.bean(bean)
			.build();

		StepVerifier.create(callback.apply(TEST_NOTIFICATION)).verifyComplete();

		assertThat(bean.lastNotification).isEqualTo(TEST_NOTIFICATION);
	}

	@Test
	void testInvalidReturnType() throws Exception {
		InvalidMethods bean = new InvalidMethods();
		Method method = InvalidMethods.class.getMethod("invalidReturnType", LoggingMessageNotification.class);

		assertThatThrownBy(() -> AsyncMcpLoggingMethodCallback.builder().method(method).bean(bean).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method must have void or Mono<Void> return type");
	}

	@Test
	void testInvalidMonoReturnType() throws Exception {
		InvalidMethods bean = new InvalidMethods();
		Method method = InvalidMethods.class.getMethod("invalidMonoReturnType", LoggingMessageNotification.class);

		// This will pass validation since we can't check the generic type at runtime
		Function<LoggingMessageNotification, Mono<Void>> callback = AsyncMcpLoggingMethodCallback.builder()
			.method(method)
			.bean(bean)
			.build();

		// But it will fail at runtime when we try to cast the result
		StepVerifier.create(callback.apply(TEST_NOTIFICATION)).verifyError(ClassCastException.class);
	}

	@Test
	void testInvalidParameterCount() throws Exception {
		InvalidMethods bean = new InvalidMethods();
		Method method = InvalidMethods.class.getMethod("invalidParameterCount", LoggingMessageNotification.class,
				String.class);

		assertThatThrownBy(() -> AsyncMcpLoggingMethodCallback.builder().method(method).bean(bean).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method must have either 1 parameter (LoggingMessageNotification) or 3 parameters");
	}

	@Test
	void testInvalidParameterType() throws Exception {
		InvalidMethods bean = new InvalidMethods();
		Method method = InvalidMethods.class.getMethod("invalidParameterType", String.class);

		assertThatThrownBy(() -> AsyncMcpLoggingMethodCallback.builder().method(method).bean(bean).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Single parameter must be of type LoggingMessageNotification");
	}

	@Test
	void testInvalidParameterTypes() throws Exception {
		InvalidMethods bean = new InvalidMethods();
		Method method = InvalidMethods.class.getMethod("invalidParameterTypes", String.class, int.class, boolean.class);

		assertThatThrownBy(() -> AsyncMcpLoggingMethodCallback.builder().method(method).bean(bean).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("First parameter must be of type LoggingLevel");
	}

	@Test
	void testNullNotification() throws Exception {
		ValidMethods bean = new ValidMethods();
		Method method = ValidMethods.class.getMethod("handleLoggingMessage", LoggingMessageNotification.class);

		Function<LoggingMessageNotification, Mono<Void>> callback = AsyncMcpLoggingMethodCallback.builder()
			.method(method)
			.bean(bean)
			.build();

		StepVerifier.create(callback.apply(null)).verifyErrorSatisfies(e -> {
			assertThat(e).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Notification must not be null");
		});
	}

}
