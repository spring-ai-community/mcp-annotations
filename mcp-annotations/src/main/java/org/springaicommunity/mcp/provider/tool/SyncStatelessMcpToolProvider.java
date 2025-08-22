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
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.method.tool.ReactiveUtils;
import org.springaicommunity.mcp.method.tool.ReturnMode;
import org.springaicommunity.mcp.method.tool.SyncStatelessMcpToolMethodCallback;
import org.springaicommunity.mcp.method.tool.utils.ClassUtils;
import org.springaicommunity.mcp.method.tool.utils.JsonSchemaGenerator;

import io.modelcontextprotocol.server.McpStatelessServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpTransportContext;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.util.Assert;
import io.modelcontextprotocol.util.Utils;
import reactor.core.publisher.Mono;

/**
 * Provider for synchronous stateless MCP tool methods.
 * 
 * This provider creates tool specifications for methods annotated with {@link McpTool}
 * that are designed to work in a stateless manner using {@link McpTransportContext}.
 *
 * @author Christian Tzolov
 */
public class SyncStatelessMcpToolProvider {

	private static final Logger logger = LoggerFactory.getLogger(SyncStatelessMcpToolProvider.class);

	private final List<Object> toolObjects;

	/**
	 * Create a new SyncStatelessMcpToolProvider.
	 * @param toolObjects the objects containing methods annotated with {@link McpTool}
	 */
	public SyncStatelessMcpToolProvider(List<Object> toolObjects) {
		Assert.notNull(toolObjects, "toolObjects cannot be null");
		this.toolObjects = toolObjects;
	}

	/**
	 * Get the stateless tool specifications.
	 * @return the list of stateless tool specifications
	 */
	public List<SyncToolSpecification> getToolSpecifications() {

		List<SyncToolSpecification> toolSpecs = this.toolObjects.stream()
			.map(toolObject -> Stream.of(doGetClassMethods(toolObject))
				.filter(method -> method.isAnnotationPresent(McpTool.class))
				.filter(method -> !Mono.class.isAssignableFrom(method.getReturnType()))
				.map(mcpToolMethod -> {

					var toolAnnotation = doGetMcpToolAnnotation(mcpToolMethod);

					String toolName = Utils.hasText(toolAnnotation.name()) ? toolAnnotation.name()
							: mcpToolMethod.getName();

					String toolDescrption = toolAnnotation.description();

					String inputSchema = JsonSchemaGenerator.generateForMethodInput(mcpToolMethod);

					var toolBuilder = McpSchema.Tool.builder()
						.name(toolName)
						.description(toolDescrption)
						.inputSchema(inputSchema);

					// Tool annotations
					if (toolAnnotation.annotations() != null) {
						var toolAnnotations = toolAnnotation.annotations();
						toolBuilder.annotations(new McpSchema.ToolAnnotations(toolAnnotations.title(),
								toolAnnotations.readOnlyHint(), toolAnnotations.destructiveHint(),
								toolAnnotations.idempotentHint(), toolAnnotations.openWorldHint(), null));
					}

					ReactiveUtils.isReactiveReturnTypeOfCallToolResult(mcpToolMethod);
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

					BiFunction<McpTransportContext, CallToolRequest, CallToolResult> methodCallback = new SyncStatelessMcpToolMethodCallback(
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
