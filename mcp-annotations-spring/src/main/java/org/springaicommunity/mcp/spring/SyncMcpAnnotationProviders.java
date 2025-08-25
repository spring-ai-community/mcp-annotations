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

import org.springaicommunity.mcp.method.changed.resource.SyncResourceListChangedSpecification;
import org.springaicommunity.mcp.method.changed.tool.SyncToolListChangedSpecification;
import org.springaicommunity.mcp.method.elicitation.SyncElicitationSpecification;
import org.springaicommunity.mcp.method.logging.SyncLoggingSpecification;
import org.springaicommunity.mcp.method.progress.SyncProgressSpecification;
import org.springaicommunity.mcp.method.sampling.SyncSamplingSpecification;
import org.springaicommunity.mcp.provider.changed.resource.SyncMcpResourceListChangedProvider;
import org.springaicommunity.mcp.provider.changed.tool.SyncMcpToolListChangedProvider;
import org.springaicommunity.mcp.provider.complete.SyncMcpCompletionProvider;
import org.springaicommunity.mcp.provider.elicitation.SyncMcpElicitationProvider;
import org.springaicommunity.mcp.provider.logging.SyncMcpLogginProvider;
import org.springaicommunity.mcp.provider.progress.SyncMcpProgressProvider;
import org.springaicommunity.mcp.provider.prompt.SyncMcpPromptProvider;
import org.springaicommunity.mcp.provider.prompt.SyncStatelessMcpPromptProvider;
import org.springaicommunity.mcp.provider.resource.SyncMcpResourceProvider;
import org.springaicommunity.mcp.provider.resource.SyncStatelessMcpResourceProvider;
import org.springaicommunity.mcp.provider.sampling.SyncMcpSamplingProvider;
import org.springaicommunity.mcp.provider.tool.SyncMcpToolProvider;
import org.springaicommunity.mcp.provider.tool.SyncStatelessMcpToolProvider;

import io.modelcontextprotocol.server.McpServerFeatures.SyncCompletionSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpStatelessServerFeatures;

/**
 * @author Christian Tzolov
 */
public class SyncMcpAnnotationProviders {

	private static class SpringAiSyncMcpCompletionProvider extends SyncMcpCompletionProvider {

		public SpringAiSyncMcpCompletionProvider(List<Object> completeObjects) {
			super(completeObjects);
		}

		@Override
		protected Method[] doGetClassMethods(Object bean) {
			return AnnotationProviderUtil.beanMethods(bean);
		}

	};

	private static class SpringAiSyncToolProvider extends SyncMcpToolProvider {

		public SpringAiSyncToolProvider(List<Object> toolObjects) {
			super(toolObjects);
		}

		@Override
		protected Method[] doGetClassMethods(Object bean) {
			return AnnotationProviderUtil.beanMethods(bean);
		}

	}

	private static class SpringAiSyncStatelessToolProvider extends SyncStatelessMcpToolProvider {

		public SpringAiSyncStatelessToolProvider(List<Object> toolObjects) {
			super(toolObjects);
		}

		@Override
		protected Method[] doGetClassMethods(Object bean) {
			return AnnotationProviderUtil.beanMethods(bean);
		}

	}

	private static class SpringAiSyncMcpPromptProvider extends SyncMcpPromptProvider {

		public SpringAiSyncMcpPromptProvider(List<Object> promptObjects) {
			super(promptObjects);
		}

		@Override
		protected Method[] doGetClassMethods(Object bean) {
			return AnnotationProviderUtil.beanMethods(bean);
		}

	};

	private static class SpringAiSyncStatelessPromptProvider extends SyncStatelessMcpPromptProvider {

		public SpringAiSyncStatelessPromptProvider(List<Object> promptObjects) {
			super(promptObjects);
		}

		@Override
		protected Method[] doGetClassMethods(Object bean) {
			return AnnotationProviderUtil.beanMethods(bean);
		}

	}

	private static class SpringAiSyncMcpResourceProvider extends SyncMcpResourceProvider {

		public SpringAiSyncMcpResourceProvider(List<Object> resourceObjects) {
			super(resourceObjects);
		}

		@Override
		protected Method[] doGetClassMethods(Object bean) {
			return AnnotationProviderUtil.beanMethods(bean);
		}

	}

	private static class SpringAiSyncStatelessResourceProvider extends SyncStatelessMcpResourceProvider {

		public SpringAiSyncStatelessResourceProvider(List<Object> resourceObjects) {
			super(resourceObjects);
		}

		@Override
		protected Method[] doGetClassMethods(Object bean) {
			return AnnotationProviderUtil.beanMethods(bean);
		}

	}

	private static class SpringAiSyncMcpLoggingProvider extends SyncMcpLogginProvider {

		public SpringAiSyncMcpLoggingProvider(List<Object> loggingObjects) {
			super(loggingObjects);
		}

		@Override
		protected Method[] doGetClassMethods(Object bean) {
			return AnnotationProviderUtil.beanMethods(bean);
		}

	}

	private static class SpringAiSyncMcpSamplingProvider extends SyncMcpSamplingProvider {

		public SpringAiSyncMcpSamplingProvider(List<Object> samplingObjects) {
			super(samplingObjects);
		}

		@Override
		protected Method[] doGetClassMethods(Object bean) {
			return AnnotationProviderUtil.beanMethods(bean);
		}

	}

	private static class SpringAiSyncMcpElicitationProvider extends SyncMcpElicitationProvider {

		public SpringAiSyncMcpElicitationProvider(List<Object> elicitationObjects) {
			super(elicitationObjects);
		}

		@Override
		protected Method[] doGetClassMethods(Object bean) {
			return AnnotationProviderUtil.beanMethods(bean);
		}

	}

	private static class SpringAiSyncMcpProgressProvider extends SyncMcpProgressProvider {

		public SpringAiSyncMcpProgressProvider(List<Object> progressObjects) {
			super(progressObjects);
		}

		@Override
		protected Method[] doGetClassMethods(Object bean) {
			return AnnotationProviderUtil.beanMethods(bean);
		}

	}

	private static class SpringAiSyncMcpToolListChangedProvider extends SyncMcpToolListChangedProvider {

		public SpringAiSyncMcpToolListChangedProvider(List<Object> toolListChangedObjects) {
			super(toolListChangedObjects);
		}

		@Override
		protected Method[] doGetClassMethods(Object bean) {
			return AnnotationProviderUtil.beanMethods(bean);
		}

	}

	private static class SpringAiSyncMcpResourceListChangedProvider extends SyncMcpResourceListChangedProvider {

		public SpringAiSyncMcpResourceListChangedProvider(List<Object> resourceListChangedObjects) {
			super(resourceListChangedObjects);
		}

		@Override
		protected Method[] doGetClassMethods(Object bean) {
			return AnnotationProviderUtil.beanMethods(bean);
		}

	}

	public static List<SyncToolSpecification> toolSpecifications(List<Object> toolObjects) {
		return new SpringAiSyncToolProvider(toolObjects).getToolSpecifications();
	}

	public static List<McpStatelessServerFeatures.SyncToolSpecification> statelessToolSpecifications(
			List<Object> toolObjects) {
		return new SpringAiSyncStatelessToolProvider(toolObjects).getToolSpecifications();
	}

	public static List<SyncCompletionSpecification> completeSpecifications(List<Object> completeObjects) {
		return new SpringAiSyncMcpCompletionProvider(completeObjects).getCompleteSpecifications();
	}

	public static List<SyncPromptSpecification> promptSpecifications(List<Object> promptObjects) {
		return new SpringAiSyncMcpPromptProvider(promptObjects).getPromptSpecifications();
	}

	public static List<McpStatelessServerFeatures.SyncPromptSpecification> statelessPromptSpecifications(
			List<Object> promptObjects) {
		return new SpringAiSyncStatelessPromptProvider(promptObjects).getPromptSpecifications();
	}

	public static List<SyncResourceSpecification> resourceSpecifications(List<Object> resourceObjects) {
		return new SpringAiSyncMcpResourceProvider(resourceObjects).getResourceSpecifications();
	}

	public static List<McpStatelessServerFeatures.SyncResourceSpecification> statelessResourceSpecifications(
			List<Object> resourceObjects) {
		return new SpringAiSyncStatelessResourceProvider(resourceObjects).getResourceSpecifications();
	}

	public static List<SyncLoggingSpecification> loggingSpecifications(List<Object> loggingObjects) {
		return new SpringAiSyncMcpLoggingProvider(loggingObjects).getLoggingSpecifications();
	}

	public static List<SyncSamplingSpecification> samplingSpecifications(List<Object> samplingObjects) {
		return new SpringAiSyncMcpSamplingProvider(samplingObjects).getSamplingSpecifications();
	}

	public static List<SyncElicitationSpecification> elicitationSpecifications(List<Object> elicitationObjects) {
		return new SpringAiSyncMcpElicitationProvider(elicitationObjects).getElicitationSpecifications();
	}

	public static List<SyncProgressSpecification> progressSpecifications(List<Object> progressObjects) {
		return new SpringAiSyncMcpProgressProvider(progressObjects).getProgressSpecifications();
	}

	public static List<SyncToolListChangedSpecification> toolListChangedSpecifications(
			List<Object> toolListChangedObjects) {
		return new SpringAiSyncMcpToolListChangedProvider(toolListChangedObjects).getToolListChangedSpecifications();
	}

	public static List<SyncResourceListChangedSpecification> resourceListChangedSpecifications(
			List<Object> resourceListChangedObjects) {
		return new SpringAiSyncMcpResourceListChangedProvider(resourceListChangedObjects)
			.getResourceListChangedSpecifications();
	}

}
