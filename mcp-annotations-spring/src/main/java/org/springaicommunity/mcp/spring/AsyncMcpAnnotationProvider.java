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

import org.springaicommunity.mcp.provider.AsyncMcpElicitationProvider;
import org.springaicommunity.mcp.provider.AsyncMcpLoggingConsumerProvider;
import org.springaicommunity.mcp.provider.AsyncMcpSamplingProvider;
import org.springaicommunity.mcp.provider.AsyncMcpToolProvider;
import org.springaicommunity.mcp.provider.AsyncStatelessMcpPromptProvider;
import org.springaicommunity.mcp.provider.AsyncStatelessMcpResourceProvider;
import org.springaicommunity.mcp.provider.AsyncStatelessMcpToolProvider;

import io.modelcontextprotocol.server.McpServerFeatures.AsyncToolSpecification;
import io.modelcontextprotocol.server.McpStatelessServerFeatures;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import io.modelcontextprotocol.spec.McpSchema.ElicitRequest;
import io.modelcontextprotocol.spec.McpSchema.ElicitResult;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import reactor.core.publisher.Mono;

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
			return AnnotationProviderUtil.beanMethods(bean);
		}

	}

	private static class SpringAiAsyncMcpSamplingProvider extends AsyncMcpSamplingProvider {

		public SpringAiAsyncMcpSamplingProvider(List<Object> samplingObjects) {
			super(samplingObjects);
		}

		@Override
		protected Method[] doGetClassMethods(Object bean) {
			return AnnotationProviderUtil.beanMethods(bean);
		}

	}

	private static class SpringAiAsyncMcpElicitationProvider extends AsyncMcpElicitationProvider {

		public SpringAiAsyncMcpElicitationProvider(List<Object> elicitationObjects) {
			super(elicitationObjects);
		}

		@Override
		protected Method[] doGetClassMethods(Object bean) {
			return AnnotationProviderUtil.beanMethods(bean);
		}

	}

	private static class SpringAiAsyncMcpToolProvider extends AsyncMcpToolProvider {

		public SpringAiAsyncMcpToolProvider(List<Object> toolObjects) {
			super(toolObjects);
		}

		@Override
		protected Method[] doGetClassMethods(Object bean) {
			return AnnotationProviderUtil.beanMethods(bean);
		}

	}

	private static class SpringAiAsyncStatelessMcpToolProvider extends AsyncStatelessMcpToolProvider {

		public SpringAiAsyncStatelessMcpToolProvider(List<Object> toolObjects) {
			super(toolObjects);
		}

		@Override
		protected Method[] doGetClassMethods(Object bean) {
			return AnnotationProviderUtil.beanMethods(bean);
		}

	}

	private static class SpringAiAsyncStatelessPromptProvider extends AsyncStatelessMcpPromptProvider {

		public SpringAiAsyncStatelessPromptProvider(List<Object> promptObjects) {
			super(promptObjects);
		}

		@Override
		protected Method[] doGetClassMethods(Object bean) {
			return AnnotationProviderUtil.beanMethods(bean);
		}

	}

	private static class SpringAiAsyncStatelessResourceProvider extends AsyncStatelessMcpResourceProvider {

		public SpringAiAsyncStatelessResourceProvider(List<Object> resourceObjects) {
			super(resourceObjects);
		}

		@Override
		protected Method[] doGetClassMethods(Object bean) {
			return AnnotationProviderUtil.beanMethods(bean);
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

	public static Function<ElicitRequest, Mono<ElicitResult>> createAsyncElicitationHandler(
			List<Object> elicitationObjects) {
		return new SpringAiAsyncMcpElicitationProvider(elicitationObjects).getElicitationHandler();
	}

	public static List<AsyncToolSpecification> createAsyncToolSpecifications(List<Object> toolObjects) {
		return new SpringAiAsyncMcpToolProvider(toolObjects).getToolSpecifications();
	}

	public static List<McpStatelessServerFeatures.AsyncToolSpecification> createAsyncStatelessToolSpecifications(
			List<Object> toolObjects) {
		return new SpringAiAsyncStatelessMcpToolProvider(toolObjects).getToolSpecifications();
	}

	public static List<McpStatelessServerFeatures.AsyncPromptSpecification> createAsyncStatelessPromptSpecifications(
			List<Object> promptObjects) {
		return new SpringAiAsyncStatelessPromptProvider(promptObjects).getPromptSpecifications();
	}

	public static List<McpStatelessServerFeatures.AsyncResourceSpecification> createAsyncStatelessResourceSpecifications(
			List<Object> resourceObjects) {
		return new SpringAiAsyncStatelessResourceProvider(resourceObjects).getResourceSpecifications();
	}

}
