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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpArg;
import org.springaicommunity.mcp.annotation.McpPrompt;

import io.modelcontextprotocol.server.McpStatelessServerFeatures.SyncPromptSpecification;
import io.modelcontextprotocol.server.McpTransportContext;
import io.modelcontextprotocol.spec.McpSchema.GetPromptRequest;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import reactor.core.publisher.Mono;

/**
 * Tests for {@link SyncStatelessMcpPromptProvider}.
 *
 * @author Christian Tzolov
 */
public class SyncStatelessMcpPromptProviderTests {

	@Test
	void testConstructorWithNullPromptObjects() {
		assertThatThrownBy(() -> new SyncStatelessMcpPromptProvider(null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("promptObjects cannot be null");
	}

	@Test
	void testGetPromptSpecificationsWithSingleValidPrompt() {
		// Create a class with only one valid prompt method
		class SingleValidPrompt {

			@McpPrompt(name = "test-prompt", description = "A test prompt")
			public GetPromptResult testPrompt(GetPromptRequest request) {
				return new GetPromptResult("Test prompt result",
						List.of(new PromptMessage(Role.ASSISTANT, new TextContent("Hello from " + request.name()))));
			}

		}

		SingleValidPrompt promptObject = new SingleValidPrompt();
		SyncStatelessMcpPromptProvider provider = new SyncStatelessMcpPromptProvider(List.of(promptObject));

		List<SyncPromptSpecification> promptSpecs = provider.getPromptSpecifications();

		assertThat(promptSpecs).isNotNull();
		assertThat(promptSpecs).hasSize(1);

		SyncPromptSpecification promptSpec = promptSpecs.get(0);
		assertThat(promptSpec.prompt().name()).isEqualTo("test-prompt");
		assertThat(promptSpec.prompt().description()).isEqualTo("A test prompt");
		assertThat(promptSpec.promptHandler()).isNotNull();

		// Test that the handler works
		McpTransportContext context = mock(McpTransportContext.class);
		Map<String, Object> args = new HashMap<>();
		args.put("name", "John");
		GetPromptRequest request = new GetPromptRequest("test-prompt", args);
		GetPromptResult result = promptSpec.promptHandler().apply(context, request);

		assertThat(result).isNotNull();
		assertThat(result.description()).isEqualTo("Test prompt result");
		assertThat(result.messages()).hasSize(1);
		PromptMessage message = result.messages().get(0);
		assertThat(message.role()).isEqualTo(Role.ASSISTANT);
		assertThat(((TextContent) message.content()).text()).isEqualTo("Hello from test-prompt");
	}

	@Test
	void testGetPromptSpecificationsWithCustomPromptName() {
		class CustomNamePrompt {

			@McpPrompt(name = "custom-name", description = "Custom named prompt")
			public GetPromptResult methodWithDifferentName() {
				return new GetPromptResult("Custom prompt result",
						List.of(new PromptMessage(Role.ASSISTANT, new TextContent("Custom prompt content"))));
			}

		}

		CustomNamePrompt promptObject = new CustomNamePrompt();
		SyncStatelessMcpPromptProvider provider = new SyncStatelessMcpPromptProvider(List.of(promptObject));

		List<SyncPromptSpecification> promptSpecs = provider.getPromptSpecifications();

		assertThat(promptSpecs).hasSize(1);
		assertThat(promptSpecs.get(0).prompt().name()).isEqualTo("custom-name");
		assertThat(promptSpecs.get(0).prompt().description()).isEqualTo("Custom named prompt");
	}

	@Test
	void testGetPromptSpecificationsWithDefaultPromptName() {
		class DefaultNamePrompt {

			@McpPrompt(description = "Prompt with default name")
			public GetPromptResult defaultNameMethod() {
				return new GetPromptResult("Default prompt result",
						List.of(new PromptMessage(Role.ASSISTANT, new TextContent("Default prompt content"))));
			}

		}

		DefaultNamePrompt promptObject = new DefaultNamePrompt();
		SyncStatelessMcpPromptProvider provider = new SyncStatelessMcpPromptProvider(List.of(promptObject));

		List<SyncPromptSpecification> promptSpecs = provider.getPromptSpecifications();

		assertThat(promptSpecs).hasSize(1);
		assertThat(promptSpecs.get(0).prompt().name()).isEqualTo("defaultNameMethod");
		assertThat(promptSpecs.get(0).prompt().description()).isEqualTo("Prompt with default name");
	}

	@Test
	void testGetPromptSpecificationsWithEmptyPromptName() {
		class EmptyNamePrompt {

			@McpPrompt(name = "", description = "Prompt with empty name")
			public GetPromptResult emptyNameMethod() {
				return new GetPromptResult("Empty name prompt result",
						List.of(new PromptMessage(Role.ASSISTANT, new TextContent("Empty name prompt content"))));
			}

		}

		EmptyNamePrompt promptObject = new EmptyNamePrompt();
		SyncStatelessMcpPromptProvider provider = new SyncStatelessMcpPromptProvider(List.of(promptObject));

		List<SyncPromptSpecification> promptSpecs = provider.getPromptSpecifications();

		assertThat(promptSpecs).hasSize(1);
		assertThat(promptSpecs.get(0).prompt().name()).isEqualTo("emptyNameMethod");
		assertThat(promptSpecs.get(0).prompt().description()).isEqualTo("Prompt with empty name");
	}

	@Test
	void testGetPromptSpecificationsFiltersOutMonoReturnTypes() {
		class MonoReturnPrompt {

			@McpPrompt(name = "mono-prompt", description = "Prompt returning Mono")
			public Mono<GetPromptResult> monoPrompt() {
				return Mono.just(new GetPromptResult("Mono prompt result",
						List.of(new PromptMessage(Role.ASSISTANT, new TextContent("Mono prompt content")))));
			}

			@McpPrompt(name = "sync-prompt", description = "Synchronous prompt")
			public GetPromptResult syncPrompt() {
				return new GetPromptResult("Sync prompt result",
						List.of(new PromptMessage(Role.ASSISTANT, new TextContent("Sync prompt content"))));
			}

		}

		MonoReturnPrompt promptObject = new MonoReturnPrompt();
		SyncStatelessMcpPromptProvider provider = new SyncStatelessMcpPromptProvider(List.of(promptObject));

		List<SyncPromptSpecification> promptSpecs = provider.getPromptSpecifications();

		assertThat(promptSpecs).hasSize(1);
		assertThat(promptSpecs.get(0).prompt().name()).isEqualTo("sync-prompt");
		assertThat(promptSpecs.get(0).prompt().description()).isEqualTo("Synchronous prompt");
	}

	@Test
	void testGetPromptSpecificationsWithMultiplePromptMethods() {
		class MultiplePromptMethods {

			@McpPrompt(name = "prompt1", description = "First prompt")
			public GetPromptResult firstPrompt() {
				return new GetPromptResult("First prompt result",
						List.of(new PromptMessage(Role.ASSISTANT, new TextContent("First prompt content"))));
			}

			@McpPrompt(name = "prompt2", description = "Second prompt")
			public GetPromptResult secondPrompt() {
				return new GetPromptResult("Second prompt result",
						List.of(new PromptMessage(Role.ASSISTANT, new TextContent("Second prompt content"))));
			}

		}

		MultiplePromptMethods promptObject = new MultiplePromptMethods();
		SyncStatelessMcpPromptProvider provider = new SyncStatelessMcpPromptProvider(List.of(promptObject));

		List<SyncPromptSpecification> promptSpecs = provider.getPromptSpecifications();

		assertThat(promptSpecs).hasSize(2);
		assertThat(promptSpecs.get(0).prompt().name()).isIn("prompt1", "prompt2");
		assertThat(promptSpecs.get(1).prompt().name()).isIn("prompt1", "prompt2");
		assertThat(promptSpecs.get(0).prompt().name()).isNotEqualTo(promptSpecs.get(1).prompt().name());
	}

	@Test
	void testGetPromptSpecificationsWithMultiplePromptObjects() {
		class FirstPromptObject {

			@McpPrompt(name = "first-prompt", description = "First prompt")
			public GetPromptResult firstPrompt() {
				return new GetPromptResult("First prompt result",
						List.of(new PromptMessage(Role.ASSISTANT, new TextContent("First prompt content"))));
			}

		}

		class SecondPromptObject {

			@McpPrompt(name = "second-prompt", description = "Second prompt")
			public GetPromptResult secondPrompt() {
				return new GetPromptResult("Second prompt result",
						List.of(new PromptMessage(Role.ASSISTANT, new TextContent("Second prompt content"))));
			}

		}

		FirstPromptObject firstObject = new FirstPromptObject();
		SecondPromptObject secondObject = new SecondPromptObject();
		SyncStatelessMcpPromptProvider provider = new SyncStatelessMcpPromptProvider(
				List.of(firstObject, secondObject));

		List<SyncPromptSpecification> promptSpecs = provider.getPromptSpecifications();

		assertThat(promptSpecs).hasSize(2);
		assertThat(promptSpecs.get(0).prompt().name()).isIn("first-prompt", "second-prompt");
		assertThat(promptSpecs.get(1).prompt().name()).isIn("first-prompt", "second-prompt");
		assertThat(promptSpecs.get(0).prompt().name()).isNotEqualTo(promptSpecs.get(1).prompt().name());
	}

	@Test
	void testGetPromptSpecificationsWithMixedMethods() {
		class MixedMethods {

			@McpPrompt(name = "valid-prompt", description = "Valid prompt")
			public GetPromptResult validPrompt() {
				return new GetPromptResult("Valid prompt result",
						List.of(new PromptMessage(Role.ASSISTANT, new TextContent("Valid prompt content"))));
			}

			public GetPromptResult nonAnnotatedMethod() {
				return new GetPromptResult("Non-annotated result",
						List.of(new PromptMessage(Role.ASSISTANT, new TextContent("Non-annotated content"))));
			}

			@McpPrompt(name = "mono-prompt", description = "Mono prompt")
			public Mono<GetPromptResult> monoPrompt() {
				return Mono.just(new GetPromptResult("Mono prompt result",
						List.of(new PromptMessage(Role.ASSISTANT, new TextContent("Mono prompt content")))));
			}

		}

		MixedMethods promptObject = new MixedMethods();
		SyncStatelessMcpPromptProvider provider = new SyncStatelessMcpPromptProvider(List.of(promptObject));

		List<SyncPromptSpecification> promptSpecs = provider.getPromptSpecifications();

		assertThat(promptSpecs).hasSize(1);
		assertThat(promptSpecs.get(0).prompt().name()).isEqualTo("valid-prompt");
		assertThat(promptSpecs.get(0).prompt().description()).isEqualTo("Valid prompt");
	}

	@Test
	void testGetPromptSpecificationsWithArguments() {
		class ArgumentPrompt {

			@McpPrompt(name = "argument-prompt", description = "Prompt with arguments")
			public GetPromptResult argumentPrompt(
					@McpArg(name = "name", description = "User's name", required = true) String name,
					@McpArg(name = "age", description = "User's age", required = false) Integer age) {
				return new GetPromptResult("Argument prompt result",
						List.of(new PromptMessage(Role.ASSISTANT, new TextContent(
								"Hello " + name + ", you are " + (age != null ? age : "unknown") + " years old"))));
			}

		}

		ArgumentPrompt promptObject = new ArgumentPrompt();
		SyncStatelessMcpPromptProvider provider = new SyncStatelessMcpPromptProvider(List.of(promptObject));

		List<SyncPromptSpecification> promptSpecs = provider.getPromptSpecifications();

		assertThat(promptSpecs).hasSize(1);
		assertThat(promptSpecs.get(0).prompt().name()).isEqualTo("argument-prompt");
		assertThat(promptSpecs.get(0).prompt().arguments()).hasSize(2);

		// Test that the handler works with arguments
		McpTransportContext context = mock(McpTransportContext.class);
		Map<String, Object> args = new HashMap<>();
		args.put("name", "John");
		args.put("age", 30);
		GetPromptRequest request = new GetPromptRequest("argument-prompt", args);
		GetPromptResult result = promptSpecs.get(0).promptHandler().apply(context, request);

		assertThat(result).isNotNull();
		assertThat(result.description()).isEqualTo("Argument prompt result");
		assertThat(result.messages()).hasSize(1);
		PromptMessage message = result.messages().get(0);
		assertThat(message.role()).isEqualTo(Role.ASSISTANT);
		assertThat(((TextContent) message.content()).text()).isEqualTo("Hello John, you are 30 years old");
	}

	@Test
	void testGetPromptSpecificationsWithPrivateMethod() {
		class PrivateMethodPrompt {

			@McpPrompt(name = "private-prompt", description = "Private prompt method")
			private GetPromptResult privatePrompt() {
				return new GetPromptResult("Private prompt result",
						List.of(new PromptMessage(Role.ASSISTANT, new TextContent("Private prompt content"))));
			}

		}

		PrivateMethodPrompt promptObject = new PrivateMethodPrompt();
		SyncStatelessMcpPromptProvider provider = new SyncStatelessMcpPromptProvider(List.of(promptObject));

		List<SyncPromptSpecification> promptSpecs = provider.getPromptSpecifications();

		assertThat(promptSpecs).hasSize(1);
		assertThat(promptSpecs.get(0).prompt().name()).isEqualTo("private-prompt");
		assertThat(promptSpecs.get(0).prompt().description()).isEqualTo("Private prompt method");

		// Test that the handler works with private methods
		McpTransportContext context = mock(McpTransportContext.class);
		Map<String, Object> args = new HashMap<>();
		GetPromptRequest request = new GetPromptRequest("private-prompt", args);
		GetPromptResult result = promptSpecs.get(0).promptHandler().apply(context, request);

		assertThat(result).isNotNull();
		assertThat(result.description()).isEqualTo("Private prompt result");
		assertThat(result.messages()).hasSize(1);
		PromptMessage message = result.messages().get(0);
		assertThat(message.role()).isEqualTo(Role.ASSISTANT);
		assertThat(((TextContent) message.content()).text()).isEqualTo("Private prompt content");
	}

	@Test
	void testGetPromptSpecificationsWithStringReturn() {
		class StringReturnPrompt {

			@McpPrompt(name = "string-prompt", description = "Prompt returning string")
			public String stringPrompt() {
				return "Simple string response";
			}

		}

		StringReturnPrompt promptObject = new StringReturnPrompt();
		SyncStatelessMcpPromptProvider provider = new SyncStatelessMcpPromptProvider(List.of(promptObject));

		List<SyncPromptSpecification> promptSpecs = provider.getPromptSpecifications();

		assertThat(promptSpecs).hasSize(1);
		assertThat(promptSpecs.get(0).prompt().name()).isEqualTo("string-prompt");

		// Test that the handler works with string return type
		McpTransportContext context = mock(McpTransportContext.class);
		Map<String, Object> args = new HashMap<>();
		GetPromptRequest request = new GetPromptRequest("string-prompt", args);
		GetPromptResult result = promptSpecs.get(0).promptHandler().apply(context, request);

		assertThat(result).isNotNull();
		assertThat(result.messages()).hasSize(1);
		PromptMessage message = result.messages().get(0);
		assertThat(message.role()).isEqualTo(Role.ASSISTANT);
		assertThat(((TextContent) message.content()).text()).isEqualTo("Simple string response");
	}

	@Test
	void testGetPromptSpecificationsWithContextParameter() {
		class ContextParameterPrompt {

			@McpPrompt(name = "context-prompt", description = "Prompt with context parameter")
			public GetPromptResult contextPrompt(McpTransportContext context, GetPromptRequest request) {
				return new GetPromptResult("Context prompt result",
						List.of(new PromptMessage(Role.ASSISTANT, new TextContent("Prompt with context: "
								+ (context != null ? "present" : "null") + ", name: " + request.name()))));
			}

		}

		ContextParameterPrompt promptObject = new ContextParameterPrompt();
		SyncStatelessMcpPromptProvider provider = new SyncStatelessMcpPromptProvider(List.of(promptObject));

		List<SyncPromptSpecification> promptSpecs = provider.getPromptSpecifications();

		assertThat(promptSpecs).hasSize(1);
		assertThat(promptSpecs.get(0).prompt().name()).isEqualTo("context-prompt");

		// Test that the handler works with context parameter
		McpTransportContext context = mock(McpTransportContext.class);
		Map<String, Object> args = new HashMap<>();
		GetPromptRequest request = new GetPromptRequest("context-prompt", args);
		GetPromptResult result = promptSpecs.get(0).promptHandler().apply(context, request);

		assertThat(result).isNotNull();
		assertThat(result.description()).isEqualTo("Context prompt result");
		assertThat(result.messages()).hasSize(1);
		PromptMessage message = result.messages().get(0);
		assertThat(message.role()).isEqualTo(Role.ASSISTANT);
		assertThat(((TextContent) message.content()).text())
			.isEqualTo("Prompt with context: present, name: context-prompt");
	}

	@Test
	void testGetPromptSpecificationsWithRequestParameter() {
		class RequestParameterPrompt {

			@McpPrompt(name = "request-prompt", description = "Prompt with request parameter")
			public GetPromptResult requestPrompt(GetPromptRequest request) {
				return new GetPromptResult("Request prompt result", List
					.of(new PromptMessage(Role.ASSISTANT, new TextContent("Prompt for name: " + request.name()))));
			}

		}

		RequestParameterPrompt promptObject = new RequestParameterPrompt();
		SyncStatelessMcpPromptProvider provider = new SyncStatelessMcpPromptProvider(List.of(promptObject));

		List<SyncPromptSpecification> promptSpecs = provider.getPromptSpecifications();

		assertThat(promptSpecs).hasSize(1);
		assertThat(promptSpecs.get(0).prompt().name()).isEqualTo("request-prompt");

		// Test that the handler works with request parameter
		McpTransportContext context = mock(McpTransportContext.class);
		Map<String, Object> args = new HashMap<>();
		GetPromptRequest request = new GetPromptRequest("request-prompt", args);
		GetPromptResult result = promptSpecs.get(0).promptHandler().apply(context, request);

		assertThat(result).isNotNull();
		assertThat(result.description()).isEqualTo("Request prompt result");
		assertThat(result.messages()).hasSize(1);
		PromptMessage message = result.messages().get(0);
		assertThat(message.role()).isEqualTo(Role.ASSISTANT);
		assertThat(((TextContent) message.content()).text()).isEqualTo("Prompt for name: request-prompt");
	}

	@Test
	void testGetPromptSpecificationsWithMessagesList() {
		class MessagesListPrompt {

			@McpPrompt(name = "messages-list-prompt", description = "Prompt returning messages list")
			public List<PromptMessage> messagesListPrompt() {
				return List.of(new PromptMessage(Role.ASSISTANT, new TextContent("First message")),
						new PromptMessage(Role.ASSISTANT, new TextContent("Second message")));
			}

		}

		MessagesListPrompt promptObject = new MessagesListPrompt();
		SyncStatelessMcpPromptProvider provider = new SyncStatelessMcpPromptProvider(List.of(promptObject));

		List<SyncPromptSpecification> promptSpecs = provider.getPromptSpecifications();

		assertThat(promptSpecs).hasSize(1);
		assertThat(promptSpecs.get(0).prompt().name()).isEqualTo("messages-list-prompt");

		// Test that the handler works with messages list return type
		McpTransportContext context = mock(McpTransportContext.class);
		Map<String, Object> args = new HashMap<>();
		GetPromptRequest request = new GetPromptRequest("messages-list-prompt", args);
		GetPromptResult result = promptSpecs.get(0).promptHandler().apply(context, request);

		assertThat(result).isNotNull();
		assertThat(result.messages()).hasSize(2);
		assertThat(((TextContent) result.messages().get(0).content()).text()).isEqualTo("First message");
		assertThat(((TextContent) result.messages().get(1).content()).text()).isEqualTo("Second message");
	}

	@Test
	void testGetPromptSpecificationsWithSingleMessage() {
		class SingleMessagePrompt {

			@McpPrompt(name = "single-message-prompt", description = "Prompt returning single message")
			public PromptMessage singleMessagePrompt() {
				return new PromptMessage(Role.ASSISTANT, new TextContent("Single message"));
			}

		}

		SingleMessagePrompt promptObject = new SingleMessagePrompt();
		SyncStatelessMcpPromptProvider provider = new SyncStatelessMcpPromptProvider(List.of(promptObject));

		List<SyncPromptSpecification> promptSpecs = provider.getPromptSpecifications();

		assertThat(promptSpecs).hasSize(1);
		assertThat(promptSpecs.get(0).prompt().name()).isEqualTo("single-message-prompt");

		// Test that the handler works with single message return type
		McpTransportContext context = mock(McpTransportContext.class);
		Map<String, Object> args = new HashMap<>();
		GetPromptRequest request = new GetPromptRequest("single-message-prompt", args);
		GetPromptResult result = promptSpecs.get(0).promptHandler().apply(context, request);

		assertThat(result).isNotNull();
		assertThat(result.messages()).hasSize(1);
		assertThat(((TextContent) result.messages().get(0).content()).text()).isEqualTo("Single message");
	}

	@Test
	void testGetPromptSpecificationsWithStringList() {
		class StringListPrompt {

			@McpPrompt(name = "string-list-prompt", description = "Prompt returning string list")
			public List<String> stringListPrompt() {
				return List.of("First string", "Second string", "Third string");
			}

		}

		StringListPrompt promptObject = new StringListPrompt();
		SyncStatelessMcpPromptProvider provider = new SyncStatelessMcpPromptProvider(List.of(promptObject));

		List<SyncPromptSpecification> promptSpecs = provider.getPromptSpecifications();

		assertThat(promptSpecs).hasSize(1);
		assertThat(promptSpecs.get(0).prompt().name()).isEqualTo("string-list-prompt");

		// Test that the handler works with string list return type
		McpTransportContext context = mock(McpTransportContext.class);
		Map<String, Object> args = new HashMap<>();
		GetPromptRequest request = new GetPromptRequest("string-list-prompt", args);
		GetPromptResult result = promptSpecs.get(0).promptHandler().apply(context, request);

		assertThat(result).isNotNull();
		assertThat(result.messages()).hasSize(3);
		assertThat(((TextContent) result.messages().get(0).content()).text()).isEqualTo("First string");
		assertThat(((TextContent) result.messages().get(1).content()).text()).isEqualTo("Second string");
		assertThat(((TextContent) result.messages().get(2).content()).text()).isEqualTo("Third string");
	}

}
