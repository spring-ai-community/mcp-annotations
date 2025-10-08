/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.context;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.AudioContent;
import io.modelcontextprotocol.spec.McpSchema.ClientCapabilities;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest.ContextInclusionStrategy;
import io.modelcontextprotocol.spec.McpSchema.EmbeddedResource;
import io.modelcontextprotocol.spec.McpSchema.ImageContent;
import io.modelcontextprotocol.spec.McpSchema.Implementation;
import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.ResourceLink;
import io.modelcontextprotocol.spec.McpSchema.SamplingMessage;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.util.Assert;

/**
 * @author Christian Tzolov
 */
public interface McpRequestContextTypes<ET> {

	interface ElicitationSpec {

		ElicitationSpec message(String message);

		ElicitationSpec meta(Map<String, Object> m);

		ElicitationSpec meta(String k, Object v);

	}

	// --------------------------------------
	// Sampling
	// --------------------------------------

	interface ModelPreferenceSpec {

		ModelPreferenceSpec modelHints(String... models);

		ModelPreferenceSpec modelHint(String modelHint);

		ModelPreferenceSpec costPriority(Double costPriority);

		ModelPreferenceSpec speedPriority(Double speedPriority);

		ModelPreferenceSpec intelligencePriority(Double intelligencePriority);

	}

	interface SamplingSpec {

		SamplingSpec message(ResourceLink... content);

		SamplingSpec message(EmbeddedResource... content);

		SamplingSpec message(AudioContent... content);

		SamplingSpec message(ImageContent... content);

		SamplingSpec message(TextContent... content);

		default SamplingSpec message(String... text) {
			return message(List.of(text).stream().map(t -> new TextContent(t)).toList().toArray(new TextContent[0]));
		}

		SamplingSpec message(SamplingMessage... message);

		SamplingSpec modelPreferences(Consumer<ModelPreferenceSpec> modelPreferenceSpec);

		SamplingSpec systemPrompt(String systemPrompt);

		SamplingSpec includeContextStrategy(ContextInclusionStrategy includeContextStrategy);

		SamplingSpec temperature(Double temperature);

		SamplingSpec maxTokens(Integer maxTokens);

		SamplingSpec stopSequences(String... stopSequences);

		SamplingSpec metadata(Map<String, Object> m);

		SamplingSpec metadata(String k, Object v);

		SamplingSpec meta(Map<String, Object> m);

		SamplingSpec meta(String k, Object v);

	}

	// --------------------------------------
	// Progress
	// --------------------------------------

	interface ProgressSpec {

		ProgressSpec progress(double progress);

		ProgressSpec total(double total);

		ProgressSpec message(String message);

		ProgressSpec meta(Map<String, Object> m);

		ProgressSpec meta(String k, Object v);

		default ProgressSpec percentage(int percentage) {
			Assert.isTrue(percentage >= 0 && percentage <= 100, "Percentage must be between 0 and 100");
			return this.progress(percentage).total(100.0);
		}

	}

	// --------------------------------------
	// Logging
	// --------------------------------------

	interface LoggingSpec {

		LoggingSpec message(String message);

		LoggingSpec logger(String logger);

		LoggingSpec level(LoggingLevel level);

		LoggingSpec meta(Map<String, Object> m);

		LoggingSpec meta(String k, Object v);

	}

	// --------------------------------------
	// Getters
	// --------------------------------------
	McpSchema.Request request();

	ET exchange();

	String sessionId();

	Implementation clientInfo();

	ClientCapabilities clientCapabilities();

	// TODO: Should we rename it to meta()?
	Map<String, Object> requestMeta();

	McpTransportContext transportContext();

}
