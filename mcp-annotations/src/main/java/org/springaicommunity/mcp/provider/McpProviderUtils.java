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
import java.util.function.Predicate;
import java.util.regex.Pattern;

import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.context.McpAsyncRequestContext;
import org.springaicommunity.mcp.context.McpSyncRequestContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class McpProviderUtils {

	private static final Logger logger = LoggerFactory.getLogger(McpProviderUtils.class);

	private static final Pattern URI_VARIABLE_PATTERN = Pattern.compile("\\{([^/]+?)\\}");

	public static boolean isUriTemplate(String uri) {
		return URI_VARIABLE_PATTERN.matcher(uri).find();
	}

	public final static Predicate<Method> isReactiveReturnType = method -> Mono.class
		.isAssignableFrom(method.getReturnType()) || Flux.class.isAssignableFrom(method.getReturnType())
			|| Publisher.class.isAssignableFrom(method.getReturnType());

	public final static Predicate<Method> isNotReactiveReturnType = method -> !Mono.class
		.isAssignableFrom(method.getReturnType()) && !Flux.class.isAssignableFrom(method.getReturnType())
			&& !Publisher.class.isAssignableFrom(method.getReturnType());

	public static Predicate<Method> filterNonReactiveReturnTypeMethod() {
		return method -> {
			if (isReactiveReturnType.test(method)) {
				return true;
			}
			logger.warn(
					"Sync providers doesn't support reactive return types. Skipping method {} with reactive return type {}",
					method, method.getReturnType());
			return false;
		};
	}

	public static Predicate<Method> filterReactiveReturnTypeMethod() {
		return method -> {
			if (isNotReactiveReturnType.test(method)) {
				return true;
			}
			logger.warn(
					"Sync providers doesn't support reactive return types. Skipping method {} with reactive return type {}",
					method, method.getReturnType());
			return false;
		};
	}

	private static boolean hasBidirectionalParameters(Method method) {

		for (Class<?> paramType : method.getParameterTypes()) {
			if (McpSyncRequestContext.class.isAssignableFrom(paramType)
					|| McpAsyncRequestContext.class.isAssignableFrom(paramType)
					|| McpSyncServerExchange.class.isAssignableFrom(paramType)
					|| McpAsyncServerExchange.class.isAssignableFrom(paramType)) {

				return true;
			}
		}

		return false;
	}

	public static Predicate<Method> filterMethodWithBidirectionalParameters() {
		return method -> {
			if (!hasBidirectionalParameters(method)) {
				return true;
			}
			logger.warn(
					"Stateless servers doesn't support bidirectional parameters. Skipping method {} with bidirectional parameters",
					method);
			return false;
		};
	}

}
