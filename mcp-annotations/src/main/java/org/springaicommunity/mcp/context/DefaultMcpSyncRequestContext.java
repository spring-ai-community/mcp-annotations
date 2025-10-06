/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.context;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.server.McpSyncServerExchange;
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

/**
 * @author Christian Tzolov
 */
public class DefaultMcpSyncRequestContext implements McpSyncRequestContext {

	private static final Logger logger = LoggerFactory.getLogger(DefaultMcpSyncRequestContext.class);

	private static final Map<Type, Map<String, Object>> typeSchemaCache = new ConcurrentReferenceHashMap<>(256);

	private static TypeReference<Map<String, Object>> MAP_TYPE_REF = new TypeReference<Map<String, Object>>() {
	};

	private final McpSchema.Request request;

	private final McpSyncServerExchange exchange;

	private DefaultMcpSyncRequestContext(McpSchema.Request request, McpSyncServerExchange exchange) {
		Assert.notNull(request, "Request must not be null");
		Assert.notNull(exchange, "Exchange must not be null");
		this.request = request;
		this.exchange = exchange;
	}

	// Roots

	public Optional<ListRootsResult> roots() {
		if (this.exchange.getClientCapabilities() == null || this.exchange.getClientCapabilities().roots() == null) {
			logger.warn("Roots not supported by the client! Ignoring the roots request for request:" + this.request);
			return Optional.empty();
		}
		return Optional.of(this.exchange.listRoots());
	}

	// Elicitation

	@Override
	public <T> Optional<StructuredElicitResult<T>> elicit(Class<T> type) {
		Assert.notNull(type, "Elicitation response type must not be null");

		Optional<ElicitResult> elicitResult = this.elicitationInternal("Please provide the required information.", type,
				null);

		if (!elicitResult.isPresent() || elicitResult.get().action() != ElicitResult.Action.ACCEPT) {
			return Optional.empty();
		}

		return Optional.of(new StructuredElicitResult<>(elicitResult.get().action(),
				convertMapToType(elicitResult.get().content(), type), elicitResult.get().meta()));
	}

	@Override
	public <T> Optional<StructuredElicitResult<T>> elicit(TypeReference<T> type) {
		Assert.notNull(type, "Elicitation response type must not be null");

		Optional<ElicitResult> elicitResult = this.elicitationInternal("Please provide the required information.",
				type.getType(), null);

		if (!elicitResult.isPresent() || elicitResult.get().action() != ElicitResult.Action.ACCEPT) {
			return Optional.empty();
		}

		return Optional.of(new StructuredElicitResult<>(elicitResult.get().action(),
				convertMapToType(elicitResult.get().content(), type), elicitResult.get().meta()));
	}

	@Override
	public <T> Optional<StructuredElicitResult<T>> elicit(Consumer<ElicitationSpec> params, Class<T> returnType) {
		Assert.notNull(returnType, "Elicitation response type must not be null");
		Assert.notNull(params, "Elicitation params must not be null");

		DefaultElicitationSpec paramSpec = new DefaultElicitationSpec();
		params.accept(paramSpec);

		Optional<ElicitResult> elicitResult = this.elicitationInternal(paramSpec.message(), returnType,
				paramSpec.meta());

		if (!elicitResult.isPresent() || elicitResult.get().action() != ElicitResult.Action.ACCEPT) {
			return Optional.empty();
		}

		return Optional.of(new StructuredElicitResult<>(elicitResult.get().action(),
				convertMapToType(elicitResult.get().content(), returnType), elicitResult.get().meta()));
	}

	@Override
	public <T> Optional<StructuredElicitResult<T>> elicit(Consumer<ElicitationSpec> params,
			TypeReference<T> returnType) {
		Assert.notNull(returnType, "Elicitation response type must not be null");
		Assert.notNull(params, "Elicitation params must not be null");

		DefaultElicitationSpec paramSpec = new DefaultElicitationSpec();
		params.accept(paramSpec);

		Optional<ElicitResult> elicitResult = this.elicitationInternal(paramSpec.message(), returnType.getType(),
				paramSpec.meta());

		if (!elicitResult.isPresent() || elicitResult.get().action() != ElicitResult.Action.ACCEPT) {
			return Optional.empty();
		}

		return Optional.of(new StructuredElicitResult<>(elicitResult.get().action(),
				convertMapToType(elicitResult.get().content(), returnType), elicitResult.get().meta()));
	}

	@Override
	public Optional<ElicitResult> elicit(ElicitRequest elicitRequest) {
		Assert.notNull(elicitRequest, "Elicit request must not be null");

		if (this.exchange.getClientCapabilities() == null
				|| this.exchange.getClientCapabilities().elicitation() == null) {
			logger.warn("Elicitation not supported by the client! Ignoring the elicitation request for request:"
					+ elicitRequest);
			return Optional.empty();
		}

		ElicitResult elicitResult = this.exchange.createElicitation(elicitRequest);

		return Optional.of(elicitResult);
	}

	private Optional<ElicitResult> elicitationInternal(String message, Type type, Map<String, Object> meta) {
		Assert.hasText(message, "Elicitation message must not be empty");
		Assert.notNull(type, "Elicitation response type must not be null");

		Map<String, Object> schema = typeSchemaCache.computeIfAbsent(type, t -> this.generateElicitSchema(t));

		return this.elicit(ElicitRequest.builder().message(message).requestedSchema(schema).meta(meta).build());
	}

	private Map<String, Object> generateElicitSchema(Type type) {
		Map<String, Object> schema = JsonParser.fromJson(JsonSchemaGenerator.generateFromType(type), MAP_TYPE_REF);
		// remove $schema as elicitation schema does not support it
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
	public Optional<CreateMessageResult> sample(String... messages) {
		return this.sample(s -> s.message(messages));
	}

	@Override
	public Optional<CreateMessageResult> sample(Consumer<SamplingSpec> samplingSpec) {
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
	public Optional<CreateMessageResult> sample(CreateMessageRequest createMessageRequest) {

		// check if supported
		if (this.exchange.getClientCapabilities() == null || this.exchange.getClientCapabilities().sampling() == null) {
			logger.warn("Sampling not supported by the client! Ignoring the sampling request for messages:"
					+ createMessageRequest);
			return Optional.empty();
		}

		return Optional.of(this.exchange.createMessage(createMessageRequest));
	}

	// Progress

	@Override
	public void progress(int percentage) {
		Assert.isTrue(percentage >= 0 && percentage <= 100, "Percentage must be between 0 and 100");
		this.progress(p -> p.progress(percentage / 100.0).total(1.0).message(null));
	}

	@Override
	public void progress(Consumer<ProgressSpec> progressSpec) {

		Assert.notNull(progressSpec, "Progress spec consumer must not be null");
		DefaultProgressSpec spec = new DefaultProgressSpec();

		progressSpec.accept(spec);

		if (!Utils.hasText(this.request.progressToken())) {
			logger.warn("Progress notification not supported by the client!");
			return;
		}

		this.progress(new ProgressNotification(this.request.progressToken(), spec.progress, spec.total, spec.message,
				spec.meta));
	}

	@Override
	public void progress(ProgressNotification progressNotification) {
		this.exchange.progressNotification(progressNotification);
	}

	// Ping

	@Override
	public void ping() {
		this.exchange.ping();
	}

	// Logging

	@Override
	public void log(Consumer<LoggingSpec> logSpec) {
		Assert.notNull(logSpec, "Logging spec consumer must not be null");
		DefaultLoggingSpec spec = new DefaultLoggingSpec();
		logSpec.accept(spec);

		this.exchange.loggingNotification(LoggingMessageNotification.builder()
			.data(spec.message)
			.level(spec.level)
			.logger(spec.logger)
			.meta(spec.meta)
			.build());
	}

	@Override
	public void debug(String message) {
		this.logInternal(message, LoggingLevel.DEBUG);
	}

	@Override
	public void info(String message) {
		this.logInternal(message, LoggingLevel.INFO);
	}

	@Override
	public void warn(String message) {
		this.logInternal(message, LoggingLevel.WARNING);
	}

	@Override
	public void error(String message) {
		this.logInternal(message, LoggingLevel.ERROR);
	}

	private void logInternal(String message, LoggingLevel level) {
		Assert.hasText(message, "Log message must not be empty");
		this.exchange.loggingNotification(LoggingMessageNotification.builder().data(message).level(level).build());
	}

	// Getters

	@Override
	public McpSchema.Request request() {
		return this.request;
	}

	@Override
	public McpSyncServerExchange exchange() {
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

		private McpSyncServerExchange exchange;

		private McpTransportContext transportContext;

		private boolean isStateless = false;

		private Builder() {
		}

		public Builder request(McpSchema.Request request) {
			this.request = request;
			return this;
		}

		public Builder exchange(McpSyncServerExchange exchange) {
			this.exchange = exchange;
			return this;
		}

		public Builder transportContext(McpTransportContext transportContext) {
			this.transportContext = transportContext;
			return this;
		}

		public Builder stateless(boolean isStateless) {
			this.isStateless = isStateless;
			return this;
		}

		public McpSyncRequestContext build() {
			if (this.isStateless) {
				return new StatelessMcpSyncRequestContext(this.request, this.transportContext);
			}
			return new DefaultMcpSyncRequestContext(this.request, this.exchange);
		}

	}

	public final static class StatelessMcpSyncRequestContext implements McpSyncRequestContext {

		private static final Logger logger = LoggerFactory.getLogger(StatelessMcpSyncRequestContext.class);

		private final McpSchema.Request request;

		private final McpTransportContext transportContext;

		private StatelessMcpSyncRequestContext(McpSchema.Request request, McpTransportContext transportContext) {
			this.request = request;
			this.transportContext = transportContext;
		}

		@Override
		public Optional<ListRootsResult> roots() {
			logger.warn("Roots not supported by the client! Ignoring the roots request");
			return Optional.empty();
		}

		@Override
		public <T> Optional<StructuredElicitResult<T>> elicit(Class<T> type) {
			logger.warn("Stateless servers do not support elicitation! Ignoring the elicitation request");
			return Optional.empty();
		}

		@Override
		public <T> Optional<StructuredElicitResult<T>> elicit(Consumer<ElicitationSpec> params, Class<T> returnType) {
			logger.warn("Stateless servers do not support elicitation! Ignoring the elicitation request");
			return Optional.empty();
		}

		@Override
		public <T> Optional<StructuredElicitResult<T>> elicit(TypeReference<T> type) {
			logger.warn("Stateless servers do not support elicitation! Ignoring the elicitation request");
			return Optional.empty();
		}

		@Override
		public <T> Optional<StructuredElicitResult<T>> elicit(Consumer<ElicitationSpec> params,
				TypeReference<T> returnType) {
			logger.warn("Stateless servers do not support elicitation! Ignoring the elicitation request");
			return Optional.empty();
		}

		@Override
		public Optional<ElicitResult> elicit(ElicitRequest elicitRequest) {
			logger.warn("Stateless servers do not support elicitation! Ignoring the elicitation request");
			return Optional.empty();
		}

		@Override
		public Optional<CreateMessageResult> sample(String... messages) {
			logger.warn("Stateless servers do not support sampling! Ignoring the sampling request");
			return Optional.empty();
		}

		@Override
		public Optional<CreateMessageResult> sample(Consumer<SamplingSpec> samplingSpec) {
			logger.warn("Stateless servers do not support sampling! Ignoring the sampling request");
			return Optional.empty();
		}

		@Override
		public Optional<CreateMessageResult> sample(CreateMessageRequest createMessageRequest) {
			logger.warn("Stateless servers do not support sampling! Ignoring the sampling request");
			return Optional.empty();
		}

		@Override
		public void progress(int progress) {
			logger.warn("Stateless servers do not support progress notifications! Ignoring the progress request");
		}

		@Override
		public void progress(Consumer<ProgressSpec> progressSpec) {
			logger.warn("Stateless servers do not support progress notifications! Ignoring the progress request");
		}

		@Override
		public void progress(ProgressNotification progressNotification) {
			logger.warn("Stateless servers do not support progress notifications! Ignoring the progress request");
		}

		@Override
		public void ping() {
			logger.warn("Stateless servers do not support ping! Ignoring the ping request");
		}

		@Override
		public void log(Consumer<LoggingSpec> logSpec) {
			logger.warn("Stateless servers do not support logging! Ignoring the logging request");
		}

		@Override
		public void debug(String message) {
			logger.warn("Stateless servers do not support debugging! Ignoring the debugging request");
		}

		@Override
		public void info(String message) {
			logger.warn("Stateless servers do not support info logging! Ignoring the info request");
		}

		@Override
		public void warn(String message) {
			logger.warn("Stateless servers do not support warning logging! Ignoring the warning request");
		}

		@Override
		public void error(String message) {
			logger.warn("Stateless servers do not support error logging! Ignoring the error request");
		}

		public McpSchema.Request request() {
			return this.request;
		}

		public McpTransportContext transportContext() {
			return transportContext;
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

		@Override
		public McpSyncServerExchange exchange() {
			logger.warn("Stateless servers do not support exchange! Returning null");
			return null;
		}

	}

}
