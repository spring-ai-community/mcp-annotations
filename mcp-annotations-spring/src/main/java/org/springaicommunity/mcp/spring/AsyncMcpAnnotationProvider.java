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

import org.springaicommunity.mcp.method.changed.resource.AsyncResourceListChangedSpecification;
import org.springaicommunity.mcp.method.changed.tool.AsyncToolListChangedSpecification;
import org.springaicommunity.mcp.method.elicitation.AsyncElicitationSpecification;
import org.springaicommunity.mcp.method.logging.AsyncLoggingSpecification;
import org.springaicommunity.mcp.method.progress.AsyncProgressSpecification;
import org.springaicommunity.mcp.method.sampling.AsyncSamplingSpecification;
import org.springaicommunity.mcp.provider.changed.resource.AsyncMcpResourceListChangedProvider;
import org.springaicommunity.mcp.provider.changed.tool.AsyncMcpToolListChangedProvider;
import org.springaicommunity.mcp.provider.elicitation.AsyncMcpElicitationProvider;
import org.springaicommunity.mcp.provider.logging.AsyncMcpLoggingProvider;
import org.springaicommunity.mcp.provider.progress.AsyncMcpProgressProvider;
import org.springaicommunity.mcp.provider.prompt.AsyncStatelessMcpPromptProvider;
import org.springaicommunity.mcp.provider.resource.AsyncStatelessMcpResourceProvider;
import org.springaicommunity.mcp.provider.sampling.AsyncMcpSamplingProvider;
import org.springaicommunity.mcp.provider.tool.AsyncMcpToolProvider;
import org.springaicommunity.mcp.provider.tool.AsyncStatelessMcpToolProvider;

import io.modelcontextprotocol.server.McpServerFeatures.AsyncToolSpecification;
import io.modelcontextprotocol.server.McpStatelessServerFeatures;

/**
 * @author Christian Tzolov
 */
public class AsyncMcpAnnotationProvider {

	private static class SpringAiAsyncMcpLoggingProvider extends AsyncMcpLoggingProvider {

		public SpringAiAsyncMcpLoggingProvider(List<Object> loggingObjects) {
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

	private static class SpringAiAsyncMcpProgressProvider extends AsyncMcpProgressProvider {

		public SpringAiAsyncMcpProgressProvider(List<Object> progressObjects) {
			super(progressObjects);
		}

		@Override
		protected Method[] doGetClassMethods(Object bean) {
			return AnnotationProviderUtil.beanMethods(bean);
		}

	}

	private static class SpringAiAsyncMcpToolListChangedProvider extends AsyncMcpToolListChangedProvider {

		public SpringAiAsyncMcpToolListChangedProvider(List<Object> toolListChangedObjects) {
			super(toolListChangedObjects);
		}

		@Override
		protected Method[] doGetClassMethods(Object bean) {
			return AnnotationProviderUtil.beanMethods(bean);
		}

	}

	private static class SpringAiAsyncMcpResourceListChangedProvider extends AsyncMcpResourceListChangedProvider {

		public SpringAiAsyncMcpResourceListChangedProvider(List<Object> resourceListChangedObjects) {
			super(resourceListChangedObjects);
		}

		@Override
		protected Method[] doGetClassMethods(Object bean) {
			return AnnotationProviderUtil.beanMethods(bean);
		}

	}

	public static List<AsyncLoggingSpecification> createAsyncLoggingSpecifications(List<Object> loggingObjects) {
		return new SpringAiAsyncMcpLoggingProvider(loggingObjects).getLoggingSpecifications();
	}

	public static List<AsyncSamplingSpecification> createAsyncSamplingSpecifications(List<Object> samplingObjects) {
		return new SpringAiAsyncMcpSamplingProvider(samplingObjects).getSamplingSpecifictions();
	}

	public static List<AsyncElicitationSpecification> createAsyncElicitationSpecifications(
			List<Object> elicitationObjects) {
		return new SpringAiAsyncMcpElicitationProvider(elicitationObjects).getElicitationSpecifications();
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

	public static List<AsyncProgressSpecification> createAsyncProgressSpecifications(List<Object> progressObjects) {
		return new SpringAiAsyncMcpProgressProvider(progressObjects).getProgressSpecifications();
	}

	public static List<AsyncToolListChangedSpecification> createAsyncToolListChangedSpecifications(
			List<Object> toolListChangedObjects) {
		return new SpringAiAsyncMcpToolListChangedProvider(toolListChangedObjects).getToolListChangedSpecifications();
	}

	public static List<AsyncResourceListChangedSpecification> createAsyncResourceListChangedSpecifications(
			List<Object> resourceListChangedObjects) {
		return new SpringAiAsyncMcpResourceListChangedProvider(resourceListChangedObjects)
			.getResourceListChangedSpecifications();
	}

}
