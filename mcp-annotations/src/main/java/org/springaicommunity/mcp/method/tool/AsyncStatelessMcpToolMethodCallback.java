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

import io.modelcontextprotocol.server.McpTransportContext;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import reactor.core.publisher.Mono;

/**
 * Class for creating Function callbacks around async stateless tool methods.
 *
 * This class provides a way to convert methods annotated with {@link McpTool} into
 * callback functions that can be used to handle tool requests asynchronously in a
 * stateless manner using McpTransportContext.
 *
 * @author Christian Tzolov
 */
public final class AsyncStatelessMcpToolMethodCallback extends AbstractAsyncMcpToolMethodCallback<McpTransportContext>
		implements BiFunction<McpTransportContext, CallToolRequest, Mono<CallToolResult>> {

	public AsyncStatelessMcpToolMethodCallback(ReturnMode returnMode, java.lang.reflect.Method toolMethod,
			Object toolObject) {
		super(returnMode, toolMethod, toolObject);
	}

	@Override
	protected boolean isExchangeOrContextType(Class<?> paramType) {
		return McpTransportContext.class.isAssignableFrom(paramType);
	}

	/**
	 * Apply the callback to the given request.
	 * <p>
	 * This method builds the arguments for the method call, invokes the method, and
	 * returns the result asynchronously.
	 * @param mcpTransportContext The transport context
	 * @param request The tool call request, must not be null
	 * @return A Mono containing the result of the method invocation
	 */
	@Override
	public Mono<CallToolResult> apply(McpTransportContext mcpTransportContext, CallToolRequest request) {

		return validateRequest(request).then(Mono.defer(() -> {
			try {
				// Build arguments for the method call
				Object[] args = this.buildMethodArguments(mcpTransportContext, request.arguments(), request);

				// Invoke the method
				Object result = this.callMethod(args);

				// Handle reactive types - method return types should always be reactive
				return this.convertToCallToolResult(result);

			}
			catch (Exception e) {
				return this.createErrorResult(e);
			}
		}));
	}

}
