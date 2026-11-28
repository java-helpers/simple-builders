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

package org.javahelpers.codegen.model.core;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;
import org.javahelpers.codegen.model.annotation.AnnotationDto;
import org.javahelpers.codegen.model.annotation.AnnotationDtoBuilder;
import org.javahelpers.codegen.model.annotation.InterfaceName;
import org.javahelpers.codegen.model.imports.ImportStatement;
import org.javahelpers.codegen.model.imports.RegularImport;
import org.javahelpers.codegen.model.javadoc.JavadocCodeBlockDtoBuilder;
import org.javahelpers.codegen.model.javadoc.JavadocDto;
import org.javahelpers.codegen.model.javadoc.JavadocDtoBuilder;
import org.javahelpers.codegen.model.javadoc.JavadocTagDto;
import org.javahelpers.codegen.model.method.*;
import org.javahelpers.codegen.model.type.GenericParameterDto;
import org.javahelpers.codegen.model.type.GenericParameterDtoBuilder;
import org.javahelpers.codegen.model.type.NestedTypeDto;
import org.javahelpers.codegen.model.type.NestedTypeDtoBuilder;
import org.javahelpers.codegen.model.type.TypeName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

/** Each committed builder constructs complete state, including collection properties. */
class CodegenDtoBuildersTest {
  private static final AnnotationDto ANNOTATION =
      AnnotationDtoBuilder.create()
          .annotationType(Deprecated.class)
          .add2Members("since", "\"1\"")
          .build();
  private static final ImportStatement IMPORT = new RegularImport(TypeName.of(List.class));
  private static final MethodCodePlaceholder<?> ARGUMENT =
      new MethodCodeStringPlaceholder("value", "message");

  @Test
  void staticFlagAcceptsBooleanSupplier() {
    for (boolean value : List.of(true, false)) {
      BooleanSupplier supplier = () -> value;
      assertEquals(value, MethodDtoBuilder.create().isStatic(supplier).build().isStatic());
    }
    MethodDtoBuilder builder = MethodDtoBuilder.create();
    assertThrows(NullPointerException.class, () -> builder.isStatic((BooleanSupplier) null));
  }

  @Test
  void buildsAnnotation() {
    AnnotationDto dto =
        AnnotationDtoBuilder.create()
            .annotationType(Deprecated.class)
            .add2Members("since", "\"2\"")
            .build();
    assertEquals(TypeName.of(Deprecated.class), dto.getAnnotationType());
    assertEquals(Map.of("since", "\"2\""), dto.getMembers());
  }

  @Test
  void buildsClassField() {
    ClassFieldDto dto =
        ClassFieldDtoBuilder.create()
            .fieldName("names")
            .fieldType(List.class)
            .add2FieldTypeImports(List.class)
            .build();
    assertEquals("names", dto.getFieldName());
    assertEquals(TypeName.of(List.class), dto.getFieldType());
    assertEquals(Set.of(IMPORT), dto.getFieldTypeImports());
  }

  @Test
  void buildsParameter() {
    MethodParameterDto dto = parameter();
    assertEquals("value", dto.getParameterName());
    assertEquals(TypeName.of(String.class), dto.getParameterType());
    assertEquals(List.of(ANNOTATION), dto.getAnnotations());
  }

  @Test
  void buildsMethod() {
    MethodParameterDto parameter = parameter();
    GenericParameterDto generic = generic();
    MethodCodeDto code = code();
    MethodDto dto =
        MethodDtoBuilder.create()
            .methodName("render")
            .isStatic(true)
            .add2Parameters(parameter)
            .add2GenericParameters(generic)
            .add2Annotations(ANNOTATION)
            .methodCodeDto(code)
            .build();
    assertEquals("render", dto.getMethodName());
    assertEquals(true, dto.isStatic());
    assertEquals(List.of(parameter), dto.getParameters());
    assertEquals(List.of(generic), dto.getGenericParameters());
    assertEquals(List.of(ANNOTATION), dto.getAnnotations());
    assertCode(dto.getMethodCodeDto());
    assertNotSame(code, dto.getMethodCodeDto());
    code.setCodeArguments(List.of());
    assertEquals(List.of(ARGUMENT), dto.getMethodCodeDto().getCodeArguments());
  }

  @Test
  void buildsConstructor() {
    MethodParameterDto parameter = parameter();
    MethodCodeDto code = code();
    ConstructorDto dto =
        ConstructorDtoBuilder.create()
            .add2Parameters(parameter)
            .add2Annotations(ANNOTATION)
            .methodCodeDto(code)
            .build();
    assertEquals(List.of(parameter), dto.getParameters());
    assertEquals(List.of(ANNOTATION), dto.getAnnotations());
    assertCode(dto.getMethodCodeDto());
  }

  @Test
  void buildsGenericParameter() {
    GenericParameterDto dto = generic();
    assertEquals("T", dto.getName());
    assertEquals(List.of(TypeName.of(Number.class)), dto.getUpperBounds());
  }

  @Test
  void buildsNestedType() {
    MethodDto method = MethodDtoBuilder.create().methodName("apply").build();
    NestedTypeDto dto =
        NestedTypeDtoBuilder.create()
            .typeName("Factory")
            .add2Methods(method)
            .add2Annotations(ANNOTATION)
            .build();
    assertEquals("Factory", dto.getTypeName());
    assertEquals(List.of(method), dto.getMethods());
    assertEquals(List.of(ANNOTATION), dto.getAnnotations());
  }

  @Test
  void buildsJavadoc() {
    JavadocTagDto tag = new JavadocTagDto("return", "a value");
    JavadocDto dto = JavadocDtoBuilder.create().description("Summary").add2Tags(tag).build();
    assertEquals("Summary", dto.getDescription());
    assertEquals(List.of(tag), dto.getTags());
  }

  @TestFactory
  Stream<DynamicTest> copiesJavadocDescriptionAndTags() {
    return Stream.of("Summary", "Rate 100%", "Placeholder %s", "")
        .map(
            description ->
                DynamicTest.dynamicTest(
                    "copies description: " + description,
                    () -> {
                      JavadocTagDto originalTag = new JavadocTagDto("since", "1.0");
                      JavadocTagDto additionalTag = new JavadocTagDto("return", "a value");
                      JavadocDto original = new JavadocDto(description, List.of(originalTag));
                      var example =
                          JavadocCodeBlockDtoBuilder.create().codeFormat("example();").build();
                      original.setExampleUsageCodeBlock(example);

                      assertAll(
                          () -> {
                            JavadocDto copy = new JavadocDtoBuilder(original).build();
                            assertEquals(description, copy.getDescription());
                            assertEquals(List.of(originalTag), copy.getTags());
                            assertSame(example, copy.getExampleUsageCodeBlock());
                          },
                          () -> {
                            JavadocDto modified = original.with(b -> b.add2Tags(additionalTag));
                            assertEquals(description, modified.getDescription());
                            assertEquals(List.of(originalTag, additionalTag), modified.getTags());
                            assertSame(example, modified.getExampleUsageCodeBlock());
                          });
                      assertEquals(description, original.getDescription());
                      assertEquals(List.of(originalTag), original.getTags());
                    }));
  }

  @Test
  void copiesJavadocWithoutDescription() {
    JavadocTagDto tag = new JavadocTagDto("since", "1.0");
    JavadocDto original = new JavadocDto();
    original.setTags(List.of(tag));

    JavadocDtoBuilder copy = assertDoesNotThrow(() -> new JavadocDtoBuilder(original));
    JavadocDto copied = copy.build();
    assertNull(copied.getDescription());
    assertEquals(List.of(tag), copied.getTags());
    assertNull(JavadocDtoBuilder.create().build().getDescription());

    JavadocDto modified = original.with(b -> b.add2Tags("return", "a value"));
    assertNull(modified.getDescription());
    assertEquals(List.of(tag, new JavadocTagDto("return", "a value")), modified.getTags());
    modified = original.with(b -> b.description("Summary"));
    assertEquals("Summary", modified.getDescription());
    assertEquals(List.of(tag), modified.getTags());
    assertNull(original.getDescription());
    assertEquals(List.of(tag), original.getTags());
  }

  @Test
  void javadocDescriptionIsLiteralUnlessExplicitlyFormatted() {
    assertEquals(
        "Rate 100%", JavadocDtoBuilder.create().description("Rate 100%").build().getDescription());
    assertEquals(
        "100%", JavadocDtoBuilder.create().description(() -> "100%").build().getDescription());
    assertEquals(
        "100%",
        JavadocDtoBuilder.create().description(sb -> sb.append("100%")).build().getDescription());
    assertEquals(
        "100% done",
        JavadocDtoBuilder.create().description("%s done", "100%").build().getDescription());
    assertNull(JavadocDtoBuilder.create().description((String) null).build().getDescription());
  }

  @Test
  void setsJavadocDescriptionLiterally() {
    JavadocDto dto = new JavadocDto();
    dto.setDescription("Rate 100% with %s unchanged");
    assertEquals("Rate 100% with %s unchanged", dto.getDescription());
    dto.setDescription(null);
    assertNull(dto.getDescription());
  }

  @Test
  void buildsCodeTemplate() {
    assertCode(
        CodeTemplateDtoBuilder.create()
            .codeFormat("return $value:S;")
            .add2CodeArguments(ARGUMENT)
            .add2CodeBlockImports(List.class)
            .build());
  }

  @Test
  void buildsMethodCode() {
    assertCode(code());
  }

  @Test
  void buildsJavadocCodeBlock() {
    assertCode(
        JavadocCodeBlockDtoBuilder.create()
            .codeFormat("return $value:S;")
            .add2CodeArguments(ARGUMENT)
            .add2CodeBlockImports(List.class)
            .build());
  }

  @Test
  void buildsGenerationTarget() {
    InterfaceName contract = new InterfaceName("java.lang", "Runnable");
    GenerationTargetClassDto dto =
        GenerationTargetClassDtoBuilder.create()
            .typeName("example", "Target")
            .add2ClassFields(f -> f.fieldName("value"))
            .add2Methods(m -> m.methodName("apply"))
            .add2Constructors(c -> {})
            .add2Generics(g -> g.name("T").upperBounds(List.of(TypeName.of(Number.class))))
            .interfaces(Set.of(contract))
            .add2NestedTypes(n -> n.typeName("Factory"))
            .add2ClassAnnotations(ANNOTATION)
            .add2Imports(List.class)
            .build();
    assertEquals(new TypeName("example", "Target"), dto.getTypeName());
    assertEquals("value", dto.getClassFields().get(0).getFieldName());
    assertEquals("apply", dto.getMethods().get(0).getMethodName());
    assertEquals(1, dto.getConstructors().size());
    assertEquals("T", dto.getGenerics().get(0).getName());
    assertEquals(Set.of(contract), dto.getInterfaces());
    assertEquals("Factory", dto.getNestedTypes().get(0).getTypeName());
    assertEquals(Set.of(ANNOTATION), dto.getClassAnnotations());
    assertEquals(Set.of(IMPORT), dto.getImports());
  }

  @TestFactory
  Stream<DynamicTest> collectionSettersCopyClearAndRejectNull() {
    return Stream.of(
            GenerationTargetClassDto.class,
            ClassFieldDto.class,
            MethodDto.class,
            ConstructorDto.class,
            MethodParameterDto.class,
            CodeTemplateDto.class,
            GenericParameterDto.class,
            NestedTypeDto.class,
            AnnotationDto.class,
            JavadocDto.class)
        .flatMap(type -> Arrays.stream(type.getDeclaredMethods()))
        .filter(method -> method.getName().startsWith("set") && method.getParameterCount() == 1)
        .filter(
            method ->
                Collection.class.isAssignableFrom(method.getParameterTypes()[0])
                    || Map.class.isAssignableFrom(method.getParameterTypes()[0]))
        .map(
            setter ->
                DynamicTest.dynamicTest(
                    setter.getDeclaringClass().getSimpleName() + "." + setter.getName(),
                    () -> assertCollectionContract(setter)));
  }

  private static void assertCollectionContract(Method setter) throws Exception {
    Object dto = setter.getDeclaringClass().getConstructor().newInstance();
    Method getter = setter.getDeclaringClass().getMethod("get" + setter.getName().substring(3));
    boolean map = Map.class.isAssignableFrom(setter.getParameterTypes()[0]);
    boolean set = Set.class.isAssignableFrom(setter.getParameterTypes()[0]);
    Object marker = new Object();
    Object input =
        map
            ? new LinkedHashMap<>(Map.of("key", marker))
            : set ? new LinkedHashSet<>(Set.of(marker)) : new ArrayList<>(List.of(marker));
    Object expected = map ? Map.of("key", marker) : set ? Set.of(marker) : List.of(marker);
    setter.invoke(dto, input);
    if (input instanceof Map<?, ?> values) {
      values.clear();
    } else {
      ((Collection<?>) input).clear();
    }
    assertEquals(expected, getter.invoke(dto));
    setter.invoke(dto, getter.invoke(dto));
    assertEquals(expected, getter.invoke(dto));
    InvocationTargetException failure =
        assertThrows(
            InvocationTargetException.class, () -> setter.invoke(dto, new Object[] {null}));
    assertInstanceOf(NullPointerException.class, failure.getCause());
    assertEquals(expected, getter.invoke(dto));
    setter.invoke(dto, input);
    assertEquals(input, getter.invoke(dto));
  }

  @Test
  void structuredBodyRejectsNullAndSupportsSelfAssignment() {
    MethodDto dto = MethodDtoBuilder.create().methodCodeDto(code()).build();
    assertThrows(NullPointerException.class, () -> dto.setMethodCodeDto(null));
    assertCode(dto.getMethodCodeDto());
    dto.setMethodCodeDto(dto.getMethodCodeDto());
    assertCode(dto.getMethodCodeDto());
  }

  private static MethodParameterDto parameter() {
    return MethodParameterDtoBuilder.create()
        .parameterName("value")
        .parameterTypeName(String.class)
        .annotations(List.of(ANNOTATION))
        .build();
  }

  private static GenericParameterDto generic() {
    return GenericParameterDtoBuilder.create()
        .name("T")
        .upperBounds(List.of(TypeName.of(Number.class)))
        .build();
  }

  private static MethodCodeDto code() {
    return MethodCodeDtoBuilder.create()
        .codeFormat("return $value:S;")
        .codeArguments(List.of(ARGUMENT))
        .codeBlockImports(Set.of(IMPORT))
        .build();
  }

  private static void assertCode(CodeTemplateDto dto) {
    assertEquals("return $value:S;", dto.getCodeFormat());
    assertEquals(List.of(ARGUMENT), dto.getCodeArguments());
    assertEquals(Set.of(IMPORT), dto.getCodeBlockImports());
  }
}
