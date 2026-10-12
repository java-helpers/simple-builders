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

package org.javahelpers.simple.builders.example;

import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;

/**
 * Example DTO demonstrating field conflict resolution in Simple Builders.
 * 
 * <p>This class intentionally creates a field conflict scenario by having two setters
 * with the same name but different parameter types. This is a common user error that
 * Simple Builders handles gracefully by renaming conflicting fields in the generated
 * builder.</p>
 * 
 * <h2>Conflict Scenario</h2>
 * <ul>
 *   <li>Both setters are named {@code setName} but accept different types</li>
 *   <li>One accepts {@code String} for the {@code firstName} field</li>
 *   <li>One accepts {@code Optional<String>} for the {@code lastName} field</li>
 * </ul>
 * 
 * <h2>Expected Behavior</h2>
 * <p>When debug logging is enabled ({@code -Dsimplebuilder.verbose=true}), you will see
 * warning messages showing how Simple Builders resolves this conflict:</p>
 * <ul>
 *   <li>The second field gets renamed from {@code name} to {@code nameOptional}</li>
 *   <li>Method conflicts are resolved by priority</li>
 *   <li>Compilation succeeds with warnings instead of errors</li>
 * </ul>
 * 
 * <h2>How to Fix the Conflict</h2>
 * <p>The proper solution is to give the setters different names:</p>
 * <pre>{@code
 * public void setFirstName(String name) {
 *     this.firstName = name;
 * }
 * 
 * public void setLastName(Optional<String> name) {
 *     this.lastName = name;
 * }
 * }</pre>
 * 
 * <p>Alternatively, use {@code @IgnoreInBuilder} on one of the setters if the conflict
 * is intentional.</p>
 * 
 * @see org.javahelpers.simple.builders.core.annotations.IgnoreInBuilder
 */
// @SimpleBuilder - needs to be reactivated after having #132 checked
public class FieldConflictExample {
  private String firstName;
  private java.util.Optional<String> lastName;

  public void setName(String name) {
    this.firstName = name;
  }

  public void setName(java.util.Optional<String> name) {
    this.lastName = name;
  }

  public String getFirstName() {
    return firstName;
  }

  public String getLastName() {
    return lastName.orElse(null);
  }
}
