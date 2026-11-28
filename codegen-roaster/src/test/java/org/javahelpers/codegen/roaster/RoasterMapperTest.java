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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Set;
import org.javahelpers.codegen.model.annotation.AnnotationDto;
import org.javahelpers.codegen.model.annotation.AnnotationDtoBuilder;
import org.javahelpers.codegen.model.annotation.InterfaceName;
import org.javahelpers.codegen.model.method.CodeTemplateDto;
import org.javahelpers.codegen.model.method.CodeTemplateDtoBuilder;
import org.javahelpers.codegen.model.method.MethodCodePlaceholder;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.codegen.model.type.TypeNameArray;
import org.javahelpers.codegen.model.type.TypeNameGeneric;
import org.javahelpers.codegen.model.type.TypeNameNested;
import org.javahelpers.codegen.model.type.TypeNamePrimitive;
import org.javahelpers.codegen.model.type.TypeNameVariable;
import org.javahelpers.codegen.model.type.TypeNameWildcard;
import org.javahelpers.codegen.roaster.exceptions.RoasterMapperException;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link RoasterMapper}'s type/annotation/code-template rendering. */
class RoasterMapperTest {

  @Test
  void rendersWildcardBoundsRecursively() {
    assertEquals("?", RoasterMapper.mapType(new TypeNameWildcard(), Set.of()));
    assertEquals(
        "? extends Number",
        RoasterMapper.mapType(TypeNameWildcard.extendsBound(TypeName.of(Number.class)), Set.of()));
    assertEquals(
        "? super List<String>",
        RoasterMapper.mapType(
            TypeNameWildcard.superBound(new TypeNameGeneric(List.class, String.class)), Set.of()));
    assertEquals(
        "List<?>",
        RoasterMapper.mapType(
            new TypeNameGeneric(TypeName.of(List.class), List.of(new TypeNameWildcard())),
            Set.of()));
  }

  @Test
  void mapType_rendersPlainClassName() {
    assertEquals("String", RoasterMapper.mapType(TypeName.of(String.class), Set.of()));
  }

  @Test
  void mapType_rendersVariableName() {
    assertEquals("T", RoasterMapper.mapType(new TypeNameVariable("T"), Set.of()));
  }

  @Test
  void mapType_rendersPrimitive() {
    assertEquals("int", RoasterMapper.mapType(TypeNamePrimitive.INT, Set.of()));
  }

  @Test
  void mapType_rendersArrayOfInnerType() {
    assertEquals(
        "String[]", RoasterMapper.mapType(new TypeNameArray(TypeName.of(String.class)), Set.of()));
  }

  @Test
  void mapType_rendersGenericWithInnerArguments() {
    TypeNameGeneric generic = new TypeNameGeneric(List.class, String.class);
    assertEquals("List<String>", RoasterMapper.mapType(generic, Set.of()));
  }

  @Test
  void mapType_qualifiesCollidingGenericRawTypeOnly() {
    TypeNameGeneric generic = new TypeNameGeneric(List.class, String.class);
    assertEquals("java.util.List<String>", RoasterMapper.mapType(generic, Set.of("List")));
    assertEquals(
        "java.util.List<java.lang.String>",
        RoasterMapper.mapType(generic, Set.of("List", "String")));
  }

  @Test
  void mapType_rendersRawGenericWithoutArguments() {
    TypeNameGeneric generic = new TypeNameGeneric(List.class);
    assertEquals("List", RoasterMapper.mapType(generic, Set.of()));
  }

  @Test
  void mapType_rendersNestedTypeQualified() {
    TypeNameNested nested = new TypeNameNested(new TypeName("com.example", "Outer"), "Inner");
    assertEquals("com.example.Outer.Inner", RoasterMapper.mapType(nested, Set.of()));
  }

  @Test
  void mapType_rendersNestedGenericQualified() {
    TypeNameGeneric generic =
        new TypeNameGeneric(
            new TypeNameNested(new TypeName("com.example", "Outer"), "Pair"),
            TypeName.of(String.class));
    assertEquals("com.example.Outer.Pair<String>", RoasterMapper.mapType(generic, Set.of()));
  }

  @Test
  void mapType_prependsTypeUseAnnotations() {
    TypeName annotated = TypeName.of(String.class);
    AnnotationDto nonNull =
        AnnotationDtoBuilder.create().annotationType(new TypeName("test.anno", "NonNull")).build();
    annotated.addAnnotation(nonNull);
    assertEquals("@NonNull String", RoasterMapper.mapType(annotated, Set.of()));
  }

  @Test
  void mapBoxedType_boxesAllPrimitives() {
    assertEquals("Boolean", RoasterMapper.mapBoxedType(TypeNamePrimitive.BOOLEAN, Set.of()));
    assertEquals("Byte", RoasterMapper.mapBoxedType(TypeNamePrimitive.BYTE, Set.of()));
    assertEquals("Character", RoasterMapper.mapBoxedType(TypeNamePrimitive.CHAR, Set.of()));
    assertEquals("Double", RoasterMapper.mapBoxedType(TypeNamePrimitive.DOUBLE, Set.of()));
    assertEquals("Float", RoasterMapper.mapBoxedType(TypeNamePrimitive.FLOAT, Set.of()));
    assertEquals("Integer", RoasterMapper.mapBoxedType(TypeNamePrimitive.INT, Set.of()));
    assertEquals("Long", RoasterMapper.mapBoxedType(TypeNamePrimitive.LONG, Set.of()));
    assertEquals("Short", RoasterMapper.mapBoxedType(TypeNamePrimitive.SHORT, Set.of()));
    assertEquals("Void", RoasterMapper.mapBoxedType(TypeNamePrimitive.VOID, Set.of()));
  }

  @Test
  void mapBoxedType_passesThroughNonPrimitives() {
    assertEquals("String", RoasterMapper.mapBoxedType(TypeName.of(String.class), Set.of()));
  }

  @Test
  void mapAnnotation_rendersMarkerWithoutMembers() {
    AnnotationDto annotation =
        AnnotationDtoBuilder.create().annotationType(new TypeName("test.anno", "Marker")).build();
    assertEquals("@Marker", RoasterMapper.mapAnnotation(annotation, Set.of()));
  }

  @Test
  void mapAnnotation_rendersValueMemberBareAndOthersNamed() {
    AnnotationDto annotation =
        AnnotationDtoBuilder.create()
            .annotationType(new TypeName("test.anno", "Meta"))
            .add2Members("value", "\"x\"")
            .add2Members("order", "3")
            .build();
    assertEquals("@Meta(\"x\", order = 3)", RoasterMapper.mapAnnotation(annotation, Set.of()));
  }

  @Test
  void mapAnnotation_wrapsFailuresInMapperException() {
    AnnotationDto annotation =
        AnnotationDtoBuilder.create()
            .annotationType(
                new TypeName("test.anno", "Broken") {
                  @Override
                  public List<AnnotationDto> getAnnotations() {
                    throw new IllegalStateException("boom");
                  }
                })
            .build();
    assertThrows(
        RoasterMapperException.class, () -> RoasterMapper.mapAnnotation(annotation, Set.of()));
  }

  @Test
  void mapInterfaceToTypeName_rendersSimpleAndParameterized() {
    InterfaceName plain = new InterfaceName("java.util.function", "Supplier");
    assertEquals("Supplier", RoasterMapper.mapInterfaceToTypeName(plain, Set.of()));

    InterfaceName generic = new InterfaceName("java.util.function", "Supplier");
    generic.addTypeParameter(TypeName.of(String.class));
    assertEquals("Supplier<String>", RoasterMapper.mapInterfaceToTypeName(generic, Set.of()));
  }

  @Test
  void mapInterfaceToTypeName_prependsTypeUseAnnotations() {
    InterfaceName iface = new InterfaceName("java.io", "Serializable");
    AnnotationDto marker =
        AnnotationDtoBuilder.create().annotationType(new TypeName("test.anno", "Tag")).build();
    iface.addAnnotation(marker);
    assertEquals("@Tag Serializable", RoasterMapper.mapInterfaceToTypeName(iface, Set.of()));
  }

  @Test
  void mapInterfaceToTypeName_wrapsFailuresInMapperException() {
    InterfaceName iface =
        new InterfaceName("java.io", "Serializable") {
          @Override
          public List<TypeName> getTypeParameters() {
            throw new IllegalStateException("boom");
          }
        };
    iface.addTypeParameter(TypeName.of(String.class));
    // hasTypeParameters() calls getTypeParameters() -> throws
    assertThrows(
        RoasterMapperException.class, () -> RoasterMapper.mapInterfaceToTypeName(iface, Set.of()));
  }

  @Test
  void resolveCodeTemplate_replacesStringAndTypePlaceholders() {
    CodeTemplateDto code =
        CodeTemplateDtoBuilder.create()
            .codeFormat("return $name:S, $type:T, $boxed:B, $raw:N;")
            .add2CodeArguments("name", "val")
            .add2CodeArguments("type", TypeNamePrimitive.INT)
            .add2CodeArguments("boxed", TypeNamePrimitive.INT)
            .add2CodeArguments("raw", "literal")
            .build();

    assertEquals(
        "return \"val\", int, Integer, literal;",
        RoasterMapper.resolveCodeTemplate(code, Set.of()));
  }

  @Test
  void resolveCodeTemplate_rewritesTrackedValueFactoryNames() {
    CodeTemplateDto code =
        CodeTemplateDtoBuilder.create()
            .codeFormat(
                "TrackedValue.initialValue(x); TrackedValue.changedValue(y);"
                    + " TrackedValue.unsetValue();")
            .build();
    assertEquals(
        "initialValue(x); changedValue(y); unsetValue();",
        RoasterMapper.resolveCodeTemplate(code, Set.of()));
  }

  @Test
  void resolveCodeTemplate_rejectsUnknownPlaceholderKinds() {
    CodeTemplateDto code = CodeTemplateDtoBuilder.create().codeFormat("return $x:X;").build();
    code.addCodeArgument(new MethodCodePlaceholder<String>("x", "v") {});
    assertThrows(
        RoasterMapperException.class, () -> RoasterMapper.resolveCodeTemplate(code, Set.of()));
  }

  @Test
  void quote_escapesSpecialCharacters() {
    assertEquals("\"a\\\"b\\\\c\\n\\t\"", RoasterMapper.quote("a\"b\\c\n\t"));
  }

  @Test
  void packageNameOf_stripsClassName() {
    assertEquals("java.util", RoasterMapper.packageNameOf("java.util.List"));
    assertEquals("", RoasterMapper.packageNameOf("List"));
  }
}
