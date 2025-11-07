package org.springaicommunity.mcp;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Map;

import org.springaicommunity.mcp.context.MetaProvider;

/**
 * Utility methods for working with {@link MetaProvider} metadata.
 *
 * <p>
 * This class provides a single entry point {@link #getMeta(Class)} that instantiates the
 * given provider type via a no-argument constructor and returns its metadata as an
 * unmodifiable {@link Map}.
 * </p>
 *
 * <p>
 * Instantiation failures and missing no-arg constructors are reported as
 * {@link IllegalArgumentException IllegalArgumentExceptions}. This class is stateless and
 * not intended to be instantiated.
 * </p>
 */
public final class MetaUtils {

	/** Not intended to be instantiated. */
	private MetaUtils() {
	}

	/**
	 * Instantiate the supplied {@link MetaProvider} type using a no-argument constructor
	 * and return the metadata it supplies.
	 * <p>
	 * The returned map is wrapped in {@link Collections#unmodifiableMap(Map)} to prevent
	 * external modification. If the provider returns {@code null}, this method also
	 * returns {@code null}.
	 * @param metaProviderClass the {@code MetaProvider} implementation class to
	 * instantiate; must provide a no-arg constructor
	 * @return an unmodifiable metadata map, or {@code null} if the provider returns
	 * {@code null}
	 * @throws IllegalArgumentException if a no-arg constructor is missing or the instance
	 * cannot be created
	 */
	public static Map<String, Object> getMeta(Class<? extends MetaProvider> metaProviderClass) {

		if (metaProviderClass == null) {
			return null;
		}

		String className = metaProviderClass.getName();
		MetaProvider metaProvider;
		try {
			// Prefer a public no-arg constructor; fall back to a declared no-arg if
			// accessible
			Constructor<? extends MetaProvider> constructor = getConstructor(metaProviderClass);
			metaProvider = constructor.newInstance();
		}
		catch (NoSuchMethodException e) {
			throw new IllegalArgumentException("Required no-arg constructor not found in " + className, e);
		}
		catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
			throw new IllegalArgumentException(className + " instantiation failed", e);
		}

		Map<String, Object> meta = metaProvider.getMeta();
		return meta == null ? null : Collections.unmodifiableMap(meta);
	}

	/**
	 * Locate a no-argument constructor on the given class: prefer public, otherwise fall
	 * back to a declared no-arg constructor.
	 * @param metaProviderClass the class to inspect
	 * @return the resolved no-arg constructor
	 * @throws NoSuchMethodException if the class does not declare any no-arg constructor
	 */
	private static Constructor<? extends MetaProvider> getConstructor(Class<? extends MetaProvider> metaProviderClass)
			throws NoSuchMethodException {
		try {
			return metaProviderClass.getDeclaredConstructor();
		}
		catch (NoSuchMethodException ex) {
			return metaProviderClass.getConstructor();
		}
	}

}
