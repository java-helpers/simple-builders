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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.javahelpers.simple.builders.core.enums.AccessModifier;
import org.javahelpers.simple.builders.core.enums.OptionState;
import org.javahelpers.simple.builders.processor.model.core.BuilderConfiguration;
import org.javahelpers.simple.builders.processor.processing.CompilerArgumentsEnum;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link CompilerArgumentsEnum} option lookup and value application. */
class CompilerArgumentsEnumTest {

  @Test
  void fromCompilerArgument_resolvesPrefixedName() {
    assertSame(
        CompilerArgumentsEnum.GENERATE_FIELD_SUPPLIER,
        CompilerArgumentsEnum.fromCompilerArgument("simplebuilder.generateFieldSupplier"));
  }

  @Test
  void fromCompilerArgument_returnsNullForUnknownOrBare() {
    assertNull(CompilerArgumentsEnum.fromCompilerArgument("unknown"));
    // bare option names are only accepted as compiler args, not as prefixed arguments
    assertNull(CompilerArgumentsEnum.fromCompilerArgument("generateFieldSupplier"));
  }

  @Test
  void fromOptionName_resolvesAndRejects() {
    assertSame(
        CompilerArgumentsEnum.BUILDER_ACCESS,
        CompilerArgumentsEnum.fromOptionName("builderAccess"));
    assertNull(CompilerArgumentsEnum.fromOptionName("doesNotExist"));
  }

  @Test
  void isBuilderOption_distinguishesConfigFromProcessFlags() {
    assertTrue(CompilerArgumentsEnum.GENERATE_FIELD_SUPPLIER.isBuilderOption());
    assertFalse(CompilerArgumentsEnum.VERBOSE.isBuilderOption());
    assertFalse(CompilerArgumentsEnum.DEACTIVATE_GENERATION_COMPONENTS.isBuilderOption());
    assertFalse(CompilerArgumentsEnum.PERFORMANCE_TRACKING.isBuilderOption());
    assertFalse(CompilerArgumentsEnum.PERFORMANCE_OUTPUT_FILE.isBuilderOption());
  }

  @Test
  void apply_optionState_acceptsEnumNameAndKeywords() {
    BuilderConfiguration.Builder builder = BuilderConfiguration.builder();
    CompilerArgumentsEnum.GENERATE_FIELD_SUPPLIER.apply(builder, "OptionState.ENABLED");
    CompilerArgumentsEnum.GENERATE_FIELD_CONSUMER.apply(builder, "false");
    BuilderConfiguration config = builder.build();
    assertEquals(OptionState.ENABLED, config.generateFieldSupplier());
    assertEquals(OptionState.DISABLED, config.generateFieldConsumer());
  }

  @Test
  void apply_accessModifier_andString() {
    BuilderConfiguration.Builder builder = BuilderConfiguration.builder();
    CompilerArgumentsEnum.BUILDER_ACCESS.apply(builder, "AccessModifier.PRIVATE");
    CompilerArgumentsEnum.BUILDER_SUFFIX.apply(builder, "Builder2");
    BuilderConfiguration config = builder.build();
    assertEquals(AccessModifier.PRIVATE, config.builderAccess());
    assertEquals("Builder2", config.builderSuffix());
  }

  @Test
  void apply_nonBuilderOption_isIgnored() {
    BuilderConfiguration.Builder builder = BuilderConfiguration.builder();
    BuilderConfiguration before = builder.build();
    CompilerArgumentsEnum.VERBOSE.apply(builder, "true");
    assertEquals(before.generateFieldSupplier(), builder.build().generateFieldSupplier());
  }
}
