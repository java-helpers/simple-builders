/**
 * Package org.javahelpers.simple.builders.processor.classgen.javapoet
 *
 * <p>JavaPoet-based code generation for simple-builders processor.
 *
 * <p>This package contains the core code generation components:
 *
 * <ul>
 *   <li>{@link JavaCodeGenerator} - Main code generator that creates builder classes
 *   <li>{@link JavapoetMapper} - Utility for mapping DTOs to JavaPoet types
 * </ul>
 *
 * <p>Exception classes are organized in separate packages:
 *
 * <ul>
 *   <li>{@link org.javahelpers.simple.builders.processor.exceptions.BuilderException} - Exception
 *       for code generation errors
 *   <li>{@link
 *       org.javahelpers.simple.builders.processor.classgen.javapoet.exceptions.JavapoetMapperException}
 *       - Exception for mapping errors
 * </ul>
 *
 * <p>This package isolates JavaPoet-specific code, making it easier to replace the code generation
 * implementation if needed.
 */
package org.javahelpers.simple.builders.processor.classgen.javapoet;
