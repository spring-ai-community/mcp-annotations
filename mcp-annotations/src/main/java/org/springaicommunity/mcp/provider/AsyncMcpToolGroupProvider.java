package org.springaicommunity.mcp.provider;

import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import org.reactivestreams.Publisher;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.method.tool.AsyncMcpToolMethodCallback;
import org.springaicommunity.mcp.method.tool.ReactiveUtils;
import org.springaicommunity.mcp.method.tool.ReturnMode;
import org.springaicommunity.mcp.method.tool.utils.ClassUtils;

import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.server.McpServerFeatures.AsyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.util.Utils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class AsyncMcpToolGroupProvider extends McpToolGroupProvider {

	private List<AsyncToolSpecification> asyncToolSpecifications;

	public AsyncMcpToolGroupProvider(Object serviceObject, Class<?> toolGroup) {
		super(serviceObject, toolGroup);
	}

	public List<AsyncToolSpecification> getAsyncToolSpecifications() {
		if (this.asyncToolSpecifications == null) {

			List<AsyncToolSpecification> toolServiceSpecs = Arrays.asList(getToolGroup().getMethods()).stream()
					.filter(method -> method.isAnnotationPresent(McpTool.class))
					.filter(method -> Mono.class.isAssignableFrom(method.getReturnType())
							|| Flux.class.isAssignableFrom(method.getReturnType())
							|| Publisher.class.isAssignableFrom(method.getReturnType()))
					.map(mcpToolMethod -> {

						var toolAnnotation = mcpToolMethod.getAnnotation(McpTool.class);

						String toolName = Utils.hasText(toolAnnotation.name()) ? toolAnnotation.name()
								: mcpToolMethod.getName();

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

						if (toolAnnotation.generateOutputSchema()
								&& !ReactiveUtils.isReactiveReturnTypeOfVoid(mcpToolMethod)
								&& !ReactiveUtils.isReactiveReturnTypeOfCallToolResult(mcpToolMethod)) {

							ReactiveUtils.getReactiveReturnTypeArgument(mcpToolMethod).ifPresent(typeArgument -> {
								Class<?> methodReturnType = typeArgument instanceof Class<?> ? (Class<?>) typeArgument
										: null;
								if (!ClassUtils.isPrimitiveOrWrapper(methodReturnType)
										&& !ClassUtils.isSimpleValueType(methodReturnType)) {
									toolBuilder.outputSchema(generateOutputSchema((Class<?>) typeArgument));
								}
							});
						}
						var tool = toolBuilder.build();

						ReturnMode returnMode = tool.outputSchema() != null ? ReturnMode.STRUCTURED
								: ReactiveUtils.isReactiveReturnTypeOfVoid(mcpToolMethod) ? ReturnMode.VOID
										: ReturnMode.TEXT;

						BiFunction<McpAsyncServerExchange, CallToolRequest, Mono<CallToolResult>> methodCallback = new AsyncMcpToolMethodCallback(
								returnMode, mcpToolMethod, getServiceObject());

						AsyncToolSpecification toolSpec = AsyncToolSpecification.builder().tool(tool)
								.callHandler(methodCallback).build();

						return toolSpec;
					}).toList();

			if (toolServiceSpecs.isEmpty()) {
				logger.warn("No toolservice methods found in tool service object: {}", getServiceObject());
			}
			this.asyncToolSpecifications = toolServiceSpecs;
		}
		return this.asyncToolSpecifications;
	}

}
