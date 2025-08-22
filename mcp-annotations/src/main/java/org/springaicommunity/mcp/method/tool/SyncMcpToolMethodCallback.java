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

package org.springaicommunity.mcp.method.tool;

import java.util.function.BiFunction;

import org.springaicommunity.mcp.annotation.McpTool;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;

/**
 * Class for creating Function callbacks around tool methods.
 *
 * This class provides a way to convert methods annotated with {@link McpTool} into
 * callback functions that can be used to handle tool requests.
 *
 * @author Christian Tzolov
 */
public final class SyncMcpToolMethodCallback extends AbstractSyncMcpToolMethodCallback<McpSyncServerExchange>
		implements BiFunction<McpSyncServerExchange, CallToolRequest, CallToolResult> {

	public SyncMcpToolMethodCallback(ReturnMode returnMode, java.lang.reflect.Method toolMethod, Object toolObject) {
		super(returnMode, toolMethod, toolObject);
	}

	@Override
	protected boolean isExchangeOrContextType(Class<?> paramType) {
		return McpSyncServerExchange.class.isAssignableFrom(paramType);
	}

	/**
	 * Apply the callback to the given request.
	 * <p>
	 * This method builds the arguments for the method call, invokes the method, and
	 * returns the result.
	 * @param exchange The server exchange context
	 * @param request The tool call request, must not be null
	 * @return The result of the method invocation
	 */
	@Override
	public CallToolResult apply(McpSyncServerExchange exchange, CallToolRequest request) {
		validateRequest(request);

		try {
			// Build arguments for the method call, passing the full request for
			// CallToolRequest parameter support
			Object[] args = this.buildMethodArguments(exchange, request.arguments(), request);

			// Invoke the method
			Object result = this.callMethod(args);

			// Return the processed result
			return this.processResult(result);
		}
		catch (Exception e) {
			return this.createErrorResult(e);
		}
	}

}
