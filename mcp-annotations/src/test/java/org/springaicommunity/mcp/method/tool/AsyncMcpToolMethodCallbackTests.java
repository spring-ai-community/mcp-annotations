/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.tool;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link AsyncMcpToolMethodCallback}.
 *
 * @author Christian Tzolov
 */
public class AsyncMcpToolMethodCallbackTests {

	private static class TestAsyncToolProvider {

		@McpTool(name = "simple-mono-tool", description = "A simple mono tool")
		public Mono<String> simpleMonoTool(String input) {
			return Mono.just("Processed: " + input);
		}

		@McpTool(name = "simple-flux-tool", description = "A simple flux tool")
		public Flux<String> simpleFluxTool(String input) {
			return Flux.just("Processed: " + input);
		}

		@McpTool(name = "simple-publisher-tool", description = "A simple publisher tool")
		public Publisher<String> simplePublisherTool(String input) {
			return Mono.just("Processed: " + input);
		}

		@McpTool(name = "math-mono-tool", description = "A math mono tool")
		public Mono<Integer> addNumbersMono(int a, int b) {
			return Mono.just(a + b);
		}

		@McpTool(name = "complex-mono-tool", description = "A complex mono tool")
		public Mono<CallToolResult> complexMonoTool(String name, int age, boolean active) {
			return Mono.just(CallToolResult.builder()
				.addTextContent("Name: " + name + ", Age: " + age + ", Active: " + active)
				.build());
		}

		@McpTool(name = "complex-flux-tool", description = "A complex flux tool")
		public Flux<CallToolResult> complexFluxTool(String name, int age, boolean active) {
			return Flux.just(CallToolResult.builder()
				.addTextContent("Name: " + name + ", Age: " + age + ", Active: " + active)
				.build());
		}

		@McpTool(name = "exchange-mono-tool", description = "Mono tool with exchange parameter")
		public Mono<String> monoToolWithExchange(McpAsyncServerExchange exchange, String message) {
			return Mono.just("Exchange tool: " + message);
		}

		@McpTool(name = "list-mono-tool", description = "Mono tool with list parameter")
		public Mono<String> processListMono(List<String> items) {
			return Mono.just("Items: " + String.join(", ", items));
		}

		@McpTool(name = "object-mono-tool", description = "Mono tool with object parameter")
		public Mono<String> processObjectMono(TestObject obj) {
			return Mono.just("Object: " + obj.name + " - " + obj.value);
		}

		@McpTool(name = "optional-params-mono-tool", description = "Mono tool with optional parameters")
		public Mono<String> monoToolWithOptionalParams(@McpToolParam(required = true) String required,
				@McpToolParam(required = false) String optional) {
			return Mono.just("Required: " + required + ", Optional: " + (optional != null ? optional : "null"));
		}

		@McpTool(name = "no-params-mono-tool", description = "Mono tool with no parameters")
		public Mono<String> noParamsMonoTool() {
			return Mono.just("No parameters needed");
		}

		@McpTool(name = "exception-mono-tool", description = "Mono tool that throws exception")
		public Mono<String> exceptionMonoTool(String input) {
			return Mono.error(new RuntimeException("Tool execution failed: " + input));
		}

		@McpTool(name = "null-return-mono-tool", description = "Mono tool that returns null")
		public Mono<String> nullReturnMonoTool() {
			return Mono.just((String) null);
		}

		@McpTool(name = "void-mono-tool", description = "Mono<Void> tool")
		public Mono<Void> voidMonoTool(String input) {
			return Mono.empty();
		}

		@McpTool(name = "void-flux-tool", description = "Flux<Void> tool")
		public Flux<Void> voidFluxTool(String input) {
			return Flux.empty();
		}

		@McpTool(name = "enum-mono-tool", description = "Mono tool with enum parameter")
		public Mono<String> enumMonoTool(TestEnum enumValue) {
			return Mono.just("Enum: " + enumValue.name());
		}

		@McpTool(name = "primitive-types-mono-tool", description = "Mono tool with primitive types")
		public Mono<String> primitiveTypesMonoTool(boolean flag, byte b, short s, int i, long l, float f, double d) {
			return Mono.just(String.format("Primitives: %b, %d, %d, %d, %d, %.1f, %.1f", flag, b, s, i, l, f, d));
		}

		@McpTool(name = "return-object-mono-tool", description = "Mono tool that returns a complex object")
		public Mono<TestObject> returnObjectMonoTool(String name, int value) {
			return Mono.just(new TestObject(name, value));
		}

		@McpTool(name = "delayed-mono-tool", description = "Mono tool with delay")
		public Mono<String> delayedMonoTool(String input) {
			return Mono.just("Delayed: " + input);
		}

		@McpTool(name = "empty-mono-tool", description = "Mono tool that returns empty")
		public Mono<String> emptyMonoTool() {
			return Mono.empty();
		}

		@McpTool(name = "multiple-flux-tool", description = "Flux tool that emits multiple values")
		public Flux<String> multipleFluxTool(String prefix) {
			return Flux.just(prefix + "1", prefix + "2", prefix + "3");
		}

		@McpTool(name = "private-mono-tool", description = "Private mono tool")
		private Mono<String> privateMonoTool(String input) {
			return Mono.just("Private: " + input);
		}

		// Non-reactive method that should cause error in async context
		@McpTool(name = "non-reactive-tool", description = "Non-reactive tool")
		public String nonReactiveTool(String input) {
			return "Non-reactive: " + input;
		}

	}

	public static class TestObject {

		public String name;

		public int value;

		public TestObject() {
		}

		public TestObject(String name, int value) {
			this.name = name;
			this.value = value;
		}

	}

	public enum TestEnum {

		OPTION_A, OPTION_B, OPTION_C

	}

	@Test
	public void testSimpleMonoToolCallback() throws Exception {
		TestAsyncToolProvider provider = new TestAsyncToolProvider();
		Method method = TestAsyncToolProvider.class.getMethod("simpleMonoTool", String.class);
		AsyncMcpToolMethodCallback callback = new AsyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("simple-mono-tool", Map.of("input", "test message"));

		StepVerifier.create(callback.apply(exchange, request)).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.isError()).isFalse();
			assertThat(result.content()).hasSize(1);
			assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
			assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("Processed: test message");
		}).verifyComplete();
	}

	@Test
	public void testSimpleFluxToolCallback() throws Exception {
		TestAsyncToolProvider provider = new TestAsyncToolProvider();
		Method method = TestAsyncToolProvider.class.getMethod("simpleFluxTool", String.class);
		AsyncMcpToolMethodCallback callback = new AsyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("simple-flux-tool", Map.of("input", "test message"));

		StepVerifier.create(callback.apply(exchange, request)).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.isError()).isFalse();
			assertThat(result.content()).hasSize(1);
			assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
			assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("Processed: test message");
		}).verifyComplete();
	}

	@Test
	public void testSimplePublisherToolCallback() throws Exception {
		TestAsyncToolProvider provider = new TestAsyncToolProvider();
		Method method = TestAsyncToolProvider.class.getMethod("simplePublisherTool", String.class);
		AsyncMcpToolMethodCallback callback = new AsyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("simple-publisher-tool", Map.of("input", "test message"));

		StepVerifier.create(callback.apply(exchange, request)).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.isError()).isFalse();
			assertThat(result.content()).hasSize(1);
			assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
			assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("Processed: test message");
		}).verifyComplete();
	}

	@Test
	public void testMathMonoToolCallback() throws Exception {
		TestAsyncToolProvider provider = new TestAsyncToolProvider();
		Method method = TestAsyncToolProvider.class.getMethod("addNumbersMono", int.class, int.class);
		AsyncMcpToolMethodCallback callback = new AsyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("math-mono-tool", Map.of("a", 5, "b", 3));

		StepVerifier.create(callback.apply(exchange, request)).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.isError()).isFalse();
			assertThat(result.content()).hasSize(1);
			assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
			assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("8");
		}).verifyComplete();
	}

	@Test
	public void testMonoToolThatThrowsException() throws Exception {
		TestAsyncToolProvider provider = new TestAsyncToolProvider();
		Method method = TestAsyncToolProvider.class.getMethod("exceptionMonoTool", String.class);
		AsyncMcpToolMethodCallback callback = new AsyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("exception-mono-tool", Map.of("input", "test"));

		StepVerifier.create(callback.apply(exchange, request)).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.isError()).isTrue();
			assertThat(result.content()).hasSize(1);
			assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
			assertThat(((TextContent) result.content().get(0)).text()).contains("Error invoking method");
		}).verifyComplete();
	}

	@Test
	public void testComplexFluxToolCallback() throws Exception {
		TestAsyncToolProvider provider = new TestAsyncToolProvider();
		Method method = TestAsyncToolProvider.class.getMethod("complexFluxTool", String.class, int.class,
				boolean.class);
		AsyncMcpToolMethodCallback callback = new AsyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("complex-flux-tool",
				Map.of("name", "Alice", "age", 25, "active", false));

		StepVerifier.create(callback.apply(exchange, request)).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.isError()).isFalse();
			assertThat(result.content()).hasSize(1);
			assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
			assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("Name: Alice, Age: 25, Active: false");
		}).verifyComplete();
	}

	@Test
	public void testMonoToolWithExchangeParameter() throws Exception {
		TestAsyncToolProvider provider = new TestAsyncToolProvider();
		Method method = TestAsyncToolProvider.class.getMethod("monoToolWithExchange", McpAsyncServerExchange.class,
				String.class);
		AsyncMcpToolMethodCallback callback = new AsyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("exchange-mono-tool", Map.of("message", "hello"));

		StepVerifier.create(callback.apply(exchange, request)).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.isError()).isFalse();
			assertThat(result.content()).hasSize(1);
			assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
			assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("Exchange tool: hello");
		}).verifyComplete();
	}

	@Test
	public void testMonoToolWithListParameter() throws Exception {
		TestAsyncToolProvider provider = new TestAsyncToolProvider();
		Method method = TestAsyncToolProvider.class.getMethod("processListMono", List.class);
		AsyncMcpToolMethodCallback callback = new AsyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("list-mono-tool",
				Map.of("items", List.of("item1", "item2", "item3")));

		StepVerifier.create(callback.apply(exchange, request)).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.isError()).isFalse();
			assertThat(result.content()).hasSize(1);
			assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
			assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("Items: item1, item2, item3");
		}).verifyComplete();
	}

	@Test
	public void testMonoToolWithObjectParameter() throws Exception {
		TestAsyncToolProvider provider = new TestAsyncToolProvider();
		Method method = TestAsyncToolProvider.class.getMethod("processObjectMono", TestObject.class);
		AsyncMcpToolMethodCallback callback = new AsyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("object-mono-tool",
				Map.of("obj", Map.of("name", "test", "value", 42)));

		StepVerifier.create(callback.apply(exchange, request)).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.isError()).isFalse();
			assertThat(result.content()).hasSize(1);
			assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
			assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("Object: test - 42");
		}).verifyComplete();
	}

	@Test
	public void testMonoToolWithNoParameters() throws Exception {
		TestAsyncToolProvider provider = new TestAsyncToolProvider();
		Method method = TestAsyncToolProvider.class.getMethod("noParamsMonoTool");
		AsyncMcpToolMethodCallback callback = new AsyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("no-params-mono-tool", Map.of());

		StepVerifier.create(callback.apply(exchange, request)).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.isError()).isFalse();
			assertThat(result.content()).hasSize(1);
			assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
			assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("No parameters needed");
		}).verifyComplete();
	}

	@Test
	public void testMonoToolWithEnumParameter() throws Exception {
		TestAsyncToolProvider provider = new TestAsyncToolProvider();
		Method method = TestAsyncToolProvider.class.getMethod("enumMonoTool", TestEnum.class);
		AsyncMcpToolMethodCallback callback = new AsyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("enum-mono-tool", Map.of("enumValue", "OPTION_B"));

		StepVerifier.create(callback.apply(exchange, request)).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.isError()).isFalse();
			assertThat(result.content()).hasSize(1);
			assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
			assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("Enum: OPTION_B");
		}).verifyComplete();
	}

	@Test
	public void testComplexMonoToolCallback() throws Exception {
		TestAsyncToolProvider provider = new TestAsyncToolProvider();
		Method method = TestAsyncToolProvider.class.getMethod("complexMonoTool", String.class, int.class,
				boolean.class);
		AsyncMcpToolMethodCallback callback = new AsyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("complex-mono-tool",
				Map.of("name", "John", "age", 30, "active", true));

		StepVerifier.create(callback.apply(exchange, request)).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.isError()).isFalse();
			assertThat(result.content()).hasSize(1);
			assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
			assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("Name: John, Age: 30, Active: true");
		}).verifyComplete();
	}

	@Test
	public void testMonoToolWithMissingParameters() throws Exception {
		TestAsyncToolProvider provider = new TestAsyncToolProvider();
		Method method = TestAsyncToolProvider.class.getMethod("simpleMonoTool", String.class);
		AsyncMcpToolMethodCallback callback = new AsyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("simple-mono-tool", Map.of());

		StepVerifier.create(callback.apply(exchange, request)).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.isError()).isFalse();
			assertThat(result.content()).hasSize(1);
			assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
			assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("Processed: null");
		}).verifyComplete();
	}

	@Test
	public void testMonoToolWithPrimitiveTypes() throws Exception {
		TestAsyncToolProvider provider = new TestAsyncToolProvider();
		Method method = TestAsyncToolProvider.class.getMethod("primitiveTypesMonoTool", boolean.class, byte.class,
				short.class, int.class, long.class, float.class, double.class);
		AsyncMcpToolMethodCallback callback = new AsyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("primitive-types-mono-tool",
				Map.of("flag", true, "b", 1, "s", 2, "i", 3, "l", 4L, "f", 5.5f, "d", 6.6));

		StepVerifier.create(callback.apply(exchange, request)).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.isError()).isFalse();
			assertThat(result.content()).hasSize(1);
			assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
			assertThat(((TextContent) result.content().get(0)).text())
				.isEqualTo("Primitives: true, 1, 2, 3, 4, 5.5, 6.6");
		}).verifyComplete();
	}

	@Test
	public void testMonoToolWithNullParameters() throws Exception {
		TestAsyncToolProvider provider = new TestAsyncToolProvider();
		Method method = TestAsyncToolProvider.class.getMethod("simpleMonoTool", String.class);
		AsyncMcpToolMethodCallback callback = new AsyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		Map<String, Object> args = new java.util.HashMap<>();
		args.put("input", null);
		CallToolRequest request = new CallToolRequest("simple-mono-tool", args);

		StepVerifier.create(callback.apply(exchange, request)).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.isError()).isFalse();
			assertThat(result.content()).hasSize(1);
			assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
			assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("Processed: null");
		}).verifyComplete();
	}

	@Test
	public void testMonoToolThatReturnsNull() throws Exception {
		TestAsyncToolProvider provider = new TestAsyncToolProvider();
		Method method = TestAsyncToolProvider.class.getMethod("nullReturnMonoTool");
		AsyncMcpToolMethodCallback callback = new AsyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("null-return-mono-tool", Map.of());

		StepVerifier.create(callback.apply(exchange, request)).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.isError()).isTrue();
			assertThat(result.content()).hasSize(1);
			assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
			assertThat(((TextContent) result.content().get(0)).text())
				.isEqualTo("Error invoking method: Error invoking method: nullReturnMonoTool");
		}).verifyComplete();
	}

	@Test
	public void testVoidMonoTool() throws Exception {
		TestAsyncToolProvider provider = new TestAsyncToolProvider();
		Method method = TestAsyncToolProvider.class.getMethod("voidMonoTool", String.class);
		AsyncMcpToolMethodCallback callback = new AsyncMcpToolMethodCallback(ReturnMode.VOID, method, provider);

		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("void-mono-tool", Map.of("input", "test"));

		StepVerifier.create(callback.apply(exchange, request)).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.isError()).isFalse();
			assertThat(result.content()).hasSize(1);
			assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
			assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("\"Done\"");
		}).verifyComplete();
	}

	@Test
	public void testVoidFluxTool() throws Exception {
		TestAsyncToolProvider provider = new TestAsyncToolProvider();
		Method method = TestAsyncToolProvider.class.getMethod("voidFluxTool", String.class);
		AsyncMcpToolMethodCallback callback = new AsyncMcpToolMethodCallback(ReturnMode.VOID, method, provider);

		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("void-flux-tool", Map.of("input", "test"));

		StepVerifier.create(callback.apply(exchange, request)).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.isError()).isFalse();
			assertThat(result.content()).hasSize(1);
			assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
			assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("\"Done\"");
		}).verifyComplete();
	}

	@Test
	public void testPrivateMonoToolMethod() throws Exception {
		TestAsyncToolProvider provider = new TestAsyncToolProvider();
		Method method = TestAsyncToolProvider.class.getDeclaredMethod("privateMonoTool", String.class);
		AsyncMcpToolMethodCallback callback = new AsyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("private-mono-tool", Map.of("input", "test"));

		StepVerifier.create(callback.apply(exchange, request)).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.isError()).isFalse();
			assertThat(result.content()).hasSize(1);
			assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
			assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("Private: test");
		}).verifyComplete();
	}

	@Test
	public void testNullRequest() throws Exception {
		TestAsyncToolProvider provider = new TestAsyncToolProvider();
		Method method = TestAsyncToolProvider.class.getMethod("simpleMonoTool", String.class);
		AsyncMcpToolMethodCallback callback = new AsyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);

		StepVerifier.create(callback.apply(exchange, null)).expectError(IllegalArgumentException.class).verify();
	}

	@Test
	public void testMonoToolReturningComplexObject() throws Exception {
		TestAsyncToolProvider provider = new TestAsyncToolProvider();
		Method method = TestAsyncToolProvider.class.getMethod("returnObjectMonoTool", String.class, int.class);
		AsyncMcpToolMethodCallback callback = new AsyncMcpToolMethodCallback(ReturnMode.STRUCTURED, method, provider);

		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("return-object-mono-tool", Map.of("name", "test", "value", 42));

		StepVerifier.create(callback.apply(exchange, request)).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.isError()).isFalse();
			assertThat(result.content()).isEmpty();
			assertThat(result.structuredContent()).isNotNull();
			assertThat(result.structuredContent()).containsEntry("name", "test");
			assertThat(result.structuredContent()).containsEntry("value", 42);
		}).verifyComplete();
	}

	@Test
	public void testEmptyMonoTool() throws Exception {
		TestAsyncToolProvider provider = new TestAsyncToolProvider();
		Method method = TestAsyncToolProvider.class.getMethod("emptyMonoTool");
		AsyncMcpToolMethodCallback callback = new AsyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("empty-mono-tool", Map.of());

		StepVerifier.create(callback.apply(exchange, request)).verifyComplete();
	}

	@Test
	public void testMultipleFluxTool() throws Exception {
		TestAsyncToolProvider provider = new TestAsyncToolProvider();
		Method method = TestAsyncToolProvider.class.getMethod("multipleFluxTool", String.class);
		AsyncMcpToolMethodCallback callback = new AsyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("multiple-flux-tool", Map.of("prefix", "item"));

		// Flux tools should take the first element
		StepVerifier.create(callback.apply(exchange, request)).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.isError()).isFalse();
			assertThat(result.content()).hasSize(1);
			assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
			assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("item1");
		}).verifyComplete();
	}

	@Test
	public void testNonReactiveToolShouldFail() throws Exception {
		TestAsyncToolProvider provider = new TestAsyncToolProvider();
		Method method = TestAsyncToolProvider.class.getMethod("nonReactiveTool", String.class);
		AsyncMcpToolMethodCallback callback = new AsyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("non-reactive-tool", Map.of("input", "test"));

		StepVerifier.create(callback.apply(exchange, request)).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.isError()).isTrue();
			assertThat(result.content()).hasSize(1);
			assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
			assertThat(((TextContent) result.content().get(0)).text()).contains("Error invoking method");
		}).verifyComplete();
	}

	@Test
	public void testMonoToolWithInvalidJsonConversion() throws Exception {
		TestAsyncToolProvider provider = new TestAsyncToolProvider();
		Method method = TestAsyncToolProvider.class.getMethod("processObjectMono", TestObject.class);
		AsyncMcpToolMethodCallback callback = new AsyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		// Pass invalid object structure that can't be converted to TestObject
		CallToolRequest request = new CallToolRequest("object-mono-tool", Map.of("obj", "invalid-object-string"));

		StepVerifier.create(callback.apply(exchange, request)).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.isError()).isTrue();
			assertThat(result.content()).hasSize(1);
			assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
			assertThat(((TextContent) result.content().get(0)).text()).contains("Error invoking method");
		}).verifyComplete();
	}

	@Test
	public void testConstructorParameters() {
		TestAsyncToolProvider provider = new TestAsyncToolProvider();
		Method method = TestAsyncToolProvider.class.getMethods()[0]; // Any method

		AsyncMcpToolMethodCallback callback = new AsyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		// Verify that the callback was created successfully
		assertThat(callback).isNotNull();
	}

	@Test
	public void testIsExchangeType() throws Exception {
		TestAsyncToolProvider provider = new TestAsyncToolProvider();
		Method method = TestAsyncToolProvider.class.getMethod("simpleMonoTool", String.class);
		AsyncMcpToolMethodCallback callback = new AsyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		// Test that McpAsyncServerExchange is recognized as exchange type
		assertThat(callback.isExchangeType(McpAsyncServerExchange.class)).isTrue();

		// Test that other types are not recognized as exchange type
		assertThat(callback.isExchangeType(String.class)).isFalse();
		assertThat(callback.isExchangeType(Integer.class)).isFalse();
		assertThat(callback.isExchangeType(Object.class)).isFalse();
	}

	@Test
	public void testMonoToolWithOptionalParameters() throws Exception {
		TestAsyncToolProvider provider = new TestAsyncToolProvider();
		Method method = TestAsyncToolProvider.class.getMethod("monoToolWithOptionalParams", String.class, String.class);
		AsyncMcpToolMethodCallback callback = new AsyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("optional-params-mono-tool",
				Map.of("required", "test", "optional", "optional-value"));

		StepVerifier.create(callback.apply(exchange, request)).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.isError()).isFalse();
			assertThat(result.content()).hasSize(1);
			assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
			assertThat(((TextContent) result.content().get(0)).text())
				.isEqualTo("Required: test, Optional: optional-value");
		}).verifyComplete();
	}

	@Test
	public void testMonoToolWithOptionalParametersMissing() throws Exception {
		TestAsyncToolProvider provider = new TestAsyncToolProvider();
		Method method = TestAsyncToolProvider.class.getMethod("monoToolWithOptionalParams", String.class, String.class);
		AsyncMcpToolMethodCallback callback = new AsyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("optional-params-mono-tool", Map.of("required", "test"));

		StepVerifier.create(callback.apply(exchange, request)).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.isError()).isFalse();
			assertThat(result.content()).hasSize(1);
			assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
			assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("Required: test, Optional: null");
		}).verifyComplete();
	}

	@Test
	public void testMonoToolWithStructuredOutput() throws Exception {
		TestAsyncToolProvider provider = new TestAsyncToolProvider();
		Method method = TestAsyncToolProvider.class.getMethod("processObjectMono", TestObject.class);
		AsyncMcpToolMethodCallback callback = new AsyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("object-mono-tool",
				Map.of("obj", Map.of("name", "test", "value", 42)));

		StepVerifier.create(callback.apply(exchange, request)).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.isError()).isFalse();
			assertThat(result.content()).hasSize(1);
			assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
			assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("Object: test - 42");
		}).verifyComplete();
	}

	@Test
	public void testCallbackReturnsCallToolResult() throws Exception {
		TestAsyncToolProvider provider = new TestAsyncToolProvider();
		Method method = TestAsyncToolProvider.class.getMethod("complexMonoTool", String.class, int.class,
				boolean.class);
		AsyncMcpToolMethodCallback callback = new AsyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("complex-mono-tool",
				Map.of("name", "Alice", "age", 25, "active", false));

		StepVerifier.create(callback.apply(exchange, request)).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.isError()).isFalse();
			assertThat(result.content()).hasSize(1);
			assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
			assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("Name: Alice, Age: 25, Active: false");
		}).verifyComplete();
	}

}
