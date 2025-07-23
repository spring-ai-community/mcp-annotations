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

package org.springaicommunity.mcp.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpTool;

import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import reactor.core.publisher.Mono;

/**
 * Tests for {@link SyncMcpToolProvider}.
 *
 * @author Christian Tzolov
 */
public class SyncMcpToolProviderTests {

	@Test
	void testConstructorWithNullToolObjects() {
		assertThatThrownBy(() -> new SyncMcpToolProvider(null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("toolObjects cannot be null");
	}

	@Test
	void testGetToolSpecificationsWithSingleValidTool() {
		// Create a class with only one valid tool method
		class SingleValidTool {

			@McpTool(name = "test-tool", description = "A test tool")
			public String testTool(String input) {
				return "Processed: " + input;
			}

		}

		SingleValidTool toolObject = new SingleValidTool();
		SyncMcpToolProvider provider = new SyncMcpToolProvider(List.of(toolObject));

		List<SyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).isNotNull();
		assertThat(toolSpecs).hasSize(1);

		SyncToolSpecification toolSpec = toolSpecs.get(0);
		assertThat(toolSpec.tool().name()).isEqualTo("test-tool");
		assertThat(toolSpec.tool().description()).isEqualTo("A test tool");
		assertThat(toolSpec.tool().inputSchema()).isNotNull();
		assertThat(toolSpec.callHandler()).isNotNull();

		// Test that the handler works
		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("test-tool", Map.of("input", "hello"));
		CallToolResult result = toolSpec.callHandler().apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.isError()).isFalse();
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
		assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("Processed: hello");
	}

	@Test
	void testGetToolSpecificationsWithCustomToolName() {
		class CustomNameTool {

			@McpTool(name = "custom-name", description = "Custom named tool")
			public String methodWithDifferentName(String input) {
				return "Custom: " + input;
			}

		}

		CustomNameTool toolObject = new CustomNameTool();
		SyncMcpToolProvider provider = new SyncMcpToolProvider(List.of(toolObject));

		List<SyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(1);
		assertThat(toolSpecs.get(0).tool().name()).isEqualTo("custom-name");
		assertThat(toolSpecs.get(0).tool().description()).isEqualTo("Custom named tool");
	}

	@Test
	void testGetToolSpecificationsWithDefaultToolName() {
		class DefaultNameTool {

			@McpTool(description = "Tool with default name")
			public String defaultNameMethod(String input) {
				return "Default: " + input;
			}

		}

		DefaultNameTool toolObject = new DefaultNameTool();
		SyncMcpToolProvider provider = new SyncMcpToolProvider(List.of(toolObject));

		List<SyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(1);
		assertThat(toolSpecs.get(0).tool().name()).isEqualTo("defaultNameMethod");
		assertThat(toolSpecs.get(0).tool().description()).isEqualTo("Tool with default name");
	}

	@Test
	void testGetToolSpecificationsWithEmptyToolName() {
		class EmptyNameTool {

			@McpTool(name = "", description = "Tool with empty name")
			public String emptyNameMethod(String input) {
				return "Empty: " + input;
			}

		}

		EmptyNameTool toolObject = new EmptyNameTool();
		SyncMcpToolProvider provider = new SyncMcpToolProvider(List.of(toolObject));

		List<SyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(1);
		assertThat(toolSpecs.get(0).tool().name()).isEqualTo("emptyNameMethod");
		assertThat(toolSpecs.get(0).tool().description()).isEqualTo("Tool with empty name");
	}

	@Test
	void testGetToolSpecificationsFiltersOutMonoReturnTypes() {
		class MonoReturnTool {

			@McpTool(name = "mono-tool", description = "Tool returning Mono")
			public Mono<String> monoTool(String input) {
				return Mono.just("Mono: " + input);
			}

			@McpTool(name = "sync-tool", description = "Synchronous tool")
			public String syncTool(String input) {
				return "Sync: " + input;
			}

		}

		MonoReturnTool toolObject = new MonoReturnTool();
		SyncMcpToolProvider provider = new SyncMcpToolProvider(List.of(toolObject));

		List<SyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(1);
		assertThat(toolSpecs.get(0).tool().name()).isEqualTo("sync-tool");
		assertThat(toolSpecs.get(0).tool().description()).isEqualTo("Synchronous tool");
	}

	@Test
	void testGetToolSpecificationsWithMultipleToolMethods() {
		class MultipleToolMethods {

			@McpTool(name = "tool1", description = "First tool")
			public String firstTool(String input) {
				return "First: " + input;
			}

			@McpTool(name = "tool2", description = "Second tool")
			public String secondTool(String input) {
				return "Second: " + input;
			}

		}

		MultipleToolMethods toolObject = new MultipleToolMethods();
		SyncMcpToolProvider provider = new SyncMcpToolProvider(List.of(toolObject));

		List<SyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(2);
		assertThat(toolSpecs.get(0).tool().name()).isIn("tool1", "tool2");
		assertThat(toolSpecs.get(1).tool().name()).isIn("tool1", "tool2");
		assertThat(toolSpecs.get(0).tool().name()).isNotEqualTo(toolSpecs.get(1).tool().name());
	}

	@Test
	void testGetToolSpecificationsWithMultipleToolObjects() {
		class FirstToolObject {

			@McpTool(name = "first-tool", description = "First tool")
			public String firstTool(String input) {
				return "First: " + input;
			}

		}

		class SecondToolObject {

			@McpTool(name = "second-tool", description = "Second tool")
			public String secondTool(String input) {
				return "Second: " + input;
			}

		}

		FirstToolObject firstObject = new FirstToolObject();
		SecondToolObject secondObject = new SecondToolObject();
		SyncMcpToolProvider provider = new SyncMcpToolProvider(List.of(firstObject, secondObject));

		List<SyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(2);
		assertThat(toolSpecs.get(0).tool().name()).isIn("first-tool", "second-tool");
		assertThat(toolSpecs.get(1).tool().name()).isIn("first-tool", "second-tool");
		assertThat(toolSpecs.get(0).tool().name()).isNotEqualTo(toolSpecs.get(1).tool().name());
	}

	@Test
	void testGetToolSpecificationsWithMixedMethods() {
		class MixedMethods {

			@McpTool(name = "valid-tool", description = "Valid tool")
			public String validTool(String input) {
				return "Valid: " + input;
			}

			public String nonAnnotatedMethod(String input) {
				return "Non-annotated: " + input;
			}

			@McpTool(name = "mono-tool", description = "Mono tool")
			public Mono<String> monoTool(String input) {
				return Mono.just("Mono: " + input);
			}

		}

		MixedMethods toolObject = new MixedMethods();
		SyncMcpToolProvider provider = new SyncMcpToolProvider(List.of(toolObject));

		List<SyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(1);
		assertThat(toolSpecs.get(0).tool().name()).isEqualTo("valid-tool");
		assertThat(toolSpecs.get(0).tool().description()).isEqualTo("Valid tool");
	}

	@Test
	void testGetToolSpecificationsWithComplexParameters() {
		class ComplexParameterTool {

			@McpTool(name = "complex-tool", description = "Tool with complex parameters")
			public String complexTool(String name, int age, boolean active, List<String> tags) {
				return String.format("Name: %s, Age: %d, Active: %b, Tags: %s", name, age, active,
						String.join(",", tags));
			}

		}

		ComplexParameterTool toolObject = new ComplexParameterTool();
		SyncMcpToolProvider provider = new SyncMcpToolProvider(List.of(toolObject));

		List<SyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(1);
		assertThat(toolSpecs.get(0).tool().name()).isEqualTo("complex-tool");
		assertThat(toolSpecs.get(0).tool().description()).isEqualTo("Tool with complex parameters");
		assertThat(toolSpecs.get(0).tool().inputSchema()).isNotNull();

		// Test that the handler works with complex parameters
		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("complex-tool",
				Map.of("name", "John", "age", 30, "active", true, "tags", List.of("tag1", "tag2")));
		CallToolResult result = toolSpecs.get(0).callHandler().apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.isError()).isFalse();
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
		assertThat(((TextContent) result.content().get(0)).text())
			.isEqualTo("Name: John, Age: 30, Active: true, Tags: tag1,tag2");
	}

	@Test
	void testGetToolSpecificationsWithNoParameters() {
		class NoParameterTool {

			@McpTool(name = "no-param-tool", description = "Tool with no parameters")
			public String noParamTool() {
				return "No parameters needed";
			}

		}

		NoParameterTool toolObject = new NoParameterTool();
		SyncMcpToolProvider provider = new SyncMcpToolProvider(List.of(toolObject));

		List<SyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(1);
		assertThat(toolSpecs.get(0).tool().name()).isEqualTo("no-param-tool");
		assertThat(toolSpecs.get(0).tool().description()).isEqualTo("Tool with no parameters");

		// Test that the handler works with no parameters
		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("no-param-tool", Map.of());
		CallToolResult result = toolSpecs.get(0).callHandler().apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.isError()).isFalse();
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
		assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("No parameters needed");
	}

	@Test
	void testGetToolSpecificationsWithCallToolResultReturn() {
		class CallToolResultTool {

			@McpTool(name = "result-tool", description = "Tool returning CallToolResult")
			public CallToolResult resultTool(String message) {
				return CallToolResult.builder().addTextContent("Result: " + message).build();
			}

		}

		CallToolResultTool toolObject = new CallToolResultTool();
		SyncMcpToolProvider provider = new SyncMcpToolProvider(List.of(toolObject));

		List<SyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(1);
		assertThat(toolSpecs.get(0).tool().name()).isEqualTo("result-tool");
		assertThat(toolSpecs.get(0).tool().description()).isEqualTo("Tool returning CallToolResult");

		// Test that the handler works with CallToolResult return type
		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("result-tool", Map.of("message", "test"));
		CallToolResult result = toolSpecs.get(0).callHandler().apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.isError()).isFalse();
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
		assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("Result: test");
	}

	@Test
	void testGetToolSpecificationsWithPrivateMethod() {
		class PrivateMethodTool {

			@McpTool(name = "private-tool", description = "Private tool method")
			private String privateTool(String input) {
				return "Private: " + input;
			}

		}

		PrivateMethodTool toolObject = new PrivateMethodTool();
		SyncMcpToolProvider provider = new SyncMcpToolProvider(List.of(toolObject));

		List<SyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(1);
		assertThat(toolSpecs.get(0).tool().name()).isEqualTo("private-tool");
		assertThat(toolSpecs.get(0).tool().description()).isEqualTo("Private tool method");

		// Test that the handler works with private methods
		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("private-tool", Map.of("input", "test"));
		CallToolResult result = toolSpecs.get(0).callHandler().apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.isError()).isFalse();
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
		assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("Private: test");
	}

	@Test
	void testGetToolSpecificationsJsonSchemaGeneration() {
		class SchemaTestTool {

			@McpTool(name = "schema-tool", description = "Tool for schema testing")
			public String schemaTool(String requiredParam, Integer optionalParam) {
				return "Schema test: " + requiredParam + ", " + optionalParam;
			}

		}

		SchemaTestTool toolObject = new SchemaTestTool();
		SyncMcpToolProvider provider = new SyncMcpToolProvider(List.of(toolObject));

		List<SyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(1);
		SyncToolSpecification toolSpec = toolSpecs.get(0);

		assertThat(toolSpec.tool().name()).isEqualTo("schema-tool");
		assertThat(toolSpec.tool().description()).isEqualTo("Tool for schema testing");
		assertThat(toolSpec.tool().inputSchema()).isNotNull();

		// The input schema should be a valid JSON string containing parameter names
		String schemaString = toolSpec.tool().inputSchema().toString();
		assertThat(schemaString).isNotEmpty();
		assertThat(schemaString).contains("requiredParam");
		assertThat(schemaString).contains("optionalParam");
	}

}
