/*
 * Copyright 2025-2025 the original author or authors.
 */

package com.logaritex.mcp.method.resource;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.BiFunction;

import com.logaritex.mcp.annotation.McpResource;
import com.logaritex.mcp.annotation.ResourceAdaptor;
import com.logaritex.mcp.method.resource.SyncMcpResourceMethodCallback;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.BlobResourceContents;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.ResourceContents;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link SyncMcpResourceMethodCallback}.
 *
 * @author Christian Tzolov
 */
public class SyncMcpResourceMethodCallbackTests {

	private static class TestResourceProvider {

		public ReadResourceResult getResourceWithRequest(ReadResourceRequest request) {
			return new ReadResourceResult(
					List.of(new TextResourceContents(request.uri(), "text/plain", "Content for " + request.uri())));
		}

		public ReadResourceResult getResourceWithExchange(McpSyncServerExchange exchange, ReadResourceRequest request) {
			return new ReadResourceResult(List.of(new TextResourceContents(request.uri(), "text/plain",
					"Content with exchange for " + request.uri())));
		}

		public ReadResourceResult getResourceWithUri(String uri) {
			return new ReadResourceResult(
					List.of(new TextResourceContents(uri, "text/plain", "Content from URI: " + uri)));
		}

		@McpResource(uri = "users/{userId}/posts/{postId}")
		public ReadResourceResult getResourceWithUriVariables(String userId, String postId) {
			return new ReadResourceResult(List.of(new TextResourceContents("users/" + userId + "/posts/" + postId,
					"text/plain", "User: " + userId + ", Post: " + postId)));
		}

		@McpResource(uri = "users/{userId}/profile")
		public ReadResourceResult getResourceWithExchangeAndUriVariable(McpSyncServerExchange exchange, String userId) {
			return new ReadResourceResult(List.of(new TextResourceContents("users/" + userId + "/profile", "text/plain",
					"Profile for user: " + userId)));
		}

		public List<ResourceContents> getResourceContentsList(ReadResourceRequest request) {
			return List.of(new TextResourceContents(request.uri(), "text/plain", "Content list for " + request.uri()));
		}

		public List<String> getStringList(ReadResourceRequest request) {
			return List.of("String 1 for " + request.uri(), "String 2 for " + request.uri());
		}

		public ResourceContents getSingleResourceContents(ReadResourceRequest request) {
			return new TextResourceContents(request.uri(), "text/plain",
					"Single resource content for " + request.uri());
		}

		public String getSingleString(ReadResourceRequest request) {
			return "Single string for " + request.uri();
		}

		@McpResource(uri = "text-content://resource", mimeType = "text/plain")
		public String getStringWithTextContentType(ReadResourceRequest request) {
			return "Text content type for " + request.uri();
		}

		@McpResource(uri = "blob-content://resource", mimeType = "application/octet-stream")
		public String getStringWithBlobContentType(ReadResourceRequest request) {
			return "Blob content type for " + request.uri();
		}

		@McpResource(uri = "text-list://resource", mimeType = "text/html")
		public List<String> getStringListWithTextContentType(ReadResourceRequest request) {
			return List.of("HTML text 1 for " + request.uri(), "HTML text 2 for " + request.uri());
		}

		@McpResource(uri = "blob-list://resource", mimeType = "image/png")
		public List<String> getStringListWithBlobContentType(ReadResourceRequest request) {
			return List.of("PNG blob 1 for " + request.uri(), "PNG blob 2 for " + request.uri());
		}

		public void invalidReturnType(ReadResourceRequest request) {
			// Invalid return type
		}

		public ReadResourceResult invalidParameters(int value) {
			return new ReadResourceResult(List.of());
		}

		public ReadResourceResult tooManyParameters(McpSyncServerExchange exchange, ReadResourceRequest request,
				String extraParam) {
			return new ReadResourceResult(List.of());
		}

		public ReadResourceResult invalidParameterType(Object invalidParam) {
			return new ReadResourceResult(List.of());
		}

		public ReadResourceResult duplicateExchangeParameters(McpSyncServerExchange exchange1,
				McpSyncServerExchange exchange2) {
			return new ReadResourceResult(List.of());
		}

		public ReadResourceResult duplicateRequestParameters(ReadResourceRequest request1,
				ReadResourceRequest request2) {
			return new ReadResourceResult(List.of());
		}

	}

	// Helper method to create a mock McpResource annotation
	private McpResource createMockMcpResource() {
		return new McpResource() {
			@Override
			public Class<? extends java.lang.annotation.Annotation> annotationType() {
				return McpResource.class;
			}

			@Override
			public String uri() {
				return "test://resource";
			}

			@Override
			public String name() {
				return "";
			}

			@Override
			public String description() {
				return "";
			}

			@Override
			public String mimeType() {
				return "text/plain";
			}
		};
	}

	@Test
	public void testCallbackWithRequestParameter() throws Exception {
		TestResourceProvider provider = new TestResourceProvider();
		Method method = TestResourceProvider.class.getMethod("getResourceWithRequest", ReadResourceRequest.class);

		// Provide a mock McpResource annotation since the method doesn't have one
		BiFunction<McpSyncServerExchange, ReadResourceRequest, ReadResourceResult> callback = SyncMcpResourceMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.resource(ResourceAdaptor.asResource(createMockMcpResource()))
			.build();

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		ReadResourceRequest request = new ReadResourceRequest("test/resource");

		ReadResourceResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.contents()).hasSize(1);
		assertThat(result.contents().get(0)).isInstanceOf(TextResourceContents.class);
		TextResourceContents textContent = (TextResourceContents) result.contents().get(0);
		assertThat(textContent.text()).isEqualTo("Content for test/resource");
	}

	@Test
	public void testCallbackWithExchangeAndRequestParameters() throws Exception {
		TestResourceProvider provider = new TestResourceProvider();
		Method method = TestResourceProvider.class.getMethod("getResourceWithExchange", McpSyncServerExchange.class,
				ReadResourceRequest.class);

		// Use the builder to provide a mock McpResource annotation
		BiFunction<McpSyncServerExchange, ReadResourceRequest, ReadResourceResult> callback = SyncMcpResourceMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.resource(ResourceAdaptor.asResource(createMockMcpResource()))
			.build();

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		ReadResourceRequest request = new ReadResourceRequest("test/resource");

		ReadResourceResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.contents()).hasSize(1);
		assertThat(result.contents().get(0)).isInstanceOf(TextResourceContents.class);
		TextResourceContents textContent = (TextResourceContents) result.contents().get(0);
		assertThat(textContent.text()).isEqualTo("Content with exchange for test/resource");
	}

	@Test
	public void testCallbackWithUriParameter() throws Exception {
		TestResourceProvider provider = new TestResourceProvider();
		Method method = TestResourceProvider.class.getMethod("getResourceWithUri", String.class);

		BiFunction<McpSyncServerExchange, ReadResourceRequest, ReadResourceResult> callback = SyncMcpResourceMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.resource(ResourceAdaptor.asResource(createMockMcpResource()))
			.build();

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		ReadResourceRequest request = new ReadResourceRequest("test/resource");

		ReadResourceResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.contents()).hasSize(1);
		assertThat(result.contents().get(0)).isInstanceOf(TextResourceContents.class);
		TextResourceContents textContent = (TextResourceContents) result.contents().get(0);
		assertThat(textContent.text()).isEqualTo("Content from URI: test/resource");
	}

	@Test
	public void testCallbackWithUriVariables() throws Exception {
		TestResourceProvider provider = new TestResourceProvider();
		Method method = TestResourceProvider.class.getMethod("getResourceWithUriVariables", String.class, String.class);
		McpResource resourceAnnotation = method.getAnnotation(McpResource.class);

		BiFunction<McpSyncServerExchange, ReadResourceRequest, ReadResourceResult> callback = SyncMcpResourceMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.resource(ResourceAdaptor.asResource(resourceAnnotation))
			.build();

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		ReadResourceRequest request = new ReadResourceRequest("users/123/posts/456");

		ReadResourceResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.contents()).hasSize(1);
		assertThat(result.contents().get(0)).isInstanceOf(TextResourceContents.class);
		TextResourceContents textContent = (TextResourceContents) result.contents().get(0);
		assertThat(textContent.text()).isEqualTo("User: 123, Post: 456");
	}

	@Test
	public void testCallbackWithExchangeAndUriVariable() throws Exception {
		TestResourceProvider provider = new TestResourceProvider();
		Method method = TestResourceProvider.class.getMethod("getResourceWithExchangeAndUriVariable",
				McpSyncServerExchange.class, String.class);
		McpResource resourceAnnotation = method.getAnnotation(McpResource.class);

		BiFunction<McpSyncServerExchange, ReadResourceRequest, ReadResourceResult> callback = SyncMcpResourceMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.resource(ResourceAdaptor.asResource(resourceAnnotation))
			.build();

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		ReadResourceRequest request = new ReadResourceRequest("users/789/profile");

		ReadResourceResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.contents()).hasSize(1);
		assertThat(result.contents().get(0)).isInstanceOf(TextResourceContents.class);
		TextResourceContents textContent = (TextResourceContents) result.contents().get(0);
		assertThat(textContent.text()).isEqualTo("Profile for user: 789");
	}

	@Test
	public void testCallbackWithResourceContentsList() throws Exception {
		TestResourceProvider provider = new TestResourceProvider();
		Method method = TestResourceProvider.class.getMethod("getResourceContentsList", ReadResourceRequest.class);

		BiFunction<McpSyncServerExchange, ReadResourceRequest, ReadResourceResult> callback = SyncMcpResourceMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.resource(ResourceAdaptor.asResource(createMockMcpResource()))
			.build();

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		ReadResourceRequest request = new ReadResourceRequest("test/resource");

		ReadResourceResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.contents()).hasSize(1);
		assertThat(result.contents().get(0)).isInstanceOf(TextResourceContents.class);
		TextResourceContents textContent = (TextResourceContents) result.contents().get(0);
		assertThat(textContent.text()).isEqualTo("Content list for test/resource");
	}

	@Test
	public void testCallbackWithStringList() throws Exception {
		TestResourceProvider provider = new TestResourceProvider();
		Method method = TestResourceProvider.class.getMethod("getStringList", ReadResourceRequest.class);

		BiFunction<McpSyncServerExchange, ReadResourceRequest, ReadResourceResult> callback = SyncMcpResourceMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.resource(ResourceAdaptor.asResource(createMockMcpResource()))
			.build();

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		ReadResourceRequest request = new ReadResourceRequest("test/resource");

		ReadResourceResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.contents()).hasSize(2);
		assertThat(result.contents().get(0)).isInstanceOf(TextResourceContents.class);
		TextResourceContents textContent1 = (TextResourceContents) result.contents().get(0);
		TextResourceContents textContent2 = (TextResourceContents) result.contents().get(1);
		assertThat(textContent1.text()).isEqualTo("String 1 for test/resource");
		assertThat(textContent2.text()).isEqualTo("String 2 for test/resource");
	}

	@Test
	public void testCallbackWithSingleResourceContents() throws Exception {
		TestResourceProvider provider = new TestResourceProvider();
		Method method = TestResourceProvider.class.getMethod("getSingleResourceContents", ReadResourceRequest.class);

		BiFunction<McpSyncServerExchange, ReadResourceRequest, ReadResourceResult> callback = SyncMcpResourceMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.resource(ResourceAdaptor.asResource(createMockMcpResource()))
			.build();

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		ReadResourceRequest request = new ReadResourceRequest("test/resource");

		ReadResourceResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.contents()).hasSize(1);
		assertThat(result.contents().get(0)).isInstanceOf(TextResourceContents.class);
		TextResourceContents textContent = (TextResourceContents) result.contents().get(0);
		assertThat(textContent.text()).isEqualTo("Single resource content for test/resource");
	}

	@Test
	public void testCallbackWithSingleString() throws Exception {
		TestResourceProvider provider = new TestResourceProvider();
		Method method = TestResourceProvider.class.getMethod("getSingleString", ReadResourceRequest.class);

		BiFunction<McpSyncServerExchange, ReadResourceRequest, ReadResourceResult> callback = SyncMcpResourceMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.resource(ResourceAdaptor.asResource(createMockMcpResource()))
			.build();

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		ReadResourceRequest request = new ReadResourceRequest("test/resource");

		ReadResourceResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.contents()).hasSize(1);
		assertThat(result.contents().get(0)).isInstanceOf(TextResourceContents.class);
		TextResourceContents textContent = (TextResourceContents) result.contents().get(0);
		assertThat(textContent.text()).isEqualTo("Single string for test/resource");
	}

	@Test
	public void testInvalidReturnType() throws Exception {
		TestResourceProvider provider = new TestResourceProvider();
		Method method = TestResourceProvider.class.getMethod("invalidReturnType", ReadResourceRequest.class);

		assertThatThrownBy(() -> SyncMcpResourceMethodCallback.builder().method(method).bean(provider).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("URI must not be null or empty");
	}

	@Test
	public void testInvalidUriVariableParameters() throws Exception {
		TestResourceProvider provider = new TestResourceProvider();
		Method method = TestResourceProvider.class.getMethod("getResourceWithUriVariables", String.class, String.class);
		McpResource resourceAnnotation = method.getAnnotation(McpResource.class);

		// Create a mock annotation with a different URI template that has more variables
		// than the method has parameters
		McpResource mockResourceAnnotation = new McpResource() {
			@Override
			public Class<? extends java.lang.annotation.Annotation> annotationType() {
				return McpResource.class;
			}

			@Override
			public String uri() {
				return "users/{userId}/posts/{postId}/comments/{commentId}";
			}

			@Override
			public String name() {
				return "";
			}

			@Override
			public String description() {
				return "";
			}

			@Override
			public String mimeType() {
				return "";
			}
		};

		assertThatThrownBy(() -> SyncMcpResourceMethodCallback.builder()
			.method(method)
			.bean(provider)
			.resource(ResourceAdaptor.asResource(mockResourceAnnotation))
			.build()).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Method must have parameters for all URI variables");
	}

	@Test
	public void testCallbackWithStringAndTextContentType() throws Exception {
		TestResourceProvider provider = new TestResourceProvider();
		Method method = TestResourceProvider.class.getMethod("getStringWithTextContentType", ReadResourceRequest.class);
		McpResource resourceAnnotation = method.getAnnotation(McpResource.class);

		BiFunction<McpSyncServerExchange, ReadResourceRequest, ReadResourceResult> callback = SyncMcpResourceMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.resource(ResourceAdaptor.asResource(resourceAnnotation))
			.build();

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		ReadResourceRequest request = new ReadResourceRequest("test/resource");

		ReadResourceResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.contents()).hasSize(1);
		assertThat(result.contents().get(0)).isInstanceOf(TextResourceContents.class);
		TextResourceContents textContent = (TextResourceContents) result.contents().get(0);
		assertThat(textContent.text()).isEqualTo("Text content type for test/resource");
		assertThat(textContent.mimeType()).isEqualTo("text/plain");
	}

	@Test
	public void testCallbackWithStringAndBlobContentType() throws Exception {
		TestResourceProvider provider = new TestResourceProvider();
		Method method = TestResourceProvider.class.getMethod("getStringWithBlobContentType", ReadResourceRequest.class);
		McpResource resourceAnnotation = method.getAnnotation(McpResource.class);

		BiFunction<McpSyncServerExchange, ReadResourceRequest, ReadResourceResult> callback = SyncMcpResourceMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.resource(ResourceAdaptor.asResource(resourceAnnotation))
			.build();

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		ReadResourceRequest request = new ReadResourceRequest("test/resource");

		ReadResourceResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.contents()).hasSize(1);
		assertThat(result.contents().get(0)).isInstanceOf(BlobResourceContents.class);
		BlobResourceContents blobContent = (BlobResourceContents) result.contents().get(0);
		assertThat(blobContent.blob()).isEqualTo("Blob content type for test/resource");
		assertThat(blobContent.mimeType()).isEqualTo("application/octet-stream");
	}

	@Test
	public void testCallbackWithStringListAndTextContentType() throws Exception {
		TestResourceProvider provider = new TestResourceProvider();
		Method method = TestResourceProvider.class.getMethod("getStringListWithTextContentType",
				ReadResourceRequest.class);
		McpResource resourceAnnotation = method.getAnnotation(McpResource.class);

		BiFunction<McpSyncServerExchange, ReadResourceRequest, ReadResourceResult> callback = SyncMcpResourceMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.resource(ResourceAdaptor.asResource(resourceAnnotation))
			.build();

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		ReadResourceRequest request = new ReadResourceRequest("test/resource");

		ReadResourceResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.contents()).hasSize(2);
		assertThat(result.contents().get(0)).isInstanceOf(TextResourceContents.class);
		TextResourceContents textContent1 = (TextResourceContents) result.contents().get(0);
		TextResourceContents textContent2 = (TextResourceContents) result.contents().get(1);
		assertThat(textContent1.text()).isEqualTo("HTML text 1 for test/resource");
		assertThat(textContent2.text()).isEqualTo("HTML text 2 for test/resource");
		assertThat(textContent1.mimeType()).isEqualTo("text/html");
		assertThat(textContent2.mimeType()).isEqualTo("text/html");
	}

	@Test
	public void testCallbackWithStringListAndBlobContentType() throws Exception {
		TestResourceProvider provider = new TestResourceProvider();
		Method method = TestResourceProvider.class.getMethod("getStringListWithBlobContentType",
				ReadResourceRequest.class);
		McpResource resourceAnnotation = method.getAnnotation(McpResource.class);

		BiFunction<McpSyncServerExchange, ReadResourceRequest, ReadResourceResult> callback = SyncMcpResourceMethodCallback
			.builder()
			.method(method)
			.bean(provider)
			.resource(ResourceAdaptor.asResource(resourceAnnotation))
			.build();

		McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
		ReadResourceRequest request = new ReadResourceRequest("test/resource");

		ReadResourceResult result = callback.apply(exchange, request);

		assertThat(result).isNotNull();
		assertThat(result.contents()).hasSize(2);
		assertThat(result.contents().get(0)).isInstanceOf(BlobResourceContents.class);
		BlobResourceContents blobContent1 = (BlobResourceContents) result.contents().get(0);
		BlobResourceContents blobContent2 = (BlobResourceContents) result.contents().get(1);
		assertThat(blobContent1.blob()).isEqualTo("PNG blob 1 for test/resource");
		assertThat(blobContent2.blob()).isEqualTo("PNG blob 2 for test/resource");
		assertThat(blobContent1.mimeType()).isEqualTo("image/png");
		assertThat(blobContent2.mimeType()).isEqualTo("image/png");
	}

	@Test
	public void testInvalidParameters() throws Exception {
		TestResourceProvider provider = new TestResourceProvider();
		Method method = TestResourceProvider.class.getMethod("invalidParameters", int.class);

		assertThatThrownBy(() -> SyncMcpResourceMethodCallback.builder().method(method).bean(provider).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("URI must not be null or empty");
	}

	@Test
	public void testTooManyParameters() throws Exception {
		TestResourceProvider provider = new TestResourceProvider();
		Method method = TestResourceProvider.class.getMethod("tooManyParameters", McpSyncServerExchange.class,
				ReadResourceRequest.class, String.class);

		assertThatThrownBy(() -> SyncMcpResourceMethodCallback.builder().method(method).bean(provider).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("URI must not be null or empty");
	}

	@Test
	public void testInvalidParameterType() throws Exception {
		TestResourceProvider provider = new TestResourceProvider();
		Method method = TestResourceProvider.class.getMethod("invalidParameterType", Object.class);

		assertThatThrownBy(() -> SyncMcpResourceMethodCallback.builder().method(method).bean(provider).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("URI must not be null or empty");
	}

	@Test
	public void testDuplicateExchangeParameters() throws Exception {
		TestResourceProvider provider = new TestResourceProvider();
		Method method = TestResourceProvider.class.getMethod("duplicateExchangeParameters", McpSyncServerExchange.class,
				McpSyncServerExchange.class);

		assertThatThrownBy(() -> SyncMcpResourceMethodCallback.builder().method(method).bean(provider).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("URI must not be null or empty");
	}

	@Test
	public void testDuplicateRequestParameters() throws Exception {
		TestResourceProvider provider = new TestResourceProvider();
		Method method = TestResourceProvider.class.getMethod("duplicateRequestParameters", ReadResourceRequest.class,
				ReadResourceRequest.class);

		assertThatThrownBy(() -> SyncMcpResourceMethodCallback.builder().method(method).bean(provider).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("URI must not be null or empty");
	}

	@Test
	public void testMethodWithoutMcpResourceAnnotation() throws Exception {
		TestResourceProvider provider = new TestResourceProvider();
		// Use a method that doesn't have the McpResource annotation
		Method method = TestResourceProvider.class.getMethod("getResourceWithRequest", ReadResourceRequest.class);

		// Create a callback without explicitly providing the annotation
		// This should now throw an exception since the method doesn't have the annotation
		assertThatThrownBy(() -> SyncMcpResourceMethodCallback.builder().method(method).bean(provider).build())
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("URI must not be null or empty");
	}

}
