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
 * Marks a method parameter as a MCP Argument.
 *
 * @author Christian Tzolov
 */
@Target({ ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface McpArg {

	/**
	 * Argument name.
	 */
	String name() default "";

	/**
	 * Argument description.
	 */
	String description() default "";

	/**
	 * True if this argument is required. false if this argument is optional.
	 */
	boolean required() default false;

}
