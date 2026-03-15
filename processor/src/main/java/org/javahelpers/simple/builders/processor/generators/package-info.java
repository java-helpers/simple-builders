/**
 * Package org.javahelpers.simple.builders.processor.generators
 *
 * <p>Generator framework for the simple-builders processor.
 *
 * <p>This package contains the core generator interfaces and registry:
 *
 * <ul>
 *   <li>{@link Generator} - Base sealed interface for all generators
 *   <li>{@link MethodGenerator} - Interface for field-level method generators
 *   <li>{@link BuilderEnhancer} - Interface for builder-level enhancers
 *   <li>{@link GeneratorRegistry} - Registry for discovering and managing generators
 *   <li>{@link ComponentFilter} - Utility for filtering generators based on configuration
 * </ul>
 *
 * <p>Generators are organized into subpackages by their scope:
 *
 * <ul>
 *   <li>field - Generators that create methods for individual fields
 *   <li>builder - Enhancers that modify the entire builder
 *   <li>util - Utility generators for common patterns
 * </ul>
 */
package org.javahelpers.simple.builders.processor.generators;
