/*
 * Copyright 2025-2025 the original author or authors.
 */

package org.springaicommunity.mcp.context;

import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link DefaultElicitationSpec}.
 *
 * @author Christian Tzolov
 */
public class DefaultElicitationSpecTests {

	@Test
	public void testMessageSetting() {
		DefaultElicitationSpec spec = new DefaultElicitationSpec();

		spec.message("Test message");

		assertThat(spec.message).isEqualTo("Test message");
	}

	@Test
	public void testMessageWithEmptyString() {
		DefaultElicitationSpec spec = new DefaultElicitationSpec();

		assertThatThrownBy(() -> spec.message("")).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Message must not be empty");
	}

	@Test
	public void testMessageWithNull() {
		DefaultElicitationSpec spec = new DefaultElicitationSpec();

		assertThatThrownBy(() -> spec.message(null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Message must not be empty");
	}

	@Test
	public void testResponseTypeSetting() {
		DefaultElicitationSpec spec = new DefaultElicitationSpec();

		spec.responseType(String.class);

		assertThat(spec.responseType).isEqualTo(String.class);
	}

	@Test
	public void testResponseTypeWithNull() {
		DefaultElicitationSpec spec = new DefaultElicitationSpec();

		assertThatThrownBy(() -> spec.responseType(null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Response type must not be null");
	}

	@Test
	public void testMetaWithMap() {
		DefaultElicitationSpec spec = new DefaultElicitationSpec();
		Map<String, Object> metaMap = Map.of("key1", "value1", "key2", "value2");

		spec.meta(metaMap);

		assertThat(spec.meta).containsEntry("key1", "value1").containsEntry("key2", "value2");
	}

	@Test
	public void testMetaWithNullMap() {
		DefaultElicitationSpec spec = new DefaultElicitationSpec();

		assertThatThrownBy(() -> spec.meta((Map<String, Object>) null)).isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("Meta map must not be null");
	}

	@Test
	public void testMetaWithKeyValue() {
		DefaultElicitationSpec spec = new DefaultElicitationSpec();

		spec.meta("key", "value");

		assertThat(spec.meta).containsEntry("key", "value");
	}

	@Test
	public void testMetaWithNullKey() {
		DefaultElicitationSpec spec = new DefaultElicitationSpec();

		spec.meta(null, "value");

		assertThat(spec.meta).isEmpty();
	}

	@Test
	public void testMetaWithNullValue() {
		DefaultElicitationSpec spec = new DefaultElicitationSpec();

		spec.meta("key", null);

		assertThat(spec.meta).isEmpty();
	}

	@Test
	public void testMetaMultipleEntries() {
		DefaultElicitationSpec spec = new DefaultElicitationSpec();

		spec.meta("key1", "value1").meta("key2", "value2").meta("key3", "value3");

		assertThat(spec.meta).hasSize(3)
			.containsEntry("key1", "value1")
			.containsEntry("key2", "value2")
			.containsEntry("key3", "value3");
	}

	@Test
	public void testFluentInterface() {
		DefaultElicitationSpec spec = new DefaultElicitationSpec();

		McpSyncRequestContext.ElicitationSpec result = spec.message("Test message")
			.responseType(String.class)
			.meta("key", "value");

		assertThat(result).isSameAs(spec);
		assertThat(spec.message).isEqualTo("Test message");
		assertThat(spec.responseType).isEqualTo(String.class);
		assertThat(spec.meta).containsEntry("key", "value");
	}

}
