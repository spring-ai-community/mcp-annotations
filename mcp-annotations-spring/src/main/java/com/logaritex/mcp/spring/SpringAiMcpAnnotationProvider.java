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
package com.logaritex.mcp.spring;

import java.lang.reflect.Method;
import java.util.List;

import com.logaritex.mcp.provider.SyncMcpCompletionProvider;
import com.logaritex.mcp.provider.SyncMcpPromptProvider;
import com.logaritex.mcp.provider.SyncMcpResourceProvider;
import io.modelcontextprotocol.server.McpServerFeatures.SyncCompletionSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncPromptSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncResourceSpecification;

import org.springframework.aop.support.AopUtils;
import org.springframework.util.ReflectionUtils;

/**
 * @author Christian Tzolov
 */
public class SpringAiMcpAnnotationProvider {

	private static class SpringAiSyncMcpCompletionProvider extends SyncMcpCompletionProvider {

		public SpringAiSyncMcpCompletionProvider(List<Object> completeObjects) {
			super(completeObjects);
		}

		@Override
		protected Method[] doGetClassMethods(Object bean) {
			return ReflectionUtils
				.getDeclaredMethods(AopUtils.isAopProxy(bean) ? AopUtils.getTargetClass(bean) : bean.getClass());
		}

	};

	private static class SpringAiSyncMcpPromptProvider extends SyncMcpPromptProvider {

		public SpringAiSyncMcpPromptProvider(List<Object> promptObjects) {
			super(promptObjects);
		}

		@Override
		protected Method[] doGetClassMethods(Object bean) {
			return ReflectionUtils
				.getDeclaredMethods(AopUtils.isAopProxy(bean) ? AopUtils.getTargetClass(bean) : bean.getClass());
		}

	};

	private static class SpringAiSyncMcpResourceProvider extends SyncMcpResourceProvider {

		public SpringAiSyncMcpResourceProvider(List<Object> resourceObjects) {
			super(resourceObjects);
		}

		@Override
		protected Method[] doGetClassMethods(Object bean) {
			return ReflectionUtils
				.getDeclaredMethods(AopUtils.isAopProxy(bean) ? AopUtils.getTargetClass(bean) : bean.getClass());
		}

	}

	public static List<SyncCompletionSpecification> createSyncCompleteSpecifications(List<Object> completeObjects) {
		return new SpringAiSyncMcpCompletionProvider(completeObjects).getCompleteSpecifications();
	}

	public static List<SyncPromptSpecification> createSyncPromptSpecifications(List<Object> promptObjects) {
		return new SpringAiSyncMcpPromptProvider(promptObjects).getPromptSpecifications();
	}

	public static List<SyncResourceSpecification> createSyncResourceSpecifications(List<Object> resourceObjects) {
		return new SpringAiSyncMcpResourceProvider(resourceObjects).getResourceSpecifications();
	}

}
