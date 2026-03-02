package org.springaicommunity.mcp.provider.tool;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.util.Assert;
import io.modelcontextprotocol.util.Utils;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.method.tool.utils.JsonParser;

/**
 * @author Christian Tzolov
 * @author Alexandros Pappas
 * @author Vadzim Shurmialiou
 * @author Craig Walls
 */
public abstract class AbstractMcpToolProvider {

	protected final List<Object> toolObjects;

	protected McpJsonMapper jsonMapper = McpJsonDefaults.getMapper();

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

	public void setJsonMapper(McpJsonMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
	}

	public McpJsonMapper getJsonMapper() {
		return this.jsonMapper;
	}

	@SuppressWarnings("unchecked")
	protected Map<String, Object> parseMeta(String metaJson) {
		if (!Utils.hasText(metaJson)) {
			return null;
		}
		return JsonParser.fromJson(metaJson, Map.class);
	}

}
