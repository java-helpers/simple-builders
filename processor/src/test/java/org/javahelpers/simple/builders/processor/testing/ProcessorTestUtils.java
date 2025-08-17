package org.javahelpers.simple.builders.processor.testing;

import static com.google.testing.compile.CompilationSubject.assertThat;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import java.util.List;
import javax.tools.JavaFileObject;

/**
 * Utilities to simplify annotation-processor tests by reducing boilerplate for building sources,
 * compiling them, and retrieving generated files.
 */
public final class ProcessorTestUtils {

  private ProcessorTestUtils() {}

  /**
   * Creates a {@link JavaFileObject} for a simple class annotated with @SimpleBuilder. You pass the
   * inner body lines (fields/methods); imports and annotation are handled for you.
   */
  public static JavaFileObject simpleBuilderClass(
      String packageName, String className, List<String> bodyLines) {
    String fqcn = packageName + "." + className;
    String[] lines = buildSourceLines(packageName, className, bodyLines);
    return JavaFileObjects.forSourceLines(fqcn, lines);
  }

  /**
   * Creates a {@link JavaFileObject} using a single body string (ideal for Java text blocks """ ...
   * """). The body is placed inside the annotated class.
   */
  public static JavaFileObject simpleBuilderClass(
      String packageName, String className, String body) {
    String fqcn = packageName + "." + className;
    String source =
        "package "
            + packageName
            + ";\n\n"
            + "import "
            + org.javahelpers.simple.builders.core.annotations.SimpleBuilder.class.getName()
            + ";\n\n"
            + "@SimpleBuilder\n"
            + "public class "
            + className
            + " {\n"
            + body
            + (body.endsWith("\n") ? "" : "\n")
            + "}\n";
    return JavaFileObjects.forSourceString(fqcn, source);
  }

  /**
   * Asserts compilation succeeded without warnings and returns the generated builder source code
   * for the provided simple builder class name (simple name, without package), e.g.
   * "PersonBuilder".
   */
  public static String assertSucceededAndGetGenerated(
      Compilation compilation, String builderSimpleName) {
    assertThat(compilation).succeededWithoutWarnings();

    JavaFileObject file =
        compilation.generatedFiles().stream()
            .filter(f -> f.getKind() == JavaFileObject.Kind.SOURCE)
            .filter(f -> f.getName().endsWith(builderSimpleName + ".java"))
            .findFirst()
            .orElseThrow(
                () -> new AssertionError("Generated source file not found: " + builderSimpleName));

    try {
      return file.getCharContent(true).toString();
    } catch (Exception e) {
      throw new RuntimeException("Failed to read generated source for: " + builderSimpleName, e);
    }
  }

  private static String[] buildSourceLines(
      String packageName, String className, List<String> bodyLines) {
    // Build lines with imports + annotation + class skeleton
    String[] header =
        new String[] {
          "package " + packageName + ";",
          "",
          "import "
              + org.javahelpers.simple.builders.core.annotations.SimpleBuilder.class.getName()
              + ";",
          "",
          "@SimpleBuilder",
          "public class " + className + " {"
        };

    String[] footer = new String[] {"}"};

    String[] body = bodyLines.toArray(String[]::new);

    String[] lines = new String[header.length + body.length + footer.length];
    System.arraycopy(header, 0, lines, 0, header.length);
    System.arraycopy(body, 0, lines, header.length, body.length);
    System.arraycopy(footer, 0, lines, header.length + body.length, footer.length);
    return lines;
  }
}
