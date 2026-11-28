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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import org.javahelpers.codegen.enums.AccessModifier;
import org.javahelpers.codegen.enums.FormattingMode;
import org.javahelpers.codegen.model.annotation.InterfaceName;
import org.javahelpers.codegen.model.imports.RegularImport;
import org.javahelpers.codegen.model.javadoc.JavadocDto;
import org.javahelpers.codegen.model.method.MethodDto;
import org.javahelpers.codegen.model.method.MethodDtoBuilder;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.codegen.model.type.TypeNameArray;
import org.javahelpers.codegen.model.type.TypeNameGeneric;
import org.javahelpers.codegen.model.type.TypeNameList;
import org.javahelpers.codegen.model.type.TypeNameMap;
import org.junit.jupiter.api.Test;

/** Tests for the generated {@link GenerationTargetClassDtoBuilder}. */
class GenerationTargetClassDtoBuilderTest {

  @Test
  void collectionConsumersAppendWhileCollectionsAndSuppliersReplace() {
    GenerationTargetClassDtoBuilder builder = GenerationTargetClassDtoBuilder.create();
    Consumer<ClassFieldDtoBuilder> first = field -> field.fieldName("first");
    builder
        .add2ClassFields(first)
        .add2ClassFields(field -> field.fieldName("second"))
        .add2ClassFields(field -> field.fieldName("third"), field -> field.fieldName("fourth"))
        .add2ClassFields();
    assertEquals(
        List.of("first", "second", "third", "fourth"),
        builder.build().getClassFields().stream().map(ClassFieldDto::getFieldName).toList());

    Consumer<ClassFieldDtoBuilder> missing = null;
    assertThrows(NullPointerException.class, () -> builder.add2ClassFields(missing));
    assertEquals(4, builder.build().getClassFields().size());

    ClassFieldDto replacement = ClassFieldDtoBuilder.create().fieldName("replacement").build();
    builder.classFields(List.of(replacement));
    assertEquals(List.of(replacement), builder.build().getClassFields());
    builder.classFields(List::of);
    assertTrue(builder.build().getClassFields().isEmpty());
    builder.classFields(() -> List.of(replacement));
    assertEquals(List.of(replacement), builder.build().getClassFields());
    builder.classFields(List.of());
    assertTrue(builder.build().getClassFields().isEmpty());
  }

  @Test
  void buildsEquivalentDtoAsSetterSequence() {
    // Same DTO as assembled field-by-field in RoasterCodeGeneratorTest
    GenerationTargetClassDto dto =
        GenerationTargetClassDtoBuilder.create()
            .typeName("test", "AnnotatedBuilder")
            .classAccessModifier(AccessModifier.PUBLIC)
            .formattingMode(FormattingMode.NONE)
            .classJavadoc(new JavadocDto("Builder for {@link Person}."))
            .superType(new TypeName("test", "AbstractBuilder"))
            .build();

    assertEquals(new TypeName("test", "AnnotatedBuilder"), dto.getTypeName());
    assertEquals(AccessModifier.PUBLIC, dto.getClassAccessModifier());
    assertEquals(FormattingMode.NONE, dto.getFormattingMode());
    assertEquals("Builder for {@link Person}.", dto.getClassJavadoc().getDescription());
    assertEquals(new TypeName("test", "AbstractBuilder"), dto.getSuperType());
  }

  @Test
  void buildsNestedDtoViaBuilderConsumer() {
    GenerationTargetClassDto dto =
        GenerationTargetClassDtoBuilder.create()
            .typeName("test", "EmptyBuilder")
            .classJavadoc(javadoc -> javadoc.description("Builder for {@link %s}.", "Person"))
            .build();

    assertEquals("Builder for {@link Person}.", dto.getClassJavadoc().getDescription());
  }

  @Test
  void unsetPropertiesRemainNull() {
    GenerationTargetClassDto dto =
        GenerationTargetClassDtoBuilder.create().typeName("test", "EmptyBuilder").build();

    assertNull(dto.getClassAccessModifier());
    assertNull(dto.getClassJavadoc());
    assertNull(dto.getSuperType());
    assertNull(dto.getFormattingMode());
  }

  @Test
  void builderInitializedFromInstanceAppliesChanges() {
    GenerationTargetClassDto original =
        GenerationTargetClassDtoBuilder.create()
            .typeName("test", "Builder")
            .classAccessModifier(AccessModifier.PUBLIC)
            .build();

    GenerationTargetClassDto modified =
        new GenerationTargetClassDtoBuilder(original)
            .classAccessModifier(AccessModifier.PRIVATE)
            .build();

    assertEquals(original.getTypeName(), modified.getTypeName());
    assertEquals(AccessModifier.PRIVATE, modified.getClassAccessModifier());
    assertEquals(AccessModifier.PUBLIC, original.getClassAccessModifier());
  }

  @Test
  void buildsWholeDtoInOneFluentStream() {
    // One continuous stream: every nested DTO is configured through the
    // Consumer-based collection helpers, no intermediate variables.
    GenerationTargetClassDto dto =
        GenerationTargetClassDtoBuilder.create()
            .typeName("example", "Target")
            .superType(Number.class)
            .add2ClassFields(field -> field.fieldName("value").fieldTypeList(String.class))
            .add2Constructors(
                constructor ->
                    constructor.add2Parameters(
                        parameter ->
                            parameter.parameterName("arg").parameterTypeNameArray(String.class)))
            .add2Methods(
                method ->
                    method
                        .methodName("apply")
                        .returnTypeGeneric(Optional.class, String.class)
                        .add2Parameters(
                            parameter ->
                                parameter
                                    .parameterName("v")
                                    .parameterTypeNameMap(String.class, Integer.class))
                        .add2GenericParameters(
                            generic ->
                                generic
                                    .name("T")
                                    .add2UpperBounds(Number.class)
                                    .add2UpperBoundsSet(TypeName.of(Number.class)))
                        .add2Annotations(annotation -> annotation.annotationType(Deprecated.class)))
            .add2Generics(generic -> generic.name("E"))
            .interfaces(new InterfaceName("java.lang", "Runnable"))
            .add2NestedTypes(
                nested ->
                    nested.typeName("Factory").add2Methods(method -> method.methodName("create")))
            .add2ClassAnnotations(
                annotation ->
                    annotation
                        .annotationType("java.lang", "Deprecated")
                        .members(Map.of("since", "\"1\"")))
            .imports(new RegularImport(TypeName.of(List.class)))
            .build();

    assertEquals(new TypeName("example", "Target"), dto.getTypeName());
    assertEquals(TypeName.of(Number.class), dto.getSuperType());
    assertEquals("value", dto.getClassFields().get(0).getFieldName());
    assertEquals(TypeNameList.of(String.class), dto.getClassFields().get(0).getFieldType());
    assertEquals("arg", dto.getConstructors().get(0).getParameters().get(0).getParameterName());
    assertEquals(
        TypeNameArray.of(String.class),
        dto.getConstructors().get(0).getParameters().get(0).getParameterType());
    assertEquals("apply", dto.getMethods().get(0).getMethodName());
    assertEquals(
        TypeNameGeneric.of(Optional.class, String.class), dto.getMethods().get(0).getReturnType());
    assertEquals("v", dto.getMethods().get(0).getParameters().get(0).getParameterName());
    assertEquals(
        TypeNameMap.of(String.class, Integer.class),
        dto.getMethods().get(0).getParameters().get(0).getParameterType());
    assertEquals(2, dto.getMethods().get(0).getGenericParameters().get(0).getUpperBounds().size());
    assertEquals("T", dto.getMethods().get(0).getGenericParameters().get(0).getName());
    assertEquals("E", dto.getGenerics().get(0).getName());
    assertEquals(Set.of(new InterfaceName("java.lang", "Runnable")), dto.getInterfaces());
    assertEquals("Factory", dto.getNestedTypes().get(0).getTypeName());
    assertEquals("create", dto.getNestedTypes().get(0).getMethods().get(0).getMethodName());
    assertEquals(
        Map.of("since", "\"1\""), dto.getClassAnnotations().iterator().next().getMembers());
    assertEquals(Set.of(new RegularImport(TypeName.of(List.class))), dto.getImports());

    // The DTO implements its builder's With interface: copy-and-modify in one call.
    GenerationTargetClassDto renamed = dto.with(b -> b.typeName(Integer.class));
    assertEquals(TypeName.of(Integer.class), renamed.getTypeName());
    assertEquals(new TypeName("example", "Target"), dto.getTypeName());
    assertEquals(TypeName.of(Number.class), dto.getSuperType());
    assertEquals(dto.getClassFields(), renamed.getClassFields());
  }

  @Test
  void methodDtoBuilderCoversIsStaticFlag() {
    // The isStatic getter supplies a legal property name for the setStatic setter.
    MethodDto method = MethodDtoBuilder.create().methodName("m").isStatic(true).build();

    assertEquals("m", method.getMethodName());
    assertTrue(method.isStatic());
  }
}
