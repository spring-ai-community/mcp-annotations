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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpTool;
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

	protected final List<Object> toolObjects;

	// optional set of classes defining groups of annotated McpTools methods
	protected final Class<?>[] toolGroups;

	/**
	 * Create a new SyncMcpToolProvider.
	 * @param toolObjects the objects containing methods annotated with {@link McpTool}
	 * @param toolGroups optional array of classes defining the tool groups that all
	 * toolObjects are required to implement
	 * @exception IllegalArgumentException thrown if toolObjects is null, or any of the
	 * specified toolGroups are not implemented by all of the toolObjects
	 */
	public SyncMcpToolProvider(List<Object> toolObjects, Class<?>... toolGroups) {
		Assert.notNull(toolObjects, "toolObjects cannot be null");
		this.toolObjects = toolObjects;
		Assert.notNull(toolGroups, "toolGroups cannot be null");
		this.toolGroups = toolGroups;
		// verify that every toolObject is instance of all toolGroups
		this.toolObjects.forEach(toolObject -> {
			Arrays.asList(this.toolGroups)
				.forEach(clazz -> Assert.isTrue(clazz.isInstance(toolObject),
						String.format("toolObject=%s is not an instance of %s", toolObject, clazz.getName())));
		});
	}

	public SyncMcpToolProvider(Object toolObject, Class<?>... toolGroups) {
		this(List.of(toolObject), toolGroups);
	}

	protected Method[] doGetMethods(Class<?> toolGroup) {
		// For interfaces, getMethods() gets super interface methods
		if (toolGroup.isInterface()) {
			return toolGroup.getMethods();
		}
		else {
			return toolGroup.getDeclaredMethods();
		}
	}

	protected String doGetFullyQualifiedToolName(String annotationToolName, Class<?> toolGroup) {
		return (this.toolGroups.length == 0) ? annotationToolName
				: new StringBuffer(toolGroup.getName()).append(".").append(annotationToolName).toString();
	}

	protected Class<?>[] doGetClasses(Object toolObject) {
		return (this.toolGroups.length == 0) ? new Class[] { toolObject.getClass() } : this.toolGroups;
	}

	protected <T> Predicate<T> distinctByName(Function<? super T, Object> nameExtractor) {
		Map<Object, Boolean> map = new ConcurrentHashMap<>();
		return t -> map.putIfAbsent(nameExtractor.apply(t), Boolean.TRUE) == null;
	}

	/**
	 * Get the tool handler.
	 * @return the tool handler
	 * @throws IllegalStateException if no tool methods are found or if multiple tool
	 * methods are found
	 */
	public List<SyncToolSpecification> getToolSpecifications() {
		List<SyncToolSpecification> toolSpecs = this.toolObjects.stream().map(toolObject -> {
			return Stream.of(doGetClasses(toolObject)).map(toolGroup -> {
				return Stream.of(doGetMethods(toolGroup))
					.filter(method -> method.isAnnotationPresent(McpTool.class))
					.filter(method -> !Mono.class.isAssignableFrom(method.getReturnType()))
					.map(mcpToolMethod -> {

						McpTool toolAnnotation = doGetMcpToolAnnotation(mcpToolMethod);

						String annotationToolName = Utils.hasText(toolAnnotation.name()) ? toolAnnotation.name()
								: mcpToolMethod.getName();

						String toolName = doGetFullyQualifiedToolName(annotationToolName, toolGroup);

						String toolDescription = toolAnnotation.description();

						// Check if method has CallToolRequest parameter
						boolean hasCallToolRequestParam = Arrays.stream(mcpToolMethod.getParameterTypes())
							.anyMatch(type -> CallToolRequest.class.isAssignableFrom(type));

						String inputSchema;
						if (hasCallToolRequestParam) {
							// For methods with CallToolRequest, generate minimal schema
							// or
							// use the one from the request
							// The schema generation will handle this appropriately
							inputSchema = JsonSchemaGenerator.generateForMethodInput(mcpToolMethod);
							logger.debug("Tool method '{}' uses CallToolRequest parameter, using minimal schema",
									toolName);
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
					.toList();
			}).flatMap(List::stream).toList();
		}).flatMap(List::stream).filter(distinctByName(s -> s.tool().name())).toList();

		if (toolSpecs.isEmpty()) {
			logger.warn("No tool methods found in the provided tool objects: {}", this.toolObjects);
		}

		return toolSpecs;
	}

	protected McpTool doGetMcpToolAnnotation(Method method) {
		return method.getAnnotation(McpTool.class);
	}

}
