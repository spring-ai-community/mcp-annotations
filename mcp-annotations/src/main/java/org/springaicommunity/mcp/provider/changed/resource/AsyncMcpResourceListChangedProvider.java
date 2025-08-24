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

package org.springaicommunity.mcp.provider.changed.resource;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import org.springaicommunity.mcp.annotation.McpResourceListChanged;
import org.springaicommunity.mcp.method.changed.resource.AsyncResourceListChangedSpecification;
import org.springaicommunity.mcp.method.changed.resource.AsyncMcpResourceListChangedMethodCallback;

import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.util.Assert;
import reactor.core.publisher.Mono;

/**
 * Provider for asynchronous resource list changed consumer callbacks.
 *
 * <p>
 * This class scans a list of objects for methods annotated with
 * {@link McpResourceListChanged} and creates {@link Function} callbacks for them. These
 * callbacks can be used to handle resource list change notifications from MCP servers in
 * a reactive way.
 *
 * <p>
 * Example usage: <pre>{@code
 * // Create a provider with a list of objects containing @McpResourceListChanged methods
 * AsyncMcpResourceListChangedProvider provider = new AsyncMcpResourceListChangedProvider(List.of(resourceListHandler));
 *
 * // Get the list of resource list changed consumer callbacks
 * List<AsyncResourceListChangedSpecification> specifications = provider.getResourceListChangedSpecifications();
 *
 * // Add the consumers to the client features
 * McpClientFeatures.Async clientFeatures = new McpClientFeatures.Async(
 *     clientInfo, clientCapabilities, roots,
 *     toolsChangeConsumers, resourcesChangeConsumers, promptsChangeConsumers,
 *     loggingConsumers, samplingHandler);
 * }</pre>
 *
 * @author Christian Tzolov
 * @see McpResourceListChanged
 * @see AsyncMcpResourceListChangedMethodCallback
 * @see AsyncResourceListChangedSpecification
 */
public class AsyncMcpResourceListChangedProvider {

	private final List<Object> resourceListChangedConsumerObjects;

	/**
	 * Create a new AsyncMcpResourceListChangedProvider.
	 * @param resourceListChangedConsumerObjects the objects containing methods annotated
	 * with {@link McpResourceListChanged}
	 */
	public AsyncMcpResourceListChangedProvider(List<Object> resourceListChangedConsumerObjects) {
		Assert.notNull(resourceListChangedConsumerObjects, "resourceListChangedConsumerObjects cannot be null");
		this.resourceListChangedConsumerObjects = resourceListChangedConsumerObjects;
	}

	/**
	 * Get the list of resource list changed consumer specifications.
	 * @return the list of resource list changed consumer specifications
	 */
	public List<AsyncResourceListChangedSpecification> getResourceListChangedSpecifications() {

		List<AsyncResourceListChangedSpecification> resourceListChangedConsumers = this.resourceListChangedConsumerObjects
			.stream()
			.map(consumerObject -> Stream.of(doGetClassMethods(consumerObject))
				.filter(method -> method.isAnnotationPresent(McpResourceListChanged.class))
				.filter(method -> method.getReturnType() == void.class
						|| Mono.class.isAssignableFrom(method.getReturnType()))
				.map(mcpResourceListChangedConsumerMethod -> {
					var resourceListChangedAnnotation = mcpResourceListChangedConsumerMethod
						.getAnnotation(McpResourceListChanged.class);

					Function<List<McpSchema.Resource>, Mono<Void>> methodCallback = AsyncMcpResourceListChangedMethodCallback
						.builder()
						.method(mcpResourceListChangedConsumerMethod)
						.bean(consumerObject)
						.build();

					return new AsyncResourceListChangedSpecification(resourceListChangedAnnotation.clientId(),
							methodCallback);
				})
				.toList())
			.flatMap(List::stream)
			.toList();

		return resourceListChangedConsumers;
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
