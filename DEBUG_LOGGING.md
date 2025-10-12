# Debug Logging for Simple Builders

The Simple Builders annotation processor supports conditional debug logging that provides detailed information about the builder generation process.

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
        <compilerArguments>
          <Averbose>${simplebuilder.verbose}</Averbose>
        </compilerArguments>
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
        <compilerArguments>
          <Averbose>true</Averbose>
        </compilerArguments>
      </configuration>
    </plugin>
  </plugins>
</build>
```

## Example Debug Output

When debug logging is enabled, you'll see detailed output like:

```
[INFO] simple-builders: Processing round started. Found 3 annotated elements.
[INFO] simple-builders: Processing element: PersonDto
[INFO] Extracting builder definition from: org.example.PersonDto
[INFO] Builder will be generated as: org.example.PersonDtoBuilder
[INFO] Found 17 total methods in class hierarchy
[INFO] Analyzing method: setName with 1 parameter(s)
[INFO]   -> Adding field: name (type: java.lang.String)
[INFO] Analyzing method: setAge with 1 parameter(s)
[INFO]   -> Adding field: age (type: int)
[INFO] Found 2 relevant setter methods (filtered from 17 total methods)
[INFO] Processed 2 setters: added 2 fields, skipped 0 duplicates
[INFO] Starting code generation for builder: PersonDtoBuilder
[INFO] Generating methods for 2 setter fields
[INFO]   Generated 2 method(s) for field: name
[INFO]   Generated 2 method(s) for field: age
[INFO] Writing builder class to file: org.example.PersonDtoBuilder
[INFO] Successfully generated builder: PersonDtoBuilder
[INFO] simple-builders: Successfully generated builder for: PersonDto
```

**Note**: Debug messages use `Diagnostic.Kind.OTHER` which appears as `[INFO]` in Maven output.

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
