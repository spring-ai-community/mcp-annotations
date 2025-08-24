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
 * Annotation for methods that handle tool list change notifications from MCP servers.
 * This annotation is applicable only for MCP clients.
 *
 * <p>
 * Methods annotated with this annotation are used to listen for notifications when the
 * list of available tools changes on an MCP server. According to the MCP specification,
 * servers that declare the {@code listChanged} capability will send notifications when
 * their tool list is modified.
 *
 * <p>
 * The annotated method must have a void return type for synchronous consumers, or can
 * return {@code Mono<Void>} for asynchronous consumers. The method should accept a single
 * parameter of type {@code List<McpSchema.Tool>} that represents the updated list of
 * tools after the change notification.
 *
 * <p>
 * Example usage: <pre>{@code
 * &#64;McpToolListChanged
 * public void onToolListChanged(List<McpSchema.Tool> updatedTools) {
 *     // Handle tool list change notification with the updated tools
 *     logger.info("Tool list updated, now contains {} tools", updatedTools.size());
 *     // Process the updated tool list
 * }
 *
 * &#64;McpToolListChanged
 * public Mono<Void> onToolListChangedAsync(List<McpSchema.Tool> updatedTools) {
 *     // Handle tool list change notification asynchronously
 *     return processUpdatedTools(updatedTools);
 * }
 * }</pre>
 *
 * @author Christian Tzolov
 * @see <a href=
 * "https://modelcontextprotocol.io/specification/2025-06-18/server/tools#list-changed-notification">MCP
 * Tool List Changed Notification</a>
 */
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface McpToolListChanged {

	/**
	 * Used as connection or client identifier to select the MCP client that the tool
	 * change listener is associated with. If not specified, the listener is applied to
	 * all clients and will receive notifications from any connected MCP server that
	 * supports tool list change notifications.
	 * @return the client identifier, or empty string to listen to all clients
	 */
	String clientId() default "";

}
