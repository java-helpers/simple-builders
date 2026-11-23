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

package org.javahelpers.codegen.model.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link TypeName}. */
class TypeNameTest {

  @Test
  void hasWildcardArgumentsDefaultsToFalse() {
    assertFalse(TypeName.of(String.class).hasWildcardArguments());
  }

  @Test
  void hasWildcardArgumentsChecksImmediateGenericArguments() {
    TypeName unbounded = new TypeNameGeneric(TypeName.of(List.class), new TypeNameWildcard());
    TypeName upperBounded =
        new TypeNameGeneric(
            TypeName.of(List.class), TypeNameWildcard.extendsBound(TypeName.of(Number.class)));
    TypeName lowerBounded =
        new TypeNameGeneric(
            TypeName.of(List.class), TypeNameWildcard.superBound(TypeName.of(Integer.class)));
    TypeName concrete = new TypeNameGeneric(List.class, String.class);
    TypeName empty = new TypeNameGeneric(TypeName.of(List.class), List.of());
    TypeName nested = new TypeNameGeneric(TypeName.of(List.class), unbounded);

    assertTrue(unbounded.hasWildcardArguments());
    assertTrue(upperBounded.hasWildcardArguments());
    assertTrue(lowerBounded.hasWildcardArguments());
    assertFalse(concrete.hasWildcardArguments());
    assertFalse(empty.hasWildcardArguments());
    assertFalse(nested.hasWildcardArguments());
  }

  @Test
  void is_qualifiedName_matchesExactClass() {
    assertTrue(new TypeName("java.lang", "String").is(String.class));
    assertTrue(new TypeName("java.util", "Optional").is(java.util.Optional.class));
  }

  @Test
  void is_qualifiedName_rejectsOtherPackageOrClass() {
    assertFalse(new TypeName("com.example", "String").is(String.class));
    assertFalse(new TypeName("java.lang", "Integer").is(String.class));
  }

  @Test
  void is_emptyPackage_doesNotMatchPackagedClass() {
    assertFalse(new TypeName("", "String").is(String.class));
  }

  @Test
  void is_arrayType_doesNotMatchElementClass() {
    assertFalse(new TypeNameArray("java.lang", "String").is(String.class));
    assertFalse(new TypeNameArray("", "String").is(String.class));
  }

  @Test
  void is_genericType_matchesRawClass() {
    TypeNameGeneric optionalOfString =
        new TypeNameGeneric("java.util", "Optional", List.of(new TypeName("java.lang", "String")));
    assertTrue(optionalOfString.is(java.util.Optional.class));
    assertFalse(optionalOfString.is(String.class));
  }

  @Test
  void isAnyOf_matchesAnyListedClass() {
    TypeName longType = new TypeName("java.lang", "Long");
    assertTrue(longType.isAnyOf(Integer.class, Long.class, Float.class, Double.class));
    assertFalse(longType.isAnyOf(Integer.class, Float.class));
    assertFalse(new TypeName("java.lang", "String").isAnyOf(Integer.class, Long.class));
  }

  @Test
  void nestedType_exposesEnclosingTypeAndQualifiedName() {
    TypeNameNested inner = new TypeNameNested(new TypeName("test", "Outer"), "Inner");

    assertEquals("Outer.Inner", inner.getClassName());
    assertEquals("test.Outer.Inner", inner.getFullQualifiedName());
    assertTrue(inner.isNestedType());
    assertFalse(new TypeName("test", "Plain").isNestedType());
  }

  @Test
  void nestedType_supportsArbitraryDepth() {
    TypeNameNested middle = new TypeNameNested(new TypeName("test", "Outer"), "Middle");
    TypeNameNested deep = new TypeNameNested(middle, "Inner");

    assertEquals("Outer.Middle.Inner", deep.getClassName());
    assertEquals("test.Outer.Middle.Inner", deep.getFullQualifiedName());
    assertEquals("Outer.Middle", deep.getEnclosingType().getClassName());
  }

  @Test
  void nestedType_preservesParameterizedOwnersInRendering() {
    TypeName outer = new TypeNameGeneric(new TypeName("test", "Outer"), TypeName.of(String.class));
    TypeNameNested inner = new TypeNameNested(outer, "Inner");
    TypeName genericInner = new TypeNameGeneric(inner, TypeName.of(Integer.class));
    assertEquals("Outer.Inner", inner.getClassName());
    assertEquals("test.Outer<java.lang.String>.Inner", inner.getFullQualifiedName());
    assertEquals("Outer<String>.Inner", inner.getSimpleNameWithGenerics());
    assertEquals("Outer.Inner", genericInner.getClassName());
    assertEquals(
        "test.Outer<java.lang.String>.Inner<java.lang.Integer>",
        genericInner.getFullQualifiedName());
    assertEquals("Outer<String>.Inner<Integer>", genericInner.getSimpleNameWithGenerics());
    assertEquals(
        "test.Outer<java.lang.String>.Inner<java.lang.Integer>.Deep",
        new TypeNameNested(genericInner, "Deep").getFullQualifiedName());
  }

  @Test
  void genericType_preservesNestedFlag() {
    TypeNameNested pair = new TypeNameNested(new TypeName("test", "Outer"), "Pair");
    TypeNameGeneric pairOfString =
        new TypeNameGeneric(pair, List.of(new TypeName("java.lang", "String")));

    assertTrue(pairOfString.isNestedType());
    assertEquals("test.Outer.Pair", pairOfString.getRawType().getFullQualifiedName());
  }

  @Test
  void of_factoriesMatchConstructorResults() {
    TypeName string = TypeName.of(String.class);

    assertEquals(
        new TypeNameList(TypeName.of(List.class), List.of(string), string),
        TypeNameList.of(string));
    assertEquals(TypeNameList.of(string), TypeNameList.of(String.class));

    TypeNameSet expectedSet =
        new TypeNameSet(TypeName.of(java.util.Set.class), List.of(string), string);
    assertEquals(expectedSet, TypeNameSet.of(string));
    assertEquals(TypeNameSet.of(string), TypeNameSet.of(String.class));

    TypeName integer = TypeName.of(Integer.class);
    TypeNameMap expectedMap =
        new TypeNameMap(
            TypeName.of(java.util.Map.class), List.of(string, integer), string, integer);
    assertEquals(expectedMap, TypeNameMap.of(string, integer));
    assertEquals(TypeNameMap.of(string, integer), TypeNameMap.of(String.class, Integer.class));

    assertEquals(new TypeNameArray(string), TypeNameArray.of(string));
    assertEquals(TypeNameArray.of(string), TypeNameArray.of(String.class));

    assertEquals(new TypeNameGeneric(string, integer), TypeNameGeneric.of(string, integer));
    assertEquals(
        TypeNameGeneric.of(string, integer), TypeNameGeneric.of(String.class, Integer.class));
  }
}
