/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.sampling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpSampling;
import org.springaicommunity.mcp.method.sampling.AsyncMcpSamplingMethodCallback;
import org.springaicommunity.mcp.method.sampling.AbstractMcpSamplingMethodCallback.McpSamplingMethodException;

import io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Tests for {@link AsyncMcpSamplingMethodCallback}.
 *
 * @author Christian Tzolov
 */
public class AsyncMcpSamplingMethodCallbackTests {

	private final AsyncMcpSamplingMethodCallbackExample asyncExample = new AsyncMcpSamplingMethodCallbackExample();

	@Test
	void testValidMethod() throws Exception {
		Method method = AsyncMcpSamplingMethodCallbackExample.class.getMethod("handleAsyncSamplingRequest",
				CreateMessageRequest.class);
		McpSampling annotation = method.getAnnotation(McpSampling.class);

		AsyncMcpSamplingMethodCallback callback = AsyncMcpSamplingMethodCallback.builder()
			.method(method)
			.bean(asyncExample)
			.sampling(annotation)
			.build();

		CreateMessageRequest request = SamlingTestHelper.createSampleRequest();
		Mono<CreateMessageResult> resultMono = callback.apply(request);

		StepVerifier.create(resultMono).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.content()).isInstanceOf(TextContent.class);
			assertThat(((TextContent) result.content()).text())
				.isEqualTo("This is an async response to the sampling request");
		}).verifyComplete();
	}

	@Test
	void testDirectResultMethod() throws Exception {
		Method method = AsyncMcpSamplingMethodCallbackExample.class.getMethod("handleDirectSamplingRequest",
				CreateMessageRequest.class);
		McpSampling annotation = method.getAnnotation(McpSampling.class);

		AsyncMcpSamplingMethodCallback callback = AsyncMcpSamplingMethodCallback.builder()
			.method(method)
			.bean(asyncExample)
			.sampling(annotation)
			.build();

		CreateMessageRequest request = SamlingTestHelper.createSampleRequest();
		Mono<CreateMessageResult> resultMono = callback.apply(request);

		StepVerifier.create(resultMono).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.content()).isInstanceOf(TextContent.class);
			assertThat(((TextContent) result.content()).text())
				.isEqualTo("This is a direct response to the sampling request");
		}).verifyComplete();
	}

	@Test
	void testNullRequest() throws Exception {
		Method method = AsyncMcpSamplingMethodCallbackExample.class.getMethod("handleAsyncSamplingRequest",
				CreateMessageRequest.class);
		McpSampling annotation = method.getAnnotation(McpSampling.class);

		AsyncMcpSamplingMethodCallback callback = AsyncMcpSamplingMethodCallback.builder()
			.method(method)
			.bean(asyncExample)
			.sampling(annotation)
			.build();

		Mono<CreateMessageResult> resultMono = callback.apply(null);

		StepVerifier.create(resultMono).expectErrorSatisfies(error -> {
			assertThat(error).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Request must not be null");
		}).verify();
	}

	@Test
	void testInvalidMonoReturnType() throws Exception {
		Method method = AsyncMcpSamplingMethodCallbackExample.class.getMethod("invalidMonoReturnType",
				CreateMessageRequest.class);
		McpSampling annotation = method.getAnnotation(McpSampling.class);

		AsyncMcpSamplingMethodCallback callback = AsyncMcpSamplingMethodCallback.builder()
			.method(method)
			.bean(asyncExample)
			.sampling(annotation)
			.build();

		CreateMessageRequest request = SamlingTestHelper.createSampleRequest();
		Mono<CreateMessageResult> resultMono = callback.apply(request);

		StepVerifier.create(resultMono).expectNextCount(1).verifyComplete();
	}

	@Test
	void testInvalidParameterType() throws Exception {
		Method method = AsyncMcpSamplingMethodCallbackExample.class.getMethod("invalidParameterType", String.class);
		McpSampling annotation = method.getAnnotation(McpSampling.class);

		assertThatThrownBy(() -> AsyncMcpSamplingMethodCallback.builder()
			.method(method)
			.bean(asyncExample)
			.sampling(annotation)
			.build()).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Single parameter must be of type CreateMessageRequest");
	}

	@Test
	void testNoParameters() throws Exception {
		Method method = AsyncMcpSamplingMethodCallbackExample.class.getMethod("noParameters");
		McpSampling annotation = method.getAnnotation(McpSampling.class);

		assertThatThrownBy(() -> AsyncMcpSamplingMethodCallback.builder()
			.method(method)
			.bean(asyncExample)
			.sampling(annotation)
			.build()).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method must have at least 1 parameter");
	}

	@Test
	void testTooManyParameters() throws Exception {
		Method method = AsyncMcpSamplingMethodCallbackExample.class.getMethod("tooManyParameters",
				CreateMessageRequest.class, String.class);
		McpSampling annotation = method.getAnnotation(McpSampling.class);

		assertThatThrownBy(() -> AsyncMcpSamplingMethodCallback.builder()
			.method(method)
			.bean(asyncExample)
			.sampling(annotation)
			.build()).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Currently only methods with a single CreateMessageRequest parameter are supported");
	}

	@Test
	void testNullMethod() {
		assertThatThrownBy(() -> AsyncMcpSamplingMethodCallback.builder().method(null).bean(asyncExample).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method must not be null");
	}

	@Test
	void testNullBean() throws Exception {
		Method method = AsyncMcpSamplingMethodCallbackExample.class.getMethod("handleAsyncSamplingRequest",
				CreateMessageRequest.class);
		assertThatThrownBy(() -> AsyncMcpSamplingMethodCallback.builder().method(method).bean(null).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Bean must not be null");
	}

	@Test
	void testMethodInvocationError() throws Exception {
		// Create a method that will throw an exception when invoked
		Method method = AsyncMcpSamplingMethodCallbackExample.class.getMethod("handleAsyncSamplingRequest",
				CreateMessageRequest.class);
		McpSampling annotation = method.getAnnotation(McpSampling.class);

		AsyncMcpSamplingMethodCallback callback = AsyncMcpSamplingMethodCallback.builder()
			.method(method)
			.bean(new AsyncMcpSamplingMethodCallbackExample() {
				@Override
				public Mono<CreateMessageResult> handleAsyncSamplingRequest(CreateMessageRequest request) {
					throw new RuntimeException("Test exception");
				}
			})
			.sampling(annotation)
			.build();

		CreateMessageRequest request = SamlingTestHelper.createSampleRequest();
		Mono<CreateMessageResult> resultMono = callback.apply(request);

		StepVerifier.create(resultMono).expectErrorSatisfies(error -> {
			assertThat(error).isInstanceOf(McpSamplingMethodException.class)
				.hasMessageContaining("Error invoking sampling method")
				.hasCauseInstanceOf(java.lang.reflect.InvocationTargetException.class)
				.satisfies(e -> {
					Throwable cause = e.getCause().getCause();
					assertThat(cause).isInstanceOf(RuntimeException.class);
					assertThat(cause.getMessage()).isEqualTo("Test exception");
				});
		}).verify();
	}

}
