# MCP Annotations

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java Version](https://img.shields.io/badge/Java-17%2B-orange)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)

The MCP Annotations project provides annotation-based method handling for [Model Context Protocol (MCP)](https://github.com/modelcontextprotocol/spec) servers in Java. It simplifies the creation and registration of MCP server methods through a clean, declarative approach using Java annotations.

## Table of Contents
- [Overview](#overview)
- [Key Components](#key-components)
- [Usage Examples](#usage-examples)
- [Installation](#installation)
- [Features](#features)
- [Requirements](#requirements)
- [Building from Source](#building-from-source)
- [Contributing](#contributing)
- [License](#license)
- [Repository](#repository)

This project consists of two main modules:

1. **mcp-annotations** - Core annotations and method handling for MCP operations
2. **spring-ai-mcp-annotations** - Spring AI integration for MCP annotations

## Overview

The MCP Annotations project enables developers to easily create and register methods for handling MCP operations using simple annotations. It provides a clean, declarative approach to implementing MCP server functionality, reducing boilerplate code and improving maintainability.

This library builds on top of the [MCP Java SDK](https://github.com/modelcontextprotocol/sdk-java) to provide a higher-level, annotation-based programming model for implementing MCP servers and clients.

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
- **`@McpLoggingConsumer`** - Annotates methods that handle logging message notifications from MCP servers
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

### Providers

The project includes provider classes that scan for annotated methods and create appropriate callbacks:

- `SyncMcpCompletionProvider` - Processes `@McpComplete` annotations for synchronous operations
- `SyncMcpPromptProvider` - Processes `@McpPrompt` annotations for synchronous operations
- `SyncMcpResourceProvider` - Processes `@McpResource` annotations for synchronous operations
- `SyncMcpLoggingConsumerProvider` - Processes `@McpLoggingConsumer` annotations for synchronous operations
- `AsyncMcpLoggingConsumerProvider` - Processes `@McpLoggingConsumer` annotations for asynchronous operations

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

### Mcp Server with Resource, Prompt and Completion capabilities

```java
public class McpServerFactory {

    public McpSyncServer createMcpServer(
            MyResourceProvider myResourceProvider, 
            AutocompleteProvider autocompleteProvider,
            LoggingHandler loggingHandler) {
        
        List<SyncResourceSpecification> resourceSpecifications = 
            new SyncMcpResourceProvider(List.of(myResourceProvider)).getResourceSpecifications();

        List<SyncCompletionSpecification> completionSpecifications = 
            new SyncMcpCompletionProvider(List.of(autocompleteProvider)).getCompleteSpecifications();

        List<SyncPromptSpecification> promptSpecifications = 
            new SyncMcpPromptProvider(List.of(autocompleteProvider)).getPromptSpecifications();
            
        List<Consumer<LoggingMessageNotification>> loggingConsumers = 
            new SyncMcpLoggingConsumerProvider(List.of(loggingHandler)).getLoggingConsumers();

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
    public List<Consumer<LoggingMessageNotification>> syncLoggingConsumers(
            List<LoggingHandler> loggingHandlers) {
        return SpringAiMcpAnnotationProvider.createSyncLoggingConsumers(loggingHandlers);
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

The Spring integration module also requires the Spring AI dependency.


## Features

- **Annotation-based method handling** - Simplifies the creation and registration of MCP methods
- **Support for both synchronous and asynchronous operations** - Flexible integration with different application architectures
- **Builder pattern for callback creation** - Clean and fluent API for creating method callbacks
- **Comprehensive validation** - Ensures method signatures are compatible with MCP operations
- **URI template support** - Powerful URI template handling for resource and completion operations
- **Logging consumer support** - Handle logging message notifications from MCP servers
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

## Repository

- GitHub: [https://github.com/spring-ai-community/mcp-annotations](https://github.com/spring-ai-community/mcp-annotations)
