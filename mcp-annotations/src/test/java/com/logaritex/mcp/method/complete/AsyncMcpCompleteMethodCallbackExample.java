/*
 * Copyright 2025-2025 the original author or authors.
 */

package com.logaritex.mcp.method.complete;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.logaritex.mcp.annotation.McpComplete;
import com.logaritex.mcp.method.complete.AsyncMcpCompleteMethodCallback;
import io.modelcontextprotocol.server.McpAsyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema.CompleteRequest;
import io.modelcontextprotocol.spec.McpSchema.CompleteResult;
import io.modelcontextprotocol.spec.McpSchema.CompleteResult.CompleteCompletion;
import io.modelcontextprotocol.spec.McpSchema.PromptReference;
import io.modelcontextprotocol.spec.McpSchema.ResourceReference;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

/**
 * Example demonstrating how to use the {@link AsyncMcpCompleteMethodCallback} with
 * {@link McpComplete} annotations.
 *
 * @author Christian Tzolov
 */
public class AsyncMcpCompleteMethodCallbackExample {

	/**
	 * A sample completion provider class with methods annotated with {@link McpComplete}.
	 */
	public static class AsyncAutocompleteProvider {

		private final Map<String, List<String>> cityDatabase = new HashMap<>();

		private final Map<String, List<String>> countryDatabase = new HashMap<>();

		private final Map<String, List<String>> languageDatabase = new HashMap<>();

		public AsyncAutocompleteProvider() {
			// Initialize with some sample data
			cityDatabase.put("a", List.of("Amsterdam", "Athens", "Atlanta", "Austin"));
			cityDatabase.put("b", List.of("Barcelona", "Berlin", "Boston", "Brussels"));
			cityDatabase.put("c", List.of("Cairo", "Calgary", "Cape Town", "Chicago"));
			cityDatabase.put("l", List.of("Lagos", "Lima", "Lisbon", "London", "Los Angeles"));
			cityDatabase.put("n", List.of("Nairobi", "Nashville", "New Delhi", "New York"));
			cityDatabase.put("p", List.of("Paris", "Perth", "Phoenix", "Prague"));
			cityDatabase.put("s",
					List.of("San Francisco", "Santiago", "Seattle", "Seoul", "Shanghai", "Singapore", "Sydney"));
			cityDatabase.put("t", List.of("Taipei", "Tokyo", "Toronto"));

			countryDatabase.put("a", List.of("Afghanistan", "Albania", "Algeria", "Argentina", "Australia", "Austria"));
			countryDatabase.put("b", List.of("Bahamas", "Belgium", "Brazil", "Bulgaria"));
			countryDatabase.put("c", List.of("Canada", "Chile", "China", "Colombia", "Croatia"));
			countryDatabase.put("f", List.of("Finland", "France"));
			countryDatabase.put("g", List.of("Germany", "Greece"));
			countryDatabase.put("i", List.of("Iceland", "India", "Indonesia", "Ireland", "Italy"));
			countryDatabase.put("j", List.of("Japan"));
			countryDatabase.put("u", List.of("Uganda", "Ukraine", "United Kingdom", "United States"));

			languageDatabase.put("e", List.of("English"));
			languageDatabase.put("f", List.of("French"));
			languageDatabase.put("g", List.of("German"));
			languageDatabase.put("i", List.of("Italian"));
			languageDatabase.put("j", List.of("Japanese"));
			languageDatabase.put("m", List.of("Mandarin"));
			languageDatabase.put("p", List.of("Portuguese"));
			languageDatabase.put("r", List.of("Russian"));
			languageDatabase.put("s", List.of("Spanish", "Swedish"));
		}

		/**
		 * Complete method for city names in a travel prompt with reactive return type.
		 */
		@McpComplete(prompt = "travel-planner")
		public Mono<List<String>> completeCityNameAsync(CompleteRequest.CompleteArgument argument) {
			return Mono.fromCallable(() -> {
				String prefix = argument.value().toLowerCase();
				if (prefix.isEmpty()) {
					return List.of("Enter a city name");
				}

				String firstLetter = prefix.substring(0, 1);
				List<String> cities = cityDatabase.getOrDefault(firstLetter, List.of());

				return cities.stream().filter(city -> city.toLowerCase().startsWith(prefix)).toList();
			});
		}

		/**
		 * Complete method for country names in a travel prompt with reactive return type.
		 */
		@McpComplete(prompt = "travel-planner")
		public Mono<CompleteResult> completeCountryNameAsync(CompleteRequest request) {
			return Mono.fromCallable(() -> {
				String prefix = request.argument().value().toLowerCase();
				if (prefix.isEmpty()) {
					return new CompleteResult(new CompleteCompletion(List.of("Enter a country name"), 1, false));
				}

				String firstLetter = prefix.substring(0, 1);
				List<String> countries = countryDatabase.getOrDefault(firstLetter, List.of());

				List<String> matches = countries.stream()
					.filter(country -> country.toLowerCase().startsWith(prefix))
					.toList();

				return new CompleteResult(new CompleteCompletion(matches, matches.size(), false));
			});
		}

		/**
		 * Complete method for language names in a translation prompt with reactive return
		 * type.
		 */
		@McpComplete(prompt = "translator")
		public Mono<CompleteCompletion> completeLanguageNameAsync(McpAsyncServerExchange exchange,
				CompleteRequest request) {
			return Mono.fromCallable(() -> {
				String prefix = request.argument().value().toLowerCase();
				if (prefix.isEmpty()) {
					return new CompleteCompletion(List.of("Enter a language"), 1, false);
				}

				String firstLetter = prefix.substring(0, 1);
				List<String> languages = languageDatabase.getOrDefault(firstLetter, List.of());

				List<String> matches = languages.stream()
					.filter(language -> language.toLowerCase().startsWith(prefix))
					.toList();

				return new CompleteCompletion(matches, matches.size(), false);
			});
		}

		/**
		 * Complete method for a simple string value with reactive return type.
		 */
		@McpComplete(prompt = "simple-prompt")
		public Mono<String> completeSimpleValueAsync(String value) {
			return Mono.just("Completed: " + value);
		}

		/**
		 * Complete method for a URI template variable with reactive return type.
		 */
		@McpComplete(uri = "weather-api://{city}")
		public Mono<List<String>> completeCityAsync(CompleteRequest.CompleteArgument argument) {
			return Mono.fromCallable(() -> {
				String prefix = argument.value().toLowerCase();
				if (prefix.isEmpty()) {
					return List.of("Enter a city name");
				}

				String firstLetter = prefix.substring(0, 1);
				List<String> cities = cityDatabase.getOrDefault(firstLetter, List.of());

				return cities.stream().filter(city -> city.toLowerCase().startsWith(prefix)).toList();
			});
		}

		/**
		 * Non-reactive method that returns a direct result.
		 */
		@McpComplete(prompt = "direct-result")
		public List<String> getDirectResult(CompleteRequest.CompleteArgument argument) {
			String prefix = argument.value().toLowerCase();
			if (prefix.isEmpty()) {
				return List.of("Enter a value");
			}

			return List.of("Direct result for: " + prefix);
		}

	}

	/**
	 * Example of how to register complete methods using the
	 * AsyncMcpCompleteMethodCallback.
	 */
	public static void main(String[] args) {
		// Create the autocomplete provider
		AsyncAutocompleteProvider autocompleteProvider = new AsyncAutocompleteProvider();

		// Map to store the prompt completion handlers
		Map<String, BiFunction<McpAsyncServerExchange, CompleteRequest, Mono<CompleteResult>>> promptCompletionHandlers = new HashMap<>();

		// Map to store the URI completion handlers
		Map<String, BiFunction<McpAsyncServerExchange, CompleteRequest, Mono<CompleteResult>>> uriCompletionHandlers = new HashMap<>();

		// Register all methods annotated with @McpComplete
		for (Method method : AsyncAutocompleteProvider.class.getMethods()) {
			McpComplete completeAnnotation = method.getAnnotation(McpComplete.class);

			if (completeAnnotation != null) {
				try {
					// Create a callback for the method using the Builder pattern
					BiFunction<McpAsyncServerExchange, CompleteRequest, Mono<CompleteResult>> callback = AsyncMcpCompleteMethodCallback
						.builder()
						.method(method)
						.bean(autocompleteProvider)
						.complete(completeAnnotation)
						.build();

					// Register the callback with the prompt or URI pattern from the
					// annotation
					if (!completeAnnotation.prompt().isEmpty()) {
						String promptName = completeAnnotation.prompt();
						promptCompletionHandlers.put(promptName + "#" + method.getName(), callback);
						System.out.println("Registered prompt completion handler: " + promptName);
						System.out.println("  Method: " + method.getName());
						System.out.println();
					}
					else if (!completeAnnotation.uri().isEmpty()) {
						String uriPattern = completeAnnotation.uri();
						uriCompletionHandlers.put(uriPattern + "#" + method.getName(), callback);

						// Print information about URI variables if present
						if (uriPattern.contains("{") && uriPattern.contains("}")) {
							System.out.println("  URI Template: " + uriPattern);
							System.out.println("  URI Variables: " + extractUriVariables(uriPattern));
						}

						System.out.println("Registered URI completion handler: " + uriPattern);
						System.out.println("  Method: " + method.getName());
						System.out.println();
					}
				}
				catch (IllegalArgumentException e) {
					System.err
						.println("Failed to create callback for method " + method.getName() + ": " + e.getMessage());
				}
			}
		}

		// Example of using registered prompt handlers
		if (!promptCompletionHandlers.isEmpty()) {
			System.out.println("\nTesting prompt completion handlers:");

			// Test completeCityNameAsync handler
			testPromptHandler(promptCompletionHandlers, "travel-planner#completeCityNameAsync", "l",
					"City name completion");

			// Test completeCountryNameAsync handler
			testPromptHandler(promptCompletionHandlers, "travel-planner#completeCountryNameAsync", "a",
					"Country name completion");

			// Test completeLanguageNameAsync handler
			testPromptHandler(promptCompletionHandlers, "translator#completeLanguageNameAsync", "s",
					"Language name completion");

			// Test completeSimpleValueAsync handler
			testPromptHandler(promptCompletionHandlers, "simple-prompt#completeSimpleValueAsync", "test",
					"Simple value completion");

			// Test getDirectResult handler (non-reactive method)
			testPromptHandler(promptCompletionHandlers, "direct-result#getDirectResult", "test",
					"Direct result completion");
		}

		// Example of using registered URI handlers
		if (!uriCompletionHandlers.isEmpty()) {
			System.out.println("\nTesting URI completion handlers:");

			// Test completeCityAsync handler
			testUriHandler(uriCompletionHandlers, "weather-api://{city}#completeCityAsync", "s",
					"City completion for URI");
		}
	}

	/**
	 * Helper method to test a prompt completion handler.
	 */
	private static void testPromptHandler(
			Map<String, BiFunction<McpAsyncServerExchange, CompleteRequest, Mono<CompleteResult>>> handlers,
			String handlerKey, String input, String description) {

		BiFunction<McpAsyncServerExchange, CompleteRequest, Mono<CompleteResult>> handler = handlers.get(handlerKey);

		if (handler != null) {
			try {
				System.out.println("\nTesting " + description + " with input: " + input);

				// Create a mock exchange
				McpAsyncServerExchange exchange = createMockExchange();

				// Extract prompt name from handler key
				String promptName = handlerKey.split("#")[0];

				// Create a complete request
				CompleteRequest request = new CompleteRequest(new PromptReference(promptName),
						new CompleteRequest.CompleteArgument("value", input));

				// Execute the handler
				Mono<CompleteResult> resultMono = handler.apply(exchange, request);
				CompleteResult result = resultMono.block(); // Block to get the result for
															// this example

				// Print the result
				System.out.println("Completion results:");
				if (result.completion().values().isEmpty()) {
					System.out.println("  No completions found");
				}
				else {
					for (String value : result.completion().values()) {
						System.out.println("  " + value);
					}
					System.out.println("Total: " + result.completion().values().size() + " results");
					if (result.completion().hasMore() != null && result.completion().hasMore()) {
						System.out.println("More results available");
					}
				}
			}
			catch (Exception e) {
				System.out.println("Error executing handler: " + e.getMessage());
				e.printStackTrace();
			}
		}
		else {
			System.out.println("\nNo handler found for key: " + handlerKey);
		}
	}

	/**
	 * Helper method to test a URI completion handler.
	 */
	private static void testUriHandler(
			Map<String, BiFunction<McpAsyncServerExchange, CompleteRequest, Mono<CompleteResult>>> handlers,
			String handlerKey, String input, String description) {

		BiFunction<McpAsyncServerExchange, CompleteRequest, Mono<CompleteResult>> handler = handlers.get(handlerKey);

		if (handler != null) {
			try {
				System.out.println("\nTesting " + description + " with input: " + input);

				// Create a mock exchange
				McpAsyncServerExchange exchange = createMockExchange();

				// Extract URI pattern from handler key
				String uriPattern = handlerKey.split("#")[0];

				// Create a complete request
				CompleteRequest request = new CompleteRequest(new ResourceReference(uriPattern),
						new CompleteRequest.CompleteArgument("city", input));

				// Execute the handler
				Mono<CompleteResult> resultMono = handler.apply(exchange, request);
				CompleteResult result = resultMono.block(); // Block to get the result for
															// this example

				// Print the result
				System.out.println("Completion results:");
				if (result.completion().values().isEmpty()) {
					System.out.println("  No completions found");
				}
				else {
					for (String value : result.completion().values()) {
						System.out.println("  " + value);
					}
					System.out.println("Total: " + result.completion().values().size() + " results");
					if (result.completion().hasMore() != null && result.completion().hasMore()) {
						System.out.println("More results available");
					}
				}
			}
			catch (Exception e) {
				System.out.println("Error executing handler: " + e.getMessage());
				e.printStackTrace();
			}
		}
		else {
			System.out.println("\nNo handler found for key: " + handlerKey);
		}
	}

	/**
	 * Create a simple mock exchange for testing.
	 */
	private static McpAsyncServerExchange createMockExchange() {
		return Mockito.mock(McpAsyncServerExchange.class);
	}

	/**
	 * Extract URI variable names from a URI template.
	 */
	private static List<String> extractUriVariables(String uriTemplate) {
		List<String> variables = new ArrayList<>();
		Pattern pattern = Pattern.compile("\\{([^/]+?)\\}");
		Matcher matcher = pattern.matcher(uriTemplate);

		while (matcher.find()) {
			variables.add(matcher.group(1));
		}

		return variables;
	}

}
