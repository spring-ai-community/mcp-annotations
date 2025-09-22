/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp;

import java.util.Objects;

public class ErrorUtils {

	public static Throwable findCauseUsingPlainJava(Throwable throwable) {
		Objects.requireNonNull(throwable);
		Throwable rootCause = throwable;
		while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
			rootCause = rootCause.getCause();
		}
		return rootCause;
	}

}
