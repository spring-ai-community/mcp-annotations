package bndtools.demo.mcpserver;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.method.tool.ReactiveUtils;
import org.springaicommunity.mcp.method.tool.ReturnMode;
import org.springaicommunity.mcp.method.tool.SyncMcpToolMethodCallback;
import org.springaicommunity.mcp.method.tool.utils.ClassUtils;

import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.util.Utils;
import reactor.core.publisher.Mono;

public class SyncMcpToolGroupProvider extends McpToolGroupProvider {

	private List<SyncToolSpecification> syncToolSpecifications;

	public SyncMcpToolGroupProvider(Object serviceObject, Class<?> toolGroup) {
		super(serviceObject, toolGroup);
	}

	public List<SyncToolSpecification> getSyncToolSpecifications() {
		if (this.syncToolSpecifications == null) {
			List<SyncToolSpecification> toolServiceSpecs = Arrays.asList(getToolGroup().getMethods()).stream()
					.filter(method -> method.isAnnotationPresent(McpTool.class))
					.filter(method -> !Mono.class.isAssignableFrom(method.getReturnType())).map(mcpToolMethod -> {

						var toolAnnotation = mcpToolMethod.getAnnotation(McpTool.class);

						String toolName = createFullyQualifiedToolName(
								Utils.hasText(toolAnnotation.name()) ? toolAnnotation.name() : mcpToolMethod.getName());

						String toolDescrption = toolAnnotation.description();

						String inputSchema = generateInputSchema(mcpToolMethod);

						var toolBuilder = McpSchema.Tool.builder().name(toolName).description(toolDescrption)
								.inputSchema(inputSchema);

						// Tool annotations
						if (toolAnnotation.annotations() != null) {
							var toolAnnotations = toolAnnotation.annotations();
							toolBuilder.annotations(new McpSchema.ToolAnnotations(toolAnnotations.title(),
									toolAnnotations.readOnlyHint(), toolAnnotations.destructiveHint(),
									toolAnnotations.idempotentHint(), toolAnnotations.openWorldHint(), null));
						}

						ReactiveUtils.isReactiveReturnTypeOfCallToolResult(mcpToolMethod);

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
				logger.warn("No toolservice methods found in tool service object: {}", getServiceObject());
			}
			this.syncToolSpecifications = toolServiceSpecs;
		}
		return this.syncToolSpecifications;
	}

}
