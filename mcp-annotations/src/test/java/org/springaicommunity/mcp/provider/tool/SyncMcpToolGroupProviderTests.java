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

package org.springaicommunity.mcp.provider.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpTool;

import org.springaicommunity.mcp.provider.toolgroup.SyncMcpToolGroupProvider;
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
 * @author Scott Lewis
 */
public class SyncMcpToolGroupProviderTests {
	
	@Test
	void testConstructorWithNullToolObjects() {
		interface Foo {
			void bar();
		};
		
		assertThatThrownBy(() -> new SyncMcpToolGroupProvider(null, null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("serviceObject cannot be null");
		assertThatThrownBy(() -> new SyncMcpToolGroupProvider(new Object(), null)).isInstanceOf(IllegalArgumentException.class)
		.hasMessageContaining("toolGroup cannot be null");
		assertThatThrownBy(() -> new SyncMcpToolGroupProvider(new Object(), Foo.class)).isInstanceOf(IllegalArgumentException.class)
		.hasMessageContaining("serviceObject must be instance of toolGroup");

	}

	interface SingleValidToolInterface {

		@McpTool(name = "test-tool", description = "A test tool")
		String testTool(String input);

	}

	@Test
	void testGetToolSpecificationsWithSingleValidTool() {
		
		// Create a class with only one valid tool method
		class SingleValidTool implements SingleValidToolInterface {

			public String testTool(String input) {
				return "Processed: " + input;
			}

		}

		SingleValidTool toolObject = new SingleValidTool();
		SyncMcpToolGroupProvider provider = new SyncMcpToolGroupProvider(toolObject, SingleValidToolInterface.class);

		List<SyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).isNotNull();
		assertThat(toolSpecs).hasSize(1);

		SyncToolSpecification toolSpec = toolSpecs.get(0);
		assertThat(toolSpec.tool().name()).isEqualTo(SingleValidToolInterface.class.getName() + ".test-tool");
		assertThat(toolSpec.tool().description()).isEqualTo("A test tool");
		assertThat(toolSpec.tool().inputSchema()).isNotNull();
		assertThat(toolSpec.callHandler()).isNotNull();

		// Test that the handler works
		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		CallToolRequest request = new CallToolRequest(SingleValidToolInterface.class.getName() + ".test-tool", Map.of("input", "hello"));
		CallToolResult result = toolSpec.callHandler().apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.isError()).isFalse();
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
		assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("Processed: hello");
	}

	@Test
	void testGetToolSpecificationsWithSingleValidToolNoInterface() {
		
		// Create a class with only one valid tool method
		class SingleValidTool {

			@McpTool(name = "test-tool", description = "A test tool")
			public String testTool(String input) {
				return "Processed: " + input;
			}

		}

		SingleValidTool toolObject = new SingleValidTool();
		// Use with 
		SyncMcpToolGroupProvider provider = new SyncMcpToolGroupProvider(toolObject, SingleValidTool.class);

		List<SyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).isNotNull();
		assertThat(toolSpecs).hasSize(1);

		SyncToolSpecification toolSpec = toolSpecs.get(0);
		assertThat(toolSpec.tool().name()).isEqualTo(SingleValidTool.class.getName() + ".test-tool");
		assertThat(toolSpec.tool().description()).isEqualTo("A test tool");
		assertThat(toolSpec.tool().inputSchema()).isNotNull();
		assertThat(toolSpec.callHandler()).isNotNull();

		// Test that the handler works
		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		CallToolRequest request = new CallToolRequest(SingleValidTool.class.getName() + ".test-tool", Map.of("input", "hello"));
		CallToolResult result = toolSpec.callHandler().apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.isError()).isFalse();
		assertThat(result.content()).hasSize(1);
		assertThat(result.content().get(0)).isInstanceOf(TextContent.class);
		assertThat(((TextContent) result.content().get(0)).text()).isEqualTo("Processed: hello");
	}

	interface CustomNameToolInterface {
		@McpTool(name = "custom-name", description = "Custom named tool")
		public String methodWithDifferentName(String input);
	}
	
	@Test
	void testGetToolSpecificationsWithCustomToolName() {
		class CustomNameTool implements CustomNameToolInterface {

			public String methodWithDifferentName(String input) {
				return "Custom: " + input;
			}

		}

		CustomNameTool toolObject = new CustomNameTool();
		SyncMcpToolGroupProvider provider = new SyncMcpToolGroupProvider(toolObject, CustomNameToolInterface.class);

		List<SyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(1);
		assertThat(toolSpecs.get(0).tool().name()).isEqualTo(CustomNameToolInterface.class.getName() + ".custom-name");
		assertThat(toolSpecs.get(0).tool().description()).isEqualTo("Custom named tool");
	}

	interface DefaultNameToolInterface {

		@McpTool(description = "Tool with default name")
		public String defaultNameMethod(String input);
	}

	@Test
	void testGetToolSpecificationsWithDefaultToolName() {
		class DefaultNameTool implements DefaultNameToolInterface {

			public String defaultNameMethod(String input) {
				return "Default: " + input;
			}

		}

		DefaultNameTool toolObject = new DefaultNameTool();
		SyncMcpToolGroupProvider provider = new SyncMcpToolGroupProvider(toolObject, DefaultNameToolInterface.class);

		List<SyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(1);
		assertThat(toolSpecs.get(0).tool().name()).isEqualTo(DefaultNameToolInterface.class.getName() + ".defaultNameMethod");
		assertThat(toolSpecs.get(0).tool().description()).isEqualTo("Tool with default name");
	}

	interface EmptyNameToolInterface {

		@McpTool(name = "", description = "Tool with empty name")
		public String emptyNameMethod(String input);

	}

	@Test
	void testGetToolSpecificationsWithEmptyToolName() {
		class EmptyNameTool implements EmptyNameToolInterface {

			public String emptyNameMethod(String input) {
				return "Empty: " + input;
			}

		}

		EmptyNameTool toolObject = new EmptyNameTool();
		SyncMcpToolGroupProvider provider = new SyncMcpToolGroupProvider(toolObject, EmptyNameToolInterface.class);

		List<SyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(1);
		assertThat(toolSpecs.get(0).tool().name()).isEqualTo(EmptyNameToolInterface.class.getName() + ".emptyNameMethod");
		assertThat(toolSpecs.get(0).tool().description()).isEqualTo("Tool with empty name");
	}

	interface MonoReturnToolInterface {

		@McpTool(name = "mono-tool", description = "Tool returning Mono")
		public Mono<String> monoTool(String input);

		@McpTool(name = "sync-tool", description = "Synchronous tool")
		public String syncTool(String input);

	}

	@Test
	void testGetToolSpecificationsFiltersOutMonoReturnTypes() {
		class MonoReturnTool implements MonoReturnToolInterface {

			public Mono<String> monoTool(String input) {
				return Mono.just("Mono: " + input);
			}

			public String syncTool(String input) {
				return "Sync: " + input;
			}

		}

		MonoReturnTool toolObject = new MonoReturnTool();
		SyncMcpToolGroupProvider provider = new SyncMcpToolGroupProvider(toolObject, MonoReturnToolInterface.class);

		List<SyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(1);
		assertThat(toolSpecs.get(0).tool().name()).isEqualTo(MonoReturnToolInterface.class.getName() + ".sync-tool");
		assertThat(toolSpecs.get(0).tool().description()).isEqualTo("Synchronous tool");
	}

	interface MultipleToolMethodsInterface {

		@McpTool(name = "tool1", description = "First tool")
		public String firstTool(String input);

		@McpTool(name = "tool2", description = "Second tool")
		public String secondTool(String input);

	}

	@Test
	void testGetToolSpecificationsWithMultipleToolMethods() {
		class MultipleToolMethods implements MultipleToolMethodsInterface {

			public String firstTool(String input) {
				return "First: " + input;
			}

			public String secondTool(String input) {
				return "Second: " + input;
			}

		}

		MultipleToolMethods toolObject = new MultipleToolMethods();
		SyncMcpToolGroupProvider provider = new SyncMcpToolGroupProvider(toolObject, MultipleToolMethodsInterface.class);

		List<SyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(2);
		assertThat(toolSpecs.get(0).tool().name()).isIn(MultipleToolMethodsInterface.class.getName() + ".tool1", MultipleToolMethodsInterface.class.getName() + ".tool2");
		assertThat(toolSpecs.get(1).tool().name()).isIn(MultipleToolMethodsInterface.class.getName() + ".tool1", MultipleToolMethodsInterface.class.getName() + ".tool2");
		assertThat(toolSpecs.get(0).tool().name()).isNotEqualTo(toolSpecs.get(1).tool().name());
	}

	interface MixedMethodsInterface {

		@McpTool(name = "valid-tool", description = "Valid tool")
		public String validTool(String input);

		public String nonAnnotatedMethod(String input);

		@McpTool(name = "mono-tool", description = "Mono tool")
		public Mono<String> monoTool(String input);

	}

	@Test
	void testGetToolSpecificationsWithMixedMethods() {
		class MixedMethods implements MixedMethodsInterface {

			public String validTool(String input) {
				return "Valid: " + input;
			}

			public String nonAnnotatedMethod(String input) {
				return "Non-annotated: " + input;
			}

			public Mono<String> monoTool(String input) {
				return Mono.just("Mono: " + input);
			}

		}

		MixedMethods toolObject = new MixedMethods();
		SyncMcpToolGroupProvider provider = new SyncMcpToolGroupProvider(toolObject, MixedMethodsInterface.class);

		List<SyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(1);
		assertThat(toolSpecs.get(0).tool().name()).isEqualTo(MixedMethodsInterface.class.getName() + ".valid-tool");
		assertThat(toolSpecs.get(0).tool().description()).isEqualTo("Valid tool");
	}

	interface ComplexParameterToolInterface {

		@McpTool(name = "complex-tool", description = "Tool with complex parameters")
		public String complexTool(String name, int age, boolean active, List<String> tags);
		
	}

	@Test
	void testGetToolSpecificationsWithComplexParameters() {
		class ComplexParameterTool implements ComplexParameterToolInterface {

			public String complexTool(String name, int age, boolean active, List<String> tags) {
				return String.format("Name: %s, Age: %d, Active: %b, Tags: %s", name, age, active,
						String.join(",", tags));
			}

		}

		ComplexParameterTool toolObject = new ComplexParameterTool();
		SyncMcpToolGroupProvider provider = new SyncMcpToolGroupProvider(toolObject, ComplexParameterToolInterface.class);

		List<SyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(1);
		assertThat(toolSpecs.get(0).tool().name()).isEqualTo(ComplexParameterToolInterface.class.getName() + ".complex-tool");
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

	interface NoParameterToolInterface {

		@McpTool(name = "no-param-tool", description = "Tool with no parameters")
		public String noParamTool();

	}

	@Test
	void testGetToolSpecificationsWithNoParameters() {
		class NoParameterTool implements NoParameterToolInterface {

			public String noParamTool() {
				return "No parameters needed";
			}

		}

		NoParameterTool toolObject = new NoParameterTool();
		SyncMcpToolGroupProvider provider = new SyncMcpToolGroupProvider(toolObject, NoParameterToolInterface.class);

		List<SyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(1);
		assertThat(toolSpecs.get(0).tool().name()).isEqualTo(NoParameterToolInterface.class.getName() + ".no-param-tool");
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

	interface CallToolResultToolInterface {

		@McpTool(name = "result-tool", description = "Tool returning CallToolResult")
		public CallToolResult resultTool(String message);

	}

	@Test
	void testGetToolSpecificationsWithCallToolResultReturn() {
		class CallToolResultTool implements CallToolResultToolInterface {

			public CallToolResult resultTool(String message) {
				return CallToolResult.builder().addTextContent("Result: " + message).build();
			}

		}

		CallToolResultTool toolObject = new CallToolResultTool();
		SyncMcpToolGroupProvider provider = new SyncMcpToolGroupProvider(toolObject, CallToolResultToolInterface.class);

		List<SyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(1);
		assertThat(toolSpecs.get(0).tool().name()).isEqualTo(CallToolResultToolInterface.class.getName() + ".result-tool");
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

	interface SchemaTestToolInterface {

		@McpTool(name = "schema-tool", description = "Tool for schema testing")
		public String schemaTool(String requiredParam, Integer optionalParam);

	}

	@Test
	void testGetToolSpecificationsJsonSchemaGeneration() {
		class SchemaTestTool implements SchemaTestToolInterface {

			public String schemaTool(String requiredParam, Integer optionalParam) {
				return "Schema test: " + requiredParam + ", " + optionalParam;
			}

		}

		SchemaTestTool toolObject = new SchemaTestTool();
		SyncMcpToolGroupProvider provider = new SyncMcpToolGroupProvider(toolObject, SchemaTestToolInterface.class);

		List<SyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(1);
		SyncToolSpecification toolSpec = toolSpecs.get(0);

		assertThat(toolSpec.tool().name()).isEqualTo(SchemaTestToolInterface.class.getName() + ".schema-tool");
		assertThat(toolSpec.tool().description()).isEqualTo("Tool for schema testing");
		assertThat(toolSpec.tool().inputSchema()).isNotNull();

		// The input schema should be a valid JSON string containing parameter names
		String schemaString = toolSpec.tool().inputSchema().toString();
		assertThat(schemaString).isNotEmpty();
		assertThat(schemaString).contains("requiredParam");
		assertThat(schemaString).contains("optionalParam");
	}

}
