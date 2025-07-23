/*
 * Copyright 2025-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springaicommunity.mcp.method.tool.utils;

import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.temporal.Temporal;
import java.util.Currency;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Pattern;

import io.modelcontextprotocol.util.Assert;

public abstract class ClassUtils {

	/**
	 * Map with primitive wrapper type as key and corresponding primitive type as value,
	 * for example: {@code Integer.class -> int.class}.
	 */
	private static final Map<Class<?>, Class<?>> primitiveWrapperTypeMap = new IdentityHashMap<>(9);

	/**
	 * Map with primitive type as key and corresponding wrapper type as value, for
	 * example: {@code int.class -> Integer.class}.
	 */
	private static final Map<Class<?>, Class<?>> primitiveTypeToWrapperMap = new IdentityHashMap<>(9);

	static {
		primitiveWrapperTypeMap.put(Boolean.class, boolean.class);
		primitiveWrapperTypeMap.put(Byte.class, byte.class);
		primitiveWrapperTypeMap.put(Character.class, char.class);
		primitiveWrapperTypeMap.put(Double.class, double.class);
		primitiveWrapperTypeMap.put(Float.class, float.class);
		primitiveWrapperTypeMap.put(Integer.class, int.class);
		primitiveWrapperTypeMap.put(Long.class, long.class);
		primitiveWrapperTypeMap.put(Short.class, short.class);
		primitiveWrapperTypeMap.put(Void.class, void.class);

		// Map entry iteration is less expensive to initialize than forEach with lambdas
		for (Map.Entry<Class<?>, Class<?>> entry : primitiveWrapperTypeMap.entrySet()) {
			primitiveTypeToWrapperMap.put(entry.getValue(), entry.getKey());
		}
	}

	/**
	 * Check if the given class represents a primitive wrapper, i.e. Boolean, Byte,
	 * Character, Short, Integer, Long, Float, Double, or Void.
	 * @param clazz the class to check
	 * @return whether the given class is a primitive wrapper class
	 */
	public static boolean isPrimitiveWrapper(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		return primitiveWrapperTypeMap.containsKey(clazz);
	}

	/**
	 * Check if the given class represents a primitive (i.e. boolean, byte, char, short,
	 * int, long, float, or double), {@code void}, or a wrapper for those types (i.e.
	 * Boolean, Byte, Character, Short, Integer, Long, Float, Double, or Void).
	 * @param clazz the class to check
	 * @return {@code true} if the given class represents a primitive, void, or a wrapper
	 * class
	 */
	public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		return (clazz.isPrimitive() || isPrimitiveWrapper(clazz));
	}

	/**
	 * Resolve the given class if it is a primitive class, returning the corresponding
	 * primitive wrapper type instead.
	 * @param clazz the class to check
	 * @return the original class, or a primitive wrapper for the original primitive type
	 */
	@SuppressWarnings("NullAway")
	public static Class<?> resolvePrimitiveIfNecessary(Class<?> clazz) {
		Assert.notNull(clazz, "Class must not be null");
		return (clazz.isPrimitive() && clazz != void.class ? primitiveTypeToWrapperMap.get(clazz) : clazz);
	}

	/**
	 * Determine if the given type represents either {@code Void} or {@code void}.
	 * @param type the type to check
	 * @return {@code true} if the type represents {@code Void} or {@code void}
	 * @since 6.1.4
	 * @see Void
	 * @see Void#TYPE
	 */
	public static boolean isVoidType(Class<?> type) {
		return (type == void.class || type == Void.class);
	}

	/**
	 * Delegate for {@link org.springframework.beans.BeanUtils#isSimpleValueType}. Also
	 * used by {@link ObjectUtils#nullSafeConciseToString}.
	 * <p>
	 * Check if the given type represents a common "simple" value type: primitive or
	 * primitive wrapper, {@link Enum}, {@link String} or other {@link CharSequence},
	 * {@link Number}, {@link Date}, {@link Temporal}, {@link ZoneId}, {@link TimeZone},
	 * {@link File}, {@link Path}, {@link URI}, {@link URL}, {@link InetAddress},
	 * {@link Charset}, {@link Currency}, {@link Locale}, {@link UUID}, {@link Pattern},
	 * or {@link Class}.
	 * <p>
	 * {@code Void} and {@code void} are not considered simple value types.
	 * @param type the type to check
	 * @return whether the given type represents a "simple" value type, suggesting
	 * value-based data binding and {@code toString} output
	 * @since 6.1
	 */
	public static boolean isSimpleValueType(Class<?> type) {
		return (!isVoidType(type) && (isPrimitiveOrWrapper(type) || Enum.class.isAssignableFrom(type)
				|| CharSequence.class.isAssignableFrom(type) || Number.class.isAssignableFrom(type)
				|| Date.class.isAssignableFrom(type) || Temporal.class.isAssignableFrom(type)
				|| ZoneId.class.isAssignableFrom(type) || TimeZone.class.isAssignableFrom(type)
				|| File.class.isAssignableFrom(type) || Path.class.isAssignableFrom(type)
				|| Charset.class.isAssignableFrom(type) || Currency.class.isAssignableFrom(type)
				|| InetAddress.class.isAssignableFrom(type) || URI.class == type || URL.class == type
				|| UUID.class == type || Locale.class == type || Pattern.class == type || Class.class == type));
	}

	/**
	 * Check if the right-hand side type may be assigned to the left-hand side type,
	 * assuming setting by reflection. Considers primitive wrapper classes as assignable
	 * to the corresponding primitive types.
	 * @param lhsType the target type (left-hand side (LHS) type)
	 * @param rhsType the value type (right-hand side (RHS) type) that should be assigned
	 * to the target type
	 * @return {@code true} if {@code rhsType} is assignable to {@code lhsType}
	 * @see TypeUtils#isAssignable(java.lang.reflect.Type, java.lang.reflect.Type)
	 */
	public static boolean isAssignable(Class<?> lhsType, Class<?> rhsType) {
		Assert.notNull(lhsType, "Left-hand side type must not be null");
		Assert.notNull(rhsType, "Right-hand side type must not be null");
		if (lhsType.isAssignableFrom(rhsType)) {
			return true;
		}
		if (lhsType.isPrimitive()) {
			Class<?> resolvedPrimitive = primitiveWrapperTypeMap.get(rhsType);
			return (lhsType == resolvedPrimitive);
		}
		else if (rhsType.isPrimitive()) {
			Class<?> resolvedWrapper = primitiveTypeToWrapperMap.get(rhsType);
			return (resolvedWrapper != null && lhsType.isAssignableFrom(resolvedWrapper));
		}
		return false;
	}

}
