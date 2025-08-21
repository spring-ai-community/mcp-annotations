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
2. **mcp-annotations-spring** - Spring AI integration for MCP annotations

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
    <groupId>org.springaicommunity</groupId>
    <artifactId>mcp-annotations-spring</artifactId>
    <version>0.2.0-SNAPSHOT</version>
</dependency>
```

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
7. **Elicitation** - For handling elicitation requests to gather additional information from users

Each operation type has both synchronous and asynchronous implementations, allowing for flexible integration with different application architectures.

### Spring Integration Module (mcp-annotations-spring)

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
- **`@McpElicitation`** - Annotates methods that handle elicitation requests to gather additional information from users
- **`@McpArg`** - Annotates method parameters as MCP arguments

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
- `AbstractMcpLoggingConsumerMethodCallback` - Base class for logging consumer method callbacks
- `SyncMcpLoggingConsumerMethodCallback` - Synchronous implementation
- `AsyncMcpLoggingConsumerMethodCallback` - Asynchronous implementation using Reactor's Mono

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

### Providers

The project includes provider classes that scan for annotated methods and create appropriate callbacks:

#### Stateful Providers (using McpSyncServerExchange/McpAsyncServerExchange)
- `SyncMcpCompletionProvider` - Processes `@McpComplete` annotations for synchronous operations
- `SyncMcpPromptProvider` - Processes `@McpPrompt` annotations for synchronous operations
- `SyncMcpResourceProvider` - Processes `@McpResource` annotations for synchronous operations
- `SyncMcpToolProvider` - Processes `@McpTool` annotations for synchronous operations
- `AsyncMcpToolProvider` - Processes `@McpTool` annotations for asynchronous operations
- `SyncMcpLoggingConsumerProvider` - Processes `@McpLoggingConsumer` annotations for synchronous operations
- `AsyncMcpLoggingConsumerProvider` - Processes `@McpLoggingConsumer` annotations for asynchronous operations
- `SyncMcpSamplingProvider` - Processes `@McpSampling` annotations for synchronous operations
- `AsyncMcpSamplingProvider` - Processes `@McpSampling` annotations for asynchronous operations
- `SyncMcpElicitationProvider` - Processes `@McpElicitation` annotations for synchronous operations
- `AsyncMcpElicitationProvider` - Processes `@McpElicitation` annotations for asynchronous operations

#### Stateless Providers (using McpTransportContext)
- `SyncStatelessMcpCompleteProvider` - Processes `@McpComplete` annotations for synchronous stateless operations
- `AsyncStatelessMcpCompleteProvider` - Processes `@McpComplete` annotations for asynchronous stateless operations
- `SyncStatelessMcpPromptProvider` - Processes `@McpPrompt` annotations for synchronous stateless operations
- `AsyncStatelessMcpPromptProvider` - Processes `@McpPrompt` annotations for asynchronous stateless operations
- `SyncStatelessMcpResourceProvider` - Processes `@McpResource` annotations for synchronous stateless operations
- `AsyncStatelessMcpResourceProvider` - Processes `@McpResource` annotations for asynchronous stateless operations
- `SyncStatelessMcpToolProvider` - Processes `@McpTool` annotations for synchronous stateless operations
- `AsyncStatelessMcpToolProvider` - Processes `@McpTool` annotations for asynchronous stateless operations

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

    /**
     * Handle logging message notifications for a specific client.
     * @param notification The logging message notification
     */
    @McpLoggingConsumer(clientId = "client-1")
    public void handleClient1LoggingMessage(LoggingMessageNotification notification) {
        System.out.println("Client-1 logging message: " + notification.level() + " - " + notification.data());
    }

    /**
     * Handle logging message notifications for another specific client.
     * @param notification The logging message notification
     */
    @McpLoggingConsumer(clientId = "client-2")
    public void handleClient2LoggingMessage(LoggingMessageNotification notification) {
        System.out.println("Client-2 logging message: " + notification.level() + " - " + notification.data());
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

    /**
     * Handle sampling requests for a specific client.
     * @param request The create message request
     * @return The create message result
     */
    @McpSampling(clientId = "client-1")
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

    /**
     * Handle sampling requests for a specific client asynchronously.
     * @param request The create message request
     * @return A Mono containing the create message result
     */
    @McpSampling(clientId = "client-2")
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

### Mcp Client Elicitation Example

```java
public class ElicitationHandler {

    /**
     * Handle elicitation requests with a synchronous implementation.
     * @param request The elicitation request
     * @return The elicitation result
     */
    @McpElicitation
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
     * @param request The elicitation request
     * @return The elicitation result with decline action
     */
    @McpElicitation
    public ElicitResult handleDeclineElicitationRequest(ElicitRequest request) {
        // Example of declining an elicitation request
        return new ElicitResult(ElicitResult.Action.DECLINE, null);
    }

    /**
     * Handle elicitation requests for a specific client.
     * @param request The elicitation request
     * @return The elicitation result
     */
    @McpElicitation(clientId = "client-1")
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
     * @param request The elicitation request
     * @return A Mono containing the elicitation result
     */
    @McpElicitation
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
     * @param request The elicitation request
     * @return A Mono containing the elicitation result with cancel action
     */
    @McpElicitation
    public Mono<ElicitResult> handleCancelElicitationRequest(ElicitRequest request) {
        return Mono.just(new ElicitResult(ElicitResult.Action.CANCEL, null));
    }

    /**
     * Handle elicitation requests for a specific client asynchronously.
     * @param request The elicitation request
     * @return A Mono containing the elicitation result
     */
    @McpElicitation(clientId = "client-2")
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
        Function<ElicitRequest, ElicitResult> elicitationHandler = 
            new SyncMcpElicitationProvider(List.of(elicitationHandler)).getElicitationHandler();

        McpSyncClient client = McpClient.sync(transport)
            .capabilities(ClientCapabilities.builder()
                .elicitation()  // Enable elicitation support
                // Other capabilities...
                .build())
            .elicitationHandler(elicitationHandler)
            .build();

        return client;
    }
    
    public static McpAsyncClient createAsyncClientWithElicitation(AsyncElicitationHandler asyncElicitationHandler) {
        Function<ElicitRequest, Mono<ElicitResult>> elicitationHandler = 
            new AsyncMcpElicitationProvider(List.of(asyncElicitationHandler)).getElicitationHandler();

        McpAsyncClient client = McpClient.async(transport)
            .capabilities(ClientCapabilities.builder()
                .elicitation()  // Enable elicitation support
                // Other capabilities...
                .build())
            .elicitationHandler(elicitationHandler)
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
    public List<SyncLoggingSpecification> syncLoggingSpecifications(
            List<LoggingHandler> loggingHandlers) {
        return SpringAiMcpAnnotationProvider.createSyncLoggingSpecifications(loggingHandlers);
    }
    
    @Bean
    public List<AsyncLoggingSpecification> asyncLoggingSpecifications(
            List<AsyncLoggingHandler> asyncLoggingHandlers) {
        return SpringAiMcpAnnotationProvider.createAsyncLoggingSpecifications(asyncLoggingHandlers);
    }
    
    @Bean
    public List<SyncSamplingSpecification> syncSamplingSpecifications(
            List<SamplingHandler> samplingHandlers) {
        return SpringAiMcpAnnotationProvider.createSyncSamplingSpecifications(samplingHandlers);
    }
    
    @Bean
    public List<AsyncSamplingSpecification> asyncSamplingSpecifications(
            List<AsyncSamplingHandler> asyncSamplingHandlers) {
        return SpringAiMcpAnnotationProvider.createAsyncSamplingSpecifications(asyncSamplingHandlers);
    }
    
    @Bean
    public List<SyncElicitationSpecification> syncElicitationSpecifications(
            List<ElicitationHandler> elicitationHandlers) {
        return SpringAiMcpAnnotationProvider.createSyncElicitationSpecifications(elicitationHandlers);
    }
    
    @Bean
    public List<AsyncElicitationSpecification> asyncElicitationSpecifications(
            List<AsyncElicitationHandler> asyncElicitationHandlers) {
        return SpringAiMcpAnnotationProvider.createAsyncElicitationSpecifications(asyncElicitationHandlers);
    }
    
    // Stateless Spring Integration Examples
    
    @Bean
    public List<McpStatelessServerFeatures.SyncToolSpecification> syncStatelessToolSpecifications(
            List<StatelessCalculatorProvider> statelessToolProviders) {
        return SpringAiMcpAnnotationProvider.createSyncStatelessToolSpecifications(statelessToolProviders);
    }
    
    @Bean
    public List<McpStatelessServerFeatures.SyncPromptSpecification> syncStatelessPromptSpecifications(
            List<StatelessPromptProvider> statelessPromptProviders) {
        return SpringAiMcpAnnotationProvider.createSyncStatelessPromptSpecifications(statelessPromptProviders);
    }
    
    @Bean
    public List<McpStatelessServerFeatures.SyncResourceSpecification> syncStatelessResourceSpecifications(
            List<StatelessResourceProvider> statelessResourceProviders) {
        return SpringAiMcpAnnotationProvider.createSyncStatelessResourceSpecifications(statelessResourceProviders);
    }
}
```

## Features

- **Annotation-based method handling** - Simplifies the creation and registration of MCP methods
- **Support for both synchronous and asynchronous operations** - Flexible integration with different application architectures
- **Stateful and stateless implementations** - Choose between full server exchange context (`McpSyncServerExchange`/`McpAsyncServerExchange`) or lightweight transport context (`McpTransportContext`) for all MCP operations
- **Comprehensive stateless support** - All MCP operations (Complete, Prompt, Resource, Tool) support stateless implementations for scenarios where full server context is not needed
- **Builder pattern for callback creation** - Clean and fluent API for creating method callbacks
- **Comprehensive validation** - Ensures method signatures are compatible with MCP operations
- **URI template support** - Powerful URI template handling for resource and completion operations
- **Tool support with automatic JSON schema generation** - Create MCP tools with automatic input/output schema generation from method signatures
- **Logging consumer support** - Handle logging message notifications from MCP servers
- **Sampling support** - Handle sampling requests from MCP servers
- **Spring integration** - Seamless integration with Spring Framework and Spring AI, including support for both stateful and stateless operations
- **AOP proxy support** - Proper handling of Spring AOP proxies when processing annotations

## Requirements

- Java 17 or higher
- Reactor Core (for async operations)
- MCP Java SDK 0.11.2 or higher
- Spring Framework and Spring AI (for mcp-annotations-spring module)

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
