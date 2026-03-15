/**
 * Package org.javahelpers.simple.builders.processor.generators
 *
 * <p>Generator framework for the simple-builders processor.
 *
 * <p>This package contains the core generator interfaces:
 *
 * <ul>
 *   <li>{@link Generator} - Base sealed interface for all generators
 *   <li>{@link MethodGenerator} - Interface for field-level method generators
 *   <li>{@link BuilderEnhancer} - Interface for builder-level enhancers
 * </ul>
 *
 * <p>Generators are organized into subpackages by their scope:
 *
 * <ul>
 *   <li>field - Generators that create methods for individual fields
 *   <li>builder - Enhancers that modify the entire builder
 *   <li>util - Utility generators for common patterns
 *   <li>registry - Generator infrastructure and management
 *   <li>integration - Framework integration generators
 * </ul>
 */
package org.javahelpers.simple.builders.processor.generators;
