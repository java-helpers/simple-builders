# Project: Simple Builders

## Build & Test Commands

- **Run all processor tests:** `mvn -pl processor test`
- **Run a specific test class:** `mvn -pl processor test -Dtest=DefaultValueTest -Dsurefire.failIfNoSpecifiedTests=false`
- **Run a single test method:** `mvn -pl processor test -Dtest=DefaultValueTest#methodName -Dsurefire.failIfNoSpecifiedTests=false`

## Architecture Notes

- Annotation processor that generates fluent builders at compile time
- `processor` module: the annotation processor itself
- `core` module: runtime annotations and utilities (TrackedValue, etc.)
- Type model: `TypeName` and subclasses (`TypeNamePrimitive`, `TypeNameGeneric`, `TypeNameList`, etc.)
- `JavaLangMapper.extractType()` converts `TypeMirror` to `TypeName`, setting flags like `enumType`, `hasEmptyConstructor`, `builderType`
- `FieldAnnotationExtractor.formatDefaultExpression()` formats raw `@Default` string values as Java expressions based on field type
- `ImportCollector` collects imports from field types, method parameters, and code blocks
- Tests use `com.google.testing.compile` with `ProcessorTestUtils.forSource()` to create in-memory source files
- Multiple source files can be compiled together by passing multiple `JavaFileObject` args to `compile()`
- Build method fields are ordered alphabetically by field name, not by declaration order
