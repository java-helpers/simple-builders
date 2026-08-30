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
package org.javahelpers.simple.builders.processor.classgen.roaster;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import org.javahelpers.simple.builders.core.enums.FormattingMode;
import org.javahelpers.simple.builders.processor.processing.logging.ProcessingLogger;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link RoasterSourceFormatter}.
 *
 * <p>Tests cover:
 *
 * <ul>
 *   <li>{@code lightweightFormat()} edge cases: tab conversion, blank line collapsing, javadoc
 *       fixup, concatenated import/javadoc splitting
 *   <li>{@code format()} dispatch logic for NONE, LIGHTWEIGHT, and JDT modes
 *   <li>Fallback behavior when JDT formatter profile is unavailable
 *   <li>Constructor null checks
 * </ul>
 */
class RoasterSourceFormatterTest {

  /** A minimal ProcessingEnvironment stub that provides a no-op Messager. */
  private static final class TestProcessingEnv implements ProcessingEnvironment {
    final TestMessager messager = new TestMessager();

    @Override
    public Messager getMessager() {
      return messager;
    }

    @Override
    public Map<String, String> getOptions() {
      return Collections.emptyMap();
    }

    @Override
    public Elements getElementUtils() {
      return null;
    }

    @Override
    public Types getTypeUtils() {
      return null;
    }

    @Override
    public Filer getFiler() {
      return null;
    }

    @Override
    public SourceVersion getSourceVersion() {
      return SourceVersion.RELEASE_17;
    }

    @Override
    public Locale getLocale() {
      return Locale.getDefault();
    }
  }

  /** A minimal Messager that captures warnings. */
  private static final class TestMessager implements Messager {
    final List<String> warnings = new ArrayList<>();

    @Override
    public void printMessage(Diagnostic.Kind kind, CharSequence msg) {
      if (kind == Diagnostic.Kind.WARNING) {
        warnings.add(msg.toString());
      }
    }

    @Override
    public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e) {
      if (kind == Diagnostic.Kind.WARNING) {
        warnings.add(msg.toString());
      }
    }

    @Override
    public void printMessage(
        Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a) {
      if (kind == Diagnostic.Kind.WARNING) {
        warnings.add(msg.toString());
      }
    }

    @Override
    public void printMessage(
        Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a, AnnotationValue v) {
      if (kind == Diagnostic.Kind.WARNING) {
        warnings.add(msg.toString());
      }
    }
  }

  private TestProcessingEnv createProcessingEnv() {
    return new TestProcessingEnv();
  }

  private RoasterSourceFormatter createFormatter(FormattingMode mode) {
    return new RoasterSourceFormatter(new ProcessingLogger(createProcessingEnv()), mode);
  }

  // === Constructor tests ===

  @Test
  void constructor_nullLogger_throwsNullPointerException() {
    assertThrows(
        NullPointerException.class, () -> new RoasterSourceFormatter(null, FormattingMode.JDT));
  }

  @Test
  void constructor_nullFormattingMode_throwsNullPointerException() {
    assertThrows(
        NullPointerException.class,
        () -> new RoasterSourceFormatter(new ProcessingLogger(createProcessingEnv()), null));
  }

  // === NONE mode tests ===

  @Test
  void format_noneMode_returnsRawSourceUnchanged() {
    RoasterSourceFormatter formatter = createFormatter(FormattingMode.NONE);
    String raw =
        """
        package test;
        public class Foo {
        }
        """;
    assertEquals(raw, formatter.format(raw));
  }

  // === LIGHTWEIGHT mode tests ===

  @Test
  void lightweightFormat_convertsTabsToSpaces() {
    RoasterSourceFormatter formatter = createFormatter(FormattingMode.LIGHTWEIGHT);
    String input =
        """
        package test;
        public class Foo {
        \tpublic void bar() {
        \t\treturn;
        \t}
        }
        """;
    String result = formatter.lightweightFormat(input);
    assertTrue(result.contains("  public void bar()"), "Tabs should be converted to 2 spaces");
    assertTrue(result.contains("    return;"), "Nested tabs should be converted to 4 spaces");
    assertTrue(!result.contains("\t"), "No tabs should remain in output");
  }

  @Test
  void lightweightFormat_collapsesConsecutiveBlankLines() {
    RoasterSourceFormatter formatter = createFormatter(FormattingMode.LIGHTWEIGHT);
    String input =
        """
        package test;



        public class Foo {
        }
        """;
    String result = formatter.lightweightFormat(input);
    long blankCount = result.lines().filter(String::isBlank).count();
    assertEquals(1, blankCount, "Multiple consecutive blank lines should collapse to 1");
  }

  @Test
  void lightweightFormat_preservesSingleBlankLine() {
    RoasterSourceFormatter formatter = createFormatter(FormattingMode.LIGHTWEIGHT);
    String input =
        """
        package test;

        public class Foo {
        }
        """;
    String result = formatter.lightweightFormat(input);
    long blankCount = result.lines().filter(String::isBlank).count();
    assertEquals(1, blankCount, "Single blank line should be preserved");
  }

  @Test
  void lightweightFormat_splitsConcatenatedImportAndJavadoc() {
    RoasterSourceFormatter formatter = createFormatter(FormattingMode.LIGHTWEIGHT);
    String input =
        """
        package test;import java.util.List;/**
         * This is a javadoc.
         */
        public class Foo {
        }
        """;
    String result = formatter.lightweightFormat(input);
    assertTrue(
        result.contains("import java.util.List;\n/**"),
        "Import and javadoc opening should be split into separate lines");
  }

  @Test
  void lightweightFormat_addsJavadocAsteriskPrefixes() {
    RoasterSourceFormatter formatter = createFormatter(FormattingMode.LIGHTWEIGHT);
    String input =
        """
        package test;
        /**
        This is a javadoc body line.
        Another body line.
         */
        public class Foo {
        }
        """;
    String result = formatter.lightweightFormat(input);
    assertTrue(
        result.contains(" * This is a javadoc body line."),
        "Javadoc body lines should get ' * ' prefix");
    assertTrue(
        result.contains(" * Another body line."),
        "Multiple javadoc body lines should get ' * ' prefix");
  }

  @Test
  void lightweightFormat_normalizesBlankJavadocLines() {
    RoasterSourceFormatter formatter = createFormatter(FormattingMode.LIGHTWEIGHT);
    String input =
        """
        package test;
        /**
        First line.

        Second line.
         */
        public class Foo {
        }
        """;
    String result = formatter.lightweightFormat(input);
    assertTrue(
        result.contains(" * First line.\n *\n * Second line."),
        "Blank javadoc lines should get ' *' prefix");
  }

  @Test
  void lightweightFormat_handlesInlineJavadocClose() {
    RoasterSourceFormatter formatter = createFormatter(FormattingMode.LIGHTWEIGHT);
    String input =
        """
        package test;
        /** This is a one-line javadoc. */
        public class Foo {
        }
        """;
    String result = formatter.lightweightFormat(input);
    assertTrue(
        result.contains("/** This is a one-line javadoc. */"),
        "Inline javadoc (/** ... */) should be preserved as-is");
  }

  @Test
  void lightweightFormat_handlesEmptyInput() {
    RoasterSourceFormatter formatter = createFormatter(FormattingMode.LIGHTWEIGHT);
    String result = formatter.lightweightFormat("");
    assertEquals("", result, "Empty input should produce empty output");
  }

  @Test
  void lightweightFormat_handlesJavadocWithIndentation() {
    RoasterSourceFormatter formatter = createFormatter(FormattingMode.LIGHTWEIGHT);
    String input =
        """
        package test;
        public class Foo {
          /**
          Body line.
          */
          public void bar() {
          }
        }
        """;
    String result = formatter.lightweightFormat(input);
    assertTrue(
        result.contains("  /**\n   * Body line."),
        "Javadoc body lines should be indented to match the enclosing member");
    assertTrue(
        result.contains("  */\n  public void bar()"),
        "Closing javadoc should align with the enclosing member");
  }

  @Test
  void lightweightFormat_preservesAlreadyPrefixedJavadocLines() {
    RoasterSourceFormatter formatter = createFormatter(FormattingMode.LIGHTWEIGHT);
    String input =
        """
        package test;
        /**
         * Already has prefix.
         */
        public class Foo {
        }
        """;
    String result = formatter.lightweightFormat(input);
    assertTrue(
        result.contains(" * Already has prefix."),
        "Lines that already have ' * ' prefix should be preserved");
  }

  @Test
  void lightweightFormat_handlesMultipleJavadocBlocks() {
    RoasterSourceFormatter formatter = createFormatter(FormattingMode.LIGHTWEIGHT);
    String input =
        """
        package test;
        /**
        Class-level javadoc.
         */
        public class Foo {
          /**
          Method javadoc.
          */
          public void bar() {
          }
        }
        """;
    String result = formatter.lightweightFormat(input);
    assertTrue(
        result.contains(" * Class-level javadoc."),
        "First javadoc block body should get ' * ' prefix");
    assertTrue(
        result.contains("   * Method javadoc."),
        "Second javadoc block body should get ' * ' prefix with correct indentation");
  }

  @Test
  void lightweightFormat_preservesJavadocTagLines() {
    RoasterSourceFormatter formatter = createFormatter(FormattingMode.LIGHTWEIGHT);
    String input =
        """
        package test;
        /**
        Description.
        @param value the value
        @return the result
         */
        public class Foo {
        }
        """;
    String result = formatter.lightweightFormat(input);
    assertTrue(
        result.contains(" * @param value the value"),
        "Javadoc @param tags should get ' * ' prefix");
    assertTrue(
        result.contains(" * @return the result"), "Javadoc @return tags should get ' * ' prefix");
  }

  @Test
  void lightweightFormat_handlesMixedTabsAndSpaces() {
    RoasterSourceFormatter formatter = createFormatter(FormattingMode.LIGHTWEIGHT);
    String input =
        """
        package test;
        public class Foo {
        \t  public void bar() {
        \t  \treturn;
        \t  }
        }
        """;
    String result = formatter.lightweightFormat(input);
    assertTrue(
        result.contains("    public void bar()"),
        "Mixed tab+space indentation should be converted (tab=2 spaces, then existing spaces)");
    assertTrue(!result.contains("\t"), "No tabs should remain in output");
  }

  @Test
  void lightweightFormat_preservesStarPrefixWithoutSpace() {
    RoasterSourceFormatter formatter = createFormatter(FormattingMode.LIGHTWEIGHT);
    String input =
        """
        package test;
        /**
        *Body line without space.
         */
        public class Foo {
        }
        """;
    String result = formatter.lightweightFormat(input);
    assertTrue(
        result.contains("*Body line without space."),
        "Lines with '*' prefix but no space should be preserved as-is (already have asterisk)");
  }

  // === format() dispatch tests ===

  @Test
  void format_lightweightMode_usesLightweightFormat() {
    RoasterSourceFormatter formatter = createFormatter(FormattingMode.LIGHTWEIGHT);
    String input =
        """
        package test;
        \tpublic class Foo {
        }
        """;
    String result = formatter.format(input);
    assertTrue(!result.contains("\t"), "LIGHTWEIGHT mode should convert tabs");
  }

  @Test
  void format_noneMode_doesNotConvertTabs() {
    RoasterSourceFormatter formatter = createFormatter(FormattingMode.NONE);
    String input =
        """
        package test;
        \tpublic class Foo {
        }
        """;
    String result = formatter.format(input);
    assertTrue(result.contains("\t"), "NONE mode should preserve tabs");
  }

  // === JDT mode tests ===

  @Test
  void format_jdtMode_withProfile_producesFormattedOutput() {
    TestProcessingEnv env = createProcessingEnv();
    ProcessingLogger logger = new ProcessingLogger(env);
    RoasterSourceFormatter formatter = new RoasterSourceFormatter(logger, FormattingMode.JDT);
    String input =
        """
        package test;
        \tpublic class Foo {
        }
        """;
    String result = formatter.format(input);
    assertNotNull(result, "Format should always return a non-null string");
    assertTrue(
        env.messager.warnings.stream().noneMatch(w -> w.contains("JDT formatting requested")),
        "No fallback warning should be logged when formatter profile is available on classpath");
  }
}
