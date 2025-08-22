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
 * Annotation for methods that handle elicitation requests from MCP servers. This
 * annotation is applicable only for MCP clients.
 *
 * <p>
 * Methods annotated with this annotation can be used to process elicitation requests from
 * MCP servers.
 *
 * <p>
 * For synchronous handlers, the method must return {@code ElicitResult}. For asynchronous
 * handlers, the method must return {@code Mono<ElicitResult>}.
 *
 * <p>
 * Example usage: <pre>{@code
 * &#64;McpElicitation
 * public ElicitResult handleElicitationRequest(ElicitRequest request) {
 *     return ElicitResult.builder()
 *         .message("Generated response")
 *         .requestedSchema(
 *             Map.of("type", "object", "properties", Map.of("message", Map.of("type", "string"))))
 *         .build();
 * }
 *
 * &#64;McpElicitation
 * public Mono<ElicitResult> handleAsyncElicitationRequest(ElicitRequest request) {
 *     return Mono.just(ElicitResult.builder()
 *         .message("Generated response")
 *         .requestedSchema(
 *             Map.of("type", "object", "properties", Map.of("message", Map.of("type", "string"))))
 *         .build());
 * }
 * }</pre>
 *
 * @author Christian Tzolov
 * @see io.modelcontextprotocol.spec.McpSchema.ElicitRequest
 * @see io.modelcontextprotocol.spec.McpSchema.ElicitResult
 */
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface McpElicitation {

	/**
	 * Used as connection or client identifier to select the MCP client, the elicitation
	 * method is associated with. If not specified, is applied to all clients.
	 */
	String clientId() default "";

}
