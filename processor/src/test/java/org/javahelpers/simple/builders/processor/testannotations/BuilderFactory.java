package org.javahelpers.simple.builders.processor.testannotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Test annotation for CUSTOMIZING.md BuilderFactoryEnhancer example.
 *
 * <p>This annotation is used to test the custom enhancer pattern shown in the documentation.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface BuilderFactory {}
