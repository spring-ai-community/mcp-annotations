/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.elicitation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;

import org.junit.jupiter.api.Test;

import io.modelcontextprotocol.spec.McpSchema.ElicitRequest;
import io.modelcontextprotocol.spec.McpSchema.ElicitResult;
import reactor.core.publisher.Mono;

/**
 * Tests for {@link SyncElicitationSpecification} and
 * {@link AsyncElicitationSpecification} validation requirements.
 *
 * @author Christian Tzolov
 */
public class ElicitationSpecificationTests {

	@Test
	void testSyncElicitationSpecificationValidClientId() {
		// Valid clientId should work
		SyncElicitationSpecification spec = new SyncElicitationSpecification("valid-client-id",
				request -> new ElicitResult(ElicitResult.Action.ACCEPT, Map.of("test", "value")));

		assertThat(spec.clientId()).isEqualTo("valid-client-id");
		assertThat(spec.elicitationHandler()).isNotNull();
	}

	@Test
	void testSyncElicitationSpecificationNullClientId() {
		assertThatThrownBy(() -> new SyncElicitationSpecification(null,
				request -> new ElicitResult(ElicitResult.Action.ACCEPT, Map.of("test", "value"))))
			.isInstanceOf(NullPointerException.class)
			.hasMessage("clientId must not be null");
	}

	@Test
	void testSyncElicitationSpecificationEmptyClientId() {
		assertThatThrownBy(() -> new SyncElicitationSpecification("",
				request -> new ElicitResult(ElicitResult.Action.ACCEPT, Map.of("test", "value"))))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("clientId must not be empty");
	}

	@Test
	void testSyncElicitationSpecificationBlankClientId() {
		assertThatThrownBy(() -> new SyncElicitationSpecification("   ",
				request -> new ElicitResult(ElicitResult.Action.ACCEPT, Map.of("test", "value"))))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("clientId must not be empty");
	}

	@Test
	void testSyncElicitationSpecificationNullHandler() {
		assertThatThrownBy(() -> new SyncElicitationSpecification("valid-client-id", null))
			.isInstanceOf(NullPointerException.class)
			.hasMessage("elicitationHandler must not be null");
	}

	@Test
	void testAsyncElicitationSpecificationValidClientId() {
		// Valid clientId should work
		AsyncElicitationSpecification spec = new AsyncElicitationSpecification("valid-client-id",
				request -> Mono.just(new ElicitResult(ElicitResult.Action.ACCEPT, Map.of("test", "value"))));

		assertThat(spec.clientId()).isEqualTo("valid-client-id");
		assertThat(spec.elicitationHandler()).isNotNull();
	}

	@Test
	void testAsyncElicitationSpecificationNullClientId() {
		assertThatThrownBy(() -> new AsyncElicitationSpecification(null,
				request -> Mono.just(new ElicitResult(ElicitResult.Action.ACCEPT, Map.of("test", "value")))))
			.isInstanceOf(NullPointerException.class)
			.hasMessage("clientId must not be null");
	}

	@Test
	void testAsyncElicitationSpecificationEmptyClientId() {
		assertThatThrownBy(() -> new AsyncElicitationSpecification("",
				request -> Mono.just(new ElicitResult(ElicitResult.Action.ACCEPT, Map.of("test", "value")))))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("clientId must not be empty");
	}

	@Test
	void testAsyncElicitationSpecificationBlankClientId() {
		assertThatThrownBy(() -> new AsyncElicitationSpecification("   ",
				request -> Mono.just(new ElicitResult(ElicitResult.Action.ACCEPT, Map.of("test", "value")))))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessage("clientId must not be empty");
	}

	@Test
	void testAsyncElicitationSpecificationNullHandler() {
		assertThatThrownBy(() -> new AsyncElicitationSpecification("valid-client-id", null))
			.isInstanceOf(NullPointerException.class)
			.hasMessage("elicitationHandler must not be null");
	}

	@Test
	void testSyncElicitationSpecificationFunctionality() {
		SyncElicitationSpecification spec = new SyncElicitationSpecification("test-client",
				request -> new ElicitResult(ElicitResult.Action.ACCEPT,
						Map.of("message", request.message(), "clientId", "test-client")));

		ElicitRequest request = ElicitationTestHelper.createSampleRequest("Test message");
		ElicitResult result = spec.elicitationHandler().apply(request);

		assertThat(result).isNotNull();
		assertThat(result.action()).isEqualTo(ElicitResult.Action.ACCEPT);
		assertThat(result.content()).containsEntry("message", "Test message");
		assertThat(result.content()).containsEntry("clientId", "test-client");
	}

	@Test
	void testAsyncElicitationSpecificationFunctionality() {
		AsyncElicitationSpecification spec = new AsyncElicitationSpecification("test-client",
				request -> Mono.just(new ElicitResult(ElicitResult.Action.ACCEPT,
						Map.of("message", request.message(), "clientId", "test-client"))));

		ElicitRequest request = ElicitationTestHelper.createSampleRequest("Test async message");
		Mono<ElicitResult> resultMono = spec.elicitationHandler().apply(request);

		ElicitResult result = resultMono.block();
		assertThat(result).isNotNull();
		assertThat(result.action()).isEqualTo(ElicitResult.Action.ACCEPT);
		assertThat(result.content()).containsEntry("message", "Test async message");
		assertThat(result.content()).containsEntry("clientId", "test-client");
	}

}
