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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import org.javahelpers.codegen.model.method.CodeTemplateDto;
import org.javahelpers.codegen.model.method.CodeTemplateDtoBuilder;
import org.javahelpers.codegen.model.method.ConstructorDto;
import org.javahelpers.codegen.model.method.ConstructorDtoBuilder;
import org.javahelpers.codegen.model.method.MethodCodeDto;
import org.javahelpers.codegen.model.method.MethodCodeDtoBuilder;
import org.javahelpers.codegen.model.method.MethodCodePlaceholder;
import org.javahelpers.codegen.model.method.MethodCodeStringPlaceholder;
import org.javahelpers.codegen.model.method.MethodDto;
import org.javahelpers.codegen.model.method.MethodDtoBuilder;
import org.javahelpers.codegen.model.method.MethodParameterDto;
import org.javahelpers.codegen.model.method.MethodParameterDtoBuilder;
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
          .annotationType(TypeName.of(Deprecated.class))
          .members(Map.of("since", "\"1\""))
          .build();
  private static final ImportStatement IMPORT = new RegularImport(TypeName.of(List.class));
  private static final MethodCodePlaceholder<?> ARGUMENT =
      new MethodCodeStringPlaceholder("value", "message");

  @Test
  void buildsAnnotation() {
    AnnotationDto dto =
        AnnotationDtoBuilder.create()
            .annotationType(TypeName.of(Deprecated.class))
            .members(Map.of("since", "\"2\""))
            .build();
    assertEquals(TypeName.of(Deprecated.class), dto.getAnnotationType());
    assertEquals(Map.of("since", "\"2\""), dto.getMembers());
  }

  @Test
  void buildsClassField() {
    ClassFieldDto dto =
        ClassFieldDtoBuilder.create()
            .fieldName("names")
            .fieldType(TypeName.of(List.class))
            .fieldTypeImports(Set.of(IMPORT))
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
            .parameters(List.of(parameter))
            .genericParameters(List.of(generic))
            .annotations(List.of(ANNOTATION))
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
            .parameters(List.of(parameter))
            .annotations(List.of(ANNOTATION))
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
            .methods(List.of(method))
            .annotations(List.of(ANNOTATION))
            .build();
    assertEquals("Factory", dto.getTypeName());
    assertEquals(List.of(method), dto.getMethods());
    assertEquals(List.of(ANNOTATION), dto.getAnnotations());
  }

  @Test
  void buildsJavadoc() {
    JavadocTagDto tag = new JavadocTagDto("return", "a value");
    JavadocDto dto =
        JavadocDtoBuilder.create().format("Summary").args(new Object[0]).tags(List.of(tag)).build();
    assertEquals("Summary", dto.getDescription());
    assertEquals(List.of(tag), dto.getTags());
  }

  @Test
  void buildsCodeTemplate() {
    assertCode(
        CodeTemplateDtoBuilder.create()
            .codeFormat("return $value:S;")
            .codeArguments(List.of(ARGUMENT))
            .codeBlockImports(Set.of(IMPORT))
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
            .codeArguments(List.of(ARGUMENT))
            .codeBlockImports(Set.of(IMPORT))
            .build());
  }

  @Test
  void buildsGenerationTarget() {
    ClassFieldDto field = ClassFieldDtoBuilder.create().fieldName("value").build();
    MethodDto method = MethodDtoBuilder.create().methodName("apply").build();
    ConstructorDto constructor = ConstructorDtoBuilder.create().build();
    GenericParameterDto generic = generic();
    NestedTypeDto nested = NestedTypeDtoBuilder.create().typeName("Factory").build();
    InterfaceName contract = new InterfaceName("java.lang", "Runnable");
    GenerationTargetClassDto dto =
        GenerationTargetClassDtoBuilder.create()
            .typeName(new TypeName("example", "Target"))
            .classFields(List.of(field))
            .methods(List.of(method))
            .constructors(List.of(constructor))
            .generics(List.of(generic))
            .interfaces(Set.of(contract))
            .nestedTypes(List.of(nested))
            .classAnnotations(Set.of(ANNOTATION))
            .imports(Set.of(IMPORT))
            .build();
    assertEquals(new TypeName("example", "Target"), dto.getTypeName());
    assertEquals(List.of(field), dto.getClassFields());
    assertEquals(List.of(method), dto.getMethods());
    assertEquals(List.of(constructor), dto.getConstructors());
    assertEquals(List.of(generic), dto.getGenerics());
    assertEquals(Set.of(contract), dto.getInterfaces());
    assertEquals(List.of(nested), dto.getNestedTypes());
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
        .parameterTypeName(TypeName.of(String.class))
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
