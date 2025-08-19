/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.complete;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.BiFunction;

import io.modelcontextprotocol.server.McpTransportContext;
import io.modelcontextprotocol.spec.McpSchema.CompleteRequest;
import io.modelcontextprotocol.spec.McpSchema.CompleteResult;
import io.modelcontextprotocol.spec.McpSchema.CompleteResult.CompleteCompletion;
import io.modelcontextprotocol.spec.McpSchema.PromptReference;
import io.modelcontextprotocol.spec.McpSchema.ResourceReference;
import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpComplete;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link SyncStatelessMcpCompleteMethodCallback}.
 *
 * @author Christian Tzolov
 */
public class SyncStatelessMcpCompleteMethodCallbackTests {

	private static class TestCompleteProvider {

		public CompleteResult getCompletionWithRequest(CompleteRequest request) {
			return new CompleteResult(
					new CompleteCompletion(List.of("Completion for " + request.argument().value()), 1, false));
		}

		public CompleteResult getCompletionWithContext(McpTransportContext context, CompleteRequest request) {
			return new CompleteResult(new CompleteCompletion(
					List.of("Completion with context for " + request.argument().value()), 1, false));
		}

		public CompleteResult getCompletionWithArgument(CompleteRequest.CompleteArgument argument) {
			return new CompleteResult(
					new CompleteCompletion(List.of("Completion from argument: " + argument.value()), 1, false));
		}

		public CompleteResult getCompletionWithValue(String value) {
			return new CompleteResult(new CompleteCompletion(List.of("Completion from value: " + value), 1, false));
		}

		@McpComplete(prompt = "test-prompt")
		public CompleteResult getCompletionWithPrompt(CompleteRequest request) {
			return new CompleteResult(new CompleteCompletion(
					List.of("Completion for prompt with: " + request.argument().value()), 1, false));
		}

		@McpComplete(uri = "test://{variable}")
		public CompleteResult getCompletionWithUri(CompleteRequest request) {
			return new CompleteResult(new CompleteCompletion(
					List.of("Completion for URI with: " + request.argument().value()), 1, false));
		}

		public CompleteCompletion getCompletionObject(CompleteRequest request) {
			return new CompleteCompletion(List.of("Completion object for: " + request.argument().value()), 1, false);
		}

		public List<String> getCompletionList(CompleteRequest request) {
			return List.of("List item 1 for: " + request.argument().value(),
					"List item 2 for: " + request.argument().value());
		}

		public String getCompletionString(CompleteRequest request) {
			return "String completion for: " + request.argument().value();
		}

		public void invalidReturnType(CompleteRequest request) {
			// Invalid return type
		}

		public CompleteResult invalidParameters(int value) {
			return new CompleteResult(new CompleteCompletion(List.of(), 0, false));
		}

		public CompleteResult tooManyParameters(McpTransportContext context, CompleteRequest request, String extraParam,
				String extraParam2) {
			return new CompleteResult(new CompleteCompletion(List.of(), 0, false));
		}

		public CompleteResult invalidParameterType(Object invalidParam) {
			return new CompleteResult(new CompleteCompletion(List.of(), 0, false));
		}

		public CompleteResult duplicateContextParameters(McpTransportContext context1, McpTransportContext context2) {
			return new CompleteResult(new CompleteCompletion(List.of(), 0, false));
		}

		public CompleteResult duplicateRequestParameters(CompleteRequest request1, CompleteRequest request2) {
			return new CompleteResult(new CompleteCompletion(List.of(), 0, false));
		}

		public CompleteResult duplicateArgumentParameters(CompleteRequest.CompleteArgument arg1,
				CompleteRequest.CompleteArgument arg2) {
			return new CompleteResult(new CompleteCompletion(List.of(), 0, false));
		}

	}

	@Test
	public void testCallbackWithRequestParameter() throws Exception {
		TestCompleteProvider provider = new TestCompleteProvider();
		Method method = TestCompleteProvider.class.getMethod("getCompletionWithRequest", CompleteRequest.class);

		BiFunction<McpTransportContext, CompleteRequest, CompleteResult> callback = SyncStatelessMcpCompleteMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.prompt("test-prompt")
			.build();

		McpTransportContext context = mock(McpTransportContext.class);
		CompleteRequest request = new CompleteRequest(new PromptReference("test-prompt"),
				new CompleteRequest.CompleteArgument("test", "value"));

		CompleteResult result = callback.apply(context, request);

		assertThat(result).isNotNull();
		assertThat(result.completion()).isNotNull();
		assertThat(result.completion().values()).hasSize(1);
		assertThat(result.completion().values().get(0)).isEqualTo("Completion for value");
	}

	@Test
	public void testCallbackWithContextAndRequestParameters() throws Exception {
		TestCompleteProvider provider = new TestCompleteProvider();
		Method method = TestCompleteProvider.class.getMethod("getCompletionWithContext", McpTransportContext.class,
				CompleteRequest.class);

		BiFunction<McpTransportContext, CompleteRequest, CompleteResult> callback = SyncStatelessMcpCompleteMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.prompt("test-prompt")
			.build();

		McpTransportContext context = mock(McpTransportContext.class);
		CompleteRequest request = new CompleteRequest(new PromptReference("test-prompt"),
				new CompleteRequest.CompleteArgument("test", "value"));

		CompleteResult result = callback.apply(context, request);

		assertThat(result).isNotNull();
		assertThat(result.completion()).isNotNull();
		assertThat(result.completion().values()).hasSize(1);
		assertThat(result.completion().values().get(0)).isEqualTo("Completion with context for value");
	}

	@Test
	public void testCallbackWithArgumentParameter() throws Exception {
		TestCompleteProvider provider = new TestCompleteProvider();
		Method method = TestCompleteProvider.class.getMethod("getCompletionWithArgument",
				CompleteRequest.CompleteArgument.class);

		BiFunction<McpTransportContext, CompleteRequest, CompleteResult> callback = SyncStatelessMcpCompleteMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.prompt("test-prompt")
			.build();

		McpTransportContext context = mock(McpTransportContext.class);
		CompleteRequest request = new CompleteRequest(new PromptReference("test-prompt"),
				new CompleteRequest.CompleteArgument("test", "value"));

		CompleteResult result = callback.apply(context, request);

		assertThat(result).isNotNull();
		assertThat(result.completion()).isNotNull();
		assertThat(result.completion().values()).hasSize(1);
		assertThat(result.completion().values().get(0)).isEqualTo("Completion from argument: value");
	}

	@Test
	public void testCallbackWithValueParameter() throws Exception {
		TestCompleteProvider provider = new TestCompleteProvider();
		Method method = TestCompleteProvider.class.getMethod("getCompletionWithValue", String.class);

		BiFunction<McpTransportContext, CompleteRequest, CompleteResult> callback = SyncStatelessMcpCompleteMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.prompt("test-prompt")
			.build();

		McpTransportContext context = mock(McpTransportContext.class);
		CompleteRequest request = new CompleteRequest(new PromptReference("test-prompt"),
				new CompleteRequest.CompleteArgument("test", "value"));

		CompleteResult result = callback.apply(context, request);

		assertThat(result).isNotNull();
		assertThat(result.completion()).isNotNull();
		assertThat(result.completion().values()).hasSize(1);
		assertThat(result.completion().values().get(0)).isEqualTo("Completion from value: value");
	}

	@Test
	public void testCallbackWithPromptAnnotation() throws Exception {
		TestCompleteProvider provider = new TestCompleteProvider();
		Method method = TestCompleteProvider.class.getMethod("getCompletionWithPrompt", CompleteRequest.class);
		McpComplete completeAnnotation = method.getAnnotation(McpComplete.class);

		BiFunction<McpTransportContext, CompleteRequest, CompleteResult> callback = SyncStatelessMcpCompleteMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.complete(completeAnnotation)
			.build();

		McpTransportContext context = mock(McpTransportContext.class);
		CompleteRequest request = new CompleteRequest(new PromptReference("test-prompt"),
				new CompleteRequest.CompleteArgument("test", "value"));

		CompleteResult result = callback.apply(context, request);

		assertThat(result).isNotNull();
		assertThat(result.completion()).isNotNull();
		assertThat(result.completion().values()).hasSize(1);
		assertThat(result.completion().values().get(0)).isEqualTo("Completion for prompt with: value");
	}

	@Test
	public void testCallbackWithUriAnnotation() throws Exception {
		TestCompleteProvider provider = new TestCompleteProvider();
		Method method = TestCompleteProvider.class.getMethod("getCompletionWithUri", CompleteRequest.class);
		McpComplete completeAnnotation = method.getAnnotation(McpComplete.class);

		BiFunction<McpTransportContext, CompleteRequest, CompleteResult> callback = SyncStatelessMcpCompleteMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.complete(completeAnnotation)
			.build();

		McpTransportContext context = mock(McpTransportContext.class);
		CompleteRequest request = new CompleteRequest(new ResourceReference("test://value"),
				new CompleteRequest.CompleteArgument("variable", "value"));

		CompleteResult result = callback.apply(context, request);

		assertThat(result).isNotNull();
		assertThat(result.completion()).isNotNull();
		assertThat(result.completion().values()).hasSize(1);
		assertThat(result.completion().values().get(0)).isEqualTo("Completion for URI with: value");
	}

	@Test
	public void testCallbackWithCompletionObject() throws Exception {
		TestCompleteProvider provider = new TestCompleteProvider();
		Method method = TestCompleteProvider.class.getMethod("getCompletionObject", CompleteRequest.class);

		BiFunction<McpTransportContext, CompleteRequest, CompleteResult> callback = SyncStatelessMcpCompleteMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.prompt("test-prompt")
			.build();

		McpTransportContext context = mock(McpTransportContext.class);
		CompleteRequest request = new CompleteRequest(new PromptReference("test-prompt"),
				new CompleteRequest.CompleteArgument("test", "value"));

		CompleteResult result = callback.apply(context, request);

		assertThat(result).isNotNull();
		assertThat(result.completion()).isNotNull();
		assertThat(result.completion().values()).hasSize(1);
		assertThat(result.completion().values().get(0)).isEqualTo("Completion object for: value");
	}

	@Test
	public void testCallbackWithCompletionList() throws Exception {
		TestCompleteProvider provider = new TestCompleteProvider();
		Method method = TestCompleteProvider.class.getMethod("getCompletionList", CompleteRequest.class);

		BiFunction<McpTransportContext, CompleteRequest, CompleteResult> callback = SyncStatelessMcpCompleteMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.prompt("test-prompt")
			.build();

		McpTransportContext context = mock(McpTransportContext.class);
		CompleteRequest request = new CompleteRequest(new PromptReference("test-prompt"),
				new CompleteRequest.CompleteArgument("test", "value"));

		CompleteResult result = callback.apply(context, request);

		assertThat(result).isNotNull();
		assertThat(result.completion()).isNotNull();
		assertThat(result.completion().values()).hasSize(2);
		assertThat(result.completion().values().get(0)).isEqualTo("List item 1 for: value");
		assertThat(result.completion().values().get(1)).isEqualTo("List item 2 for: value");
	}

	@Test
	public void testCallbackWithCompletionString() throws Exception {
		TestCompleteProvider provider = new TestCompleteProvider();
		Method method = TestCompleteProvider.class.getMethod("getCompletionString", CompleteRequest.class);

		BiFunction<McpTransportContext, CompleteRequest, CompleteResult> callback = SyncStatelessMcpCompleteMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.prompt("test-prompt")
			.build();

		McpTransportContext context = mock(McpTransportContext.class);
		CompleteRequest request = new CompleteRequest(new PromptReference("test-prompt"),
				new CompleteRequest.CompleteArgument("test", "value"));

		CompleteResult result = callback.apply(context, request);

		assertThat(result).isNotNull();
		assertThat(result.completion()).isNotNull();
		assertThat(result.completion().values()).hasSize(1);
		assertThat(result.completion().values().get(0)).isEqualTo("String completion for: value");
	}

	@Test
	public void testInvalidReturnType() throws Exception {
		TestCompleteProvider provider = new TestCompleteProvider();
		Method method = TestCompleteProvider.class.getMethod("invalidReturnType", CompleteRequest.class);

		assertThatThrownBy(() -> SyncStatelessMcpCompleteMethodCallback.builder()
			.method(method)
			.bean(provider)
			.prompt("test-prompt")
			.build()).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining(
					"Method must return either CompleteResult, CompleteCompletion, List<String>, or String");
	}

	@Test
	public void testInvalidParameters() throws Exception {
		TestCompleteProvider provider = new TestCompleteProvider();
		Method method = TestCompleteProvider.class.getMethod("invalidParameters", int.class);

		assertThatThrownBy(() -> SyncStatelessMcpCompleteMethodCallback.builder()
			.method(method)
			.bean(provider)
			.prompt("test-prompt")
			.build()).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method parameters must be exchange, CompleteRequest, CompleteArgument, or String");
	}

	@Test
	public void testTooManyParameters() throws Exception {
		TestCompleteProvider provider = new TestCompleteProvider();
		Method method = TestCompleteProvider.class.getMethod("tooManyParameters", McpTransportContext.class,
				CompleteRequest.class, String.class, String.class);

		assertThatThrownBy(() -> SyncStatelessMcpCompleteMethodCallback.builder()
			.method(method)
			.bean(provider)
			.prompt("test-prompt")
			.build()).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method can have at most 3 input parameters");
	}

	@Test
	public void testInvalidParameterType() throws Exception {
		TestCompleteProvider provider = new TestCompleteProvider();
		Method method = TestCompleteProvider.class.getMethod("invalidParameterType", Object.class);

		assertThatThrownBy(() -> SyncStatelessMcpCompleteMethodCallback.builder()
			.method(method)
			.bean(provider)
			.prompt("test-prompt")
			.build()).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method parameters must be exchange, CompleteRequest, CompleteArgument, or String");
	}

	@Test
	public void testDuplicateContextParameters() throws Exception {
		TestCompleteProvider provider = new TestCompleteProvider();
		Method method = TestCompleteProvider.class.getMethod("duplicateContextParameters", McpTransportContext.class,
				McpTransportContext.class);

		assertThatThrownBy(() -> SyncStatelessMcpCompleteMethodCallback.builder()
			.method(method)
			.bean(provider)
			.prompt("test-prompt")
			.build()).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method cannot have more than one exchange parameter");
	}

	@Test
	public void testDuplicateRequestParameters() throws Exception {
		TestCompleteProvider provider = new TestCompleteProvider();
		Method method = TestCompleteProvider.class.getMethod("duplicateRequestParameters", CompleteRequest.class,
				CompleteRequest.class);

		assertThatThrownBy(() -> SyncStatelessMcpCompleteMethodCallback.builder()
			.method(method)
			.bean(provider)
			.prompt("test-prompt")
			.build()).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method cannot have more than one CompleteRequest parameter");
	}

	@Test
	public void testDuplicateArgumentParameters() throws Exception {
		TestCompleteProvider provider = new TestCompleteProvider();
		Method method = TestCompleteProvider.class.getMethod("duplicateArgumentParameters",
				CompleteRequest.CompleteArgument.class, CompleteRequest.CompleteArgument.class);

		assertThatThrownBy(() -> SyncStatelessMcpCompleteMethodCallback.builder()
			.method(method)
			.bean(provider)
			.prompt("test-prompt")
			.build()).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method cannot have more than one CompleteArgument parameter");
	}

	@Test
	public void testMissingPromptAndUri() throws Exception {
		TestCompleteProvider provider = new TestCompleteProvider();
		Method method = TestCompleteProvider.class.getMethod("getCompletionWithRequest", CompleteRequest.class);

		assertThatThrownBy(() -> SyncStatelessMcpCompleteMethodCallback.builder().method(method).bean(provider).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Either prompt or uri must be provided");
	}

	@Test
	public void testBothPromptAndUri() throws Exception {
		TestCompleteProvider provider = new TestCompleteProvider();
		Method method = TestCompleteProvider.class.getMethod("getCompletionWithRequest", CompleteRequest.class);

		assertThatThrownBy(() -> SyncStatelessMcpCompleteMethodCallback.builder()
			.method(method)
			.bean(provider)
			.prompt("test-prompt")
			.uri("test://resource")
			.build()).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Only one of prompt or uri can be provided");
	}

	@Test
	public void testNullRequest() throws Exception {
		TestCompleteProvider provider = new TestCompleteProvider();
		Method method = TestCompleteProvider.class.getMethod("getCompletionWithRequest", CompleteRequest.class);

		BiFunction<McpTransportContext, CompleteRequest, CompleteResult> callback = SyncStatelessMcpCompleteMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.prompt("test-prompt")
			.build();

		McpTransportContext context = mock(McpTransportContext.class);

		assertThatThrownBy(() -> callback.apply(context, null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Request must not be null");
	}

}
