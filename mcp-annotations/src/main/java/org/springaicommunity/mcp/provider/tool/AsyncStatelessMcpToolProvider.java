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

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.server.McpStatelessServerFeatures.AsyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.McpPredicates;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.method.tool.AsyncStatelessMcpToolMethodCallback;
import org.springaicommunity.mcp.method.tool.ReactiveUtils;
import org.springaicommunity.mcp.method.tool.ReturnMode;
import org.springaicommunity.mcp.method.tool.utils.ClassUtils;
import org.springaicommunity.mcp.method.tool.utils.JsonSchemaGenerator;
import reactor.core.publisher.Mono;

/**
 * Provider for asynchronous stateless MCP tool methods.
 *
 * This provider creates tool specifications for methods annotated with {@link McpTool}
 * that are designed to work in a stateless manner using {@link McpTransportContext} and
 * return reactive types.
 *
 * @author Christian Tzolov
 */
public class AsyncStatelessMcpToolProvider extends AbstractMcpToolProvider {

	private static final Logger logger = LoggerFactory.getLogger(AsyncStatelessMcpToolProvider.class);

	/**
	 * Create a new AsyncStatelessMcpToolProvider.
	 * @param toolObjects the objects containing methods annotated with {@link McpTool}
	 */
	public AsyncStatelessMcpToolProvider(List<Object> toolObjects) {
		super(toolObjects);
	}

	/**
	 * Get the async stateless tool specifications.
	 * @return the list of async stateless tool specifications
	 */
	public List<AsyncToolSpecification> getToolSpecifications() {

		List<AsyncToolSpecification> toolSpecs = this.toolObjects.stream()
			.map(toolObject -> Stream.of(doGetClassMethods(toolObject))
				.filter(method -> method.isAnnotationPresent(McpTool.class))
				.filter(McpPredicates.filterNonReactiveReturnTypeMethod())
				.filter(McpPredicates.filterMethodWithBidirectionalParameters())
				.sorted((m1, m2) -> m1.getName().compareTo(m2.getName()))
				.map(mcpToolMethod -> {

					var toolJavaAnnotation = doGetMcpToolAnnotation(mcpToolMethod);

					String toolName = Utils.hasText(toolJavaAnnotation.name()) ? toolJavaAnnotation.name()
							: mcpToolMethod.getName();

					String toolDescrption = toolJavaAnnotation.description();

					String inputSchema = JsonSchemaGenerator.generateForMethodInput(mcpToolMethod);

					var toolBuilder = McpSchema.Tool.builder()
						.name(toolName)
						.description(toolDescrption)
						.inputSchema(this.getJsonMapper(), inputSchema);

					var title = toolJavaAnnotation.title();

					// Tool annotations
					if (toolJavaAnnotation.annotations() != null) {
						var toolAnnotations = toolJavaAnnotation.annotations();
						toolBuilder.annotations(new McpSchema.ToolAnnotations(toolAnnotations.title(),
								toolAnnotations.readOnlyHint(), toolAnnotations.destructiveHint(),
								toolAnnotations.idempotentHint(), toolAnnotations.openWorldHint(), null));

						// If not provided, the name should be used for display (except
						// for Tool, where annotations.title should be given precedence
						// over using name, if present).
						if (!Utils.hasText(title)) {
							title = toolAnnotations.title();
						}
					}

					// If not provided, the name should be used for display (except
					// for Tool, where annotations.title should be given precedence
					// over using name, if present).
					if (!Utils.hasText(title)) {
						title = toolName;
					}
					toolBuilder.title(title);

					// Generate Output Schema from the method return type.
					// Output schema is not generated for primitive types, void,
					// CallToolResult, simple value types (String, etc.)
					// or if generateOutputSchema attribute is set to false.
					if (toolJavaAnnotation.generateOutputSchema()
							&& !ReactiveUtils.isReactiveReturnTypeOfVoid(mcpToolMethod)
							&& !ReactiveUtils.isReactiveReturnTypeOfCallToolResult(mcpToolMethod)) {

						ReactiveUtils.getReactiveReturnTypeArgument(mcpToolMethod).ifPresent(typeArgument -> {
							Class<?> methodReturnType = typeArgument instanceof Class<?> ? (Class<?>) typeArgument
									: null;
							if (!ClassUtils.isPrimitiveOrWrapper(methodReturnType)
									&& !ClassUtils.isSimpleValueType(methodReturnType)) {
								toolBuilder.outputSchema(this.getJsonMapper(),
										JsonSchemaGenerator.generateFromType(typeArgument));
							}
						});
					}
					var tool = toolBuilder.build();

					ReturnMode returnMode = tool.outputSchema() != null ? ReturnMode.STRUCTURED
							: ReactiveUtils.isReactiveReturnTypeOfVoid(mcpToolMethod) ? ReturnMode.VOID
									: ReturnMode.TEXT;

					BiFunction<McpTransportContext, CallToolRequest, Mono<CallToolResult>> methodCallback = new AsyncStatelessMcpToolMethodCallback(
							returnMode, mcpToolMethod, toolObject, this.doGetToolCallException());

					AsyncToolSpecification toolSpec = AsyncToolSpecification.builder()
						.tool(tool)
						.callHandler(methodCallback)
						.build();

					return toolSpec;
				})
				.toList())
			.flatMap(List::stream)
			.toList();

		if (toolSpecs.isEmpty()) {
			logger.warn("No tool methods found in the provided tool objects: {}", this.toolObjects);
		}

		return toolSpecs;
	}

}
