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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.method.tool.ReactiveUtils;
import org.springaicommunity.mcp.method.tool.ReturnMode;
import org.springaicommunity.mcp.method.tool.SyncMcpToolMethodCallback;
import org.springaicommunity.mcp.method.tool.utils.ClassUtils;
import org.springaicommunity.mcp.method.tool.utils.JsonSchemaGenerator;

import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.util.Assert;
import io.modelcontextprotocol.util.Utils;
import reactor.core.publisher.Mono;

/**
 * @author Christian Tzolov
 */
public class SyncMcpToolProvider {

	private static final Logger logger = LoggerFactory.getLogger(SyncMcpToolProvider.class);

	private final List<Object> toolObjects;

	/**
	 * Create a new SyncMcpToolProvider.
	 * @param toolObjects the objects containing methods annotated with {@link McpTool}
	 */
	public SyncMcpToolProvider(List<Object> toolObjects) {
		Assert.notNull(toolObjects, "toolObjects cannot be null");
		this.toolObjects = toolObjects;
	}

	/**
	 * Get the tool handler.
	 * @return the tool handler
	 * @throws IllegalStateException if no tool methods are found or if multiple tool
	 * methods are found
	 */
	public List<SyncToolSpecification> getToolSpecifications() {

		List<SyncToolSpecification> toolSpecs = this.toolObjects.stream()
			.map(toolObject -> Stream.of(doGetClassMethods(toolObject))
				.filter(method -> method.isAnnotationPresent(McpTool.class))
				.filter(method -> !Mono.class.isAssignableFrom(method.getReturnType()))
				.map(mcpToolMethod -> {

					McpTool toolAnnotation = doGetMcpToolAnnotation(mcpToolMethod);

					String toolName = Utils.hasText(toolAnnotation.name()) ? toolAnnotation.name()
							: mcpToolMethod.getName();

					String toolDescription = toolAnnotation.description();

					// Check if method has CallToolRequest parameter
					boolean hasCallToolRequestParam = Arrays.stream(mcpToolMethod.getParameterTypes())
						.anyMatch(type -> CallToolRequest.class.isAssignableFrom(type));

					String inputSchema;
					if (hasCallToolRequestParam) {
						// For methods with CallToolRequest, generate minimal schema or
						// use the one from the request
						// The schema generation will handle this appropriately
						inputSchema = JsonSchemaGenerator.generateForMethodInput(mcpToolMethod);
						logger.debug("Tool method '{}' uses CallToolRequest parameter, using minimal schema", toolName);
					}
					else {
						inputSchema = JsonSchemaGenerator.generateForMethodInput(mcpToolMethod);
					}

					var toolBuilder = McpSchema.Tool.builder()
						.name(toolName)
						.description(toolDescription)
						.inputSchema(inputSchema);

					// Tool annotations
					if (toolAnnotation.annotations() != null) {
						var toolAnnotations = toolAnnotation.annotations();
						toolBuilder.annotations(new McpSchema.ToolAnnotations(toolAnnotations.title(),
								toolAnnotations.readOnlyHint(), toolAnnotations.destructiveHint(),
								toolAnnotations.idempotentHint(), toolAnnotations.openWorldHint(), null));
					}

					// ReactiveUtils.isReactiveReturnTypeOfCallToolResult(mcpToolMethod);

					// Generate Output Schema from the method return type.
					// Output schema is not generated for primitive types, void,
					// CallToolResult, simple value types (String, etc.)
					// or if generateOutputSchema attribute is set to false.
					Class<?> methodReturnType = mcpToolMethod.getReturnType();
					if (toolAnnotation.generateOutputSchema() && methodReturnType != null
							&& methodReturnType != CallToolResult.class && methodReturnType != Void.class
							&& methodReturnType != void.class && !ClassUtils.isPrimitiveOrWrapper(methodReturnType)
							&& !ClassUtils.isSimpleValueType(methodReturnType)) {

						toolBuilder.outputSchema(JsonSchemaGenerator.generateFromClass(methodReturnType));
					}

					var tool = toolBuilder.build();

					boolean useStructuredOtput = tool.outputSchema() != null;

					ReturnMode returnMode = useStructuredOtput ? ReturnMode.STRUCTURED
							: (methodReturnType == Void.TYPE || methodReturnType == void.class ? ReturnMode.VOID
									: ReturnMode.TEXT);

					BiFunction<McpSyncServerExchange, CallToolRequest, CallToolResult> methodCallback = new SyncMcpToolMethodCallback(
							returnMode, mcpToolMethod, toolObject);

					var toolSpec = SyncToolSpecification.builder().tool(tool).callHandler(methodCallback).build();

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

	protected Method[] doGetClassMethods(Object bean) {
		return bean.getClass().getDeclaredMethods();
	}

	protected McpTool doGetMcpToolAnnotation(Method method) {
		return method.getAnnotation(McpTool.class);
	}

}
