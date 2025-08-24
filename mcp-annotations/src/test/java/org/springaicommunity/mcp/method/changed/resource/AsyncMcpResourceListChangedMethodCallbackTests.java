/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.changed.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpResourceListChanged;

import io.modelcontextprotocol.spec.McpSchema;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Tests for {@link AsyncMcpResourceListChangedMethodCallback}.
 *
 * @author Christian Tzolov
 */
public class AsyncMcpResourceListChangedMethodCallbackTests {

	private static final List<McpSchema.Resource> TEST_RESOURCES = List.of(
			McpSchema.Resource.builder()
				.uri("file:///test1.txt")
				.name("test-resource-1")
				.description("Test Resource 1")
				.mimeType("text/plain")
				.build(),
			McpSchema.Resource.builder()
				.uri("file:///test2.txt")
				.name("test-resource-2")
				.description("Test Resource 2")
				.mimeType("text/plain")
				.build());

	/**
	 * Test class with valid methods.
	 */
	static class ValidMethods {

		private List<McpSchema.Resource> lastUpdatedResources;

		@McpResourceListChanged
		public Mono<Void> handleResourceListChanged(List<McpSchema.Resource> updatedResources) {
			return Mono.fromRunnable(() -> {
				this.lastUpdatedResources = updatedResources;
			});
		}

		@McpResourceListChanged
		public void handleResourceListChangedVoid(List<McpSchema.Resource> updatedResources) {
			this.lastUpdatedResources = updatedResources;
		}

	}

	/**
	 * Test class with invalid methods.
	 */
	static class InvalidMethods {

		@McpResourceListChanged
		public String invalidReturnType(List<McpSchema.Resource> updatedResources) {
			return "Invalid";
		}

		@McpResourceListChanged
		public Mono<String> invalidMonoReturnType(List<McpSchema.Resource> updatedResources) {
			return Mono.just("Invalid");
		}

		@McpResourceListChanged
		public Mono<Void> invalidParameterCount(List<McpSchema.Resource> updatedResources, String extra) {
			return Mono.empty();
		}

		@McpResourceListChanged
		public Mono<Void> invalidParameterType(String invalidType) {
			return Mono.empty();
		}

		@McpResourceListChanged
		public Mono<Void> noParameters() {
			return Mono.empty();
		}

	}

	@Test
	void testValidMethodWithResourceList() throws Exception {
		ValidMethods bean = new ValidMethods();
		Method method = ValidMethods.class.getMethod("handleResourceListChanged", List.class);

		Function<List<McpSchema.Resource>, Mono<Void>> callback = AsyncMcpResourceListChangedMethodCallback.builder()
			.method(method)
			.bean(bean)
			.build();

		StepVerifier.create(callback.apply(TEST_RESOURCES)).verifyComplete();

		assertThat(bean.lastUpdatedResources).isEqualTo(TEST_RESOURCES);
		assertThat(bean.lastUpdatedResources).hasSize(2);
		assertThat(bean.lastUpdatedResources.get(0).name()).isEqualTo("test-resource-1");
		assertThat(bean.lastUpdatedResources.get(1).name()).isEqualTo("test-resource-2");
	}

	@Test
	void testValidVoidMethod() throws Exception {
		ValidMethods bean = new ValidMethods();
		Method method = ValidMethods.class.getMethod("handleResourceListChangedVoid", List.class);

		Function<List<McpSchema.Resource>, Mono<Void>> callback = AsyncMcpResourceListChangedMethodCallback.builder()
			.method(method)
			.bean(bean)
			.build();

		StepVerifier.create(callback.apply(TEST_RESOURCES)).verifyComplete();

		assertThat(bean.lastUpdatedResources).isEqualTo(TEST_RESOURCES);
		assertThat(bean.lastUpdatedResources).hasSize(2);
		assertThat(bean.lastUpdatedResources.get(0).name()).isEqualTo("test-resource-1");
		assertThat(bean.lastUpdatedResources.get(1).name()).isEqualTo("test-resource-2");
	}

	@Test
	void testInvalidReturnType() throws Exception {
		InvalidMethods bean = new InvalidMethods();
		Method method = InvalidMethods.class.getMethod("invalidReturnType", List.class);

		assertThatThrownBy(() -> AsyncMcpResourceListChangedMethodCallback.builder().method(method).bean(bean).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method must have void or Mono<Void> return type");
	}

	@Test
	void testInvalidMonoReturnType() throws Exception {
		InvalidMethods bean = new InvalidMethods();
		Method method = InvalidMethods.class.getMethod("invalidMonoReturnType", List.class);

		// This will pass validation since we can't check the generic type at runtime
		Function<List<McpSchema.Resource>, Mono<Void>> callback = AsyncMcpResourceListChangedMethodCallback.builder()
			.method(method)
			.bean(bean)
			.build();

		// But it will fail at runtime when we try to cast the result
		StepVerifier.create(callback.apply(TEST_RESOURCES)).verifyError(ClassCastException.class);
	}

	@Test
	void testInvalidParameterCount() throws Exception {
		InvalidMethods bean = new InvalidMethods();
		Method method = InvalidMethods.class.getMethod("invalidParameterCount", List.class, String.class);

		assertThatThrownBy(() -> AsyncMcpResourceListChangedMethodCallback.builder().method(method).bean(bean).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method must have exactly 1 parameter (List<McpSchema.Resource>)");
	}

	@Test
	void testInvalidParameterType() throws Exception {
		InvalidMethods bean = new InvalidMethods();
		Method method = InvalidMethods.class.getMethod("invalidParameterType", String.class);

		assertThatThrownBy(() -> AsyncMcpResourceListChangedMethodCallback.builder().method(method).bean(bean).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Parameter must be of type List<McpSchema.Resource>");
	}

	@Test
	void testNoParameters() throws Exception {
		InvalidMethods bean = new InvalidMethods();
		Method method = InvalidMethods.class.getMethod("noParameters");

		assertThatThrownBy(() -> AsyncMcpResourceListChangedMethodCallback.builder().method(method).bean(bean).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method must have exactly 1 parameter (List<McpSchema.Resource>)");
	}

	@Test
	void testNullResourceList() throws Exception {
		ValidMethods bean = new ValidMethods();
		Method method = ValidMethods.class.getMethod("handleResourceListChanged", List.class);

		Function<List<McpSchema.Resource>, Mono<Void>> callback = AsyncMcpResourceListChangedMethodCallback.builder()
			.method(method)
			.bean(bean)
			.build();

		StepVerifier.create(callback.apply(null)).verifyErrorSatisfies(e -> {
			assertThat(e).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Updated resources list must not be null");
		});
	}

	@Test
	void testEmptyResourceList() throws Exception {
		ValidMethods bean = new ValidMethods();
		Method method = ValidMethods.class.getMethod("handleResourceListChanged", List.class);

		Function<List<McpSchema.Resource>, Mono<Void>> callback = AsyncMcpResourceListChangedMethodCallback.builder()
			.method(method)
			.bean(bean)
			.build();

		List<McpSchema.Resource> emptyList = List.of();
		StepVerifier.create(callback.apply(emptyList)).verifyComplete();

		assertThat(bean.lastUpdatedResources).isEqualTo(emptyList);
		assertThat(bean.lastUpdatedResources).isEmpty();
	}

	@Test
	void testNullMethod() {
		ValidMethods bean = new ValidMethods();

		assertThatThrownBy(() -> AsyncMcpResourceListChangedMethodCallback.builder().method(null).bean(bean).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method must not be null");
	}

	@Test
	void testNullBean() throws Exception {
		Method method = ValidMethods.class.getMethod("handleResourceListChanged", List.class);

		assertThatThrownBy(() -> AsyncMcpResourceListChangedMethodCallback.builder().method(method).bean(null).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Bean must not be null");
	}

	@Test
	void testMethodInvocationException() throws Exception {
		// Test class that throws an exception in the method
		class ThrowingMethod {

			@McpResourceListChanged
			public Mono<Void> handleResourceListChanged(List<McpSchema.Resource> updatedResources) {
				return Mono.fromRunnable(() -> {
					throw new RuntimeException("Test exception");
				});
			}

		}

		ThrowingMethod bean = new ThrowingMethod();
		Method method = ThrowingMethod.class.getMethod("handleResourceListChanged", List.class);

		Function<List<McpSchema.Resource>, Mono<Void>> callback = AsyncMcpResourceListChangedMethodCallback.builder()
			.method(method)
			.bean(bean)
			.build();

		StepVerifier.create(callback.apply(TEST_RESOURCES)).verifyError(RuntimeException.class);
	}

	@Test
	void testMethodInvocationExceptionVoid() throws Exception {
		// Test class that throws an exception in a void method
		class ThrowingVoidMethod {

			@McpResourceListChanged
			public void handleResourceListChanged(List<McpSchema.Resource> updatedResources) {
				throw new RuntimeException("Test exception");
			}

		}

		ThrowingVoidMethod bean = new ThrowingVoidMethod();
		Method method = ThrowingVoidMethod.class.getMethod("handleResourceListChanged", List.class);

		Function<List<McpSchema.Resource>, Mono<Void>> callback = AsyncMcpResourceListChangedMethodCallback.builder()
			.method(method)
			.bean(bean)
			.build();

		StepVerifier.create(callback.apply(TEST_RESOURCES)).verifyErrorSatisfies(e -> {
			assertThat(e).isInstanceOf(
					AbstractMcpResourceListChangedMethodCallback.McpResourceListChangedConsumerMethodException.class)
				.hasMessageContaining("Error invoking resource list changed consumer method");
		});
	}

}
