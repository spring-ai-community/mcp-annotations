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
 * Marks a method as a MCP Prompt.
 *
 * @author Christian Tzolov
 */
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface McpPrompt {

	/**
	 * Unique identifier for the prompt
	 */
	String name() default "";

	/**
	 * Optional human-readable name of the prompt for display purposes.
	 */
	String title() default "";

	/**
	 * Optional human-readable description.
	 */
	String description() default "";

}
