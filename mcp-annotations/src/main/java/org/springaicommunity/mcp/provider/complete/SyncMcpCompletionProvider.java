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

package org.springaicommunity.mcp.provider.complete;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

import org.springaicommunity.mcp.annotation.CompleteAdapter;
import org.springaicommunity.mcp.annotation.McpComplete;
import org.springaicommunity.mcp.method.complete.SyncMcpCompleteMethodCallback;

import io.modelcontextprotocol.server.McpServerFeatures.SyncCompletionSpecification;
import io.modelcontextprotocol.util.Assert;
import reactor.core.publisher.Mono;

/**
 */
public class SyncMcpCompletionProvider {

	private final List<Object> completeObjects;

	public SyncMcpCompletionProvider(List<Object> completeObjects) {
		Assert.notNull(completeObjects, "completeObjects cannot be null");
		this.completeObjects = completeObjects;
	}

	public List<SyncCompletionSpecification> getCompleteSpecifications() {

		List<SyncCompletionSpecification> syncCompleteSpecification = this.completeObjects.stream()
			.map(completeObject -> Stream.of(doGetClassMethods(completeObject))
				.filter(method -> method.isAnnotationPresent(McpComplete.class))
				.filter(method -> !Mono.class.isAssignableFrom(method.getReturnType()))
				.map(mcpCompleteMethod -> {
					var completeAnnotation = mcpCompleteMethod.getAnnotation(McpComplete.class);
					var completeRef = CompleteAdapter.asCompleteReference(completeAnnotation, mcpCompleteMethod);

					var methodCallback = SyncMcpCompleteMethodCallback.builder()
						.method(mcpCompleteMethod)
						.bean(completeObject)
						.reference(completeRef)
						.build();

					return new SyncCompletionSpecification(completeRef, methodCallback);
				})
				.toList())
			.flatMap(List::stream)
			.toList();

		return syncCompleteSpecification;
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
