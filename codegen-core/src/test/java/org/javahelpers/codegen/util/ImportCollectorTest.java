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

package org.javahelpers.codegen.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.javahelpers.codegen.model.core.ClassFieldDto;
import org.javahelpers.codegen.model.core.ClassFieldDtoBuilder;
import org.javahelpers.codegen.model.core.GenerationTargetClassDto;
import org.javahelpers.codegen.model.core.GenerationTargetClassDtoBuilder;
import org.javahelpers.codegen.model.imports.ImportStatement;
import org.javahelpers.codegen.model.method.CodeTemplateDto;
import org.javahelpers.codegen.model.method.CodeTemplateDtoBuilder;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.codegen.model.type.TypeNameGeneric;
import org.javahelpers.codegen.model.type.TypeNameWildcard;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ImportCollector}: skip rules, generic args and sort order. */
class ImportCollectorTest {

  @Test
  void convenienceImportMethodsTraverseWildcardBounds() {
    GenerationTargetClassDto target =
        GenerationTargetClassDtoBuilder.create()
            .typeName("test", "Target")
            .add2Imports(Instant.class)
            .build();
    ClassFieldDto field = ClassFieldDtoBuilder.create().add2FieldTypeImports(Instant.class).build();
    CodeTemplateDto code =
        CodeTemplateDtoBuilder.create().add2CodeBlockImports(Instant.class).build();
    String expected =
        """
        java.time.Instant""";
    assertEquals(expected, fqnsText(target.getImports()));
    assertEquals(expected, fqnsText(field.getFieldTypeImports()));
    assertEquals(expected, fqnsText(code.getCodeBlockImports()));
  }

  @Test
  void collectsWildcardBoundImportsInsteadOfWildcardTokens() {
    ImportCollector collector = new ImportCollector(new TypeName("test", "Target"));
    collector.addTypeImports(new TypeNameWildcard());
    collector.addTypeImports(TypeNameWildcard.extendsBound(TypeName.of(Instant.class)));
    collector.addTypeImports(
        TypeNameWildcard.superBound(new TypeNameGeneric(List.class, File.class)));
    assertEquals(
        """
        java.io.File
        java.time.Instant
        java.util.List""",
        fqnsText(collector.getSortedImports()));
  }

  private static String fqnsText(Set<ImportStatement> imports) {
    return imports.stream()
        .map(ImportStatement::getFullyQualifiedName)
        .collect(Collectors.joining("\n"));
  }

  private static List<String> fqns(GenerationTargetClassDto classDef) {
    return ImportCollector.collectAndSortImports(classDef).stream()
        .map(ImportStatement::getFullyQualifiedName)
        .toList();
  }

  @Test
  void skipsJavaLangAndSamePackageTypes() {
    GenerationTargetClassDto classDef =
        GenerationTargetClassDtoBuilder.create()
            .typeName("com.example", "Dto")
            .add2ClassFields(
                f -> f.fieldName("samePackage").fieldType("com.example", "Sibling"),
                f -> f.fieldName("langType").fieldType("java.lang", "String"),
                f -> f.fieldName("needed").fieldType("java.util", "Optional"))
            .build();

    assertEquals(List.of("java.util.Optional"), fqns(classDef));
  }

  @Test
  void importsSortedAlphabetically() {
    GenerationTargetClassDto classDef =
        GenerationTargetClassDtoBuilder.create()
            .typeName("com.example", "Dto")
            .add2ClassFields(
                f -> f.fieldName("z").fieldType("java.util", "TreeSet"),
                f -> f.fieldName("a").fieldType("java.util", "ArrayDeque"),
                f -> f.fieldName("m").fieldType("java.time", "Instant"))
            .build();

    assertEquals(
        List.of("java.time.Instant", "java.util.ArrayDeque", "java.util.TreeSet"), fqns(classDef));
  }

  @Test
  void toleratesNullImportStatements() {
    GenerationTargetClassDto classDef =
        GenerationTargetClassDtoBuilder.create()
            .typeName("com.example", "Dto")
            .add2ClassFields(f -> f.fieldName("needed").fieldType("java.util", "Optional"))
            .build();
    classDef.addImport((ImportStatement) null);

    assertEquals(List.of("java.util.Optional"), fqns(classDef));
  }

  @Test
  void genericTypeImportsRawTypeAndArguments() {
    GenerationTargetClassDto classDef =
        GenerationTargetClassDtoBuilder.create()
            .typeName("com.example", "Dto")
            .add2ClassFields(
                f -> f.fieldName("mapping").fieldType("com.example", "StringIntegerMap"))
            .add2Methods(
                m ->
                    m.methodName("values")
                        .returnType("java.util", "Collection")
                        .add2Parameters(
                            p -> p.parameterName("key").parameterTypeName("com.other", "KeyDto")))
            .build();

    assertTrue(fqns(classDef).contains("java.util.Collection"));
    assertTrue(fqns(classDef).contains("com.other.KeyDto"));
  }
}
