# Configuration

Simple-builders supports fine-grained configuration through the `@SimpleBuilder.Options` annotation and compiler options.

## Table of Contents

- [Overview](#overview)
- [Annotation Configuration](#annotation-configuration)
- [Template Annotations](#template-annotations)
- [Compiler Options](#compiler-options)
  - [Maven Configuration](#maven-configuration)
  - [Gradle Configuration](#gradle-configuration)
  - [IntelliJ IDEA Configuration](#intellij-idea-configuration)
- [Configuration Options](#configuration-options)
  - [Field Setter Generation](#field-setter-generation)
  - [Conditional Logic](#conditional-logic)
  - [Access Control](#access-control)
  - [Collection Helpers](#collection-helpers)
  - [Integration](#integration)
- [Examples](#examples)
  - [Minimal Builder](#minimal-builder)
  - [Internal API Builder](#internal-api-builder)
  - [Collection-Heavy Builder](#collection-heavy-builder)
  - [Minimal Builder Template](#minimal-builder-template)
  - [Project-Wide Defaults](#project-wide-defaults)
- [Priority Rules](#priority-rules)
  - [Example: Priority in Action](#example-priority-in-action)
- [AccessModifier Enum](#accessmodifier-enum)
- [Troubleshooting](#troubleshooting)
  - [Compiler Options Not Working](#compiler-options-not-working)
  - [Annotation Values Not Applied](#annotation-values-not-applied)
  - [Access Level Issues](#access-level-issues)
  - [Template Annotations Not Working](#template-annotations-not-working)
- [Best Practices](#best-practices)
- [Reference](#reference)
  - [All Compiler Options](#all-compiler-options)
  - [Complete Options Example](#complete-options-example)

## Overview

Configuration follows a priority system:
1. **Annotation values** - Highest priority
2. **Compiler options** - Medium priority  
3. **Default values** - Lowest priority

This allows you to set project-wide defaults while still being able to override them per-class when needed.

## Annotation Configuration

Configure individual builders using `@SimpleBuilder` with `@SimpleBuilder.Options`:

```java
@SimpleBuilder
@SimpleBuilder.Options(
    generateFieldSupplier = true,
    generateFieldProvider = true,
    generateBuilderProvider = true,
    generateConditionalHelper = true,
    builderAccess = AccessModifier.PUBLIC,
    methodAccess = AccessModifier.PUBLIC,
    generateVarArgsHelpers = true,
    usingArrayListBuilder = true,
    usingHashMapBuilder = true,
    generateWithInterface = true
)
public class PersonDto {
    private String name;
    private int age;
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }
}
```

## Template Annotations

Create reusable configuration presets with custom template annotations:

```java
@SimpleBuilder.Template(options = @SimpleBuilder.Options(
    generateFieldSupplier = false,
    generateFieldProvider = false,
    generateBuilderProvider = false,
    generateConditionalHelper = false,
    generateVarArgsHelpers = false,
    usingArrayListBuilder = false,
    usingArrayListBuilderWithElementBuilders = false,
    usingHashSetBuilder = false,
    usingHashSetBuilderWithElementBuilders = false,
    usingHashMapBuilder = false,
    generateWithInterface = false
))
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface MinimalBuilder {}
```

Then use your template:

```java
@MinimalBuilder  // No need for @SimpleBuilder - template includes it!
public class PersonDto {
    private String name;
}
```

## Compiler Options

Set project-wide defaults via compiler options. These apply to all builders unless overridden by annotations.

### Maven Configuration

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.11.0</version>
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
        <compilerArgs>
            <arg>-Asimplebuilder.generateFieldSupplier=true</arg>
            <arg>-Asimplebuilder.generateFieldProvider=true</arg>
            <arg>-Asimplebuilder.builderAccess=PUBLIC</arg>
            <arg>-Asimplebuilder.usingArrayListBuilder=true</arg>
        </compilerArgs>
    </configuration>
</plugin>
```

### Gradle Configuration

```gradle
dependencies {
    annotationProcessor "io.github.java-helpers:simple-builders-processor:${simpleBuildersVersion}"
}

compileJava {
    options.compilerArgs += [
        "-Asimplebuilder.generateFieldSupplier=true",
        "-Asimplebuilder.generateFieldProvider=true",
        "-Asimplebuilder.builderAccess=PUBLIC"
    ]
}
```

### IntelliJ IDEA Configuration

1. Go to **Settings → Build, Execution, Deployment → Compiler → Java Compiler**
2. Add to **Additional command line parameters**:
   ```
   -Asimplebuilder.generateFieldSupplier=true -Asimplebuilder.builderAccess=PUBLIC
   ```

## Configuration Options

### Field Setter Generation

| Option | Type | Default | Compiler Option | Description |
|--------|------|---------|------------------|-------------|
| `generateFieldSupplier` | boolean | `true` | `-Asimplebuilder.generateFieldSupplier` | Generate setter methods accepting `Supplier<T>` for field values |
| `generateFieldProvider` | boolean | `true` | `-Asimplebuilder.generateFieldProvider` | Generate setter methods accepting `Provider<T>` for complex field types |
| `generateBuilderProvider` | boolean | `true` | `-Asimplebuilder.generateBuilderProvider` | Generate setter methods accepting `Provider<Builder<T>>` for buildable types |

### Conditional Logic

| Option | Type | Default | Compiler Option | Description |
|--------|------|---------|------------------|-------------|
| `generateConditionalHelper` | boolean | `true` | `-Asimplebuilder.generateConditionalHelper` | Generate conditional/when methods for fluent conditional logic |

### Access Control

| Option | Type | Default | Values | Compiler Option | Description |
|--------|------|---------|--------|------------------|-------------|
| `builderAccess` | AccessModifier | `PUBLIC` | `PUBLIC`, `PROTECTED`, `PACKAGE_PRIVATE`, `PRIVATE` | `-Asimplebuilder.builderAccess` | Visibility level for generated builder class |
| `methodAccess` | AccessModifier | `PUBLIC` | `PUBLIC`, `PROTECTED`, `PACKAGE_PRIVATE`, `PRIVATE` | `-Asimplebuilder.methodAccess` | Visibility level for generated builder methods |

### Collection Helpers

| Option | Type | Default | Compiler Option | Description |
|--------|------|---------|------------------|-------------|
| `generateVarArgsHelpers` | boolean | `true` | `-Asimplebuilder.generateVarArgsHelpers` | Generate varargs methods for Lists and Sets |
| `usingArrayListBuilder` | boolean | `true` | `-Asimplebuilder.usingArrayListBuilder` | Use chaining ArrayListBuilder for Lists |
| `usingArrayListBuilderWithElementBuilders` | boolean | `true` | `-Asimplebuilder.usingArrayListBuilderWithElementBuilders` | Use ArrayListBuilderWithElementBuilders for Lists of complex objects |
| `usingHashSetBuilder` | boolean | `true` | `-Asimplebuilder.usingHashSetBuilder` | Use chaining HashSetBuilder for Sets |
| `usingHashSetBuilderWithElementBuilders` | boolean | `true` | `-Asimplebuilder.usingHashSetBuilderWithElementBuilders` | Use HashSetBuilderWithElementBuilders for Sets of complex objects |
| `usingHashMapBuilder` | boolean | `true` | `-Asimplebuilder.usingHashMapBuilder` | Use chaining HashMapBuilder for Maps |

### Integration

| Option | Type | Default | Compiler Option | Description |
|--------|------|---------|------------------|-------------|
| `generateWithInterface` | boolean | `true` | `-Asimplebuilder.generateWithInterface` | Generate With interface for DTO integration |

## Examples

### Minimal Builder

Generate only essential builder methods:

```java
@SimpleBuilder
@SimpleBuilder.Options(
    generateFieldSupplier = false,
    generateFieldProvider = false,
    generateBuilderProvider = false,
    generateConditionalHelper = false,
    generateVarArgsHelpers = false,
    usingArrayListBuilder = false,
    usingHashMapBuilder = false,
    generateWithInterface = false
)
public class MinimalDto {
    private String name;
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
```

**Generated**: Only basic builder methods (`create()`, field setters, `build()`)

### Internal API Builder

Create builders for internal use only:

```java
@SimpleBuilder
@SimpleBuilder.Options(
    builderAccess = AccessModifier.PACKAGE_PRIVATE,
    methodAccess = AccessModifier.PACKAGE_PRIVATE
)
public class InternalConfig {
    private String secretKey;
    
    public String getSecretKey() { return secretKey; }
    public void setSecretKey(String secretKey) { this.secretKey = secretKey; }
}
```

**Generated**: Package-private builder and methods, only accessible within the same package

### Collection-Heavy Builder

Optimize for collection manipulation:

```java
@SimpleBuilder
@SimpleBuilder.Options(
    generateVarArgsHelpers = true,
    usingArrayListBuilder = true,
    usingArrayListBuilderWithElementBuilders = true,
    usingHashSetBuilder = true,
    usingHashMapBuilder = true
)
public class TeamDto {
    private List<String> memberNames;
    private Set<PersonDto> members;
    private Map<String, PersonDto> memberMap;
}
```

**Generated**: Chained collection builders for fluent collection manipulation

Example usage:
```java
TeamDto team = TeamDtoBuilder.create()
    .memberNames(list -> list.add("Alice").add("Bob"))
    .members(set -> set
        .add(person -> person.name("Alice").age(30))
        .add(person -> person.name("Bob").age(25)))
    .memberMap(map -> map.put("Alice", alice).put("Bob", bob))
    .build();
```

### Minimal Builder Template

Create a reusable template for lightweight builders:

```java
@SimpleBuilder.Template(options = @SimpleBuilder.Options(
    generateFieldSupplier = false,
    generateFieldProvider = false,
    generateBuilderProvider = false,
    generateConditionalHelper = false,
    generateVarArgsHelpers = false,
    usingArrayListBuilder = false,
    usingArrayListBuilderWithElementBuilders = false,
    usingHashSetBuilder = false,
    usingHashSetBuilderWithElementBuilders = false,
    usingHashMapBuilder = false,
    generateWithInterface = false
))
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface MinimalBuilder {}
```

Use everywhere:
```java
@MinimalBuilder
public class CustomerDto {
    private String name;
    private List<OrderDto> orders;
}
```

### Project-Wide Defaults

Set sensible defaults for your entire project:

```xml
<!-- Maven compiler plugin configuration -->
<compilerArgs>
    <!-- Enable all field setter variants -->
    <arg>-Asimplebuilder.generateFieldSupplier=true</arg>
    <arg>-Asimplebuilder.generateFieldProvider=true</arg>
    <arg>-Asimplebuilder.generateBuilderProvider=true</arg>
    
    <!-- Make builders package-private for internal APIs -->
    <arg>-Asimplebuilder.builderAccess=PACKAGE_PRIVATE</arg>
    
    <!-- Enable collection helpers -->
    <arg>-Asimplebuilder.usingArrayListBuilder=true</arg>
    <arg>-Asimplebuilder.usingHashMapBuilder=true</arg>
</compilerArgs>
```

Override per-class when needed:

```java
@SimpleBuilder
@SimpleBuilder.Options(builderAccess = AccessModifier.PUBLIC)  // Override: make this one public
public class PublicApiDto {
    private String data;
}

@SimpleBuilder  // Uses project defaults: package-private
public class InternalDto {
    private String data;
}
```

## Priority Rules

Configuration resolution follows these priority rules:

1. **Annotation values** (highest priority)
   - Values in `@SimpleBuilder.Options(...)` always win
2. **Compiler options** (medium priority)
   - Used when no annotation value is specified
3. **Default values** (lowest priority)
   - Used when neither annotation nor compiler option is specified

### Example: Priority in Action

```java
// Compiler option: -Asimple.builders.generateFieldSupplier=false
// Global default: true

@SimpleBuilder
@SimpleBuilder.Options(generateFieldSupplier = true)  // Annotation wins!
public class Person {
    private String name;
}

@SimpleBuilder  // Uses compiler option (false)
public class Company {
    private String name;
}

// No compiler option set
@SimpleBuilder  // Uses global default (true)
public class Product {
    private String name;
}
```

## AccessModifier Enum

The `AccessModifier` enum provides type-safe access control:

```java
public enum AccessModifier {
    PUBLIC,           // Accessible from anywhere
    PROTECTED,        // Accessible within same package and subclasses
    PACKAGE_PRIVATE,  // Accessible only within same package (default Java visibility)
    PRIVATE           // Accessible only within same class
}
```

Use in annotations:
```java
@SimpleBuilder.Options(
    builderAccess = AccessModifier.PACKAGE_PRIVATE,
    methodAccess = AccessModifier.PUBLIC
)
```

Or in compiler options:
```
-Asimplebuilder.builderAccess=PACKAGE_PRIVATE
```

## Troubleshooting

### Compiler Options Not Working

1. **Check option names**: Ensure you're using the full option name (e.g., `-Asimplebuilder.generateFieldSupplier`)
2. **Verify processor is running**: Ensure annotation processor is configured correctly
3. **Check IDE configuration**: Some IDEs need special configuration for compiler options
4. **Clean and rebuild**: Run `mvn clean compile` to ensure fresh build

### Annotation Values Not Applied

1. **Verify annotation import**: Import `org.javahelpers.simple.builders.core.annotations.SimpleBuilder`
2. **Check annotation placement**: Use `@SimpleBuilder` on the class, `@SimpleBuilder.Options` on the same class
3. **Verify compilation**: Recompile after changing annotations
4. **Check for syntax errors**: Ensure AccessModifier enum values are correct

### Access Level Issues

1. **Package-private builders**: Ensure DTO and builder are in the same package
2. **Private builders**: May cause issues with reflection-based frameworks
3. **Protected builders**: Only accessible to subclasses
4. **AccessModifier import**: Import `org.javahelpers.simple.builders.core.enums.AccessModifier`

### Template Annotations Not Working

1. **Check @SimpleBuilder.Template**: Ensure template annotation has `@SimpleBuilder.Template`
2. **Verify options parameter**: Template must specify `options = @SimpleBuilder.Options(...)`
3. **Retention and Target**: Add `@Retention(RetentionPolicy.CLASS)` and `@Target(ElementType.TYPE)`
4. **Don't combine**: Don't use `@SimpleBuilder` when using a template annotation

## Best Practices

1. **Use templates for common patterns**: Define reusable templates for your project
2. **Set project-wide defaults**: Configure sensible defaults via compiler options
3. **Override sparingly**: Only override when truly necessary
4. **Document templates**: Add JavaDoc to custom template annotations
5. **Use type-safe enums**: Prefer `AccessModifier` enum over string values
6. **Test configurations**: Verify generated code meets expectations
7. **Consider team preferences**: Choose configurations that work for everyone

## Reference

### All Compiler Options

```
# Field Setter Generation
-Asimplebuilder.generateFieldSupplier=true|false
-Asimplebuilder.generateFieldProvider=true|false
-Asimplebuilder.generateBuilderProvider=true|false

# Conditional Logic
-Asimplebuilder.generateConditionalHelper=true|false

# Access Control
-Asimplebuilder.builderAccess=PUBLIC|PROTECTED|PACKAGE_PRIVATE|PRIVATE
-Asimplebuilder.methodAccess=PUBLIC|PROTECTED|PACKAGE_PRIVATE|PRIVATE

# Collection Helpers
-Asimplebuilder.generateVarArgsHelpers=true|false
-Asimplebuilder.usingArrayListBuilder=true|false
-Asimplebuilder.usingArrayListBuilderWithElementBuilders=true|false
-Asimplebuilder.usingHashSetBuilder=true|false
-Asimplebuilder.usingHashSetBuilderWithElementBuilders=true|false
-Asimplebuilder.usingHashMapBuilder=true|false

# Integration
-Asimplebuilder.generateWithInterface=true|false
```

### Complete Options Example

```java
@SimpleBuilder
@SimpleBuilder.Options(
    // Field Setter Generation
    generateFieldSupplier = true,
    generateFieldProvider = true,
    generateBuilderProvider = true,
    
    // Conditional Logic
    generateConditionalHelper = true,
    
    // Access Control
    builderAccess = AccessModifier.PUBLIC,
    methodAccess = AccessModifier.PUBLIC,
    
    // Collection Helpers
    generateVarArgsHelpers = true,
    usingArrayListBuilder = true,
    usingArrayListBuilderWithElementBuilders = true,
    usingHashSetBuilder = true,
    usingHashSetBuilderWithElementBuilders = true,
    usingHashMapBuilder = true,
    
    // Integration
    generateWithInterface = true
)
public class ExampleDto {
    private String name;
}
```

---

**Last Updated**: 2025-11-01  
**Version**: 0.2.0  
**Related**: [README.md](../README.md)
