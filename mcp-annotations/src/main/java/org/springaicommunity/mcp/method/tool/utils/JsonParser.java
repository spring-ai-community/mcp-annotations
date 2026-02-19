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

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.Map;

import io.modelcontextprotocol.util.Assert;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

/**
 * Utilities to perform parsing operations between JSON and Java.
 */
public final class JsonParser {

	private static TypeReference<Map<String, Object>> MAP_TYPE_REF = new TypeReference<Map<String, Object>>() {
	};

	public static Map<String, Object> convertObjectToMap(Object object) {
		Assert.notNull(object, "object cannot be null");
		return OBJECT_MAPPER.convertValue(object, MAP_TYPE_REF);
	}

	private static final JsonMapper OBJECT_MAPPER = JsonMapper.builder()
		.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
		.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
		.disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
		.addModule(new SimpleModule())
		.build();

	private JsonParser() {
	}

	/**
	 * Returns a Jackson {@link JsonMapper} instance tailored for JSON-parsing operations
	 * for tool calling and structured output.
	 */
	public static JsonMapper getObjectMapper() {
		return OBJECT_MAPPER;
	}

	/**
	 * Converts a JSON string to a Java object.
	 */
	public static <T> T fromJson(String json, Class<T> type) {
		Assert.notNull(json, "json cannot be null");
		Assert.notNull(type, "type cannot be null");

		try {
			return OBJECT_MAPPER.readValue(json, type);
		}
		catch (JacksonException ex) {
			throw new IllegalStateException("Conversion from JSON to %s failed".formatted(type.getName()), ex);
		}
	}

	/**
	 * Converts a JSON string to a Java object.
	 */
	public static <T> T fromJson(String json, Type type) {
		Assert.notNull(json, "json cannot be null");
		Assert.notNull(type, "type cannot be null");

		try {
			return OBJECT_MAPPER.readValue(json, OBJECT_MAPPER.constructType(type));
		}
		catch (JacksonException ex) {
			throw new IllegalStateException("Conversion from JSON to %s failed".formatted(type.getTypeName()), ex);
		}
	}

	/**
	 * Converts a JSON string to a Java object.
	 */
	public static <T> T fromJson(String json, TypeReference<T> type) {
		Assert.notNull(json, "json cannot be null");
		Assert.notNull(type, "type cannot be null");

		try {
			return OBJECT_MAPPER.readValue(json, type);
		}
		catch (JacksonException ex) {
			throw new IllegalStateException("Conversion from JSON to %s failed".formatted(type.getType().getTypeName()),
					ex);
		}
	}

	/**
	 * Checks if a string is a valid JSON string.
	 */
	private static boolean isValidJson(String input) {
		try {
			OBJECT_MAPPER.readTree(input);
			return true;
		}
		catch (JacksonException e) {
			return false;
		}
	}

	/**
	 * Converts a Java object to a JSON string if it's not already a valid JSON string.
	 */
	public static String toJson(Object object) {
		if (object instanceof String && isValidJson((String) object)) {
			return (String) object;
		}
		try {
			return OBJECT_MAPPER.writeValueAsString(object);
		}
		catch (JacksonException ex) {
			throw new IllegalStateException("Conversion from Object to JSON failed", ex);
		}
	}

	/**
	 * Convert a Java Object to a typed Object. Based on the implementation in
	 * MethodToolCallback.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Object toTypedObject(Object value, Class<?> type) {
		Assert.notNull(value, "value cannot be null");
		Assert.notNull(type, "type cannot be null");

		var javaType = ClassUtils.resolvePrimitiveIfNecessary(type);

		if (javaType == String.class) {
			return value.toString();
		}
		else if (javaType == Byte.class) {
			return Byte.parseByte(value.toString());
		}
		else if (javaType == Integer.class) {
			BigDecimal bigDecimal = new BigDecimal(value.toString());
			return bigDecimal.intValueExact();
		}
		else if (javaType == Short.class) {
			return Short.parseShort(value.toString());
		}
		else if (javaType == Long.class) {
			BigDecimal bigDecimal = new BigDecimal(value.toString());
			return bigDecimal.longValueExact();
		}
		else if (javaType == Double.class) {
			return Double.parseDouble(value.toString());
		}
		else if (javaType == Float.class) {
			return Float.parseFloat(value.toString());
		}
		else if (javaType == Boolean.class) {
			return Boolean.parseBoolean(value.toString());
		}
		else if (javaType.isEnum()) {
			return Enum.valueOf((Class<Enum>) javaType, value.toString());
		}

		String json = JsonParser.toJson(value);
		return JsonParser.fromJson(json, javaType);
	}

	public static <T> T convertMapToType(Map<String, Object> map, Class<T> targetType) {
		JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructType(targetType);
		return OBJECT_MAPPER.convertValue(map, javaType);
	}

	public static <T> T convertMapToType(Map<String, Object> map, TypeReference<T> targetType) {
		JavaType javaType = OBJECT_MAPPER.getTypeFactory().constructType(targetType);
		return OBJECT_MAPPER.convertValue(map, javaType);
	}

}
