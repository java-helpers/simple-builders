/*
 * MIT License
 *
 * Copyright (c) 2025-2026 Andreas Igel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.javahelpers.codegen.roaster;

import static org.javahelpers.codegen.testing.ProcessorAsserts.assertNormalizedEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import java.util.List;
import org.javahelpers.codegen.enums.AccessModifier;
import org.javahelpers.codegen.enums.FormattingMode;
import org.javahelpers.codegen.model.annotation.AnnotationDto;
import org.javahelpers.codegen.model.annotation.InterfaceName;
import org.javahelpers.codegen.model.core.ClassFieldDto;
import org.javahelpers.codegen.model.core.GenerationTargetClassDto;
import org.javahelpers.codegen.model.javadoc.JavadocCodeBlockDto;
import org.javahelpers.codegen.model.javadoc.JavadocDto;
import org.javahelpers.codegen.model.method.ConstructorDto;
import org.javahelpers.codegen.model.method.MethodDto;
import org.javahelpers.codegen.model.method.MethodParameterDto;
import org.javahelpers.codegen.model.type.GenericParameterDto;
import org.javahelpers.codegen.model.type.NestedTypeDto;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.codegen.model.type.TypeNameArray;
import org.javahelpers.codegen.model.type.TypeNameGeneric;
import org.javahelpers.codegen.model.type.TypeNameNested;
import org.javahelpers.codegen.model.type.TypeNamePrimitive;
import org.javahelpers.codegen.model.type.TypeNameVariable;
import org.javahelpers.codegen.testing.CodegenTestUtils;
import org.javahelpers.codegen.testing.TestingGenerationEnvironment;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link RoasterCodeGenerator} driving {@link GenerationTargetClassDto}s directly — no
 * simple-builder types involved, the {@code GenerationEnvironment} comes from shared test support
 * ({@link TestingGenerationEnvironment}) and the emitted source is asserted as a whole text block.
 */
class RoasterCodeGeneratorTest {

  @Test
  void preservesGenericOwnersAndEarlierAnnotationsWhenParsingParameters() throws Exception {
    GenerationTargetClassDto classDef = new GenerationTargetClassDto();
    classDef.setTypeName(new TypeName("test", "Target"));
    classDef.setClassAccessModifier(AccessModifier.PUBLIC);
    TypeName owner = new TypeNameGeneric(new TypeName("other", "Outer"), TypeName.of(String.class));
    TypeName member =
        new TypeNameGeneric(new TypeNameNested(owner, "Inner"), TypeName.of(Integer.class));
    MethodDto method = new MethodDto("accept", null);
    method.setModifier(AccessModifier.PUBLIC);
    MethodParameterDto first = new MethodParameterDto();
    first.setParameterName("first");
    first.setParameterTypeName(TypeName.of(String.class));
    AnnotationDto deprecated = new AnnotationDto();
    deprecated.setAnnotationType(TypeName.of(Deprecated.class));
    first.addAnnotation(deprecated);
    method.addParameter(first);
    MethodParameterDto second = new MethodParameterDto();
    second.setParameterName("second");
    second.setParameterTypeName(member);
    method.addParameter(second);
    MethodParameterDto rest = new MethodParameterDto();
    rest.setParameterName("rest");
    rest.setParameterTypeName(new TypeNameArray(member));
    method.addParameter(rest);
    classDef.addMethod(method);
    assertGeneratedSource(
        """
        package test;
        import other.Outer.Inner;
        public class Target {
          public void accept(@Deprecated String first,
              other.Outer<java.lang.String>.Inner<Integer> second,
              other.Outer<java.lang.String>.Inner<Integer>... rest) {
          }
        }
        """,
        classDef);
  }

  @Test
  void preservesLeadingFieldTypeAnnotation() throws Exception {
    GenerationTargetClassDto classDef = annotatedClassDefinition();
    ClassFieldDto field = new ClassFieldDto();
    field.setFieldName("value");
    field.setFieldType(stringTypeNullableAnnotated());
    field.setVisibility(AccessModifier.PRIVATE);
    classDef.addClassField(field);

    assertAnnotatedSource(
        """
        package test;
        import test.anno.Nullable;
        public class AnnotatedTypes {
          @Nullable
          private String value;
        }
        """,
        classDef);
  }

  @Test
  void preservesLeadingReturnTypeAnnotation() throws Exception {
    GenerationTargetClassDto classDef = annotatedClassDefinition();
    MethodDto method = new MethodDto("value", stringTypeNullableAnnotated());
    method.setModifier(AccessModifier.PUBLIC);
    method.setCode("return null;");
    AnnotationDto deprecated = new AnnotationDto();
    deprecated.setAnnotationType(TypeName.of(Deprecated.class));
    method.addAnnotation(deprecated);
    classDef.addMethod(method);

    assertAnnotatedSource(
        """
        package test;
        import test.anno.Nullable;
        public class AnnotatedTypes {
          @Nullable
          @Deprecated
          public String value() {
            return null;
          }
        }
        """,
        classDef);
  }

  @Test
  void preservesAnnotatedGenericReturnType() throws Exception {
    GenerationTargetClassDto classDef = annotatedClassDefinition();
    TypeName listOfStringsAnnotated =
        new TypeNameGeneric(TypeName.of(List.class), stringTypeNullableAnnotated());
    MethodDto method = new MethodDto("values", listOfStringsAnnotated);
    method.setModifier(AccessModifier.PUBLIC);
    method.setCode("return null;");
    classDef.addMethod(method);

    NestedTypeDto nested = new NestedTypeDto();
    nested.setTypeName("Nested");
    nested.setKind(NestedTypeDto.NestedTypeKind.INTERFACE);
    nested.setVisibility(AccessModifier.PUBLIC);
    TypeName listOfListsAnnotated =
        new TypeNameGeneric(TypeName.of(List.class), listOfStringsAnnotated);
    MethodDto nestedMethod = new MethodDto("values", listOfListsAnnotated);
    nestedMethod.setCode("return null;");
    nested.addMethod(nestedMethod);
    classDef.addNestedType(nested);

    assertAnnotatedSource(
        """
        package test;
        import java.util.List;
        import test.anno.Nullable;
        public class AnnotatedTypes {
          public List<@Nullable String> values() {
            return null;
          }
          public interface Nested {
            default List<List<@Nullable String>> values() {
              return null;
            }
          }
        }
        """,
        classDef);
  }

  @Test
  void generatesDelegatingBuilderFromDto() throws Exception {
    assertGeneratedSource(
        """
        package test;
        public class PersonBuilder {

          private Person target = new Person();

          public PersonBuilder name(String name) {
            target.setName(name);
            return this;
          }

          public Person build() {
            return target;
          }
        }""",
        personBuilderDefinition());
  }

  @Test
  void writesFileUnderQualifiedName() throws Exception {
    GenerationTargetClassDto empty = new GenerationTargetClassDto();
    empty.setTypeName(new TypeName("com.example", "EmptyBuilder"));
    empty.setClassAccessModifier(AccessModifier.PUBLIC);
    empty.setFormattingMode(FormattingMode.NONE);

    assertEquals("mem:///com/example/EmptyBuilder.java", generate(empty).writtenUri().toString());
  }

  @Test
  void qualifiesCollidingDateTypeNames() throws Exception {
    GenerationTargetClassDto classDef = new GenerationTargetClassDto();
    classDef.setTypeName(new TypeName("test", "DateBuilder"));
    classDef.setClassAccessModifier(AccessModifier.PUBLIC);
    classDef.setFormattingMode(FormattingMode.NONE);

    ClassFieldDto utilDate = new ClassFieldDto();
    utilDate.setFieldName("utilDate");
    utilDate.setFieldType(new TypeName("java.util", "Date"));
    classDef.addClassField(utilDate);

    ClassFieldDto sqlDate = new ClassFieldDto();
    sqlDate.setFieldName("sqlDate");
    sqlDate.setFieldType(new TypeName("java.sql", "Date"));
    classDef.addClassField(sqlDate);

    assertGeneratedSource(
        """
        package test;
        import java.sql.Date;
        public class DateBuilder {
          private java.util.Date utilDate;
          private Date sqlDate;
        }""",
        classDef);
  }

  @Test
  void qualifiesCollidingGenericListTypeNames() throws Exception {
    GenerationTargetClassDto classDef = new GenerationTargetClassDto();
    classDef.setTypeName(new TypeName("test", "ListBuilder"));
    classDef.setClassAccessModifier(AccessModifier.PUBLIC);
    classDef.setFormattingMode(FormattingMode.NONE);

    ClassFieldDto utilList = new ClassFieldDto();
    utilList.setFieldName("utilList");
    utilList.setFieldType(new TypeNameGeneric(java.util.List.class, String.class));
    classDef.addClassField(utilList);

    ClassFieldDto awtList = new ClassFieldDto();
    awtList.setFieldName("awtList");
    awtList.setFieldType(new TypeName("java.awt", "List"));
    classDef.addClassField(awtList);

    assertGeneratedSource(
        """
        package test;
        import java.awt.List;
        public class ListBuilder {
          private java.util.List<String> utilList;
          private List awtList;
        }""",
        classDef);
  }

  @Test
  void qualifiesCollidingAnnotationAndInterfaceSimpleNames() throws Exception {
    GenerationTargetClassDto classDef = new GenerationTargetClassDto();
    classDef.setTypeName(new TypeName("test", "AmbiguousBuilder"));
    classDef.setClassAccessModifier(AccessModifier.PUBLIC);
    classDef.setFormattingMode(FormattingMode.NONE);

    // Two distinct NonNull annotations on different declaration sites: the one that wins the
    // import renders simple, the other stays qualified.
    AnnotationDto classAnnotation = new AnnotationDto();
    classAnnotation.setAnnotationType(new TypeName("org.a", "NonNull"));
    classDef.addClassAnnotation(classAnnotation);

    MethodDto method = new MethodDto("x", TypeName.of(String.class));
    method.setModifier(AccessModifier.PUBLIC);
    method.setCode("return null;");
    AnnotationDto methodAnnotation = new AnnotationDto();
    methodAnnotation.setAnnotationType(new TypeName("org.b", "NonNull"));
    method.addAnnotation(methodAnnotation);
    MethodParameterDto param = new MethodParameterDto();
    param.setParameterName("in");
    param.setParameterTypeName(TypeName.of(String.class));
    AnnotationDto paramAnnotation = new AnnotationDto();
    paramAnnotation.setAnnotationType(new TypeName("org.a", "NonNull"));
    param.addAnnotation(paramAnnotation);
    method.addParameter(param);
    classDef.addMethod(method);

    // Two distinct interfaces sharing a simple name.
    classDef.addInterface(new InterfaceName("org.a", "Cloneable"));
    classDef.addInterface(new InterfaceName("org.b", "Cloneable"));

    assertGeneratedSource(
        """
        package test;
        import org.a.Cloneable;
        import org.a.NonNull;
        @NonNull
        public class AmbiguousBuilder implements Cloneable, org.b.Cloneable {
          @org.b.NonNull
          public String x(@NonNull String in) {
            return null;
          }
        }""",
        classDef);
  }

  @Test
  void qualifiesCollidingTypeUseAnnotationSimpleNames() throws Exception {
    GenerationTargetClassDto classDef = new GenerationTargetClassDto();
    classDef.setTypeName(new TypeName("test", "AmbiguousBuilder"));
    classDef.setClassAccessModifier(AccessModifier.PUBLIC);
    classDef.setFormattingMode(FormattingMode.NONE);

    // A field type carrying a type-use annotation whose simple name collides with a method
    // annotation type: both usages must render qualified.
    AnnotationDto methodAnnotation = new AnnotationDto();
    methodAnnotation.setAnnotationType(new TypeName("org.a", "Nullable"));
    MethodDto method = new MethodDto("x", TypeName.of(String.class));
    method.setModifier(AccessModifier.PUBLIC);
    method.setCode("return null;");
    method.addAnnotation(methodAnnotation);
    classDef.addMethod(method);

    AnnotationDto typeUseAnnotation = new AnnotationDto();
    typeUseAnnotation.setAnnotationType(new TypeName("org.b", "Nullable"));
    TypeName fieldType = TypeName.of(String.class);
    fieldType.addAnnotation(typeUseAnnotation);
    ClassFieldDto field = new ClassFieldDto();
    field.setFieldName("value");
    field.setFieldType(fieldType);
    classDef.addClassField(field);

    assertGeneratedSource(
        """
        package test;
        import org.a.Nullable;
        public class AmbiguousBuilder {
          @org.b.Nullable
          private String value;
          @Nullable
          public String x() {
            return null;
          }
        }""",
        classDef);
  }

  @Test
  void generatesClassMetadataJavadocAndAnnotations() throws Exception {
    GenerationTargetClassDto classDef = new GenerationTargetClassDto();
    classDef.setTypeName(new TypeName("test", "AnnotatedBuilder"));
    classDef.setClassAccessModifier(AccessModifier.PUBLIC);
    classDef.setFormattingMode(FormattingMode.NONE);
    classDef.setClassJavadoc(new JavadocDto("Builder for {@link Person}."));
    AnnotationDto generated = new AnnotationDto();
    generated.setAnnotationType(new TypeName("javax.annotation", "Generated"));
    generated.addMember("value", "\"simple-builders\"");
    classDef.addClassAnnotation(generated);
    classDef.setSuperType(new TypeName("test", "AbstractBuilder"));
    InterfaceName iface = new InterfaceName("java.io", "Serializable");
    classDef.addInterface(iface);

    assertGeneratedSource(
        """
        package test;
        import java.io.Serializable;
        import javax.annotation.Generated;
        /**
         * Builder for {@link Person}.
         */
        @Generated("simple-builders")
        public class AnnotatedBuilder extends AbstractBuilder implements Serializable {
        }""",
        classDef);
  }

  @Test
  void generatesGenericClassAndMethods() throws Exception {
    GenerationTargetClassDto classDef = new GenerationTargetClassDto();
    classDef.setTypeName(new TypeName("test", "GenericBuilder"));
    classDef.setClassAccessModifier(AccessModifier.PUBLIC);
    classDef.setFormattingMode(FormattingMode.NONE);
    GenericParameterDto generic = new GenericParameterDto();
    generic.setName("T");
    generic.addUpperBound(TypeName.of(Number.class));
    classDef.addGeneric(generic);

    MethodDto genericMethod = new MethodDto("of", new TypeNameVariable("T"));
    genericMethod.setModifier(AccessModifier.PUBLIC);
    genericMethod.setStatic(true);
    GenericParameterDto methodGeneric = new GenericParameterDto();
    methodGeneric.setName("R");
    methodGeneric.addUpperBound(TypeName.of(Number.class));
    genericMethod.addGenericParameter(methodGeneric);
    MethodParameterDto param = new MethodParameterDto();
    param.setParameterName("input");
    param.setParameterTypeName(new TypeNameVariable("R"));
    genericMethod.addParameter(param);
    genericMethod.setCode("return null;");
    classDef.addMethod(genericMethod);

    assertGeneratedSource(
        """
        package test;
        public class GenericBuilder<T extends Number> {
          public static <R extends Number> T of(R input) {
            return null;
          }
        }""",
        classDef);
  }

  @Test
  void generatesVoidVarargsAndPackagePrivateMembers() throws Exception {
    GenerationTargetClassDto classDef = new GenerationTargetClassDto();
    classDef.setTypeName(new TypeName("test", "MiscBuilder"));
    classDef.setClassAccessModifier(AccessModifier.PUBLIC);
    classDef.setFormattingMode(FormattingMode.NONE);

    MethodDto voidMethod = new MethodDto("close", null);
    voidMethod.setModifier(AccessModifier.PACKAGE_PRIVATE);
    MethodParameterDto varArgs = new MethodParameterDto();
    varArgs.setParameterName("items");
    varArgs.setParameterTypeName(new TypeNameArray(TypeName.of(String.class)));
    voidMethod.addParameter(varArgs);
    AnnotationDto suppressWarnings = new AnnotationDto();
    suppressWarnings.setAnnotationType(TypeName.of(SuppressWarnings.class));
    suppressWarnings.addMember("value", "\"unchecked\"");
    voidMethod.addAnnotation(suppressWarnings);
    classDef.addMethod(voidMethod);

    assertGeneratedSource(
        """
        package test;
        public class MiscBuilder {
          @SuppressWarnings("unchecked")
          void close(String... items) {
          }
        }""",
        classDef);
  }

  @Test
  void generatesJavadocCodeBlockAndValuelessTag() throws Exception {
    GenerationTargetClassDto classDef = new GenerationTargetClassDto();
    classDef.setTypeName(new TypeName("test", "DocBuilder"));
    classDef.setClassAccessModifier(AccessModifier.PUBLIC);
    classDef.setFormattingMode(FormattingMode.NONE);

    MethodDto method = new MethodDto("apply", TypeNamePrimitive.VOID);
    method.setModifier(AccessModifier.PUBLIC);
    JavadocDto javadoc = new JavadocDto("Applies the values.");
    javadoc.addTag("since");
    JavadocCodeBlockDto codeBlock = new JavadocCodeBlockDto();
    codeBlock.setCodeFormat("builder.apply();");
    javadoc.setExampleUsageCodeBlock(codeBlock);
    method.setJavadoc(javadoc);
    classDef.addMethod(method);

    assertGeneratedSource(
        """
        package test;
        public class DocBuilder {
          /**
           * Applies the values.
           <h4>Example:</h4><pre>{@code
           * builder.apply();
           * }</pre>
           * @since
           */
          public void apply() {
          }
        }""",
        classDef);
  }

  @Test
  void generatesNestedInterfaceWithAbstractMethod() throws Exception {
    GenerationTargetClassDto classDef = new GenerationTargetClassDto();
    classDef.setTypeName(new TypeName("test", "NestedBuilder"));
    classDef.setClassAccessModifier(AccessModifier.PUBLIC);
    classDef.setFormattingMode(FormattingMode.NONE);

    NestedTypeDto nested = new NestedTypeDto();
    nested.setTypeName("Callback");
    nested.setKind(NestedTypeDto.NestedTypeKind.INTERFACE);
    MethodDto abstractMethod = new MethodDto("onDone", TypeNamePrimitive.VOID);
    nested.addMethod(abstractMethod);
    classDef.addNestedType(nested);

    assertGeneratedSource(
        """
        package test;
        public class NestedBuilder {
          public interface Callback {
            public abstract void onDone() {
            }
          }
        }""",
        classDef);
  }

  @Test
  void generatesConstructorAndFieldVariants() throws Exception {
    GenerationTargetClassDto classDef = new GenerationTargetClassDto();
    classDef.setTypeName(new TypeName("test", "CtorBuilder"));
    classDef.setClassAccessModifier(AccessModifier.PUBLIC);
    classDef.setFormattingMode(FormattingMode.NONE);

    ClassFieldDto constant = new ClassFieldDto();
    constant.setFieldName("DEFAULT_SIZE");
    constant.setFieldType(TypeNamePrimitive.INT);
    constant.setVisibility(AccessModifier.PRIVATE);
    constant.setLiteralInitializer("42");
    classDef.addClassField(constant);

    ConstructorDto ctor = new ConstructorDto();
    ctor.setVisibility(AccessModifier.PROTECTED);
    MethodParameterDto param = new MethodParameterDto();
    param.setParameterName("capacity");
    param.setParameterTypeName(TypeNamePrimitive.INT);
    AnnotationDto paramAnnotation = new AnnotationDto();
    paramAnnotation.setAnnotationType(new TypeName("test.anno", "NonNull"));
    param.addAnnotation(paramAnnotation);
    ctor.addParameter(param);
    classDef.addConstructor(ctor);

    assertGeneratedSource(
        """
        package test;
        import test.anno.NonNull;
        public class CtorBuilder {
          private int DEFAULT_SIZE = 42;
          protected CtorBuilder(int capacity) {
          }
        }""",
        classDef);
  }

  @Test
  void generatesNestedTypesWithMembers() throws Exception {
    GenerationTargetClassDto classDef = new GenerationTargetClassDto();
    classDef.setTypeName(new TypeName("test", "OuterBuilder"));
    classDef.setClassAccessModifier(AccessModifier.PUBLIC);
    classDef.setFormattingMode(FormattingMode.NONE);

    NestedTypeDto nested = new NestedTypeDto();
    nested.setTypeName("Helper");
    nested.setKind(NestedTypeDto.NestedTypeKind.CLASS);
    nested.setVisibility(AccessModifier.PUBLIC);
    nested.setJavadoc(new JavadocDto("Nested helper."));
    MethodDto nestedMethod = new MethodDto("assist", TypeNamePrimitive.VOID);
    nestedMethod.setModifier(AccessModifier.PRIVATE);
    nestedMethod.setCode("// nothing");
    nested.addMethod(nestedMethod);
    classDef.addNestedType(nested);

    assertGeneratedSource(
        """
        package test;
        public class OuterBuilder {
          /**
           * Nested helper.
           */
          public class Helper {
            public void assist() {
            }
          }
        }""",
        classDef);
  }

  @Test
  void generatesMethodWithJavadocAnnotationsAndTypedParameters() throws Exception {
    GenerationTargetClassDto classDef = new GenerationTargetClassDto();
    classDef.setTypeName(new TypeName("test", "RichBuilder"));
    classDef.setClassAccessModifier(AccessModifier.PUBLIC);
    classDef.setFormattingMode(FormattingMode.NONE);

    MethodDto method = new MethodDto("items", new TypeNameGeneric(List.class, String.class));
    method.setModifier(AccessModifier.PUBLIC);
    JavadocDto javadoc = new JavadocDto("Adds items.");
    javadoc.addParam("values", "items to add");
    javadoc.addReturn("this builder");
    method.setJavadoc(javadoc);
    AnnotationDto override_ = new AnnotationDto();
    override_.setAnnotationType(TypeName.of(Override.class));
    method.addAnnotation(override_);
    MethodParameterDto param = new MethodParameterDto();
    param.setParameterName("values");
    param.setParameterTypeName(new TypeNameGeneric(List.class, String.class));
    method.addParameter(param);
    method.setCode("return null;");
    classDef.addMethod(method);

    assertGeneratedSource(
        """
        package test;
        import java.util.List;
        public class RichBuilder {
          /**
           * Adds items.
           * @param values items to add
           * @return this builder
           */
          @Override
          public List<String> items(List<String> values) {
            return null;
          }
        }""",
        classDef);
  }

  /**
   * Generates {@code classDef} into a fresh {@link TestingGenerationEnvironment} and returns it.
   */
  private static TestingGenerationEnvironment generate(GenerationTargetClassDto classDef)
      throws Exception {
    TestingGenerationEnvironment env = TestingGenerationEnvironment.create();
    new RoasterCodeGenerator(env)
        .generateClass(classDef, new RoasterSourceFormatter(env.getLogger(), FormattingMode.NONE));
    return env;
  }

  /** Generates {@code classDef} and compares the emitted source against {@code expected}. */
  private static void assertGeneratedSource(String expected, GenerationTargetClassDto classDef)
      throws Exception {
    assertNormalizedEquals(
        expected,
        generate(classDef).generatedSource(),
        "generated " + classDef.getTypeName().getClassName() + " must match the expected source");
  }

  /** A builder-style DTO: private target field, one delegating setter, one build method. */
  private static GenerationTargetClassDto personBuilderDefinition() {
    TypeName builderType = new TypeName("test", "PersonBuilder");
    TypeName personType = new TypeName("test", "Person");

    GenerationTargetClassDto target = new GenerationTargetClassDto();
    target.setTypeName(builderType);
    target.setClassAccessModifier(AccessModifier.PUBLIC);
    target.setFormattingMode(FormattingMode.NONE);

    ClassFieldDto field = new ClassFieldDto();
    field.setFieldName("target");
    field.setFieldType(personType);
    field.setVisibility(AccessModifier.PRIVATE);
    field.setLiteralInitializer("new Person()");
    target.addClassField(field);

    MethodDto name = new MethodDto("name", builderType);
    name.setModifier(AccessModifier.PUBLIC);
    MethodParameterDto param = new MethodParameterDto();
    param.setParameterName("name");
    param.setParameterTypeName(TypeName.of(String.class));
    name.addParameter(param);
    name.setCode(
        """
        target.setName(name);
        return this;""");
    target.addMethod(name);

    MethodDto build = new MethodDto("build", personType);
    build.setModifier(AccessModifier.PUBLIC);
    build.setCode("return target;");
    target.addMethod(build);
    return target;
  }

  private static GenerationTargetClassDto annotatedClassDefinition() {
    GenerationTargetClassDto classDef = new GenerationTargetClassDto();
    classDef.setTypeName(new TypeName("test", "AnnotatedTypes"));
    classDef.setClassAccessModifier(AccessModifier.PUBLIC);
    classDef.setFormattingMode(FormattingMode.NONE);
    return classDef;
  }

  private static TypeName stringTypeNullableAnnotated() {
    AnnotationDto nullable = new AnnotationDto();
    nullable.setAnnotationType(new TypeName("test.anno", "Nullable"));
    TypeName type = TypeName.of(String.class);
    type.addAnnotation(nullable);
    return type;
  }

  private static void assertAnnotatedSource(String expected, GenerationTargetClassDto classDef)
      throws Exception {
    String source = generate(classDef).generatedSource();
    assertNormalizedEquals(expected, source, "annotated declarations must be preserved");
    Compilation compilation =
        CodegenTestUtils.createCompiler()
            .compile(
                JavaFileObjects.forSourceString("test.AnnotatedTypes", source),
                JavaFileObjects.forSourceString(
                    "test.anno.Nullable",
                    """
                    package test.anno;
                    import java.lang.annotation.ElementType;
                    import java.lang.annotation.Target;
                    @Target(ElementType.TYPE_USE)
                    public @interface Nullable {
                    }
                    """));
    assertEquals(
        Compilation.Status.SUCCESS, compilation.status(), compilation.diagnostics().toString());
  }
}
