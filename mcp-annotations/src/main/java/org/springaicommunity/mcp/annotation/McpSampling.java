/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for methods that handle sampling requests from MCP servers. This annotation
 * is applicable only for MCP clients.
 *
 * <p>
 * Methods annotated with this annotation can be used to process sampling requests from
 * MCP servers. The methods can have one of two signatures:
 * <ul>
 * <li>A single parameter of type {@code CreateMessageRequest}
 * <li>Multiple parameters corresponding to the fields of {@code CreateMessageRequest}
 * </ul>
 *
 * <p>
 * For synchronous handlers, the method must return {@code CreateMessageResult}. For
 * asynchronous handlers, the method must return {@code Mono<CreateMessageResult>}.
 *
 * <p>
 * Example usage: <pre>{@code
 * &#64;McpSampling
 * public CreateMessageResult handleSamplingRequest(CreateMessageRequest request) {
 *     // Process the request and return a result
 *     return CreateMessageResult.builder()
 *         .message("Generated response")
 *         .build();
 * }
 *
 * &#64;McpSampling
 * public Mono<CreateMessageResult> handleAsyncSamplingRequest(CreateMessageRequest request) {
 *     // Process the request asynchronously and return a result
 *     return Mono.just(CreateMessageResult.builder()
 *         .message("Generated response")
 *         .build());
 * }
 * }</pre>
 *
 * @author Christian Tzolov
 * @see io.modelcontextprotocol.spec.McpSchema.CreateMessageRequest
 * @see io.modelcontextprotocol.spec.McpSchema.CreateMessageResult
 */
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface McpSampling {

	/**
	 * Used as connection or client identifier to select the MCP client, the sampling
	 * method is associated with. If not specified, is applied to all clients.
	 */
	String clientId() default "";

}
