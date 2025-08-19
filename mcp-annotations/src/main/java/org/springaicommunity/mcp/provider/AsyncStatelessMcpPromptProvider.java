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

package org.springaicommunity.mcp.provider;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpPrompt;
import org.springaicommunity.mcp.annotation.PromptAdaptor;
import org.springaicommunity.mcp.method.prompt.AsyncStatelessMcpPromptMethodCallback;

import io.modelcontextprotocol.server.McpStatelessServerFeatures.AsyncPromptSpecification;
import io.modelcontextprotocol.server.McpTransportContext;
import io.modelcontextprotocol.spec.McpSchema.GetPromptRequest;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Provider for asynchronous stateless MCP prompt methods.
 *
 * This provider creates prompt specifications for methods annotated with
 * {@link McpPrompt} that are designed to work in a stateless manner using
 * {@link McpTransportContext} and return reactive types.
 *
 * @author Christian Tzolov
 */
public class AsyncStatelessMcpPromptProvider {

	private static final Logger logger = LoggerFactory.getLogger(AsyncStatelessMcpPromptProvider.class);

	private final List<Object> promptObjects;

	/**
	 * Create a new AsyncStatelessMcpPromptProvider.
	 * @param promptObjects the objects containing methods annotated with
	 * {@link McpPrompt}
	 */
	public AsyncStatelessMcpPromptProvider(List<Object> promptObjects) {
		Assert.notNull(promptObjects, "promptObjects cannot be null");
		this.promptObjects = promptObjects;
	}

	/**
	 * Get the async stateless prompt specifications.
	 * @return the list of async stateless prompt specifications
	 */
	public List<AsyncPromptSpecification> getPromptSpecifications() {

		List<AsyncPromptSpecification> promptSpecs = this.promptObjects.stream()
			.map(promptObject -> Stream.of(doGetClassMethods(promptObject))
				.filter(method -> method.isAnnotationPresent(McpPrompt.class))
				.filter(method -> Mono.class.isAssignableFrom(method.getReturnType())
						|| Flux.class.isAssignableFrom(method.getReturnType())
						|| Publisher.class.isAssignableFrom(method.getReturnType()))
				.map(mcpPromptMethod -> {
					var promptAnnotation = mcpPromptMethod.getAnnotation(McpPrompt.class);
					var mcpPrompt = PromptAdaptor.asPrompt(promptAnnotation, mcpPromptMethod);

					BiFunction<McpTransportContext, GetPromptRequest, Mono<GetPromptResult>> methodCallback = AsyncStatelessMcpPromptMethodCallback
						.builder()
						.method(mcpPromptMethod)
						.bean(promptObject)
						.prompt(mcpPrompt)
						.build();

					return new AsyncPromptSpecification(mcpPrompt, methodCallback);
				})
				.toList())
			.flatMap(List::stream)
			.toList();

		if (promptSpecs.isEmpty()) {
			logger.warn("No prompt methods found in the provided prompt objects: {}", this.promptObjects);
		}

		return promptSpecs;
	}

	/**
	 * Returns the methods of the given bean class.
	 * @param bean the bean instance
	 * @return the methods of the bean class
	 */
	protected Method[] doGetClassMethods(Object bean) {
		return bean.getClass().getDeclaredMethods();
	}

}
