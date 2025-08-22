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
import java.util.stream.Stream;

import org.springaicommunity.mcp.annotation.McpResource;
import org.springaicommunity.mcp.method.resource.SyncMcpResourceMethodCallback;

import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.util.Assert;
import reactor.core.publisher.Mono;

/**
 */
public class SyncMcpResourceProvider {

	private final List<Object> resourceObjects;

	public SyncMcpResourceProvider(List<Object> resourceObjects) {
		Assert.notNull(resourceObjects, "resourceObjects cannot be null");
		this.resourceObjects = resourceObjects;
	}

	public List<SyncResourceSpecification> getResourceSpecifications() {

		List<SyncResourceSpecification> methodCallbacks = this.resourceObjects.stream()
			.map(resourceObject -> Stream.of(this.doGetClassMethods(resourceObject))
				.filter(resourceMethod -> resourceMethod.isAnnotationPresent(McpResource.class))
				.filter(method -> !Mono.class.isAssignableFrom(method.getReturnType()))
				.map(mcpResourceMethod -> {
					var resourceAnnotation = mcpResourceMethod.getAnnotation(McpResource.class);

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

					var methodCallback = SyncMcpResourceMethodCallback.builder()
						.method(mcpResourceMethod)
						.bean(resourceObject)
						.resource(mcpResource)
						.build();

					return new SyncResourceSpecification(mcpResource, methodCallback);
				})
				.toList())
			.flatMap(List::stream)
			.toList();

		return methodCallbacks;
	}

	/**
	 * Returns the methods of the given bean class.
	 * @param bean the bean instance
	 * @return the methods of the bean class
	 */
	protected Method[] doGetClassMethods(Object bean) {
		return bean.getClass().getDeclaredMethods();
	}

	private static String getName(Method method, McpResource resource) {
		Assert.notNull(method, "method cannot be null");
		if (resource == null || resource.name() == null || resource.name().isEmpty()) {
			return method.getName();
		}
		return resource.name();
	}

}
