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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpComplete;

import io.modelcontextprotocol.server.McpStatelessServerFeatures.SyncCompletionSpecification;
import io.modelcontextprotocol.server.McpTransportContext;
import io.modelcontextprotocol.spec.McpSchema.CompleteRequest;
import io.modelcontextprotocol.spec.McpSchema.CompleteResult;
import io.modelcontextprotocol.spec.McpSchema.CompleteResult.CompleteCompletion;
import io.modelcontextprotocol.spec.McpSchema.PromptReference;
import io.modelcontextprotocol.spec.McpSchema.ResourceReference;
import reactor.core.publisher.Mono;

/**
 * Tests for {@link SyncStatelessMcpCompleteProvider}.
 *
 * @author Christian Tzolov
 */
public class SyncStatelessMcpCompleteProviderTests {

	@Test
	void testConstructorWithNullCompleteObjects() {
		assertThatThrownBy(() -> new SyncStatelessMcpCompleteProvider(null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("completeObjects cannot be null");
	}

	@Test
	void testGetCompleteSpecificationsWithSingleValidComplete() {
		// Create a class with only one valid sync complete method
		class SingleValidComplete {

			@McpComplete(prompt = "test-prompt")
			public CompleteResult testComplete(CompleteRequest request) {
				return new CompleteResult(
						new CompleteCompletion(List.of("Sync completion for " + request.argument().value()), 1, false));
			}

		}

		SingleValidComplete completeObject = new SingleValidComplete();
		SyncStatelessMcpCompleteProvider provider = new SyncStatelessMcpCompleteProvider(List.of(completeObject));

		List<SyncCompletionSpecification> completeSpecs = provider.getCompleteSpecifications();

		assertThat(completeSpecs).isNotNull();
		assertThat(completeSpecs).hasSize(1);

		SyncCompletionSpecification completeSpec = completeSpecs.get(0);
		assertThat(completeSpec.referenceKey()).isInstanceOf(PromptReference.class);
		PromptReference promptRef = (PromptReference) completeSpec.referenceKey();
		assertThat(promptRef.name()).isEqualTo("test-prompt");
		assertThat(completeSpec.completionHandler()).isNotNull();

		// Test that the handler works
		McpTransportContext context = mock(McpTransportContext.class);
		CompleteRequest request = new CompleteRequest(new PromptReference("test-prompt"),
				new CompleteRequest.CompleteArgument("test", "value"));
		CompleteResult result = completeSpec.completionHandler().apply(context, request);

		assertThat(result).isNotNull();
		assertThat(result.completion()).isNotNull();
		assertThat(result.completion().values()).hasSize(1);
		assertThat(result.completion().values().get(0)).isEqualTo("Sync completion for value");
	}

	@Test
	void testGetCompleteSpecificationsWithUriReference() {
		class UriComplete {

			@McpComplete(uri = "test://{variable}")
			public CompleteResult uriComplete(CompleteRequest request) {
				return new CompleteResult(new CompleteCompletion(
						List.of("Sync URI completion for " + request.argument().value()), 1, false));
			}

		}

		UriComplete completeObject = new UriComplete();
		SyncStatelessMcpCompleteProvider provider = new SyncStatelessMcpCompleteProvider(List.of(completeObject));

		List<SyncCompletionSpecification> completeSpecs = provider.getCompleteSpecifications();

		assertThat(completeSpecs).hasSize(1);
		assertThat(completeSpecs.get(0).referenceKey()).isInstanceOf(ResourceReference.class);
		ResourceReference resourceRef = (ResourceReference) completeSpecs.get(0).referenceKey();
		assertThat(resourceRef.uri()).isEqualTo("test://{variable}");

		// Test that the handler works
		McpTransportContext context = mock(McpTransportContext.class);
		CompleteRequest request = new CompleteRequest(new ResourceReference("test://value"),
				new CompleteRequest.CompleteArgument("variable", "value"));
		CompleteResult result = completeSpecs.get(0).completionHandler().apply(context, request);

		assertThat(result).isNotNull();
		assertThat(result.completion()).isNotNull();
		assertThat(result.completion().values()).hasSize(1);
		assertThat(result.completion().values().get(0)).isEqualTo("Sync URI completion for value");
	}

	@Test
	void testGetCompleteSpecificationsFiltersOutReactiveReturnTypes() {
		class MixedReturnComplete {

			@McpComplete(prompt = "sync-complete")
			public CompleteResult syncComplete(CompleteRequest request) {
				return new CompleteResult(
						new CompleteCompletion(List.of("Sync completion for " + request.argument().value()), 1, false));
			}

			@McpComplete(prompt = "async-complete")
			public Mono<CompleteResult> asyncComplete(CompleteRequest request) {
				return Mono.just(new CompleteResult(new CompleteCompletion(
						List.of("Async completion for " + request.argument().value()), 1, false)));
			}

		}

		MixedReturnComplete completeObject = new MixedReturnComplete();
		SyncStatelessMcpCompleteProvider provider = new SyncStatelessMcpCompleteProvider(List.of(completeObject));

		List<SyncCompletionSpecification> completeSpecs = provider.getCompleteSpecifications();

		assertThat(completeSpecs).hasSize(1);
		PromptReference promptRef = (PromptReference) completeSpecs.get(0).referenceKey();
		assertThat(promptRef.name()).isEqualTo("sync-complete");
	}

	@Test
	void testGetCompleteSpecificationsWithMultipleCompleteMethods() {
		class MultipleCompleteMethods {

			@McpComplete(prompt = "complete1")
			public CompleteResult firstComplete(CompleteRequest request) {
				return new CompleteResult(new CompleteCompletion(
						List.of("First completion for " + request.argument().value()), 1, false));
			}

			@McpComplete(prompt = "complete2")
			public CompleteResult secondComplete(CompleteRequest request) {
				return new CompleteResult(new CompleteCompletion(
						List.of("Second completion for " + request.argument().value()), 1, false));
			}

		}

		MultipleCompleteMethods completeObject = new MultipleCompleteMethods();
		SyncStatelessMcpCompleteProvider provider = new SyncStatelessMcpCompleteProvider(List.of(completeObject));

		List<SyncCompletionSpecification> completeSpecs = provider.getCompleteSpecifications();

		assertThat(completeSpecs).hasSize(2);
		PromptReference promptRef1 = (PromptReference) completeSpecs.get(0).referenceKey();
		PromptReference promptRef2 = (PromptReference) completeSpecs.get(1).referenceKey();
		assertThat(promptRef1.name()).isIn("complete1", "complete2");
		assertThat(promptRef2.name()).isIn("complete1", "complete2");
		assertThat(promptRef1.name()).isNotEqualTo(promptRef2.name());
	}

	@Test
	void testGetCompleteSpecificationsWithMultipleCompleteObjects() {
		class FirstCompleteObject {

			@McpComplete(prompt = "first-complete")
			public CompleteResult firstComplete(CompleteRequest request) {
				return new CompleteResult(new CompleteCompletion(
						List.of("First completion for " + request.argument().value()), 1, false));
			}

		}

		class SecondCompleteObject {

			@McpComplete(prompt = "second-complete")
			public CompleteResult secondComplete(CompleteRequest request) {
				return new CompleteResult(new CompleteCompletion(
						List.of("Second completion for " + request.argument().value()), 1, false));
			}

		}

		FirstCompleteObject firstObject = new FirstCompleteObject();
		SecondCompleteObject secondObject = new SecondCompleteObject();
		SyncStatelessMcpCompleteProvider provider = new SyncStatelessMcpCompleteProvider(
				List.of(firstObject, secondObject));

		List<SyncCompletionSpecification> completeSpecs = provider.getCompleteSpecifications();

		assertThat(completeSpecs).hasSize(2);
		PromptReference promptRef1 = (PromptReference) completeSpecs.get(0).referenceKey();
		PromptReference promptRef2 = (PromptReference) completeSpecs.get(1).referenceKey();
		assertThat(promptRef1.name()).isIn("first-complete", "second-complete");
		assertThat(promptRef2.name()).isIn("first-complete", "second-complete");
		assertThat(promptRef1.name()).isNotEqualTo(promptRef2.name());
	}

	@Test
	void testGetCompleteSpecificationsWithMixedMethods() {
		class MixedMethods {

			@McpComplete(prompt = "valid-complete")
			public CompleteResult validComplete(CompleteRequest request) {
				return new CompleteResult(new CompleteCompletion(
						List.of("Valid completion for " + request.argument().value()), 1, false));
			}

			public CompleteResult nonAnnotatedMethod(CompleteRequest request) {
				return new CompleteResult(new CompleteCompletion(
						List.of("Non-annotated completion for " + request.argument().value()), 1, false));
			}

			@McpComplete(prompt = "async-complete")
			public Mono<CompleteResult> asyncComplete(CompleteRequest request) {
				return Mono.just(new CompleteResult(new CompleteCompletion(
						List.of("Async completion for " + request.argument().value()), 1, false)));
			}

		}

		MixedMethods completeObject = new MixedMethods();
		SyncStatelessMcpCompleteProvider provider = new SyncStatelessMcpCompleteProvider(List.of(completeObject));

		List<SyncCompletionSpecification> completeSpecs = provider.getCompleteSpecifications();

		assertThat(completeSpecs).hasSize(1);
		PromptReference promptRef = (PromptReference) completeSpecs.get(0).referenceKey();
		assertThat(promptRef.name()).isEqualTo("valid-complete");
	}

	@Test
	void testGetCompleteSpecificationsWithPrivateMethod() {
		class PrivateMethodComplete {

			@McpComplete(prompt = "private-complete")
			private CompleteResult privateComplete(CompleteRequest request) {
				return new CompleteResult(new CompleteCompletion(
						List.of("Private completion for " + request.argument().value()), 1, false));
			}

		}

		PrivateMethodComplete completeObject = new PrivateMethodComplete();
		SyncStatelessMcpCompleteProvider provider = new SyncStatelessMcpCompleteProvider(List.of(completeObject));

		List<SyncCompletionSpecification> completeSpecs = provider.getCompleteSpecifications();

		assertThat(completeSpecs).hasSize(1);
		PromptReference promptRef = (PromptReference) completeSpecs.get(0).referenceKey();
		assertThat(promptRef.name()).isEqualTo("private-complete");

		// Test that the handler works with private methods
		McpTransportContext context = mock(McpTransportContext.class);
		CompleteRequest request = new CompleteRequest(new PromptReference("private-complete"),
				new CompleteRequest.CompleteArgument("test", "value"));
		CompleteResult result = completeSpecs.get(0).completionHandler().apply(context, request);

		assertThat(result).isNotNull();
		assertThat(result.completion()).isNotNull();
		assertThat(result.completion().values()).hasSize(1);
		assertThat(result.completion().values().get(0)).isEqualTo("Private completion for value");
	}

	@Test
	void testGetCompleteSpecificationsWithStringReturn() {
		class StringReturnComplete {

			@McpComplete(prompt = "string-complete")
			public String stringComplete(CompleteRequest request) {
				return "Simple string completion for " + request.argument().value();
			}

		}

		StringReturnComplete completeObject = new StringReturnComplete();
		SyncStatelessMcpCompleteProvider provider = new SyncStatelessMcpCompleteProvider(List.of(completeObject));

		List<SyncCompletionSpecification> completeSpecs = provider.getCompleteSpecifications();

		assertThat(completeSpecs).hasSize(1);
		PromptReference promptRef = (PromptReference) completeSpecs.get(0).referenceKey();
		assertThat(promptRef.name()).isEqualTo("string-complete");

		// Test that the handler works with String return type
		McpTransportContext context = mock(McpTransportContext.class);
		CompleteRequest request = new CompleteRequest(new PromptReference("string-complete"),
				new CompleteRequest.CompleteArgument("test", "value"));
		CompleteResult result = completeSpecs.get(0).completionHandler().apply(context, request);

		assertThat(result).isNotNull();
		assertThat(result.completion()).isNotNull();
		assertThat(result.completion().values()).hasSize(1);
		assertThat(result.completion().values().get(0)).isEqualTo("Simple string completion for value");
	}

	@Test
	void testGetCompleteSpecificationsWithContextParameter() {
		class ContextParameterComplete {

			@McpComplete(prompt = "context-complete")
			public CompleteResult contextComplete(McpTransportContext context, CompleteRequest request) {
				return new CompleteResult(new CompleteCompletion(List.of("Completion with context: "
						+ (context != null ? "present" : "null") + ", value: " + request.argument().value()), 1,
						false));
			}

		}

		ContextParameterComplete completeObject = new ContextParameterComplete();
		SyncStatelessMcpCompleteProvider provider = new SyncStatelessMcpCompleteProvider(List.of(completeObject));

		List<SyncCompletionSpecification> completeSpecs = provider.getCompleteSpecifications();

		assertThat(completeSpecs).hasSize(1);
		PromptReference promptRef = (PromptReference) completeSpecs.get(0).referenceKey();
		assertThat(promptRef.name()).isEqualTo("context-complete");

		// Test that the handler works with context parameter
		McpTransportContext context = mock(McpTransportContext.class);
		CompleteRequest request = new CompleteRequest(new PromptReference("context-complete"),
				new CompleteRequest.CompleteArgument("test", "value"));
		CompleteResult result = completeSpecs.get(0).completionHandler().apply(context, request);

		assertThat(result).isNotNull();
		assertThat(result.completion()).isNotNull();
		assertThat(result.completion().values()).hasSize(1);
		assertThat(result.completion().values().get(0)).isEqualTo("Completion with context: present, value: value");
	}

	@Test
	void testGetCompleteSpecificationsWithListReturn() {
		class ListReturnComplete {

			@McpComplete(prompt = "list-complete")
			public List<String> listComplete(CompleteRequest request) {
				return List.of("First completion for " + request.argument().value(),
						"Second completion for " + request.argument().value());
			}

		}

		ListReturnComplete completeObject = new ListReturnComplete();
		SyncStatelessMcpCompleteProvider provider = new SyncStatelessMcpCompleteProvider(List.of(completeObject));

		List<SyncCompletionSpecification> completeSpecs = provider.getCompleteSpecifications();

		assertThat(completeSpecs).hasSize(1);
		PromptReference promptRef = (PromptReference) completeSpecs.get(0).referenceKey();
		assertThat(promptRef.name()).isEqualTo("list-complete");

		// Test that the handler works with List<String> return type
		McpTransportContext context = mock(McpTransportContext.class);
		CompleteRequest request = new CompleteRequest(new PromptReference("list-complete"),
				new CompleteRequest.CompleteArgument("test", "value"));
		CompleteResult result = completeSpecs.get(0).completionHandler().apply(context, request);

		assertThat(result).isNotNull();
		assertThat(result.completion()).isNotNull();
		assertThat(result.completion().values()).hasSize(2);
		assertThat(result.completion().values().get(0)).isEqualTo("First completion for value");
		assertThat(result.completion().values().get(1)).isEqualTo("Second completion for value");
	}

	@Test
	void testGetCompleteSpecificationsWithCompletionReturn() {
		class CompletionReturnComplete {

			@McpComplete(prompt = "completion-complete")
			public CompleteCompletion completionComplete(CompleteRequest request) {
				return new CompleteCompletion(List.of("Completion object for " + request.argument().value()), 1, false);
			}

		}

		CompletionReturnComplete completeObject = new CompletionReturnComplete();
		SyncStatelessMcpCompleteProvider provider = new SyncStatelessMcpCompleteProvider(List.of(completeObject));

		List<SyncCompletionSpecification> completeSpecs = provider.getCompleteSpecifications();

		assertThat(completeSpecs).hasSize(1);
		PromptReference promptRef = (PromptReference) completeSpecs.get(0).referenceKey();
		assertThat(promptRef.name()).isEqualTo("completion-complete");

		// Test that the handler works with CompleteCompletion return type
		McpTransportContext context = mock(McpTransportContext.class);
		CompleteRequest request = new CompleteRequest(new PromptReference("completion-complete"),
				new CompleteRequest.CompleteArgument("test", "value"));
		CompleteResult result = completeSpecs.get(0).completionHandler().apply(context, request);

		assertThat(result).isNotNull();
		assertThat(result.completion()).isNotNull();
		assertThat(result.completion().values()).hasSize(1);
		assertThat(result.completion().values().get(0)).isEqualTo("Completion object for value");
	}

	@Test
	void testGetCompleteSpecificationsWithEmptyList() {
		SyncStatelessMcpCompleteProvider provider = new SyncStatelessMcpCompleteProvider(List.of());

		List<SyncCompletionSpecification> completeSpecs = provider.getCompleteSpecifications();

		assertThat(completeSpecs).isNotNull();
		assertThat(completeSpecs).isEmpty();
	}

	@Test
	void testGetCompleteSpecificationsWithNoValidMethods() {
		class NoValidMethods {

			public void voidMethod() {
				// No return value
			}

			public String nonAnnotatedMethod() {
				return "Not annotated";
			}

		}

		NoValidMethods completeObject = new NoValidMethods();
		SyncStatelessMcpCompleteProvider provider = new SyncStatelessMcpCompleteProvider(List.of(completeObject));

		List<SyncCompletionSpecification> completeSpecs = provider.getCompleteSpecifications();

		assertThat(completeSpecs).isNotNull();
		assertThat(completeSpecs).isEmpty();
	}

}
