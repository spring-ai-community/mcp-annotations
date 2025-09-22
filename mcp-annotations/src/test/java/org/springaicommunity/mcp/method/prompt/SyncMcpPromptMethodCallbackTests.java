/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.prompt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpArg;
import org.springaicommunity.mcp.annotation.McpMeta;
import org.springaicommunity.mcp.annotation.McpProgressToken;
import org.springaicommunity.mcp.annotation.McpPrompt;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema.GetPromptRequest;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.Prompt;
import io.modelcontextprotocol.spec.McpSchema.PromptArgument;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

/**
 * Tests for {@link SyncMcpPromptMethodCallback}.
 *
 * @author Christian Tzolov
 */
public class SyncMcpPromptMethodCallbackTests {

	private static class TestPromptProvider {

		@McpPrompt(name = "failing-prompt", description = "A prompt that throws an exception")
		public GetPromptResult getFailingPrompt(GetPromptRequest request) {
			throw new RuntimeException("Test exception");
		}

		@McpPrompt(name = "greeting", description = "A simple greeting prompt")
		public GetPromptResult getPromptWithRequest(GetPromptRequest request) {
			return new GetPromptResult("Greeting prompt",
					List.of(new PromptMessage(Role.ASSISTANT, new TextContent("Hello from " + request.name()))));
		}

		@McpPrompt(name = "exchange-greeting", description = "A greeting prompt with exchange")
		public GetPromptResult getPromptWithExchange(McpSyncServerExchange exchange, GetPromptRequest request) {
			return new GetPromptResult("Greeting with exchange", List
				.of(new PromptMessage(Role.ASSISTANT, new TextContent("Hello with exchange from " + request.name()))));
		}

		@McpPrompt(name = "arguments-greeting", description = "A greeting prompt with arguments")
		public GetPromptResult getPromptWithArguments(Map<String, Object> arguments) {
			String name = arguments.containsKey("name") ? arguments.get("name").toString() : "unknown";
			return new GetPromptResult("Greeting with arguments",
					List.of(new PromptMessage(Role.ASSISTANT, new TextContent("Hello " + name + " from arguments"))));
		}

		@McpPrompt(name = "individual-args", description = "A prompt with individual arguments")
		public GetPromptResult getPromptWithIndividualArgs(
				@McpArg(name = "name", description = "The user's name", required = true) String name,
				@McpArg(name = "age", description = "The user's age", required = true) Integer age) {
			return new GetPromptResult("Individual arguments prompt", List.of(new PromptMessage(Role.ASSISTANT,
					new TextContent("Hello " + name + ", you are " + age + " years old"))));
		}

		@McpPrompt(name = "mixed-args", description = "A prompt with mixed argument types")
		public GetPromptResult getPromptWithMixedArgs(McpSyncServerExchange exchange,
				@McpArg(name = "name", description = "The user's name", required = true) String name,
				@McpArg(name = "age", description = "The user's age", required = true) Integer age) {
			return new GetPromptResult("Mixed arguments prompt", List.of(new PromptMessage(Role.ASSISTANT,
					new TextContent("Hello " + name + ", you are " + age + " years old (with exchange)"))));
		}

		@McpPrompt(name = "list-messages", description = "A prompt returning a list of messages")
		public List<PromptMessage> getPromptMessagesList(GetPromptRequest request) {
			return List.of(new PromptMessage(Role.ASSISTANT, new TextContent("Message 1 for " + request.name())),
					new PromptMessage(Role.ASSISTANT, new TextContent("Message 2 for " + request.name())));
		}

		@McpPrompt(name = "string-prompt", description = "A prompt returning a string")
		public String getStringPrompt(GetPromptRequest request) {
			return "Simple string response for " + request.name();
		}

		@McpPrompt(name = "single-message", description = "A prompt returning a single message")
		public PromptMessage getSingleMessage(GetPromptRequest request) {
			return new PromptMessage(Role.ASSISTANT, new TextContent("Single message for " + request.name()));
		}

		@McpPrompt(name = "string-list", description = "A prompt returning a list of strings")
		public List<String> getStringList(GetPromptRequest request) {
			return List.of("String 1 for " + request.name(), "String 2 for " + request.name(),
					"String 3 for " + request.name());
		}

		public void invalidReturnType(GetPromptRequest request) {
			// Invalid return type
		}

		public GetPromptResult duplicateExchangeParameters(McpSyncServerExchange exchange1,
				McpSyncServerExchange exchange2) {
			return new GetPromptResult("Invalid", List.of());
		}

		public GetPromptResult duplicateRequestParameters(GetPromptRequest request1, GetPromptRequest request2) {
			return new GetPromptResult("Invalid", List.of());
		}

		public GetPromptResult duplicateMapParameters(Map<String, Object> args1, Map<String, Object> args2) {
			return new GetPromptResult("Invalid", List.of());
		}

		@McpPrompt(name = "progress-token", description = "A prompt with progress token")
		public GetPromptResult getPromptWithProgressToken(@McpProgressToken String progressToken,
				@McpArg(name = "name", description = "The user's name", required = true) String name) {
			String tokenInfo = progressToken != null ? " (token: " + progressToken + ")" : " (no token)";
			return new GetPromptResult("Progress token prompt",
					List.of(new PromptMessage(Role.ASSISTANT, new TextContent("Hello " + name + tokenInfo))));
		}

		@McpPrompt(name = "mixed-with-progress", description = "A prompt with mixed args and progress token")
		public GetPromptResult getPromptWithMixedAndProgress(McpSyncServerExchange exchange,
				@McpProgressToken String progressToken,
				@McpArg(name = "name", description = "The user's name", required = true) String name,
				GetPromptRequest request) {
			String tokenInfo = progressToken != null ? " (token: " + progressToken + ")" : " (no token)";
			return new GetPromptResult("Mixed with progress prompt", List.of(new PromptMessage(Role.ASSISTANT,
					new TextContent("Hello " + name + " from " + request.name() + tokenInfo))));
		}

		public GetPromptResult duplicateProgressTokenParameters(@McpProgressToken String token1,
				@McpProgressToken String token2) {
			return new GetPromptResult("Invalid", List.of());
		}

		@McpPrompt(name = "meta-prompt", description = "A prompt with meta parameter")
		public GetPromptResult getPromptWithMeta(
				@McpArg(name = "name", description = "The user's name", required = true) String name, McpMeta meta) {
			String metaInfo = meta != null && meta.meta() != null ? meta.meta().toString() : "null";
			return new GetPromptResult("Meta prompt", List
				.of(new PromptMessage(Role.ASSISTANT, new TextContent("Hello " + name + ", Meta: " + metaInfo))));
		}

		@McpPrompt(name = "mixed-with-meta", description = "A prompt with mixed args and meta")
		public GetPromptResult getPromptWithMixedAndMeta(McpSyncServerExchange exchange,
				@McpArg(name = "name", description = "The user's name", required = true) String name, McpMeta meta,
				GetPromptRequest request) {
			String metaInfo = meta != null && meta.meta() != null ? meta.meta().toString() : "null";
			return new GetPromptResult("Mixed with meta prompt", List.of(new PromptMessage(Role.ASSISTANT,
					new TextContent("Hello " + name + " from " + request.name() + ", Meta: " + metaInfo))));
		}

		public GetPromptResult duplicateMetaParameters(McpMeta meta1, McpMeta meta2) {
			return new GetPromptResult("Invalid", List.of());
		}

	}

	private Prompt createTestPrompt(String name, String description) {
		return new Prompt(name, description, List.of(new PromptArgument("name", "User's name", true),
				new PromptArgument("age", "User's age", false)));
	}

	@Test
	public void testMethodInvocationError() throws Exception {
		TestPromptProvider provider = new TestPromptProvider();
		Method method = TestPromptProvider.class.getMethod("getFailingPrompt", GetPromptRequest.class);

		Prompt prompt = createTestPrompt("failing-prompt", "A prompt that throws an exception");

		BiFunction<McpSyncServerExchange, GetPromptRequest, GetPromptResult> callback = SyncMcpPromptMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.prompt(prompt)
			.build();

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		Map<String, Object> args = new HashMap<>();
		args.put("name", "John");
		GetPromptRequest request = new GetPromptRequest("failing-prompt", args);

		// The new error handling should throw McpError instead of
		// McpPromptMethodException
		assertThatThrownBy(() -> callback.apply(exchange, request)).isInstanceOf(McpError.class)
			.hasMessageContaining("Error invoking prompt method");
	}

	@Test
	public void testCallbackWithRequestParameter() throws Exception {
		TestPromptProvider provider = new TestPromptProvider();
		Method method = TestPromptProvider.class.getMethod("getPromptWithRequest", GetPromptRequest.class);

		Prompt prompt = createTestPrompt("greeting", "A simple greeting prompt");

		BiFunction<McpSyncServerExchange, GetPromptRequest, GetPromptResult> callback = SyncMcpPromptMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.prompt(prompt)
			.build();

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		Map<String, Object> args = new HashMap<>();
		args.put("name", "John");
		GetPromptRequest request = new GetPromptRequest("greeting", args);

		GetPromptResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.description()).isEqualTo("Greeting prompt");
		assertThat(result.messages()).hasSize(1);
		PromptMessage message = result.messages().get(0);
		assertThat(message.role()).isEqualTo(Role.ASSISTANT);
		assertThat(((TextContent) message.content()).text()).isEqualTo("Hello from greeting");
	}

	@Test
	public void testCallbackWithExchangeAndRequestParameters() throws Exception {
		TestPromptProvider provider = new TestPromptProvider();
		Method method = TestPromptProvider.class.getMethod("getPromptWithExchange", McpSyncServerExchange.class,
				GetPromptRequest.class);

		Prompt prompt = createTestPrompt("exchange-greeting", "A greeting prompt with exchange");

		BiFunction<McpSyncServerExchange, GetPromptRequest, GetPromptResult> callback = SyncMcpPromptMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.prompt(prompt)
			.build();

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		Map<String, Object> args = new HashMap<>();
		args.put("name", "John");
		GetPromptRequest request = new GetPromptRequest("exchange-greeting", args);

		GetPromptResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.description()).isEqualTo("Greeting with exchange");
		assertThat(result.messages()).hasSize(1);
		PromptMessage message = result.messages().get(0);
		assertThat(message.role()).isEqualTo(Role.ASSISTANT);
		assertThat(((TextContent) message.content()).text()).isEqualTo("Hello with exchange from exchange-greeting");
	}

	@Test
	public void testCallbackWithArgumentsMap() throws Exception {
		TestPromptProvider provider = new TestPromptProvider();
		Method method = TestPromptProvider.class.getMethod("getPromptWithArguments", Map.class);

		Prompt prompt = createTestPrompt("arguments-greeting", "A greeting prompt with arguments");

		BiFunction<McpSyncServerExchange, GetPromptRequest, GetPromptResult> callback = SyncMcpPromptMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.prompt(prompt)
			.build();

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		Map<String, Object> args = new HashMap<>();
		args.put("name", "John");
		GetPromptRequest request = new GetPromptRequest("arguments-greeting", args);

		GetPromptResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.description()).isEqualTo("Greeting with arguments");
		assertThat(result.messages()).hasSize(1);
		PromptMessage message = result.messages().get(0);
		assertThat(message.role()).isEqualTo(Role.ASSISTANT);
		assertThat(((TextContent) message.content()).text()).isEqualTo("Hello John from arguments");
	}

	@Test
	public void testCallbackWithIndividualArguments() throws Exception {
		TestPromptProvider provider = new TestPromptProvider();
		Method method = TestPromptProvider.class.getMethod("getPromptWithIndividualArgs", String.class, Integer.class);

		Prompt prompt = createTestPrompt("individual-args", "A prompt with individual arguments");

		BiFunction<McpSyncServerExchange, GetPromptRequest, GetPromptResult> callback = SyncMcpPromptMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.prompt(prompt)
			.build();

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		Map<String, Object> args = new HashMap<>();
		args.put("name", "John");
		args.put("age", 30);
		GetPromptRequest request = new GetPromptRequest("individual-args", args);

		GetPromptResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.description()).isEqualTo("Individual arguments prompt");
		assertThat(result.messages()).hasSize(1);
		PromptMessage message = result.messages().get(0);
		assertThat(message.role()).isEqualTo(Role.ASSISTANT);
		assertThat(((TextContent) message.content()).text()).isEqualTo("Hello John, you are 30 years old");
	}

	@Test
	public void testCallbackWithMixedArguments() throws Exception {
		TestPromptProvider provider = new TestPromptProvider();
		Method method = TestPromptProvider.class.getMethod("getPromptWithMixedArgs", McpSyncServerExchange.class,
				String.class, Integer.class);

		Prompt prompt = createTestPrompt("mixed-args", "A prompt with mixed argument types");

		BiFunction<McpSyncServerExchange, GetPromptRequest, GetPromptResult> callback = SyncMcpPromptMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.prompt(prompt)
			.build();

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		Map<String, Object> args = new HashMap<>();
		args.put("name", "John");
		args.put("age", 30);
		GetPromptRequest request = new GetPromptRequest("mixed-args", args);

		GetPromptResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.description()).isEqualTo("Mixed arguments prompt");
		assertThat(result.messages()).hasSize(1);
		PromptMessage message = result.messages().get(0);
		assertThat(message.role()).isEqualTo(Role.ASSISTANT);
		assertThat(((TextContent) message.content()).text())
			.isEqualTo("Hello John, you are 30 years old (with exchange)");
	}

	@Test
	public void testCallbackWithMessagesList() throws Exception {
		TestPromptProvider provider = new TestPromptProvider();
		Method method = TestPromptProvider.class.getMethod("getPromptMessagesList", GetPromptRequest.class);

		Prompt prompt = createTestPrompt("list-messages", "A prompt returning a list of messages");

		BiFunction<McpSyncServerExchange, GetPromptRequest, GetPromptResult> callback = SyncMcpPromptMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.prompt(prompt)
			.build();

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		Map<String, Object> args = new HashMap<>();
		args.put("name", "John");
		GetPromptRequest request = new GetPromptRequest("list-messages", args);

		GetPromptResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.description()).isNull();
		assertThat(result.messages()).hasSize(2);
		PromptMessage message1 = result.messages().get(0);
		PromptMessage message2 = result.messages().get(1);
		assertThat(message1.role()).isEqualTo(Role.ASSISTANT);
		assertThat(message2.role()).isEqualTo(Role.ASSISTANT);
		assertThat(((TextContent) message1.content()).text()).isEqualTo("Message 1 for list-messages");
		assertThat(((TextContent) message2.content()).text()).isEqualTo("Message 2 for list-messages");
	}

	@Test
	public void testCallbackWithStringReturn() throws Exception {
		TestPromptProvider provider = new TestPromptProvider();
		Method method = TestPromptProvider.class.getMethod("getStringPrompt", GetPromptRequest.class);

		Prompt prompt = createTestPrompt("string-prompt", "A prompt returning a string");

		BiFunction<McpSyncServerExchange, GetPromptRequest, GetPromptResult> callback = SyncMcpPromptMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.prompt(prompt)
			.build();

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		Map<String, Object> args = new HashMap<>();
		args.put("name", "John");
		GetPromptRequest request = new GetPromptRequest("string-prompt", args);

		GetPromptResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.messages()).hasSize(1);
		PromptMessage message = result.messages().get(0);
		assertThat(message.role()).isEqualTo(Role.ASSISTANT);
		assertThat(((TextContent) message.content()).text()).isEqualTo("Simple string response for string-prompt");
	}

	@Test
	public void testCallbackWithSingleMessage() throws Exception {
		TestPromptProvider provider = new TestPromptProvider();
		Method method = TestPromptProvider.class.getMethod("getSingleMessage", GetPromptRequest.class);

		Prompt prompt = createTestPrompt("single-message", "A prompt returning a single message");

		BiFunction<McpSyncServerExchange, GetPromptRequest, GetPromptResult> callback = SyncMcpPromptMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.prompt(prompt)
			.build();

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		Map<String, Object> args = new HashMap<>();
		args.put("name", "John");
		GetPromptRequest request = new GetPromptRequest("single-message", args);

		GetPromptResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.description()).isNull();
		assertThat(result.messages()).hasSize(1);
		PromptMessage message = result.messages().get(0);
		assertThat(message.role()).isEqualTo(Role.ASSISTANT);
		assertThat(((TextContent) message.content()).text()).isEqualTo("Single message for single-message");
	}

	@Test
	public void testCallbackWithStringList() throws Exception {
		TestPromptProvider provider = new TestPromptProvider();
		Method method = TestPromptProvider.class.getMethod("getStringList", GetPromptRequest.class);

		Prompt prompt = createTestPrompt("string-list", "A prompt returning a list of strings");

		BiFunction<McpSyncServerExchange, GetPromptRequest, GetPromptResult> callback = SyncMcpPromptMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.prompt(prompt)
			.build();

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		Map<String, Object> args = new HashMap<>();
		args.put("name", "John");
		GetPromptRequest request = new GetPromptRequest("string-list", args);

		GetPromptResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.description()).isNull();
		assertThat(result.messages()).hasSize(3);

		PromptMessage message1 = result.messages().get(0);
		PromptMessage message2 = result.messages().get(1);
		PromptMessage message3 = result.messages().get(2);

		assertThat(message1.role()).isEqualTo(Role.ASSISTANT);
		assertThat(message2.role()).isEqualTo(Role.ASSISTANT);
		assertThat(message3.role()).isEqualTo(Role.ASSISTANT);

		assertThat(((TextContent) message1.content()).text()).isEqualTo("String 1 for string-list");
		assertThat(((TextContent) message2.content()).text()).isEqualTo("String 2 for string-list");
		assertThat(((TextContent) message3.content()).text()).isEqualTo("String 3 for string-list");
	}

	@Test
	public void testInvalidReturnType() throws Exception {
		TestPromptProvider provider = new TestPromptProvider();
		Method method = TestPromptProvider.class.getMethod("invalidReturnType", GetPromptRequest.class);

		Prompt prompt = createTestPrompt("invalid", "Invalid return type");

		assertThatThrownBy(
				() -> SyncMcpPromptMethodCallback.builder().method(method).bean(provider).prompt(prompt).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method must return either GetPromptResult, List<PromptMessage>");
	}

	@Test
	public void testDuplicateExchangeParameters() throws Exception {
		TestPromptProvider provider = new TestPromptProvider();
		Method method = TestPromptProvider.class.getMethod("duplicateExchangeParameters", McpSyncServerExchange.class,
				McpSyncServerExchange.class);

		Prompt prompt = createTestPrompt("invalid", "Invalid parameters");

		assertThatThrownBy(
				() -> SyncMcpPromptMethodCallback.builder().method(method).bean(provider).prompt(prompt).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method cannot have more than one exchange parameter");
	}

	@Test
	public void testDuplicateRequestParameters() throws Exception {
		TestPromptProvider provider = new TestPromptProvider();
		Method method = TestPromptProvider.class.getMethod("duplicateRequestParameters", GetPromptRequest.class,
				GetPromptRequest.class);

		Prompt prompt = createTestPrompt("invalid", "Invalid parameters");

		assertThatThrownBy(
				() -> SyncMcpPromptMethodCallback.builder().method(method).bean(provider).prompt(prompt).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method cannot have more than one GetPromptRequest parameter");
	}

	@Test
	public void testDuplicateMapParameters() throws Exception {
		TestPromptProvider provider = new TestPromptProvider();
		Method method = TestPromptProvider.class.getMethod("duplicateMapParameters", Map.class, Map.class);

		Prompt prompt = createTestPrompt("invalid", "Invalid parameters");

		assertThatThrownBy(
				() -> SyncMcpPromptMethodCallback.builder().method(method).bean(provider).prompt(prompt).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method cannot have more than one Map parameter");
	}

	@Test
	public void testNullRequest() throws Exception {
		TestPromptProvider provider = new TestPromptProvider();
		Method method = TestPromptProvider.class.getMethod("getPromptWithRequest", GetPromptRequest.class);

		Prompt prompt = createTestPrompt("greeting", "A simple greeting prompt");

		BiFunction<McpSyncServerExchange, GetPromptRequest, GetPromptResult> callback = SyncMcpPromptMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.prompt(prompt)
			.build();

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);

		assertThatThrownBy(() -> callback.apply(exchange, null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Request must not be null");
	}

	@Test
	public void testCallbackWithProgressToken() throws Exception {
		TestPromptProvider provider = new TestPromptProvider();
		Method method = TestPromptProvider.class.getMethod("getPromptWithProgressToken", String.class, String.class);

		Prompt prompt = createTestPrompt("progress-token", "A prompt with progress token");

		BiFunction<McpSyncServerExchange, GetPromptRequest, GetPromptResult> callback = SyncMcpPromptMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.prompt(prompt)
			.build();

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		Map<String, Object> args = new HashMap<>();
		args.put("name", "John");
		// Note: GetPromptRequest doesn't have progressToken in current spec, so it will
		// be null
		GetPromptRequest request = new GetPromptRequest("progress-token", args);

		GetPromptResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.description()).isEqualTo("Progress token prompt");
		assertThat(result.messages()).hasSize(1);
		PromptMessage message = result.messages().get(0);
		assertThat(message.role()).isEqualTo(Role.ASSISTANT);
		// Since GetPromptRequest doesn't have progressToken, it should be null
		assertThat(((TextContent) message.content()).text()).isEqualTo("Hello John (no token)");
	}

	@Test
	public void testCallbackWithMixedAndProgressToken() throws Exception {
		TestPromptProvider provider = new TestPromptProvider();
		Method method = TestPromptProvider.class.getMethod("getPromptWithMixedAndProgress", McpSyncServerExchange.class,
				String.class, String.class, GetPromptRequest.class);

		Prompt prompt = createTestPrompt("mixed-with-progress", "A prompt with mixed args and progress token");

		BiFunction<McpSyncServerExchange, GetPromptRequest, GetPromptResult> callback = SyncMcpPromptMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.prompt(prompt)
			.build();

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		Map<String, Object> args = new HashMap<>();
		args.put("name", "John");
		GetPromptRequest request = new GetPromptRequest("mixed-with-progress", args);

		GetPromptResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.description()).isEqualTo("Mixed with progress prompt");
		assertThat(result.messages()).hasSize(1);
		PromptMessage message = result.messages().get(0);
		assertThat(message.role()).isEqualTo(Role.ASSISTANT);
		// Since GetPromptRequest doesn't have progressToken, it should be null
		assertThat(((TextContent) message.content()).text())
			.isEqualTo("Hello John from mixed-with-progress (no token)");
	}

	@Test
	public void testDuplicateProgressTokenParameters() throws Exception {
		TestPromptProvider provider = new TestPromptProvider();
		Method method = TestPromptProvider.class.getMethod("duplicateProgressTokenParameters", String.class,
				String.class);

		Prompt prompt = createTestPrompt("invalid", "Invalid parameters");

		assertThatThrownBy(
				() -> SyncMcpPromptMethodCallback.builder().method(method).bean(provider).prompt(prompt).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method cannot have more than one @McpProgressToken parameter");
	}

	@Test
	public void testCallbackWithMeta() throws Exception {
		TestPromptProvider provider = new TestPromptProvider();
		Method method = TestPromptProvider.class.getMethod("getPromptWithMeta", String.class, McpMeta.class);

		Prompt prompt = createTestPrompt("meta-prompt", "A prompt with meta parameter");

		BiFunction<McpSyncServerExchange, GetPromptRequest, GetPromptResult> callback = SyncMcpPromptMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.prompt(prompt)
			.build();

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		Map<String, Object> args = new HashMap<>();
		args.put("name", "John");

		// Create request with meta data
		GetPromptRequest request = new GetPromptRequest("meta-prompt", args,
				Map.of("userId", "user123", "sessionId", "session456"));

		GetPromptResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.description()).isEqualTo("Meta prompt");
		assertThat(result.messages()).hasSize(1);
		PromptMessage message = result.messages().get(0);
		assertThat(message.role()).isEqualTo(Role.ASSISTANT);
		assertThat(((TextContent) message.content()).text())
			.contains("Hello John, Meta: {userId=user123, sessionId=session456}");
	}

	@Test
	public void testCallbackWithMetaNull() throws Exception {
		TestPromptProvider provider = new TestPromptProvider();
		Method method = TestPromptProvider.class.getMethod("getPromptWithMeta", String.class, McpMeta.class);

		Prompt prompt = createTestPrompt("meta-prompt", "A prompt with meta parameter");

		BiFunction<McpSyncServerExchange, GetPromptRequest, GetPromptResult> callback = SyncMcpPromptMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.prompt(prompt)
			.build();

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		Map<String, Object> args = new HashMap<>();
		args.put("name", "John");

		// Create request without meta
		GetPromptRequest request = new GetPromptRequest("meta-prompt", args);

		GetPromptResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.description()).isEqualTo("Meta prompt");
		assertThat(result.messages()).hasSize(1);
		PromptMessage message = result.messages().get(0);
		assertThat(message.role()).isEqualTo(Role.ASSISTANT);
		assertThat(((TextContent) message.content()).text()).isEqualTo("Hello John, Meta: {}");
	}

	@Test
	public void testCallbackWithMixedAndMeta() throws Exception {
		TestPromptProvider provider = new TestPromptProvider();
		Method method = TestPromptProvider.class.getMethod("getPromptWithMixedAndMeta", McpSyncServerExchange.class,
				String.class, McpMeta.class, GetPromptRequest.class);

		Prompt prompt = createTestPrompt("mixed-with-meta", "A prompt with mixed args and meta");

		BiFunction<McpSyncServerExchange, GetPromptRequest, GetPromptResult> callback = SyncMcpPromptMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.prompt(prompt)
			.build();

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		Map<String, Object> args = new HashMap<>();
		args.put("name", "John");

		// Create request with meta data
		GetPromptRequest request = new GetPromptRequest("mixed-with-meta", args, Map.of("userId", "user123"));

		GetPromptResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.description()).isEqualTo("Mixed with meta prompt");
		assertThat(result.messages()).hasSize(1);
		PromptMessage message = result.messages().get(0);
		assertThat(message.role()).isEqualTo(Role.ASSISTANT);
		assertThat(((TextContent) message.content()).text())
			.isEqualTo("Hello John from mixed-with-meta, Meta: {userId=user123}");
	}

	@Test
	public void testDuplicateMetaParameters() throws Exception {
		TestPromptProvider provider = new TestPromptProvider();
		Method method = TestPromptProvider.class.getMethod("duplicateMetaParameters", McpMeta.class, McpMeta.class);

		Prompt prompt = createTestPrompt("invalid", "Invalid parameters");

		assertThatThrownBy(
				() -> SyncMcpPromptMethodCallback.builder().method(method).bean(provider).prompt(prompt).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method cannot have more than one McpMeta parameter");
	}

}
