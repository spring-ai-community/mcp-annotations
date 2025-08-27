package org.springaicommunity.mcp.provider.toolgroup;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.method.tool.AsyncStatelessMcpToolMethodCallback;
import org.springaicommunity.mcp.method.tool.ReactiveUtils;
import org.springaicommunity.mcp.method.tool.ReturnMode;
import org.springaicommunity.mcp.method.tool.utils.ClassUtils;
import org.springaicommunity.mcp.method.tool.utils.JsonSchemaGenerator;

import io.modelcontextprotocol.server.McpStatelessServerFeatures.AsyncToolSpecification;
import io.modelcontextprotocol.server.McpTransportContext;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.util.Assert;
import io.modelcontextprotocol.util.Utils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class AsyncStatelessMcpToolGroupProvider {

	protected static final Logger logger = LoggerFactory.getLogger(AsyncStatelessMcpToolGroupProvider.class);

	private final Object serviceObject;

	private final Class<?> toolGroup;

	public AsyncStatelessMcpToolGroupProvider(Object serviceObject, Class<?> toolGroup) {
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

	public List<AsyncToolSpecification> getToolSpecifications() {
		List<AsyncToolSpecification> toolGroupSpecs = Arrays.asList(doGetClassMethods()).stream()
				.filter(method -> method.isAnnotationPresent(McpTool.class))
				.filter(method -> Mono.class.isAssignableFrom(method.getReturnType())
						|| Flux.class.isAssignableFrom(method.getReturnType())
						|| Publisher.class.isAssignableFrom(method.getReturnType()))
				.map(mcpToolMethod -> {

					var toolAnnotation = doGetMcpToolAnnotation(mcpToolMethod);

					String toolName = createFullyQualifiedToolName(Utils.hasText(toolAnnotation.name()) ? toolAnnotation.name()
							: mcpToolMethod.getName());

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

					// Generate Output Schema from the method return type.
					// Output schema is not generated for primitive types, void,
					// CallToolResult, simple value types (String, etc.)
					// or if generateOutputSchema attribute is set to false.

					if (toolAnnotation.generateOutputSchema()
							&& !ReactiveUtils.isReactiveReturnTypeOfVoid(mcpToolMethod)
							&& !ReactiveUtils.isReactiveReturnTypeOfCallToolResult(mcpToolMethod)) {

						ReactiveUtils.getReactiveReturnTypeArgument(mcpToolMethod).ifPresent(typeArgument -> {
							Class<?> methodReturnType = typeArgument instanceof Class<?> ? (Class<?>) typeArgument
									: null;
							if (!ClassUtils.isPrimitiveOrWrapper(methodReturnType)
									&& !ClassUtils.isSimpleValueType(methodReturnType)) {
								toolBuilder
									.outputSchema(JsonSchemaGenerator.generateFromClass((Class<?>) typeArgument));
							}
						});
					}
					var tool = toolBuilder.build();

					ReturnMode returnMode = tool.outputSchema() != null ? ReturnMode.STRUCTURED
							: ReactiveUtils.isReactiveReturnTypeOfVoid(mcpToolMethod) ? ReturnMode.VOID
									: ReturnMode.TEXT;

					BiFunction<McpTransportContext, CallToolRequest, Mono<CallToolResult>> methodCallback = new AsyncStatelessMcpToolMethodCallback(
							returnMode, mcpToolMethod, getServiceObject());

					AsyncToolSpecification toolSpec = AsyncToolSpecification.builder()
						.tool(tool)
						.callHandler(methodCallback)
						.build();

					if (logger.isDebugEnabled()) {
						logger.debug("created async toolspec={}", toolSpec);
					}

					return toolSpec;
				}).toList();

		if (toolGroupSpecs.isEmpty()) {
			logger.warn("No async toolgroup methods found in service object: {}", getServiceObject());
		}
		return toolGroupSpecs;
	}

}
