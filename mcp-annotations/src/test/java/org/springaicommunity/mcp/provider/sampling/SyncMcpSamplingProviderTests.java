/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.provider.sampling;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.annotation.McpSampling;
import org.springaicommunity.mcp.method.sampling.SamlingTestHelper;
import org.springaicommunity.mcp.method.sampling.SyncSamplingSpecification;

import io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;

/**
 * Tests for {@link SyncMcpSamplingProvider}.
 *
 * @author Christian Tzolov
 */
public class SyncMcpSamplingProviderTests {

	@Test
	void testGetSamplingHandler() {
		// Create a class with only one valid sampling method
		class SingleValidMethod {

			@McpSampling
			public CreateMessageResult handleSamplingRequest(CreateMessageRequest request) {
				return CreateMessageResult.builder()
					.role(io.modelcontextprotocol.spec.McpSchema.Role.ASSISTANT)
					.content(new TextContent("This is a response to the sampling request"))
					.model("test-model")
					.build();
			}

		}

		SingleValidMethod example = new SingleValidMethod();
		SyncMcpSamplingProvider provider = new SyncMcpSamplingProvider(List.of(example));

		List<SyncSamplingSpecification> samplingSpecs = provider.getSamplingSpecifications();

		Function<CreateMessageRequest, CreateMessageResult> handler = samplingSpecs.get(0).samplingHandler();

		assertThat(handler).isNotNull();

		CreateMessageRequest request = SamlingTestHelper.createSampleRequest();
		CreateMessageResult result = handler.apply(request);

		assertThat(result).isNotNull();
		assertThat(result.content()).isInstanceOf(TextContent.class);
		assertThat(((TextContent) result.content()).text()).isEqualTo("This is a response to the sampling request");
	}

	@Test
	void testNullSamplingObjects() {
		assertThatThrownBy(() -> new SyncMcpSamplingProvider(null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("samplingObjects cannot be null");
	}

}
