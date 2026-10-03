/*
 * Copyright 2025 Andreas Igel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.javahelpers.simple.builders.processor.model.type;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import javax.lang.model.element.TypeElement;
import org.javahelpers.simple.builders.processor.analysis.JavaLangAnalyser;
import org.javahelpers.simple.builders.processor.processing.ProcessingContext;

/**
 * The instantiation path generated code uses to obtain an instance of a resolved builder.
 *
 * <p>The resolver records the concrete decision made while checking the builder contract, so
 * generation sites emit exactly the path that was resolved - constructor call or static factory
 * call - instead of silently falling back between the two.
 */
public sealed interface BuilderInstantiation {

  /**
   * Renders the code obtaining a builder instance of {@code builderTypeExpression} invoked with
   * {@code arguments}.
   *
   * @param builderTypeExpression expression evaluating to the builder type, may be a template
   *     placeholder like {@code $helperType:T}
   * @param arguments the arguments for the constructor or factory call
   * @return the instantiation code
   */
  String instantiationCode(String builderTypeExpression, String arguments);

  /**
   * Renders a method reference obtaining a builder instance (e.g. {@code B::new} or {@code
   * B::create}).
   *
   * @param builderTypeExpression expression evaluating to the builder type, may be a template
   *     placeholder like {@code $elementBuilderType:T}
   * @return the method reference
   */
  String methodReference(String builderTypeExpression);

  /** Instantiation through a constructor: {@code new B(arguments)}. */
  record ConstructorCall() implements BuilderInstantiation {
    @Override
    public String instantiationCode(String builderTypeExpression, String arguments) {
      return "new " + builderTypeExpression + "(" + arguments + ")";
    }

    @Override
    public String methodReference(String builderTypeExpression) {
      return builderTypeExpression + "::new";
    }
  }

  /**
   * Instantiation through a static factory method on the builder: {@code B.methodName(arguments)}.
   *
   * @param methodName name of the static factory method (e.g. {@code create}, {@code of})
   */
  record StaticFactoryCall(String methodName) implements BuilderInstantiation {

    // Factory method names preferred when a builder offers several candidates
    private static final List<String> PREFERRED_NAMES = List.of("create", "of");

    /**
     * Finds the static factory creating an empty builder instance: an accessible static
     * parameterless function on the builder type returning the builder type.
     *
     * @param builderType the builder type element to inspect
     * @param context the processing context, used to access all members
     * @return the instantiation calling the found factory, or empty when none exists
     */
    public static Optional<BuilderInstantiation> forEmptyBuilder(
        TypeElement builderType, ProcessingContext context) {
      return find(builderType, List.of(), context);
    }

    /**
     * Finds the static factory creating a builder instance prefilled with a value of the referenced
     * type: an accessible static function accepting the type and returning the builder type.
     *
     * @param builderType the builder type element to inspect
     * @param expectedType the qualified name of the referenced type the factory must accept
     * @param context the processing context, used to access all members
     * @return the instantiation calling the found factory, or empty when none exists
     */
    public static Optional<BuilderInstantiation> forPrefilledBuilder(
        TypeElement builderType, String expectedType, ProcessingContext context) {
      return find(builderType, List.of(expectedType), context);
    }

    private static Optional<BuilderInstantiation> find(
        TypeElement builderType, List<String> parameterTypes, ProcessingContext context) {
      return JavaLangAnalyser.findStaticFunction(
              builderType, parameterTypes, builderType.getQualifiedName().toString(), context)
          .stream()
          .min(
              Comparator.comparingInt(StaticFactoryCall::nameRank)
                  .thenComparing(Comparator.naturalOrder()))
          .<BuilderInstantiation>map(StaticFactoryCall::new);
    }

    private static int nameRank(String name) {
      int index = PREFERRED_NAMES.indexOf(name);
      return index < 0 ? PREFERRED_NAMES.size() : index;
    }

    @Override
    public String instantiationCode(String builderTypeExpression, String arguments) {
      return builderTypeExpression + "." + methodName + "(" + arguments + ")";
    }

    @Override
    public String methodReference(String builderTypeExpression) {
      return builderTypeExpression + "::" + methodName;
    }
  }
}
