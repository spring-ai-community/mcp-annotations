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
 * Annotation for methods that handle prompt list change notifications from MCP servers.
 * This annotation is applicable only for MCP clients.
 *
 * <p>
 * Methods annotated with this annotation are used to listen for notifications when the
 * list of available prompts changes on an MCP server. According to the MCP specification,
 * servers that declare the {@code listChanged} capability will send notifications when
 * their prompt list is modified.
 *
 * <p>
 * The annotated method must have a void return type for synchronous consumers, or can
 * return {@code Mono<Void>} for asynchronous consumers. The method should accept a single
 * parameter of type {@code List<McpSchema.Prompt>} that represents the updated list of
 * prompts after the change notification.
 *
 * <p>
 * Example usage: <pre>{@code
 * @McpPromptListChanged(clients = "test-client")
 * public void onPromptListChanged(List<McpSchema.Prompt> updatedPrompts) {
 *     // Handle prompt list change notification with the updated prompts
 *     logger.info("Prompt list updated, now contains {} prompts", updatedPrompts.size());
 *     // Process the updated prompt list
 * }
 *
 * @McpPromptListChanged(clients = "test-client")
 * public Mono<Void> onPromptListChangedAsync(List<McpSchema.Prompt> updatedPrompts) {
 *     // Handle prompt list change notification asynchronously
 *     return processUpdatedPrompts(updatedPrompts);
 * }
 * }</pre>
 *
 * @author Christian Tzolov
 * @see <a href=
 * "https://modelcontextprotocol.io/specification/2025-06-18/server/prompts#list-changed-notification">MCP
 * Prompt List Changed Notification</a>
 */
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface McpPromptListChanged {

	/**
	 * Used as connection or client identifier to select the MCP client that the prompt
	 * change listener is associated with. At least one client identifier must be
	 * specified.
	 * @return the client identifier, or empty string to listen to all clients
	 */
	String[] clients();

}
