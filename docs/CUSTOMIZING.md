# Customizing Simple Builders

This guide explains how to extend and customize simple-builders by creating custom generators and enhancers.

## Table of Contents

- [Overview](#overview)
- [Generators vs Enhancers](#generators-vs-enhancers)
- [Creating Custom Components](#creating-custom-components)
  - [Custom Method Generators](#custom-method-generators)
  - [Custom Builder Enhancers](#custom-builder-enhancers)
- [ServiceLoader Registration](#serviceloader-registration)
- [Component Override Workflow](#component-override-workflow)
- [Available Default Components](#available-default-components)
- [Best Practices](#best-practices)
- [Examples](#examples)

## Overview

Simple-builders is designed to be extensible through custom generators and enhancers. You can:

- **Override default behavior** by replacing built-in components with custom implementations
- **Add new functionality** by creating generators for specific use cases
- **Integrate with frameworks** by creating enhancers that add annotations or methods

## Generators vs Enhancers

### Method Generators

Method generators create individual methods for builder fields. They implement the `MethodGenerator` interface.

**Use cases**:
- Custom setter methods (e.g., validation setters)
- Domain-specific helper methods (e.g., date parsing setters)
- Integration methods (e.g., with other builders)

### Builder Enhancers

Builder enhancers modify the entire builder class after all methods are generated. They implement the `BuilderEnhancer` interface.

**Use cases**:
- Adding annotations (e.g., Jackson, validation)
- Adding utility methods (e.g., conditional logic)
- Modifying class structure (e.g., implementing interfaces)

## Creating Custom Components

### Custom Method Generator

```java
package com.yourpackage;

import org.javahelpers.simple.builders.processor.generators.MethodGenerator;
import org.javahelpers.simple.builders.processor.dtos.FieldDto;
import org.javahelpers.simple.builders.processor.dtos.MethodDto;
import org.javahelpers.simple.builders.processor.dtos.MethodParameterDto;
import org.javahelpers.simple.builders.processor.dtos.TypeName;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

import java.util.List;

public class CustomValidationGenerator implements MethodGenerator {
    
    @Override
    public boolean appliesTo(FieldDto field, TypeName dtoType, ProcessingContext context) {
        // Only apply to String fields with @Email annotation
        return field.getFieldType().isString() 
            && field.hasAnnotation("javax.validation.constraints.Email");
    }
    
    @Override
    public List<MethodDto> generateMethods(FieldDto field, TypeName builderType, ProcessingContext context) {
        String fieldName = field.getFieldName();
        String methodName = "validated" + capitalize(fieldName);
        
        // Generate validation setter method
        MethodDto method = new MethodDto();
        method.setMethodName(methodName);
        method.setReturnType(builderType);
        
        // Add parameter
        MethodParameterDto parameter = new MethodParameterDto();
        parameter.setParameterName(fieldName);
        parameter.setParameterTypeName(new TypeName("java.lang", "String"));
        method.addParameter(parameter);
        
        method.setCode("""
            if (!isValidEmail(%s)) {
                throw new IllegalArgumentException("Invalid email: " + %s);
            }
            return this.%s(%s);
            """.formatted(fieldName, fieldName, fieldName, fieldName));
        method.addArgument("fieldName", fieldName);
        method.addArgument("fieldName", fieldName);
        method.addArgument("fieldName", fieldName);
        method.addArgument("fieldName", fieldName);
            
        return List.of(method);
    }
    
    @Override
    public int getPriority() {
        return 1000; // Higher than default generators
    }
    
    private boolean isValidEmail(String email) {
        return email != null && email.contains("@");
    }
    
    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
```

### Custom Builder Enhancer

```java
package com.yourpackage;

import org.javahelpers.simple.builders.processor.generators.BuilderEnhancer;
import org.javahelpers.simple.builders.processor.dtos.BuilderDefinitionDto;
import org.javahelpers.simple.builders.processor.dtos.TypeName;
import org.javahelpers.simple.builders.processor.util.ProcessingContext;

public class CustomValidationEnhancer implements BuilderEnhancer {
    
    @Override
    public boolean appliesTo(BuilderDefinitionDto builderDto, TypeName dtoType, ProcessingContext context) {
        // Only apply to DTOs with validation annotations
        return builderDto.getFields().stream()
            .anyMatch(field -> field.hasAnnotation("javax.validation.constraints.*"));
    }
    
    @Override
    public void enhanceBuilder(BuilderDefinitionDto builderDto, ProcessingContext context) {
        // Add validation method to builder
        MethodDto validateMethod = createValidateMethod(builderDto);
        builderDto.addCoreMethod(validateMethodMethod);
        
        // Add @Valid annotation if available
        if (isValidationAvailable(context)) {
            AnnotationDto validAnnotation = new AnnotationDto();
            validAnnotation.setAnnotationType(new TypeName("javax.validation", "Valid"));
            builderDto.addClassAnnotation(validAnnotation);
        }
        
        context.debug("Added validation enhancements to builder %s", 
            builderDto.getBuilderTypeName().getClassName());
    }
    
    @Override
    public int getPriority() {
        return 500; // Medium priority
    }
    
    private MethodDto createValidateMethod(BuilderDefinitionDto builderDto) {
        // Implementation for creating validate() method
        // ...
    }
    
    private boolean isValidationAvailable(ProcessingContext context) {
        return context.getTypeElement("javax.validation.Valid") != null;
    }
}
```

## ServiceLoader Registration

To make your custom components discoverable, create service files in `META-INF/services/`:

### Method Generator Registration

Create file: `META-INF/services/org.javahelpers.simple.builders.processor.generators.MethodGenerator`

```
com.yourpackage.CustomValidationGenerator
com.yourpackage.AnotherCustomGenerator
```

### Builder Enhancer Registration

Create file: `META-INF/services/org.javahelpers.simple.builders.processor.generators.BuilderEnhancer`

```
com.yourpackage.CustomValidationEnhancer
com.yourpackage.AnotherCustomEnhancer
```

## Component Override Workflow

To override default components:

1. **Create Custom Component**: Implement `MethodGenerator` or `BuilderEnhancer`
2. **Register via ServiceLoader**: Add to appropriate service file
3. **Deactivate Default**: Use compiler option to disable the default component
4. **Configure Build**: Add the compiler option to your build configuration

### Example: Override Conditional Logic

1. **Create Custom Enhancer** (see example above)
2. **Register in ServiceLoader**:
   ```
   com.yourpackage.CustomConditionalEnhancer
   ```
3. **Deactivate Default**:
   ```bash
   -Asimplebuilder.deactivateGenerationComponents=ConditionalEnhancer
   ```

### Maven Configuration

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <version>3.11.0</version>
    <configuration>
        <compilerArgs>
            <arg>-Asimplebuilder.deactivateGenerationComponents=ConditionalEnhancer</arg>
        </compilerArgs>
    </configuration>
</plugin>
```

### Gradle Configuration

```gradle
compileJava {
    options.compilerArgs += ["-Asimplebuilder.deactivateGenerationComponents=ConditionalEnhancer"]
}
```

## Available Default Components

### Method Generators

| Generator | Purpose | Priority |
|-----------|---------|----------|
| `BasicSetterGenerator` | Basic field setters | 100 |
| `SupplierMethodGenerator` | Supplier-based setters | 80 |
| `FieldConsumerGenerator` | Consumer-based setters | 80 |
| `BuilderConsumerGenerator` | Builder consumer methods | 80 |
| `MapConsumerGenerator` | Map consumer methods | 80 |
| `ListConsumerGenerator` | List consumer methods | 80 |
| `SetConsumerGenerator` | Set consumer methods | 80 |
| `StringFormatHelperGenerator` | String.format helpers | 50 |
| `VarArgsHelperGenerator` | Varargs helpers | 50 |
| `AddToCollectionGenerator` | add2FieldName methods for List/Set | 30 |
| `ArrayConversionGenerator` | Array-from-List conversion methods | 35 |
| `ArrayBuilderConsumerGenerator` | ArrayListBuilder consumer methods for arrays | 25 |

### Builder Enhancers

| Enhancer | Purpose | Priority |
|----------|---------|----------|
| `ConditionalEnhancer` | Conditional logic methods | 100 |
| `GeneratedAnnotationEnhancer` | @Generated annotation | 10 |
| `JacksonAnnotationEnhancer` | Jackson annotations | 100 |
| `ClassJavaDocEnhancer` | Class-level JavaDoc | 10 |

## Best Practices

### Priority Management

- **Higher priority** = executed first
- Use priority `> 100` to override defaults
- Use priority `< 10` for utility enhancers

### Error Handling

```java
@Override
public List<MethodDto> generateMethods(FieldDto field, TypeName builderType, ProcessingContext context) {
    try {
        // Your generation logic
        return methods;
    } catch (Exception e) {
        context.error("Failed to generate method for field %s: %s", field.getFieldName(), e.getMessage());
        return List.of(); // Return empty list on error
    }
}
```

### Conditional Application

```java
@Override
public boolean appliesTo(FieldDto field, TypeName dtoType, ProcessingContext context) {
    // Check if your component should apply
    if (!shouldApply(field, context)) {
        return false;
    }
    
    // Check if a higher priority component already handles this
    return !isAlreadyHandled(field, context);
}
```

### Testing Custom Components

When testing custom components, you have several approaches depending on your testing setup:

#### Option 1: Integration Testing with Maven/Gradle

Create a test project and verify the generated code manually:

```bash
# 1. Create a test DTO with your custom annotations
# 2. Compile with your custom component and deactivated default
mvn compile -Asimplebuilder.deactivateGenerationComponents=BasicSetterGenerator

# 3. Check the generated builder code
cat target/generated-sources/annotations/your/package/YourDtoBuilder.java
```

#### Option 2: Unit Testing the Component Logic

Test your component logic directly:

```java
@Test
void customGeneratorAppliesToTest() {
    CustomValidationGenerator generator = new CustomValidationGenerator();
    
    // Mock field with @Email annotation
    FieldDto emailField = createMockField("email", String.class, List.of("javax.validation.constraints.Email"));
    FieldDto nameField = createMockField("name", String.class, List.of());
    
    // Test appliesTo logic
    assertTrue(generator.appliesTo(emailField, dtoType, context));
    assertFalse(generator.appliesTo(nameField, dtoType, context));
}

@Test
void customGeneratorMethodGenerationTest() {
    CustomValidationGenerator generator = new CustomValidationGenerator();
    
    // Test method generation
    List<MethodDto> methods = generator.generateMethods(emailField, builderType, context);
    
    assertEquals(1, methods.size());
    assertEquals("validatedEmail", methods.get(0).getMethodName());
    assertTrue(methods.get(0).getCode().contains("isValidEmail"));
}
```

#### Option 3: Using google-compile-testing (Advanced)

If you want to use the same testing framework as simple-builders:

```xml
<!-- Add to your test pom.xml -->
<dependency>
    <groupId>com.google.testing.compile</groupId>
    <artifactId>compile-testing</artifactId>
    <version>0.21.0</version>
    <scope>test</scope>
</dependency>
```

```java
@Test
void customGeneratorIntegrationTest() {
    Compilation compilation = javac()
        .withProcessors(new BuilderProcessor())
        .withOptions("-Asimplebuilder.deactivateGenerationComponents=BasicSetterGenerator")
        .compile(forSourceString("test.TestDto", TEST_DTO));
    
    assertThat(compilation).succeeded();
    
    // Load and verify generated code
    JavaFileObject generatedBuilder = compilation.generatedSource("test.TestDtoBuilder");
    String content = generatedBuilder.getSourceContents();
    
    // Verify your custom methods are present
    assertThat(content).contains("validatedEmail(");
    assertThat(content).contains("isValidEmail");
}
```

#### Manual Verification Steps

1. **Compile your project** with the custom component
2. **Check generated builder code** in `target/generated-sources/annotations/`
3. **Run integration tests** to ensure the builder works correctly
4. **Verify edge cases** by testing with different field types and annotations

## Examples

### Example 1: Custom Date Parser Generator

Creates setters that parse string dates into `LocalDate`:

```java
public class DateParserGenerator implements MethodGenerator {
    
    @Override
    public boolean appliesTo(FieldDto field, TypeName dtoType, ProcessingContext context) {
        return field.getFieldType().isClass("java.time.LocalDate")
            && field.hasAnnotation("com.example.ParseFromString");
    }
    
    @Override
    public List<MethodDto> generateMethods(FieldDto field, TypeName builderType, ProcessingContext context) {
        String fieldName = field.getFieldName();
        String methodName = fieldName + "FromString";
        
        MethodDto method = new MethodDto();
        method.setMethodName(methodName);
        method.setReturnType(builderType);
        
        // Add parameter
        String parameterName = fieldName + "String";
        MethodParameterDto parameter = new MethodParameterDto();
        parameter.setParameterName(parameterName);
        parameter.setParameterTypeName(new TypeName("java.lang", "String"));
        method.addParameter(parameter);
        
        method.setCode("""
            try {
                return this.%s(LocalDate.parse(%s));
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date format: " + %s, e);
            }
            """.formatted(fieldName, parameterName, parameterName));
            
        return List.of(method);
    }
    
    @Override
    public int getPriority() {
        return 200; // Higher than basic setters
    }
}
```

### Example 2: Custom Builder Factory Enhancer

Adds static factory methods to builders:

```java
public class BuilderFactoryEnhancer implements BuilderEnhancer {
    
    @Override
    public boolean appliesTo(BuilderDefinitionDto builderDto, TypeName dtoType, ProcessingContext context) {
        return dtoType.hasAnnotation("com.example.BuilderFactory");
    }
    
    @Override
    public void enhanceBuilder(BuilderDefinitionDto builderDto, ProcessingContext context) {
        TypeName builderType = builderDto.getBuilderTypeName();
        TypeName dtoType = builderDto.getTargetTypeName();
        
        // Add static factory method
        MethodDto factoryMethod = new MethodDto();
        factoryMethod.setMethodName("from");
        factoryMethod.setReturnType(builderType);
        factoryMethod.setStatic(true);
        
        // Add parameter
        MethodParameterDto parameter = new MethodParameterDto();
        parameter.setParameterName("template");
        parameter.setParameterTypeName(dtoType);
        factoryMethod.addParameter(parameter);
        
        factoryMethod.setCode("return new %s(template);");
        factoryMethod.addArgument("builderType", builderType.getClassName());
            
        builderDto.addStaticMethod(factoryMethod);
        
        context.debug("Added factory method to builder %s", builderType.getClassName());
    }
    
    @Override
    public int getPriority() {
        return 50; // Low priority, runs after most enhancements
    }
}
```

### Example 3: Custom Validation Integration

Integrates with Bean Validation API:

```java
public class BeanValidationEnhancer implements BuilderEnhancer {
    
    @Override
    public boolean appliesTo(BuilderDefinitionDto builderDto, TypeName dtoType, ProcessingContext context) {
        return isBeanValidationAvailable(context);
    }
    
    @Override
    public void enhanceBuilder(BuilderDefinitionDto builderDto, ProcessingContext context) {
        // Add validate() method
        MethodDto validateMethod = createValidateMethod(builderDto);
        builderDto.addCoreMethod(validateMethod);
        
        // Add @Validated annotation
        AnnotationDto validatedAnnotation = new AnnotationDto();
        validatedAnnotation.setAnnotationType(new TypeName("org.springframework.validation.annotation", "Validated"));
        builderDto.addClassAnnotation(validatedAnnotation);
    }
    
    @Override
    public int getPriority() {
        return 200; // High priority for validation
    }
    
    private boolean isBeanValidationAvailable(ProcessingContext context) {
        return context.getTypeElement("javax.validation.Validator") != null;
    }
}
```

## Integration with Frameworks

### Spring Integration

```java
public class SpringBuilderEnhancer implements BuilderEnhancer {
    
    @Override
    public boolean appliesTo(BuilderDefinitionDto builderDto, TypeName dtoType, ProcessingContext context) {
        return isSpringAvailable(context);
    }
    
    @Override
    public void enhanceBuilder(BuilderDefinitionDto builderDto, ProcessingContext context) {
        // Add @Component annotation
        AnnotationDto componentAnnotation = new AnnotationDto();
        componentAnnotation.setAnnotationType(new TypeName("org.springframework.stereotype", "Component"));
        componentAnnotation.addMember("value", "\"" + builderDto.getTargetTypeName().getClassName() + "Builder\"");
        builderDto.addClassAnnotation(componentAnnotation);
    }
    
    private boolean isSpringAvailable(ProcessingContext context) {
        return context.getTypeElement("org.springframework.stereotype.Component") != null;
    }
}
```

For more examples, see the source code of the built-in generators and enhancers.
