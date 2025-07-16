/*
* Copyright 2025 - 2025 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* https://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.springaicommunity.mcp.spring;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;

import io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import reactor.core.publisher.Mono;

import org.springaicommunity.mcp.provider.AsyncMcpLoggingConsumerProvider;
import org.springaicommunity.mcp.provider.AsyncMcpSamplingProvider;
import org.springframework.aop.support.AopUtils;
import org.springframework.util.ReflectionUtils;

/**
 * @author Christian Tzolov
 */
public class AsyncMcpAnnotationProvider {

	private static class SpringAiAsyncMcpLoggingConsumerProvider extends AsyncMcpLoggingConsumerProvider {

		public SpringAiAsyncMcpLoggingConsumerProvider(List<Object> loggingObjects) {
			super(loggingObjects);
		}

		@Override
		protected Method[] doGetClassMethods(Object bean) {
			return ReflectionUtils
				.getDeclaredMethods(AopUtils.isAopProxy(bean) ? AopUtils.getTargetClass(bean) : bean.getClass());
		}

	}

	private static class SpringAiAsyncMcpSamplingProvider extends AsyncMcpSamplingProvider {

		public SpringAiAsyncMcpSamplingProvider(List<Object> samplingObjects) {
			super(samplingObjects);
		}

		@Override
		protected Method[] doGetClassMethods(Object bean) {
			return ReflectionUtils
				.getDeclaredMethods(AopUtils.isAopProxy(bean) ? AopUtils.getTargetClass(bean) : bean.getClass());
		}

	}

	public static List<Function<LoggingMessageNotification, Mono<Void>>> createAsyncLoggingConsumers(
			List<Object> loggingObjects) {
		return new SpringAiAsyncMcpLoggingConsumerProvider(loggingObjects).getLoggingConsumers();
	}

	public static Function<CreateMessageRequest, Mono<CreateMessageResult>> createAsyncSamplingHandler(
			List<Object> samplingObjects) {
		return new SpringAiAsyncMcpSamplingProvider(samplingObjects).getSamplingHandler();
	}

}
