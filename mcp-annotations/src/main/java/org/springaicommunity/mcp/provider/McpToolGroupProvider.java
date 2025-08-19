package bndtools.demo.mcpserver;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.method.tool.utils.JsonSchemaGenerator;

import io.modelcontextprotocol.util.Assert;

public class McpToolGroupProvider {

	protected static final Logger logger = LoggerFactory.getLogger(McpToolGroupProvider.class);

	private final Object serviceObject;
	private final Class<?> toolGroup;

	public McpToolGroupProvider(Object serviceObject, Class<?> toolGroup) {
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

}
