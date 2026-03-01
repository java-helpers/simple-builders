# Example Custom Generator

This directory contains a complete example of how to create and use custom generators with the simple-builders framework.

## Overview

The example custom generator demonstrates:

1. **Creating a custom method generator** - `StringValidationGenerator`
2. **Service registration** - Using Java ServiceLoader mechanism
3. **Integration with existing DTOs** - Applied to `PersonDto` and `SponsorDto`
4. **Annotation processor configuration** - Proper setup for custom generators

## Project Structure

```
example-custom-generator/
├── pom.xml                                    # Maven configuration
├── src/main/java/org/javahelpers/simple/builders/example/custom/
│   └── StringValidationGenerator.java         # Custom generator implementation
└── src/main/resources/META-INF/services/
    └── org.javahelpers.simple.builders.processor.generators.Generator  # Service registration
```

## The Custom Generator

### StringValidationGenerator

This generator adds validation methods for all `String` fields in DTOs. For each String field named `fieldName`, it generates a method called `validateFieldName()` that:

- Checks if the field is set (`isSet()`)
- Validates that the value is not null or empty after trimming
- Throws `IllegalArgumentException` if validation fails
- Returns the builder instance for method chaining

#### Example Generated Method

For a field named `name`, the generator creates:

```java
/**
 * Validates that the name field is not null or empty.
 *
 * @return this builder instance for chaining
 * @throws IllegalArgumentException if name is null or empty
 */
public PersonDtoBuilder validateName() {
    if (!name.isSet() || name.value().trim().isEmpty()) {
        throw new IllegalArgumentException("Name cannot be null or empty");
    }
    return this;
}
```

## How It Works

### 1. Generator Implementation

The custom generator implements the `MethodGenerator` interface:

```java
public class StringValidationGenerator implements MethodGenerator {
    
    @Override
    public boolean appliesTo(FieldDto field, TypeName dtoType, ProcessingContext context) {
        // Only apply to String fields
        return "java.lang.String".equals(field.getFieldType().getFullQualifiedName());
    }
    
    @Override
    public List<MethodDto> generateMethods(FieldDto field, TypeName builderType, ProcessingContext context) {
        // Generate validation method for the field
        // ...
    }
}
```

### 2. Service Registration

The generator is registered using the Java ServiceLoader mechanism by creating the file:

`src/main/resources/META-INF/services/org.javahelpers.simple.builders.processor.generators.Generator`

With the content:
```
org.javahelpers.simple.builders.example.custom.StringValidationGenerator
```

### 3. Integration

To use the custom generator in your project:

1. Add the example-custom-generator to your annotation processor path:

```xml
<annotationProcessorPaths>
    <path>
        <groupId>io.github.java-helpers</groupId>
        <artifactId>simple-builders-processor</artifactId>
        <version>${project.version}</version>
    </path>
    <path>
        <groupId>org.javahelpers.simple.builders.example</groupId>
        <artifactId>example-custom-generator</artifactId>
        <version>${project.version}</version>
    </path>
</annotationProcessorPaths>
```

**Note:** The custom generator only needs to be in the `annotationProcessorPaths`, not in the regular dependencies, since it's only used during compilation. The generated builders only need `simple-builders-core` as a regular dependency.

## Usage Example

Once the custom generator is integrated, you can use the validation methods in your builders:

```java
// Valid usage
PersonDto person = PersonDtoBuilder.create()
    .name("John Doe")
    .validateName()  // Custom validation method
    .build();

// Invalid usage - throws IllegalArgumentException
PersonDto invalidPerson = PersonDtoBuilder.create()
    .name("")  // Empty string
    .validateName()  // Throws exception
    .build();
```

## Build and Test

```bash
# Build the example custom generator module
mvn clean install -pl example-custom-generator

# Test with the example module (includes custom generator)
mvn clean test -pl example -am
```

This example demonstrates the power and flexibility of the simple-builders framework's extension mechanism, allowing you to add custom functionality that integrates seamlessly with the generated builders.
