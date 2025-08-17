# Simple Builders - Java Builder Generation at Compile Time

[![License](https://img.shields.io/badge/License-MIT%202.0-yellowgreen.svg)](https://github.com/java-helpers/simple-builders/blob/main/LICENSE)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.java-helpers/simple-builders-core.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:io.github.java-helpers%20AND%20a:simple-builders-core)
[![Java Version](https://img.shields.io/badge/Java-17%2B-blue)](https://www.oracle.com/java/technologies/javase/17-relnote-issues.html)
[![Maven](https://img.shields.io/badge/Maven-3.6%2B-orange)](https://maven.apache.org/)

## Table of Contents
- [What is Simple Builders?](#what-is-simple-builders)
- [Features](#features)
- [Requirements](#requirements)
- [Installation](#installation)
- [Usage](#usage)
  - [Basic Usage](#basic-usage)
  - [Collections and Nested Objects](#collections-and-nested-objects)
- [Building from Source](#building-from-source)
- [Contributing](#contributing)
- [License](#license)

## What is Simple Builders?

Simple Builders is a Java [annotation processor](https://docs.oracle.com/en/java/javase/17/docs/specs/man/javac.html#annotation-processing) designed to generate type-safe and high-performance builders for Java classes. It generates builder code at compile-time, ensuring type safety without any runtime reflection overhead.

## Features

- **Zero Runtime Dependencies**: The generated code has no runtime dependencies
- **Type-Safe Builders**: Compile-time type checking for all builder methods
- **Fluent API**: Clean, chainable API for object construction
- **Collections Support**: Built-in support for collections and maps
- **Nested Builders**: Automatic generation of nested object builders

## Requirements

- Java 17 or later
- Maven 3.8+ (for building from source)

## Installation

For Maven-based projects, add the following to your POM file in order to use Simple Builders (the dependencies are available at Maven Central):

```xml
...
<properties>
    <org.simple-builders.version>0.1.0</org.simple-builders.version>
</properties>
...
<dependencies>
    <dependency>
        <groupId>io.github.java-helpers</groupId>
        <artifactId>simple-builders-core</artifactId>
        <version>${org.simple-builders.version}</version>
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
                        <version>${org.simple-builders.version}</version>
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
import org.javahelpers.simple.builders.core.annotation.SimpleBuilder;

@SimpleBuilder
public class Person {
    private String name;
    private int age;
    private List<String> emailAddresses;

    // Getters and setters
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
Project project = ProjectBuilder.create()
    .name("Simple Builders with a bit of complexity")
    .status(ProjectStatus.IN_PROGRESS)
    .tasks(tasks -> tasks
        .add(TaskBuilder.create()
            .title("Implement core functionality")
            .completed(true)
            .build())
        .add(TaskBuilder.create()
            .title("Add documentation")
            .description("Update README and add Javadocs")
            .build())
    )
    .metadata(Map.of("version", "1.0.0", "owner", "dev-team"))
    .build();
```

## Building from Source

1. Clone the repository:
   ```bash
   git clone https://github.com/java-helpers/simple-builders.git
   cd simple-builders
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run tests:
   ```bash
   mvn test
   ```

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a new Pull Request

Please ensure your code follows the project's code style and includes appropriate tests.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.


## Links

* [Source code](https://github.com/java-helpers/simple-builders/)
* [Downloads](https://github.com/java-helpers/simple-builders/releases)
* [Issue tracker](https://github.com/java-helpers/simple-builders/issues)
* [CI build](https://github.com/java-helpers/simple-builders/actions/)

## Licensing

MapStruct is licensed under the MIT License; you may not use this project except in compliance with the License. You may obtain a copy of the License at http://www.opensource.org/licenses/mit-license.php.
