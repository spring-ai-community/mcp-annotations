/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.elicitation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;
import java.util.Map;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpElicitation;
import org.springaicommunity.mcp.method.elicitation.AbstractMcpElicitationMethodCallback.McpElicitationMethodException;

import io.modelcontextprotocol.spec.McpSchema.ElicitRequest;
import io.modelcontextprotocol.spec.McpSchema.ElicitResult;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Tests for {@link AsyncMcpElicitationMethodCallback}.
 *
 * @author Christian Tzolov
 */
public class AsyncMcpElicitationMethodCallbackTests {

	private final AsyncMcpElicitationMethodCallbackExample asyncExample = new AsyncMcpElicitationMethodCallbackExample();

	@Test
	void testValidMethodAccept() throws Exception {
		Method method = AsyncMcpElicitationMethodCallbackExample.class.getMethod("handleElicitationRequest",
				ElicitRequest.class);
		McpElicitation annotation = method.getAnnotation(McpElicitation.class);
		assertThat(annotation).isNotNull();

		AsyncMcpElicitationMethodCallback callback = AsyncMcpElicitationMethodCallback.builder()
			.method(method)
			.bean(asyncExample)
			.build();

		ElicitRequest request = ElicitationTestHelper.createSampleRequest();
		Mono<ElicitResult> resultMono = callback.apply(request);

		StepVerifier.create(resultMono).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.action()).isEqualTo(ElicitResult.Action.ACCEPT);
			assertThat(result.content()).isNotNull();
			assertThat(result.content()).containsEntry("userInput", "Example async user input");
			assertThat(result.content()).containsEntry("confirmed", true);
			assertThat(result.content()).containsKey("timestamp");
		}).verifyComplete();
	}

	@Test
	void testValidMethodDecline() throws Exception {
		Method method = AsyncMcpElicitationMethodCallbackExample.class.getMethod("handleDeclineElicitationRequest",
				ElicitRequest.class);
		McpElicitation annotation = method.getAnnotation(McpElicitation.class);
		assertThat(annotation).isNotNull();

		AsyncMcpElicitationMethodCallback callback = AsyncMcpElicitationMethodCallback.builder()
			.method(method)
			.bean(asyncExample)
			.build();

		ElicitRequest request = ElicitationTestHelper.createSampleRequest();
		Mono<ElicitResult> resultMono = callback.apply(request);

		StepVerifier.create(resultMono).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.action()).isEqualTo(ElicitResult.Action.DECLINE);
			assertThat(result.content()).isNull();
		}).verifyComplete();
	}

	@Test
	void testValidMethodCancel() throws Exception {
		Method method = AsyncMcpElicitationMethodCallbackExample.class.getMethod("handleCancelElicitationRequest",
				ElicitRequest.class);
		McpElicitation annotation = method.getAnnotation(McpElicitation.class);
		assertThat(annotation).isNotNull();

		AsyncMcpElicitationMethodCallback callback = AsyncMcpElicitationMethodCallback.builder()
			.method(method)
			.bean(asyncExample)
			.build();

		ElicitRequest request = ElicitationTestHelper.createSampleRequest();
		Mono<ElicitResult> resultMono = callback.apply(request);

		StepVerifier.create(resultMono).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.action()).isEqualTo(ElicitResult.Action.CANCEL);
			assertThat(result.content()).isNull();
		}).verifyComplete();
	}

	@Test
	void testSyncMethodWrappedInMono() throws Exception {
		Method method = AsyncMcpElicitationMethodCallbackExample.class.getMethod("handleSyncElicitationRequest",
				ElicitRequest.class);
		McpElicitation annotation = method.getAnnotation(McpElicitation.class);
		assertThat(annotation).isNotNull();

		assertThatThrownBy(() -> AsyncMcpElicitationMethodCallback.builder().method(method).bean(asyncExample).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method must return Mono<ElicitResult> or Mono<StructuredElicitResult>");

	}

	@Test
	void testNullRequest() throws Exception {
		Method method = AsyncMcpElicitationMethodCallbackExample.class.getMethod("handleElicitationRequest",
				ElicitRequest.class);
		McpElicitation annotation = method.getAnnotation(McpElicitation.class);
		assertThat(annotation).isNotNull();

		AsyncMcpElicitationMethodCallback callback = AsyncMcpElicitationMethodCallback.builder()
			.method(method)
			.bean(asyncExample)
			.build();

		Mono<ElicitResult> resultMono = callback.apply(null);

		StepVerifier.create(resultMono).expectErrorSatisfies(error -> {
			assertThat(error).isInstanceOf(IllegalArgumentException.class)
				.hasMessageContaining("Request must not be null");
		}).verify();
	}

	@Test
	void testInvalidReturnType() throws Exception {
		Method method = AsyncMcpElicitationMethodCallbackExample.class.getMethod("invalidReturnType",
				ElicitRequest.class);
		McpElicitation annotation = method.getAnnotation(McpElicitation.class);
		assertThat(annotation).isNotNull();

		assertThatThrownBy(() -> AsyncMcpElicitationMethodCallback.builder().method(method).bean(asyncExample).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method must return Mono<ElicitResult> or Mono<StructuredElicitResult>");
	}

	@Disabled
	@Test
	void testInvalidMonoReturnType() throws Exception {
		Method method = AsyncMcpElicitationMethodCallbackExample.class.getMethod("invalidMonoReturnType",
				ElicitRequest.class);
		McpElicitation annotation = method.getAnnotation(McpElicitation.class);
		assertThat(annotation).isNotNull();

		AsyncMcpElicitationMethodCallback callback = AsyncMcpElicitationMethodCallback.builder()
			.method(method)
			.bean(asyncExample)
			.build();

		ElicitRequest request = ElicitationTestHelper.createSampleRequest();

		assertThatThrownBy(() -> callback.apply(request)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method must return Mono<ElicitResult> or Mono<StructuredElicitResult>");

	}

	@Test
	void testInvalidParameterType() throws Exception {
		Method method = AsyncMcpElicitationMethodCallbackExample.class.getMethod("invalidParameterType", String.class);
		McpElicitation annotation = method.getAnnotation(McpElicitation.class);
		assertThat(annotation).isNotNull();

		assertThatThrownBy(() -> AsyncMcpElicitationMethodCallback.builder().method(method).bean(asyncExample).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Single parameter must be of type ElicitRequest");
	}

	@Test
	void testNoParameters() throws Exception {
		Method method = AsyncMcpElicitationMethodCallbackExample.class.getMethod("noParameters");
		McpElicitation annotation = method.getAnnotation(McpElicitation.class);
		assertThat(annotation).isNotNull();

		assertThatThrownBy(() -> AsyncMcpElicitationMethodCallback.builder().method(method).bean(asyncExample).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method must have at least 1 parameter");
	}

	@Test
	void testTooManyParameters() throws Exception {
		Method method = AsyncMcpElicitationMethodCallbackExample.class.getMethod("tooManyParameters",
				ElicitRequest.class, String.class);
		McpElicitation annotation = method.getAnnotation(McpElicitation.class);
		assertThat(annotation).isNotNull();

		assertThatThrownBy(() -> AsyncMcpElicitationMethodCallback.builder().method(method).bean(asyncExample).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Currently only methods with a single ElicitRequest parameter are supported");
	}

	@Test
	void testNullMethod() {
		assertThatThrownBy(() -> AsyncMcpElicitationMethodCallback.builder().method(null).bean(asyncExample).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method must not be null");
	}

	@Test
	void testNullBean() throws Exception {
		Method method = AsyncMcpElicitationMethodCallbackExample.class.getMethod("handleElicitationRequest",
				ElicitRequest.class);
		assertThatThrownBy(() -> AsyncMcpElicitationMethodCallback.builder().method(method).bean(null).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Bean must not be null");
	}

	@Test
	void testMethodInvocationError() throws Exception {
		// Create a method that will throw an exception when invoked
		Method method = AsyncMcpElicitationMethodCallbackExample.class.getMethod("handleElicitationRequest",
				ElicitRequest.class);
		McpElicitation annotation = method.getAnnotation(McpElicitation.class);
		assertThat(annotation).isNotNull();

		AsyncMcpElicitationMethodCallback callback = AsyncMcpElicitationMethodCallback.builder()
			.method(method)
			.bean(new AsyncMcpElicitationMethodCallbackExample() {
				@Override
				public Mono<ElicitResult> handleElicitationRequest(ElicitRequest request) {
					throw new RuntimeException("Test exception");
				}
			})
			.build();

		ElicitRequest request = ElicitationTestHelper.createSampleRequest();
		Mono<ElicitResult> resultMono = callback.apply(request);

		StepVerifier.create(resultMono).expectErrorSatisfies(error -> {
			assertThat(error).isInstanceOf(McpElicitationMethodException.class)
				.hasMessageContaining("Error invoking elicitation method")
				.hasCauseInstanceOf(java.lang.reflect.InvocationTargetException.class)
				.satisfies(e -> {
					Throwable cause = e.getCause().getCause();
					assertThat(cause).isInstanceOf(RuntimeException.class);
					assertThat(cause.getMessage()).isEqualTo("Test exception");
				});
		}).verify();
	}

	@Test
	void testBuilderValidation() {
		// Test that builder validates required fields
		assertThatThrownBy(() -> AsyncMcpElicitationMethodCallback.builder().build())
			.isInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void testCustomRequestContent() throws Exception {
		Method method = AsyncMcpElicitationMethodCallbackExample.class.getMethod("handleElicitationRequest",
				ElicitRequest.class);

		AsyncMcpElicitationMethodCallback callback = AsyncMcpElicitationMethodCallback.builder()
			.method(method)
			.bean(asyncExample)
			.build();

		ElicitRequest customRequest = ElicitationTestHelper.createSampleRequest("Custom async prompt",
				Map.of("customKey", "customValue", "priority", "high", "async", true));
		Mono<ElicitResult> resultMono = callback.apply(customRequest);

		StepVerifier.create(resultMono).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.action()).isEqualTo(ElicitResult.Action.ACCEPT);
			assertThat(result.content()).isNotNull();
			assertThat(result.content()).containsEntry("userInput", "Example async user input");
			assertThat(result.content()).containsEntry("confirmed", true);
			assertThat(result.content()).containsKey("timestamp");
		}).verifyComplete();
	}

	@Test
	void testMonoErrorHandling() throws Exception {
		Method method = AsyncMcpElicitationMethodCallbackExample.class.getMethod("handleElicitationRequest",
				ElicitRequest.class);

		AsyncMcpElicitationMethodCallback callback = AsyncMcpElicitationMethodCallback.builder()
			.method(method)
			.bean(new AsyncMcpElicitationMethodCallbackExample() {
				@Override
				public Mono<ElicitResult> handleElicitationRequest(ElicitRequest request) {
					return Mono.error(new RuntimeException("Async test exception"));
				}
			})
			.build();

		ElicitRequest request = ElicitationTestHelper.createSampleRequest();
		Mono<ElicitResult> resultMono = callback.apply(request);

		StepVerifier.create(resultMono).expectErrorSatisfies(error -> {
			assertThat(error).isInstanceOf(RuntimeException.class).hasMessage("Async test exception");
		}).verify();
	}

}
