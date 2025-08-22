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

package org.springaicommunity.mcp.provider.resource;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpResource;
import org.springaicommunity.mcp.method.resource.AsyncStatelessMcpResourceMethodCallback;

import io.modelcontextprotocol.server.McpStatelessServerFeatures.AsyncResourceSpecification;
import io.modelcontextprotocol.server.McpTransportContext;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Provider for asynchronous stateless MCP resource methods.
 *
 * This provider creates resource specifications for methods annotated with
 * {@link McpResource} that are designed to work in a stateless manner using
 * {@link McpTransportContext} and return reactive types.
 *
 * @author Christian Tzolov
 */
public class AsyncStatelessMcpResourceProvider {

	private static final Logger logger = LoggerFactory.getLogger(AsyncStatelessMcpResourceProvider.class);

	private final List<Object> resourceObjects;

	/**
	 * Create a new AsyncStatelessMcpResourceProvider.
	 * @param resourceObjects the objects containing methods annotated with
	 * {@link McpResource}
	 */
	public AsyncStatelessMcpResourceProvider(List<Object> resourceObjects) {
		Assert.notNull(resourceObjects, "resourceObjects cannot be null");
		this.resourceObjects = resourceObjects;
	}

	/**
	 * Get the async stateless resource specifications.
	 * @return the list of async stateless resource specifications
	 */
	public List<AsyncResourceSpecification> getResourceSpecifications() {

		List<AsyncResourceSpecification> resourceSpecs = this.resourceObjects.stream()
			.map(resourceObject -> Stream.of(doGetClassMethods(resourceObject))
				.filter(method -> method.isAnnotationPresent(McpResource.class))
				.filter(method -> Mono.class.isAssignableFrom(method.getReturnType())
						|| Flux.class.isAssignableFrom(method.getReturnType())
						|| Publisher.class.isAssignableFrom(method.getReturnType()))
				.map(mcpResourceMethod -> {

					var resourceAnnotation = doGetMcpResourceAnnotation(mcpResourceMethod);

					var uri = resourceAnnotation.uri();
					var name = getName(mcpResourceMethod, resourceAnnotation);
					var description = resourceAnnotation.description();
					var mimeType = resourceAnnotation.mimeType();

					var mcpResource = McpSchema.Resource.builder()
						.uri(uri)
						.name(name)
						.description(description)
						.mimeType(mimeType)
						.build();

					BiFunction<McpTransportContext, ReadResourceRequest, Mono<ReadResourceResult>> methodCallback = AsyncStatelessMcpResourceMethodCallback
						.builder()
						.method(mcpResourceMethod)
						.bean(resourceObject)
						.resource(mcpResource)
						.build();

					var resourceSpec = new AsyncResourceSpecification(mcpResource, methodCallback);

					return resourceSpec;
				})
				.toList())
			.flatMap(List::stream)
			.toList();

		if (resourceSpecs.isEmpty()) {
			logger.warn("No resource methods found in the provided resource objects: {}", this.resourceObjects);
		}

		return resourceSpecs;
	}

	protected Method[] doGetClassMethods(Object bean) {
		return bean.getClass().getDeclaredMethods();
	}

	protected McpResource doGetMcpResourceAnnotation(Method method) {
		return method.getAnnotation(McpResource.class);
	}

	private static String getName(Method method, McpResource resource) {
		Assert.notNull(method, "method cannot be null");
		if (resource == null || resource.name() == null || resource.name().isEmpty()) {
			return method.getName();
		}
		return resource.name();
	}

}
