/*
 * MIT License
 *
 * Copyright (c) 2025 Andreas Igel
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

package org.javahelpers.simple.builders.processor.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.processing.ProcessingEnvironment;
import org.apache.commons.lang3.StringUtils;
import org.javahelpers.simple.builders.processor.enums.CompilerArgumentsEnum;

/**
 * Utility class for filtering generators and enhancers based on deactivation patterns.
 *
 * <p>This class provides methods to check if a generator or enhancer should be deactivated based on
 * class name patterns provided via compiler arguments. Patterns support wildcards and can match
 * simple class names or fully qualified class names.
 *
 * <h3>Pattern Examples:</h3>
 *
 * <ul>
 *   <li>{@code "ConditionalEnhancer"} - deactivates exactly this class
 *   <li>{@code "*HelperGenerator"} - deactivates all classes ending with HelperGenerator
 *   <li>{@code "*Consumer*"} - deactivates all classes containing Consumer
 *   <li>{@code "org.example.*"} - deactivates all classes in org.example package
 * </ul>
 *
 * <h3>Usage:</h3>
 *
 * <p>The deactivation patterns are provided as a single comma-separated list that can include both
 * method generators and builder enhancers. The filter will automatically determine which components
 * to deactivate based on their class names.
 */
public class ComponentFilter {

  private final Set<String> deactivatedPatterns;

  /**
   * Creates a new ComponentFilter and reads deactivation patterns from compiler arguments.
   *
   * @param processingEnv the processing environment to read compiler arguments from
   */
  public ComponentFilter(ProcessingEnvironment processingEnv) {
    CompilerArgumentsReader argumentsReader = new CompilerArgumentsReader(processingEnv);
    String deactivatedPatterns =
        argumentsReader.readValue(CompilerArgumentsEnum.DEACTIVATE_GENERATION_COMPONENTS);
    this.deactivatedPatterns = parsePatterns(deactivatedPatterns);
  }

  /**
   * Checks if a component should be deactivated.
   *
   * @param componentClassName the fully qualified class name of the component
   * @return true if the component should be deactivated, false otherwise
   */
  public boolean shouldDeactivateComponent(String componentClassName) {
    return shouldDeactivate(componentClassName, deactivatedPatterns);
  }

  /**
   * Parses comma-separated patterns into a set of trimmed patterns.
   *
   * @param patterns the comma-separated patterns, may be null or empty
   * @return a set of trimmed patterns, empty if input is null or empty
   */
  private Set<String> parsePatterns(String patterns) {
    if (StringUtils.isBlank(patterns)) {
      return new HashSet<>();
    }

    // Use Apache Commons split and streams to create Set directly
    return Arrays.stream(StringUtils.split(patterns, ",")).collect(Collectors.toSet());
  }

  /**
   * Checks if a class name should be deactivated based on the given patterns.
   *
   * @param className the fully qualified class name to check
   * @param patterns the set of patterns to match against
   * @return true if the class should be deactivated, false otherwise
   */
  private boolean shouldDeactivate(String className, Set<String> patterns) {
    if (patterns.isEmpty()) {
      return false;
    }

    String simpleClassName = extractSimpleClassName(className);

    for (String pattern : patterns) {
      if (matchesPattern(className, simpleClassName, pattern)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if a class name matches a pattern.
   *
   * @param fullClassName the fully qualified class name
   * @param simpleClassName the simple class name (without package)
   * @param pattern the pattern to match
   * @return true if the pattern matches, false otherwise
   */
  private boolean matchesPattern(String fullClassName, String simpleClassName, String pattern) {
    // Handle wildcards
    if (pattern.contains("*")) {
      // Convert wildcard pattern to regex
      String regex =
          pattern
              .replace(".", "\\.") // Escape dots
              .replace("*", ".*"); // Convert * to .*

      // Check against both full class name and simple class name
      return fullClassName.matches(regex) || simpleClassName.matches(regex);
    } else {
      // Exact match - check both full class name and simple class name
      return pattern.equals(fullClassName) || pattern.equals(simpleClassName);
    }
  }

  /**
   * Extracts the simple class name from a fully qualified class name.
   *
   * @param fullClassName the fully qualified class name
   * @return the simple class name
   */
  private String extractSimpleClassName(String fullClassName) {
    int lastDot = fullClassName.lastIndexOf('.');
    return lastDot >= 0 ? fullClassName.substring(lastDot + 1) : fullClassName;
  }
}
