# MCP Annotations

The MCP Annotations project provides annotation-based method handling for Model Context Protocol (MCP) servers in Java. This project consists of two main modules:

1. **mcp-annotations** - Core annotations and method handling for MCP operations
2. **spring-ai-mcp-annotations** - Spring AI integration for MCP annotations

## Overview

The MCP Annotations project enables developers to easily create and register methods for handling MCP operations using simple annotations. It provides a clean, declarative approach to implementing MCP server functionality, reducing boilerplate code and improving maintainability.

### Core Module (mcp-annotations)

The core module provides a set of annotations and callback implementations for three primary MCP operations:

1. **Complete** - For auto-completion functionality in prompts and URI templates
2. **Prompt** - For generating prompt messages
3. **Resource** - For accessing resources via URI templates

Each operation type has both synchronous and asynchronous implementations, allowing for flexible integration with different application architectures.

### Spring Integration Module (spring-ai-mcp-annotations)

The Spring integration module provides seamless integration with Spring AI and Spring Framework applications. It handles Spring-specific concerns such as AOP proxies and integrates with Spring AI's model abstractions.

## Key Components

### Annotations

- **`@McpComplete`** - Annotates methods that provide completion functionality for prompts or URI templates
- **`@McpPrompt`** - Annotates methods that generate prompt messages
- **`@McpResource`** - Annotates methods that provide access to resources
- **`@McpArg`** - Annotates method parameters as MCP arguments

### Method Callbacks

The modules provide callback implementations for each operation type:

#### Complete
- `AbstractMcpCompleteMethodCallback` - Base class for complete method callbacks
- `SyncMcpCompleteMethodCallback` - Synchronous implementation
- `AsyncMcpCompleteMethodCallback` - Asynchronous implementation using Reactor's Mono

#### Prompt
- `AbstractMcpPromptMethodCallback` - Base class for prompt method callbacks
- `SyncMcpPromptMethodCallback` - Synchronous implementation
- `AsyncMcpPromptMethodCallback` - Asynchronous implementation using Reactor's Mono

#### Resource
- `AbstractMcpResourceMethodCallback` - Base class for resource method callbacks
- `SyncMcpResourceMethodCallback` - Synchronous implementation
- `AsyncMcpResourceMethodCallback` - Asynchronous implementation using Reactor's Mono

### Providers

The project includes provider classes that scan for annotated methods and create appropriate callbacks:

- `SyncMcpCompletionProvider` - Processes `@McpComplete` annotations for synchronous operations
- `SyncMcpPromptProvider` - Processes `@McpPrompt` annotations for synchronous operations
- `SyncMcpResourceProvider` - Processes `@McpResource` annotations for synchronous operations

### Spring Integration

The Spring integration module provides:

- `SpringAiMcpAnnotationProvider` - Handles Spring-specific concerns when processing MCP annotations
- Integration with Spring AOP proxies
- Support for Spring AI model abstractions

## Usage Examples

### Complete Example

```java
public class AutocompleteProvider {
    private final Map<String, List<String>> cityDatabase = new HashMap<>();
    
    public AutocompleteProvider() {
        // Initialize with sample data
        cityDatabase.put("l", List.of("Lagos", "Lima", "Lisbon", "London", "Los Angeles"));
        // Add more data...
    }
    
    @McpComplete(prompt = "travel-planner")
    public List<String> completeCityName(CompleteRequest.CompleteArgument argument) {
        String prefix = argument.value().toLowerCase();
        if (prefix.isEmpty()) {
            return List.of("Enter a city name");
        }
        
        String firstLetter = prefix.substring(0, 1);
        List<String> cities = cityDatabase.getOrDefault(firstLetter, List.of());
        
        return cities.stream()
            .filter(city -> city.toLowerCase().startsWith(prefix))
            .toList();
    }
    
    @McpComplete(uri = "weather-api://{city}")
    public List<String> completeCity(CompleteRequest.CompleteArgument argument) {
        // Similar implementation for URI template completion
        // ...
    }
}
```

### Registering Complete Methods

```java
// Create the autocomplete provider
AutocompleteProvider provider = new AutocompleteProvider();

// Register a method with SyncMcpCompleteMethodCallback
Method method = AutocompleteProvider.class.getMethod("completeCityName", CompleteRequest.CompleteArgument.class);
McpComplete annotation = method.getAnnotation(McpComplete.class);

BiFunction<McpSyncServerExchange, CompleteRequest, CompleteResult> callback = 
    SyncMcpCompleteMethodCallback.builder()
        .method(method)
        .bean(provider)
        .complete(annotation)
        .build();

// Use the callback with your MCP server
```

### Async Complete Example

```java
public class AsyncAutocompleteProvider {
    // ...
    
    @McpComplete(prompt = "travel-planner")
    public Mono<List<String>> completeCityNameAsync(CompleteRequest.CompleteArgument argument) {
        return Mono.fromCallable(() -> {
            // Implementation similar to sync version
            // ...
        });
    }
}
```

### Resource Example

```java
public class WeatherResourceProvider {
    private final WeatherService weatherService;
    
    public WeatherResourceProvider(WeatherService weatherService) {
        this.weatherService = weatherService;
    }
    
    @McpResource(
        name = "Current Weather",
        uri = "weather-api://{city}",
        description = "Get current weather for a city",
        mimeType = "application/json"
    )
    public String getCurrentWeather(@McpArg(name = "city", required = true) String city) {
        return weatherService.getWeatherForCity(city);
    }
}
```

### Spring Integration Example

```java
@Configuration
public class McpConfig {
    
    @Bean
    public List<SyncCompletionSpecification> syncCompletionSpecifications(
            List<AutocompleteProvider> completeProviders) {
        return SpringAiMcpAnnotationProvider.createSyncCompleteSpecifications(
            new ArrayList<>(completeProviders));
    }
    
    @Bean
    public List<SyncPromptSpecification> syncPromptSpecifications(
            List<PromptProvider> promptProviders) {
        return SpringAiMcpAnnotationProvider.createSyncPromptSpecifications(
            new ArrayList<>(promptProviders));
    }
    
    @Bean
    public List<SyncResourceSpecification> syncResourceSpecifications(
            List<ResourceProvider> resourceProviders) {
        return SpringAiMcpAnnotationProvider.createSyncResourceSpecifications(
            new ArrayList<>(resourceProviders));
    }
}
```

## Installation

### Core Module

To use the MCP Annotations core module in your project, add the following dependency to your Maven POM file:

```xml
<dependency>
    <groupId>com.logaritex.mcp</groupId>
    <artifactId>mcp-annotations</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### Spring Integration Module

To use the Spring integration module, add the following dependency:

```xml
<dependency>
    <groupId>com.logaritex.mcp</groupId>
    <artifactId>spring-ai-mcp-annotations</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

## Features

- **Annotation-based method handling** - Simplifies the creation and registration of MCP methods
- **Support for both synchronous and asynchronous operations** - Flexible integration with different application architectures
- **Builder pattern for callback creation** - Clean and fluent API for creating method callbacks
- **Comprehensive validation** - Ensures method signatures are compatible with MCP operations
- **URI template support** - Powerful URI template handling for resource and completion operations
- **Spring integration** - Seamless integration with Spring Framework and Spring AI
- **AOP proxy support** - Proper handling of Spring AOP proxies when processing annotations

## Requirements

- Java 17 or higher
- Reactor Core (for async operations)
- Spring Framework and Spring AI (for spring-ai-mcp-annotations module)

## License

This project is licensed under the MIT License - see the LICENSE file for details.
