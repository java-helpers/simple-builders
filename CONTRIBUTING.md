# Contributing to Simple Builders

Thank you for your interest in contributing to Simple Builders! This document provides guidelines and instructions for developing and testing the project.

## Table of Contents
- [Development Setup](#development-setup)
- [Project Structure](#project-structure)
- [Building and Testing](#building-and-testing)
- [Code Style](#code-style)
- [Submitting Changes](#submitting-changes)

## Development Setup

### Prerequisites

- **Java 17+**: Required for development
- **Maven 3.8+**: Build tool
- **Git**: Version control

### Clone and Build

```bash
git clone https://github.com/java-helpers/simple-builders.git
cd simple-builders
mvn clean install
```

## Project Structure

The project is organized as a multi-module Maven project:

```
simple-builders/
├── core/              # Core annotations and runtime utilities
├── processor/         # Annotation processor (compile-time code generation)
├── example/          # Example usage and integration tests
└── pom.xml           # Parent POM
```

### Module Dependencies

**Important**: The `example` module depends on the `processor` module being installed in your local Maven repository. This is because:
1. The annotation processor must be available at compile-time
2. The example uses `@SimpleBuilder` annotations that trigger code generation
3. Tests in example validate the generated builders

## Building and Testing

### Understanding Module Dependencies

Due to the annotation processor architecture, you must follow specific test strategies based on what you're modifying:

### Test Strategies

#### 1. When Modifying Processor Code

If you're changing code in the `processor` module:

```bash
# Test only the processor
mvn test -pl processor

# With annotation processor verbose output (shows detailed field/method analysis)
mvn test -pl processor -Dsimplebuilder.verbose=true
```

#### 2. When Modifying Example Code

If you're changing code in the `example` module, you **must** install the processor first:

```bash
# Install processor (skip its tests for speed)
mvn install -pl processor -DskipTests

# Then test example
mvn test -pl example
```

#### 3. When Modifying Both Modules

For changes affecting both processor and example:

```bash
# Option A: Use reactor with -am (also-make) flag
mvn test -pl processor,example -am

# Option B: Full clean install (safest)
mvn clean install
```

#### 4. Full Validation Before Committing

Always run a full build with all tests before committing:

```bash
# Clean build with all tests
mvn clean test

# Or full install
mvn clean install
```

### Common Maven Commands

```bash
# Clean everything
mvn clean

# Compile without tests
mvn compile -DskipTests

# Install to local repository without tests
mvn install -DskipTests

# Run tests for specific modules
mvn test -pl processor,example

# Run a specific test class
mvn test -Dtest=BuilderProcessorTest -pl processor

# Run with Maven debug output (shows Maven internals, not processor details)
mvn test -X -pl processor
```

### Debug Logging for Annotation Processor

The annotation processor has its own verbose logging that shows detailed information about field discovery, method analysis, and code generation. This is **different** from Maven's `-X` debug flag.

Both `processor` and `example` modules now support the `simplebuilder.verbose` property:

```bash
# Enable verbose output for processor tests
mvn test -pl processor -Dsimplebuilder.verbose=true

# Enable verbose output for example compilation
mvn compile -pl example -Dsimplebuilder.verbose=true

# Enable for all modules
mvn test -Dsimplebuilder.verbose=true
```

**How it works**: 
1. The `ProcessorTestUtils.createCompiler()` utility automatically checks for the `simplebuilder.verbose` system property and applies `-Averbose=true` to all test compilations when enabled.
2. The `printDiagnosticsOnVerbose()` helper prints the processor's detailed debug output to the console, making it visible in test output and CI logs.
3. This is especially useful when debugging failing tests - you'll see exactly what the processor is doing.

**Example verbose output**:
```
========== Compilation Diagnostics ==========
--- NOTES ---
[DEBUG] simple-builders: Processing element: Project
[DEBUG] Extracting builder definition from: test.Project
[DEBUG] Analyzing method: setName with 1 parameter(s)
[DEBUG]   -> Adding field: name (type: java.lang.String)
[DEBUG] Generated 4 methods for field: name
[DEBUG] Successfully generated builder: ProjectBuilder
=============================================
```

For complete documentation on debug logging and configuration, see [DEBUG_LOGGING.md](DEBUG_LOGGING.md).

### Troubleshooting Build Issues

#### Maven Compilation Cache Issues

If you encounter strange compilation errors in the `example` module:

1. **Clean the affected module**:
   ```bash
   mvn clean -pl example
   ```

2. **Reinstall dependencies**:
   ```bash
   mvn clean install -pl processor -DskipTests
   mvn compile -pl example
   ```

3. **Full clean (nuclear option)**:
   ```bash
   mvn clean
   mvn install
   ```

#### Test Compilation Failures

If test classes can't find generated builders:

```bash
# Ensure processor is installed
mvn install -pl processor -DskipTests

# Clean and rebuild example
mvn clean compile -pl example
mvn test -pl example
```

## Code Style

### Formatting

The project uses [google-java-format](https://github.com/google/google-java-format) for consistent code formatting.

**Automatic formatting** is applied during build via the `fmt-maven-plugin`:

```bash
# Format code automatically
mvn fmt:format

# Check formatting without modifying files
mvn fmt:check
```

### Code Quality

- **SonarLint**: We use SonarQube rules. Install the SonarLint IDE plugin for real-time feedback.
- **Test Coverage**: Aim for high test coverage for new features.
- **JavaDoc**: Public APIs should have comprehensive JavaDoc comments.

### Naming Conventions

- **Classes**: `PascalCase` (e.g., `BuilderProcessor`)
- **Methods**: `camelCase` (e.g., `generateBuilder`)
- **Constants**: `UPPER_SNAKE_CASE` (e.g., `DEFAULT_TIMEOUT`)
- **Packages**: lowercase (e.g., `org.javahelpers.simple.builders`)

## Submitting Changes

### Before Submitting

1. **Run all tests**:
   ```bash
   mvn clean test
   ```

2. **Check code formatting**:
   ```bash
   mvn fmt:check
   ```

3. **Update documentation** if needed

4. **Write tests** for new features

### Pull Request Process

1. **Fork** the repository
2. **Create a feature branch** from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```

3. **Make your changes** following the code style guidelines

4. **Commit** with clear, descriptive messages:
   ```bash
   git commit -m "Add feature: description of feature"
   ```

5. **Push** to your fork:
   ```bash
   git push origin feature/your-feature-name
   ```

6. **Open a Pull Request** against the `main` branch

### Pull Request Guidelines

- **Clear description**: Explain what your PR does and why
- **Reference issues**: Link related issues (e.g., "Fixes #123")
- **Keep it focused**: One feature or fix per PR
- **Include tests**: Add tests for new functionality
- **Update docs**: Update README.md or other docs if needed

## Testing Philosophy

### Test Preferences

1. **Explicit assertions**: Prefer explicit expected string literals over building assertion strings from variables
2. **Readable tests**: Tests should be self-documenting and easy to understand
3. **Comprehensive coverage**: Test edge cases, error conditions, and happy paths

### Running Specific Tests

```bash
# Run a single test method
mvn test -Dtest=BuilderProcessorTest#shouldGenerateBasicBuilder -pl processor

# Run all tests in a class
mvn test -Dtest=BuilderProcessorTest -pl processor

# Run tests matching a pattern
mvn test -Dtest=*ProcessorTest -pl processor
```

## Questions?

If you have questions or need help:
- Open an [issue](https://github.com/java-helpers/simple-builders/issues)
- Check existing [discussions](https://github.com/java-helpers/simple-builders/discussions)

Thank you for contributing to Simple Builders! 🎉
