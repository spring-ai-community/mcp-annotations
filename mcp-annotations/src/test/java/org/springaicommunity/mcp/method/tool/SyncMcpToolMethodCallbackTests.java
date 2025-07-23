/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.tool;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link SyncMcpToolMethodCallback}.
 *
 * @author Christian Tzolov
 */
public class SyncMcpToolMethodCallbackTests {

	private static class TestToolProvider {

		@McpTool(name = "simple-tool", description = "A simple tool")
		public String simpleTool(String input) {
			return "Processed: " + input;
		}

		@McpTool(name = "math-tool", description = "A math tool")
		public int addNumbers(int a, int b) {
			return a + b;
		}

		@McpTool(name = "complex-tool", description = "A complex tool")
		public CallToolResult complexTool(String name, int age, boolean active) {
			return CallToolResult.builder()
				.addTextContent("Name: " + name + ", Age: " + age + ", Active: " + active)
				.build();
		}

		@McpTool(name = "exchange-tool", description = "Tool with exchange parameter")
		public String toolWithExchange(McpSyncServerExchange exchange, String message) {
			return "Exchange tool: " + message;
		}

		@McpTool(name = "list-tool", description = "Tool with list parameter")
		public String processList(List<String> items) {
			return "Items: " + String.join(", ", items);
		}

		@McpTool(name = "object-tool", description = "Tool with object parameter")
		public String processObject(TestObject obj) {
			return "Object: " + obj.name + " - " + obj.value;
		}

		@McpTool(name = "optional-params-tool", description = "Tool with optional parameters")
		public String toolWithOptionalParams(@McpToolParam(required = true) String required,
				@McpToolParam(required = false) String optional) {
			return "Required: " + required + ", Optional: " + (optional != null ? optional : "null");
		}

		@McpTool(name = "no-params-tool", description = "Tool with no parameters")
		public String noParamsTool() {
			return "No parameters needed";
		}

		@McpTool(name = "exception-tool", description = "Tool that throws exception")
		public String exceptionTool(String input) {
			throw new RuntimeException("Tool execution failed: " + input);
		}

		@McpTool(name = "null-return-tool", description = "Tool that returns null")
		public String nullReturnTool() {
			return null;
		}

		public String nonAnnotatedTool(String input) {
			return "Non-annotated: " + input;
		}

		@McpTool(name = "private-tool", description = "Private tool")
		private String privateTool(String input) {
			return "Private: " + input;
		}

		@McpTool(name = "enum-tool", description = "Tool with enum parameter")
		public String enumTool(TestEnum enumValue) {
			return "Enum: " + enumValue.name();
		}

		@McpTool(name = "primitive-types-tool", description = "Tool with primitive types")
		public String primitiveTypesTool(boolean flag, byte b, short s, int i, long l, float f, double d) {
			return String.format("Primitives: %b, %d, %d, %d, %d, %.1f, %.1f", flag, b, s, i, l, f, d);
		}

		@McpTool(name = "return-object-tool", description = "Tool that returns a complex object")
		public TestObject returnObjectTool(String name, int value) {
			return new TestObject(name, value);
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
	public void testSimpleToolCallback() throws Exception {
		TestToolProvider provider = new TestToolProvider();
		Method method = TestToolProvider.class.getMethod("simpleTool", String.class);
		SyncMcpToolMethodCallback callback = new SyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("simple-tool", Map.of("input", "test message"));

		CallToolResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.isError()).isFalse();
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
		assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("Processed: test message");
	}

	@Test
	public void testMathToolCallback() throws Exception {
		TestToolProvider provider = new TestToolProvider();
		Method method = TestToolProvider.class.getMethod("addNumbers", int.class, int.class);
		SyncMcpToolMethodCallback callback = new SyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("math-tool", Map.of("a", 5, "b", 3));

		CallToolResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.isError()).isFalse();
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
		assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("8");
	}

	@Test
	public void testComplexToolCallback() throws Exception {
		TestToolProvider provider = new TestToolProvider();
		Method method = TestToolProvider.class.getMethod("complexTool", String.class, int.class, boolean.class);
		SyncMcpToolMethodCallback callback = new SyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("complex-tool",
				Map.of("name", "John", "age", 30, "active", true));

		CallToolResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.isError()).isFalse();
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
		assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("Name: John, Age: 30, Active: true");
	}

	@Test
	public void testToolWithExchangeParameter() throws Exception {
		TestToolProvider provider = new TestToolProvider();
		Method method = TestToolProvider.class.getMethod("toolWithExchange", McpSyncServerExchange.class, String.class);
		SyncMcpToolMethodCallback callback = new SyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("exchange-tool", Map.of("message", "hello"));

		CallToolResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.isError()).isFalse();
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
		assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("Exchange tool: hello");
	}

	@Test
	public void testToolWithListParameter() throws Exception {
		TestToolProvider provider = new TestToolProvider();
		Method method = TestToolProvider.class.getMethod("processList", List.class);
		SyncMcpToolMethodCallback callback = new SyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("list-tool", Map.of("items", List.of("item1", "item2", "item3")));

		CallToolResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.isError()).isFalse();
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
		assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("Items: item1, item2, item3");
	}

	@Test
	public void testToolWithObjectParameter() throws Exception {
		TestToolProvider provider = new TestToolProvider();
		Method method = TestToolProvider.class.getMethod("processObject", TestObject.class);
		SyncMcpToolMethodCallback callback = new SyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("object-tool",
				Map.of("obj", Map.of("name", "test", "value", 42)));

		CallToolResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.isError()).isFalse();
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
		assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("Object: test - 42");
	}

	@Test
	public void testToolWithNoParameters() throws Exception {
		TestToolProvider provider = new TestToolProvider();
		Method method = TestToolProvider.class.getMethod("noParamsTool");
		SyncMcpToolMethodCallback callback = new SyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("no-params-tool", Map.of());

		CallToolResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.isError()).isFalse();
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
		assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("No parameters needed");
	}

	@Test
	public void testToolWithEnumParameter() throws Exception {
		TestToolProvider provider = new TestToolProvider();
		Method method = TestToolProvider.class.getMethod("enumTool", TestEnum.class);
		SyncMcpToolMethodCallback callback = new SyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("enum-tool", Map.of("enumValue", "OPTION_B"));

		CallToolResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.isError()).isFalse();
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
		assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("Enum: OPTION_B");
	}

	@Test
	public void testToolWithPrimitiveTypes() throws Exception {
		TestToolProvider provider = new TestToolProvider();
		Method method = TestToolProvider.class.getMethod("primitiveTypesTool", boolean.class, byte.class, short.class,
				int.class, long.class, float.class, double.class);
		SyncMcpToolMethodCallback callback = new SyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("primitive-types-tool",
				Map.of("flag", true, "b", 1, "s", 2, "i", 3, "l", 4L, "f", 5.5f, "d", 6.6));

		CallToolResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.isError()).isFalse();
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
		assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("Primitives: true, 1, 2, 3, 4, 5.5, 6.6");
	}

	@Test
	public void testToolWithNullParameters() throws Exception {
		TestToolProvider provider = new TestToolProvider();
		Method method = TestToolProvider.class.getMethod("simpleTool", String.class);
		SyncMcpToolMethodCallback callback = new SyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		Map<String, Object> args = new java.util.HashMap<>();
		args.put("input", null);
		CallToolRequest request = new CallToolRequest("simple-tool", args);

		CallToolResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.isError()).isFalse();
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
		assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("Processed: null");
	}

	@Test
	public void testToolWithMissingParameters() throws Exception {
		TestToolProvider provider = new TestToolProvider();
		Method method = TestToolProvider.class.getMethod("simpleTool", String.class);
		SyncMcpToolMethodCallback callback = new SyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("simple-tool", Map.of());

		CallToolResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.isError()).isFalse();
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
		assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("Processed: null");
	}

	@Test
	public void testToolThatThrowsException() throws Exception {
		TestToolProvider provider = new TestToolProvider();
		Method method = TestToolProvider.class.getMethod("exceptionTool", String.class);
		SyncMcpToolMethodCallback callback = new SyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("exception-tool", Map.of("input", "test"));

		CallToolResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.isError()).isTrue();
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
		assertThat(((TextContent) result.content().get(0)).text()).contains("Error invoking method");
		// The actual error message format may be different, so let's just check for the
		// method name
		assertThat(((TextContent) result.content().get(0)).text()).contains("exceptionTool");
	}

	@Test
	public void testToolThatReturnsNull() throws Exception {
		TestToolProvider provider = new TestToolProvider();
		Method method = TestToolProvider.class.getMethod("nullReturnTool");
		SyncMcpToolMethodCallback callback = new SyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("null-return-tool", Map.of());

		CallToolResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.isError()).isFalse();
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
		assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("null");
	}

	@Test
	public void testPrivateToolMethod() throws Exception {
		TestToolProvider provider = new TestToolProvider();
		Method method = TestToolProvider.class.getDeclaredMethod("privateTool", String.class);
		SyncMcpToolMethodCallback callback = new SyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("private-tool", Map.of("input", "test"));

		CallToolResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.isError()).isFalse();
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
		assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("Private: test");
	}

	@Test
	public void testNullRequest() throws Exception {
		TestToolProvider provider = new TestToolProvider();
		Method method = TestToolProvider.class.getMethod("simpleTool", String.class);
		SyncMcpToolMethodCallback callback = new SyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);

		assertThatThrownBy(() -> callback.apply(exchange, null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessage("Request must not be null");
	}

	@Test
	public void testCallbackReturnsCallToolResult() throws Exception {
		TestToolProvider provider = new TestToolProvider();
		Method method = TestToolProvider.class.getMethod("complexTool", String.class, int.class, boolean.class);
		SyncMcpToolMethodCallback callback = new SyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("complex-tool",
				Map.of("name", "Alice", "age", 25, "active", false));

		CallToolResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.isError()).isFalse();
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
		assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("Name: Alice, Age: 25, Active: false");
	}

	@Test
	public void testIsExchangeType() throws Exception {
		TestToolProvider provider = new TestToolProvider();
		Method method = TestToolProvider.class.getMethod("simpleTool", String.class);
		SyncMcpToolMethodCallback callback = new SyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		// Test that McpSyncServerExchange is recognized as exchange type
		assertThat(callback.isExchangeType(McpSyncServerExchange.class)).isTrue();

		// Test that other types are not recognized as exchange type
		assertThat(callback.isExchangeType(String.class)).isFalse();
		assertThat(callback.isExchangeType(Integer.class)).isFalse();
		assertThat(callback.isExchangeType(Object.class)).isFalse();
	}

	@Test
	public void testToolWithInvalidJsonConversion() throws Exception {
		TestToolProvider provider = new TestToolProvider();
		Method method = TestToolProvider.class.getMethod("processObject", TestObject.class);
		SyncMcpToolMethodCallback callback = new SyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		// Pass invalid object structure that can't be converted to TestObject
		CallToolRequest request = new CallToolRequest("object-tool", Map.of("obj", "invalid-object-string"));

		CallToolResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.isError()).isTrue();
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
		assertThat(((TextContent) result.content().get(0)).text()).contains("Error invoking method");
	}

	@Test
	public void testConstructorParameters() {
		TestToolProvider provider = new TestToolProvider();
		Method method = TestToolProvider.class.getMethods()[0]; // Any method

		SyncMcpToolMethodCallback callback = new SyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		// Verify that the callback was created successfully
		assertThat(callback).isNotNull();
	}

	@Test
	public void testToolWithStructuredOutput() throws Exception {
		TestToolProvider provider = new TestToolProvider();
		Method method = TestToolProvider.class.getMethod("processObject", TestObject.class);
		SyncMcpToolMethodCallback callback = new SyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("object-tool",
				Map.of("obj", Map.of("name", "test", "value", 42)));

		CallToolResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.isError()).isFalse();
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
		assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("Object: test - 42");
	}

	@Test
	public void testToolReturningComplexObject() throws Exception {
		TestToolProvider provider = new TestToolProvider();
		Method method = TestToolProvider.class.getMethod("returnObjectTool", String.class, int.class);
		SyncMcpToolMethodCallback callback = new SyncMcpToolMethodCallback(ReturnMode.STRUCTURED, method, provider);

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("return-object-tool", Map.of("name", "test", "value", 42));

		CallToolResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.isError()).isFalse();
		// For complex return types (non-primitive, non-wrapper, non-CallToolResult),
		// the new implementation should return structured content
		assertThat(result.content()).isEmpty();
		assertThat(result.structuredContent()).isNotNull();
		assertThat(result.structuredContent()).containsEntry("name", "test");
		assertThat(result.structuredContent()).containsEntry("value", 42);
	}

}
