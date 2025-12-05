package org.springaicommunity.mcp.context;

import java.util.Map;

/**
 * Default {@link MetaProvider} implementation that disables the "_meta" field in tool,
 * prompt, resource declarations.
 *
 * <p>
 * This provider deliberately returns {@code null} from {@link #getMeta()} to signal that
 * no "_meta" information is included.
 * </p>
 *
 * <p>
 * Use this when your tool, prompt, or resource does not need to expose any meta
 * information or you want to keep responses minimal by default.
 * </p>
 */
public class DefaultMetaProvider implements MetaProvider {

	/**
	 * Returns {@code null} to indicate that no "_meta" field should be included in.
	 */
	@Override
	public Map<String, Object> getMeta() {
		return null;
	}

}
