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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.CompleteAdapter;
import org.springaicommunity.mcp.annotation.McpComplete;
import org.springaicommunity.mcp.method.complete.SyncStatelessMcpCompleteMethodCallback;

import io.modelcontextprotocol.server.McpStatelessServerFeatures.SyncCompletionSpecification;
import io.modelcontextprotocol.server.McpTransportContext;
import io.modelcontextprotocol.spec.McpSchema.CompleteRequest;
import io.modelcontextprotocol.spec.McpSchema.CompleteResult;
import io.modelcontextprotocol.util.Assert;
import reactor.core.publisher.Mono;

/**
 * Provider for synchronous stateless MCP complete methods.
 *
 * This provider creates completion specifications for methods annotated with
 * {@link McpComplete} that are designed to work in a stateless manner using
 * {@link McpTransportContext}.
 *
 * @author Christian Tzolov
 */
public class SyncStatelessMcpCompleteProvider {

	private static final Logger logger = LoggerFactory.getLogger(SyncStatelessMcpCompleteProvider.class);

	private final List<Object> completeObjects;

	/**
	 * Create a new SyncStatelessMcpCompleteProvider.
	 * @param completeObjects the objects containing methods annotated with
	 * {@link McpComplete}
	 */
	public SyncStatelessMcpCompleteProvider(List<Object> completeObjects) {
		Assert.notNull(completeObjects, "completeObjects cannot be null");
		this.completeObjects = completeObjects;
	}

	/**
	 * Get the stateless completion specifications.
	 * @return the list of stateless completion specifications
	 */
	public List<SyncCompletionSpecification> getCompleteSpecifications() {

		List<SyncCompletionSpecification> completeSpecs = this.completeObjects.stream()
			.map(completeObject -> Stream.of(doGetClassMethods(completeObject))
				.filter(method -> method.isAnnotationPresent(McpComplete.class))
				.filter(method -> !Mono.class.isAssignableFrom(method.getReturnType()))
				.map(mcpCompleteMethod -> {
					var completeAnnotation = mcpCompleteMethod.getAnnotation(McpComplete.class);
					var completeRef = CompleteAdapter.asCompleteReference(completeAnnotation, mcpCompleteMethod);

					BiFunction<McpTransportContext, CompleteRequest, CompleteResult> methodCallback = SyncStatelessMcpCompleteMethodCallback
						.builder()
						.method(mcpCompleteMethod)
						.bean(completeObject)
						.complete(completeAnnotation)
						.build();

					return new SyncCompletionSpecification(completeRef, methodCallback);
				})
				.toList())
			.flatMap(List::stream)
			.toList();

		if (completeSpecs.isEmpty()) {
			logger.warn("No complete methods found in the provided complete objects: {}", this.completeObjects);
		}

		return completeSpecs;
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
