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

package org.javahelpers.simple.builders.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.tools.JavaFileObject;
import org.javahelpers.codegen.model.core.GenerationTargetClassDto;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/** Explicit regeneration entry point; ordinary builds never rewrite committed sources. */
@EnabledIfSystemProperty(named = "codegen.regenerate", matches = "true")
class CodegenDtoBuilderRegenerationTest {

  @Test
  void regenerateBuilders() throws Exception {
    Path root = Path.of(System.getProperty("basedir")).getParent();
    Path sources = root.resolve("codegen-core/src/main/java");
    List<JavaFileObject> inputs = new ArrayList<>();
    try (var paths = Files.walk(sources)) {
      for (Path path :
          paths
              .filter(p -> p.toString().endsWith(".java"))
              .filter(p -> !p.toString().endsWith("Builder.java"))
              .sorted()
              .toList()) {
        String className =
            sources.relativize(path).toString().replace('/', '.').replace(".java", "");
        inputs.add(JavaFileObjects.forSourceString(className, Files.readString(path)));
      }
    }
    for (String group :
        List.of(
            "core:GenerationTargetClassDto,ClassFieldDto",
            "method:MethodDto,ConstructorDto,MethodParameterDto,CodeTemplateDto,MethodCodeDto",
            "type:GenericParameterDto,NestedTypeDto",
            "annotation:AnnotationDto",
            "javadoc:JavadocDto,JavadocCodeBlockDto")) {
      String[] parts = group.split(":");
      String packageName = "org.javahelpers.codegen.model." + parts[0];
      String types =
          java.util.Arrays.stream(parts[1].split(","))
              .map(name -> name + ".class")
              .collect(java.util.stream.Collectors.joining(", "));
      inputs.add(
          JavaFileObjects.forSourceString(
              packageName + ".CodegenBuilders",
              """
          package %s;
          @org.javahelpers.simple.builders.core.annotations.SimpleBuilderFor({%s})
          class CodegenBuilders {}
          """
                  .formatted(packageName, types)));
    }

    File codegenClasses =
        new File(
            GenerationTargetClassDto.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI());
    List<File> classpath =
        Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator))
            .map(File::new)
            .filter(file -> !file.equals(codegenClasses))
            .toList();
    Compilation compilation =
        ProcessorTestUtils.createCompiler()
            .withClasspath(classpath)
            .withOptions("-Asimplebuilder.strict=true")
            .compile(inputs);
    assertThat(compilation).succeeded();
    List<JavaFileObject> builders =
        compilation.generatedSourceFiles().stream()
            .filter(file -> file.getName().endsWith("Builder.java"))
            .toList();
    assertEquals(12, builders.size());
    String header =
        "/*\n"
            + Files.readAllLines(root.resolve("LICENSE.txt")).stream()
                .map(line -> line.isEmpty() ? " *" : " * " + line)
                .collect(java.util.stream.Collectors.joining("\n"))
            + "\n */\n\n";
    for (JavaFileObject builder : builders) {
      String source = builder.getCharContent(false).toString();
      String packageName = source.substring(source.indexOf("package ") + 8, source.indexOf(';'));
      String fileName = Path.of(builder.toUri().getPath()).getFileName().toString();
      Files.writeString(
          sources.resolve(packageName.replace('.', '/')).resolve(fileName), header + source);
    }
  }
}
