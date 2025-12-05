package org.springaicommunity.mcp.context;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DefaultMetaProviderTest {

	@Test
	void testGetMetaReturningNull() {

		DefaultMetaProvider provider = new DefaultMetaProvider();

		Map<String, Object> actual = provider.getMeta();

		assertThat(actual).isNull();
	}

}