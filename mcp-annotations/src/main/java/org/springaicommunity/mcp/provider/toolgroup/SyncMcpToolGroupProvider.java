package org.springaicommunity.mcp.provider.toolgroup;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

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

public class SyncMcpToolGroupProvider {

	protected static final Logger logger = LoggerFactory.getLogger(SyncMcpToolGroupProvider.class);

	private final Object serviceObject;

	private final Class<?> toolGroup;

	public SyncMcpToolGroupProvider(Object serviceObject, Class<?> toolGroup) {
		Assert.notNull(serviceObject, "serviceObject cannot be null");
		this.serviceObject = serviceObject;
		Assert.notNull(toolGroup, "toolGroup cannot be null");
		this.toolGroup = toolGroup;
		Assert.isTrue(this.toolGroup.isInstance(this.serviceObject),
				String.format("serviceObject must be instance of toolGroup=%s", this.toolGroup.getName()));
	}

	protected Object getServiceObject() {
		return this.serviceObject;
	}

	protected Class<?> getToolGroup() {
		return this.toolGroup;
	}

	protected String createFullyQualifiedToolName(String toolName) {
		return new StringBuffer(this.toolGroup.getName()).append(".").append(toolName).toString();
	}

	protected String generateInputSchema(Method method) {
		return JsonSchemaGenerator.generateForMethodInput(method);
	}

	protected String generateOutputSchema(Class<?> methodReturnType) {
		return JsonSchemaGenerator.generateFromClass(methodReturnType);
	}

	protected McpTool doGetMcpToolAnnotation(Method method) {
		return method.getAnnotation(McpTool.class);
	}

	protected Method[] doGetClassMethods() {
		Class<?> toolGroup = getToolGroup();
		if (toolGroup.isInterface()) {
			return toolGroup.getMethods();
		} else {
			return toolGroup.getDeclaredMethods();
		}
	}

	public List<SyncToolSpecification> getToolSpecifications() {
		List<SyncToolSpecification> toolServiceSpecs = Arrays.asList(doGetClassMethods()).stream()
				.filter(method -> method.isAnnotationPresent(McpTool.class))
				.filter(method -> !Mono.class.isAssignableFrom(method.getReturnType())).map(mcpToolMethod -> {

					var toolAnnotation = doGetMcpToolAnnotation(mcpToolMethod);

					String toolName = createFullyQualifiedToolName(
							Utils.hasText(toolAnnotation.name()) ? toolAnnotation.name() : mcpToolMethod.getName());

					String toolDescrption = toolAnnotation.description();

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

					var toolBuilder = McpSchema.Tool.builder().name(toolName).description(toolDescrption)
							.inputSchema(inputSchema);

					// Tool annotations
					if (toolAnnotation.annotations() != null) {
						var toolAnnotations = toolAnnotation.annotations();
						toolBuilder.annotations(new McpSchema.ToolAnnotations(toolAnnotations.title(),
								toolAnnotations.readOnlyHint(), toolAnnotations.destructiveHint(),
								toolAnnotations.idempotentHint(), toolAnnotations.openWorldHint(), null));
					}

					Class<?> methodReturnType = mcpToolMethod.getReturnType();
					if (toolAnnotation.generateOutputSchema() && methodReturnType != null
							&& methodReturnType != CallToolResult.class && methodReturnType != Void.class
							&& methodReturnType != void.class && !ClassUtils.isPrimitiveOrWrapper(methodReturnType)
							&& !ClassUtils.isSimpleValueType(methodReturnType)) {

						toolBuilder.outputSchema(generateOutputSchema(methodReturnType));
					}

					var tool = toolBuilder.build();

					boolean useStructuredOtput = tool.outputSchema() != null;

					ReturnMode returnMode = useStructuredOtput ? ReturnMode.STRUCTURED
							: (methodReturnType == Void.TYPE || methodReturnType == void.class ? ReturnMode.VOID
									: ReturnMode.TEXT);

					BiFunction<McpSyncServerExchange, CallToolRequest, CallToolResult> methodCallback = new SyncMcpToolMethodCallback(
							returnMode, mcpToolMethod, getServiceObject());

					var toolSpec = SyncToolSpecification.builder().tool(tool).callHandler(methodCallback).build();

					if (logger.isDebugEnabled()) {
						logger.debug("created sync toolspec={}", toolSpec);
					}

					return toolSpec;
				}).toList();
		if (toolServiceSpecs.isEmpty()) {
			logger.warn("No sync toolgroup methods found in service object: {}", getServiceObject());
		}
		return toolServiceSpecs;
	}

}
