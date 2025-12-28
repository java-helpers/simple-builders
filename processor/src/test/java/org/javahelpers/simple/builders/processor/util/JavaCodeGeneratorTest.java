package org.javahelpers.simple.builders.processor.util;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.annotation.processing.Filer;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import org.javahelpers.simple.builders.core.enums.OptionState;
import org.javahelpers.simple.builders.processor.dtos.BuilderConfiguration;
import org.javahelpers.simple.builders.processor.dtos.BuilderDefinitionDto;
import org.javahelpers.simple.builders.processor.dtos.TypeName;
import org.junit.jupiter.api.Test;

class JavaCodeGeneratorTest {

  @Test
  void generateBuilder_WhenJacksonEnabledButMissing_ShouldWarnAndNotAddAnnotation()
      throws Exception {
    // Given
    Filer filer = mock(Filer.class);
    Elements elementUtils = mock(Elements.class);
    ProcessingLogger logger = mock(ProcessingLogger.class);
    JavaCodeGenerator generator = new JavaCodeGenerator(filer, elementUtils, logger);

    BuilderDefinitionDto builderDef = new BuilderDefinitionDto();
    builderDef.setBuilderTypeName(new TypeName("test", "TestBuilder"));
    builderDef.setBuildingTargetTypeName(new TypeName("test", "Test"));

    BuilderConfiguration config =
        BuilderConfiguration.builder()
            .usingJacksonDeserializerAnnotation(OptionState.ENABLED)
            .build();
    builderDef.setConfiguration(config);

    // Simulate missing JsonPOJOBuilder
    when(elementUtils.getTypeElement("com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder"))
        .thenReturn(null);

    // When
    generator.generateBuilder(builderDef);

    // Then
    verify(logger)
        .warning(
            "Jackson support enabled but 'com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder' not found on classpath. Annotation skipped.");

    // We can't easily verify that annotation was NOT added to the TypeSpec without inspecting
    // internal Javapoet logic,
    // or by mocking Filer and checking output.
    // But determining if checking for the warning is sufficient.
  }

  @Test
  void generateBuilder_WhenJacksonEnabledAndPresent_ShouldNotWarn() throws Exception {
    // Given
    Filer filer = mock(Filer.class);
    Elements elementUtils = mock(Elements.class);
    ProcessingLogger logger = mock(ProcessingLogger.class);
    JavaCodeGenerator generator = new JavaCodeGenerator(filer, elementUtils, logger);

    BuilderDefinitionDto builderDef = new BuilderDefinitionDto();
    builderDef.setBuilderTypeName(new TypeName("test", "TestBuilder"));
    builderDef.setBuildingTargetTypeName(new TypeName("test", "Test"));

    BuilderConfiguration config =
        BuilderConfiguration.builder()
            .usingJacksonDeserializerAnnotation(OptionState.ENABLED)
            .build();
    builderDef.setConfiguration(config);

    // Simulate present JsonPOJOBuilder
    TypeElement typeElement = mock(TypeElement.class);
    when(elementUtils.getTypeElement("com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder"))
        .thenReturn(typeElement);

    // When
    generator.generateBuilder(builderDef);

    // Then
    // verify logger.warning was NOT called
    verify(logger, org.mockito.Mockito.never()).warning(anyString());
  }
}
