/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.changed.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpToolListChanged;

import io.modelcontextprotocol.spec.McpSchema;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Tests for {@link AsyncMcpToolListChangedMethodCallback}.
 *
 * @author Christian Tzolov
 */
public class AsyncMcpToolListChangedMethodCallbackTests {

	private static final List<McpSchema.Tool> TEST_TOOLS = List.of(
			McpSchema.Tool.builder().name("test-tool-1").description("Test Tool 1").inputSchema("{}").build(),
			McpSchema.Tool.builder().name("test-tool-2").description("Test Tool 2").inputSchema("{}").build());

	/**
	 * Test class with valid methods.
	 */
	static class ValidMethods {

		private List<McpSchema.Tool> lastUpdatedTools;

		@McpToolListChanged
		public Mono<Void> handleToolListChanged(List<McpSchema.Tool> updatedTools) {
			return Mono.fromRunnable(() -> {
				this.lastUpdatedTools = updatedTools;
			});
		}

		@McpToolListChanged
		public void handleToolListChangedVoid(List<McpSchema.Tool> updatedTools) {
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
		public Mono<String> invalidMonoReturnType(List<McpSchema.Tool> updatedTools) {
			return Mono.just("Invalid");
		}

		@McpToolListChanged
		public Mono<Void> invalidParameterCount(List<McpSchema.Tool> updatedTools, String extra) {
			return Mono.empty();
		}

		@McpToolListChanged
		public Mono<Void> invalidParameterType(String invalidType) {
			return Mono.empty();
		}

		@McpToolListChanged
		public Mono<Void> noParameters() {
			return Mono.empty();
		}

	}

	@Test
	void testValidMethodWithToolList() throws Exception {
		ValidMethods bean = new ValidMethods();
		Method method = ValidMethods.class.getMethod("handleToolListChanged", List.class);

		Function<List<McpSchema.Tool>, Mono<Void>> callback = AsyncMcpToolListChangedMethodCallback.builder()
			.method(method)
			.bean(bean)
			.build();

		StepVerifier.create(callback.apply(TEST_TOOLS)).verifyComplete();

		assertThat(bean.lastUpdatedTools).isEqualTo(TEST_TOOLS);
		assertThat(bean.lastUpdatedTools).hasSize(2);
		assertThat(bean.lastUpdatedTools.get(0).name()).isEqualTo("test-tool-1");
		assertThat(bean.lastUpdatedTools.get(1).name()).isEqualTo("test-tool-2");
	}

	@Test
	void testValidVoidMethod() throws Exception {
		ValidMethods bean = new ValidMethods();
		Method method = ValidMethods.class.getMethod("handleToolListChangedVoid", List.class);

		Function<List<McpSchema.Tool>, Mono<Void>> callback = AsyncMcpToolListChangedMethodCallback.builder()
			.method(method)
			.bean(bean)
			.build();

		StepVerifier.create(callback.apply(TEST_TOOLS)).verifyComplete();

		assertThat(bean.lastUpdatedTools).isEqualTo(TEST_TOOLS);
		assertThat(bean.lastUpdatedTools).hasSize(2);
		assertThat(bean.lastUpdatedTools.get(0).name()).isEqualTo("test-tool-1");
		assertThat(bean.lastUpdatedTools.get(1).name()).isEqualTo("test-tool-2");
	}

	@Test
	void testInvalidReturnType() throws Exception {
		InvalidMethods bean = new InvalidMethods();
		Method method = InvalidMethods.class.getMethod("invalidReturnType", List.class);

		assertThatThrownBy(() -> AsyncMcpToolListChangedMethodCallback.builder().method(method).bean(bean).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method must have void or Mono<Void> return type");
	}

	@Test
	void testInvalidMonoReturnType() throws Exception {
		InvalidMethods bean = new InvalidMethods();
		Method method = InvalidMethods.class.getMethod("invalidMonoReturnType", List.class);

		// This will pass validation since we can't check the generic type at runtime
		Function<List<McpSchema.Tool>, Mono<Void>> callback = AsyncMcpToolListChangedMethodCallback.builder()
			.method(method)
			.bean(bean)
			.build();

		// But it will fail at runtime when we try to cast the result
		StepVerifier.create(callback.apply(TEST_TOOLS)).verifyError(ClassCastException.class);
	}

	@Test
	void testInvalidParameterCount() throws Exception {
		InvalidMethods bean = new InvalidMethods();
		Method method = InvalidMethods.class.getMethod("invalidParameterCount", List.class, String.class);

		assertThatThrownBy(() -> AsyncMcpToolListChangedMethodCallback.builder().method(method).bean(bean).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method must have exactly 1 parameter (List<McpSchema.Tool>)");
	}

	@Test
	void testInvalidParameterType() throws Exception {
		InvalidMethods bean = new InvalidMethods();
		Method method = InvalidMethods.class.getMethod("invalidParameterType", String.class);

		assertThatThrownBy(() -> AsyncMcpToolListChangedMethodCallback.builder().method(method).bean(bean).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Parameter must be of type List<McpSchema.Tool>");
	}

	@Test
	void testNoParameters() throws Exception {
		InvalidMethods bean = new InvalidMethods();
		Method method = InvalidMethods.class.getMethod("noParameters");

		assertThatThrownBy(() -> AsyncMcpToolListChangedMethodCallback.builder().method(method).bean(bean).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method must have exactly 1 parameter (List<McpSchema.Tool>)");
	}

	@Test
	void testNullToolList() throws Exception {
		ValidMethods bean = new ValidMethods();
		Method method = ValidMethods.class.getMethod("handleToolListChanged", List.class);

		Function<List<McpSchema.Tool>, Mono<Void>> callback = AsyncMcpToolListChangedMethodCallback.builder()
			.method(method)
			.bean(bean)
			.build();

		StepVerifier.create(callback.apply(null)).verifyErrorSatisfies(e -> {
			assertThat(e).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Updated tools list must not be null");
		});
	}

	@Test
	void testEmptyToolList() throws Exception {
		ValidMethods bean = new ValidMethods();
		Method method = ValidMethods.class.getMethod("handleToolListChanged", List.class);

		Function<List<McpSchema.Tool>, Mono<Void>> callback = AsyncMcpToolListChangedMethodCallback.builder()
			.method(method)
			.bean(bean)
			.build();

		List<McpSchema.Tool> emptyList = List.of();
		StepVerifier.create(callback.apply(emptyList)).verifyComplete();

		assertThat(bean.lastUpdatedTools).isEqualTo(emptyList);
		assertThat(bean.lastUpdatedTools).isEmpty();
	}

	@Test
	void testNullMethod() {
		ValidMethods bean = new ValidMethods();

		assertThatThrownBy(() -> AsyncMcpToolListChangedMethodCallback.builder().method(null).bean(bean).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method must not be null");
	}

	@Test
	void testNullBean() throws Exception {
		Method method = ValidMethods.class.getMethod("handleToolListChanged", List.class);

		assertThatThrownBy(() -> AsyncMcpToolListChangedMethodCallback.builder().method(method).bean(null).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Bean must not be null");
	}

	@Test
	void testMethodInvocationException() throws Exception {
		// Test class that throws an exception in the method
		class ThrowingMethod {

			@McpToolListChanged
			public Mono<Void> handleToolListChanged(List<McpSchema.Tool> updatedTools) {
				return Mono.fromRunnable(() -> {
					throw new RuntimeException("Test exception");
				});
			}

		}

		ThrowingMethod bean = new ThrowingMethod();
		Method method = ThrowingMethod.class.getMethod("handleToolListChanged", List.class);

		Function<List<McpSchema.Tool>, Mono<Void>> callback = AsyncMcpToolListChangedMethodCallback.builder()
			.method(method)
			.bean(bean)
			.build();

		StepVerifier.create(callback.apply(TEST_TOOLS)).verifyError(RuntimeException.class);
	}

	@Test
	void testMethodInvocationExceptionVoid() throws Exception {
		// Test class that throws an exception in a void method
		class ThrowingVoidMethod {

			@McpToolListChanged
			public void handleToolListChanged(List<McpSchema.Tool> updatedTools) {
				throw new RuntimeException("Test exception");
			}

		}

		ThrowingVoidMethod bean = new ThrowingVoidMethod();
		Method method = ThrowingVoidMethod.class.getMethod("handleToolListChanged", List.class);

		Function<List<McpSchema.Tool>, Mono<Void>> callback = AsyncMcpToolListChangedMethodCallback.builder()
			.method(method)
			.bean(bean)
			.build();

		StepVerifier.create(callback.apply(TEST_TOOLS)).verifyErrorSatisfies(e -> {
			assertThat(e)
				.isInstanceOf(AbstractMcpToolListChangedMethodCallback.McpToolListChangedConsumerMethodException.class)
				.hasMessageContaining("Error invoking tool list changed consumer method");
		});
	}

}
