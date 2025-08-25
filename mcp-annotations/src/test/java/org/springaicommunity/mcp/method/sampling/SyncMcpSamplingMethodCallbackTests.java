/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.sampling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpSampling;
import org.springaicommunity.mcp.method.sampling.AbstractMcpSamplingMethodCallback.McpSamplingMethodException;

import io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

/**
 * Tests for {@link SyncMcpSamplingMethodCallback}.
 *
 * @author Christian Tzolov
 */
public class SyncMcpSamplingMethodCallbackTests {

	private final SyncMcpSamplingMethodCallbackExample example = new SyncMcpSamplingMethodCallbackExample();

	@Test
	void testValidMethod() throws Exception {
		Method method = SyncMcpSamplingMethodCallbackExample.class.getMethod("handleSamplingRequest",
				CreateMessageRequest.class);
		McpSampling annotation = method.getAnnotation(McpSampling.class);

		SyncMcpSamplingMethodCallback callback = SyncMcpSamplingMethodCallback.builder()
			.method(method)
			.bean(example)
			.sampling(annotation)
			.build();

		CreateMessageRequest request = SamlingTestHelper.createSampleRequest();
		CreateMessageResult result = callback.apply(request);

		assertThat(result).isNotNull();
		assertThat(result.content()).isInstanceOf(TextContent.class);
		assertThat(((TextContent) result.content()).text()).isEqualTo("This is a response to the sampling request");
	}

	@Test
	void testNullRequest() throws Exception {
		Method method = SyncMcpSamplingMethodCallbackExample.class.getMethod("handleSamplingRequest",
				CreateMessageRequest.class);
		McpSampling annotation = method.getAnnotation(McpSampling.class);

		SyncMcpSamplingMethodCallback callback = SyncMcpSamplingMethodCallback.builder()
			.method(method)
			.bean(example)
			.sampling(annotation)
			.build();

		assertThatThrownBy(() -> callback.apply(null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Request must not be null");
	}

	@Test
	void testInvalidReturnType() throws Exception {
		Method method = SyncMcpSamplingMethodCallbackExample.class.getMethod("invalidReturnType",
				CreateMessageRequest.class);
		McpSampling annotation = method.getAnnotation(McpSampling.class);

		assertThatThrownBy(
				() -> SyncMcpSamplingMethodCallback.builder().method(method).bean(example).sampling(annotation).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method must return CreateMessageResult");
	}

	@Test
	void testInvalidParameterType() throws Exception {
		Method method = SyncMcpSamplingMethodCallbackExample.class.getMethod("invalidParameterType", String.class);
		McpSampling annotation = method.getAnnotation(McpSampling.class);

		assertThatThrownBy(
				() -> SyncMcpSamplingMethodCallback.builder().method(method).bean(example).sampling(annotation).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Single parameter must be of type CreateMessageRequest");
	}

	@Test
	void testNoParameters() throws Exception {
		Method method = SyncMcpSamplingMethodCallbackExample.class.getMethod("noParameters");
		McpSampling annotation = method.getAnnotation(McpSampling.class);

		assertThatThrownBy(
				() -> SyncMcpSamplingMethodCallback.builder().method(method).bean(example).sampling(annotation).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method must have at least 1 parameter");
	}

	@Test
	void testTooManyParameters() throws Exception {
		Method method = SyncMcpSamplingMethodCallbackExample.class.getMethod("tooManyParameters",
				CreateMessageRequest.class, String.class);
		McpSampling annotation = method.getAnnotation(McpSampling.class);

		assertThatThrownBy(
				() -> SyncMcpSamplingMethodCallback.builder().method(method).bean(example).sampling(annotation).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Currently only methods with a single CreateMessageRequest parameter are supported");
	}

	@Test
	void testNullMethod() {
		assertThatThrownBy(() -> SyncMcpSamplingMethodCallback.builder().method(null).bean(example).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method must not be null");
	}

	@Test
	void testNullBean() throws Exception {
		Method method = SyncMcpSamplingMethodCallbackExample.class.getMethod("handleSamplingRequest",
				CreateMessageRequest.class);
		assertThatThrownBy(() -> SyncMcpSamplingMethodCallback.builder().method(method).bean(null).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Bean must not be null");
	}

	@Test
	void testMethodInvocationError() throws Exception {
		// Create a method that will throw an exception when invoked
		Method method = SyncMcpSamplingMethodCallbackExample.class.getMethod("handleSamplingRequest",
				CreateMessageRequest.class);
		McpSampling annotation = method.getAnnotation(McpSampling.class);

		SyncMcpSamplingMethodCallback callback = SyncMcpSamplingMethodCallback.builder()
			.method(method)
			.bean(new SyncMcpSamplingMethodCallbackExample() {
				@Override
				public CreateMessageResult handleSamplingRequest(CreateMessageRequest request) {
					throw new RuntimeException("Test exception");
				}
			})
			.sampling(annotation)
			.build();

		CreateMessageRequest request = SamlingTestHelper.createSampleRequest();
		assertThatThrownBy(() -> callback.apply(request)).isInstanceOf(McpSamplingMethodException.class)
			.hasMessageContaining("Error invoking sampling method")
			.hasCauseInstanceOf(java.lang.reflect.InvocationTargetException.class)
			.satisfies(e -> {
				Throwable cause = e.getCause().getCause();
				assertThat(cause).isInstanceOf(RuntimeException.class);
				assertThat(cause.getMessage()).isEqualTo("Test exception");
			});
	}

}
