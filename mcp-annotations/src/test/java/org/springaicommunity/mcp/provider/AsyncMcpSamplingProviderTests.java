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
import org.springaicommunity.mcp.method.sampling.AsyncSamplingSpecification;
import org.springaicommunity.mcp.method.sampling.SamlingTestHelper;

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

		List<AsyncSamplingSpecification> samplingSpecs = provider.getSamplingSpecifictions();

		Function<CreateMessageRequest, Mono<CreateMessageResult>> handler = samplingSpecs.get(0).samplingHandler();

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

		List<AsyncSamplingSpecification> samplingSpecs = provider.getSamplingSpecifictions();

		Function<CreateMessageRequest, Mono<CreateMessageResult>> handler = samplingSpecs.get(0).samplingHandler();

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
