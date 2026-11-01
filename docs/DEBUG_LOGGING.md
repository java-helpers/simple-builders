# Debug Logging for Simple Builders

The Simple Builders annotation processor supports conditional debug logging that provides detailed information about the builder generation process.

## Logging Levels

- **INFO**: Always visible - Shows success messages for each builder generated. When debug mode is enabled, INFO messages use hierarchical indentation.
- **WARNING**: Always visible - Shows when a builder cannot be generated (e.g., wrong annotation target, generation errors). When debug mode is enabled, WARNING messages use hierarchical indentation.
- **ERROR**: Always visible - Shows fatal configuration errors (e.g., unsupported JDK version). Stops compilation completely.
- **DEBUG**: Conditional - Shows detailed tracing of field discovery, method analysis, and code generation steps with hierarchical indentation

## Enabling Debug Logging

Debug logging uses `Diagnostic.Kind.OTHER` but is only activated when explicitly enabled via the compiler argument `-Averbose=true` or the Maven system property `-Dsimplebuilder.verbose=true`.

When Maven runs javac in-process, `mvn clean compile -Dsimplebuilder.verbose=true` works without an `-Averbose=${simplebuilder.verbose}` mapping in `pom.xml`, and the `-D` value overrides any `-A` mapping from the build file. Gradle also supports `-Dsimplebuilder.verbose=true` (or `systemProp.simplebuilder.verbose=true` in `gradle.properties`) with in-process compilation; use the `-A` mapping for forked compilation and IDE builds. Gradle does not track the property as a task input, so run `clean` or `--rerun-tasks` after changing it.

### Option 1: Via Maven Property (Recommended)

Add a property in your `pom.xml`:

```xml
<properties>
  <simplebuilder.verbose>false</simplebuilder.verbose>
</properties>

<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>3.13.0</version>
      <configuration>
        <compilerArgs>
          <arg>-Averbose=${simplebuilder.verbose}</arg>
        </compilerArgs>
      </configuration>
    </plugin>
  </plugins>
</build>
```

Then enable debug logging from command line:
```bash
mvn clean compile -Dsimplebuilder.verbose=true
```

### Option 2: Static Configuration

Set it permanently in your `pom.xml`:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>3.13.0</version>
      <configuration>
        <compilerArgs>
          <arg>-Averbose=true</arg>
        </compilerArgs>
      </configuration>
    </plugin>
  </plugins>
</build>
```

## Conditional Hierarchical Logging

The Simple Builders processor provides **conditional hierarchical logging** to balance readability in production with detailed debugging when needed:

### **When Debug Mode is DISABLED (Default):**
- INFO and WARNING messages appear flat without indentation
- Suitable for production systems where log noise should be minimized
- Example: `simple-builders: Successfully generated 3 target(s) in this processing round`

### **When Debug Mode is ENABLED (-Averbose=true):**
- Every message carries the `simple-builders:` logger prefix; DEBUG messages are posted on the `[INFO]` channel so they show as `[INFO] [DEBUG]`
- Provides full visibility into the processing hierarchy
- Example: `[INFO]         simple-builders: │  └─ Successfully generated 3 target(s) in this processing round`
- Example: `[WARNING]      simple-builders: │  │  ├─ Builder field conflict: field 'name'...`
- Example: `[INFO] [DEBUG] simple-builders: │  │  │  └─ Processing method: setName`
- All message types align vertically despite different prefix lengths

This approach ensures clean production logs while maintaining full debugging capabilities when needed.

## Example Debug Output

The following is a verbatim excerpt of a real run (`mvn -pl example clean compile -Dsimplebuilder.verbose=true` on the `example` module, slightly truncated where marked with `…`). It follows `SponsorDto`, the smallest annotated class (one field). Every annotated element produces the same step structure — extraction, enhancer pass, finalize, code generation:

```
[INFO] [DEBUG] simple-builders: Starting BuilderProcessor...
[INFO] [DEBUG] simple-builders: Loaded global configuration from compiler arguments: BuilderConfiguration[verbose=ENABLED]
[INFO] [DEBUG] simple-builders: Initializing generator registry
[INFO] [DEBUG] simple-builders: ├─ Loaded 16 method generators and 10 builder enhancers total
[INFO] [DEBUG] simple-builders: └─ Initialized GeneratorRegistry with 16 method generators and 10 builder enhancers
[INFO] simple-builders: PROCESSING ROUND START
[INFO] [DEBUG] simple-builders: Processing round started.
[INFO] [DEBUG] simple-builders: Found 12 annotated elements.
[INFO] [DEBUG] simple-builders: Found 1 type(s) for generation with @SimpleBuilderFor.
[INFO] [DEBUG] simple-builders: Processing element: SponsorDto
[INFO] [DEBUG] simple-builders: ├─ Resolving configuration for element: SponsorDto
[INFO] [DEBUG] simple-builders: │  ├─ Built-in template @SimpleBuilder found in DIRECT scope
[INFO] [DEBUG] simple-builders: │  └─ Resulting configuration resolved: BuilderConfiguration[generateFieldSupplier=ENABLED,…,builderSuffix=Builder,formattingMode=jdt,strict=DISABLED,verbose=ENABLED]
… (the same configuration-resolution steps repeat for each of the 12 elements)
[INFO] [DEBUG] simple-builders: 12 of 12 annotated element(s) are inside the builderGenerationPackages scope.
[INFO] [DEBUG] simple-builders: ├─ Processing element: SponsorDto
[INFO] [DEBUG] simple-builders: │  ├─ Extracting builder definition from: org.javahelpers.simple.builders.example.SponsorDto
[INFO] [DEBUG] simple-builders: │  │  ├─ Builder will be generated as: org.javahelpers.simple.builders.example.SponsorDtoBuilder
[INFO] [DEBUG] simple-builders: │  │  ├─ Analysing setters for finding fields
[INFO] [DEBUG] simple-builders: │  │  │  ├─ Analyzing method: setName(java.lang.String)
[INFO] [DEBUG] simple-builders: │  │  │  │  ├─ Processing method generators
[INFO] [DEBUG] simple-builders: │  │  │  │  │  ├─ Applied: BasicSetterGenerator (priority: 100): +1 methods
[INFO] [DEBUG] simple-builders: │  │  │  │  │  ├─ Applied: StringFormatHelperGenerator (priority: 80): +1 methods
[INFO] [DEBUG] simple-builders: │  │  │  │  │  ├─ Applied: SupplierMethodGenerator (priority: 60): +1 methods
[INFO] [DEBUG] simple-builders: │  │  │  │  │  ├─ Applied: StringValidationGenerator (priority: 60): +1 methods
[INFO] [DEBUG] simple-builders: │  │  │  │  │  ├─ Applied: StringBuilderConsumerGenerator (priority: 45): +1 methods
[INFO] [DEBUG] simple-builders: │  │  │  │  │  ├─ Applied: UpdateHelperGenerator (priority: 31): +1 methods
[INFO] [DEBUG] simple-builders: │  │  │  │  │  └─ Generated 6 methods
[INFO] [DEBUG] simple-builders: │  │  │  │  └─ Adding field: name (type: java.lang.String)
[INFO] [DEBUG] simple-builders: │  │  │  └─ Processed 1 possible setters: added 1 fields, skipped 0
[INFO] [DEBUG] simple-builders: │  │  ├─ Processing class based enhancer
[INFO] [DEBUG] simple-builders: │  │  │  ├─ Applied: GeneratedAnnotationEnhancer (priority: 120): +1 annotations
[INFO] [DEBUG] simple-builders: │  │  │  ├─ Added @BuilderImplementation annotation to builder SponsorDtoBuilder
[INFO] [DEBUG] simple-builders: │  │  │  ├─ Applied: BuilderImplementationAnnotationEnhancer (priority: 115): +1 annotations
[INFO] [DEBUG] simple-builders: │  │  │  ├─ Applied: CoreMethodsEnhancer (priority: 100): +3 methods
[INFO] [DEBUG] simple-builders: │  │  │  ├─ Applied: ConstructorEnhancer (priority: 95): +2 constructors
[INFO] [DEBUG] simple-builders: │  │  │  ├─ Applied: WithInterfaceEnhancer (priority: 95): +1 nested types
[INFO] [DEBUG] simple-builders: │  │  │  ├─ Applied: InterfaceEnhancer (priority: 90): +1 interfaces
[INFO] [DEBUG] simple-builders: │  │  │  ├─ Added conditional methods to builder SponsorDtoBuilder
[INFO] [DEBUG] simple-builders: │  │  │  ├─ Applied: ConditionalEnhancer (priority: 80): +2 methods
[INFO] [DEBUG] simple-builders: │  │  │  ├─ Applied: ClassJavaDocEnhancer (priority: 10): javadoc added
[INFO] [DEBUG] simple-builders: │  │  │  └─ Applied 8 builder enhancers
[INFO] [DEBUG] simple-builders: │  │  ├─ Finalizing builder definition
[INFO] [DEBUG] simple-builders: │  │  │  ├─ Resolving method conflicts
[INFO] [DEBUG] simple-builders: │  │  │  │  └─ Resolved: 11 signatures, 0 conflicts, 0 methods removed
[INFO] [DEBUG] simple-builders: │  │  │  └─ Finalized: 1 class fields, 5 builder-level methods, 2 constructors
[INFO] [DEBUG] simple-builders: │  │  ├─ Builder will be generated as: SponsorDtoBuilder
[INFO] [DEBUG] simple-builders: │  │  └─ Builder definition extracted: SponsorDtoBuilder
… (the same extraction steps repeat for each remaining element)
[INFO] [DEBUG] simple-builders: Generating targets for: SponsorDto
[INFO] [DEBUG] simple-builders: ├─ Code generation for class: SponsorDtoBuilder
[INFO] [DEBUG] simple-builders: │  ├─ JavaClassSource created
[INFO] [DEBUG] simple-builders: │  ├─ Class metadata added
[INFO] [DEBUG] simple-builders: │  ├─ Generating 1 fields
[INFO] [DEBUG] simple-builders: │  │  └─ Fields added: 1 fields
[INFO] [DEBUG] simple-builders: │  ├─ Generating 2 constructors
[INFO] [DEBUG] simple-builders: │  │  └─ Constructors added: 2
[INFO] [DEBUG] simple-builders: │  ├─ Generating 11 method candidates
[INFO] [DEBUG] simple-builders: │  │  ├─ Resolved to 11 methods after conflict resolution
[INFO] [DEBUG] simple-builders: │  │  └─ Methods added: 11
[INFO] [DEBUG] simple-builders: │  ├─ Generating 1 nested type(s)
[INFO] [DEBUG] simple-builders: │  │  ├─ Generated nested type: With
[INFO] [DEBUG] simple-builders: │  │  └─ Nested types added
[INFO] [DEBUG] simple-builders: │  ├─ Class-level annotations added
[INFO] [DEBUG] simple-builders: │  ├─ Creating JavaFileObject: org.javahelpers.simple.builders.example.SponsorDtoBuilder
[INFO] [DEBUG] simple-builders: │  ├─ Writing class to file: …/example/generated-example-builder/org/javahelpers/simple/builders/example/SponsorDtoBuilder.java
[INFO] [DEBUG] simple-builders: │  └─ Successfully generated class: SponsorDtoBuilder
[INFO] [DEBUG] simple-builders: ├─ Generated builder with 1 fields and 11 methods for SponsorDtoBuilder
… (the same code-generation steps repeat for each remaining element; elements whose
    configuration enables the Jackson module additionally emit a registration line:)
[INFO] [DEBUG] simple-builders: ├─ Registered JacksonIntegrationDtoBuilder for Jackson module in package 'org.javahelpers.simple.builders.example'
[INFO]         simple-builders: Successfully generated 12 target(s) in this processing round
[INFO] simple-builders: PROCESSING ROUND START
[INFO] [DEBUG] simple-builders: Processing round started.
[INFO] [DEBUG] simple-builders: No matching elements collected — skipping planning for this round.
… (empty rounds repeat until processing finishes)
[INFO]         simple-builders: Processing OVER. JacksonModuleEnabled: true, Packages: 1
[INFO]         simple-builders: Generating Jackson module in package 'org.javahelpers.simple.builders.example'
[INFO] [DEBUG] simple-builders: Code generation for class: SimpleBuildersJacksonModule
[INFO] [DEBUG] simple-builders: ├─ JavaClassSource created
[INFO] [DEBUG] simple-builders: ├─ Class metadata added
[INFO] [DEBUG] simple-builders: ├─ Generating 1 constructors
[INFO] [DEBUG] simple-builders: │  └─ Constructors added: 1
[INFO] [DEBUG] simple-builders: ├─ Generating 1 nested type(s)
[INFO] [DEBUG] simple-builders: │  ├─ Generated nested type: JacksonIntegrationDtoMixin
[INFO] [DEBUG] simple-builders: │  └─ Nested types added
[WARNING] File for type 'org.javahelpers.simple.builders.example.SimpleBuildersJacksonModule' created in the last round will not be subject to annotation processing.
[INFO] [DEBUG] simple-builders: ├─ Creating JavaFileObject: org.javahelpers.simple.builders.example.SimpleBuildersJacksonModule
[INFO] [DEBUG] simple-builders: ├─ Writing class to file: …/example/generated-example-builder/org/javahelpers/simple/builders/example/SimpleBuildersJacksonModule.java
[INFO] [DEBUG] simple-builders: └─ Successfully generated class: SimpleBuildersJacksonModule
```

Notes on individual lines:

- `Applied: <generator/enhancer> (priority: N): +M <contribution>` — logged **after** each generator/enhancer ran, with what it actually contributed (methods, constructors, nested types, annotations, interfaces, or javadoc). An enhancer that adds nothing measurable prints just `Applied: <name> (priority: N)` with no suffix.
- `Writing class to file:` — the real path of the generated source file, resolved via the filer's `JavaFileObject.toUri()`.
- `Registered <Builder> for Jackson module in package '<pkg>'` — emitted by the Jackson module generator only when the builder actually registered for module generation (i.e. `generateJacksonModule` enabled on that element's configuration and Jackson on the classpath). It is not a generation step of its own.
- Code-generation phases with nothing to emit log nothing — a class without fields, methods or nested types (like `SimpleBuildersJacksonModule`, which only has a constructor and a mixin) produces no `Generating 0 …` / `added: 0` noise.

**Note**: Debug messages are prefixed with `[DEBUG]` and use `Diagnostic.Kind.OTHER` which appears as `[INFO]` in Maven output.

## Normal Output (Without Debug)

Without debug logging enabled, you only see the INFO-level messages:

```
[INFO] simple-builders: PROCESSING ROUND START
[INFO] simple-builders: Successfully generated 3 target(s) in this processing round
```

## Troubleshooting

If debug logging is not appearing:

1. Verify the compiler plugin configuration includes the debug or verbose argument
2. Check that you're using Maven 3.6+ 
3. Ensure the annotation processor is actually running (check for generated files in `target/generated-sources/annotations`)
4. Try with `-X` flag to see all Maven debug output and verify the compiler arguments are being passed
