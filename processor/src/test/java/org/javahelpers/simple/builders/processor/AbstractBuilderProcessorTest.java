package org.javahelpers.simple.builders.processor;

import static com.google.testing.compile.Compiler.javac;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base test class for testing the {@link BuilderProcessor}. Provides utilities for compiling test
 * sources and verifying the generated code.
 */
public abstract class AbstractBuilderProcessorTest {

  protected BuilderProcessor processor;
  protected Compiler compiler;

  @BeforeEach
  protected void setUp() {
    processor = new BuilderProcessor();
    compiler = javac().withProcessors(processor);
  }

  /**
   * Compiles the given source files with the annotation processor.
   *
   * @param sourceFiles the source files to compile
   * @return the compilation result
   */
  protected Compilation compile(JavaFileObject... sourceFiles) {
    return compiler.compile(sourceFiles);
  }
}
