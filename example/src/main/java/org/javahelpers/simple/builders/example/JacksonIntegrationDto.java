package org.javahelpers.simple.builders.example;

import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
import org.javahelpers.simple.builders.core.enums.OptionState;

@SimpleBuilder(
    options =
        @SimpleBuilder.Options(
            generateJacksonModule = OptionState.ENABLED,
            usingJacksonDeserializerAnnotation = OptionState.ENABLED))
public class JacksonIntegrationDto {
    private final String name;
    private final int age;

    // Protected constructor - Jackson can't access this, but the builder can
    protected JacksonIntegrationDto(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public String name() {
        return name;
    }

    public int age() {
        return age;
    }
}
