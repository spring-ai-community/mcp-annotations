# MCP Method Module

The MCP Method module is part of the Java MCP SDK, providing annotation-based method handling for Model Context Protocol (MCP) servers. This module enables developers to easily create and register methods for handling MCP operations using simple annotations.

## Overview

The MCP Method module provides a set of annotations and callback implementations for three primary MCP operations:

1. **Complete** - For auto-completion functionality in prompts and URI templates
2. **Prompt** - For generating prompt messages
3. **Resource** - For accessing resources via URI templates

Each operation type has both synchronous and asynchronous implementations, allowing for flexible integration with different application architectures.

## Key Components

### Annotations

- **`@McpComplete`** - Annotates methods that provide completion functionality for prompts or URI templates
- **`@McpPrompt`** - Annotates methods that generate prompt messages
- **`@McpResource`** - Annotates methods that provide access to resources
- **`@McpArg`** - Annotates method parameters as MCP arguments

### Method Callbacks

The module provides callback implementations for each operation type:

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

## Installation

To use the MCP Method module in your project, add the following dependency to your Maven POM file:

```xml
<dependency>
    <groupId>io.modelcontextprotocol.sdk</groupId>
    <artifactId>mcp-method</artifactId>
    <version>0.10.0-SNAPSHOT</version>
</dependency>
```

## Features

- **Annotation-based method handling** - Simplifies the creation and registration of MCP methods
- **Support for both synchronous and asynchronous operations** - Flexible integration with different application architectures
- **Builder pattern for callback creation** - Clean and fluent API for creating method callbacks
- **Comprehensive validation** - Ensures method signatures are compatible with MCP operations
- **URI template support** - Powerful URI template handling for resource and completion operations

## Requirements

- Java 17 or higher
- Reactor Core (for async operations)
