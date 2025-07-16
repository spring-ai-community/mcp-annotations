/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpSampling;
import org.springaicommunity.mcp.method.sampling.AsyncMcpSamplingMethodCallbackExample;
import org.springaicommunity.mcp.method.sampling.SamlingTestHelper;
import org.springaicommunity.mcp.provider.AsyncMcpSamplingProvider;

import io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Tests for {@link AsyncMcpSamplingProvider}.
 *
 * @author Christian Tzolov
 */
public class AsyncMcpSamplingProviderTests {

	@Test
	void testGetSamplingHandler() {
		// Create a class with only one valid sampling method
		class SingleValidMethod {

			@McpSampling
			public Mono<CreateMessageResult> handleAsyncSamplingRequest(CreateMessageRequest request) {
				return Mono.just(CreateMessageResult.builder()
					.role(io.modelcontextprotocol.spec.McpSchema.Role.ASSISTANT)
					.content(new TextContent("This is an async response to the sampling request"))
					.model("test-model")
					.build());
			}

		}

		SingleValidMethod example = new SingleValidMethod();
		AsyncMcpSamplingProvider provider = new AsyncMcpSamplingProvider(List.of(example));

		Function<CreateMessageRequest, Mono<CreateMessageResult>> handler = provider.getSamplingHandler();

		assertThat(handler).isNotNull();

		CreateMessageRequest request = SamlingTestHelper.createSampleRequest();
		Mono<CreateMessageResult> resultMono = handler.apply(request);

		StepVerifier.create(resultMono).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.content()).isInstanceOf(TextContent.class);
			assertThat(((TextContent) result.content()).text())
				.isEqualTo("This is an async response to the sampling request");
		}).verifyComplete();
	}

	@Test
	void testNullSamplingObjects() {
		assertThatThrownBy(() -> new AsyncMcpSamplingProvider(null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("samplingObjects cannot be null");
	}

	@Test
	void testEmptySamplingObjects() {
		AsyncMcpSamplingProvider provider = new AsyncMcpSamplingProvider(Collections.emptyList());

		assertThatThrownBy(() -> provider.getSamplingHandler()).isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("No sampling methods found");
	}

	@Test
	void testMultipleSamplingMethods() {
		// Create a class with multiple valid sampling methods
		class MultipleSamplingMethods extends AsyncMcpSamplingMethodCallbackExample {

			// This class already has multiple valid sampling methods

		}

		MultipleSamplingMethods example = new MultipleSamplingMethods();
		AsyncMcpSamplingProvider provider = new AsyncMcpSamplingProvider(List.of(example)) {
			@Override
			protected java.lang.reflect.Method[] doGetClassMethods(Object bean) {
				// Override to include methods from both the class and its parent
				return bean.getClass().getMethods();
			}
		};

		assertThatThrownBy(() -> provider.getSamplingHandler()).isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("Multiple sampling methods found");
	}

	@Test
	void testDirectResultMethod() {
		// Create a class with only the direct result method
		class DirectResultOnly {

			@McpSampling
			public CreateMessageResult handleDirectSamplingRequest(CreateMessageRequest request) {
				return CreateMessageResult.builder()
					.role(io.modelcontextprotocol.spec.McpSchema.Role.ASSISTANT)
					.content(new TextContent("This is a direct response to the sampling request"))
					.model("test-model")
					.build();
			}

		}

		DirectResultOnly example = new DirectResultOnly();
		AsyncMcpSamplingProvider provider = new AsyncMcpSamplingProvider(List.of(example));

		Function<CreateMessageRequest, Mono<CreateMessageResult>> handler = provider.getSamplingHandler();

		assertThat(handler).isNotNull();

		CreateMessageRequest request = SamlingTestHelper.createSampleRequest();
		Mono<CreateMessageResult> resultMono = handler.apply(request);

		StepVerifier.create(resultMono).assertNext(result -> {
			assertThat(result).isNotNull();
			assertThat(result.content()).isInstanceOf(TextContent.class);
			assertThat(((TextContent) result.content()).text())
				.isEqualTo("This is a direct response to the sampling request");
		}).verifyComplete();
	}

}
