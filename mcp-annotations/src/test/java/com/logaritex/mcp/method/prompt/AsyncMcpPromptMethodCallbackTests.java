/*
 * Copyright 2025-2025 the original author or authors.
 */

package com.logaritex.mcp.method.prompt;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import com.logaritex.mcp.annotation.McpPrompt;
import com.logaritex.mcp.method.prompt.AsyncMcpPromptMethodCallback;
import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.GetPromptRequest;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.Prompt;
import io.modelcontextprotocol.spec.McpSchema.PromptArgument;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link AsyncMcpPromptMethodCallback}.
 *
 * @author Christian Tzolov
 */
public class AsyncMcpPromptMethodCallbackTests {

	private static class TestPromptProvider {

		@McpPrompt(name = "greeting", description = "A simple greeting prompt")
		public GetPromptResult getPromptWithRequest(GetPromptRequest request) {
			return new GetPromptResult("Greeting prompt",
					List.of(new PromptMessage(Role.ASSISTANT, new TextContent("Hello from " + request.name()))));
		}

		@McpPrompt(name = "exchange-greeting", description = "A greeting prompt with exchange")
		public GetPromptResult getPromptWithExchange(McpAsyncServerExchange exchange, GetPromptRequest request) {
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
		public GetPromptResult getPromptWithIndividualArgs(String name, Integer age) {
			return new GetPromptResult("Individual arguments prompt", List.of(new PromptMessage(Role.ASSISTANT,
					new TextContent("Hello " + name + ", you are " + age + " years old"))));
		}

		@McpPrompt(name = "mixed-args", description = "A prompt with mixed argument types")
		public GetPromptResult getPromptWithMixedArgs(McpAsyncServerExchange exchange, String name, Integer age) {
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

		@McpPrompt(name = "mono-prompt", description = "A prompt returning a Mono")
		public Mono<GetPromptResult> getMonoPrompt(GetPromptRequest request) {
			return Mono.just(new GetPromptResult("Mono prompt", List
				.of(new PromptMessage(Role.ASSISTANT, new TextContent("Async response for " + request.name())))));
		}

		@McpPrompt(name = "mono-string", description = "A prompt returning a Mono<String>")
		public Mono<String> getMonoString(GetPromptRequest request) {
			return Mono.just("Async string response for " + request.name());
		}

		@McpPrompt(name = "mono-message", description = "A prompt returning a Mono<PromptMessage>")
		public Mono<PromptMessage> getMonoMessage(GetPromptRequest request) {
			return Mono
				.just(new PromptMessage(Role.ASSISTANT, new TextContent("Async single message for " + request.name())));
		}

		@McpPrompt(name = "mono-message-list", description = "A prompt returning a Mono<List<PromptMessage>>")
		public Mono<List<PromptMessage>> getMonoMessageList(GetPromptRequest request) {
			return Mono.just(List.of(
					new PromptMessage(Role.ASSISTANT, new TextContent("Async message 1 for " + request.name())),
					new PromptMessage(Role.ASSISTANT, new TextContent("Async message 2 for " + request.name()))));
		}

		@McpPrompt(name = "mono-string-list", description = "A prompt returning a Mono<List<String>>")
		public Mono<List<String>> getMonoStringList(GetPromptRequest request) {
			return Mono.just(List.of("Async string 1 for " + request.name(), "Async string 2 for " + request.name(),
					"Async string 3 for " + request.name()));
		}

		public void invalidReturnType(GetPromptRequest request) {
			// Invalid return type
		}

		public GetPromptResult duplicateExchangeParameters(McpAsyncServerExchange exchange1,
				McpAsyncServerExchange exchange2) {
			return new GetPromptResult("Invalid", List.of());
		}

		public GetPromptResult duplicateRequestParameters(GetPromptRequest request1, GetPromptRequest request2) {
			return new GetPromptResult("Invalid", List.of());
		}

		public GetPromptResult duplicateMapParameters(Map<String, Object> args1, Map<String, Object> args2) {
			return new GetPromptResult("Invalid", List.of());
		}

	}

	private Prompt createTestPrompt(String name, String description) {
		return new Prompt(name, description, List.of(new PromptArgument("name", "User's name", true),
				new PromptArgument("age", "User's age", false)));
	}

	@Test
	public void testInvalidNonMonoReturnType() throws Exception {
		TestPromptProvider provider = new TestPromptProvider();
		Method method = TestPromptProvider.class.getMethod("getPromptWithRequest", GetPromptRequest.class);

		Prompt prompt = createTestPrompt("greeting", "A simple greeting prompt");

		assertThatThrownBy(
				() -> AsyncMcpPromptMethodCallback.builder().method(method).bean(provider).prompt(prompt).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method must return a Mono<T>");
	}

	@Test
	public void testCallbackWithMonoPromptResult() throws Exception {
		TestPromptProvider provider = new TestPromptProvider();
		Method method = TestPromptProvider.class.getMethod("getMonoPrompt", GetPromptRequest.class);

		Prompt prompt = createTestPrompt("mono-prompt", "A prompt returning a Mono");

		BiFunction<McpAsyncServerExchange, GetPromptRequest, Mono<GetPromptResult>> callback = AsyncMcpPromptMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.prompt(prompt)
			.build();

		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		Map<String, Object> args = new HashMap<>();
		args.put("name", "John");
		GetPromptRequest request = new GetPromptRequest("mono-prompt", args);

		Mono<GetPromptResult> resultMono = callback.apply(exchange, request);

		StepVerifier.create(resultMono).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.description()).isEqualTo("Mono prompt");
			assertThat(result.messages()).hasSize(1);
			PromptMessage message = result.messages().get(0);
			assertThat(message.role()).isEqualTo(Role.ASSISTANT);
			assertThat(((TextContent) message.content()).text()).isEqualTo("Async response for mono-prompt");
		}).verifyComplete();
	}

	@Test
	public void testCallbackWithMonoString() throws Exception {
		TestPromptProvider provider = new TestPromptProvider();
		Method method = TestPromptProvider.class.getMethod("getMonoString", GetPromptRequest.class);

		Prompt prompt = createTestPrompt("mono-string", "A prompt returning a Mono<String>");

		BiFunction<McpAsyncServerExchange, GetPromptRequest, Mono<GetPromptResult>> callback = AsyncMcpPromptMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.prompt(prompt)
			.build();

		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		Map<String, Object> args = new HashMap<>();
		args.put("name", "John");
		GetPromptRequest request = new GetPromptRequest("mono-string", args);

		Mono<GetPromptResult> resultMono = callback.apply(exchange, request);

		StepVerifier.create(resultMono).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.messages()).hasSize(1);
			PromptMessage message = result.messages().get(0);
			assertThat(message.role()).isEqualTo(Role.ASSISTANT);
			assertThat(((TextContent) message.content()).text()).isEqualTo("Async string response for mono-string");
		}).verifyComplete();
	}

	@Test
	public void testCallbackWithMonoMessage() throws Exception {
		TestPromptProvider provider = new TestPromptProvider();
		Method method = TestPromptProvider.class.getMethod("getMonoMessage", GetPromptRequest.class);

		Prompt prompt = createTestPrompt("mono-message", "A prompt returning a Mono<PromptMessage>");

		BiFunction<McpAsyncServerExchange, GetPromptRequest, Mono<GetPromptResult>> callback = AsyncMcpPromptMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.prompt(prompt)
			.build();

		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		Map<String, Object> args = new HashMap<>();
		args.put("name", "John");
		GetPromptRequest request = new GetPromptRequest("mono-message", args);

		Mono<GetPromptResult> resultMono = callback.apply(exchange, request);

		StepVerifier.create(resultMono).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.messages()).hasSize(1);
			PromptMessage message = result.messages().get(0);
			assertThat(message.role()).isEqualTo(Role.ASSISTANT);
			assertThat(((TextContent) message.content()).text()).isEqualTo("Async single message for mono-message");
		}).verifyComplete();
	}

	@Test
	public void testCallbackWithMonoMessageList() throws Exception {
		TestPromptProvider provider = new TestPromptProvider();
		Method method = TestPromptProvider.class.getMethod("getMonoMessageList", GetPromptRequest.class);

		Prompt prompt = createTestPrompt("mono-message-list", "A prompt returning a Mono<List<PromptMessage>>");

		BiFunction<McpAsyncServerExchange, GetPromptRequest, Mono<GetPromptResult>> callback = AsyncMcpPromptMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.prompt(prompt)
			.build();

		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		Map<String, Object> args = new HashMap<>();
		args.put("name", "John");
		GetPromptRequest request = new GetPromptRequest("mono-message-list", args);

		Mono<GetPromptResult> resultMono = callback.apply(exchange, request);

		StepVerifier.create(resultMono).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.messages()).hasSize(2);
			PromptMessage message1 = result.messages().get(0);
			PromptMessage message2 = result.messages().get(1);
			assertThat(message1.role()).isEqualTo(Role.ASSISTANT);
			assertThat(message2.role()).isEqualTo(Role.ASSISTANT);
			assertThat(((TextContent) message1.content()).text()).isEqualTo("Async message 1 for mono-message-list");
			assertThat(((TextContent) message2.content()).text()).isEqualTo("Async message 2 for mono-message-list");
		}).verifyComplete();
	}

	@Test
	public void testCallbackWithMonoStringList() throws Exception {
		TestPromptProvider provider = new TestPromptProvider();
		Method method = TestPromptProvider.class.getMethod("getMonoStringList", GetPromptRequest.class);

		Prompt prompt = createTestPrompt("mono-string-list", "A prompt returning a Mono<List<String>>");

		BiFunction<McpAsyncServerExchange, GetPromptRequest, Mono<GetPromptResult>> callback = AsyncMcpPromptMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.prompt(prompt)
			.build();

		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		Map<String, Object> args = new HashMap<>();
		args.put("name", "John");
		GetPromptRequest request = new GetPromptRequest("mono-string-list", args);

		Mono<GetPromptResult> resultMono = callback.apply(exchange, request);

		StepVerifier.create(resultMono).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.messages()).hasSize(3);
			PromptMessage message1 = result.messages().get(0);
			PromptMessage message2 = result.messages().get(1);
			PromptMessage message3 = result.messages().get(2);
			assertThat(message1.role()).isEqualTo(Role.ASSISTANT);
			assertThat(message2.role()).isEqualTo(Role.ASSISTANT);
			assertThat(message3.role()).isEqualTo(Role.ASSISTANT);
			assertThat(((TextContent) message1.content()).text()).isEqualTo("Async string 1 for mono-string-list");
			assertThat(((TextContent) message2.content()).text()).isEqualTo("Async string 2 for mono-string-list");
			assertThat(((TextContent) message3.content()).text()).isEqualTo("Async string 3 for mono-string-list");
		}).verifyComplete();
	}

	@Test
	public void testNullRequest() throws Exception {
		TestPromptProvider provider = new TestPromptProvider();
		Method method = TestPromptProvider.class.getMethod("getMonoPrompt", GetPromptRequest.class);

		Prompt prompt = createTestPrompt("mono-prompt", "A prompt returning a Mono");

		BiFunction<McpAsyncServerExchange, GetPromptRequest, Mono<GetPromptResult>> callback = AsyncMcpPromptMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.prompt(prompt)
			.build();

		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);

		StepVerifier.create(callback.apply(exchange, null)).expectErrorMessage("Request must not be null").verify();
	}

}
