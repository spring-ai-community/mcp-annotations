package org.springaicommunity.mcp;

import org.junit.jupiter.api.Test;
import org.springaicommunity.mcp.context.DefaultMetaProvider;
import org.springaicommunity.mcp.context.MetaProvider;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class MetaUtilsTest {

	@Test
	void testGetMetaNonNull() {

		Map<String, Object> actual = MetaUtils.getMeta(MetaProviderWithDefaultConstructor.class);

		assertThat(actual).containsExactlyInAnyOrderEntriesOf(new MetaProviderWithDefaultConstructor().getMeta());
	}

	@Test
	void testGetMetaWithPublicConstructor() {

		Map<String, Object> actual = MetaUtils.getMeta(MetaProviderWithAvailableConstructor.class);

		assertThat(actual).containsExactlyInAnyOrderEntriesOf(new MetaProviderWithAvailableConstructor().getMeta());
	}

	@Test
	void testGetMetaWithUnavailableConstructor() {

		assertThatIllegalArgumentException()
			.isThrownBy(() -> MetaUtils.getMeta(MetaProviderWithUnavailableConstructor.class))
			.withMessage(
					"org.springaicommunity.mcp.MetaUtilsTest$MetaProviderWithUnavailableConstructor instantiation failed");
	}

	@Test
	void testGetMetaWithConstructorWithWrongSignature() {

		assertThatIllegalArgumentException()
			.isThrownBy(() -> MetaUtils.getMeta(MetaProviderWithConstructorWithWrongSignature.class))
			.withMessage(
					"Required no-arg constructor not found in org.springaicommunity.mcp.MetaUtilsTest$MetaProviderWithConstructorWithWrongSignature");
	}

	@Test
	void testGetMetaNull() {

		Map<String, Object> actual = MetaUtils.getMeta(DefaultMetaProvider.class);

		assertThat(actual).isNull();
	}

	@Test
	void testMetaProviderClassIsNullReturnsNull() {

		Map<String, Object> actual = MetaUtils.getMeta(null);

		assertThat(actual).isNull();
	}

	static class MetaProviderWithDefaultConstructor implements MetaProvider {

		@Override
		public Map<String, Object> getMeta() {
			return Map.of("a", "1", "b", "2");
		}

	}

	@SuppressWarnings("unused")
	static class MetaProviderWithAvailableConstructor extends MetaProviderWithDefaultConstructor {

		public MetaProviderWithAvailableConstructor() {
			// Nothing to do here
		}

	}

	@SuppressWarnings("unused")
	static class MetaProviderWithUnavailableConstructor extends MetaProviderWithDefaultConstructor {

		private MetaProviderWithUnavailableConstructor() {
			// Nothing to do here
		}

	}

	@SuppressWarnings("unused")
	static class MetaProviderWithConstructorWithWrongSignature extends MetaProviderWithDefaultConstructor {

		private MetaProviderWithConstructorWithWrongSignature(int invalid) {
			// Nothing to do here
		}

	}

}
