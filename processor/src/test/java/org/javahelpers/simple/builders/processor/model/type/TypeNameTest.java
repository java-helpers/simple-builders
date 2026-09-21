/*
 * MIT License
 *
 * Copyright (c) 2026 Andreas Igel
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

package org.javahelpers.simple.builders.processor.model.type;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link TypeName}. */
class TypeNameTest {

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
}
