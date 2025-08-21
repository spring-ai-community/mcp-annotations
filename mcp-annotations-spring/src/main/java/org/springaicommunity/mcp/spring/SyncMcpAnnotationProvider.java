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

import org.springaicommunity.mcp.method.elicitation.SyncElicitationSpecification;
import org.springaicommunity.mcp.method.logging.SyncLoggingSpecification;
import org.springaicommunity.mcp.method.sampling.SyncSamplingSpecification;
import org.springaicommunity.mcp.provider.SyncMcpCompletionProvider;
import org.springaicommunity.mcp.provider.SyncMcpElicitationProvider;
import org.springaicommunity.mcp.provider.SyncMcpLoggingConsumerProvider;
import org.springaicommunity.mcp.provider.SyncMcpPromptProvider;
import org.springaicommunity.mcp.provider.SyncMcpResourceProvider;
import org.springaicommunity.mcp.provider.SyncMcpSamplingProvider;
import org.springaicommunity.mcp.provider.SyncMcpToolProvider;
import org.springaicommunity.mcp.provider.SyncStatelessMcpPromptProvider;
import org.springaicommunity.mcp.provider.SyncStatelessMcpResourceProvider;
import org.springaicommunity.mcp.provider.SyncStatelessMcpToolProvider;

import io.modelcontextprotocol.server.McpServerFeatures.SyncCompletionSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpStatelessServerFeatures;

/**
 * @author Christian Tzolov
 */
public class SyncMcpAnnotationProvider {

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

	private static class SpringAiSyncMcpLoggingConsumerProvider extends SyncMcpLoggingConsumerProvider {

		public SpringAiSyncMcpLoggingConsumerProvider(List<Object> loggingObjects) {
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

	public static List<SyncToolSpecification> createSyncToolSpecifications(List<Object> toolObjects) {
		return new SpringAiSyncToolProvider(toolObjects).getToolSpecifications();
	}

	public static List<McpStatelessServerFeatures.SyncToolSpecification> createSyncStatelessToolSpecifications(
			List<Object> toolObjects) {
		return new SpringAiSyncStatelessToolProvider(toolObjects).getToolSpecifications();
	}

	public static List<SyncCompletionSpecification> createSyncCompleteSpecifications(List<Object> completeObjects) {
		return new SpringAiSyncMcpCompletionProvider(completeObjects).getCompleteSpecifications();
	}

	public static List<SyncPromptSpecification> createSyncPromptSpecifications(List<Object> promptObjects) {
		return new SpringAiSyncMcpPromptProvider(promptObjects).getPromptSpecifications();
	}

	public static List<McpStatelessServerFeatures.SyncPromptSpecification> createSyncStatelessPromptSpecifications(
			List<Object> promptObjects) {
		return new SpringAiSyncStatelessPromptProvider(promptObjects).getPromptSpecifications();
	}

	public static List<SyncResourceSpecification> createSyncResourceSpecifications(List<Object> resourceObjects) {
		return new SpringAiSyncMcpResourceProvider(resourceObjects).getResourceSpecifications();
	}

	public static List<McpStatelessServerFeatures.SyncResourceSpecification> createSyncStatelessResourceSpecifications(
			List<Object> resourceObjects) {
		return new SpringAiSyncStatelessResourceProvider(resourceObjects).getResourceSpecifications();
	}

	public static List<SyncLoggingSpecification> createSyncLoggingSpecifications(List<Object> loggingObjects) {
		return new SpringAiSyncMcpLoggingConsumerProvider(loggingObjects).getLoggingSpecifications();
	}

	public static List<SyncSamplingSpecification> createSyncSamplingSpecifications(List<Object> samplingObjects) {
		return new SpringAiSyncMcpSamplingProvider(samplingObjects).getSamplingSpecifications();
	}

	public static List<SyncElicitationSpecification> createSyncElicitationSpecifications(
			List<Object> elicitationObjects) {
		return new SpringAiSyncMcpElicitationProvider(elicitationObjects).getElicitationSpecifications();
	}

}
