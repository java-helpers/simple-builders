# Debug Logging for Simple Builders

The Simple Builders annotation processor supports conditional debug logging that provides detailed information about the builder generation process.

## Logging Levels

- **INFO**: Always visible - Shows success messages for each builder generated
- **WARNING**: Always visible - Shows when a builder cannot be generated (e.g., wrong annotation target, generation errors). Other builders will continue to be generated.
- **ERROR**: Always visible - Shows fatal configuration errors (e.g., unsupported JDK version). Stops compilation completely.
- **DEBUG**: Conditional - Shows detailed tracing of field discovery, method analysis, and code generation steps

## Enabling Debug Logging

Debug logging uses `Diagnostic.Kind.OTHER` but is only activated when explicitly enabled via the compiler argument `-Averbose=true`.

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

## Example Debug Output

When debug logging is enabled, you'll see detailed output with visual separators:

```
[INFO] [DEBUG] ===============================
[INFO] simple-builders: PROCESSING ROUND START
[INFO] [DEBUG] ===============================
[INFO] [DEBUG] simple-builders: Processing round started. Found 3 annotated elements.
[INFO] [DEBUG] ------------------------------------
[INFO] [DEBUG] simple-builders: Processing element: PersonDto
[INFO] [DEBUG] ------------------------------------
[INFO] [DEBUG] Extracting builder definition from: org.example.PersonDto
[INFO] [DEBUG] Builder will be generated as: org.example.PersonDtoBuilder
[INFO] [DEBUG] Analyzing method: setName with 1 parameter(s)
[INFO] [DEBUG]   -> Adding field: name (type: java.lang.String)
[INFO] [DEBUG] Analyzing method: setAge with 1 parameter(s)
[INFO] [DEBUG]   -> Adding field: age (type: int)
[INFO] [DEBUG] Processed 2 possible setters: added 2 fields, skipped 0
[INFO] [DEBUG] Starting code generation for builder: PersonDtoBuilder
[INFO] [DEBUG] Generating 0 constructor fields and 2 setter fields
[INFO] [DEBUG]   Generated 2 methods for field: name
[INFO] [DEBUG]   Generated 2 methods for field: age
[INFO] [DEBUG] Writing builder class to file: org.example.PersonDtoBuilder
[INFO] [DEBUG] Successfully generated builder: PersonDtoBuilder
[INFO] simple-builders: Successfully generated builder for: PersonDto
```

**Note**: Debug messages are prefixed with `[DEBUG]` and use `Diagnostic.Kind.OTHER` which appears as `[INFO]` in Maven output.

## Normal Output (Without Debug)

Without debug logging enabled, you only see the INFO-level messages:

```
[INFO] simple-builders: Successfully generated builder for: PersonDto
[INFO] simple-builders: Successfully generated builder for: OrderDto
[INFO] simple-builders: Successfully generated builder for: CustomerDto
```

## Troubleshooting

If debug logging is not appearing:

1. Verify the compiler plugin configuration includes the debug or verbose argument
2. Check that you're using Maven 3.6+ 
3. Ensure the annotation processor is actually running (check for generated files in `target/generated-sources/annotations`)
4. Try with `-X` flag to see all Maven debug output and verify the compiler arguments are being passed
