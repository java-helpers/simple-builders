package org.javahelpers.simple.builders.example;

import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
import org.javahelpers.simple.builders.core.enums.OptionState;

@SimpleBuilder(
    options =
        @SimpleBuilder.Options(
            generateJacksonModule = OptionState.ENABLED,
            usingJacksonDeserializerAnnotation = OptionState.ENABLED))
public class JacksonIntegrationDto {
    private String name;
    private int age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }
}
