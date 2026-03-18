package org.javahelpers.simple.builders.example;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.module.SimpleModule;

public class SimpleBuildersJacksonModule extends SimpleModule {

  @JsonDeserialize(builder = JacksonIntegrationDtoBuilder.class)
  private interface JacksonIntegrationDtoMixin {
  }

  public SimpleBuildersJacksonModule() {
    setMixInAnnotation(JacksonIntegrationDto.class, JacksonIntegrationDtoMixin.class);
  }
}