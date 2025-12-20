# Simple Builders - Java Builder Generation at Compile Time

[![License](https://img.shields.io/badge/License-MIT%202.0-yellowgreen.svg)](https://github.com/java-helpers/simple-builders/blob/main/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.java-helpers/simple-builders-core.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:io.github.java-helpers%20AND%20a:simple-builders-core)
[![Java Version](https://img.shields.io/badge/Java-17%2B-blue)](https://www.oracle.com/java/technologies/javase/17-relnote-issues.html)
[![Maven](https://img.shields.io/badge/Maven-3.8%2B-orange)](https://maven.apache.org/)
[![codecov](https://codecov.io/gh/java-helpers/simple-builders/graph/badge.svg)](https://codecov.io/gh/java-helpers/simple-builders)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=java-helpers_simple-builders&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=java-helpers_simple-builders)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=java-helpers_simple-builders&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=java-helpers_simple-builders)

## Table of Contents
- [What is Simple Builders?](#what-is-simple-builders)
- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Usage](#usage)
  - [Basic Usage](#basic-usage)
    - [Validation Annotations](#validation-annotations)
    - [Conditional Builder Logic](#conditional-builder-logic)
  - [Collections and Nested Objects](#collections-and-nested-objects)
  - [With Interface Pattern](#with-interface-pattern)
  - [Builder Configuration](#builder-configuration)
    - [Compiler Arguments](#compiler-arguments)
- [Examples](#examples)
  - [Elementary Builder Example](#elementary-builder-example)
  - [Full-Featured Examples](#full-featured-examples)
  - [Advanced Features](#advanced-features)
- [Contributing](#contributing)
- [License](#license)
- [Acknowledgements](#acknowledgements)
- [Links](#links)

## What is Simple Builders?

Simple Builders is a Java [annotation processor](https://docs.oracle.com/en/java/javase/17/docs/specs/man/javac.html#annotation-processing) designed to generate type-safe and high-performance builders for Java classes. It generates builder code at compile-time, ensuring type safety without any runtime reflection overhead.

## Features

- **Low Runtime Dependencies**: The generated code has only dependencies to a core dependency for CollectionBuilders and to Apache.CommonLang3
- **Type-Safe Builders**: Compile-time type checking for all builder methods
- **Fluent API**: Clean, chainable API for object construction
- **Collections Support**: Built-in support for collections and maps
- **Annotation Preservation**: Validation annotations are automatically copied to builder methods
- **With Interface Pattern**: Type-safe object modifications using generated With interfaces

## Requirements

- Java 17 or later
- Maven 3.8+ (for building from source)

## Installation

For Maven-based projects, add the following to your POM file in order to use Simple Builders (the dependencies are available at Maven Central):

```xml
...
<properties>
    <simple-builders.version>0.2.0</simple-builders.version>
</properties>
...
<dependencies>
    <dependency>
        <groupId>io.github.java-helpers</groupId>
        <artifactId>simple-builders-core</artifactId>
        <version>${simple-builders.version}</version>
    </dependency>
</dependencies>
...
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.13.0</version>
            <configuration>
                <source>17</source>
                <target>17</target>
                <annotationProcessorPaths>
                    <path>
                        <groupId>io.github.java-helpers</groupId>
                        <artifactId>simple-builders-processor</artifactId>
                        <version>${simple-builders.version}</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
...
```

If you don't work with a dependency management tool, you can obtain a distribution bundle from [Releases page](https://github.com/java-helpers/simple-builders/releases).

## Usage

### Basic Usage

Annotate your class with `@SimpleBuilder` to generate a builder:

```java
import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

@SimpleBuilder
public class Person {
    private String name;
    private int age;
    private List<String> emailAddresses;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
    
    public List<String> getEmailAddresses() { return emailAddresses; }
    public void setEmailAddresses(List<String> emailAddresses) { 
        this.emailAddresses = emailAddresses; 
    }
}
```

Use the generated builder:

```java
Person person = PersonBuilder.create()
    .name("John Doe")
    .age(30)
    .emailAddresses("john@example.com", "j.doe@example.com")
    .build();
```

#### Validation Annotations

Simple Builders preserves validation annotations on your builder methods:

```java
import jakarta.validation.constraints.*;

@SimpleBuilder
public class User {
    private String email;
    private int age;

    public String getEmail() { return email; }
    public void setEmail(@Email @NotNull String email) { 
        this.email = email; 
    }
    
    public int getAge() { return age; }
    public void setAge(@Min(18) int age) { 
        this.age = age; 
    }
}
```

The generated builder preserves these annotations:

```java
User user = UserBuilder.create()
    .email("user@example.com")  // @Email and @NotNull are on the parameter
    .age(25)                     // @Min(18) is on the parameter
    .build();
```

This ensures validation frameworks work seamlessly with builder-generated objects.

#### Conditional Builder Logic

Apply builder modifications conditionally using the `conditional()` method:

```java
int age = 45;
Person person = PersonBuilder.create()
    .name("Jane Doe")
    .conditional(
        () -> age >= 18,
        p -> p.role("ADULT"),
        p -> p.role("MINOR"))
    .build();
```

For simple conditions without an else case, use the two-parameter overload:

```java
PersonBuilder.create()
    .name("John Doe")
    .conditional(() -> isPremiumUser, p -> p.discountRate(0.15))
    .build();
```

### Collections and Nested Objects

Simple Builders provides special handling for collections and nested objects:

```java
@SimpleBuilder
public class Project {
    private String name;
    private List<Task> tasks;
    private Map<String, String> metadata;
    private ProjectStatus status;
    
    // Getters and setters...
}

@SimpleBuilder
public class Task {
    private String title;
    private String description;
    private boolean completed;
    
    // Getters and setters...
}

public enum ProjectStatus {
    PLANNING, IN_PROGRESS, COMPLETED, ON_HOLD
}
```

Usage with collections and nested builders:

```java
String version = "1.0.0";
Project project = ProjectBuilder.create()
    .name("Simple Builders in version '%s' with a bit of complexity", version)
    .status(ProjectStatus.IN_PROGRESS)
    .tasks(tasks -> tasks
        .add(taskBuilder -> taskBuilder
            .title("Implement core functionality")
            .completed(true)
        )
        .add(taskBuilder -> taskBuilder
            .title("Add documentation")
            .description("Update README and add Javadocs")
        )
    )
    .metadata(metadata -> metadata
        .put("version", "1.0.0")
        .put("owner", "dev-team"))
    .build();
```


### With Interface Pattern

Simple Builders generates a nested `With` interface for each builder field, enabling a clean, type-safe way to create modified copies of objects. This pattern is particularly useful for creating variations of an object:

```java
Person person = PersonBuilder.create()
    .name("John Doe")
    .age(30)
    .build();

// Create a modified copy using the With interface
Person olderPerson = PersonBuilder.create()
    .with(person)
    .age(31)  // Only change the age
    .build();

// By implementing the With interface, you can create modified copies of objects in a type-safe way
Person youngerPerson = person.with(p -> p.age(29));
```

The `With` interface provides type-safe setter methods that mirror the builder's API, making it easy to create object variations without manually copying all fields.

### Builder Configuration

Simple Builders provides extensive configuration options to customize the generated builder code. You can control:

- Field setter generation (Supplier, Provider, Builder patterns)
- Conditional logic helpers
- Access modifiers for builders and methods
- Collection helper methods
- Integration features

Configuration can be applied per-class using `@SimpleBuilder.Options` annotation or project-wide using compiler options.

#### Compiler Arguments

All configuration options are available as compiler arguments using the `-A` flag. For example:

```bash
javac -Asimplebuilder.verbose=true \
      -Asimplebuilder.generateFieldSupplier=false \
      YourClass.java
```

Or in Maven:

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <compilerArgs>
            <arg>-Asimplebuilder.verbose=true</arg>
            <arg>-Asimplebuilder.generateFieldSupplier=false</arg>
        </compilerArgs>
    </configuration>
</plugin>
```

📋 **For a complete list of all available compiler arguments, see [`CompilerArgumentsEnum`](processor/src/main/java/org/javahelpers/simple/builders/processor/enums/CompilerArgumentsEnum.java).**

📖 **For complete documentation, examples, and all available options, see the [Configuration Guide](docs/CONFIGURATION.md).**

## Examples

The `example` module contains real-world examples demonstrating various builder configurations and features. You can explore the source DTOs and their generated builders:

### Elementary Builder Example

A comprehensive example showcasing all fundamental Java property types with a minimal, setter-only builder configuration:

- **Source DTO**: [`BookDto.java`](example/src/main/java/org/javahelpers/simple/builders/example/BookDto.java) - Demonstrates all primitive types, collections, Optional, BigDecimal, date/time types, and nested objects
- **Custom Annotation**: [`@ElementaryBuilder`](example/src/main/java/org/javahelpers/simple/builders/example/ElementaryBuilder.java) - A template annotation that disables all advanced features (suppliers, consumers, collection builders, With interface, @Generated annotation)
- **Generated Builder**: [`BookDtoBuilder.java`](example/target/generated-sources/annotations/org/javahelpers/simple/builders/example/BookDtoBuilder.java) - Clean, minimal builder with only setter methods
- **Tests**: [`BookDtoBuilderTest.java`](example/src/test/java/org/javahelpers/simple/builders/example/BookDtoBuilderTest.java) - Usage examples

### Full-Featured Examples

Examples with all builder features enabled:

- **Person DTO**: [`PersonDto.java`](example/src/main/java/org/javahelpers/simple/builders/example/PersonDto.java) and [`PersonDtoBuilder.java`](example/target/generated-sources/annotations/org/javahelpers/simple/builders/example/PersonDtoBuilder.java) - Demonstrates nested objects, collections, and various setter patterns
- **Product Record**: [`ProductRecord.java`](example/src/main/java/org/javahelpers/simple/builders/example/ProductRecord.java) and [`ProductRecordBuilder.java`](example/target/generated-sources/annotations/org/javahelpers/simple/builders/example/ProductRecordBuilder.java) - Java Record support with full builder features

### Advanced Features

Examples demonstrating special annotations and nested object relationships:

- **Sponsor DTO**: [`SponsorDto.java`](example/src/main/java/org/javahelpers/simple/builders/example/SponsorDto.java) and [`SponsorDtoBuilder.java`](example/target/generated-sources/annotations/org/javahelpers/simple/builders/example/SponsorDtoBuilder.java) - Simple DTO used as nested object in other examples
- **Mannschaft DTO**: [`MannschaftDto.java`](example/src/main/java/org/javahelpers/simple/builders/example/MannschaftDto.java) and [`MannschaftDtoBuilder.java`](example/target/generated-sources/annotations/org/javahelpers/simple/builders/example/MannschaftDtoBuilder.java) - Demonstrates `@IgnoreInBuilder` annotation to exclude specific setter methods from the generated builder, plus Set collections with nested objects

These examples serve as both documentation and integration tests for the annotation processor.

## Contributing

Contributions are welcome! Please see [CONTRIBUTING.md](docs/CONTRIBUTING.md) for:

- Development setup and project structure
- Building and testing strategies (important for annotation processor modules)
- Debugging with verbose output
- Code style and formatting
- Pull request process

For maintainers, see [RELEASE.md](RELEASE.md) for the release process.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Acknowledgements

This project was made possible thanks to the following:

### Inspiration and Patterns

- **[Benji Weber](https://benjiweber.co.uk/blog/2020/09/19/fun-with-java-records/)** - The With interface pattern is inspired by Benji's innovative work on functional builders and extending Java Records.
- **[RecordBuilder](https://github.com/Randgalt/record-builder)** by Randall Hauch - A state-of-the-art builder solution for Java records. If your project uses records exclusively, RecordBuilder is an excellent choice. Simple Builders extends these concepts to traditional Java classes.

### Tools and Libraries

- **[JavaPoet](https://github.com/palantir/javapoet)** - An excellent library for generating Java source code. Originally created by Square, now maintained by Palantir. JavaPoet made it straightforward to generate clean, readable builder code.
- **[Google Compile Testing](https://github.com/google/compile-testing)** - Essential for testing annotation processors with comprehensive compilation diagnostics.

### Learning Resources

The following resources were invaluable for understanding annotation processing:

- **[Baeldung: Java Annotation Processing and Creating a Builder](https://www.baeldung.com/java-annotation-processing-builder)** - Comprehensive guide to annotation processing fundamentals
- **[SkyRo Tech: Code Generation with JavaPoet in Practice](https://medium.com/skyro-tech/code-generation-with-javapoet-on-practice-bfbe8ca56a61)** - Practical examples of using JavaPoet
- **[Annotation Processing Demo](https://github.com/ledungcobra/annotation-processing-demo)** by Le Dung - Hands-on examples of annotation processor implementation

Thank you to all contributors and the Java community for making this project possible!

## Links

* [Source code](https://github.com/java-helpers/simple-builders/)
* [Downloads](https://github.com/java-helpers/simple-builders/releases)
* [Issue tracker](https://github.com/java-helpers/simple-builders/issues)
* [CI build](https://github.com/java-helpers/simple-builders/actions/)
