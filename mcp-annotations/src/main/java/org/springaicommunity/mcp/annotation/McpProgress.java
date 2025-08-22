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
 * Annotation for methods that handle progress notifications from MCP servers. This
 * annotation is applicable only for MCP clients.
 *
 * <p>
 * Methods annotated with this annotation can be used to consume progress messages from
 * MCP servers. The methods takes a single parameter of type {@code ProgressNotification}
 *
 *
 * <p>
 * Example usage: <pre>{@code
 * &#64;McpProgress
 * public void handleProgressMessage(ProgressNotification notification) {
 *     // Handle the notification *
 * }</pre>
 *
 * @author Christian Tzolov
 *
 * @see io.modelcontextprotocol.spec.McpSchema.ProgressNotification
 */
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface McpProgress {

	/**
	 * Used as connection or client identifier to select the MCP client, the logging
	 * consumer is associated with. If not specified, is applied to all clients.
	 */
	String clientId() default "";

}
