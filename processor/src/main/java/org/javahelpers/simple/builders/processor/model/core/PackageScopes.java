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

package org.javahelpers.simple.builders.processor.model.core;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

/**
 * Immutable set of package scopes used for builder generation and usage restrictions.
 *
 * <p>A package is in scope when it equals a configured scope or is a subpackage of one; matching
 * ignores case. An empty scope set is "unscoped" and matches nothing — whether that means "no
 * restriction" is up to the caller.
 */
public final class PackageScopes {

  private static final PackageScopes UNSCOPED = new PackageScopes(Set.of());

  private final Set<String> packages;

  private PackageScopes(Set<String> packages) {
    this.packages = packages;
  }

  /**
   * Returns the unscoped instance holding no packages.
   *
   * @return the shared unscoped instance
   */
  public static PackageScopes unscoped() {
    return UNSCOPED;
  }

  /**
   * Parses a comma-separated list of package names into scopes.
   *
   * @param value the raw configured value, may be null or blank
   * @return the parsed scopes, unscoped when the value is blank
   */
  public static PackageScopes parse(String value) {
    if (StringUtils.isBlank(value)) {
      return UNSCOPED;
    }
    return new PackageScopes(
        Arrays.stream(StringUtils.split(value, ","))
            .map(String::trim)
            .filter(StringUtils::isNotBlank)
            .collect(
                Collectors.collectingAndThen(
                    Collectors.toCollection(LinkedHashSet::new), Collections::unmodifiableSet)));
  }

  /**
   * Returns whether no package scopes are configured.
   *
   * @return true if this instance holds no packages
   */
  public boolean isEmpty() {
    return packages.isEmpty();
  }

  /**
   * Checks whether the given package is within the configured scopes.
   *
   * <p>A package matches when it equals a configured scope or is one of its subpackages. Matching
   * ignores case. An empty scope set matches nothing.
   *
   * @param packageName the package to check
   * @return true if the package is within one of the configured scopes
   */
  public boolean includes(String packageName) {
    for (String scope : packages) {
      if (Strings.CI.equals(packageName, scope)
          || Strings.CI.startsWith(packageName, scope + ".")) {
        return true;
      }
    }
    return false;
  }

  /**
   * Returns the configured package names in declaration order.
   *
   * @return unmodifiable set of package names
   */
  public Set<String> packages() {
    return packages;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    return o instanceof PackageScopes other && packages.equals(other.packages);
  }

  @Override
  public int hashCode() {
    return packages.hashCode();
  }

  @Override
  public String toString() {
    return String.join(", ", packages);
  }
}
