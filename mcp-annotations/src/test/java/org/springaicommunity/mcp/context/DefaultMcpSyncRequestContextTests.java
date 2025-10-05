/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.context;

import java.util.Map;
import java.util.Optional;

import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
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
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.SamplingMessage;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link DefaultMcpSyncRequestContext}.
 *
 * @author Christian Tzolov
 */
public class DefaultMcpSyncRequestContextTests {

	private CallToolRequest request;

	private McpSyncServerExchange exchange;

	private McpSyncRequestContext context;

	@BeforeEach
	public void setUp() {
		request = new CallToolRequest("test-tool", Map.of());
		exchange = mock(McpSyncServerExchange.class);
		context = DefaultMcpSyncRequestContext.builder().request(request).exchange(exchange).build();
	}

	// Builder Tests

	@Test
	public void testBuilderWithValidParameters() {
		CallToolRequest testRequest = new CallToolRequest("test-tool", Map.of());
		McpSyncRequestContext ctx = DefaultMcpSyncRequestContext.builder()
			.request(testRequest)
			.exchange(exchange)
			.build();

		assertThat(ctx).isNotNull();
		assertThat(ctx.request()).isEqualTo(testRequest);
		assertThat(ctx.exchange()).isEqualTo(exchange);
	}

	@Test
	public void testBuilderWithNullRequest() {
		assertThatThrownBy(() -> DefaultMcpSyncRequestContext.builder().request(null).exchange(exchange).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Request must not be null");
	}

	@Test
	public void testBuilderWithNullExchange() {
		CallToolRequest testRequest = new CallToolRequest("test-tool", Map.of());
		assertThatThrownBy(() -> DefaultMcpSyncRequestContext.builder().request(testRequest).exchange(null).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Exchange must not be null");
	}

	// Roots Tests

	@Test
	public void testRootsWhenSupported() {
		ClientCapabilities capabilities = mock(ClientCapabilities.class);
		McpSchema.ClientCapabilities.RootCapabilities roots = mock(McpSchema.ClientCapabilities.RootCapabilities.class);
		when(capabilities.roots()).thenReturn(roots);
		when(exchange.getClientCapabilities()).thenReturn(capabilities);

		ListRootsResult expectedResult = mock(ListRootsResult.class);
		when(exchange.listRoots()).thenReturn(expectedResult);

		Optional<ListRootsResult> result = context.roots();

		assertThat(result).isPresent();
		assertThat(result.get()).isEqualTo(expectedResult);
		verify(exchange).listRoots();
	}

	@Test
	public void testRootsWhenNotSupported() {
		when(exchange.getClientCapabilities()).thenReturn(null);

		Optional<ListRootsResult> result = context.roots();

		assertThat(result).isEmpty();
	}

	@Test
	public void testRootsWhenCapabilitiesNullRoots() {
		ClientCapabilities capabilities = mock(ClientCapabilities.class);
		when(capabilities.roots()).thenReturn(null);
		when(exchange.getClientCapabilities()).thenReturn(capabilities);

		Optional<ListRootsResult> result = context.roots();

		assertThat(result).isEmpty();
	}

	// Elicitation Tests

	@Test
	public void testElicitationWithConsumer() {
		ClientCapabilities capabilities = mock(ClientCapabilities.class);
		ClientCapabilities.Elicitation elicitation = mock(ClientCapabilities.Elicitation.class);
		when(capabilities.elicitation()).thenReturn(elicitation);
		when(exchange.getClientCapabilities()).thenReturn(capabilities);

		ElicitResult expectedResult = mock(ElicitResult.class);
		when(exchange.createElicitation(any(ElicitRequest.class))).thenReturn(expectedResult);

		Optional<ElicitResult> result = context.elicitation(spec -> {
			spec.message("Test message");
			spec.responseType(String.class);
		});

		assertThat(result).isPresent();
		assertThat(result.get()).isEqualTo(expectedResult);

		ArgumentCaptor<ElicitRequest> captor = ArgumentCaptor.forClass(ElicitRequest.class);
		verify(exchange).createElicitation(captor.capture());

		ElicitRequest capturedRequest = captor.getValue();
		assertThat(capturedRequest.message()).isEqualTo("Test message");
		assertThat(capturedRequest.requestedSchema()).isNotNull();
	}

	@Test
	public void testElicitationWithConsumerAndMeta() {
		ClientCapabilities capabilities = mock(ClientCapabilities.class);
		ClientCapabilities.Elicitation elicitation = mock(ClientCapabilities.Elicitation.class);
		when(capabilities.elicitation()).thenReturn(elicitation);
		when(exchange.getClientCapabilities()).thenReturn(capabilities);

		ElicitResult expectedResult = mock(ElicitResult.class);
		when(exchange.createElicitation(any(ElicitRequest.class))).thenReturn(expectedResult);

		Optional<ElicitResult> result = context.elicitation(spec -> {
			spec.message("Test message");
			spec.responseType(String.class);
			spec.meta("key", "value");
		});

		assertThat(result).isPresent();

		ArgumentCaptor<ElicitRequest> captor = ArgumentCaptor.forClass(ElicitRequest.class);
		verify(exchange).createElicitation(captor.capture());

		ElicitRequest capturedRequest = captor.getValue();
		assertThat(capturedRequest.meta()).containsEntry("key", "value");
	}

	@Test
	public void testElicitationWithNullConsumer() {
		assertThatThrownBy(
				() -> context.elicitation((java.util.function.Consumer<McpSyncRequestContext.ElicitationSpec>) null))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Elicitation spec consumer must not be null");
	}

	@Test
	public void testElicitationWithEmptyMessage() {
		assertThatThrownBy(() -> context.elicitation(spec -> {
			spec.message("");
			spec.responseType(String.class);
		})).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Message must not be empty");
	}

	@Test
	public void testElicitationWithNullResponseType() {
		assertThatThrownBy(() -> context.elicitation(spec -> {
			spec.message("Test message");
			spec.responseType(null);
		})).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("Response type must not be null");
	}

	@Test
	public void testElicitationWithMessageAndType() {
		ClientCapabilities capabilities = mock(ClientCapabilities.class);
		ClientCapabilities.Elicitation elicitation = mock(ClientCapabilities.Elicitation.class);
		when(capabilities.elicitation()).thenReturn(elicitation);
		when(exchange.getClientCapabilities()).thenReturn(capabilities);

		ElicitResult expectedResult = mock(ElicitResult.class);
		when(exchange.createElicitation(any(ElicitRequest.class))).thenReturn(expectedResult);

		Optional<ElicitResult> result = context.elicitation("Test message", String.class);

		assertThat(result).isPresent();
		assertThat(result.get()).isEqualTo(expectedResult);
	}

	@Test
	public void testElicitationWithRequest() {
		ClientCapabilities capabilities = mock(ClientCapabilities.class);
		ClientCapabilities.Elicitation elicitation = mock(ClientCapabilities.Elicitation.class);
		when(capabilities.elicitation()).thenReturn(elicitation);
		when(exchange.getClientCapabilities()).thenReturn(capabilities);

		ElicitResult expectedResult = mock(ElicitResult.class);
		ElicitRequest elicitRequest = ElicitRequest.builder()
			.message("Test message")
			.requestedSchema(Map.of("type", "string"))
			.build();

		when(exchange.createElicitation(elicitRequest)).thenReturn(expectedResult);

		Optional<ElicitResult> result = context.elicitation(elicitRequest);

		assertThat(result).isPresent();
		assertThat(result.get()).isEqualTo(expectedResult);
	}

	@Test
	public void testElicitationWhenNotSupported() {
		when(exchange.getClientCapabilities()).thenReturn(null);

		ElicitRequest elicitRequest = ElicitRequest.builder()
			.message("Test message")
			.requestedSchema(Map.of("type", "string"))
			.build();

		Optional<ElicitResult> result = context.elicitation(elicitRequest);

		assertThat(result).isEmpty();
	}

	// Sampling Tests

	@Test
	public void testSamplingWithMessages() {
		ClientCapabilities capabilities = mock(ClientCapabilities.class);
		ClientCapabilities.Sampling sampling = mock(ClientCapabilities.Sampling.class);
		when(capabilities.sampling()).thenReturn(sampling);
		when(exchange.getClientCapabilities()).thenReturn(capabilities);

		CreateMessageResult expectedResult = mock(CreateMessageResult.class);
		when(exchange.createMessage(any(CreateMessageRequest.class))).thenReturn(expectedResult);

		Optional<CreateMessageResult> result = context.sampling("Message 1", "Message 2");

		assertThat(result).isPresent();
		assertThat(result.get()).isEqualTo(expectedResult);
	}

	@Test
	public void testSamplingWithConsumer() {
		ClientCapabilities capabilities = mock(ClientCapabilities.class);
		ClientCapabilities.Sampling sampling = mock(ClientCapabilities.Sampling.class);
		when(capabilities.sampling()).thenReturn(sampling);
		when(exchange.getClientCapabilities()).thenReturn(capabilities);

		CreateMessageResult expectedResult = mock(CreateMessageResult.class);
		when(exchange.createMessage(any(CreateMessageRequest.class))).thenReturn(expectedResult);

		Optional<CreateMessageResult> result = context.sampling(spec -> {
			spec.message(new TextContent("Test message"));
			spec.systemPrompt("System prompt");
			spec.temperature(0.7);
			spec.maxTokens(100);
		});

		assertThat(result).isPresent();
		assertThat(result.get()).isEqualTo(expectedResult);

		ArgumentCaptor<CreateMessageRequest> captor = ArgumentCaptor.forClass(CreateMessageRequest.class);
		verify(exchange).createMessage(captor.capture());

		CreateMessageRequest capturedRequest = captor.getValue();
		assertThat(capturedRequest.systemPrompt()).isEqualTo("System prompt");
		assertThat(capturedRequest.temperature()).isEqualTo(0.7);
		assertThat(capturedRequest.maxTokens()).isEqualTo(100);
	}

	@Test
	public void testSamplingWithRequest() {
		ClientCapabilities capabilities = mock(ClientCapabilities.class);
		ClientCapabilities.Sampling sampling = mock(ClientCapabilities.Sampling.class);
		when(capabilities.sampling()).thenReturn(sampling);
		when(exchange.getClientCapabilities()).thenReturn(capabilities);

		CreateMessageResult expectedResult = mock(CreateMessageResult.class);
		CreateMessageRequest createRequest = CreateMessageRequest.builder()
			.messages(java.util.List.of(new SamplingMessage(Role.USER, new TextContent("Test"))))
			.maxTokens(500)
			.build();

		when(exchange.createMessage(createRequest)).thenReturn(expectedResult);

		Optional<CreateMessageResult> result = context.sampling(createRequest);

		assertThat(result).isPresent();
		assertThat(result.get()).isEqualTo(expectedResult);
	}

	@Test
	public void testSamplingWhenNotSupported() {
		when(exchange.getClientCapabilities()).thenReturn(null);

		CreateMessageRequest createRequest = CreateMessageRequest.builder()
			.messages(java.util.List.of(new SamplingMessage(Role.USER, new TextContent("Test"))))
			.maxTokens(500)
			.build();

		Optional<CreateMessageResult> result = context.sampling(createRequest);

		assertThat(result).isEmpty();
	}

	// Progress Tests

	@Test
	public void testProgressWithPercentage() {
		CallToolRequest requestWithToken = CallToolRequest.builder()
			.name("test-tool")
			.arguments(Map.of())
			.progressToken("token-123")
			.build();
		McpSyncRequestContext contextWithToken = DefaultMcpSyncRequestContext.builder()
			.request(requestWithToken)
			.exchange(exchange)
			.build();

		contextWithToken.progress(50);

		ArgumentCaptor<ProgressNotification> captor = ArgumentCaptor.forClass(ProgressNotification.class);
		verify(exchange).progressNotification(captor.capture());

		ProgressNotification notification = captor.getValue();
		assertThat(notification.progressToken()).isEqualTo("token-123");
		assertThat(notification.progress()).isEqualTo(0.5);
		assertThat(notification.total()).isEqualTo(1.0);
	}

	@Test
	public void testProgressWithInvalidPercentage() {
		assertThatThrownBy(() -> context.progress(-1)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Percentage must be between 0 and 100");

		assertThatThrownBy(() -> context.progress(101)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Percentage must be between 0 and 100");
	}

	@Test
	public void testProgressWithConsumer() {
		CallToolRequest requestWithToken = CallToolRequest.builder()
			.name("test-tool")
			.arguments(Map.of())
			.progressToken("token-123")
			.build();
		McpSyncRequestContext contextWithToken = DefaultMcpSyncRequestContext.builder()
			.request(requestWithToken)
			.exchange(exchange)
			.build();

		contextWithToken.progress(spec -> {
			spec.progress(0.75);
			spec.total(1.0);
			spec.message("Processing...");
		});

		ArgumentCaptor<ProgressNotification> captor = ArgumentCaptor.forClass(ProgressNotification.class);
		verify(exchange).progressNotification(captor.capture());

		ProgressNotification notification = captor.getValue();
		assertThat(notification.progressToken()).isEqualTo("token-123");
		assertThat(notification.progress()).isEqualTo(0.75);
		assertThat(notification.total()).isEqualTo(1.0);
		assertThat(notification.message()).isEqualTo("Processing...");
	}

	@Test
	public void testProgressWithNotification() {
		ProgressNotification notification = new ProgressNotification("token-123", 0.5, 1.0, "Test", null);

		context.progress(notification);

		verify(exchange).progressNotification(notification);
	}

	@Test
	public void testProgressWithoutToken() {
		// request already has no progress token (null by default)
		// Should not throw, just log warning
		context.progress(50);
	}

	// Ping Tests

	@Test
	public void testPing() {
		context.ping();

		verify(exchange).ping();
	}

	// Logging Tests

	@Test
	public void testLogWithConsumer() {
		context.log(spec -> {
			spec.message("Test log message");
			spec.level(LoggingLevel.INFO);
			spec.logger("test-logger");
		});

		ArgumentCaptor<LoggingMessageNotification> captor = ArgumentCaptor.forClass(LoggingMessageNotification.class);
		verify(exchange).loggingNotification(captor.capture());

		LoggingMessageNotification notification = captor.getValue();
		assertThat(notification.data()).isEqualTo("Test log message");
		assertThat(notification.level()).isEqualTo(LoggingLevel.INFO);
		assertThat(notification.logger()).isEqualTo("test-logger");
	}

	@Test
	public void testDebug() {
		context.debug("Debug message");

		ArgumentCaptor<LoggingMessageNotification> captor = ArgumentCaptor.forClass(LoggingMessageNotification.class);
		verify(exchange).loggingNotification(captor.capture());

		LoggingMessageNotification notification = captor.getValue();
		assertThat(notification.data()).isEqualTo("Debug message");
		assertThat(notification.level()).isEqualTo(LoggingLevel.DEBUG);
	}

	@Test
	public void testInfo() {
		context.info("Info message");

		ArgumentCaptor<LoggingMessageNotification> captor = ArgumentCaptor.forClass(LoggingMessageNotification.class);
		verify(exchange).loggingNotification(captor.capture());

		LoggingMessageNotification notification = captor.getValue();
		assertThat(notification.data()).isEqualTo("Info message");
		assertThat(notification.level()).isEqualTo(LoggingLevel.INFO);
	}

	@Test
	public void testWarn() {
		context.warn("Warning message");

		ArgumentCaptor<LoggingMessageNotification> captor = ArgumentCaptor.forClass(LoggingMessageNotification.class);
		verify(exchange).loggingNotification(captor.capture());

		LoggingMessageNotification notification = captor.getValue();
		assertThat(notification.data()).isEqualTo("Warning message");
		assertThat(notification.level()).isEqualTo(LoggingLevel.WARNING);
	}

	@Test
	public void testError() {
		context.error("Error message");

		ArgumentCaptor<LoggingMessageNotification> captor = ArgumentCaptor.forClass(LoggingMessageNotification.class);
		verify(exchange).loggingNotification(captor.capture());

		LoggingMessageNotification notification = captor.getValue();
		assertThat(notification.data()).isEqualTo("Error message");
		assertThat(notification.level()).isEqualTo(LoggingLevel.ERROR);
	}

	@Test
	public void testLogWithEmptyMessage() {
		assertThatThrownBy(() -> context.debug("")).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Log message must not be empty");
	}

	// Getter Tests

	@Test
	public void testGetRequest() {
		assertThat(context.request()).isEqualTo(request);
	}

	@Test
	public void testGetExchange() {
		assertThat(context.exchange()).isEqualTo(exchange);
	}

	@Test
	public void testGetSessionId() {
		when(exchange.sessionId()).thenReturn("session-123");

		assertThat(context.sessionId()).isEqualTo("session-123");
	}

	@Test
	public void testGetClientInfo() {
		Implementation clientInfo = mock(Implementation.class);
		when(exchange.getClientInfo()).thenReturn(clientInfo);

		assertThat(context.clientInfo()).isEqualTo(clientInfo);
	}

	@Test
	public void testGetClientCapabilities() {
		ClientCapabilities capabilities = mock(ClientCapabilities.class);
		when(exchange.getClientCapabilities()).thenReturn(capabilities);

		assertThat(context.clientCapabilities()).isEqualTo(capabilities);
	}

	@Test
	public void testGetRequestMeta() {
		Map<String, Object> meta = Map.of("key", "value");
		CallToolRequest requestWithMeta = CallToolRequest.builder()
			.name("test-tool")
			.arguments(Map.of())
			.meta(meta)
			.build();
		McpSyncRequestContext contextWithMeta = DefaultMcpSyncRequestContext.builder()
			.request(requestWithMeta)
			.exchange(exchange)
			.build();

		assertThat(contextWithMeta.requestMeta()).isEqualTo(meta);
	}

}
