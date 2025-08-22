/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.progress;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpProgress;

import io.modelcontextprotocol.spec.McpSchema.ProgressNotification;

/**
 * Tests for {@link SyncMcpProgressMethodCallback}.
 *
 * @author Christian Tzolov
 */
public class SyncMcpProgressMethodCallbackTests {

	// ProgressNotification constructor: (String progressToken, double progress, Double
	// total, String message)
	private static final ProgressNotification TEST_NOTIFICATION = new ProgressNotification("progress-token-123", // progressToken
			0.5, // progress
			100.0, // total
			"Processing..." // message
	);

	/**
	 * Test class with valid methods.
	 */
	static class ValidMethods {

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

	}

	/**
	 * Test class with invalid methods.
	 */
	static class InvalidMethods {

		@McpProgress
		public String invalidReturnType(ProgressNotification notification) {
			return "Invalid";
		}

		@McpProgress
		public void invalidParameterCount(ProgressNotification notification, String extra) {
			// Invalid parameter count
		}

		@McpProgress
		public void invalidParameterType(String invalidType) {
			// Invalid parameter type
		}

		@McpProgress
		public void invalidParameterTypes(String progress, int progressToken, boolean total) {
			// Invalid parameter types
		}

		@McpProgress
		public void invalidFirstParameterType(String progress, String progressToken, String total) {
			// Invalid first parameter type
		}

		@McpProgress
		public void invalidSecondParameterType(Double progress, int progressToken, String total) {
			// Invalid second parameter type
		}

		@McpProgress
		public void invalidThirdParameterType(Double progress, String progressToken, int total) {
			// Invalid third parameter type
		}

	}

	@Test
	void testValidMethodWithNotification() throws Exception {
		ValidMethods bean = new ValidMethods();
		Method method = ValidMethods.class.getMethod("handleProgressNotification", ProgressNotification.class);

		Consumer<ProgressNotification> callback = SyncMcpProgressMethodCallback.builder()
			.method(method)
			.bean(bean)
			.build();

		callback.accept(TEST_NOTIFICATION);

		assertThat(bean.lastNotification).isEqualTo(TEST_NOTIFICATION);
	}

	@Test
	void testValidMethodWithParams() throws Exception {
		ValidMethods bean = new ValidMethods();
		Method method = ValidMethods.class.getMethod("handleProgressWithParams", Double.class, String.class,
				String.class);

		Consumer<ProgressNotification> callback = SyncMcpProgressMethodCallback.builder()
			.method(method)
			.bean(bean)
			.build();

		callback.accept(TEST_NOTIFICATION);

		assertThat(bean.lastProgress).isEqualTo(TEST_NOTIFICATION.progress());
		assertThat(bean.lastProgressToken).isEqualTo(TEST_NOTIFICATION.progressToken());
		assertThat(bean.lastTotal).isEqualTo(String.valueOf(TEST_NOTIFICATION.total()));
	}

	@Test
	void testValidMethodWithPrimitiveDouble() throws Exception {
		ValidMethods bean = new ValidMethods();
		Method method = ValidMethods.class.getMethod("handleProgressWithPrimitiveDouble", double.class, String.class,
				String.class);

		Consumer<ProgressNotification> callback = SyncMcpProgressMethodCallback.builder()
			.method(method)
			.bean(bean)
			.build();

		callback.accept(TEST_NOTIFICATION);

		assertThat(bean.lastProgress).isEqualTo(TEST_NOTIFICATION.progress());
		assertThat(bean.lastProgressToken).isEqualTo(TEST_NOTIFICATION.progressToken());
		assertThat(bean.lastTotal).isEqualTo(String.valueOf(TEST_NOTIFICATION.total()));
	}

	@Test
	void testInvalidReturnType() throws Exception {
		InvalidMethods bean = new InvalidMethods();
		Method method = InvalidMethods.class.getMethod("invalidReturnType", ProgressNotification.class);

		assertThatThrownBy(() -> SyncMcpProgressMethodCallback.builder().method(method).bean(bean).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Synchronous progress methods must return void");
	}

	@Test
	void testInvalidParameterCount() throws Exception {
		InvalidMethods bean = new InvalidMethods();
		Method method = InvalidMethods.class.getMethod("invalidParameterCount", ProgressNotification.class,
				String.class);

		assertThatThrownBy(() -> SyncMcpProgressMethodCallback.builder().method(method).bean(bean).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method must have either 1 parameter (ProgressNotification) or 3 parameters");
	}

	@Test
	void testInvalidParameterType() throws Exception {
		InvalidMethods bean = new InvalidMethods();
		Method method = InvalidMethods.class.getMethod("invalidParameterType", String.class);

		assertThatThrownBy(() -> SyncMcpProgressMethodCallback.builder().method(method).bean(bean).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Single parameter must be of type ProgressNotification");
	}

	@Test
	void testInvalidParameterTypes() throws Exception {
		InvalidMethods bean = new InvalidMethods();
		Method method = InvalidMethods.class.getMethod("invalidParameterTypes", String.class, int.class, boolean.class);

		assertThatThrownBy(() -> SyncMcpProgressMethodCallback.builder().method(method).bean(bean).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("First parameter must be of type Double or double");
	}

	@Test
	void testInvalidFirstParameterType() throws Exception {
		InvalidMethods bean = new InvalidMethods();
		Method method = InvalidMethods.class.getMethod("invalidFirstParameterType", String.class, String.class,
				String.class);

		assertThatThrownBy(() -> SyncMcpProgressMethodCallback.builder().method(method).bean(bean).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("First parameter must be of type Double or double");
	}

	@Test
	void testInvalidSecondParameterType() throws Exception {
		InvalidMethods bean = new InvalidMethods();
		Method method = InvalidMethods.class.getMethod("invalidSecondParameterType", Double.class, int.class,
				String.class);

		assertThatThrownBy(() -> SyncMcpProgressMethodCallback.builder().method(method).bean(bean).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Second parameter must be of type String");
	}

	@Test
	void testInvalidThirdParameterType() throws Exception {
		InvalidMethods bean = new InvalidMethods();
		Method method = InvalidMethods.class.getMethod("invalidThirdParameterType", Double.class, String.class,
				int.class);

		assertThatThrownBy(() -> SyncMcpProgressMethodCallback.builder().method(method).bean(bean).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Third parameter must be of type String");
	}

	@Test
	void testNullNotification() throws Exception {
		ValidMethods bean = new ValidMethods();
		Method method = ValidMethods.class.getMethod("handleProgressNotification", ProgressNotification.class);

		Consumer<ProgressNotification> callback = SyncMcpProgressMethodCallback.builder()
			.method(method)
			.bean(bean)
			.build();

		assertThatThrownBy(() -> callback.accept(null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Notification must not be null");
	}

}
