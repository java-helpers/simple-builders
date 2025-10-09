package org.javahelpers.simple.builders.processor;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.javahelpers.simple.builders.processor.util.JavaLangAnalyser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** Unit tests for {@link JavaLangAnalyser}. */
class JavaLangAnalyserTest {

  private static class TestVariableElement implements VariableElement {
    private final String name;

    TestVariableElement(String name) {
      this.name = name;
    }

    @Override
    public Name getSimpleName() {
      return new Name() {
        @Override
        public String toString() {
          return name;
        }

        @Override
        public boolean contentEquals(CharSequence cs) {
          return name.contentEquals(cs);
        }

        @Override
        public int length() {
          return name.length();
        }

        @Override
        public char charAt(int index) {
          return name.charAt(index);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
          return name.subSequence(start, end);
        }
      };
    }

    // Required methods with default implementations
    @Override
    public Object getConstantValue() {
      return null;
    }

    @Override
    public <R, P> R accept(ElementVisitor<R, P> v, P p) {
      return null;
    }

    @Override
    public Element getEnclosingElement() {
      return null;
    }

    @Override
    public List<? extends Element> getEnclosedElements() {
      return List.of();
    }

    @Override
    public TypeMirror asType() {
      return null;
    }

    @Override
    public ElementKind getKind() {
      return ElementKind.PARAMETER;
    }

    @Override
    public Set<Modifier> getModifiers() {
      return Set.of();
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors() {
      return List.of();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
      return null;
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
      return null;
    }
  }

  private final VariableElement parameter = new TestVariableElement("testParam");

  private static Stream<Arguments> extractParamJavaDocNullCases() {
    VariableElement param = new TestVariableElement("testParam");
    return Stream.of(
        Arguments.of("JavaDoc is null", null, param),
        Arguments.of("Parameter is null", "Some Javadoc", null),
        Arguments.of(
            "Param tag not found",
            "/**\n         * Some method description.\n         * @param otherParam some other parameter\n         * @return something\n         */",
            param),
        Arguments.of(
            "Empty param doc",
            "/**\n         * Some method description.\n         * @param testParam\n         * @return something\n         */",
            param),
        Arguments.of(
            "Whitespace only param doc",
            "/**\n         * Some method description.\n         * @param testParam    \n         * @return something\n         */",
            param));
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("extractParamJavaDocNullCases")
  void extractParamJavaDoc_shouldReturnNull(
      String testCase, String javaDoc, VariableElement param) {
    assertNull(JavaLangAnalyser.extractParamJavaDoc(javaDoc, param));
  }

  @Test
  void extractParamJavaDoc_shouldExtractSingleLineParamDoc() {
    String javaDoc =
        "/**\n         * Some method description.\n         * @param testParam this is a test parameter\n         * @return something\n         */";
    assertEquals(
        "this is a test parameter", JavaLangAnalyser.extractParamJavaDoc(javaDoc, parameter));
  }

  @Test
  void extractParamJavaDoc_shouldExtractMultiLineParamDoc() {
    String javaDoc =
        "/**\n         * Some method description.\n         * @param testParam this is a test parameter\n         *                 that spans multiple lines\n         *                 with additional details\n         * @return something\n         */";
    assertEquals(
        "this is a test parameter that spans multiple lines with additional details",
        JavaLangAnalyser.extractParamJavaDoc(javaDoc, parameter));
  }

  @Test
  void extractParamJavaDoc_shouldStopAtNextTag() {
    String javaDoc =
        "/**\n         * Some method description.\n         * @param testParam this is a test parameter\n         * @return something\n         * @since 1.0\n         */";
    assertEquals(
        "this is a test parameter", JavaLangAnalyser.extractParamJavaDoc(javaDoc, parameter));
  }

  @Test
  void extractParamJavaDoc_shouldHandleWindowsLineEndings() {
    String javaDoc =
        "/**\r\n * Some method description.\r\n * @param testParam this is a test parameter\r\n * @return something\r\n */";
    assertEquals(
        "this is a test parameter", JavaLangAnalyser.extractParamJavaDoc(javaDoc, parameter));
  }

  @Test
  void extractParamJavaDoc_shouldStopAtEmptyLine() {
    String javaDoc =
        "/**\n         * @param testParam this is a test parameter\n         *\n         * Some additional description\n         * @return something\n         */";
    assertEquals(
        "this is a test parameter", JavaLangAnalyser.extractParamJavaDoc(javaDoc, parameter));
  }

  @Test
  void extractParamJavaDoc_shouldHandleEmptyParamTag() {
    String javaDoc = "/**\n         * @param testParam\n         * @return something\n         */";
    assertNull(JavaLangAnalyser.extractParamJavaDoc(javaDoc, parameter));
  }

  @Test
  void extractParamJavaDoc_shouldHandleWhitespaceOnlyParamTag() {
    String javaDoc =
        "/**\n         * @param testParam    \t\n         * @return something\n         */";
    assertNull(JavaLangAnalyser.extractParamJavaDoc(javaDoc, parameter));
  }

  @Test
  void extractParamJavaDoc_shouldAddSpaceBetweenLines() {
    String javaDoc =
        "/**\n         * @param testParam first line\n         * second line\n         * third line\n         * @return something\n         */";
    assertEquals(
        "first line second line third line",
        JavaLangAnalyser.extractParamJavaDoc(javaDoc, parameter));
  }

  @Test
  void findGetterForField_shouldReturnEmpty_whenDtoTypeIsNull() {
    assertTrue(JavaLangAnalyser.findGetterForField(null, "fieldName", null, null, null).isEmpty());
  }

  @Test
  void findGetterForField_shouldReturnEmpty_whenFieldNameIsNull() {
    assertTrue(JavaLangAnalyser.findGetterForField(null, null, null, null, null).isEmpty());
  }

  @Test
  void findGetterForField_shouldReturnEmpty_whenFieldTypeMirrorIsNull() {
    assertTrue(JavaLangAnalyser.findGetterForField(null, "fieldName", null, null, null).isEmpty());
  }
}
