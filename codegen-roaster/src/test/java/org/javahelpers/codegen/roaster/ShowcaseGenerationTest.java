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

package org.javahelpers.codegen.roaster;

import static org.javahelpers.codegen.roaster.RoasterCodeGeneratorTest.assertGeneratedSource;

import java.util.List;
import org.javahelpers.codegen.enums.AccessModifier;
import org.javahelpers.codegen.enums.FormattingMode;
import org.javahelpers.codegen.model.core.GenerationTargetClassDto;
import org.javahelpers.codegen.model.core.GenerationTargetClassDtoBuilder;
import org.javahelpers.codegen.model.javadoc.JavadocTagDto;
import org.junit.jupiter.api.Test;

class ShowcaseGenerationTest {

  @Test
  void rendersCompleteClassBuiltInOneFluentChain() throws Exception {
    GenerationTargetClassDto profile =
        GenerationTargetClassDtoBuilder.create()
            .typeName("example.profile", "UserProfile")
            .classAccessModifier(AccessModifier.PUBLIC)
            .formattingMode(FormattingMode.NONE)
            .classJavadoc(javadoc -> javadoc.description("A user's display name and tags."))
            .add2ClassAnnotations(
                annotation ->
                    annotation
                        .annotationType(SuppressWarnings.class)
                        .members(members -> members.put("value", "\"unused\"")))
            .add2ClassFields(
                field ->
                    field
                        .fieldName("name")
                        .fieldType(String.class)
                        .visibility(AccessModifier.PRIVATE),
                field ->
                    field
                        .fieldName("tags")
                        .fieldTypeList(String.class)
                        .visibility(AccessModifier.PRIVATE))
            .add2Constructors(
                constructor ->
                    constructor
                        .visibility(AccessModifier.PUBLIC)
                        .add2Parameters(
                            parameter ->
                                parameter.parameterName("name").parameterTypeName(String.class),
                            parameter ->
                                parameter.parameterName("tags").parameterTypeNameList(String.class))
                        .methodCodeDto(
                            code ->
                                code.codeFormat(
                                        """
                                        this.$name:N = $name:N;
                                        this.$tags:N = $tags:N;
                                        """)
                                    .add2CodeArguments("name", "name")
                                    .add2CodeArguments("tags", "tags")))
            .add2Methods(
                method ->
                    method
                        .methodName("getName")
                        .modifier(AccessModifier.PUBLIC)
                        .returnType(String.class)
                        .javadoc(
                            javadoc ->
                                javadoc
                                    .description("Returns the display name.")
                                    .add2Tags(new JavadocTagDto("return", "the display name")))
                        .code("return name;"))
            .add2Imports(List.class)
            .build();

    assertGeneratedSource(
        """
        package example.profile;
        import java.util.List;
        /**
         * A user's display name and tags.
         */
        @SuppressWarnings("unused")
        public class UserProfile {
          private String name;
          private List<String> tags;

          public UserProfile(String name, List<String> tags) {
            this.name = name;
            this.tags = tags;
          }

          /**
           * Returns the display name.
           * @return the display name
           */
          public String getName() {
            return name;
          }
        }
        """,
        profile);
  }
}
