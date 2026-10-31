package org.javahelpers.codegen.processing;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import org.javahelpers.simple.builders.core.enums.AccessModifier;
import org.javahelpers.simple.builders.core.enums.FormattingMode;
import org.javahelpers.simple.builders.processor.analysis.JavaLangMapper;
import org.javahelpers.simple.builders.processor.model.core.ClassFieldDto;
import org.javahelpers.simple.builders.processor.model.core.GenerationTargetClassDto;
import org.javahelpers.simple.builders.processor.model.method.MethodDto;
import org.javahelpers.simple.builders.processor.model.method.MethodParameterDto;
import org.javahelpers.simple.builders.processor.model.type.TypeName;
import org.javahelpers.simple.builders.processor.processing.AnnotationProcessingContext;
import org.javahelpers.simple.builders.processor.processing.CompilerArgumentsReader;
import org.javahelpers.simple.builders.processor.processing.logging.ProcessingLogger;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.api.Test;

/**
 * A complete codegen processor in minimal form — only the domain members that have no generic
 * default are implemented; collection, round-driving, generator and formatter wiring come from
 * {@link AbstractCodegenProcessor}.
 */
public class MinimalCodegenProcessorTest {

  /** Marks a type for delegate-builder generation in this test. */
  @Target(ElementType.TYPE)
  @Retention(RetentionPolicy.SOURCE)
  public @interface GenerateBuilder {}

  /** The whole per-target configuration a minimal processor needs. */
  record MinimalConfiguration() implements CodegenConfiguration {
    @Override
    public boolean isStrictModeEnabled() {
      return false;
    }

    @Override
    public boolean isVerboseEnabled() {
      return false;
    }
  }

  /** One discovered setter of the target type. */
  record Setter(String name, MethodParameterDto parameter) {}

  /** Analysis result: target type, builder name and the setters to delegate to. */
  record MinimalDefinition(TypeName sourceTypeName, TypeName builderTypeName, List<Setter> setters)
      implements CodegenDefinition {
    @Override
    public TypeName getTypeName() {
      return sourceTypeName;
    }

    @Override
    public TypeName targetTypeName() {
      return builderTypeName;
    }
  }

  /** A processor that generates a builder delegating to the target type's setters. */
  static class MinimalProcessor
      extends AbstractCodegenProcessor<
          AnnotationProcessingContext, MinimalConfiguration, MinimalDefinition> {

    MinimalProcessor() {
      super(
          CodegenProcessorConfiguration.builder()
              .elementCollector(
                  ElementCollector.builder()
                      .selector(
                          selector ->
                              selector.groupName("targets").annotationTypes(GenerateBuilder.class))
                      .build())
              .loggerPrefix("minimal")
              .build());
    }

    @Override
    protected AnnotationProcessingContext createContext(
        ProcessingEnvironment processingEnv,
        CompilerArgumentsReader reader,
        ProcessingLogger logger) {
      return new AnnotationProcessingContext(logger, processingEnv);
    }

    @Override
    protected List<CodegenTask<MinimalConfiguration>> planTasks(
        ElementGroups collected, AnnotationProcessingContext ctx) {
      return collected.byGroup("targets").stream()
          .map(
              element ->
                  new CodegenTask<>(
                      element, new MinimalConfiguration(), element, ctx.getPackageName(element)))
          .toList();
    }

    @Override
    protected List<TypeName> predictTaskResultTypeNames(
        CodegenTask<MinimalConfiguration> task, AnnotationProcessingContext ctx) {
      return List.of(builderName((TypeElement) task.element()));
    }

    @Override
    protected List<MinimalDefinition> analyse(
        CodegenTask<MinimalConfiguration> task, AnnotationProcessingContext ctx) {
      TypeElement target = (TypeElement) task.element();
      List<Setter> setters =
          ctx.getAllMembers(target).stream()
              .filter(ExecutableElement.class::isInstance)
              .map(ExecutableElement.class::cast)
              .filter(method -> method.getSimpleName().toString().startsWith("set"))
              .filter(method -> method.getParameters().size() == 1)
              .map(
                  method ->
                      new Setter(
                          method.getSimpleName().toString(),
                          JavaLangMapper.map2MethodParameter(method.getParameters().get(0), ctx)))
              .toList();
      return List.of(
          new MinimalDefinition(
              JavaLangMapper.mapToTypeName(target, ctx), builderName(target), setters));
    }

    @Override
    protected GenerationTargetClassDto mapToTargetSpecification(
        MinimalDefinition definition,
        MinimalConfiguration configuration,
        AnnotationProcessingContext ctx) {
      GenerationTargetClassDto target = new GenerationTargetClassDto();
      target.setTypeName(definition.builderTypeName());
      target.setClassAccessModifier(AccessModifier.PUBLIC);
      target.setFormattingMode(FormattingMode.NONE);

      ClassFieldDto field = new ClassFieldDto();
      field.setFieldName("target");
      field.setFieldType(definition.sourceTypeName());
      field.setVisibility(AccessModifier.PRIVATE);
      field.setLiteralInitializer("new " + definition.sourceTypeName().getClassName() + "()");
      target.addClassField(field);

      for (Setter setter : definition.setters()) {
        String name = setter.name().substring(3, 4).toLowerCase() + setter.name().substring(4);
        MethodDto method = new MethodDto(name, definition.builderTypeName());
        method.setModifier(AccessModifier.PUBLIC);
        method.addParameter(setter.parameter());
        method.setCode(
            "target."
                + setter.name()
                + "("
                + setter.parameter().getParameterName()
                + ");\nreturn this;");
        target.addMethod(method);
      }
      MethodDto build = new MethodDto("build", definition.sourceTypeName());
      build.setModifier(AccessModifier.PUBLIC);
      build.setCode("return target;");
      target.addMethod(build);
      return target;
    }

    private static TypeName builderName(TypeElement target) {
      String qualified = target.getQualifiedName().toString();
      return new TypeName(
          qualified.substring(0, qualified.lastIndexOf('.')), target.getSimpleName() + "Builder");
    }
  }

  @Test
  void minimalProcessorGeneratesDelegatingBuilder() throws IOException {
    JavaFileObject source =
        JavaFileObjects.forSourceString(
            "test.Person",
            """
            package test;
            import org.javahelpers.codegen.processing.MinimalCodegenProcessorTest.GenerateBuilder;
            @GenerateBuilder
            public class Person {
              private String name;
              public void setName(String name) { this.name = name; }
            }
            """);

    Compilation compilation =
        ProcessorTestUtils.createCompiler(new MinimalProcessor()).compile(source);

    assertTrue(
        compilation.status() == Compilation.Status.SUCCESS, compilation.diagnostics().toString());
    JavaFileObject generated =
        compilation
            .generatedSourceFile("test.PersonBuilder")
            .orElseThrow(() -> new AssertionError("no PersonBuilder generated"));
    String code = generated.getCharContent(true).toString();
    assertTrue(code.contains("public class PersonBuilder"), code);
    assertTrue(code.contains("target.setName(name);"), code);
    assertTrue(code.contains("public Person build()"), code);
    assertTrue(
        compilation.diagnostics().stream().noneMatch(d -> d.getKind() == Diagnostic.Kind.ERROR),
        compilation.diagnostics().toString());
  }
}
