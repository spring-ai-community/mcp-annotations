# MCP Annotations

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java Version](https://img.shields.io/badge/Java-17%2B-orange)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)

The MCP Annotations project provides annotation-based method handling for [Model Context Protocol (MCP)](https://github.com/modelcontextprotocol/spec) servers in Java. It simplifies the creation and registration of MCP server methods through a clean, declarative approach using Java annotations.

## Table of Contents
- [Overview](#overview)
- [Installation](#installation)
- [Key Components](#key-components)
- [Usage Examples](#usage-examples)
- [Features](#features)
- [Requirements](#requirements)
- [Building from Source](#building-from-source)
- [Contributing](#contributing)

This project consists of two main modules:

1. **mcp-annotations** - Core annotations and method handling for MCP operations. Depends only on MCP Java SDK. 
2. **spring-ai-mcp-annotations** - Spring AI integration for MCP annotations

## Overview

The MCP Annotations project enables developers to easily create and register methods for handling MCP operations using simple annotations. It provides a clean, declarative approach to implementing MCP server functionality, reducing boilerplate code and improving maintainability.

This library builds on top of the [MCP Java SDK](https://github.com/modelcontextprotocol/sdk-java) to provide a higher-level, annotation-based programming model for implementing MCP servers and clients.

## Installation

### Core Module

To use the MCP Annotations core module in your project, add the following dependency to your Maven POM file:

```xml
<dependency>
    <groupId>org.springaicommunity</groupId>
    <artifactId>mcp-annotations</artifactId>
    <version>0.2.0-SNAPSHOT</version>
</dependency>
```

### Spring Integration Module

To use the Spring integration module, add the following dependency:

```xml
<dependency>
    <groupId>corg.springaicommunity</groupId>
    <artifactId>spring-ai-mcp-annotations</artifactId>
    <version>0.2.0-SNAPSHOT</version>
</dependency>
```

The Spring integration module also requires the Spring AI dependency.

### Snapshot repositories

To use the mcp-annotations snapshot version you need to add the following repositories to your Maven POM:

```xml
<repositories>
  <repository>
    <id>spring-snapshots</id>
    <name>Spring Snapshots</name>
    <url>https://repo.spring.io/snapshot</url>
    <releases>
      <enabled>false</enabled>
    </releases>
  </repository>
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

### Core Module (mcp-annotations)

The core module provides a set of annotations and callback implementations for primary MCP operations:

1. **Complete** - For auto-completion functionality in prompts and URI templates
2. **Prompt** - For generating prompt messages
3. **Resource** - For accessing resources via URI templates
4. **Tool** - For implementing MCP tools with automatic JSON schema generation
5. **Logging Consumer** - For handling logging message notifications
6. **Sampling** - For handling sampling requests

Each operation type has both synchronous and asynchronous implementations, allowing for flexible integration with different application architectures.

### Spring Integration Module (spring-ai-mcp-annotations)

The Spring integration module provides seamless integration with Spring AI and Spring Framework applications. It handles Spring-specific concerns such as AOP proxies and integrates with Spring AI's model abstractions.

## Key Components

### Annotations

- **`@McpComplete`** - Annotates methods that provide completion functionality for prompts or URI templates
- **`@McpPrompt`** - Annotates methods that generate prompt messages
- **`@McpResource`** - Annotates methods that provide access to resources
- **`@McpTool`** - Annotates methods that implement MCP tools with automatic JSON schema generation
- **`@McpToolParam`** - Annotates tool method parameters with descriptions and requirement specifications
- **`@McpLoggingConsumer`** - Annotates methods that handle logging message notifications from MCP servers
- **`@McpSampling`** - Annotates methods that handle sampling requests from MCP servers
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

#### Logging Consumer
- `AbstractMcpLoggingConsumerMethodCallback` - Base class for logging consumer method callbacks
- `SyncMcpLoggingConsumerMethodCallback` - Synchronous implementation
- `AsyncMcpLoggingConsumerMethodCallback` - Asynchronous implementation using Reactor's Mono

#### Tool
- `SyncMcpToolMethodCallback` - Synchronous implementation for tool method callbacks
- `AsyncMcpToolMethodCallback` - Asynchronous implementation using Reactor's Mono

#### Sampling
- `AbstractMcpSamplingMethodCallback` - Base class for sampling method callbacks
- `SyncMcpSamplingMethodCallback` - Synchronous implementation
- `AsyncMcpSamplingMethodCallback` - Asynchronous implementation using Reactor's Mono

### Providers

The project includes provider classes that scan for annotated methods and create appropriate callbacks:

- `SyncMcpCompletionProvider` - Processes `@McpComplete` annotations for synchronous operations
- `SyncMcpPromptProvider` - Processes `@McpPrompt` annotations for synchronous operations
- `SyncMcpResourceProvider` - Processes `@McpResource` annotations for synchronous operations
- `SyncMcpToolProvider` - Processes `@McpTool` annotations for synchronous operations
- `AsyncMcpToolProvider` - Processes `@McpTool` annotations for asynchronous operations
- `SyncMcpLoggingConsumerProvider` - Processes `@McpLoggingConsumer` annotations for synchronous operations
- `AsyncMcpLoggingConsumerProvider` - Processes `@McpLoggingConsumer` annotations for asynchronous operations
- `SyncMcpSamplingProvider` - Processes `@McpSampling` annotations for synchronous operations
- `AsyncMcpSamplingProvider` - Processes `@McpSampling` annotations for asynchronous operations

### Spring Integration

The Spring integration module provides:

- `SpringAiMcpAnnotationProvider` - Handles Spring-specific concerns when processing MCP annotations
- Integration with Spring AOP proxies
- Support for Spring AI model abstractions

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
             annotations = @McpTool.McpAnnotations(
                 title = "Rectangle Area Calculator",
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
            new SyncMcpCompletionProvider(List.of(autocompleteProvider)).getCompleteSpecifications();

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
     * @param notification The logging message notification
     */
    @McpLoggingConsumer
    public void handleLoggingMessage(LoggingMessageNotification notification) {
        System.out.println("Received logging message: " + notification.level() + " - " + notification.logger() + " - "
                + notification.data());
    }

    /**
     * Handle logging message notifications with individual parameters.
     * @param level The logging level
     * @param logger The logger name
     * @param data The log message data
     */
    @McpLoggingConsumer
    public void handleLoggingMessageWithParams(LoggingLevel level, String logger, String data) {
        System.out.println("Received logging message with params: " + level + " - " + logger + " - " + data);
    }
}

public class MyMcpClient {

    public static McpSyncClient createClient(LoggingHandler loggingHandler) {

        List<Consumer<LoggingMessageNotification>> loggingCOnsummers = 
            new SyncMcpLoggingConsumerProvider(List.of(loggingHandler)).getLoggingConsumers();

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
     * @param request The create message request
     * @return The create message result
     */
    @McpSampling
    public CreateMessageResult handleSamplingRequest(CreateMessageRequest request) {
        // Process the request and generate a response
        return CreateMessageResult.builder()
            .role(Role.ASSISTANT)
            .content(new TextContent("This is a response to the sampling request"))
            .model("test-model")
            .build();
    }
}

public class AsyncSamplingHandler {

    /**
     * Handle sampling requests with an asynchronous implementation.
     * @param request The create message request
     * @return A Mono containing the create message result
     */
    @McpSampling
    public Mono<CreateMessageResult> handleAsyncSamplingRequest(CreateMessageRequest request) {
        return Mono.just(CreateMessageResult.builder()
            .role(Role.ASSISTANT)
            .content(new TextContent("This is an async response to the sampling request"))
            .model("test-model")
            .build());
    }
}

public class MyMcpClient {

    public static McpSyncClient createSyncClient(SamplingHandler samplingHandler) {
        Function<CreateMessageRequest, CreateMessageResult> samplingHandler = 
            new SyncMcpSamplingProvider(List.of(samplingHandler)).getSamplingHandler();

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
        Function<CreateMessageRequest, Mono<CreateMessageResult>> samplingHandler = 
            new AsyncMcpSamplingProvider(List.of(asyncSamplingHandler)).getSamplingHandler();

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


### Spring Integration Example

```java
@Configuration
public class McpConfig {
    
    @Bean
    public List<SyncCompletionSpecification> syncCompletionSpecifications(
            List<AutocompleteProvider> completeProviders) {
        return SpringAiMcpAnnotationProvider.createSyncCompleteSpecifications(completeProviders);
    }
    
    @Bean
    public List<SyncPromptSpecification> syncPromptSpecifications(
            List<PromptProvider> promptProviders) {
        return SpringAiMcpAnnotationProvider.createSyncPromptSpecifications(promptProviders);
    }
    
    @Bean
    public List<SyncResourceSpecification> syncResourceSpecifications(
            List<ResourceProvider> resourceProviders) {
        return SpringAiMcpAnnotationProvider.createSyncResourceSpecifications(resourceProviders);
    }
    
    @Bean
    public List<SyncToolSpecification> syncToolSpecifications(
            List<CalculatorToolProvider> toolProviders) {
        return SpringAiMcpAnnotationProvider.createSyncToolSpecifications(toolProviders);
    }
    
    @Bean
    public List<AsyncToolSpecification> asyncToolSpecifications(
            List<AsyncToolProvider> asyncToolProviders) {
        return SpringAiMcpAnnotationProvider.createAsyncToolSpecifications(asyncToolProviders);
    }
    
    @Bean
    public List<Consumer<LoggingMessageNotification>> syncLoggingConsumers(
            List<LoggingHandler> loggingHandlers) {
        return SpringAiMcpAnnotationProvider.createSyncLoggingConsumers(loggingHandlers);
    }
    
    @Bean
    public Function<CreateMessageRequest, CreateMessageResult> syncSamplingHandler(
            List<SamplingHandler> samplingHandlers) {
        return SpringAiMcpAnnotationProvider.createSyncSamplingHandler(samplingHandlers);
    }
    
    @Bean
    public Function<CreateMessageRequest, Mono<CreateMessageResult>> asyncSamplingHandler(
            List<AsyncSamplingHandler> asyncSamplingHandlers) {
        return SpringAiMcpAnnotationProvider.createAsyncSamplingHandler(asyncSamplingHandlers);
    }
}
```

## Features

- **Annotation-based method handling** - Simplifies the creation and registration of MCP methods
- **Support for both synchronous and asynchronous operations** - Flexible integration with different application architectures
- **Builder pattern for callback creation** - Clean and fluent API for creating method callbacks
- **Comprehensive validation** - Ensures method signatures are compatible with MCP operations
- **URI template support** - Powerful URI template handling for resource and completion operations
- **Tool support with automatic JSON schema generation** - Create MCP tools with automatic input/output schema generation from method signatures
- **Logging consumer support** - Handle logging message notifications from MCP servers
- **Sampling support** - Handle sampling requests from MCP servers
- **Spring integration** - Seamless integration with Spring Framework and Spring AI
- **AOP proxy support** - Proper handling of Spring AOP proxies when processing annotations

## Requirements

- Java 17 or higher
- Reactor Core (for async operations)
- MCP Java SDK 0.10.0 or higher
- Spring Framework and Spring AI (for spring-ai-mcp-annotations module)

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
