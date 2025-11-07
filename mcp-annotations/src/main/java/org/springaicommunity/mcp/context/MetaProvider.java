package org.springaicommunity.mcp.context;

import java.util.Map;

/**
 * Common interface for classes that provide metadata for the "_meta" field. This metadata
 * is used in tool, prompt, and resource declarations.
 */
public interface MetaProvider {

	/**
	 * Returns metadata key-value pairs that will be included in the "_meta" field. These
	 * metadata values provide additional context and information for tools, prompts, and
	 * resource declarations.
	 * @return A Map containing metadata key-value pairs, where keys are strings and
	 * values can be any object type.
	 */
	Map<String, Object> getMeta();

}
