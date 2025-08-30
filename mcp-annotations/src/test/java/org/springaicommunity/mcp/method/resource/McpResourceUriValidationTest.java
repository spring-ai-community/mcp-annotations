/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.resource;

import java.lang.reflect.Method;
import java.util.List;

import io.modelcontextprotocol.spec.McpSchema.ReadResourceRequest;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import org.springaicommunity.mcp.adapter.ResourceAdapter;
import org.springaicommunity.mcp.annotation.McpResource;

/**
 * Simple test to verify that McpResourceMethodCallback requires a non-empty URI in the
 * McpResource annotation.
 */
public class McpResourceUriValidationTest {

	// Test class with resource methods
	private static class TestResourceProvider {

		@McpResource(uri = "valid://uri")
		public ReadResourceResult validMethod(ReadResourceRequest request) {
			return new ReadResourceResult(List.of());
		}

		public ReadResourceResult methodWithoutAnnotation(ReadResourceRequest request) {
			return new ReadResourceResult(List.of());
		}

	}

	// Mock McpResource annotation with empty URI
	private static McpResource createMockResourceWithEmptyUri() {
		return new McpResource() {
			@Override
			public Class<? extends java.lang.annotation.Annotation> annotationType() {
				return McpResource.class;
			}

			@Override
			public String uri() {
				return "";
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
	}

	// Mock McpResource annotation with non-empty URI
	private static McpResource createMockResourceWithValidUri() {
		return new McpResource() {
			@Override
			public Class<? extends java.lang.annotation.Annotation> annotationType() {
				return McpResource.class;
			}

			@Override
			public String uri() {
				return "valid://uri";
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
	}

	public static void main(String[] args) {
		TestResourceProvider provider = new TestResourceProvider();

		try {
			// Test 1: Method with valid annotation from the class
			Method validMethod = TestResourceProvider.class.getMethod("validMethod", ReadResourceRequest.class);
			McpResource validAnnotation = validMethod.getAnnotation(McpResource.class);

			System.out.println("Test 1: Method with valid annotation from the class");
			try {
				SyncMcpResourceMethodCallback.builder()
					.method(validMethod)
					.bean(provider)
					.resource(ResourceAdapter.asResource(validAnnotation))
					.build();
				System.out.println("  PASS: Successfully created callback with valid URI");
			}
			catch (IllegalArgumentException e) {
				System.out.println("  FAIL: " + e.getMessage());
			}

			// Test 2: Method with mock annotation with empty URI
			System.out.println("\nTest 2: Method with mock annotation with empty URI");
			try {
				SyncMcpResourceMethodCallback.builder()
					.method(validMethod)
					.bean(provider)
					.resource(ResourceAdapter.asResource(createMockResourceWithEmptyUri()))
					.build();
				System.out.println("  FAIL: Should have thrown exception for empty URI");
			}
			catch (IllegalArgumentException e) {
				System.out.println("  PASS: Correctly rejected empty URI: " + e.getMessage());
			}

			// Test 3: Method with mock annotation with valid URI
			System.out.println("\nTest 3: Method with mock annotation with valid URI");
			try {
				SyncMcpResourceMethodCallback.builder()
					.method(validMethod)
					.bean(provider)
					.resource(ResourceAdapter.asResource(createMockResourceWithValidUri()))
					.build();
				System.out.println("  PASS: Successfully created callback with valid URI");
			}
			catch (IllegalArgumentException e) {
				System.out.println("  FAIL: " + e.getMessage());
			}

			// Test 4: Method without annotation using createCallback
			Method methodWithoutAnnotation = TestResourceProvider.class.getMethod("methodWithoutAnnotation",
					ReadResourceRequest.class);
			System.out.println("\nTest 4: Method without annotation using createCallback");
			try {
				SyncMcpResourceMethodCallback.builder().method(methodWithoutAnnotation).bean(provider).build();
				System.out.println("  FAIL: Should have thrown exception for missing annotation");
			}
			catch (IllegalArgumentException e) {
				System.out.println("  PASS: Correctly rejected method without annotation: " + e.getMessage());
			}

			System.out.println("\nAll tests completed.");

		}
		catch (Exception e) {
			System.out.println("Unexpected error: " + e.getMessage());
			e.printStackTrace();
		}
	}

}
