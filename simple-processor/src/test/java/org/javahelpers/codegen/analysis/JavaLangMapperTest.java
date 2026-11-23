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

package org.javahelpers.codegen.analysis;

import static org.javahelpers.codegen.testing.ElementTestUtils.method;
import static org.javahelpers.codegen.testing.ElementTestUtils.parameter;
import static org.javahelpers.codegen.testing.ElementTestUtils.typeElement;
import static org.javahelpers.codegen.testing.ProcessingTestUtils.context;
import static org.javahelpers.codegen.testing.ProcessingTestUtils.probe;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.lang.model.element.TypeElement;
import org.javahelpers.codegen.model.method.MethodParameterDto;
import org.javahelpers.codegen.model.type.GenericParameterDto;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.codegen.model.type.TypeNameGeneric;
import org.javahelpers.codegen.model.type.TypeNameList;
import org.javahelpers.codegen.model.type.TypeNameMap;
import org.javahelpers.codegen.model.type.TypeNameNested;
import org.javahelpers.codegen.model.type.TypeNamePrimitive;
import org.javahelpers.codegen.model.type.TypeNameSet;
import org.javahelpers.codegen.model.type.TypeNameWildcard;
import org.javahelpers.codegen.processing.AnnotationProcessingContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Unit tests for {@link JavaLangMapper} against elements captured from a real compilation. */
class JavaLangMapperTest {

  @ParameterizedTest
  @ValueSource(
      strings = {"?", "? extends java.time.Instant", "? super java.util.List<java.lang.String>"})
  void mapsWildcardArguments(String argument) {
    AtomicReference<TypeName> result = new AtomicReference<>();
    probe(
        (roundEnv, processingEnv) -> {
          TypeElement dto = typeElement(processingEnv, "test.Dto");
          result.set(
              JavaLangMapper.map2MethodParameter(
                      parameter(method(dto, "setValues"), 0), context(processingEnv))
                  .getParameterType());
        },
        """
        package test;
        public class Dto {
          public void setValues(java.util.List<%s> values) {}
        }
        """
            .formatted(argument));
    TypeNameList list = assertInstanceOf(TypeNameList.class, result.get());
    TypeNameWildcard wildcard = assertInstanceOf(TypeNameWildcard.class, list.getElementType());
    assertEquals(argument, wildcard.getFullQualifiedName());
  }

  @Test
  void map2TypeName_mapsJavaClass() {
    TypeName typeName = JavaLangMapper.map2TypeName(String.class);
    assertEquals("java.lang", typeName.getPackageName());
    assertEquals("String", typeName.getClassName());
  }

  @Test
  void mapToTypeName_mapsTypeElement() {
    AtomicReference<TypeName> result = new AtomicReference<>();
    probe(
        (roundEnv, processingEnv) ->
            result.set(
                JavaLangMapper.mapToTypeName(
                    typeElement(processingEnv, "test.Dto"), context(processingEnv))),
        """
            package test;
            public class Dto {}
            """);
    assertEquals("test", result.get().getPackageName());
    assertEquals("Dto", result.get().getClassName());
  }

  @Test
  void map2MethodParameter_mapsNameAndListType() {
    AtomicReference<MethodParameterDto> result = new AtomicReference<>();
    probe(
        (roundEnv, processingEnv) -> {
          TypeElement dto = typeElement(processingEnv, "test.Dto");
          result.set(
              JavaLangMapper.map2MethodParameter(
                  parameter(method(dto, "setTags"), 0), context(processingEnv)));
        },
        """
            package test;
            import java.util.List;
            public class Dto {
              private List<String> tags;
              public void setTags(List<String> tags) { this.tags = tags; }
            }
            """);
    MethodParameterDto param = result.get();
    assertEquals("tags", param.getParameterName());
    TypeNameList typeName = assertInstanceOf(TypeNameList.class, param.getParameterType());
    assertEquals("java.util.List", typeName.getPackageName() + "." + typeName.getClassName());
    assertEquals("String", typeName.getElementType().getClassName());
  }

  @Test
  void map2MethodParameter_mapsSetToTypeNameSet() {
    AtomicReference<TypeName> result = new AtomicReference<>();
    probe(
        (roundEnv, processingEnv) -> {
          TypeElement dto = typeElement(processingEnv, "test.Dto");
          result.set(paramType(dto, "setIds", 0, context(processingEnv)));
        },
        """
            package test;
            import java.util.Set;
            public class Dto {
              public void setIds(Set<Integer> ids) {}
            }
            """);
    assertInstanceOf(TypeNameSet.class, result.get());
    assertEquals("Integer", ((TypeNameSet) result.get()).getElementType().getClassName());
  }

  @Test
  void map2MethodParameter_mapsMapToTypeNameMap() {
    AtomicReference<TypeName> result = new AtomicReference<>();
    probe(
        (roundEnv, processingEnv) -> {
          TypeElement dto = typeElement(processingEnv, "test.Dto");
          result.set(paramType(dto, "setLookup", 0, context(processingEnv)));
        },
        """
            package test;
            import java.util.Map;
            public class Dto {
              public void setLookup(Map<String, Integer> lookup) {}
            }
            """);
    TypeNameMap typeName = assertInstanceOf(TypeNameMap.class, result.get());
    assertEquals("String", typeName.getKeyType().getClassName());
    assertEquals("Integer", typeName.getValueType().getClassName());
  }

  @Test
  void map2MethodParameter_mapsPrimitive() {
    AtomicReference<TypeName> result = new AtomicReference<>();
    probe(
        (roundEnv, processingEnv) -> {
          TypeElement dto = typeElement(processingEnv, "test.Dto");
          result.set(paramType(dto, "setCount", 0, context(processingEnv)));
        },
        """
            package test;
            public class Dto {
              public void setCount(int count) {}
            }
            """);
    assertInstanceOf(TypeNamePrimitive.class, result.get());
  }

  @Test
  void createDerivedTypeName_appendsSuffix() {
    AtomicReference<TypeName> result = new AtomicReference<>();
    probe(
        (roundEnv, processingEnv) ->
            result.set(
                JavaLangMapper.createDerivedTypeName(
                    typeElement(processingEnv, "test.Dto"), context(processingEnv), "Builder")),
        """
            package test;
            public class Dto {}
            """);
    assertEquals("test", result.get().getPackageName());
    assertEquals("DtoBuilder", result.get().getClassName());
  }

  @Test
  void extractInterfaceTypeArguments_resolvesInheritedListArgument() {
    // CustomList<X,Y> extends ArrayList<Y>: the List element type is Y, not the declared X/Y pair
    AtomicReference<List<TypeName>> result = new AtomicReference<>();
    probe(
        (roundEnv, processingEnv) -> {
          AnnotationProcessingContext ctx = context(processingEnv);
          TypeElement customList = typeElement(processingEnv, "test.CustomList");
          TypeElement listInterface = typeElement(processingEnv, "java.util.List");
          result.set(
              JavaLangMapper.extractInterfaceTypeArguments(
                  customList.asType(), listInterface, ctx));
        },
        """
            package test;
            import java.util.ArrayList;
            public class CustomList<X, Y> extends ArrayList<Y> {}
            """);
    assertEquals(1, result.get().size());
    assertEquals("Y", result.get().get(0).getClassName());
  }

  @Test
  void map2MethodParameter_wrapsConcreteCollections() {
    // ArrayList/HashMap/TreeMap take the hasConstructorWithParameterOfType path and still wrap
    AtomicReference<List<TypeName>> result = new AtomicReference<>();
    probe(
        (roundEnv, processingEnv) -> {
          TypeElement dto = typeElement(processingEnv, "test.Dto");
          AnnotationProcessingContext ctx = context(processingEnv);
          result.set(
              List.of(
                  paramType(dto, "setTags", 0, ctx),
                  paramType(dto, "setLookup", 0, ctx),
                  paramType(dto, "setSorted", 0, ctx)));
        },
        """
            package test;
            import java.util.ArrayList;
            import java.util.HashMap;
            import java.util.TreeMap;
            public class Dto {
              public void setTags(ArrayList<String> tags) {}
              public void setLookup(HashMap<String, Integer> lookup) {}
              public void setSorted(TreeMap<String, Integer> sorted) {}
            }
            """);
    assertInstanceOf(TypeNameList.class, result.get().get(0));
    assertEquals("String", ((TypeNameList) result.get().get(0)).getElementType().getClassName());
    assertInstanceOf(TypeNameMap.class, result.get().get(1));
    assertEquals("Integer", ((TypeNameMap) result.get().get(1)).getValueType().getClassName());
    assertInstanceOf(TypeNameMap.class, result.get().get(2));
  }

  @Test
  void map2MethodParameter_wrapsGenericListToTypeNameList() {
    AtomicReference<TypeName> result = new AtomicReference<>();
    probe(
        (roundEnv, processingEnv) -> {
          TypeElement dto = typeElement(processingEnv, "test.Dto");
          result.set(paramType(dto, "setTags", 0, context(processingEnv)));
        },
        """
            package test;
            import java.util.LinkedList;
            public class Dto {
              public void setTags(LinkedList<Double> tags) {}
            }
            """);
    assertInstanceOf(TypeNameList.class, result.get());
    assertEquals("Double", ((TypeNameList) result.get()).getElementType().getClassName());
  }

  @Test
  void map2GenericParameterDtos_mapsTypeParametersWithBounds() {
    AtomicReference<List<GenericParameterDto>> result = new AtomicReference<>();
    probe(
        (roundEnv, processingEnv) -> {
          TypeElement dto = typeElement(processingEnv, "test.Dto");
          result.set(JavaLangMapper.map2GenericParameterDtos(dto, context(processingEnv)));
        },
        """
            package test;
            public class Dto<T extends Number, U> {}
            """);
    assertEquals(2, result.get().size());
    assertEquals("T", result.get().get(0).getName());
    assertEquals("Number", result.get().get(0).getUpperBounds().get(0).getClassName());
    assertEquals("U", result.get().get(1).getName());
  }

  @Test
  void map2MethodParameter_mapsArrayAndTypeVariable() {
    AtomicReference<List<TypeName>> result = new AtomicReference<>();
    probe(
        (roundEnv, processingEnv) -> {
          TypeElement dto = typeElement(processingEnv, "test.Dto");
          AnnotationProcessingContext ctx = context(processingEnv);
          result.set(List.of(paramType(dto, "setArr", 0, ctx), paramType(dto, "setT", 0, ctx)));
        },
        """
            package test;
            public class Dto<T> {
              public void setArr(String[] arr) {}
              public void setT(T t) {}
            }
            """);
    assertEquals("TypeNameArray", result.get().get(0).getClass().getSimpleName());
    assertEquals("T", result.get().get(1).getClassName());
  }

  @Test
  void mapToTypeName_nullElement_returnsNull() {
    assertNull(JavaLangMapper.mapToTypeName(null, null));
    assertNull(JavaLangMapper.map2TypeName((TypeElement) null, null));
  }

  /**
   * Every primitive kind has its own switch branch — map them all in one DTO to cover the whole
   * {@code visitPrimitive} dispatch.
   */
  @Test
  void map2MethodParameter_mapsAllPrimitiveKinds() {
    AtomicReference<List<TypeName>> result = new AtomicReference<>();
    probe(
        (roundEnv, processingEnv) -> {
          TypeElement dto = typeElement(processingEnv, "test.Dto");
          AnnotationProcessingContext ctx = context(processingEnv);
          result.set(
              List.of(
                  paramType(dto, "setB", 0, ctx),
                  paramType(dto, "setBy", 0, ctx),
                  paramType(dto, "setS", 0, ctx),
                  paramType(dto, "setI", 0, ctx),
                  paramType(dto, "setL", 0, ctx),
                  paramType(dto, "setC", 0, ctx),
                  paramType(dto, "setF", 0, ctx),
                  paramType(dto, "setD", 0, ctx)));
        },
        """
            package test;
            public class Dto {
              public void setB(boolean b) {}
              public void setBy(byte b) {}
              public void setS(short s) {}
              public void setI(int i) {}
              public void setL(long l) {}
              public void setC(char c) {}
              public void setF(float f) {}
              public void setD(double d) {}
            }
            """);
    List<String> expected =
        List.of("BOOLEAN", "BYTE", "SHORT", "INT", "LONG", "CHAR", "FLOAT", "DOUBLE");
    for (int i = 0; i < expected.size(); i++) {
      assertInstanceOf(TypeNamePrimitive.class, result.get().get(i));
      assertEquals(expected.get(i), ((TypeNamePrimitive) result.get().get(i)).getType().name());
    }
  }

  /**
   * A parameter type that is a concrete non-JDK class with a public no-arg constructor is marked
   * {@code hasEmptyConstructor} — the builder can instantiate it for nested construction.
   */
  @Test
  void map2MethodParameter_marksTypeWithEmptyConstructor() {
    AtomicReference<TypeName> result = new AtomicReference<>();
    probe(
        (roundEnv, processingEnv) ->
            result.set(
                paramType(
                    typeElement(processingEnv, "test.Dto"),
                    "setAddress",
                    0,
                    context(processingEnv))),
        """
            package test;
            public class Address {
              public Address() {}
            }
            """,
        """
            package test;
            public class Dto {
              public void setAddress(Address a) {}
            }
            """);
    assertTrue(result.get().hasEmptyConstructor());
  }

  /**
   * A static nested class maps to {@link TypeNameNested} — the qualified class name keeps the
   * {@code Outer.Inner} form.
   */
  @Test
  void map2MethodParameter_nestedType_mapsTypeNameNested() {
    AtomicReference<TypeName> result = new AtomicReference<>();
    probe(
        (roundEnv, processingEnv) ->
            result.set(
                paramType(
                    typeElement(processingEnv, "test.Dto"), "setInner", 0, context(processingEnv))),
        """
            package test;
            public class Outer {
              public static class Inner {}
            }
            """,
        """
            package test;
            public class Dto {
              public void setInner(Outer.Inner i) {}
            }
            """);
    assertInstanceOf(TypeNameNested.class, result.get());
    assertEquals("Outer.Inner", result.get().getClassName());
  }

  @ParameterizedTest
  @ValueSource(strings = {"Inner", "Inner2<Integer>", "Static<Integer>"})
  void map2MethodParameter_preservesGenericOwnersAndChecksConstructibility(String member) {
    AtomicReference<TypeName> result = new AtomicReference<>();
    String type = member.startsWith("Static") ? "Outer." + member : "Outer<String>." + member;
    probe(
        (roundEnv, processingEnv) ->
            result.set(
                paramType(
                    typeElement(processingEnv, "test.Dto"), "setValue", 0, context(processingEnv))),
        """
        package test;
        public class Outer<T> {
          public class Inner {}
          public class Inner2<U> {}
          public static class Static<U> {}
        }
        """,
        """
        package test;
        public class Dto {
          public void setValue(%s value) {}
        }
        """
            .formatted(type));
    if (member.startsWith("Static")) {
      assertEquals("test.Outer.Static<java.lang.Integer>", result.get().getFullQualifiedName());
      assertTrue(result.get().hasEmptyConstructor());
    } else {
      assertEquals(
          "test.Outer<java.lang.String>." + member.replace("Integer", "java.lang.Integer"),
          result.get().getFullQualifiedName());
      assertFalse(result.get().hasEmptyConstructor());
    }
  }

  /**
   * A {@code Map} implementation without a {@code Map}-parameter copy constructor cannot offer the
   * generated map helpers — it falls back to a plain generic type name instead of {@code
   * TypeNameMap}.
   */
  @Test
  void map2MethodParameter_fallsBackWhenMapLacksCopyConstructor() {
    AtomicReference<TypeName> result = new AtomicReference<>();
    probe(
        (roundEnv, processingEnv) ->
            result.set(
                paramType(
                    typeElement(processingEnv, "test.Dto"),
                    "setLookup",
                    0,
                    context(processingEnv))),
        """
            package test;
            public class WeirdMap<K, V> extends java.util.AbstractMap<K, V> {
              @Override
              public java.util.Set<java.util.Map.Entry<K, V>> entrySet() {
                return java.util.Set.of();
              }
            }
            """,
        """
            package test;
            public class Dto {
              public void setLookup(WeirdMap<String, Integer> lookup) {}
            }
            """);
    assertInstanceOf(TypeNameGeneric.class, result.get());
  }

  private static TypeName paramType(
      TypeElement dto, String methodName, int paramIndex, TypeMappingContext ctx) {
    return JavaLangMapper.map2MethodParameter(parameter(method(dto, methodName), paramIndex), ctx)
        .getParameterType();
  }
}
