# Regenerating the codegen DTO builders

The twelve `*DtoBuilder.java` files under `codegen-core/src/main/java` are
committed bootstrap sources: the processor itself depends on codegen-core.
Use Java 17 and run these commands from the repository root:

```sh
mvn -pl processor -am install -Dmaven.test.skip=true
mvn -pl processor -Dtest=CodegenDtoBuilderRegenerationTest -Dcodegen.regenerate=true test
mvn -pl codegen-core fmt:format
git diff --exit-code -- 'codegen-core/src/main/java/**/*Builder.java'
```

The explicit regeneration test compiles the model sources and same-package
`@SimpleBuilderFor` holders with the current processor, asserts that exactly twelve
builders were emitted, and replaces only those committed builders. It is disabled
in ordinary builds. After intentional model or generator changes, review and
commit the regenerated files; otherwise the final command must report no changes.
Java CI runs regeneration after installing the reactor and rejects drift.

Collection setters replace contents with a shallow defensive copy into each DTO's
own mutable collection. They reject null collections with `NullPointerException`
before modifying existing state. Empty collections clear the property, and passing
the DTO's own collection is supported. Existing `addX` methods remain available.
