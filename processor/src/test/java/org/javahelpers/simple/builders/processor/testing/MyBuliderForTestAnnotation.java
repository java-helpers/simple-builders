package org.javahelpers.simple.builders.processor.testing;

import java.lang.annotation.*;
import org.javahelpers.simple.builders.core.annotations.SimpleBuilder;
import org.javahelpers.simple.builders.core.enums.OptionState;

@SimpleBuilder.Template(
    options =
        @SimpleBuilder.Options(
            generateFieldSupplier = OptionState.DISABLED,
            generateFieldConsumer = OptionState.DISABLED,
            generateBuilderConsumer = OptionState.DISABLED,
            generateVarArgsHelpers = OptionState.DISABLED,
            generateConditionalHelper = OptionState.DISABLED,
            generateWithInterface = OptionState.DISABLED,
            builderSuffix = "MiniBuilder",
            setterSuffix = "set"))
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.TYPE)
public @interface MyBuliderForTestAnnotation {}
