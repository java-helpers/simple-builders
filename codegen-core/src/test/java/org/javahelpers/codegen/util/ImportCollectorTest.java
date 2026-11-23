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
import org.javahelpers.codegen.model.core.GenerationTargetClassDto;
import org.javahelpers.codegen.model.imports.ImportStatement;
import org.javahelpers.codegen.model.method.CodeTemplateDto;
import org.javahelpers.codegen.model.method.MethodDto;
import org.javahelpers.codegen.model.method.MethodParameterDto;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.codegen.model.type.TypeNameGeneric;
import org.javahelpers.codegen.model.type.TypeNameNested;
import org.javahelpers.codegen.model.type.TypeNameWildcard;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ImportCollector}: skip rules, generic args and sort order. */
class ImportCollectorTest {

  @Test
  void nestedImportsEraseOwnersAndCollectTheirArguments() {
    TypeName outer =
        new TypeNameGeneric(new TypeName("other", "Outer"), TypeName.of(Instant.class));
    TypeName inner =
        new TypeNameGeneric(new TypeNameNested(outer, "Inner"), TypeName.of(File.class));
    ImportCollector collector = new ImportCollector(new TypeName("test", "Target"));
    collector.addTypeImports(new TypeNameNested(inner, "Deep"));
    assertEquals(
        """
        java.io.File
        java.time.Instant
        other.Outer.Inner.Deep""",
        fqnsText(collector.getSortedImports()));
  }

  @Test
  void convenienceImportMethodsTraverseWildcardBounds() {
    TypeNameWildcard wildcard = TypeNameWildcard.extendsBound(TypeName.of(Instant.class));
    GenerationTargetClassDto target = classDef("test", "Target");
    ClassFieldDto field = new ClassFieldDto();
    CodeTemplateDto code = new CodeTemplateDto();
    target.addImport(new TypeNameWildcard());
    target.addImport(wildcard);
    field.addImport(new TypeNameWildcard());
    field.addImport(wildcard);
    code.addCodeBlockImport(new TypeNameWildcard());
    code.addCodeBlockImport(wildcard);
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

  private static GenerationTargetClassDto classDef(String pkg, String name) {
    GenerationTargetClassDto classDef = new GenerationTargetClassDto();
    classDef.setTypeName(new TypeName(pkg, name));
    return classDef;
  }

  private static ClassFieldDto field(String name, String pkg, String type) {
    ClassFieldDto field = new ClassFieldDto();
    field.setFieldName(name);
    field.setFieldType(new TypeName(pkg, type));
    return field;
  }

  @Test
  void skipsJavaLangAndSamePackageTypes() {
    GenerationTargetClassDto classDef = classDef("com.example", "Dto");
    classDef.addClassField(field("samePackage", "com.example", "Sibling"));
    classDef.addClassField(field("langType", "java.lang", "String"));
    classDef.addClassField(field("needed", "java.util", "Optional"));

    assertEquals(List.of("java.util.Optional"), fqns(classDef));
  }

  @Test
  void importsSortedAlphabetically() {
    GenerationTargetClassDto classDef = classDef("com.example", "Dto");
    classDef.addClassField(field("z", "java.util", "TreeSet"));
    classDef.addClassField(field("a", "java.util", "ArrayDeque"));
    classDef.addClassField(field("m", "java.time", "Instant"));

    assertEquals(
        List.of("java.time.Instant", "java.util.ArrayDeque", "java.util.TreeSet"), fqns(classDef));
  }

  @Test
  void toleratesNullImportStatements() {
    GenerationTargetClassDto classDef = classDef("com.example", "Dto");
    classDef.addImport((ImportStatement) null);
    classDef.addClassField(field("needed", "java.util", "Optional"));

    assertEquals(List.of("java.util.Optional"), fqns(classDef));
  }

  @Test
  void genericTypeImportsRawTypeAndArguments() {
    GenerationTargetClassDto classDef = classDef("com.example", "Dto");
    classDef.addClassField(field("mapping", "com.example", "StringIntegerMap"));

    MethodDto method = new MethodDto("values", new TypeName("java.util", "Collection"));
    MethodParameterDto param = new MethodParameterDto();
    param.setParameterName("key");
    param.setParameterTypeName(new TypeName("com.other", "KeyDto"));
    method.addParameter(param);
    classDef.addMethod(method);

    assertTrue(fqns(classDef).contains("java.util.Collection"));
    assertTrue(fqns(classDef).contains("com.other.KeyDto"));
  }
}
