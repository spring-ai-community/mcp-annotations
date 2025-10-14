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
import java.util.List;
import java.util.function.Predicate;

import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import org.springaicommunity.mcp.context.McpAsyncRequestContext;
import org.springaicommunity.mcp.context.McpSyncRequestContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link McpProviderUtils}.
 *
 * @author Christian Tzolov
 */
public class McpProviderUtilsTests {

	// Test classes for method reflection tests
	static class TestMethods {

		public String nonReactiveMethod() {
			return "test";
		}

		public Mono<String> monoMethod() {
			return Mono.just("test");
		}

		public Flux<String> fluxMethod() {
			return Flux.just("test");
		}

		public Publisher<String> publisherMethod() {
			return Mono.just("test");
		}

		public void voidMethod() {
		}

		public List<String> listMethod() {
			return List.of("test");
		}

		public String methodWithSyncContext(McpSyncRequestContext context) {
			return "test";
		}

		public String methodWithAsyncContext(McpAsyncRequestContext context) {
			return "test";
		}

		public String methodWithSyncExchange(McpSyncServerExchange exchange) {
			return "test";
		}

		public String methodWithAsyncExchange(McpAsyncServerExchange exchange) {
			return "test";
		}

		public String methodWithMultipleParams(String param1, McpSyncRequestContext context, int param2) {
			return "test";
		}

		public String methodWithoutBidirectionalParams(String param1, int param2) {
			return "test";
		}

	}

	// URI Template Tests

	@Test
	public void testIsUriTemplateWithSimpleVariable() {
		assertThat(McpProviderUtils.isUriTemplate("/api/{id}")).isTrue();
	}

	@Test
	public void testIsUriTemplateWithMultipleVariables() {
		assertThat(McpProviderUtils.isUriTemplate("/api/{userId}/posts/{postId}")).isTrue();
	}

	@Test
	public void testIsUriTemplateWithVariableAtStart() {
		assertThat(McpProviderUtils.isUriTemplate("{id}/details")).isTrue();
	}

	@Test
	public void testIsUriTemplateWithVariableAtEnd() {
		assertThat(McpProviderUtils.isUriTemplate("/api/users/{id}")).isTrue();
	}

	@Test
	public void testIsUriTemplateWithComplexVariableName() {
		assertThat(McpProviderUtils.isUriTemplate("/api/{user_id}")).isTrue();
		assertThat(McpProviderUtils.isUriTemplate("/api/{userId123}")).isTrue();
	}

	@Test
	public void testIsUriTemplateWithNoVariables() {
		assertThat(McpProviderUtils.isUriTemplate("/api/users")).isFalse();
	}

	@Test
	public void testIsUriTemplateWithEmptyString() {
		assertThat(McpProviderUtils.isUriTemplate("")).isFalse();
	}

	@Test
	public void testIsUriTemplateWithOnlySlashes() {
		assertThat(McpProviderUtils.isUriTemplate("/")).isFalse();
		assertThat(McpProviderUtils.isUriTemplate("//")).isFalse();
	}

	@Test
	public void testIsUriTemplateWithIncompleteBraces() {
		assertThat(McpProviderUtils.isUriTemplate("/api/{id")).isFalse();
		assertThat(McpProviderUtils.isUriTemplate("/api/id}")).isFalse();
	}

	@Test
	public void testIsUriTemplateWithEmptyBraces() {
		assertThat(McpProviderUtils.isUriTemplate("/api/{}")).isFalse();
	}

	@Test
	public void testIsUriTemplateWithNestedPath() {
		assertThat(McpProviderUtils.isUriTemplate("/api/v1/users/{userId}/posts/{postId}/comments")).isTrue();
	}

	// Reactive Return Type Predicate Tests

	@Test
	public void testIsReactiveReturnTypeWithMono() throws NoSuchMethodException {
		Method method = TestMethods.class.getMethod("monoMethod");
		assertThat(McpProviderUtils.isReactiveReturnType.test(method)).isTrue();
	}

	@Test
	public void testIsReactiveReturnTypeWithFlux() throws NoSuchMethodException {
		Method method = TestMethods.class.getMethod("fluxMethod");
		assertThat(McpProviderUtils.isReactiveReturnType.test(method)).isTrue();
	}

	@Test
	public void testIsReactiveReturnTypeWithPublisher() throws NoSuchMethodException {
		Method method = TestMethods.class.getMethod("publisherMethod");
		assertThat(McpProviderUtils.isReactiveReturnType.test(method)).isTrue();
	}

	@Test
	public void testIsReactiveReturnTypeWithNonReactive() throws NoSuchMethodException {
		Method method = TestMethods.class.getMethod("nonReactiveMethod");
		assertThat(McpProviderUtils.isReactiveReturnType.test(method)).isFalse();
	}

	@Test
	public void testIsReactiveReturnTypeWithVoid() throws NoSuchMethodException {
		Method method = TestMethods.class.getMethod("voidMethod");
		assertThat(McpProviderUtils.isReactiveReturnType.test(method)).isFalse();
	}

	@Test
	public void testIsReactiveReturnTypeWithList() throws NoSuchMethodException {
		Method method = TestMethods.class.getMethod("listMethod");
		assertThat(McpProviderUtils.isReactiveReturnType.test(method)).isFalse();
	}

	// Non-Reactive Return Type Predicate Tests

	@Test
	public void testIsNotReactiveReturnTypeWithMono() throws NoSuchMethodException {
		Method method = TestMethods.class.getMethod("monoMethod");
		assertThat(McpProviderUtils.isNotReactiveReturnType.test(method)).isFalse();
	}

	@Test
	public void testIsNotReactiveReturnTypeWithFlux() throws NoSuchMethodException {
		Method method = TestMethods.class.getMethod("fluxMethod");
		assertThat(McpProviderUtils.isNotReactiveReturnType.test(method)).isFalse();
	}

	@Test
	public void testIsNotReactiveReturnTypeWithPublisher() throws NoSuchMethodException {
		Method method = TestMethods.class.getMethod("publisherMethod");
		assertThat(McpProviderUtils.isNotReactiveReturnType.test(method)).isFalse();
	}

	@Test
	public void testIsNotReactiveReturnTypeWithNonReactive() throws NoSuchMethodException {
		Method method = TestMethods.class.getMethod("nonReactiveMethod");
		assertThat(McpProviderUtils.isNotReactiveReturnType.test(method)).isTrue();
	}

	@Test
	public void testIsNotReactiveReturnTypeWithVoid() throws NoSuchMethodException {
		Method method = TestMethods.class.getMethod("voidMethod");
		assertThat(McpProviderUtils.isNotReactiveReturnType.test(method)).isTrue();
	}

	@Test
	public void testIsNotReactiveReturnTypeWithList() throws NoSuchMethodException {
		Method method = TestMethods.class.getMethod("listMethod");
		assertThat(McpProviderUtils.isNotReactiveReturnType.test(method)).isTrue();
	}

	// Filter Non-Reactive Return Type Method Tests

	@Test
	public void testFilterNonReactiveReturnTypeMethodWithReactiveType() throws NoSuchMethodException {
		Method method = TestMethods.class.getMethod("monoMethod");
		Predicate<Method> filter = McpProviderUtils.filterNonReactiveReturnTypeMethod();
		assertThat(filter.test(method)).isTrue();
	}

	@Test
	public void testFilterNonReactiveReturnTypeMethodWithNonReactiveType() throws NoSuchMethodException {
		Method method = TestMethods.class.getMethod("nonReactiveMethod");
		Predicate<Method> filter = McpProviderUtils.filterNonReactiveReturnTypeMethod();
		// This should return false and log a warning
		assertThat(filter.test(method)).isFalse();
	}

	@Test
	public void testFilterNonReactiveReturnTypeMethodWithFlux() throws NoSuchMethodException {
		Method method = TestMethods.class.getMethod("fluxMethod");
		Predicate<Method> filter = McpProviderUtils.filterNonReactiveReturnTypeMethod();
		assertThat(filter.test(method)).isTrue();
	}

	@Test
	public void testFilterNonReactiveReturnTypeMethodWithPublisher() throws NoSuchMethodException {
		Method method = TestMethods.class.getMethod("publisherMethod");
		Predicate<Method> filter = McpProviderUtils.filterNonReactiveReturnTypeMethod();
		assertThat(filter.test(method)).isTrue();
	}

	// Filter Reactive Return Type Method Tests

	@Test
	public void testFilterReactiveReturnTypeMethodWithReactiveType() throws NoSuchMethodException {
		Method method = TestMethods.class.getMethod("monoMethod");
		Predicate<Method> filter = McpProviderUtils.filterReactiveReturnTypeMethod();
		// This should return false and log a warning
		assertThat(filter.test(method)).isFalse();
	}

	@Test
	public void testFilterReactiveReturnTypeMethodWithNonReactiveType() throws NoSuchMethodException {
		Method method = TestMethods.class.getMethod("nonReactiveMethod");
		Predicate<Method> filter = McpProviderUtils.filterReactiveReturnTypeMethod();
		assertThat(filter.test(method)).isTrue();
	}

	@Test
	public void testFilterReactiveReturnTypeMethodWithFlux() throws NoSuchMethodException {
		Method method = TestMethods.class.getMethod("fluxMethod");
		Predicate<Method> filter = McpProviderUtils.filterReactiveReturnTypeMethod();
		// This should return false and log a warning
		assertThat(filter.test(method)).isFalse();
	}

	@Test
	public void testFilterReactiveReturnTypeMethodWithPublisher() throws NoSuchMethodException {
		Method method = TestMethods.class.getMethod("publisherMethod");
		Predicate<Method> filter = McpProviderUtils.filterReactiveReturnTypeMethod();
		// This should return false and log a warning
		assertThat(filter.test(method)).isFalse();
	}

	@Test
	public void testFilterReactiveReturnTypeMethodWithVoid() throws NoSuchMethodException {
		Method method = TestMethods.class.getMethod("voidMethod");
		Predicate<Method> filter = McpProviderUtils.filterReactiveReturnTypeMethod();
		assertThat(filter.test(method)).isTrue();
	}

	// Filter Method With Bidirectional Parameters Tests

	@Test
	public void testFilterMethodWithBidirectionalParametersWithSyncContext() throws NoSuchMethodException {
		Method method = TestMethods.class.getMethod("methodWithSyncContext", McpSyncRequestContext.class);
		Predicate<Method> filter = McpProviderUtils.filterMethodWithBidirectionalParameters();
		// This should return false and log a warning
		assertThat(filter.test(method)).isFalse();
	}

	@Test
	public void testFilterMethodWithBidirectionalParametersWithAsyncContext() throws NoSuchMethodException {
		Method method = TestMethods.class.getMethod("methodWithAsyncContext", McpAsyncRequestContext.class);
		Predicate<Method> filter = McpProviderUtils.filterMethodWithBidirectionalParameters();
		// This should return false and log a warning
		assertThat(filter.test(method)).isFalse();
	}

	@Test
	public void testFilterMethodWithBidirectionalParametersWithSyncExchange() throws NoSuchMethodException {
		Method method = TestMethods.class.getMethod("methodWithSyncExchange", McpSyncServerExchange.class);
		Predicate<Method> filter = McpProviderUtils.filterMethodWithBidirectionalParameters();
		// This should return false and log a warning
		assertThat(filter.test(method)).isFalse();
	}

	@Test
	public void testFilterMethodWithBidirectionalParametersWithAsyncExchange() throws NoSuchMethodException {
		Method method = TestMethods.class.getMethod("methodWithAsyncExchange", McpAsyncServerExchange.class);
		Predicate<Method> filter = McpProviderUtils.filterMethodWithBidirectionalParameters();
		// This should return false and log a warning
		assertThat(filter.test(method)).isFalse();
	}

	@Test
	public void testFilterMethodWithBidirectionalParametersWithMultipleParams() throws NoSuchMethodException {
		Method method = TestMethods.class.getMethod("methodWithMultipleParams", String.class,
				McpSyncRequestContext.class, int.class);
		Predicate<Method> filter = McpProviderUtils.filterMethodWithBidirectionalParameters();
		// This should return false because it has a bidirectional parameter
		assertThat(filter.test(method)).isFalse();
	}

	@Test
	public void testFilterMethodWithBidirectionalParametersWithoutBidirectionalParams() throws NoSuchMethodException {
		Method method = TestMethods.class.getMethod("methodWithoutBidirectionalParams", String.class, int.class);
		Predicate<Method> filter = McpProviderUtils.filterMethodWithBidirectionalParameters();
		assertThat(filter.test(method)).isTrue();
	}

	@Test
	public void testFilterMethodWithBidirectionalParametersWithNoParams() throws NoSuchMethodException {
		Method method = TestMethods.class.getMethod("nonReactiveMethod");
		Predicate<Method> filter = McpProviderUtils.filterMethodWithBidirectionalParameters();
		assertThat(filter.test(method)).isTrue();
	}

	// Combined Filter Tests

	@Test
	public void testCombinedFiltersForStatelessSyncProvider() throws NoSuchMethodException {
		// Stateless sync providers should filter out:
		// 1. Methods with reactive return types
		// 2. Methods with bidirectional parameters

		Method validMethod = TestMethods.class.getMethod("methodWithoutBidirectionalParams", String.class, int.class);
		Method reactiveMethod = TestMethods.class.getMethod("monoMethod");
		Method bidirectionalMethod = TestMethods.class.getMethod("methodWithSyncContext", McpSyncRequestContext.class);

		Predicate<Method> reactiveFilter = McpProviderUtils.filterReactiveReturnTypeMethod();
		Predicate<Method> bidirectionalFilter = McpProviderUtils.filterMethodWithBidirectionalParameters();
		Predicate<Method> combinedFilter = reactiveFilter.and(bidirectionalFilter);

		assertThat(combinedFilter.test(validMethod)).isTrue();
		assertThat(combinedFilter.test(reactiveMethod)).isFalse();
		assertThat(combinedFilter.test(bidirectionalMethod)).isFalse();
	}

	@Test
	public void testCombinedFiltersForStatelessAsyncProvider() throws NoSuchMethodException {
		// Stateless async providers should filter out:
		// 1. Methods with non-reactive return types
		// 2. Methods with bidirectional parameters

		Method validMethod = TestMethods.class.getMethod("monoMethod");
		Method nonReactiveMethod = TestMethods.class.getMethod("nonReactiveMethod");
		Method bidirectionalMethod = TestMethods.class.getMethod("methodWithAsyncContext",
				McpAsyncRequestContext.class);

		Predicate<Method> nonReactiveFilter = McpProviderUtils.filterNonReactiveReturnTypeMethod();
		Predicate<Method> bidirectionalFilter = McpProviderUtils.filterMethodWithBidirectionalParameters();
		Predicate<Method> combinedFilter = nonReactiveFilter.and(bidirectionalFilter);

		assertThat(combinedFilter.test(validMethod)).isTrue();
		assertThat(combinedFilter.test(nonReactiveMethod)).isFalse();
		assertThat(combinedFilter.test(bidirectionalMethod)).isFalse();
	}

	// Edge Case Tests

	@Test
	public void testIsUriTemplateWithSpecialCharacters() {
		assertThat(McpProviderUtils.isUriTemplate("/api/{user-id}")).isTrue();
		assertThat(McpProviderUtils.isUriTemplate("/api/{user.id}")).isTrue();
	}

	@Test
	public void testIsUriTemplateWithQueryParameters() {
		// Query parameters are not URI template variables
		assertThat(McpProviderUtils.isUriTemplate("/api/users?id={id}")).isTrue();
	}

	@Test
	public void testIsUriTemplateWithFragment() {
		assertThat(McpProviderUtils.isUriTemplate("/api/users#{id}")).isTrue();
	}

	@Test
	public void testIsUriTemplateWithMultipleConsecutiveVariables() {
		assertThat(McpProviderUtils.isUriTemplate("/{id}{name}")).isTrue();
	}

	@Test
	public void testPredicatesAreReusable() throws NoSuchMethodException {
		// Test that predicates can be reused multiple times
		Predicate<Method> filter = McpProviderUtils.filterReactiveReturnTypeMethod();

		Method method1 = TestMethods.class.getMethod("nonReactiveMethod");
		Method method2 = TestMethods.class.getMethod("monoMethod");
		Method method3 = TestMethods.class.getMethod("listMethod");

		assertThat(filter.test(method1)).isTrue();
		assertThat(filter.test(method2)).isFalse();
		assertThat(filter.test(method3)).isTrue();
	}

}
