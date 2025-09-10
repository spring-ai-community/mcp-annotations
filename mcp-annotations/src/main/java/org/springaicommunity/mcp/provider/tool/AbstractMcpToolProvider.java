package org.springaicommunity.mcp.provider.tool;

import java.lang.reflect.Method;
import java.util.List;

import io.modelcontextprotocol.util.Assert;
import org.springaicommunity.mcp.annotation.McpTool;

public abstract class AbstractMcpToolProvider {

	protected final List<Object> toolObjects;

	public AbstractMcpToolProvider(List<Object> toolObjects) {
		Assert.notNull(toolObjects, "toolObjects cannot be null");
		this.toolObjects = toolObjects;
	}

	protected Method[] doGetClassMethods(Object bean) {
		return bean.getClass().getDeclaredMethods();
	}

	protected McpTool doGetMcpToolAnnotation(Method method) {
		return method.getAnnotation(McpTool.class);
	}

	protected Class<? extends Throwable> doGetToolCallException() {
		return Exception.class;
	}

}
