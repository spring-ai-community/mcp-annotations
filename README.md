# MCP Annotations

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Maven Central](https://img.shields.io/maven-central/v/org.springaicommunity/mcp-annotations.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/org.springaicommunity/mcp-annotations)
[![Java Version](https://img.shields.io/badge/Java-17%2B-orange)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)

The MCP Annotations project provides annotation-based method handling for [Model Context Protocol (MCP)](https://github.com/modelcontextprotocol/spec) servers in Java. It simplifies the creation and registration of MCP server methods through a clean, declarative approach using Java annotations.

## Table of Contents
- [Overview](#overview)
- [Installation](#installation)
- [Spring AI MCP](#spring-ai-mcp)
- [Key Components](#key-components)
- [Usage Examples](#usage-examples)
- [Features](#features)
- [Requirements](#requirements)
- [Building from Source](#building-from-source)
- [Contributing](#contributing)

This project consists of one module:

1. **mcp-annotations** - Core annotations and method handling for MCP operations. Depends only on MCP Java SDK. 

## Overview

The MCP Annotations project enables developers to easily create and register methods for handling MCP operations using simple annotations. It provides a clean, declarative approach to implementing MCP server functionality, reducing boilerplate code and improving maintainability.

This library builds on top of the [MCP Java SDK](https://github.com/modelcontextprotocol/sdk-java) to provide a higher-level, annotation-based programming model for implementing MCP servers and clients.

## Installation

To use the MCP Annotations core module in your project, add the following dependency to your Maven POM file:

```xml
<dependency>
    <groupId>org.springaicommunity</groupId>
    <artifactId>mcp-annotations</artifactId>
    <version>...</version>
</dependency>
```
(_Select the latest released mcp-annotations [version](https://central.sonatype.com/artifact/org.springaicommunity/mcp-annotations/versions) or latest snapshot._)

and a Java MCP SDK dependency:

```xml
<dependency>
    <groupId>io.modelcontextprotocol.sdk</groupId>
    <artifactId>mcp</artifactId>
    <version>...</version>
</dependency>
```

(_Select the latest released mcp-java [version](https://central.sonatype.com/artifact/io.modelcontextprotocol.sdk/mcp/versions) or latest snapshot._)


### Snapshot repositories

To use the mcp-annotations and mcp-java-sdk snapshot versions you need to add the following repositories to your Maven POM:

```xml
<repositories>
  <repository>
    <name>Central Portal Snapshots</name>
    <id>central-portal-snapshots</id>
    <url>https://central.sonatype.com/repository/maven-snapshots/</url>
    <releases>
      <enabled>false</enabled>
    </releases>
    <snapshots>
      <enabled>true</enabled>
    </snapshots>
  </repository>
</repositories>
```

## Spring AI MCP
For a complete out-of-the-box experience, use the [Spring AI MCP Annotation Auto-configurations](https://docs.spring.io/spring-ai/reference/1.1-SNAPSHOT/api/mcp/mcp-annotations-overview.html) integration.

Please check the [Spring AI MCP Annotations Example](https://github.com/spring-projects/spring-ai-examples/tree/main/model-context-protocol/mcp-annotations).

## mcp-annotations

The core module provides a set of annotations and callback implementations for primary MCP operations.

### For MCP Servers: 

1. **Complete** - For auto-completion functionality in prompts and URI templates
2. **Prompt** - For generating prompt messages
3. **Resource** - For accessing resources via URI templates
4. **Tool** - For implementing MCP tools with automatic JSON schema generation

### For MCP Clients:

1. **Logging Consumer** - For handling logging message notifications
2. **Sampling** - For handling sampling requests
3. **Elicitation** - For handling elicitation requests to gather additional information from users
4. **Progress** - For handling progress notifications during long-running operations

Each operation type has both synchronous and asynchronous implementations, allowing for flexible integration with different application architectures.

## Key Components

### Annotations

#### Client
- **`@McpLogging`** - Annotates methods that handle logging message notifications from MCP servers (requires `clients` parameter)
- **`@McpSampling`** - Annotates methods that handle sampling requests from MCP servers (requires `clients` parameter)
- **`@McpElicitation`** - Annotates methods that handle elicitation requests to gather additional information from users (requires `clients` parameter)
- **`@McpProgress`** - Annotates methods that handle progress notifications for long-running operations (requires `clients` parameter)
- **`@McpToolListChanged`** - Annotates methods that handle tool list change notifications from MCP servers
- **`@McpResourceListChanged`** - Annotates methods that handle resource list change notifications from MCP servers
- **`@McpPromptListChanged`** - Annotates methods that handle prompt list change notifications from MCP servers

#### Server
- **`@McpComplete`** - Annotates methods that provide completion functionality for prompts or URI templates
- **`@McpPrompt`** - Annotates methods that generate prompt messages
  - **`@McpArg`** - Annotates method parameters as MCP arguments
- **`@McpResource`** - Annotates methods that provide access to resources
- **`@McpTool`** - Annotates methods that implement MCP tools with automatic JSON schema generation
  - **`@McpToolParam`** - Annotates tool method parameters with descriptions and requirement specifications

#### Special Parameters and Annotations
- **`McpSyncRequestContext`** - Special parameter type for synchronous operations that provides a unified interface for accessing MCP request context, including the original request, server exchange (for stateful operations), transport context (for stateless operations), and convenient methods for logging, progress, sampling, and elicitation. This parameter is automatically injected and excluded from JSON schema generation
- **`McpAsyncRequestContext`** - Special parameter type for asynchronous operations that provides the same unified interface as `McpSyncRequestContext` but with reactive (Mono-based) return types. This parameter is automatically injected and excluded from JSON schema generation
- **(Deprecated and replaced by `McpSyncRequestContext`) `McpSyncServerExchange`** - Special parameter type for stateful synchronous operations that provides access to server exchange functionality including logging notifications, progress updates, and other server-side operations. This parameter is automatically injected and excluded from JSON schema generation. 
- **(Deprecated and replaced by `McpAsyncRequestContext`) `McpAsyncServerExchange`** - Special parameter type for stateful asynchronous operations that provides access to server exchange functionality with reactive support. This parameter is automatically injected and excluded from JSON schema generation
- **`McpTransportContext`** - Special parameter type for stateless operations that provides lightweight access to transport-level context without full server exchange functionality. This parameter is automatically injected and excluded from JSON schema generation
- **(Deprecated. Handled internally by `McpSyncRequestContext` and `McpAsyncRequestContext`)`@McpProgressToken`** - Marks a method parameter to receive the progress token from the request. This parameter is automatically injected and excluded from the generated JSON schema
**Note:** if using the `McpSyncRequestContext` or `McpAsyncRequestContext` the progress token is handled internally.
- **`McpMeta`** - Special parameter type that provides access to metadata from MCP requests, notifications, and results. This parameter is automatically injected and excluded from parameter count limits and JSON schema generation. 
**Note:** if using the McpSyncRequestContext or McpAsyncRequestContext the meta can be obatined via `requestMeta()` instead.

### Method Callbacks

The modules provide callback implementations for each operation type:

#### Complete
- `AbstractMcpCompleteMethodCallback` - Base class for complete method callbacks
- `SyncMcpCompleteMethodCallback` - Synchronous implementation
- `AsyncMcpCompleteMethodCallback` - Asynchronous implementation using Reactor's Mono
- `SyncStatelessMcpCompleteMethodCallback` - Synchronous stateless implementation using `McpTransportContext`
- `AsyncStatelessMcpCompleteMethodCallback` - Asynchronous stateless implementation using `McpTransportContext`

#### Prompt
- `AbstractMcpPromptMethodCallback` - Base class for prompt method callbacks
- `SyncMcpPromptMethodCallback` - Synchronous implementation
- `AsyncMcpPromptMethodCallback` - Asynchronous implementation using Reactor's Mono
- `SyncStatelessMcpPromptMethodCallback` - Synchronous stateless implementation using `McpTransportContext`
- `AsyncStatelessMcpPromptMethodCallback` - Asynchronous stateless implementation using `McpTransportContext`

#### Resource
- `AbstractMcpResourceMethodCallback` - Base class for resource method callbacks
- `SyncMcpResourceMethodCallback` - Synchronous implementation
- `AsyncMcpResourceMethodCallback` - Asynchronous implementation using Reactor's Mono
- `SyncStatelessMcpResourceMethodCallback` - Synchronous stateless implementation using `McpTransportContext`
- `AsyncStatelessMcpResourceMethodCallback` - Asynchronous stateless implementation using `McpTransportContext`

#### Logging Consumer
- `AbstractMcpLoggingMethodCallback` - Base class for logging consumer method callbacks
- `SyncMcpLoggingMethodCallback` - Synchronous implementation
- `AsyncMcpLoggingMethodCallback` - Asynchronous implementation using Reactor's Mono

#### Tool
- `AbstractSyncMcpToolMethodCallback` - Base class for synchronous tool method callbacks
- `AbstractAsyncMcpToolMethodCallback` - Base class for asynchronous tool method callbacks
- `SyncMcpToolMethodCallback` - Synchronous implementation for tool method callbacks with server exchange
- `AsyncMcpToolMethodCallback` - Asynchronous implementation using Reactor's Mono with server exchange
- `SyncStatelessMcpToolMethodCallback` - Synchronous stateless implementation for tool method callbacks
- `AsyncStatelessMcpToolMethodCallback` - Asynchronous stateless implementation using Reactor's Mono

#### Sampling
- `AbstractMcpSamplingMethodCallback` - Base class for sampling method callbacks
- `SyncMcpSamplingMethodCallback` - Synchronous implementation
- `AsyncMcpSamplingMethodCallback` - Asynchronous implementation using Reactor's Mono

#### Elicitation
- `AbstractMcpElicitationMethodCallback` - Base class for elicitation method callbacks
- `SyncMcpElicitationMethodCallback` - Synchronous implementation
- `AsyncMcpElicitationMethodCallback` - Asynchronous implementation using Reactor's Mono

#### Progress
- `AbstractMcpProgressMethodCallback` - Base class for progress method callbacks
- `SyncMcpProgressMethodCallback` - Synchronous implementation
- `AsyncMcpProgressMethodCallback` - Asynchronous implementation using Reactor's Mono

#### Tool List Changed
- `AbstractMcpToolListChangedMethodCallback` - Base class for tool list changed method callbacks
- `SyncMcpToolListChangedMethodCallback` - Synchronous implementation
- `AsyncMcpToolListChangedMethodCallback` - Asynchronous implementation using Reactor's Mono

#### Resource List Changed
- `AbstractMcpResourceListChangedMethodCallback` - Base class for resource list changed method callbacks
- `SyncMcpResourceListChangedMethodCallback` - Synchronous implementation
- `AsyncMcpResourceListChangedMethodCallback` - Asynchronous implementation using Reactor's Mono

### Providers

The project includes provider classes that scan for annotated methods and create appropriate callbacks:

#### Stateful Providers (using McpSyncServerExchange/McpAsyncServerExchange)
- `SyncMcpCompleteProvider` - Processes `@McpComplete` annotations for synchronous operations
- `AsyncMcpCompleteProvider` - Processes `@McpComplete` annotations for asynchronous operations
- `SyncMcpPromptProvider` - Processes `@McpPrompt` annotations for synchronous operations
- `AsyncMcpPromptProvider` - Processes `@McpPrompt` annotations for asynchronous operations
- `SyncMcpResourceProvider` - Processes `@McpResource` annotations for synchronous operations
- `AsyncMcpResourceProvider` - Processes `@McpResource` annotations for asynchronous operations
- `SyncMcpToolProvider` - Processes `@McpTool` annotations for synchronous operations
- `AsyncMcpToolProvider` - Processes `@McpTool` annotations for asynchronous operations
- `SyncMcpLoggingProvider` - Processes `@McpLogging` annotations for synchronous operations
- `AsyncMcpLoggingProvider` - Processes `@McpLogging` annotations for asynchronous operations
- `SyncMcpSamplingProvider` - Processes `@McpSampling` annotations for synchronous operations
- `AsyncMcpSamplingProvider` - Processes `@McpSampling` annotations for asynchronous operations
- `SyncMcpElicitationProvider` - Processes `@McpElicitation` annotations for synchronous operations
- `AsyncMcpElicitationProvider` - Processes `@McpElicitation` annotations for asynchronous operations
- `SyncMcpProgressProvider` - Processes `@McpProgress` annotations for synchronous operations
- `AsyncMcpProgressProvider` - Processes `@McpProgress` annotations for asynchronous operations
- `SyncMcpToolListChangedProvider` - Processes `@McpToolListChanged` annotations for synchronous operations
- `AsyncMcpToolListChangedProvider` - Processes `@McpToolListChanged` annotations for asynchronous operations
- `SyncMcpResourceListChangedProvider` - Processes `@McpResourceListChanged` annotations for synchronous operations
- `AsyncMcpResourceListChangedProvider` - Processes `@McpResourceListChanged` annotations for asynchronous operations
- `SyncMcpPromptListChangedProvider` - Processes `@McpPromptListChanged` annotations for synchronous operations
- `AsyncMcpPromptListChangedProvider` - Processes `@McpPromptListChanged` annotations for asynchronous operations

#### Stateless Providers (using McpTransportContext)
- `SyncStatelessMcpCompleteProvider` - Processes `@McpComplete` annotations for synchronous stateless operations
- `AsyncStatelessMcpCompleteProvider` - Processes `@McpComplete` annotations for asynchronous stateless operations
- `SyncStatelessMcpPromptProvider` - Processes `@McpPrompt` annotations for synchronous stateless operations
- `AsyncStatelessMcpPromptProvider` - Processes `@McpPrompt` annotations for asynchronous stateless operations
- `SyncStatelessMcpResourceProvider` - Processes `@McpResource` annotations for synchronous stateless operations
- `SyncStatelessMcpResourceTemplateProvider` - Processes `@McpResource` annotations for synchronous stateless operations
- `AsyncStatelessMcpResourceProvider` - Processes `@McpResource` annotations for asynchronous stateless operations
- `AsyncStatelessMcpResourceTemplateProvider` - Processes `@McpResource` annotations for asynchronous stateless operations
- `SyncStatelessMcpToolProvider` - Processes `@McpTool` annotations for synchronous stateless operations
- `AsyncStatelessMcpToolProvider` - Processes `@McpTool` annotations for asynchronous stateless operations

## Usage Examples

### Prompt Example
```java
public class PromptProvider {

    @McpPrompt(name = "personalized-message",
            description = "Generates a personalized message based on user information")
    public GetPromptResult personalizedMessage(McpSyncServerExchange exchange,
            @McpArg(name = "name", description = "The user's name", required = true) String name,
            @McpArg(name = "age", description = "The user's age", required = false) Integer age,
            @McpArg(name = "interests", description = "The user's interests", required = false) String interests) {

        exchange.loggingNotification(LoggingMessageNotification.builder()
            .level(LoggingLevel.INFO)	
            .data("personalized-message event").build());

        StringBuilder message = new StringBuilder();
        message.append("Hello, ").append(name).append("!\n\n");

        if (age != null) {
            message.append("At ").append(age).append(" years old, you have ");
            if (age < 30) {
                message.append("so much ahead of you.\n\n");
            }
            else if (age < 60) {
                message.append("gained valuable life experience.\n\n");
            }
            else {
                message.append("accumulated wisdom to share with others.\n\n");
            }
        }

        if (interests != null && !interests.isEmpty()) {
            message.append("Your interest in ")
                .append(interests)
                .append(" shows your curiosity and passion for learning.\n\n");
        }

        message
            .append("I'm here to assist you with any questions you might have about the Model Context Protocol.");

        return new GetPromptResult("Personalized Message",
                List.of(new PromptMessage(Role.ASSISTANT, new TextContent(message.toString()))));
    }
}
```

### Complete Example

```java
public class AutocompleteProvider {

    private final Map<String, List<String>> usernameDatabase = new HashMap<>();
    private final Map<String, List<String>> cityDatabase = new HashMap<>();
    
    public AutocompleteProvider() {
        // Initialize with sample data
        cityDatabase.put("l", List.of("Lagos", "Lima", "Lisbon", "London", "Los Angeles"));
        // ....
        usernameDatabase.put("a", List.of("alex123", "admin", "alice_wonder", "andrew99"));
        // Add more data...
    }    

	@McpComplete(prompt = "personalized-message")
	public List<String> completeName(String name) {
		String prefix = name.toLowerCase();
		String firstLetter = prefix.substring(0, 1);
		List<String> usernames = usernameDatabase.getOrDefault(firstLetter, List.of());

		return usernames.stream().filter(username -> username.toLowerCase().startsWith(prefix)).toList();
	}

    @McpComplete(prompt = "travel-planner")
    public List<String> completeCityName(CompleteRequest.CompleteArgument argument) {
        String prefix = argument.value().toLowerCase();        
        String firstLetter = prefix.substring(0, 1);
        List<String> cities = cityDatabase.getOrDefault(firstLetter, List.of());
        
        return cities.stream()
            .filter(city -> city.toLowerCase().startsWith(prefix))
            .toList();
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
public class MyResourceProvider {

	private String getUserStatus(String username) {
		// Simple logic to generate a status
		if (username.equals("john")) {
			return "🟢 Online";
		} else if (username.equals("jane")) {
			return "🟠 Away";
		} else if (username.equals("bob")) {
			return "⚪ Offline";
		} else if (username.equals("alice")) {
			return "🔴 Busy";
		} else {
			return "⚪ Offline";
		}
	}

    @McpResource(uri = "user-status://{username}", 
        name = "User Status", 
        description = "Provides the current status for a specific user")
	public String getUserStatus(String username) {		
		return this.getUserStatus(username);
	}

    @McpResource(uri = "user-profile-exchange://{username}", 
        name = "User Profile with Exchange", 
        description = "Provides user profile information with server exchange context")
	public ReadResourceResult getProfileWithExchange(McpSyncServerExchange exchange, String username) {

        exchange.loggingNotification(LoggingMessageNotification.builder()
			.level(LoggingLevel.INFO)	
			.data("user-profile-exchange")
            .build());

		String profileInfo = formatProfileInfo(userProfiles.getOrDefault(username.toLowerCase(), new HashMap<>()));

		return new ReadResourceResult(List.of(new TextResourceContents("user-profile-exchange://" + username,
				"text/plain", "Profile with exchange for " + username + ": " + profileInfo)));
	}
}
```

### Tool Example

```java
public class CalculatorToolProvider {

    @McpTool(name = "add", description = "Add two numbers together")
    public int add(
            @McpToolParam(description = "First number to add", required = true) int a,
            @McpToolParam(description = "Second number to add", required = true) int b) {
        return a + b;
    }

    @McpTool(name = "multiply", description = "Multiply two numbers")
    public double multiply(
            @McpToolParam(description = "First number", required = true) double x,
            @McpToolParam(description = "Second number", required = true) double y) {
        return x * y;
    }

    @McpTool(name = "calculate-area", 
             description = "Calculate the area of a rectangle",
             title = "Rectangle Area Calculator",  // Human-readable display name
             annotations = @McpTool.McpAnnotations(
                 readOnlyHint = true,
                 destructiveHint = false,
                 idempotentHint = true
             ))
    public AreaResult calculateRectangleArea(
            @McpToolParam(description = "Width of the rectangle", required = true) double width,
            @McpToolParam(description = "Height of the rectangle", required = true) double height) {
        
        double area = width * height;
        return new AreaResult(area, "square units");
    }

    @McpTool(name = "process-data", description = "Process data with exchange context")
    public String processData(
            McpSyncServerExchange exchange,
            @McpToolParam(description = "Data to process", required = true) String data) {
        
        exchange.loggingNotification(LoggingMessageNotification.builder()
            .level(LoggingLevel.INFO)
            .data("Processing data: " + data)
            .build());
        
        return "Processed: " + data.toUpperCase();
    }

    // Async tool example
    @McpTool(name = "async-calculation", description = "Perform async calculation")
    public Mono<String> asyncCalculation(
            @McpToolParam(description = "Input value", required = true) int value) {
        return Mono.fromCallable(() -> {
            // Simulate some async work
            Thread.sleep(100);
            return "Async result: " + (value * 2);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // Tool with CallToolRequest parameter for dynamic schema support
    @McpTool(name = "dynamic-processor", description = "Process data with dynamic schema")
    public CallToolResult processDynamic(CallToolRequest request) {
        // Access the full request including dynamic schema
        Map<String, Object> args = request.arguments();
        
        // Process based on runtime schema
        String result = "Processed " + args.size() + " arguments dynamically";
        
        return CallToolResult.builder()
            .addTextContent(result)
            .build();
    }

    // Tool with mixed parameters - typed and CallToolRequest
    @McpTool(name = "hybrid-processor", description = "Process with both typed and dynamic parameters")
    public String processHybrid(
            @McpToolParam(description = "Action to perform", required = true) String action,
            CallToolRequest request) {
        
        // Use typed parameter
        String actionResult = "Action: " + action;
        
        // Also access additional dynamic arguments
        Map<String, Object> additionalArgs = request.arguments();
        
        return actionResult + " with " + (additionalArgs.size() - 1) + " additional parameters";
    }

    public static class AreaResult {
        public double area;
        public String unit;
        
        public AreaResult(double area, String unit) {
            this.area = area;
            this.unit = unit;
        }
    }
}
```

#### Output Schema Generation

The `@McpTool` annotation includes a `generateOutputSchema` attribute that controls whether output schemas are automatically generated for tool methods:

```java
@McpTool(name = "calculate", 
         description = "Perform calculation",
         generateOutputSchema = true)  // Explicitly enable output schema generation
public CalculationResult calculate(double value) {
    return new CalculationResult(value * 2, "doubled");
}

@McpTool(name = "simple-tool", 
         description = "Simple tool without output schema")  // Default: no output schema
public String simpleTool(String input) {
    return "Processed: " + input;
}
```

**Output Schema Behavior:**
- **Default**: `generateOutputSchema = false` - No output schema is automatically generated
- **When enabled**: `generateOutputSchema = true` - Output schema is generated for complex return types
- **Primitive types**: No output schema is generated regardless of the setting (String, int, boolean, etc.)
- **Void types**: No output schema is generated
- **Complex types**: Output schema is generated only when explicitly enabled

**Output Serialization:**
- **String return types**: Returned directly as text content without JSON serialization
- **Complex objects**: Serialized to JSON for text content
- **Null values**: Returned as "null" text content
- **Void methods**: Return "Done" as text content

#### Tool Title Attribute

The `@McpTool` annotation supports a `title` attribute that provides a human-readable display name for tools. This is intended for UI and end-user contexts, optimized to be easily understood even by those unfamiliar with domain-specific terminology.

**Title Precedence Order:**
1. If the `title` attribute is explicitly set, it takes precedence
2. If not set but `annotations.title` exists, that value is used  
3. If neither is provided, the tool's `name` is used as the title
4. If the `name` is not set the method name is used as the title

Example usage:

```java
// Using the title attribute directly
@McpTool(name = "calc-area", 
         description = "Calculate rectangle area",
         title = "Rectangle Area Calculator")  // Human-friendly display name
public double calculateArea(double width, double height) {
    return width * height;
}

// Title attribute takes precedence over annotations.title
@McpTool(name = "data-processor",
         description = "Process complex data",
         title = "Data Processor",  // This takes precedence
         annotations = @McpTool.McpAnnotations(
             title = "Complex Data Handler"  // This is overridden
         ))
public String processData(String input) {
    return process(input);
}

// Using annotations.title when title attribute is not set
@McpTool(name = "file-converter",
         description = "Convert file formats",
         annotations = @McpTool.McpAnnotations(
             title = "File Format Converter"  // This will be used as title
         ))
public String convertFile(String filePath) {
    return convert(filePath);
}

// Falls back to name when no title is provided
@McpTool(name = "simple-tool",
         description = "A simple tool")
public String simpleTool(String input) {
    // Title will be "simple-tool"
    return input;
}
```

The title is particularly useful for:
- Displaying tools in user interfaces with friendly names
- Providing clear, non-technical names for end users
- Maintaining backward compatibility (tools without titles continue to work)

#### CallToolRequest Support

The library supports special `CallToolRequest` parameters in tool methods, enabling dynamic schema handling at runtime. This is useful when you need to:

- Accept tools with schemas defined at runtime
- Process requests where the input structure isn't known at compile time
- Build flexible tools that adapt to different input schemas

When a tool method includes a `CallToolRequest` parameter:
- The parameter receives the complete tool request including all arguments
- For methods with only `CallToolRequest`, a minimal schema is generated
- For methods with mixed parameters, only non-`CallToolRequest` parameters are included in the schema
- The `CallToolRequest` parameter is automatically injected and doesn't appear in the tool's input schema

Example usage:

```java
// Tool that accepts any schema at runtime
@McpTool(name = "flexible-tool")
public CallToolResult processAnySchema(CallToolRequest request) {
    Map<String, Object> args = request.arguments();
    // Process based on whatever schema was provided at runtime
    return CallToolResult.success(processedResult);
}

// Tool with both typed and dynamic parameters
@McpTool(name = "mixed-tool")
public String processMixed(
        @McpToolParam("operation") String operation,
        @McpToolParam("count") int count,
        CallToolRequest request) {
    
    // Use typed parameters for known fields
    String result = operation + " x " + count;
    
    // Access any additional fields from the request
    Map<String, Object> allArgs = request.arguments();
    
    return result;
}
```

This feature works with all tool callback types:
- `SyncMcpToolMethodCallback` - Synchronous with server exchange
- `AsyncMcpToolMethodCallback` - Asynchronous with server exchange
- `SyncStatelessMcpToolMethodCallback` - Synchronous stateless
- `AsyncStatelessMcpToolMethodCallback` - Asynchronous stateless

#### @McpProgressToken Support

The `@McpProgressToken` annotation allows methods to receive progress tokens from MCP requests. This is useful for tracking long-running operations and providing progress updates to clients.

When a method parameter is annotated with `@McpProgressToken`:
- The parameter automatically receives the progress token value from the request
- The parameter is excluded from the generated JSON schema
- The parameter type should be `String` to receive the token value
- If no progress token is present in the request, `null` is injected

Example usage with tools:

```java
@McpTool(name = "long-running-task", description = "Performs a long-running task with progress tracking")
public String performLongTask(
        @McpProgressToken String progressToken,
        @McpToolParam(description = "Task name", required = true) String taskName,
        @McpToolParam(description = "Duration in seconds", required = true) int duration) {
    
    // Use the progress token to send progress updates
    if (progressToken != null) {
        // Send progress notifications using the token
        sendProgressUpdate(progressToken, 0.0, "Starting task: " + taskName);
        
        // Simulate work with progress updates
        for (int i = 1; i <= duration; i++) {
            Thread.sleep(1000);
            double progress = (double) i / duration;
            sendProgressUpdate(progressToken, progress, "Processing... " + (i * 100 / duration) + "%");
        }
    }
    
    return "Task " + taskName + " completed successfully";
}

// Tool with both CallToolRequest and progress token
@McpTool(name = "flexible-task", description = "Flexible task with progress tracking")
public CallToolResult flexibleTask(
        @McpProgressToken String progressToken,
        CallToolRequest request) {
    
    // Access progress token for tracking
    if (progressToken != null) {
        // Track progress for this operation
        System.out.println("Progress token: " + progressToken);
    }
    
    // Process the request
    Map<String, Object> args = request.arguments();
    return CallToolResult.success("Processed with token: " + progressToken);
}
```

The `@McpProgressToken` annotation is also supported in other MCP callback types:

**Resource callbacks:**
```java
@McpResource(uri = "data://{id}", name = "Data Resource", description = "Resource with progress tracking")
public ReadResourceResult getDataWithProgress(
        @McpProgressToken String progressToken,
        String id) {
    
    if (progressToken != null) {
        // Use progress token for tracking resource access
        trackResourceAccess(progressToken, id);
    }
    
    return new ReadResourceResult(List.of(
        new TextResourceContents("data://" + id, "text/plain", "Data for " + id)
    ));
}
```

**Prompt callbacks:**
```java
@McpPrompt(name = "generate-content", description = "Generate content with progress tracking")
public GetPromptResult generateContent(
        @McpProgressToken String progressToken,
        @McpArg(name = "topic", required = true) String topic) {
    
    if (progressToken != null) {
        // Track prompt generation progress
        System.out.println("Generating prompt with token: " + progressToken);
    }
    
    return new GetPromptResult("Generated Content",
        List.of(new PromptMessage(Role.ASSISTANT, new TextContent("Content about " + topic))));
}
```

**Complete callbacks:**
```java
@McpComplete(prompt = "auto-complete")
public List<String> completeWithProgress(
        @McpProgressToken String progressToken,
        String prefix) {
    
    if (progressToken != null) {
        // Track completion progress
        System.out.println("Completion with token: " + progressToken);
    }
    
    return generateCompletions(prefix);
}
```

This feature enables better tracking and monitoring of MCP operations, especially for long-running tasks that need to report progress back to clients.

#### McpMeta Support

The `McpMeta` class provides access to metadata from MCP requests, notifications, and results. This is useful for accessing contextual information that clients may include with their requests.

When a method parameter is of type `McpMeta`:
- The parameter automatically receives metadata from the request wrapped in an `McpMeta` object
- The parameter is excluded from parameter count limits and JSON schema generation
- The parameter provides convenient access to metadata through the `get(String key)` method
- If no metadata is present in the request, an empty `McpMeta` object is injected

Example usage with tools:

```java
@McpTool(name = "personalized-task", description = "Performs a task with user context")
public String personalizedTask(
        @McpToolParam(description = "Task name", required = true) String taskName,
        McpMeta meta) {
    
    // Access metadata from the request
    String userId = (String) meta.get("userId");
    String sessionId = (String) meta.get("sessionId");
    
    if (userId != null) {
        return "Task " + taskName + " executed for user: " + userId + 
               " (session: " + sessionId + ")";
    }
    
    return "Task " + taskName + " executed (no user context)";
}

// Tool with both CallToolRequest and McpMeta
@McpTool(name = "flexible-task", description = "Flexible task with metadata")
public CallToolResult flexibleTask(
        CallToolRequest request,
        McpMeta meta) {
    
    // Access both the full request and metadata
    Map<String, Object> args = request.arguments();
    String userRole = (String) meta.get("userRole");
    
    String result = "Processed " + args.size() + " arguments";
    if (userRole != null) {
        result += " for user with role: " + userRole;
    }
    
    return CallToolResult.builder()
        .addTextContent(result)
        .build();
}
```

The `McpMeta` parameter is also supported in other MCP callback types:

**Resource callbacks:**
```java
@McpResource(uri = "user-data://{id}", name = "User Data", description = "User data with context")
public ReadResourceResult getUserData(
        String id,
        McpMeta meta) {
    
    String requestingUser = (String) meta.get("requestingUser");
    String accessLevel = (String) meta.get("accessLevel");
    
    // Use metadata to customize response based on requesting user
    String content = "User data for " + id;
    if ("admin".equals(accessLevel)) {
        content += " (full access granted to " + requestingUser + ")";
    } else {
        content += " (limited access)";
    }
    
    return new ReadResourceResult(List.of(
        new TextResourceContents("user-data://" + id, "text/plain", content)
    ));
}
```

**Prompt callbacks:**
```java
@McpPrompt(name = "contextual-prompt", description = "Generate contextual prompt")
public GetPromptResult contextualPrompt(
        @McpArg(name = "topic", required = true) String topic,
        McpMeta meta) {
    
    String userPreference = (String) meta.get("preferredStyle");
    String language = (String) meta.get("language");
    
    String message = "Let's discuss " + topic;
    if ("formal".equals(userPreference)) {
        message = "I would like to formally discuss the topic of " + topic;
    } else if ("casual".equals(userPreference)) {
        message = "Hey! Let's chat about " + topic;
    }
    
    if (language != null && !"en".equals(language)) {
        message += " (Note: Response requested in " + language + ")";
    }
    
    return new GetPromptResult("Contextual Prompt",
        List.of(new PromptMessage(Role.ASSISTANT, new TextContent(message))));
}
```

**Complete callbacks:**
```java
@McpComplete(prompt = "smart-complete")
public List<String> smartComplete(
        String prefix,
        McpMeta meta) {
    
    String userLevel = (String) meta.get("userLevel");
    String domain = (String) meta.get("domain");
    
    // Customize completions based on user context
    List<String> completions = generateBasicCompletions(prefix);
    
    if ("expert".equals(userLevel)) {
        completions.addAll(generateAdvancedCompletions(prefix));
    }
    
    if (domain != null) {
        completions = filterByDomain(completions, domain);
    }
    
    return completions;
}
```

This feature enables context-aware MCP operations where the behavior can be customized based on client-provided metadata such as user identity, preferences, session information, or any other contextual data.

#### McpRequestContext Support

The library provides unified request context interfaces (`McpSyncRequestContext` and `McpAsyncRequestContext`) that offer a higher-level abstraction over the underlying MCP infrastructure. These context objects provide convenient access to:

- The original request (CallToolRequest, ReadResourceRequest, etc.)
- Server exchange (for stateful operations) or transport context (for stateless operations)
- Convenient methods for logging, progress updates, sampling, elicitation, and more

**Key Benefits:**
- **Unified API**: Single parameter type works for both stateful and stateless operations
- **Convenience Methods**: Built-in helpers for common operations like logging and progress tracking
- **Type Safety**: Strongly-typed access to request data and context
- **Automatic Injection**: Context is automatically created and injected by the framework

When a method parameter is of type `McpSyncRequestContext` or `McpAsyncRequestContext`:
- The parameter is automatically injected with the appropriate context implementation
- The parameter is excluded from JSON schema generation
- For stateful operations, the context provides access to `McpSyncServerExchange` or `McpAsyncServerExchange`
- For stateless operations, the context provides access to `McpTransportContext`

**Synchronous Context Example:**

```java
public record UserInfo(String name, String email, Number age) {}

@McpTool(name = "process-with-context", description = "Process data with unified context")
public String processWithContext(
        McpSyncRequestContext context,
        @McpToolParam(description = "Data to process", required = true) String data) {
    
    // Access the original request
    CallToolRequest request = (CallToolRequest) context.request();
    
    // Log information
    context.info("Processing data: " + data);
    
    // Send progress updates
    context.progress(50); // 50% complete
    
    // Check if running in stateful mode
    if (!context.isStateless()) {
        // Access server exchange for stateful operations
        McpSyncServerExchange exchange = context.exchange().orElseThrow();
        // Use exchange for additional operations...
    }
    
    // Check if elicitation is supported before using it
    if (context.elicitEnabled()) {
        // Perform elicitation with default message - returns StructuredElicitResult
        StructuredElicitResult<UserInfo> result = context.elicit(new TypeReference<UserInfo>() {});
        
        // Or perform elicitation with custom configuration - returns StructuredElicitResult
        StructuredElicitResult<UserInfo> structuredResult = context.elicit(
            e -> e.message("Please provide your information").meta("context", "user-registration"),
            new TypeReference<UserInfo>() {}
        );
        
        if (structuredResult.action() == ElicitResult.Action.ACCEPT) {
            UserInfo info = structuredResult.structuredContent();
            return "Processed: " + data + " for user " + info.name();
        }
    }
    
    return "Processed: " + data;
}

@McpResource(uri = "data://{id}", name = "Data Resource", description = "Resource with context")
public ReadResourceResult getDataWithContext(
        McpSyncRequestContext context,
        String id) {
    
    // Log the resource access
    context.debug("Accessing resource: " + id);
    
    // Access metadata from the request
    Map<String, Object> metadata = context.request()._meta();
    
    String content = "Data for " + id;
    return new ReadResourceResult(List.of(
        new TextResourceContents("data://" + id, "text/plain", content)
    ));
}

@McpPrompt(name = "generate-with-context", description = "Generate prompt with context")
public GetPromptResult generateWithContext(
        McpSyncRequestContext context,
        @McpArg(name = "topic", required = true) String topic) {
    
    // Log prompt generation
    context.info("Generating prompt for topic: " + topic);
    
    // Check if sampling is supported before using it
    if (context.sampleEnabled()) {
        // Perform sampling if needed
        CreateMessageResult samplingResult = context.sample(
            "What are the key points about " + topic + "?"
        );
        // Use sampling result...
    }
    
    String message = "Let's discuss " + topic;
    return new GetPromptResult("Generated Prompt",
        List.of(new PromptMessage(Role.ASSISTANT, new TextContent(message))));
}
```

**Asynchronous Context Example:**

```java
public record UserInfo(String name, String email, int age) {}

@McpTool(name = "async-process-with-context", description = "Async process with unified context")
public Mono<String> asyncProcessWithContext(
        McpAsyncRequestContext context,
        @McpToolParam(description = "Data to process", required = true) String data) {
    
    return Mono.fromCallable(() -> {
        // Access the original request
        CallToolRequest request = (CallToolRequest) context.request();
        return data;
    })
    .flatMap(processedData -> {
        // Log information (returns Mono<Void>)
        return context.info("Processing data: " + processedData)
            .thenReturn(processedData);
    })
    .flatMap(processedData -> {
        // Send progress updates (returns Mono<Void>)
        return context.progress(50)
            .thenReturn(processedData);
    })
    .flatMap(processedData -> {
        // Perform elicitation with default message - returns Mono<UserInfo>
        return context.elicitation(new TypeReference<UserInfo>() {})
            .map(userInfo -> "Processed: " + processedData + " for user " + userInfo.name());
    })
    .switchIfEmpty(Mono.fromCallable(() -> {
        // Or perform elicitation with custom message and metadata - returns Mono<StructuredElicitResult<UserInfo>>
        return context.elicitation(
            new TypeReference<UserInfo>() {},
            "Please provide your information",
            Map.of("context", "user-registration")
        )
        .filter(result -> result.action() == ElicitResult.Action.ACCEPT)
        .map(result -> "Processed: " + data + " for user " + result.structuredContent().name())
        .defaultIfEmpty("Processed: " + data);
    }).flatMap(mono -> mono));
}

@McpResource(uri = "async-data://{id}", name = "Async Data Resource", 
             description = "Async resource with context")
public Mono<ReadResourceResult> getAsyncDataWithContext(
        McpAsyncRequestContext context,
        String id) {
    
    // Log the resource access (returns Mono<Void>)
    return context.debug("Accessing async resource: " + id)
        .then(Mono.fromCallable(() -> {
            String content = "Async data for " + id;
            return new ReadResourceResult(List.of(
                new TextResourceContents("async-data://" + id, "text/plain", content)
            ));
        }));
}

@McpPrompt(name = "async-generate-with-context", 
           description = "Async generate prompt with context")
public Mono<GetPromptResult> asyncGenerateWithContext(
        McpAsyncRequestContext context,
        @McpArg(name = "topic", required = true) String topic) {
    
    // Log prompt generation and perform sampling
    return context.info("Generating async prompt for topic: " + topic)
        .then(context.sampling("What are the key points about " + topic + "?"))
        .map(samplingResult -> {
            String message = "Let's discuss " + topic;
            return new GetPromptResult("Generated Async Prompt",
                List.of(new PromptMessage(Role.ASSISTANT, new TextContent(message))));
        });
}
```

**Available Context Methods:**

`McpSyncRequestContext` provides:
- `request()` - Access the original request object
- `exchange()` - Access the server exchange (for stateful operations)
- `transportContext()` - Access the transport context (for stateless operations)
- `isStateless()` - Check if running in stateless mode
- `log(Consumer<LoggingSpec>)` - Send log messages with custom configuration
- `debug(String)`, `info(String)`, `warn(String)`, `error(String)` - Convenience logging methods
- `progress(int)`, `progress(Consumer<ProgressSpec>)` - Send progress updates
- `rootsEnabled()` - Check if roots capability is supported by the client
- `roots()` - Access root directories (throws `IllegalStateException` if not supported)
- `elicitEnabled()` - Check if elicitation capability is supported by the client
- `elicit(TypeReference<T>)` - Request user input with default message, returns `StructuredElicitResult<T>` with action, typed content, and metadata (throws `IllegalStateException` if not supported)
- `elicit(Class<T>)` - Request user input with default message using Class type, returns `StructuredElicitResult<T>` (throws `IllegalStateException` if not supported)
- `elicit(Consumer<ElicitationSpec>, TypeReference<T>)` - Request user input with custom configuration, returns `StructuredElicitResult<T>` (throws `IllegalStateException` if not supported)
- `elicit(Consumer<ElicitationSpec>, Class<T>)` - Request user input with custom configuration using Class type, returns `StructuredElicitResult<T>` (throws `IllegalStateException` if not supported)
- `elicit(ElicitRequest)` - Request user input with full control over the elicitation request (throws `IllegalStateException` if not supported)
- `sampleEnabled()` - Check if sampling capability is supported by the client
- `sample(...)` - Request LLM sampling with various configuration options (throws `IllegalStateException` if not supported)
- `ping()` - Send ping to check connection

`McpAsyncRequestContext` provides the same methods but with reactive return types (`Mono<T>` instead of `T`). Methods that throw `IllegalStateException` in sync context return `Mono.error(IllegalStateException)` in async context.

**Important Notes on Capability Checking:**
- Always check capability support using `rootsEnabled()`, `elicitEnabled()`, or `sampleEnabled()` before calling the corresponding methods
- Calling capability methods when not supported will throw `IllegalStateException` (sync) or return `Mono.error()` (async)
- Stateless servers do not support bidirectional operations (roots, elicitation, sampling) and will always return `false` for capability checks

This unified context approach simplifies method signatures and provides a consistent API across different operation types and execution modes (stateful vs stateless, sync vs async).

### Async Tool Example

```java
public class AsyncToolProvider {

    @McpTool(name = "fetch-data", description = "Fetch data asynchronously")
    public Mono<DataResponse> fetchData(
            @McpToolParam(description = "Data ID to fetch", required = true) String dataId,
            @McpToolParam(description = "Include metadata", required = false) Boolean includeMetadata) {
        
        return Mono.fromCallable(() -> {
            // Simulate async data fetching
            DataResponse response = new DataResponse();
            response.id = dataId;
            response.data = "Sample data for " + dataId;
            response.metadata = Boolean.TRUE.equals(includeMetadata) ? 
                Map.of("timestamp", System.currentTimeMillis()) : null;
            return response;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @McpTool(name = "stream-process", description = "Process data stream")
    public Flux<String> streamProcess(
            @McpToolParam(description = "Number of items to process", required = true) int count) {
        
        return Flux.range(1, count)
            .map(i -> "Processed item " + i)
            .delayElements(Duration.ofMillis(100));
    }

    public static class DataResponse {
        public String id;
        public String data;
        public Map<String, Object> metadata;
    }
}
```

### Mcp Server with Tool capabilities

```java
public class McpServerFactory {

    public McpSyncServer createMcpServerWithTools(
            CalculatorToolProvider calculatorProvider,
            MyResourceProvider resourceProvider) {
        
        List<SyncToolSpecification> toolSpecifications = 
            new SyncMcpToolProvider(List.of(calculatorProvider)).getToolSpecifications();

        List<SyncResourceSpecification> resourceSpecifications = 
            new SyncMcpResourceProvider(List.of(resourceProvider)).getResourceSpecifications();
            
        // Create a server with tool support
        McpSyncServer syncServer = McpServer.sync(transportProvider)
            .serverInfo("calculator-server", "1.0.0")
            .capabilities(ServerCapabilities.builder()
                .tools(true)         // Enable tool support
                .resources(true)     // Enable resource support
                .logging()           // Enable logging support
                .build())
            .tools(toolSpecifications)
            .resources(resourceSpecifications)
            .build();

        return syncServer;
    }

    public McpAsyncServer createAsyncMcpServerWithTools(
            AsyncToolProvider asyncToolProvider) {
        
        List<AsyncToolSpecification> asyncToolSpecifications = 
            new AsyncMcpToolProvider(List.of(asyncToolProvider)).getToolSpecifications();
            
        // Create an async server with tool support
        McpAsyncServer asyncServer = McpServer.async(transportProvider)
            .serverInfo("async-tool-server", "1.0.0")
            .capabilities(ServerCapabilities.builder()
                .tools(true)         // Enable tool support
                .logging()           // Enable logging support
                .build())
            .tools(asyncToolSpecifications)
            .build();

        return asyncServer;
    }
}
```

### Mcp Server with Resource, Prompt and Completion capabilities

```java
public class McpServerFactory {

    public McpSyncServer createMcpServer(
            MyResourceProvider myResourceProvider, 
            AutocompleteProvider autocompleteProvider,
            PromptProvider promptProvider) {
        
        List<SyncResourceSpecification> resourceSpecifications = 
            new SyncMcpResourceProvider(List.of(myResourceProvider)).getResourceSpecifications();

        List<SyncCompletionSpecification> completionSpecifications = 
            new SyncMcpCompleteProvider(List.of(autocompleteProvider)).getCompleteSpecifications();

        List<SyncPromptSpecification> promptSpecifications = 
            new SyncMcpPromptProvider(List.of(promptProvider)).getPromptSpecifications();
            
        // Create a server with custom configuration
        McpSyncServer syncServer = McpServer.sync(transportProvider)
            .serverInfo("my-server", "1.0.0")
            .capabilities(ServerCapabilities.builder()
                .resources(true)     // Enable resource support
                .prompts(true)       // Enable prompt support
                .logging()           // Enable logging support
                .completions()       // Enable completions support
                .build())
            .resources(resourceSpecifications)
            .completions(completionSpecifications)
            .prompts(promptSpecifications)
            .build();

        return syncServer;
    }
}
```

### Mcp Client Logging Consumer Example

```java
public class LoggingHandler {

    /**
     * Handle logging message notifications with a single parameter.
     * Note: clients are required for all @McpLogging annotations.
     * @param notification The logging message notification
     */
    @McpLogging(clients = "default-client")
    public void handleLoggingMessage(LoggingMessageNotification notification) {
        System.out.println("Received logging message: " + notification.level() + " - " + notification.logger() + " - "
                + notification.data());
    }

    /**
     * Handle logging message notifications with individual parameters.
     * Note: clients are required for all @McpLogging annotations.
     * @param level The logging level
     * @param logger The logger name
     * @param data The log message data
     */
    @McpLogging(clients = "default-client")
    public void handleLoggingMessageWithParams(LoggingLevel level, String logger, String data) {
        System.out.println("Received logging message with params: " + level + " - " + logger + " - " + data);
    }

    /**
     * Handle logging message notifications for a specific client.
     * @param notification The logging message notification
     */
    @McpLogging(clients = "client-1")
    public void handleClient1LoggingMessage(LoggingMessageNotification notification) {
        System.out.println("Client-1 logging message: " + notification.level() + " - " + notification.data());
    }

    /**
     * Handle logging message notifications for another specific client.
     * @param notification The logging message notification
     */
    @McpLogging(clients = "client-2")
    public void handleClient2LoggingMessage(LoggingMessageNotification notification) {
        System.out.println("Client-2 logging message: " + notification.level() + " - " + notification.data());
    }
}

public class MyMcpClient {

    public static McpSyncClient createClient(LoggingHandler loggingHandler) {

        List<Consumer<LoggingMessageNotification>> loggingCOnsummers = 
            new SyncMcpLoggingProvider(List.of(loggingHandler)).getLoggingConsumers();

        McpSyncClient client = McpClient.sync(transport)
            .capabilities(ClientCapabilities.builder()
                // Enable capabilities ..
                .build())
            .loggingConsumers(loggingCOnsummers)
            .build();

        return client;
    }
}
```

### Mcp Client Sampling Example

```java
public class SamplingHandler {

    /**
     * Handle sampling requests with a synchronous implementation.
     * Note: clients are required for all @McpSampling annotations.
     * @param request The create message request
     * @return The create message result
     */
    @McpSampling(clients = "default-client")
    public CreateMessageResult handleSamplingRequest(CreateMessageRequest request) {
        // Process the request and generate a response
        return CreateMessageResult.builder()
            .role(Role.ASSISTANT)
            .content(new TextContent("This is a response to the sampling request"))
            .model("test-model")
            .build();
    }

    /**
     * Handle sampling requests for a specific client.
     * @param request The create message request
     * @return The create message result
     */
    @McpSampling(clients = "client-1")
    public CreateMessageResult handleClient1SamplingRequest(CreateMessageRequest request) {
        return CreateMessageResult.builder()
            .role(Role.ASSISTANT)
            .content(new TextContent("Client-1 specific sampling response"))
            .model("client-1-model")
            .build();
    }
}

public class AsyncSamplingHandler {

    /**
     * Handle sampling requests with an asynchronous implementation.
     * Note: clients are required for all @McpSampling annotations.
     * @param request The create message request
     * @return A Mono containing the create message result
     */
    @McpSampling(clients = "default-client")
    public Mono<CreateMessageResult> handleAsyncSamplingRequest(CreateMessageRequest request) {
        return Mono.just(CreateMessageResult.builder()
            .role(Role.ASSISTANT)
            .content(new TextContent("This is an async response to the sampling request"))
            .model("test-model")
            .build());
    }

    /**
     * Handle sampling requests for a specific client asynchronously.
     * @param request The create message request
     * @return A Mono containing the create message result
     */
    @McpSampling(clients = "client-2")
    public Mono<CreateMessageResult> handleClient2AsyncSamplingRequest(CreateMessageRequest request) {
        return Mono.just(CreateMessageResult.builder()
            .role(Role.ASSISTANT)
            .content(new TextContent("Client-2 async sampling response"))
            .model("client-2-model")
            .build());
    }
}

public class MyMcpClient {

    public static McpSyncClient createSyncClient(SamplingHandler samplingHandler) {
        List<SyncSamplingSpecification> samplingSpecifications = 
            new SyncMcpSamplingProvider(List.of(samplingHandler)).getSamplingSpecifications();

        Function<CreateMessageRequest, CreateMessageResult> samplingHandler = 
            samplingSpecifications.get(0).samplingHandler();

        McpSyncClient client = McpClient.sync(transport)
            .capabilities(ClientCapabilities.builder()
                .sampling(true)  // Enable sampling support
                // Other capabilities...
                .build())
            .samplingHandler(samplingHandler)
            .build();

        return client;
    }
    
    public static McpAsyncClient createAsyncClient(AsyncSamplingHandler asyncSamplingHandler) {
        List<AsyncSamplingSpecification> samplingSpecifications = 
            new AsyncMcpSamplingProvider(List.of(asyncSamplingHandler)).getSamplingSpecifications();

        Function<CreateMessageRequest, Mono<CreateMessageResult>> samplingHandler = 
            samplingSpecifications.get(0).samplingHandler();

        McpAsyncClient client = McpClient.async(transport)
            .capabilities(ClientCapabilities.builder()
                .sampling(true)  // Enable sampling support
                // Other capabilities...
                .build())
            .samplingHandler(samplingHandler)
            .build();

        return client;
    }
}
```

### Mcp Client Progress Example

```java
public class ProgressHandler {

    /**
     * Handle progress notifications with a single parameter.
     * Note: clients are required for all @McpProgress annotations.
     * @param notification The progress notification
     */
    @McpProgress(clients = "default-client")
    public void handleProgressNotification(ProgressNotification notification) {
        System.out.println(String.format("Progress: %.2f%% - %s", 
            notification.progress() * 100, 
            notification.message()));
    }

    /**
     * Handle progress notifications with individual parameters.
     * Note: clients are required for all @McpProgress annotations.
     * @param progressToken The progress token identifying the operation
     * @param progress The current progress (0.0 to 1.0)
     * @param total Optional total value for the operation
     * @param message Optional progress message
     */
    @McpProgress(clients = "default-client")
    public void handleProgressWithParams(String progressToken, double progress, Double total, String message) {
        if (total != null) {
            System.out.println(String.format("Progress [%s]: %.0f/%.0f - %s", 
                progressToken, progress, total, message));
        } else {
            System.out.println(String.format("Progress [%s]: %.2f%% - %s", 
                progressToken, progress * 100, message));
        }
    }

    /**
     * Handle progress notifications for a specific client.
     * @param notification The progress notification
     */
    @McpProgress(clients = "client-1")
    public void handleClient1Progress(ProgressNotification notification) {
        System.out.println(String.format("Client-1 Progress: %.2f%% - %s", 
            notification.progress() * 100, 
            notification.message()));
    }
}

public class AsyncProgressHandler {

    /**
     * Handle progress notifications asynchronously.
     * @param notification The progress notification
     * @return A Mono that completes when the notification is handled
     */
    @McpProgress
    public Mono<Void> handleAsyncProgress(ProgressNotification notification) {
        return Mono.fromRunnable(() -> {
            System.out.println(String.format("Async Progress: %.2f%% - %s", 
                notification.progress() * 100, 
                notification.message()));
        });
    }

    /**
     * Handle progress notifications for a specific client asynchronously.
     * @param progressToken The progress token
     * @param progress The current progress
     * @param total Optional total value
     * @param message Optional message
     * @return A Mono that completes when the notification is handled
     */
    @McpProgress(clients = "client-2")
    public Mono<Void> handleClient2AsyncProgress(
            String progressToken, 
            double progress, 
            Double total, 
            String message) {
        
        return Mono.fromRunnable(() -> {
            String progressText = total != null ? 
                String.format("%.0f/%.0f", progress, total) : 
                String.format("%.2f%%", progress * 100);
            
            System.out.println(String.format("Client-2 Progress [%s]: %s - %s", 
                progressToken, progressText, message));
        }).then();
    }
}

public class MyMcpClient {

    public static McpSyncClient createSyncClientWithProgress(ProgressHandler progressHandler) {
        List<Consumer<ProgressNotification>> progressConsumers = 
            new SyncMcpProgressProvider(List.of(progressHandler)).getProgressConsumers();

        McpSyncClient client = McpClient.sync(transport)
            .capabilities(ClientCapabilities.builder()
                // Enable capabilities...
                .build())
            .progressConsumers(progressConsumers)
            .build();

        return client;
    }
    
    public static McpAsyncClient createAsyncClientWithProgress(AsyncProgressHandler asyncProgressHandler) {
        List<Function<ProgressNotification, Mono<Void>>> progressHandlers = 
            new AsyncMcpProgressProvider(List.of(asyncProgressHandler)).getProgressHandlers();

        McpAsyncClient client = McpClient.async(transport)
            .capabilities(ClientCapabilities.builder()
                // Enable capabilities...
                .build())
            .progressHandlers(progressHandlers)
            .build();

        return client;
    }
}
```

### Mcp Client Tool List Changed Example

```java
public class ToolListChangedHandler {

    /**
     * Handle tool list change notifications with a single parameter.
     * @param updatedTools The updated list of tools after the change
     */
    @McpToolListChanged
    public void handleToolListChanged(List<McpSchema.Tool> updatedTools) {
        System.out.println("Tool list updated, now contains " + updatedTools.size() + " tools:");
        for (McpSchema.Tool tool : updatedTools) {
            System.out.println("  - " + tool.name() + ": " + tool.description());
        }
    }

    /**
     * Handle tool list change notifications for a specific client.
     * @param updatedTools The updated list of tools after the change
     */
    @McpToolListChanged(clients = "client-1")
    public void handleClient1ToolListChanged(List<McpSchema.Tool> updatedTools) {
        System.out.println("Client-1 tool list updated with " + updatedTools.size() + " tools");
        // Process the updated tool list for client-1
        updateClientToolCache("client-1", updatedTools);
    }

    /**
     * Handle tool list change notifications for another specific client.
     * @param updatedTools The updated list of tools after the change
     */
    @McpToolListChanged(clients = "client-2")
    public void handleClient2ToolListChanged(List<McpSchema.Tool> updatedTools) {
        System.out.println("Client-2 tool list updated with " + updatedTools.size() + " tools");
        // Process the updated tool list for client-2
        updateClientToolCache("client-2", updatedTools);
    }

    private void updateClientToolCache(String[] clients, List<McpSchema.Tool> tools) {
        // Implementation to update tool cache for specific client
        System.out.println("Updated tool cache for " + clients + " with " + tools.size() + " tools");
    }
}

public class AsyncToolListChangedHandler {

    /**
     * Handle tool list change notifications asynchronously.
     * @param updatedTools The updated list of tools after the change
     * @return A Mono that completes when the notification is handled
     */
    @McpToolListChanged
    public Mono<Void> handleAsyncToolListChanged(List<McpSchema.Tool> updatedTools) {
        return Mono.fromRunnable(() -> {
            System.out.println("Async tool list update: " + updatedTools.size() + " tools");
            // Process the updated tool list asynchronously
            processToolListUpdate(updatedTools);
        });
    }

    /**
     * Handle tool list change notifications for a specific client asynchronously.
     * @param updatedTools The updated list of tools after the change
     * @return A Mono that completes when the notification is handled
     */
    @McpToolListChanged(clients = "client-2")
    public Mono<Void> handleClient2AsyncToolListChanged(List<McpSchema.Tool> updatedTools) {
        return Mono.fromRunnable(() -> {
            System.out.println("Client-2 async tool list update: " + updatedTools.size() + " tools");
            // Process the updated tool list for client-2 asynchronously
            processClientToolListUpdate("client-2", updatedTools);
        }).then();
    }

    private void processToolListUpdate(List<McpSchema.Tool> tools) {
        // Implementation to process tool list update
        System.out.println("Processing tool list update with " + tools.size() + " tools");
    }

    private void processClientToolListUpdate(String[] clients, List<McpSchema.Tool> tools) {
        // Implementation to process tool list update for specific client
        System.out.println("Processing tool list update for " + clients + " with " + tools.size() + " tools");
    }
}

public class MyMcpClient {

    public static McpSyncClient createSyncClientWithToolListChanged(ToolListChangedHandler toolListChangedHandler) {
        List<Consumer<List<McpSchema.Tool>>> toolListChangedConsumers = 
            new SyncMcpToolListChangedProvider(List.of(toolListChangedHandler)).getToolListChangedConsumers();

        McpSyncClient client = McpClient.sync(transport)
            .capabilities(ClientCapabilities.builder()
                // Enable capabilities...
                .build())
            .toolListChangedConsumers(toolListChangedConsumers)
            .build();

        return client;
    }
    
    public static McpAsyncClient createAsyncClientWithToolListChanged(AsyncToolListChangedHandler asyncToolListChangedHandler) {
        List<Function<List<McpSchema.Tool>, Mono<Void>>> toolListChangedHandlers = 
            new AsyncMcpToolListChangedProvider(List.of(asyncToolListChangedHandler)).getToolListChangedHandlers();

        McpAsyncClient client = McpClient.async(transport)
            .capabilities(ClientCapabilities.builder()
                // Enable capabilities...
                .build())
            .toolListChangedHandlers(toolListChangedHandlers)
            .build();

        return client;
    }
}
```

### Mcp Client Resource List Changed Example

```java
public class ResourceListChangedHandler {

    /**
     * Handle resource list change notifications with a single parameter.
     * @param updatedResources The updated list of resources after the change
     */
    @McpResourceListChanged
    public void handleResourceListChanged(List<McpSchema.Resource> updatedResources) {
        System.out.println("Resource list updated, now contains " + updatedResources.size() + " resources:");
        for (McpSchema.Resource resource : updatedResources) {
            System.out.println("  - " + resource.name() + ": " + resource.description());
        }
    }

    /**
     * Handle resource list change notifications for a specific client.
     * @param updatedResources The updated list of resources after the change
     */
    @McpResourceListChanged(clients = "client-1")
    public void handleClient1ResourceListChanged(List<McpSchema.Resource> updatedResources) {
        System.out.println("Client-1 resource list updated with " + updatedResources.size() + " resources");
        // Process the updated resource list for client-1
        updateClientResourceCache("client-1", updatedResources);
    }

    /**
     * Handle resource list change notifications for another specific client.
     * @param updatedResources The updated list of resources after the change
     */
    @McpResourceListChanged(clients = "client-2")
    public void handleClient2ResourceListChanged(List<McpSchema.Resource> updatedResources) {
        System.out.println("Client-2 resource list updated with " + updatedResources.size() + " resources");
        // Process the updated resource list for client-2
        updateClientResourceCache("client-2", updatedResources);
    }

    private void updateClientResourceCache(String[] clients, List<McpSchema.Resource> resources) {
        // Implementation to update resource cache for specific client
        System.out.println("Updated resource cache for " + clients + " with " + resources.size() + " resources");
    }
}

public class AsyncResourceListChangedHandler {

    /**
     * Handle resource list change notifications asynchronously.
     * @param updatedResources The updated list of resources after the change
     * @return A Mono that completes when the notification is handled
     */
    @McpResourceListChanged
    public Mono<Void> handleAsyncResourceListChanged(List<McpSchema.Resource> updatedResources) {
        return Mono.fromRunnable(() -> {
            System.out.println("Async resource list update: " + updatedResources.size() + " resources");
            // Process the updated resource list asynchronously
            processResourceListUpdate(updatedResources);
        });
    }

    /**
     * Handle resource list change notifications for a specific client asynchronously.
     * @param updatedResources The updated list of resources after the change
     * @return A Mono that completes when the notification is handled
     */
    @McpResourceListChanged(clients = "client-2")
    public Mono<Void> handleClient2AsyncResourceListChanged(List<McpSchema.Resource> updatedResources) {
        return Mono.fromRunnable(() -> {
            System.out.println("Client-2 async resource list update: " + updatedResources.size() + " resources");
            // Process the updated resource list for client-2 asynchronously
            processClientResourceListUpdate("client-2", updatedResources);
        }).then();
    }

    private void processResourceListUpdate(List<McpSchema.Resource> resources) {
        // Implementation to process resource list update
        System.out.println("Processing resource list update with " + resources.size() + " resources");
    }

    private void processClientResourceListUpdate(String[] clients, List<McpSchema.Resource> resources) {
        // Implementation to process resource list update for specific client
        System.out.println("Processing resource list update for " + clients + " with " + resources.size() + " resources");
    }
}

public class MyMcpClient {

    public static McpSyncClient createSyncClientWithResourceListChanged(ResourceListChangedHandler resourceListChangedHandler) {
        List<Consumer<List<McpSchema.Resource>>> resourceListChangedConsumers = 
            new SyncMcpResourceListChangedProvider(List.of(resourceListChangedHandler)).getResourceListChangedConsumers();

        McpSyncClient client = McpClient.sync(transport)
            .capabilities(ClientCapabilities.builder()
                // Enable capabilities...
                .build())
            .resourceListChangedConsumers(resourceListChangedConsumers)
            .build();

        return client;
    }
    
    public static McpAsyncClient createAsyncClientWithResourceListChanged(AsyncResourceListChangedHandler asyncResourceListChangedHandler) {
        List<Function<List<McpSchema.Resource>, Mono<Void>>> resourceListChangedHandlers = 
            new AsyncMcpResourceListChangedProvider(List.of(asyncResourceListChangedHandler)).getResourceListChangedHandlers();

        McpAsyncClient client = McpClient.async(transport)
            .capabilities(ClientCapabilities.builder()
                // Enable capabilities...
                .build())
            .resourceListChangedHandlers(resourceListChangedHandlers)
            .build();

        return client;
    }
}
```

### Mcp Client Prompt List Changed Example

```java
public class PromptListChangedHandler {

    /**
     * Handle prompt list change notifications with a single parameter.
     * @param updatedPrompts The updated list of prompts after the change
     */
    @McpPromptListChanged
    public void handlePromptListChanged(List<McpSchema.Prompt> updatedPrompts) {
        System.out.println("Prompt list updated, now contains " + updatedPrompts.size() + " prompts:");
        for (McpSchema.Prompt prompt : updatedPrompts) {
            System.out.println("  - " + prompt.name() + ": " + prompt.description());
        }
    }

    /**
     * Handle prompt list change notifications for a specific client.
     * @param updatedPrompts The updated list of prompts after the change
     */
    @McpPromptListChanged(clients = "client-1")
    public void handleClient1PromptListChanged(List<McpSchema.Prompt> updatedPrompts) {
        System.out.println("Client-1 prompt list updated with " + updatedPrompts.size() + " prompts");
        // Process the updated prompt list for client-1
        updateClientPromptCache("client-1", updatedPrompts);
    }

    /**
     * Handle prompt list change notifications for another specific client.
     * @param updatedPrompts The updated list of prompts after the change
     */
    @McpPromptListChanged(clients = "client-2")
    public void handleClient2PromptListChanged(List<McpSchema.Prompt> updatedPrompts) {
        System.out.println("Client-2 prompt list updated with " + updatedPrompts.size() + " prompts");
        // Process the updated prompt list for client-2
        updateClientPromptCache("client-2", updatedPrompts);
    }

    private void updateClientPromptCache(String[] clients, List<McpSchema.Prompt> prompts) {
        // Implementation to update prompt cache for specific client
        System.out.println("Updated prompt cache for " + clients + " with " + prompts.size() + " prompts");
    }
}

public class AsyncPromptListChangedHandler {

    /**
     * Handle prompt list change notifications asynchronously.
     * @param updatedPrompts The updated list of prompts after the change
     * @return A Mono that completes when the notification is handled
     */
    @McpPromptListChanged
    public Mono<Void> handleAsyncPromptListChanged(List<McpSchema.Prompt> updatedPrompts) {
        return Mono.fromRunnable(() -> {
            System.out.println("Async prompt list update: " + updatedPrompts.size() + " prompts");
            // Process the updated prompt list asynchronously
            processPromptListUpdate(updatedPrompts);
        });
    }

    /**
     * Handle prompt list change notifications for a specific client asynchronously.
     * @param updatedPrompts The updated list of prompts after the change
     * @return A Mono that completes when the notification is handled
     */
    @McpPromptListChanged(clients = "client-2")
    public Mono<Void> handleClient2AsyncPromptListChanged(List<McpSchema.Prompt> updatedPrompts) {
        return Mono.fromRunnable(() -> {
            System.out.println("Client-2 async prompt list update: " + updatedPrompts.size() + " prompts");
            // Process the updated prompt list for client-2 asynchronously
            processClientPromptListUpdate("client-2", updatedPrompts);
        }).then();
    }

    private void processPromptListUpdate(List<McpSchema.Prompt> prompts) {
        // Implementation to process prompt list update
        System.out.println("Processing prompt list update with " + prompts.size() + " prompts");
    }

    private void processClientPromptListUpdate(String[] clients, List<McpSchema.Prompt> prompts) {
        // Implementation to process prompt list update for specific client
        System.out.println("Processing prompt list update for " + clients + " with " + prompts.size() + " prompts");
    }
}

public class MyMcpClient {

    public static McpSyncClient createSyncClientWithPromptListChanged(PromptListChangedHandler promptListChangedHandler) {
        List<Consumer<List<McpSchema.Prompt>>> promptListChangedConsumers = 
            new SyncMcpPromptListChangedProvider(List.of(promptListChangedHandler)).getPromptListChangedConsumers();

        McpSyncClient client = McpClient.sync(transport)
            .capabilities(ClientCapabilities.builder()
                // Enable capabilities...
                .build())
            .promptListChangedConsumers(promptListChangedConsumers)
            .build();

        return client;
    }
    
    public static McpAsyncClient createAsyncClientWithPromptListChanged(AsyncPromptListChangedHandler asyncPromptListChangedHandler) {
        List<Function<List<McpSchema.Prompt>, Mono<Void>>> promptListChangedHandlers = 
            new AsyncMcpPromptListChangedProvider(List.of(asyncPromptListChangedHandler)).getPromptListChangedHandlers();

        McpAsyncClient client = McpClient.async(transport)
            .capabilities(ClientCapabilities.builder()
                // Enable capabilities...
                .build())
            .promptListChangedHandlers(promptListChangedHandlers)
            .build();

        return client;
    }
}
```

### Mcp Client Elicitation Example

```java
public class ElicitationHandler {

    /**
     * Handle elicitation requests with a synchronous implementation.
     * Note: clients are required for all @McpElicitation annotations.
     * @param request The elicitation request
     * @return The elicitation result
     */
    @McpElicitation(clients = "default-client")
    public ElicitResult handleElicitationRequest(ElicitRequest request) {
        // Example implementation that accepts the request and returns user data
        // In a real implementation, this would present a form to the user
        // and collect their input based on the requested schema
        
        Map<String, Object> userData = new HashMap<>();
        
        // Check what information is being requested based on the schema
        Map<String, Object> schema = request.requestedSchema();
        if (schema != null && schema.containsKey("properties")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
            
            // Simulate user providing the requested information
            if (properties.containsKey("name")) {
                userData.put("name", "John Doe");
            }
            if (properties.containsKey("email")) {
                userData.put("email", "john.doe@example.com");
            }
            if (properties.containsKey("age")) {
                userData.put("age", 30);
            }
            if (properties.containsKey("preferences")) {
                userData.put("preferences", Map.of("theme", "dark", "notifications", true));
            }
        }
        
        return new ElicitResult(ElicitResult.Action.ACCEPT, userData);
    }

    /**
     * Handle elicitation requests that should be declined.
     * Note: clients are required for all @McpElicitation annotations.
     * @param request The elicitation request
     * @return The elicitation result with decline action
     */
    @McpElicitation(clients = "default-client")
    public ElicitResult handleDeclineElicitationRequest(ElicitRequest request) {
        // Example of declining an elicitation request
        return new ElicitResult(ElicitResult.Action.DECLINE, null);
    }

    /**
     * Handle elicitation requests for a specific client.
     * @param request The elicitation request
     * @return The elicitation result
     */
    @McpElicitation(clients = "client-1")
    public ElicitResult handleClient1ElicitationRequest(ElicitRequest request) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("client", "client-1");
        userData.put("response", "Client-1 specific elicitation response");
        return new ElicitResult(ElicitResult.Action.ACCEPT, userData);
    }
}

public class AsyncElicitationHandler {

    /**
     * Handle elicitation requests with an asynchronous implementation.
     * Note: clients are required for all @McpElicitation annotations.
     * @param request The elicitation request
     * @return A Mono containing the elicitation result
     */
    @McpElicitation(clients = "default-client")
    public Mono<ElicitResult> handleAsyncElicitationRequest(ElicitRequest request) {
        return Mono.fromCallable(() -> {
            // Simulate async processing of the elicitation request
            // In a real implementation, this might involve showing a UI form
            // and waiting for user input
            
            Map<String, Object> userData = new HashMap<>();
            userData.put("response", "Async elicitation response");
            userData.put("timestamp", System.currentTimeMillis());
            userData.put("message", request.message());
            
            return new ElicitResult(ElicitResult.Action.ACCEPT, userData);
        }).delayElement(Duration.ofMillis(100)); // Simulate processing delay
    }

    /**
     * Handle elicitation requests that might be cancelled.
     * Note: clients are required for all @McpElicitation annotations.
     * @param request The elicitation request
     * @return A Mono containing the elicitation result with cancel action
     */
    @McpElicitation(clients = "default-client")
    public Mono<ElicitResult> handleCancelElicitationRequest(ElicitRequest request) {
        return Mono.just(new ElicitResult(ElicitResult.Action.CANCEL, null));
    }

    /**
     * Handle elicitation requests for a specific client asynchronously.
     * @param request The elicitation request
     * @return A Mono containing the elicitation result
     */
    @McpElicitation(clients = "client-2")
    public Mono<ElicitResult> handleClient2AsyncElicitationRequest(ElicitRequest request) {
        return Mono.fromCallable(() -> {
            Map<String, Object> userData = new HashMap<>();
            userData.put("client", "client-2");
            userData.put("response", "Client-2 async elicitation response");
            userData.put("timestamp", System.currentTimeMillis());
            return new ElicitResult(ElicitResult.Action.ACCEPT, userData);
        }).delayElement(Duration.ofMillis(50));
    }
}

public class MyMcpClient {

    public static McpSyncClient createSyncClientWithElicitation(ElicitationHandler elicitationHandler) {
        Function<ElicitRequest, ElicitResult> elicitationHandlerFunc = 
            new SyncMcpElicitationProvider(List.of(elicitationHandler)).getElicitationHandler();

        McpSyncClient client = McpClient.sync(transport)
            .capabilities(ClientCapabilities.builder()
                .elicitation()  // Enable elicitation support
                // Other capabilities...
                .build())
            .elicitationHandler(elicitationHandlerFunc)
            .build();

        return client;
    }
    
    public static McpAsyncClient createAsyncClientWithElicitation(AsyncElicitationHandler asyncElicitationHandler) {
        Function<ElicitRequest, Mono<ElicitResult>> elicitationHandlerFunc = 
            new AsyncMcpElicitationProvider(List.of(asyncElicitationHandler)).getElicitationHandler();

        McpAsyncClient client = McpClient.async(transport)
            .capabilities(ClientCapabilities.builder()
                .elicitation()  // Enable elicitation support
                // Other capabilities...
                .build())
            .elicitationHandler(elicitationHandlerFunc)
            .build();

        return client;
    }
}
```

### Stateless Examples

The library supports stateless implementations that use `McpTransportContext` instead of `McpSyncServerExchange` or `McpAsyncServerExchange`. This is useful for scenarios where you don't need the full server exchange context.

#### Stateless Complete Example

```java
public class StatelessAutocompleteProvider {

    private final Map<String, List<String>> cityDatabase = new HashMap<>();
    
    public StatelessAutocompleteProvider() {
        // Initialize with sample data
        cityDatabase.put("l", List.of("Lagos", "Lima", "Lisbon", "London", "Los Angeles"));
        // Add more data...
    }    

    @McpComplete(prompt = "travel-planner")
    public List<String> completeCityName(McpTransportContext context, CompleteRequest.CompleteArgument argument) {
        String prefix = argument.value().toLowerCase();        
        String firstLetter = prefix.substring(0, 1);
        List<String> cities = cityDatabase.getOrDefault(firstLetter, List.of());
        
        return cities.stream()
            .filter(city -> city.toLowerCase().startsWith(prefix))
            .toList();
    }

    // Stateless method without context parameter
    @McpComplete(prompt = "simple-complete")
    public List<String> simpleComplete(String value) {
        return List.of("option1", "option2", "option3")
            .stream()
            .filter(option -> option.startsWith(value.toLowerCase()))
            .toList();
    }
}
```

#### Stateless Prompt Example

```java
public class StatelessPromptProvider {

    @McpPrompt(name = "simple-greeting", description = "Generate a simple greeting")
    public GetPromptResult simpleGreeting(
            @McpArg(name = "name", description = "The user's name", required = true) String name) {
        
        String message = "Hello, " + name + "! How can I help you today?";
        
        return new GetPromptResult("Simple Greeting",
                List.of(new PromptMessage(Role.ASSISTANT, new TextContent(message))));
    }

    @McpPrompt(name = "contextual-greeting", description = "Generate a greeting with context")
    public GetPromptResult contextualGreeting(
            McpTransportContext context,
            @McpArg(name = "name", description = "The user's name", required = true) String name) {
        
        // You can access transport context if needed
        String message = "Hello, " + name + "! Welcome to our stateless MCP server.";
        
        return new GetPromptResult("Contextual Greeting",
                List.of(new PromptMessage(Role.ASSISTANT, new TextContent(message))));
    }
}
```

#### Stateless Resource Example

```java
public class StatelessResourceProvider {

    private final Map<String, String> resourceData = new HashMap<>();
    
    public StatelessResourceProvider() {
        resourceData.put("config", "server.port=8080\nserver.host=localhost");
        resourceData.put("readme", "# Welcome\nThis is a sample resource.");
    }

    @McpResource(uri = "config://{key}", 
        name = "Configuration", 
        description = "Provides configuration data")
    public String getConfig(String key) {
        return resourceData.getOrDefault(key, "Configuration not found");
    }

    @McpResource(uri = "data://{id}", 
        name = "Data Resource", 
        description = "Provides data with transport context")
    public ReadResourceResult getData(McpTransportContext context, String id) {
        String data = resourceData.getOrDefault(id, "Data not found for ID: " + id);
        
        return new ReadResourceResult(List.of(
            new TextResourceContents("data://" + id, "text/plain", data)
        ));
    }
}
```

**Important Note on Stateless Operations:**
Stateless server methods cannot use bidirectional parameters like `McpSyncRequestContext`, `McpAsyncRequestContext`, `McpSyncServerExchange`, or `McpAsyncServerExchange`. These parameters require client capabilities (roots, elicitation, sampling) that are not available in stateless mode. Methods with these parameters will be automatically filtered out and not registered as stateless operations.

#### Stateless Tool Example

```java
public class StatelessCalculatorProvider {

    @McpTool(name = "add-stateless", description = "Add two numbers (stateless)")
    public int addStateless(
            @McpToolParam(description = "First number", required = true) int a,
            @McpToolParam(description = "Second number", required = true) int b) {
        return a + b;
    }

    @McpTool(name = "multiply-with-context", description = "Multiply with transport context")
    public double multiplyWithContext(
            McpTransportContext context,
            @McpToolParam(description = "First number", required = true) double x,
            @McpToolParam(description = "Second number", required = true) double y) {
        // Access transport context if needed
        return x * y;
    }

    // Async stateless tool
    @McpTool(name = "async-divide", description = "Divide two numbers asynchronously")
    public Mono<Double> asyncDivide(
            @McpToolParam(description = "Dividend", required = true) double dividend,
            @McpToolParam(description = "Divisor", required = true) double divisor) {
        
        return Mono.fromCallable(() -> {
            if (divisor == 0) {
                throw new IllegalArgumentException("Division by zero");
            }
            return dividend / divisor;
        });
    }
}
```

#### Using Stateless Providers

```java
public class StatelessMcpServerFactory {

    public McpSyncServer createStatelessServer(
            StatelessAutocompleteProvider completeProvider,
            StatelessPromptProvider promptProvider,
            StatelessResourceProvider resourceProvider,
            StatelessCalculatorProvider toolProvider) {
        
        // Create stateless specifications
        List<McpStatelessServerFeatures.SyncCompletionSpecification> completionSpecs = 
            new SyncStatelessMcpCompleteProvider(List.of(completeProvider)).getCompleteSpecifications();

        List<McpStatelessServerFeatures.SyncPromptSpecification> promptSpecs = 
            new SyncStatelessMcpPromptProvider(List.of(promptProvider)).getPromptSpecifications();

        List<McpStatelessServerFeatures.SyncResourceSpecification> resourceSpecs = 
            new SyncStatelessMcpResourceProvider(List.of(resourceProvider)).getResourceSpecifications();

        List<McpStatelessServerFeatures.SyncToolSpecification> toolSpecs = 
            new SyncStatelessMcpToolProvider(List.of(toolProvider)).getToolSpecifications();
            
        // Create a stateless server
        McpSyncServer syncServer = McpServer.sync(transportProvider)
            .serverInfo("stateless-server", "1.0.0")
            .capabilities(ServerCapabilities.builder()
                .tools(true)
                .resources(true)
                .prompts(true)
                .completions()
                .logging()
                .build())
            .statelessTools(toolSpecs)
            .statelessResources(resourceSpecs)
            .statelessPrompts(promptSpecs)
            .statelessCompletions(completionSpecs)
            .build();

        return syncServer;
    }
}
```

## Tool Call Exception Handling

The library provides configurable exception handling for tool method callbacks, allowing exceptions to be converted into structured error responses.

### Configuration

Each callback can be configured with a `toolCallExceptionClass` that determines exception handling behavior:

- **Default**: `Exception.class` - All exceptions converted to `CallToolResult` error responses
- **Custom**: Specify a specific exception type for selective handling

### Behavior

| Exception Type | Matches Configuration | Result |
|---|---|---|
| Configured type | ✅ Yes | Returns `CallToolResult` with `isError(true)` |
| Other type | ❌ No | Exception propagated (sync) / `Mono.error()` (async) |

### Error Result Format

Matching exceptions are converted to structured error results:

```java
CallToolResult.builder()
    .isError(true)
    .addTextContent("Error invoking method: " + exception.getMessage())
    .build()
```

### Custom Exception Types

Override `AbstractMcpToolProvider#doGetToolCallException()` to customize the exception type passed to callbacks.


## Features

- **Annotation-based method handling** - Simplifies the creation and registration of MCP methods
- **Support for both synchronous and asynchronous operations** - Flexible integration with different application architectures
- **Stateful and stateless implementations** - Choose between full server exchange context (`McpSyncServerExchange`/`McpAsyncServerExchange`) or lightweight transport context (`McpTransportContext`) for all MCP operations
- **Comprehensive stateless support** - All MCP operations (Complete, Prompt, Resource, Tool) support stateless implementations for scenarios where full server context is not needed
- **Builder pattern for callback creation** - Clean and fluent API for creating method callbacks
- **Comprehensive validation** - Ensures method signatures are compatible with MCP operations
- **URI template support** - Powerful URI template handling for resource and completion operations
- **Tool support with automatic JSON schema generation** - Create MCP tools with automatic input/output schema generation from method signatures
- **Dynamic schema support via CallToolRequest** - Tools can accept `CallToolRequest` parameters to handle dynamic schemas at runtime
- **Configurable exception handling** - Flexible error handling with customizable exception types and automatic error result generation
- **Logging consumer support** - Handle logging message notifications from MCP servers
- **Sampling support** - Handle sampling requests from MCP servers
- **Progress notification support** - Handle progress notifications for long-running operations
- **Tool list changed support** - Handle tool list change notifications from MCP servers when tools are dynamically added, removed, or modified

## Requirements

- Java 17 or higher
- Reactor Core (for async operations)
- MCP Java SDK 0.13.0-SNAPSHOT or higher

## Building from Source

To build the project from source, you'll need:
- JDK 17 or later
- Maven 3.6 or later

Clone the repository and build using Maven:

```bash
git clone https://github.com/spring-ai-community/mcp-annotations.git
cd mcp-annotations
./mvnw clean install
```

## Contributing

Contributions to the MCP Annotations project are welcome! Here's how you can contribute:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/your-feature-name`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin feature/your-feature-name`)
5. Create a new Pull Request

Please make sure to follow the existing code style and include appropriate tests for your changes.

## License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.
