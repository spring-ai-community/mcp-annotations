/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpLogging;

import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;

/**
 * Tests for {@link SyncMcpLoggingMethodCallback}.
 *
 * @author Christian Tzolov
 */
public class SyncMcpLoggingMethodCallbackTests {

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
		public void handleLoggingMessage(LoggingMessageNotification notification) {
			this.lastNotification = notification;
		}

		@McpLogging
		public void handleLoggingMessageWithParams(LoggingLevel level, String logger, String data) {
			this.lastLevel = level;
			this.lastLogger = logger;
			this.lastData = data;
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
		public void invalidParameterCount(LoggingMessageNotification notification, String extra) {
			// Invalid parameter count
		}

		@McpLogging
		public void invalidParameterType(String invalidType) {
			// Invalid parameter type
		}

		@McpLogging
		public void invalidParameterTypes(String level, int logger, boolean data) {
			// Invalid parameter types
		}

	}

	@Test
	void testValidMethodWithNotification() throws Exception {
		ValidMethods bean = new ValidMethods();
		Method method = ValidMethods.class.getMethod("handleLoggingMessage", LoggingMessageNotification.class);

		Consumer<LoggingMessageNotification> callback = SyncMcpLoggingMethodCallback.builder()
			.method(method)
			.bean(bean)
			.build();

		callback.accept(TEST_NOTIFICATION);

		assertThat(bean.lastNotification).isEqualTo(TEST_NOTIFICATION);
	}

	@Test
	void testValidMethodWithParams() throws Exception {
		ValidMethods bean = new ValidMethods();
		Method method = ValidMethods.class.getMethod("handleLoggingMessageWithParams", LoggingLevel.class, String.class,
				String.class);

		Consumer<LoggingMessageNotification> callback = SyncMcpLoggingMethodCallback.builder()
			.method(method)
			.bean(bean)
			.build();

		callback.accept(TEST_NOTIFICATION);

		assertThat(bean.lastLevel).isEqualTo(TEST_NOTIFICATION.level());
		assertThat(bean.lastLogger).isEqualTo(TEST_NOTIFICATION.logger());
		assertThat(bean.lastData).isEqualTo(TEST_NOTIFICATION.data());
	}

	@Test
	void testInvalidReturnType() throws Exception {
		InvalidMethods bean = new InvalidMethods();
		Method method = InvalidMethods.class.getMethod("invalidReturnType", LoggingMessageNotification.class);

		assertThatThrownBy(() -> SyncMcpLoggingMethodCallback.builder().method(method).bean(bean).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method must have void return type");
	}

	@Test
	void testInvalidParameterCount() throws Exception {
		InvalidMethods bean = new InvalidMethods();
		Method method = InvalidMethods.class.getMethod("invalidParameterCount", LoggingMessageNotification.class,
				String.class);

		assertThatThrownBy(() -> SyncMcpLoggingMethodCallback.builder().method(method).bean(bean).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method must have either 1 parameter (LoggingMessageNotification) or 3 parameters");
	}

	@Test
	void testInvalidParameterType() throws Exception {
		InvalidMethods bean = new InvalidMethods();
		Method method = InvalidMethods.class.getMethod("invalidParameterType", String.class);

		assertThatThrownBy(() -> SyncMcpLoggingMethodCallback.builder().method(method).bean(bean).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Single parameter must be of type LoggingMessageNotification");
	}

	@Test
	void testInvalidParameterTypes() throws Exception {
		InvalidMethods bean = new InvalidMethods();
		Method method = InvalidMethods.class.getMethod("invalidParameterTypes", String.class, int.class, boolean.class);

		assertThatThrownBy(() -> SyncMcpLoggingMethodCallback.builder().method(method).bean(bean).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("First parameter must be of type LoggingLevel");
	}

	@Test
	void testNullNotification() throws Exception {
		ValidMethods bean = new ValidMethods();
		Method method = ValidMethods.class.getMethod("handleLoggingMessage", LoggingMessageNotification.class);

		Consumer<LoggingMessageNotification> callback = SyncMcpLoggingMethodCallback.builder()
			.method(method)
			.bean(bean)
			.build();

		assertThatThrownBy(() -> callback.accept(null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Notification must not be null");
	}

}
