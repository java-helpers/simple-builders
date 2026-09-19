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
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.javahelpers.simple.builders.processor.model.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link PackageScopes}. */
class PackageScopesTest {

  @Test
  void unscoped_isEmptyAndMatchesNothing() {
    PackageScopes scopes = PackageScopes.unscoped();
    assertTrue(scopes.isEmpty());
    assertFalse(scopes.includes("com.example"));
  }

  @Test
  void parse_emptyOrBlank_returnsUnscoped() {
    assertSame(PackageScopes.unscoped(), PackageScopes.parse(null));
    assertSame(PackageScopes.unscoped(), PackageScopes.parse(""));
    assertSame(PackageScopes.unscoped(), PackageScopes.parse("   "));
  }

  @Test
  void parse_trimsNormalizesCaseAndFiltersBlankEntries() {
    PackageScopes scopes = PackageScopes.parse(" com.Example , , COM.other ");
    assertFalse(scopes.isEmpty());
    assertTrue(scopes.includes("com.example"));
    assertTrue(scopes.includes("com.other"));
    assertEquals("com.example, com.other", scopes.toString());
  }

  @Test
  void includes_matchesExactPackage() {
    PackageScopes scopes = PackageScopes.parse("com.example");
    assertTrue(scopes.includes("com.example"));
  }

  @Test
  void includes_matchesSubpackage() {
    PackageScopes scopes = PackageScopes.parse("com.example");
    assertTrue(scopes.includes("com.example.sub"));
    assertTrue(scopes.includes("com.example.deep.nested"));
  }

  @Test
  void includes_rejectsPackageWithSamePrefixButDifferentSegment() {
    // This is the critical test: "com.examplefoo" starts with "com.example" as a string,
    // but is NOT a subpackage. The "." separator in the scope check prevents this false
    // positive. Removing the "." from scope + "." would break this test.
    PackageScopes scopes = PackageScopes.parse("com.example");
    assertFalse(scopes.includes("com.examplefoo"));
    assertFalse(scopes.includes("com.examples"));
    assertFalse(scopes.includes("com.exampl"));
  }

  @Test
  void includes_isCaseInsensitive() {
    PackageScopes scopes = PackageScopes.parse("com.Example");
    assertTrue(scopes.includes("com.example"));
    assertTrue(scopes.includes("COM.EXAMPLE"));
    assertTrue(scopes.includes("com.Example.Sub"));
  }

  @Test
  void includes_rejectsUnrelatedPackage() {
    PackageScopes scopes = PackageScopes.parse("com.example");
    assertFalse(scopes.includes("org.other"));
    assertFalse(scopes.includes("com"));
  }

  @Test
  void merge_withEmptyReturnsOther() {
    PackageScopes a = PackageScopes.parse("com.example");
    PackageScopes b = PackageScopes.unscoped();
    assertSame(a, PackageScopes.merge(a, b));
    assertSame(a, PackageScopes.merge(b, a));
  }

  @Test
  void merge_bothEmptyReturnsUnscoped() {
    assertSame(
        PackageScopes.unscoped(),
        PackageScopes.merge(PackageScopes.unscoped(), PackageScopes.unscoped()));
  }

  @Test
  void merge_bothNonEmptyCombinesBoth() {
    PackageScopes a = PackageScopes.parse("com.example");
    PackageScopes b = PackageScopes.parse("org.other");
    PackageScopes merged = PackageScopes.merge(a, b);
    assertTrue(merged.includes("com.example"));
    assertTrue(merged.includes("com.example.sub"));
    assertTrue(merged.includes("org.other"));
    assertTrue(merged.includes("org.other.deep"));
  }

  @Test
  void packages_returnsConfiguredInDeclarationOrder() {
    PackageScopes scopes = PackageScopes.parse("com.first, com.second");
    assertEquals(2, scopes.packages().size());
  }
}
