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

package org.javahelpers.simple.builders.processor.analysis;

import java.util.List;
import java.util.Optional;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import org.javahelpers.simple.builders.processor.model.type.TypeName;

/**
 * Everything {@link JavaLangMapper} needs for mapping elements and type mirrors to {@code TypeName}
 * instances: javax.lang.model lookups plus the two mapping decisions (annotation copying and
 * generated-type resolution). Implemented by the processing context; the mapper stays independent
 * of builder internals.
 */
public interface TypeMappingContext {

  /**
   * Resolves a type element by its fully qualified name.
   *
   * @param qualifiedName fully qualified type name
   * @return the type element, or {@code null} if it cannot be resolved
   */
  TypeElement getTypeElement(String qualifiedName);

  /**
   * Returns the package name of the given element.
   *
   * @param element the element
   * @return the qualified package name
   */
  String getPackageName(Element element);

  /**
   * Returns the element corresponding to a type mirror.
   *
   * @param typeMirror the type mirror
   * @return the corresponding element
   */
  Element asElement(TypeMirror typeMirror);

  /**
   * Returns the erasure of a type mirror: the raw type with generic information stripped (JLS
   * §4.6). A parameterized type like {@code List<String>} erases to the raw type {@code List}, and
   * a type variable erases to its leftmost bound.
   *
   * <p>Mapping uses erasure to test whether a type is some kind of a known family (e.g. {@code
   * Supplier}, {@code Collection}) independent of its type arguments.
   *
   * @param typeMirror the type mirror
   * @return the erased type
   */
  TypeMirror erasure(TypeMirror typeMirror);

  /**
   * Tests whether one type is assignable to another.
   *
   * @param type1 the first type
   * @param type2 the second type
   * @return {@code true} if {@code type1} is assignable to {@code type2}
   */
  boolean isAssignable(TypeMirror type1, TypeMirror type2);

  /**
   * Tests whether two type mirrors represent the same type.
   *
   * @param type1 the first type
   * @param type2 the second type
   * @return {@code true} if both represent the same type
   */
  boolean isSameType(TypeMirror type1, TypeMirror type2);

  /**
   * Returns the direct supertypes of a type mirror.
   *
   * @param typeMirror the type mirror
   * @return list of direct supertypes (wildcard enforced by the javax.lang.model API)
   */
  @SuppressWarnings("java:S1452")
  List<? extends TypeMirror> directSupertypes(TypeMirror typeMirror);

  /**
   * Returns all members of a type, including inherited members.
   *
   * @param typeElement the type to inspect
   * @return list of all member elements (wildcard enforced by the javax.lang.model API)
   */
  @SuppressWarnings("java:S1452")
  List<? extends Element> getAllMembers(TypeElement typeElement);

  /**
   * Tests whether a member (type, method, constructor) is accessible from the package generated
   * code is emitted into.
   *
   * @param member the member to check
   * @return {@code true} if the member is accessible from the generated package
   */
  boolean isAccessibleFromGeneratedPackage(Element member);

  /**
   * Logs a debug message during type mapping when debug output is enabled.
   *
   * @param format the message format
   * @param args the format arguments
   */
  void debug(String format, Object... args);

  /**
   * Whether annotations declared on a type usage are copied onto the mapped {@code TypeName}.
   *
   * @return {@code true} if type-use annotations are copied
   */
  boolean shouldCopyTypeAnnotations();

  /**
   * Resolves the generated type for the given type element.
   *
   * @param typeElement the type element to resolve
   * @return the generated type name, or empty when none applies
   */
  Optional<TypeName> resolveGeneratedType(TypeElement typeElement);
}
