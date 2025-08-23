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

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springaicommunity.mcp.annotation.McpMeta;
import org.springaicommunity.mcp.annotation.McpProgressToken;
import org.springaicommunity.mcp.annotation.McpToolParam;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.victools.jsonschema.generator.Option;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import com.github.victools.jsonschema.module.jackson.JacksonModule;
import com.github.victools.jsonschema.module.jackson.JacksonOption;
import com.github.victools.jsonschema.module.swagger2.Swagger2Module;

import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.util.Assert;
import io.modelcontextprotocol.util.Utils;
import io.swagger.v3.oas.annotations.media.Schema;
import reactor.util.annotation.Nullable;

import com.github.victools.jsonschema.generator.Module;

public class JsonSchemaGenerator {

	private static final boolean PROPERTY_REQUIRED_BY_DEFAULT = true;

	private static final SchemaGenerator TYPE_SCHEMA_GENERATOR;

	private static final SchemaGenerator SUBTYPE_SCHEMA_GENERATOR;

	private static final Map<Method, String> methodSchemaCache = new ConcurrentReferenceHashMap<>(256);

	private static final Map<Class<?>, String> classSchemaCache = new ConcurrentReferenceHashMap<>(256);

	/*
	 * Initialize JSON Schema generators.
	 */
	static {
		Module jacksonModule = new JacksonModule(JacksonOption.RESPECT_JSONPROPERTY_REQUIRED);
		Module openApiModule = new Swagger2Module();
		Module springAiSchemaModule = PROPERTY_REQUIRED_BY_DEFAULT ? new SpringAiSchemaModule()
				: new SpringAiSchemaModule(SpringAiSchemaModule.Option.PROPERTY_REQUIRED_FALSE_BY_DEFAULT);

		SchemaGeneratorConfigBuilder schemaGeneratorConfigBuilder = new SchemaGeneratorConfigBuilder(
				SchemaVersion.DRAFT_2020_12, OptionPreset.PLAIN_JSON)
			.with(jacksonModule)
			.with(openApiModule)
			.with(springAiSchemaModule)
			.with(Option.EXTRA_OPEN_API_FORMAT_VALUES)
			.with(Option.STANDARD_FORMATS)
			.with(Option.PLAIN_DEFINITION_KEYS);

		SchemaGeneratorConfig typeSchemaGeneratorConfig = schemaGeneratorConfigBuilder.build();
		TYPE_SCHEMA_GENERATOR = new SchemaGenerator(typeSchemaGeneratorConfig);

		SchemaGeneratorConfig subtypeSchemaGeneratorConfig = schemaGeneratorConfigBuilder
			.without(Option.SCHEMA_VERSION_INDICATOR)
			.build();
		SUBTYPE_SCHEMA_GENERATOR = new SchemaGenerator(subtypeSchemaGeneratorConfig);
	}

	public static String generateForMethodInput(Method method) {
		Assert.notNull(method, "method cannot be null");
		return methodSchemaCache.computeIfAbsent(method, JsonSchemaGenerator::internalGenerateFromMethodArguments);
	}

	private static String internalGenerateFromMethodArguments(Method method) {
		// Check if method has CallToolRequest parameter
		boolean hasCallToolRequestParam = Arrays.stream(method.getParameterTypes())
			.anyMatch(type -> CallToolRequest.class.isAssignableFrom(type));

		// If method has CallToolRequest, return minimal schema
		if (hasCallToolRequestParam) {
			// Check if there are other parameters besides CallToolRequest, exchange
			// types,
			// @McpProgressToken annotated parameters, and McpMeta parameters
			boolean hasOtherParams = Arrays.stream(method.getParameters()).anyMatch(param -> {
				Class<?> type = param.getType();
				return !CallToolRequest.class.isAssignableFrom(type)
						&& !McpSyncServerExchange.class.isAssignableFrom(type)
						&& !McpAsyncServerExchange.class.isAssignableFrom(type)
						&& !param.isAnnotationPresent(McpProgressToken.class) && !McpMeta.class.isAssignableFrom(type);
			});

			// If only CallToolRequest (and possibly exchange), return empty schema
			if (!hasOtherParams) {
				ObjectNode schema = JsonParser.getObjectMapper().createObjectNode();
				schema.put("type", "object");
				schema.putObject("properties");
				schema.putArray("required");
				return schema.toPrettyString();
			}
		}

		ObjectNode schema = JsonParser.getObjectMapper().createObjectNode();
		schema.put("$schema", SchemaVersion.DRAFT_2020_12.getIdentifier());
		schema.put("type", "object");

		ObjectNode properties = schema.putObject("properties");
		List<String> required = new ArrayList<>();

		for (int i = 0; i < method.getParameterCount(); i++) {
			Parameter parameter = method.getParameters()[i];
			String parameterName = parameter.getName();
			Type parameterType = method.getGenericParameterTypes()[i];

			// Skip parameters annotated with @McpProgressToken
			if (parameter.isAnnotationPresent(McpProgressToken.class)) {
				continue;
			}

			// Skip McpMeta parameters
			if (parameterType instanceof Class<?> parameterClass && McpMeta.class.isAssignableFrom(parameterClass)) {
				continue;
			}

			// Skip special parameter types
			if (parameterType instanceof Class<?> parameterClass
					&& (ClassUtils.isAssignable(McpSyncServerExchange.class, parameterClass)
							|| ClassUtils.isAssignable(McpAsyncServerExchange.class, parameterClass)
							|| ClassUtils.isAssignable(CallToolRequest.class, parameterClass))) {
				continue;
			}

			if (isMethodParameterRequired(method, i)) {
				required.add(parameterName);
			}
			ObjectNode parameterNode = SUBTYPE_SCHEMA_GENERATOR.generateSchema(parameterType);
			String parameterDescription = getMethodParameterDescription(method, i);
			if (Utils.hasText(parameterDescription)) {
				parameterNode.put("description", parameterDescription);
			}
			properties.set(parameterName, parameterNode);
		}

		var requiredArray = schema.putArray("required");
		required.forEach(requiredArray::add);

		return schema.toPrettyString();
	}

	public static String generateFromClass(Class<?> clazz) {
		Assert.notNull(clazz, "clazz cannot be null");
		return classSchemaCache.computeIfAbsent(clazz, JsonSchemaGenerator::internalGenerateFromClass);
	}

	private static String internalGenerateFromClass(Class<?> clazz) {
		SchemaGeneratorConfigBuilder configBuilder = new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_2020_12,
				OptionPreset.PLAIN_JSON);
		SchemaGeneratorConfig config = configBuilder.with(Option.EXTRA_OPEN_API_FORMAT_VALUES)
			.without(Option.FLATTENED_ENUMS_FROM_TOSTRING)
			.build();

		SchemaGenerator generator = new SchemaGenerator(config);
		JsonNode jsonSchema = generator.generateSchema(clazz);
		return jsonSchema.toPrettyString();
	}

	/**
	 * Check if a method has a CallToolRequest parameter.
	 * @param method The method to check
	 * @return true if the method has a CallToolRequest parameter, false otherwise
	 */
	public static boolean hasCallToolRequestParameter(Method method) {
		return Arrays.stream(method.getParameterTypes()).anyMatch(type -> CallToolRequest.class.isAssignableFrom(type));
	}

	private static boolean isMethodParameterRequired(Method method, int index) {
		Parameter parameter = method.getParameters()[index];

		var toolParamAnnotation = parameter.getAnnotation(McpToolParam.class);
		if (toolParamAnnotation != null) {
			return toolParamAnnotation.required();
		}

		var propertyAnnotation = parameter.getAnnotation(JsonProperty.class);
		if (propertyAnnotation != null) {
			return propertyAnnotation.required();
		}

		var schemaAnnotation = parameter.getAnnotation(Schema.class);
		if (schemaAnnotation != null) {
			return schemaAnnotation.requiredMode() == Schema.RequiredMode.REQUIRED
					|| schemaAnnotation.requiredMode() == Schema.RequiredMode.AUTO || schemaAnnotation.required();
		}

		var nullableAnnotation = parameter.getAnnotation(Nullable.class);
		if (nullableAnnotation != null) {
			return false;
		}

		return PROPERTY_REQUIRED_BY_DEFAULT;
	}

	private static String getMethodParameterDescription(Method method, int index) {
		Parameter parameter = method.getParameters()[index];

		var toolParamAnnotation = parameter.getAnnotation(McpToolParam.class);
		if (toolParamAnnotation != null && Utils.hasText(toolParamAnnotation.description())) {
			return toolParamAnnotation.description();
		}

		var jacksonAnnotation = parameter.getAnnotation(JsonPropertyDescription.class);
		if (jacksonAnnotation != null && Utils.hasText(jacksonAnnotation.value())) {
			return jacksonAnnotation.value();
		}

		var schemaAnnotation = parameter.getAnnotation(Schema.class);
		if (schemaAnnotation != null && Utils.hasText(schemaAnnotation.description())) {
			return schemaAnnotation.description();
		}

		return null;
	}

}
