/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.context;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.ClientCapabilities;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest;
import io.modelcontextprotocol.spec.McpSchema.CreateMessageResult;
import io.modelcontextprotocol.spec.McpSchema.ElicitRequest;
import io.modelcontextprotocol.spec.McpSchema.ElicitResult;
import io.modelcontextprotocol.spec.McpSchema.Implementation;
import io.modelcontextprotocol.spec.McpSchema.ListRootsResult;
import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import io.modelcontextprotocol.spec.McpSchema.ProgressNotification;
import io.modelcontextprotocol.util.Assert;
import io.modelcontextprotocol.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.method.tool.utils.ConcurrentReferenceHashMap;
import org.springaicommunity.mcp.method.tool.utils.JsonParser;
import org.springaicommunity.mcp.method.tool.utils.JsonSchemaGenerator;
import reactor.core.publisher.Mono;

/**
 * Async (Reactor) implementation of McpAsyncRequestContext that returns Mono of value
 * types.
 *
 * @author Christian Tzolov
 */
public class DefaultMcpAsyncRequestContext implements McpAsyncRequestContext {

	private static final Logger logger = LoggerFactory.getLogger(DefaultMcpAsyncRequestContext.class);

	private static final Map<Type, Map<String, Object>> typeSchemaCache = new ConcurrentReferenceHashMap<>(256);

	private static TypeReference<Map<String, Object>> MAP_TYPE_REF = new TypeReference<Map<String, Object>>() {
	};

	private final McpSchema.Request request;

	private final McpAsyncServerExchange exchange;

	private DefaultMcpAsyncRequestContext(McpSchema.Request request, McpAsyncServerExchange exchange) {
		Assert.notNull(request, "Request must not be null");
		Assert.notNull(exchange, "Exchange must not be null");
		this.request = request;
		this.exchange = exchange;
	}

	// Roots

	@Override
	public Mono<ListRootsResult> roots() {
		if (this.exchange.getClientCapabilities() == null || this.exchange.getClientCapabilities().roots() == null) {
			logger.warn("Roots not supported by the client! Ignoring the roots request for request:" + this.request);
			return Mono.empty();
		}
		return this.exchange.listRoots();
	}

	// Elicitation

	@Override
	public <T> Mono<StructuredElicitResult<T>> elicit(Consumer<ElicitationSpec> spec, TypeReference<T> type) {
		Assert.notNull(type, "Elicitation response type must not be null");
		Assert.notNull(spec, "Elicitation spec consumer must not be null");
		DefaultElicitationSpec elicitationSpec = new DefaultElicitationSpec();
		spec.accept(elicitationSpec);
		return this.elicitationInternal(elicitationSpec.message, type.getType(), elicitationSpec.meta)
			.map(er -> new StructuredElicitResult<T>(er.action(), convertMapToType(er.content(), type), er.meta()));
	}

	@Override
	public <T> Mono<StructuredElicitResult<T>> elicit(Consumer<ElicitationSpec> spec, Class<T> type) {
		Assert.notNull(type, "Elicitation response type must not be null");
		Assert.notNull(spec, "Elicitation spec consumer must not be null");
		DefaultElicitationSpec elicitationSpec = new DefaultElicitationSpec();
		spec.accept(elicitationSpec);
		return this.elicitationInternal(elicitationSpec.message, type, elicitationSpec.meta)
			.map(er -> new StructuredElicitResult<T>(er.action(), convertMapToType(er.content(), type), er.meta()));
	}

	@Override
	public <T> Mono<StructuredElicitResult<T>> elicit(TypeReference<T> type) {
		Assert.notNull(type, "Elicitation response type must not be null");
		return this.elicitationInternal("Please provide the required information.", type.getType(), null)
			.map(er -> new StructuredElicitResult<T>(er.action(), convertMapToType(er.content(), type), er.meta()));
	}

	@Override
	public <T> Mono<StructuredElicitResult<T>> elicit(Class<T> type) {
		Assert.notNull(type, "Elicitation response type must not be null");
		return this.elicitationInternal("Please provide the required information.", type, null)
			.map(er -> new StructuredElicitResult<T>(er.action(), convertMapToType(er.content(), type), er.meta()));
	}

	@Override
	public Mono<ElicitResult> elicit(ElicitRequest elicitRequest) {
		Assert.notNull(elicitRequest, "Elicit request must not be null");

		if (this.exchange.getClientCapabilities() == null
				|| this.exchange.getClientCapabilities().elicitation() == null) {
			logger.warn("Elicitation not supported by the client! Ignoring the elicitation request for request:"
					+ elicitRequest);
			return Mono.empty();
		}

		return this.exchange.createElicitation(elicitRequest);
	}

	public Mono<ElicitResult> elicitationInternal(String message, Type type, Map<String, Object> meta) {
		Assert.hasText(message, "Elicitation message must not be empty");
		Assert.notNull(type, "Elicitation response type must not be null");

		Map<String, Object> schema = typeSchemaCache.computeIfAbsent(type, t -> this.generateElicitSchema(t));

		return this.elicit(ElicitRequest.builder().message(message).requestedSchema(schema).meta(meta).build());
	}

	private Map<String, Object> generateElicitSchema(Type type) {
		Map<String, Object> schema = JsonParser.fromJson(JsonSchemaGenerator.generateFromType(type), MAP_TYPE_REF);
		// remove as elicitation schema does not support it
		schema.remove("$schema");
		return schema;
	}

	private static <T> T convertMapToType(Map<String, Object> map, Class<T> targetType) {
		ObjectMapper mapper = new ObjectMapper();
		JavaType javaType = mapper.getTypeFactory().constructType(targetType);
		return mapper.convertValue(map, javaType);
	}

	private static <T> T convertMapToType(Map<String, Object> map, TypeReference<T> targetType) {
		ObjectMapper mapper = new ObjectMapper();
		JavaType javaType = mapper.getTypeFactory().constructType(targetType);
		return mapper.convertValue(map, javaType);
	}

	// Sampling

	@Override
	public Mono<CreateMessageResult> sample(String... messages) {
		return this.sample(s -> s.message(messages));
	}

	@Override
	public Mono<CreateMessageResult> sample(Consumer<SamplingSpec> samplingSpec) {
		Assert.notNull(samplingSpec, "Sampling spec consumer must not be null");
		DefaultSamplingSpec spec = new DefaultSamplingSpec();
		samplingSpec.accept(spec);

		var progressToken = this.request.progressToken();

		if (!Utils.hasText(progressToken)) {
			logger.warn("Progress notification not supported by the client!");
		}
		return this.sample(McpSchema.CreateMessageRequest.builder()
			.messages(spec.messages)
			.modelPreferences(spec.modelPreferences)
			.systemPrompt(spec.systemPrompt)
			.temperature(spec.temperature)
			.maxTokens(spec.maxTokens != null && spec.maxTokens > 0 ? spec.maxTokens : 500)
			.stopSequences(spec.stopSequences.isEmpty() ? null : spec.stopSequences)
			.includeContext(spec.includeContextStrategy)
			.meta(spec.metadata.isEmpty() ? null : spec.metadata)
			.progressToken(progressToken)
			.meta(spec.meta.isEmpty() ? null : spec.meta)
			.build());
	}

	@Override
	public Mono<CreateMessageResult> sample(CreateMessageRequest createMessageRequest) {

		// check if supported
		if (this.exchange.getClientCapabilities() == null || this.exchange.getClientCapabilities().sampling() == null) {
			logger.warn("Sampling not supported by the client! Ignoring the sampling request for messages:"
					+ createMessageRequest);
			return Mono.empty();
		}

		return this.exchange.createMessage(createMessageRequest);
	}

	// Progress

	@Override
	public Mono<Void> progress(int percentage) {
		Assert.isTrue(percentage >= 0 && percentage <= 100, "Percentage must be between 0 and 100");
		return this.progress(p -> p.progress(percentage / 100.0).total(1.0).message(null));
	}

	@Override
	public Mono<Void> progress(Consumer<ProgressSpec> progressSpec) {

		Assert.notNull(progressSpec, "Progress spec consumer must not be null");
		DefaultProgressSpec spec = new DefaultProgressSpec();

		progressSpec.accept(spec);

		if (!Utils.hasText(this.request.progressToken())) {
			logger.warn("Progress notification not supported by the client!");
			return Mono.empty();
		}

		return this.progress(new ProgressNotification(this.request.progressToken(), spec.progress, spec.total,
				spec.message, spec.meta));
	}

	@Override
	public Mono<Void> progress(ProgressNotification progressNotification) {
		return this.exchange.progressNotification(progressNotification).then(Mono.<Void>empty());
	}

	// Ping

	@Override
	public Mono<Object> ping() {
		return this.exchange.ping();
	}

	// Logging

	@Override
	public Mono<Void> log(Consumer<LoggingSpec> logSpec) {
		Assert.notNull(logSpec, "Logging spec consumer must not be null");
		DefaultLoggingSpec spec = new DefaultLoggingSpec();
		logSpec.accept(spec);

		return this.exchange
			.loggingNotification(LoggingMessageNotification.builder()
				.data(spec.message)
				.level(spec.level)
				.logger(spec.logger)
				.meta(spec.meta)
				.build())
			.then();
	}

	@Override
	public Mono<Void> debug(String message) {
		return this.logInternal(message, LoggingLevel.DEBUG);
	}

	@Override
	public Mono<Void> info(String message) {
		return this.logInternal(message, LoggingLevel.INFO);
	}

	@Override
	public Mono<Void> warn(String message) {
		return this.logInternal(message, LoggingLevel.WARNING);
	}

	@Override
	public Mono<Void> error(String message) {
		return this.logInternal(message, LoggingLevel.ERROR);
	}

	private Mono<Void> logInternal(String message, LoggingLevel level) {
		Assert.hasText(message, "Log message must not be empty");
		return this.exchange
			.loggingNotification(LoggingMessageNotification.builder().data(message).level(level).build())
			.then();
	}

	// Getters

	@Override
	public McpSchema.Request request() {
		return this.request;
	}

	@Override
	public McpAsyncServerExchange exchange() {
		return this.exchange;
	}

	@Override
	public String sessionId() {
		return this.exchange.sessionId();
	}

	@Override
	public Implementation clientInfo() {
		return this.exchange.getClientInfo();
	}

	@Override
	public ClientCapabilities clientCapabilities() {
		return this.exchange.getClientCapabilities();
	}

	@Override
	public Map<String, Object> requestMeta() {
		return this.request.meta();
	}

	@Override
	public McpTransportContext transportContext() {
		return this.exchange.transportContext();
	}

	// Builder

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private McpSchema.Request request;

		private McpAsyncServerExchange exchange;

		private boolean isStateless = false;

		private McpTransportContext transportContext;

		private Builder() {
		}

		public Builder request(McpSchema.Request request) {
			this.request = request;
			return this;
		}

		public Builder exchange(McpAsyncServerExchange exchange) {
			this.exchange = exchange;
			return this;
		}

		public Builder stateless(boolean isStateless) {
			this.isStateless = isStateless;
			return this;
		}

		public Builder transportContext(McpTransportContext transportContext) {
			this.transportContext = transportContext;
			return this;
		}

		public McpAsyncRequestContext build() {
			if (this.isStateless) {
				return new StatelessAsyncRequestContext(this.request, this.transportContext);
			}
			return new DefaultMcpAsyncRequestContext(this.request, this.exchange);
		}

	}

	private static class StatelessAsyncRequestContext implements McpAsyncRequestContext {

		private final McpSchema.Request request;

		private McpTransportContext transportContext;

		public StatelessAsyncRequestContext(McpSchema.Request request, McpTransportContext transportContext) {
			this.request = request;
			this.transportContext = transportContext;
		}

		@Override
		public Mono<ListRootsResult> roots() {
			logger.warn("Roots not supported by the client! Ignoring the roots request");
			return Mono.empty();
		}

		@Override
		public <T> Mono<StructuredElicitResult<T>> elicit(Consumer<ElicitationSpec> spec, TypeReference<T> returnType) {
			logger.warn("Elicitation not supported by the client! Ignoring the elicitation request");
			return Mono.empty();
		}

		@Override
		public <T> Mono<StructuredElicitResult<T>> elicit(TypeReference<T> type) {
			logger.warn("Elicitation not supported by the client! Ignoring the elicitation request");
			return Mono.empty();
		}

		@Override
		public <T> Mono<StructuredElicitResult<T>> elicit(Consumer<ElicitationSpec> spec, Class<T> returnType) {
			logger.warn("Elicitation not supported by the client! Ignoring the elicitation request");
			return Mono.empty();
		}

		@Override
		public <T> Mono<StructuredElicitResult<T>> elicit(Class<T> type) {
			logger.warn("Elicitation not supported by the client! Ignoring the elicitation request");
			return Mono.empty();
		}

		@Override
		public Mono<ElicitResult> elicit(ElicitRequest elicitRequest) {
			logger.warn("Elicitation not supported by the client! Ignoring the elicitation request");
			return Mono.empty();
		}

		@Override
		public Mono<CreateMessageResult> sample(String... messages) {
			logger.warn("Sampling not supported by the client! Ignoring the sampling request");
			return Mono.empty();
		}

		@Override
		public Mono<CreateMessageResult> sample(Consumer<SamplingSpec> samplingSpec) {
			logger.warn("Sampling not supported by the client! Ignoring the sampling request");
			return Mono.empty();
		}

		@Override
		public Mono<CreateMessageResult> sample(CreateMessageRequest createMessageRequest) {
			logger.warn("Sampling not supported by the client! Ignoring the sampling request");
			return Mono.empty();
		}

		@Override
		public Mono<Void> progress(int progress) {
			logger.warn("Progress not supported by the client! Ignoring the progress request");
			return Mono.empty();
		}

		@Override
		public Mono<Void> progress(Consumer<ProgressSpec> progressSpec) {
			logger.warn("Progress not supported by the client! Ignoring the progress request");
			return Mono.empty();
		}

		@Override
		public Mono<Void> progress(ProgressNotification progressNotification) {
			logger.warn("Progress not supported by the client! Ignoring the progress request");
			return Mono.empty();
		}

		@Override
		public Mono<Object> ping() {
			logger.warn("Ping not supported by the client! Ignoring the ping request");
			return Mono.empty();
		}

		@Override
		public Mono<Void> log(Consumer<LoggingSpec> logSpec) {
			logger.warn("Logging not supported by the client! Ignoring the logging request");
			return Mono.empty();
		}

		@Override
		public Mono<Void> debug(String message) {
			logger.warn("Debug not supported by the client! Ignoring the debug request");
			return Mono.empty();
		}

		@Override
		public Mono<Void> info(String message) {
			logger.warn("Info not supported by the client! Ignoring the info request");
			return Mono.empty();
		}

		@Override
		public Mono<Void> warn(String message) {
			logger.warn("Warn not supported by the client! Ignoring the warn request");
			return Mono.empty();
		}

		@Override
		public Mono<Void> error(String message) {
			logger.warn("Error not supported by the client! Ignoring the error request");
			return Mono.empty();
		}

		// Getters

		public McpSchema.Request request() {
			return this.request;
		}

		public McpAsyncServerExchange exchange() {
			logger.warn("Stateless servers do not support exchange! Returning null");
			return null;
		}

		public String sessionId() {
			logger.warn("Stateless servers do not support session ID! Returning null");
			return null;
		}

		public Implementation clientInfo() {
			logger.warn("Stateless servers do not support client info! Returning null");
			return null;
		}

		public ClientCapabilities clientCapabilities() {
			logger.warn("Stateless servers do not support client capabilities! Returning null");
			return null;
		}

		public Map<String, Object> requestMeta() {
			return this.request.meta();
		}

		public McpTransportContext transportContext() {
			return transportContext;
		}

	}

}
