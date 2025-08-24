/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.changed.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpToolListChanged;

import io.modelcontextprotocol.spec.McpSchema;

/**
 * Tests for {@link SyncMcpToolListChangedMethodCallback}.
 *
 * @author Christian Tzolov
 */
public class SyncMcpToolListChangedMethodCallbackTests {

	private static final List<McpSchema.Tool> TEST_TOOLS = List.of(
			McpSchema.Tool.builder().name("test-tool-1").description("Test Tool 1").inputSchema("{}").build(),
			McpSchema.Tool.builder().name("test-tool-2").description("Test Tool 2").inputSchema("{}").build());

	/**
	 * Test class with valid methods.
	 */
	static class ValidMethods {

		private List<McpSchema.Tool> lastUpdatedTools;

		@McpToolListChanged
		public void handleToolListChanged(List<McpSchema.Tool> updatedTools) {
			this.lastUpdatedTools = updatedTools;
		}

	}

	/**
	 * Test class with invalid methods.
	 */
	static class InvalidMethods {

		@McpToolListChanged
		public String invalidReturnType(List<McpSchema.Tool> updatedTools) {
			return "Invalid";
		}

		@McpToolListChanged
		public void invalidParameterCount(List<McpSchema.Tool> updatedTools, String extra) {
			// Invalid parameter count
		}

		@McpToolListChanged
		public void invalidParameterType(String invalidType) {
			// Invalid parameter type
		}

		@McpToolListChanged
		public void noParameters() {
			// No parameters
		}

	}

	@Test
	void testValidMethodWithToolList() throws Exception {
		ValidMethods bean = new ValidMethods();
		Method method = ValidMethods.class.getMethod("handleToolListChanged", List.class);

		Consumer<List<McpSchema.Tool>> callback = SyncMcpToolListChangedMethodCallback.builder()
			.method(method)
			.bean(bean)
			.build();

		callback.accept(TEST_TOOLS);

		assertThat(bean.lastUpdatedTools).isEqualTo(TEST_TOOLS);
		assertThat(bean.lastUpdatedTools).hasSize(2);
		assertThat(bean.lastUpdatedTools.get(0).name()).isEqualTo("test-tool-1");
		assertThat(bean.lastUpdatedTools.get(1).name()).isEqualTo("test-tool-2");
	}

	@Test
	void testInvalidReturnType() throws Exception {
		InvalidMethods bean = new InvalidMethods();
		Method method = InvalidMethods.class.getMethod("invalidReturnType", List.class);

		assertThatThrownBy(() -> SyncMcpToolListChangedMethodCallback.builder().method(method).bean(bean).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method must have void return type");
	}

	@Test
	void testInvalidParameterCount() throws Exception {
		InvalidMethods bean = new InvalidMethods();
		Method method = InvalidMethods.class.getMethod("invalidParameterCount", List.class, String.class);

		assertThatThrownBy(() -> SyncMcpToolListChangedMethodCallback.builder().method(method).bean(bean).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method must have exactly 1 parameter (List<McpSchema.Tool>)");
	}

	@Test
	void testInvalidParameterType() throws Exception {
		InvalidMethods bean = new InvalidMethods();
		Method method = InvalidMethods.class.getMethod("invalidParameterType", String.class);

		assertThatThrownBy(() -> SyncMcpToolListChangedMethodCallback.builder().method(method).bean(bean).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Parameter must be of type List<McpSchema.Tool>");
	}

	@Test
	void testNoParameters() throws Exception {
		InvalidMethods bean = new InvalidMethods();
		Method method = InvalidMethods.class.getMethod("noParameters");

		assertThatThrownBy(() -> SyncMcpToolListChangedMethodCallback.builder().method(method).bean(bean).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method must have exactly 1 parameter (List<McpSchema.Tool>)");
	}

	@Test
	void testNullToolList() throws Exception {
		ValidMethods bean = new ValidMethods();
		Method method = ValidMethods.class.getMethod("handleToolListChanged", List.class);

		Consumer<List<McpSchema.Tool>> callback = SyncMcpToolListChangedMethodCallback.builder()
			.method(method)
			.bean(bean)
			.build();

		assertThatThrownBy(() -> callback.accept(null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Updated tools list must not be null");
	}

	@Test
	void testEmptyToolList() throws Exception {
		ValidMethods bean = new ValidMethods();
		Method method = ValidMethods.class.getMethod("handleToolListChanged", List.class);

		Consumer<List<McpSchema.Tool>> callback = SyncMcpToolListChangedMethodCallback.builder()
			.method(method)
			.bean(bean)
			.build();

		List<McpSchema.Tool> emptyList = List.of();
		callback.accept(emptyList);

		assertThat(bean.lastUpdatedTools).isEqualTo(emptyList);
		assertThat(bean.lastUpdatedTools).isEmpty();
	}

	@Test
	void testNullMethod() {
		ValidMethods bean = new ValidMethods();

		assertThatThrownBy(() -> SyncMcpToolListChangedMethodCallback.builder().method(null).bean(bean).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method must not be null");
	}

	@Test
	void testNullBean() throws Exception {
		Method method = ValidMethods.class.getMethod("handleToolListChanged", List.class);

		assertThatThrownBy(() -> SyncMcpToolListChangedMethodCallback.builder().method(method).bean(null).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Bean must not be null");
	}

	@Test
	void testMethodInvocationException() throws Exception {
		// Test class that throws an exception in the method
		class ThrowingMethod {

			@McpToolListChanged
			public void handleToolListChanged(List<McpSchema.Tool> updatedTools) {
				throw new RuntimeException("Test exception");
			}

		}

		ThrowingMethod bean = new ThrowingMethod();
		Method method = ThrowingMethod.class.getMethod("handleToolListChanged", List.class);

		Consumer<List<McpSchema.Tool>> callback = SyncMcpToolListChangedMethodCallback.builder()
			.method(method)
			.bean(bean)
			.build();

		assertThatThrownBy(() -> callback.accept(TEST_TOOLS))
			.isInstanceOf(AbstractMcpToolListChangedMethodCallback.McpToolListChangedConsumerMethodException.class)
			.hasMessageContaining("Error invoking tool list changed consumer method");
	}

}
