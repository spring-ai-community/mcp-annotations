/*
 * Copyright 2025-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import org.springaicommunity.mcp.method.tool.utils.JsonSchemaGenerator;
import org.springaicommunity.mcp.provider.tool.SyncMcpToolProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests for CallToolRequest parameter support in MCP tools.
 *
 * @author Christian Tzolov
 */
public class CallToolRequestSupportTests {

	private static final ObjectMapper objectMapper = new ObjectMapper();

	private static class CallToolRequestTestProvider {

		/**
		 * Tool that only takes CallToolRequest - for fully dynamic handling
		 */
		@McpTool(name = "dynamic-tool", description = "Fully dynamic tool")
		public CallToolResult dynamicTool(CallToolRequest request) {
			// Access full request details
			String toolName = request.name();
			Map<String, Object> arguments = request.arguments();

			// Custom validation
			if (!arguments.containsKey("action")) {
				return CallToolResult.builder()
					.isError(true)
					.addTextContent("Missing required 'action' parameter")
					.build();
			}

			String action = (String) arguments.get("action");
			return CallToolResult.builder()
				.addTextContent("Processed action: " + action + " for tool: " + toolName)
				.build();
		}

		/**
		 * Tool with CallToolRequest and Exchange parameters
		 */
		@McpTool(name = "context-aware-tool", description = "Tool with context and request")
		public CallToolResult contextAwareTool(McpSyncServerExchange exchange, CallToolRequest request) {
			// Exchange is available for context
			Map<String, Object> arguments = request.arguments();

			return CallToolResult.builder()
				.addTextContent("Exchange available: " + (exchange != null) + ", Args: " + arguments.size())
				.build();
		}

		/**
		 * Tool with mixed parameters - CallToolRequest plus regular parameters
		 */
		@McpTool(name = "mixed-params-tool", description = "Tool with mixed parameters")
		public CallToolResult mixedParamsTool(CallToolRequest request,
				@McpToolParam(description = "Required string parameter", required = true) String requiredParam,
				@McpToolParam(description = "Optional integer parameter", required = false) Integer optionalParam) {

			Map<String, Object> allArguments = request.arguments();

			return CallToolResult.builder()
				.addTextContent(String.format("Required: %s, Optional: %d, Total args: %d, Tool: %s", requiredParam,
						optionalParam != null ? optionalParam : 0, allArguments.size(), request.name()))
				.build();
		}

		/**
		 * Tool that validates custom schema from CallToolRequest
		 */
		@McpTool(name = "schema-validator", description = "Validates against custom schema")
		public CallToolResult validateSchema(CallToolRequest request) {
			Map<String, Object> arguments = request.arguments();

			// Custom schema validation logic
			boolean hasRequiredFields = arguments.containsKey("data") && arguments.containsKey("format");

			if (!hasRequiredFields) {
				return CallToolResult.builder()
					.isError(true)
					.addTextContent("Schema validation failed: missing required fields 'data' and 'format'")
					.build();
			}

			return CallToolResult.builder()
				.addTextContent("Schema validation successful for: " + request.name())
				.build();
		}

		/**
		 * Regular tool without CallToolRequest for comparison
		 */
		@McpTool(name = "regular-tool", description = "Regular tool without CallToolRequest")
		public String regularTool(String input, int number) {
			return "Regular: " + input + " - " + number;
		}

		/**
		 * Tool that returns structured output
		 */
		@McpTool(name = "structured-output-tool", description = "Tool with structured output")
		public TestResult structuredOutputTool(CallToolRequest request) {
			Map<String, Object> arguments = request.arguments();
			String input = (String) arguments.get("input");

			return new TestResult(input != null ? input : "default", 42);
		}

	}

	public static class TestResult {

		public String message;

		public int value;

		public TestResult(String message, int value) {
			this.message = message;
			this.value = value;
		}

	}

	@Test
	public void testDynamicToolWithCallToolRequest() throws Exception {
		CallToolRequestTestProvider provider = new CallToolRequestTestProvider();
		Method method = CallToolRequestTestProvider.class.getMethod("dynamicTool", CallToolRequest.class);
		SyncMcpToolMethodCallback callback = new SyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("dynamic-tool", Map.of("action", "analyze", "data", "test-data"));

		CallToolResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.isError()).isFalse();
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
		assertThat(((TextContent) result.content().get(0)).text())
			.isEqualTo("Processed action: analyze for tool: dynamic-tool");
	}

	@Test
	public void testDynamicToolMissingRequiredParameter() throws Exception {
		CallToolRequestTestProvider provider = new CallToolRequestTestProvider();
		Method method = CallToolRequestTestProvider.class.getMethod("dynamicTool", CallToolRequest.class);
		SyncMcpToolMethodCallback callback = new SyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("dynamic-tool", Map.of("data", "test-data")); // Missing
																									// 'action'
																									// parameter

		CallToolResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.isError()).isTrue();
		assertThat(result.content()).hasSize(1);
		assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("Missing required 'action' parameter");
	}

	@Test
	public void testContextAwareToolWithCallToolRequestAndExchange() throws Exception {
		CallToolRequestTestProvider provider = new CallToolRequestTestProvider();
		Method method = CallToolRequestTestProvider.class.getMethod("contextAwareTool", McpSyncServerExchange.class,
				CallToolRequest.class);
		SyncMcpToolMethodCallback callback = new SyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);

		CallToolRequest request = new CallToolRequest("context-aware-tool", Map.of("key1", "value1", "key2", "value2"));

		CallToolResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.isError()).isFalse();
		assertThat(result.content()).hasSize(1);
		assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("Exchange available: true, Args: 2");
	}

	@Test
	public void testMixedParametersTool() throws Exception {
		CallToolRequestTestProvider provider = new CallToolRequestTestProvider();
		Method method = CallToolRequestTestProvider.class.getMethod("mixedParamsTool", CallToolRequest.class,
				String.class, Integer.class);
		SyncMcpToolMethodCallback callback = new SyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("mixed-params-tool",
				Map.of("requiredParam", "test-value", "optionalParam", 42, "extraParam", "extra"));

		CallToolResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.isError()).isFalse();
		assertThat(result.content()).hasSize(1);
		assertThat(((TextContent) result.content().get(0)).text())
			.isEqualTo("Required: test-value, Optional: 42, Total args: 3, Tool: mixed-params-tool");
	}

	@Test
	public void testMixedParametersToolWithNullOptional() throws Exception {
		CallToolRequestTestProvider provider = new CallToolRequestTestProvider();
		Method method = CallToolRequestTestProvider.class.getMethod("mixedParamsTool", CallToolRequest.class,
				String.class, Integer.class);
		SyncMcpToolMethodCallback callback = new SyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("mixed-params-tool", Map.of("requiredParam", "test-value"));

		CallToolResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.isError()).isFalse();
		assertThat(result.content()).hasSize(1);
		assertThat(((TextContent) result.content().get(0)).text())
			.isEqualTo("Required: test-value, Optional: 0, Total args: 1, Tool: mixed-params-tool");
	}

	@Test
	public void testSchemaValidatorTool() throws Exception {
		CallToolRequestTestProvider provider = new CallToolRequestTestProvider();
		Method method = CallToolRequestTestProvider.class.getMethod("validateSchema", CallToolRequest.class);
		SyncMcpToolMethodCallback callback = new SyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);

		// Test with valid schema
		CallToolRequest validRequest = new CallToolRequest("schema-validator",
				Map.of("data", "test-data", "format", "json"));

		CallToolResult validResult = callback.apply(exchange, validRequest);
		assertThat(validResult.isError()).isFalse();
		assertThat(((TextContent) validResult.content().get(0)).text())
			.isEqualTo("Schema validation successful for: schema-validator");

		// Test with invalid schema
		CallToolRequest invalidRequest = new CallToolRequest("schema-validator", Map.of("data", "test-data")); // Missing
																												// 'format'

		CallToolResult invalidResult = callback.apply(exchange, invalidRequest);
		assertThat(invalidResult.isError()).isTrue();
		assertThat(((TextContent) invalidResult.content().get(0)).text()).contains("Schema validation failed");
	}

	@Test
	public void testJsonSchemaGenerationForCallToolRequest() throws Exception {
		// Test that schema generation handles CallToolRequest properly
		Method dynamicMethod = CallToolRequestTestProvider.class.getMethod("dynamicTool", CallToolRequest.class);
		String dynamicSchema = JsonSchemaGenerator.generateForMethodInput(dynamicMethod);

		// Parse the schema
		JsonNode schemaNode = objectMapper.readTree(dynamicSchema);

		// Should have minimal schema with empty properties
		assertThat(schemaNode.has("type")).isTrue();
		assertThat(schemaNode.get("type").asText()).isEqualTo("object");
		assertThat(schemaNode.has("properties")).isTrue();
		assertThat(schemaNode.get("properties").size()).isEqualTo(0);
		assertThat(schemaNode.has("required")).isTrue();
		assertThat(schemaNode.get("required").size()).isEqualTo(0);
	}

	@Test
	public void testJsonSchemaGenerationForMixedParameters() throws Exception {
		// Test schema generation for method with CallToolRequest and other parameters
		Method mixedMethod = CallToolRequestTestProvider.class.getMethod("mixedParamsTool", CallToolRequest.class,
				String.class, Integer.class);
		String mixedSchema = JsonSchemaGenerator.generateForMethodInput(mixedMethod);

		// Parse the schema
		JsonNode schemaNode = objectMapper.readTree(mixedSchema);

		// Should have schema for non-CallToolRequest parameters only
		assertThat(schemaNode.has("properties")).isTrue();
		JsonNode properties = schemaNode.get("properties");
		assertThat(properties.has("requiredParam")).isTrue();
		assertThat(properties.has("optionalParam")).isTrue();
		assertThat(properties.size()).isEqualTo(2); // Only the regular parameters

		// Check required array
		assertThat(schemaNode.has("required")).isTrue();
		JsonNode required = schemaNode.get("required");
		assertThat(required.size()).isEqualTo(1);
		assertThat(required.get(0).asText()).isEqualTo("requiredParam");
	}

	@Test
	public void testJsonSchemaGenerationForRegularTool() throws Exception {
		// Test that regular tools still work as before
		Method regularMethod = CallToolRequestTestProvider.class.getMethod("regularTool", String.class, int.class);
		String regularSchema = JsonSchemaGenerator.generateForMethodInput(regularMethod);

		// Parse the schema
		JsonNode schemaNode = objectMapper.readTree(regularSchema);

		// Should have normal schema with all parameters
		assertThat(schemaNode.has("properties")).isTrue();
		JsonNode properties = schemaNode.get("properties");
		assertThat(properties.has("input")).isTrue();
		assertThat(properties.has("number")).isTrue();
		assertThat(properties.size()).isEqualTo(2);
	}

	@Test
	public void testHasCallToolRequestParameter() throws Exception {
		// Test the utility method
		Method dynamicMethod = CallToolRequestTestProvider.class.getMethod("dynamicTool", CallToolRequest.class);
		assertThat(JsonSchemaGenerator.hasCallToolRequestParameter(dynamicMethod)).isTrue();

		Method regularMethod = CallToolRequestTestProvider.class.getMethod("regularTool", String.class, int.class);
		assertThat(JsonSchemaGenerator.hasCallToolRequestParameter(regularMethod)).isFalse();

		Method mixedMethod = CallToolRequestTestProvider.class.getMethod("mixedParamsTool", CallToolRequest.class,
				String.class, Integer.class);
		assertThat(JsonSchemaGenerator.hasCallToolRequestParameter(mixedMethod)).isTrue();
	}

	@Test
	public void testSyncMcpToolProviderWithCallToolRequest() {
		// Test that SyncMcpToolProvider handles CallToolRequest tools correctly
		CallToolRequestTestProvider provider = new CallToolRequestTestProvider();
		SyncMcpToolProvider toolProvider = new SyncMcpToolProvider(List.of(provider));

		var toolSpecs = toolProvider.getToolSpecifications();

		// Should have all tools registered
		assertThat(toolSpecs).hasSize(6); // All 6 tools from the provider

		// Find the dynamic tool
		var dynamicToolSpec = toolSpecs.stream()
			.filter(spec -> spec.tool().name().equals("dynamic-tool"))
			.findFirst()
			.orElse(null);

		assertThat(dynamicToolSpec).isNotNull();
		assertThat(dynamicToolSpec.tool().description()).isEqualTo("Fully dynamic tool");

		// The input schema should be minimal
		var inputSchema = dynamicToolSpec.tool().inputSchema();
		assertThat(inputSchema).isNotNull();
		// Convert to string if it's a JsonSchema object
		String schemaStr = inputSchema.toString();
		assertThat(schemaStr).isNotNull();

		// Find the mixed params tool
		var mixedToolSpec = toolSpecs.stream()
			.filter(spec -> spec.tool().name().equals("mixed-params-tool"))
			.findFirst()
			.orElse(null);

		assertThat(mixedToolSpec).isNotNull();
		// The input schema should contain only the regular parameters
		var mixedSchema = mixedToolSpec.tool().inputSchema();
		assertThat(mixedSchema).isNotNull();
		// Convert to string if it's a JsonSchema object
		String mixedSchemaStr = mixedSchema.toString();
		assertThat(mixedSchemaStr).contains("requiredParam");
		assertThat(mixedSchemaStr).contains("optionalParam");
	}

	@Test
	public void testStructuredOutputWithCallToolRequest() throws Exception {
		CallToolRequestTestProvider provider = new CallToolRequestTestProvider();
		Method method = CallToolRequestTestProvider.class.getMethod("structuredOutputTool", CallToolRequest.class);
		SyncMcpToolMethodCallback callback = new SyncMcpToolMethodCallback(ReturnMode.STRUCTURED, method, provider);

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("structured-output-tool", Map.of("input", "test-message"));

		CallToolResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.isError()).isFalse();
		assertThat(result.structuredContent()).isNotNull();
		assertThat(result.structuredContent()).containsEntry("message", "test-message");
		assertThat(result.structuredContent()).containsEntry("value", 42);
	}

	@Test
	public void testCallToolRequestParameterInjection() throws Exception {
		// Test that CallToolRequest is properly injected as a parameter
		CallToolRequestTestProvider provider = new CallToolRequestTestProvider();
		Method method = CallToolRequestTestProvider.class.getMethod("dynamicTool", CallToolRequest.class);
		SyncMcpToolMethodCallback callback = new SyncMcpToolMethodCallback(ReturnMode.TEXT, method, provider);

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("dynamic-tool", Map.of("action", "test", "data", "sample"));

		// The callback should properly inject the CallToolRequest
		CallToolResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.isError()).isFalse();
		// The tool should have access to the full request including the tool name
		assertThat(((TextContent) result.content().get(0)).text()).contains("tool: dynamic-tool");
	}

}
