/**
 * Package org.javahelpers.simple.builders.processor.classgen.javapoet
 *
 * <p>JavaPoet-based code generation infrastructure for simple-builders.
 *
 * <p>This package contains the code generation components that use JavaPoet to generate Java source
 * code from the builder definition DTOs:
 *
 * <ul>
 *   <li>{@link JavaCodeGenerator} - Main code generator that creates builder classes
 *   <li>{@link JavapoetMapper} - Utility for mapping DTOs to JavaPoet types
 *   <li>{@link BuilderException} - Exception for code generation errors
 *   <li>{@link JavapoetMapperException} - Exception for mapping errors
 * </ul>
 *
 * <p>This package isolates JavaPoet-specific code, making it easier to replace the code generation
 * library in the future if needed.
 */
package org.javahelpers.simple.builders.processor.classgen.javapoet;
