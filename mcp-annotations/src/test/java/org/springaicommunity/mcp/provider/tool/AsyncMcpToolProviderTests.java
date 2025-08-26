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
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpTool;

import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.server.McpServerFeatures.AsyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Tests for {@link AsyncMcpToolProvider}.
 *
 * @author Christian Tzolov
 */
public class AsyncMcpToolProviderTests {

	@Test
	void testConstructorWithNullToolObjects() {
		assertThatThrownBy(() -> new AsyncMcpToolProvider(null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("toolObjects cannot be null");
	}

	@Test
	void testGetToolSpecificationsWithSingleValidTool() {
		// Create a class with only one valid async tool method
		class SingleValidTool {

			@McpTool(name = "test-tool", description = "A test tool")
			public Mono<String> testTool(String input) {
				return Mono.just("Processed: " + input);
			}

		}

		SingleValidTool toolObject = new SingleValidTool();
		AsyncMcpToolProvider provider = new AsyncMcpToolProvider(List.of(toolObject));

		List<AsyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).isNotNull();
		assertThat(toolSpecs).hasSize(1);

		AsyncToolSpecification toolSpec = toolSpecs.get(0);
		assertThat(toolSpec.tool().name()).isEqualTo("test-tool");
		assertThat(toolSpec.tool().description()).isEqualTo("A test tool");
		assertThat(toolSpec.tool().inputSchema()).isNotNull();
		assertThat(toolSpec.callHandler()).isNotNull();

		// Test that the handler works
		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("test-tool", Map.of("input", "hello"));
		Mono<CallToolResult> result = toolSpec.callHandler().apply(exchange, request);

		StepVerifier.create(result).assertNext(callToolResult -> {
			assertThat(callToolResult).isNotNull();
			assertThat(callToolResult.isError()).isFalse();
			assertThat(callToolResult.content()).hasSize(1);
			assertThat(callToolResult.content().get(0)).isInstanceOf(TextContent.class);
			assertThat(((TextContent) callToolResult.content().get(0)).text()).isEqualTo("Processed: hello");
		}).verifyComplete();
	}

	@Test
	void testGetToolSpecificationsWithCustomToolName() {
		class CustomNameTool {

			@McpTool(name = "custom-name", description = "Custom named tool")
			public Mono<String> methodWithDifferentName(String input) {
				return Mono.just("Custom: " + input);
			}

		}

		CustomNameTool toolObject = new CustomNameTool();
		AsyncMcpToolProvider provider = new AsyncMcpToolProvider(List.of(toolObject));

		List<AsyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(1);
		assertThat(toolSpecs.get(0).tool().name()).isEqualTo("custom-name");
		assertThat(toolSpecs.get(0).tool().description()).isEqualTo("Custom named tool");
	}

	@Test
	void testGetToolSpecificationsWithDefaultToolName() {
		class DefaultNameTool {

			@McpTool(description = "Tool with default name")
			public Mono<String> defaultNameMethod(String input) {
				return Mono.just("Default: " + input);
			}

		}

		DefaultNameTool toolObject = new DefaultNameTool();
		AsyncMcpToolProvider provider = new AsyncMcpToolProvider(List.of(toolObject));

		List<AsyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(1);
		assertThat(toolSpecs.get(0).tool().name()).isEqualTo("defaultNameMethod");
		assertThat(toolSpecs.get(0).tool().description()).isEqualTo("Tool with default name");
	}

	@Test
	void testGetToolSpecificationsWithEmptyToolName() {
		class EmptyNameTool {

			@McpTool(name = "", description = "Tool with empty name")
			public Mono<String> emptyNameMethod(String input) {
				return Mono.just("Empty: " + input);
			}

		}

		EmptyNameTool toolObject = new EmptyNameTool();
		AsyncMcpToolProvider provider = new AsyncMcpToolProvider(List.of(toolObject));

		List<AsyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(1);
		assertThat(toolSpecs.get(0).tool().name()).isEqualTo("emptyNameMethod");
		assertThat(toolSpecs.get(0).tool().description()).isEqualTo("Tool with empty name");
	}

	@Test
	void testGetToolSpecificationsFiltersOutSyncReturnTypes() {
		class MixedReturnTool {

			@McpTool(name = "sync-tool", description = "Synchronous tool")
			public String syncTool(String input) {
				return "Sync: " + input;
			}

			@McpTool(name = "async-tool", description = "Asynchronous tool")
			public Mono<String> asyncTool(String input) {
				return Mono.just("Async: " + input);
			}

		}

		MixedReturnTool toolObject = new MixedReturnTool();
		AsyncMcpToolProvider provider = new AsyncMcpToolProvider(List.of(toolObject));

		List<AsyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(1);
		assertThat(toolSpecs.get(0).tool().name()).isEqualTo("async-tool");
		assertThat(toolSpecs.get(0).tool().description()).isEqualTo("Asynchronous tool");
	}

	@Test
	void testGetToolSpecificationsWithFluxReturnType() {
		class FluxReturnTool {

			@McpTool(name = "flux-tool", description = "Tool returning Flux")
			public Flux<String> fluxTool(String input) {
				return Flux.just("First: " + input, "Second: " + input);
			}

			@McpTool(name = "mono-tool", description = "Tool returning Mono")
			public Mono<String> monoTool(String input) {
				return Mono.just("Mono: " + input);
			}

		}

		FluxReturnTool toolObject = new FluxReturnTool();
		AsyncMcpToolProvider provider = new AsyncMcpToolProvider(List.of(toolObject));

		List<AsyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(2);
		assertThat(toolSpecs.get(0).tool().name()).isIn("flux-tool", "mono-tool");
		assertThat(toolSpecs.get(1).tool().name()).isIn("flux-tool", "mono-tool");
		assertThat(toolSpecs.get(0).tool().name()).isNotEqualTo(toolSpecs.get(1).tool().name());
	}

	@Test
	void testGetToolSpecificationsWithMultipleToolMethods() {
		class MultipleToolMethods {

			@McpTool(name = "tool1", description = "First tool")
			public Mono<String> firstTool(String input) {
				return Mono.just("First: " + input);
			}

			@McpTool(name = "tool2", description = "Second tool")
			public Mono<String> secondTool(String input) {
				return Mono.just("Second: " + input);
			}

		}

		MultipleToolMethods toolObject = new MultipleToolMethods();
		AsyncMcpToolProvider provider = new AsyncMcpToolProvider(List.of(toolObject));

		List<AsyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(2);
		assertThat(toolSpecs.get(0).tool().name()).isIn("tool1", "tool2");
		assertThat(toolSpecs.get(1).tool().name()).isIn("tool1", "tool2");
		assertThat(toolSpecs.get(0).tool().name()).isNotEqualTo(toolSpecs.get(1).tool().name());
	}

	@Test
	void testGetToolSpecificationsWithMultipleToolObjects() {
		class FirstToolObject {

			@McpTool(name = "first-tool", description = "First tool")
			public Mono<String> firstTool(String input) {
				return Mono.just("First: " + input);
			}

		}

		class SecondToolObject {

			@McpTool(name = "second-tool", description = "Second tool")
			public Mono<String> secondTool(String input) {
				return Mono.just("Second: " + input);
			}

		}

		FirstToolObject firstObject = new FirstToolObject();
		SecondToolObject secondObject = new SecondToolObject();
		AsyncMcpToolProvider provider = new AsyncMcpToolProvider(List.of(firstObject, secondObject));

		List<AsyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(2);
		assertThat(toolSpecs.get(0).tool().name()).isIn("first-tool", "second-tool");
		assertThat(toolSpecs.get(1).tool().name()).isIn("first-tool", "second-tool");
		assertThat(toolSpecs.get(0).tool().name()).isNotEqualTo(toolSpecs.get(1).tool().name());
	}

	@Test
	void testGetToolSpecificationsWithMixedMethods() {
		class MixedMethods {

			@McpTool(name = "valid-tool", description = "Valid async tool")
			public Mono<String> validTool(String input) {
				return Mono.just("Valid: " + input);
			}

			public String nonAnnotatedMethod(String input) {
				return "Non-annotated: " + input;
			}

			@McpTool(name = "sync-tool", description = "Sync tool")
			public String syncTool(String input) {
				return "Sync: " + input;
			}

		}

		MixedMethods toolObject = new MixedMethods();
		AsyncMcpToolProvider provider = new AsyncMcpToolProvider(List.of(toolObject));

		List<AsyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(1);
		assertThat(toolSpecs.get(0).tool().name()).isEqualTo("valid-tool");
		assertThat(toolSpecs.get(0).tool().description()).isEqualTo("Valid async tool");
	}

	@Test
	void testGetToolSpecificationsWithComplexParameters() {
		class ComplexParameterTool {

			@McpTool(name = "complex-tool", description = "Tool with complex parameters")
			public Mono<String> complexTool(String name, int age, boolean active, List<String> tags) {
				return Mono.just(String.format("Name: %s, Age: %d, Active: %b, Tags: %s", name, age, active,
						String.join(",", tags)));
			}

		}

		ComplexParameterTool toolObject = new ComplexParameterTool();
		AsyncMcpToolProvider provider = new AsyncMcpToolProvider(List.of(toolObject));

		List<AsyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(1);
		assertThat(toolSpecs.get(0).tool().name()).isEqualTo("complex-tool");
		assertThat(toolSpecs.get(0).tool().description()).isEqualTo("Tool with complex parameters");
		assertThat(toolSpecs.get(0).tool().inputSchema()).isNotNull();

		// Test that the handler works with complex parameters
		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("complex-tool",
				Map.of("name", "John", "age", 30, "active", true, "tags", List.of("tag1", "tag2")));
		Mono<CallToolResult> result = toolSpecs.get(0).callHandler().apply(exchange, request);

		StepVerifier.create(result).assertNext(callToolResult -> {
			assertThat(callToolResult).isNotNull();
			assertThat(callToolResult.isError()).isFalse();
			assertThat(callToolResult.content()).hasSize(1);
			assertThat(callToolResult.content().get(0)).isInstanceOf(TextContent.class);
			assertThat(((TextContent) callToolResult.content().get(0)).text())
				.isEqualTo("Name: John, Age: 30, Active: true, Tags: tag1,tag2");
		}).verifyComplete();
	}

	@Test
	void testGetToolSpecificationsWithNoParameters() {
		class NoParameterTool {

			@McpTool(name = "no-param-tool", description = "Tool with no parameters")
			public Mono<String> noParamTool() {
				return Mono.just("No parameters needed");
			}

		}

		NoParameterTool toolObject = new NoParameterTool();
		AsyncMcpToolProvider provider = new AsyncMcpToolProvider(List.of(toolObject));

		List<AsyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(1);
		assertThat(toolSpecs.get(0).tool().name()).isEqualTo("no-param-tool");
		assertThat(toolSpecs.get(0).tool().description()).isEqualTo("Tool with no parameters");

		// Test that the handler works with no parameters
		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("no-param-tool", Map.of());
		Mono<CallToolResult> result = toolSpecs.get(0).callHandler().apply(exchange, request);

		StepVerifier.create(result).assertNext(callToolResult -> {
			assertThat(callToolResult).isNotNull();
			assertThat(callToolResult.isError()).isFalse();
			assertThat(callToolResult.content()).hasSize(1);
			assertThat(callToolResult.content().get(0)).isInstanceOf(TextContent.class);
			assertThat(((TextContent) callToolResult.content().get(0)).text()).isEqualTo("No parameters needed");
		}).verifyComplete();
	}

	@Test
	void testGetToolSpecificationsWithCallToolResultReturn() {
		class CallToolResultTool {

			@McpTool(name = "result-tool", description = "Tool returning Mono<CallToolResult>")
			public Mono<CallToolResult> resultTool(String message) {
				return Mono.just(CallToolResult.builder().addTextContent("Result: " + message).build());
			}

		}

		CallToolResultTool toolObject = new CallToolResultTool();
		AsyncMcpToolProvider provider = new AsyncMcpToolProvider(List.of(toolObject));

		List<AsyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(1);
		assertThat(toolSpecs.get(0).tool().name()).isEqualTo("result-tool");
		assertThat(toolSpecs.get(0).tool().description()).isEqualTo("Tool returning Mono<CallToolResult>");

		// Test that the handler works with Mono<CallToolResult> return type
		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("result-tool", Map.of("message", "test"));
		Mono<CallToolResult> result = toolSpecs.get(0).callHandler().apply(exchange, request);

		StepVerifier.create(result).assertNext(callToolResult -> {
			assertThat(callToolResult).isNotNull();
			assertThat(callToolResult.isError()).isFalse();
			assertThat(callToolResult.content()).hasSize(1);
			assertThat(callToolResult.content().get(0)).isInstanceOf(TextContent.class);
			assertThat(((TextContent) callToolResult.content().get(0)).text()).isEqualTo("Result: test");
		}).verifyComplete();
	}

	@Test
	void testGetToolSpecificationsWithMonoVoidReturn() {
		class MonoVoidTool {

			@McpTool(name = "void-tool", description = "Tool returning Mono<Void>")
			public Mono<Void> voidTool(String input) {
				// Simulate some side effect
				System.out.println("Processing: " + input);
				return Mono.empty();
			}

		}

		MonoVoidTool toolObject = new MonoVoidTool();
		AsyncMcpToolProvider provider = new AsyncMcpToolProvider(List.of(toolObject));

		List<AsyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(1);
		assertThat(toolSpecs.get(0).tool().name()).isEqualTo("void-tool");
		assertThat(toolSpecs.get(0).tool().description()).isEqualTo("Tool returning Mono<Void>");

		// Test that the handler works with Mono<Void> return type
		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("void-tool", Map.of("input", "test"));
		Mono<CallToolResult> result = toolSpecs.get(0).callHandler().apply(exchange, request);

		StepVerifier.create(result).assertNext(callToolResult -> {
			assertThat(callToolResult).isNotNull();
			assertThat(callToolResult.isError()).isFalse();
			// For Mono<Void>, the framework returns a "Done" message
			assertThat(callToolResult.content()).hasSize(1);
			assertThat(callToolResult.content().get(0)).isInstanceOf(TextContent.class);
			assertThat(((TextContent) callToolResult.content().get(0)).text()).isEqualTo("\"Done\"");
		}).verifyComplete();
	}

	@Test
	void testGetToolSpecificationsWithPrivateMethod() {
		class PrivateMethodTool {

			@McpTool(name = "private-tool", description = "Private tool method")
			private Mono<String> privateTool(String input) {
				return Mono.just("Private: " + input);
			}

		}

		PrivateMethodTool toolObject = new PrivateMethodTool();
		AsyncMcpToolProvider provider = new AsyncMcpToolProvider(List.of(toolObject));

		List<AsyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(1);
		assertThat(toolSpecs.get(0).tool().name()).isEqualTo("private-tool");
		assertThat(toolSpecs.get(0).tool().description()).isEqualTo("Private tool method");

		// Test that the handler works with private methods
		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("private-tool", Map.of("input", "test"));
		Mono<CallToolResult> result = toolSpecs.get(0).callHandler().apply(exchange, request);

		StepVerifier.create(result).assertNext(callToolResult -> {
			assertThat(callToolResult).isNotNull();
			assertThat(callToolResult.isError()).isFalse();
			assertThat(callToolResult.content()).hasSize(1);
			assertThat(callToolResult.content().get(0)).isInstanceOf(TextContent.class);
			assertThat(((TextContent) callToolResult.content().get(0)).text()).isEqualTo("Private: test");
		}).verifyComplete();
	}

	@Test
	void testGetToolSpecificationsJsonSchemaGeneration() {
		class SchemaTestTool {

			@McpTool(name = "schema-tool", description = "Tool for schema testing")
			public Mono<String> schemaTool(String requiredParam, Integer optionalParam) {
				return Mono.just("Schema test: " + requiredParam + ", " + optionalParam);
			}

		}

		SchemaTestTool toolObject = new SchemaTestTool();
		AsyncMcpToolProvider provider = new AsyncMcpToolProvider(List.of(toolObject));

		List<AsyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(1);
		AsyncToolSpecification toolSpec = toolSpecs.get(0);

		assertThat(toolSpec.tool().name()).isEqualTo("schema-tool");
		assertThat(toolSpec.tool().description()).isEqualTo("Tool for schema testing");
		assertThat(toolSpec.tool().inputSchema()).isNotNull();

		// The input schema should be a valid JSON string containing parameter names
		String schemaString = toolSpec.tool().inputSchema().toString();
		assertThat(schemaString).isNotEmpty();
		assertThat(schemaString).contains("requiredParam");
		assertThat(schemaString).contains("optionalParam");
	}

	@Test
	void testGetToolSpecificationsWithFluxHandling() {
		class FluxHandlingTool {

			@McpTool(name = "flux-handling-tool", description = "Tool that handles Flux properly")
			public Flux<String> fluxHandlingTool(String input) {
				return Flux.just("Item1: " + input, "Item2: " + input, "Item3: " + input);
			}

		}

		FluxHandlingTool toolObject = new FluxHandlingTool();
		AsyncMcpToolProvider provider = new AsyncMcpToolProvider(List.of(toolObject));

		List<AsyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(1);
		assertThat(toolSpecs.get(0).tool().name()).isEqualTo("flux-handling-tool");
		assertThat(toolSpecs.get(0).tool().description()).isEqualTo("Tool that handles Flux properly");

		// Test that the handler works with Flux return type
		McpAsyncServerExchange exchange = mock(McpAsyncServerExchange.class);
		CallToolRequest request = new CallToolRequest("flux-handling-tool", Map.of("input", "test"));
		Mono<CallToolResult> result = toolSpecs.get(0).callHandler().apply(exchange, request);

		StepVerifier.create(result).assertNext(callToolResult -> {
			assertThat(callToolResult).isNotNull();
			assertThat(callToolResult.isError()).isFalse();
			assertThat(callToolResult.content()).hasSize(1);
			assertThat(callToolResult.content().get(0)).isInstanceOf(TextContent.class);
			// Flux results are typically concatenated or collected into a single response
			String content = ((TextContent) callToolResult.content().get(0)).text();
			assertThat(content).contains("test");
		}).verifyComplete();
	}

	@Test
	void testGetToolSpecificationsWithOutputSchemaGeneration() {
		// Helper class for complex return type
		class ComplexResult {

			private final String message;

			private final int count;

			private final boolean success;

			public ComplexResult(String message, int count, boolean success) {
				this.message = message;
				this.count = count;
				this.success = success;
			}

			public String getMessage() {
				return message;
			}

			public int getCount() {
				return count;
			}

			public boolean isSuccess() {
				return success;
			}

		}

		class OutputSchemaTestTool {

			@McpTool(name = "output-schema-tool", description = "Tool for output schema testing",
					generateOutputSchema = true)
			public Mono<ComplexResult> outputSchemaTool(String input) {
				return Mono.just(new ComplexResult(input, 42, true));
			}

		}

		OutputSchemaTestTool toolObject = new OutputSchemaTestTool();
		AsyncMcpToolProvider provider = new AsyncMcpToolProvider(List.of(toolObject));

		List<AsyncToolSpecification> toolSpecs = provider.getToolSpecifications();

		assertThat(toolSpecs).hasSize(1);
		AsyncToolSpecification toolSpec = toolSpecs.get(0);

		assertThat(toolSpec.tool().name()).isEqualTo("output-schema-tool");
		assertThat(toolSpec.tool().description()).isEqualTo("Tool for output schema testing");
		assertThat(toolSpec.tool().inputSchema()).isNotNull();
		// Output schema should be generated for complex return types
		assertThat(toolSpec.tool().outputSchema()).isNotNull();
	}

}
