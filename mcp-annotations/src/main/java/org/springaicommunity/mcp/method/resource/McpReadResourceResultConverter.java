/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.method.resource;

import java.util.Map;

import org.springaicommunity.mcp.method.resource.AbstractMcpResourceMethodCallback.ContentType;

import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;

/**
 * Interface for converting method return values to {@link ReadResourceResult}.
 * <p>
 * This interface defines a contract for converting various return types from resource
 * methods to a standardized {@link ReadResourceResult} format.
 *
 * @author Christian Tzolov
 * @author Alexandros Pappas
 * @author Vadzim Shurmialiou
 * @author Craig Walls
 */
public interface McpReadResourceResultConverter {

	/**
	 * Converts the method's return value to a {@link ReadResourceResult}.
	 * <p>
	 * This method handles various return types and converts them to a standardized
	 * {@link ReadResourceResult} format.
	 * @param result The method's return value
	 * @param requestUri The original request URI
	 * @param mimeType The MIME type of the resource
	 * @param contentType The content type of the resource
	 * @return A {@link ReadResourceResult} containing the appropriate resource contents
	 * @throws IllegalArgumentException if the return type is not supported
	 */
	ReadResourceResult convertToReadResourceResult(Object result, String requestUri, String mimeType,
			ContentType contentType);

	/**
	 * Converts the method's return value to a {@link ReadResourceResult}, propagating
	 * resource-level metadata to the content items.
	 * <p>
	 * This default method delegates to the original
	 * {@link #convertToReadResourceResult(Object, String, String, ContentType)} to ensure
	 * backwards compatibility with existing custom implementations.
	 * @param result The method's return value
	 * @param requestUri The original request URI
	 * @param mimeType The MIME type of the resource
	 * @param contentType The content type of the resource
	 * @param meta The resource-level metadata to propagate to content items
	 * @return A {@link ReadResourceResult} containing the appropriate resource contents
	 * @throws IllegalArgumentException if the return type is not supported
	 */
	default ReadResourceResult convertToReadResourceResult(Object result, String requestUri, String mimeType,
			ContentType contentType, Map<String, Object> meta) {
		return convertToReadResourceResult(result, requestUri, mimeType, contentType);
	}

}
