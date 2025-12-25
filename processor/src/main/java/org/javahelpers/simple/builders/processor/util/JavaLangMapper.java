/*
 * MIT License
 *
 * Copyright (c) 2025 Andreas Igel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.javahelpers.simple.builders.processor.util;

import static javax.lang.model.element.Modifier.DEFAULT;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.type.TypeKind.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.SimpleTypeVisitor14;
import org.javahelpers.simple.builders.processor.dtos.*;

/** Helper functions to create simple builder types from java.lang types. */
public final class JavaLangMapper {
  /** Private constructor to prevent instantiation of utility class. */
  private JavaLangMapper() {
    // Utility class
  }

  /**
   * Mapper for {@code java.util.Set<javax.lang.model.element.Modifier>} to extract the relevant
   * modifier. If there is a public modifier, this is returned. If there is a protected modifier,
   * this will be returned. Default is returned in all other cases.
   *
   * @param modifier Set of modifiers to be checked
   * @return DEFAULT, PUBLIC or PROTECTED
   */
  public static Modifier mapRelevantModifier(Set<Modifier> modifier) {
    if (modifier.contains(PUBLIC)) {
      return PUBLIC;
    } else if (modifier.contains(PROTECTED)) {
      return PROTECTED;
    }
    return DEFAULT;
  }

  /**
   * Mapping a Java-Class to a TypeName. This method does not expact sealed or annonymous classes.
   *
   * @param clazz Class to be mapped
   * @return Typename
   */
  public static TypeName map2TypeName(Class<?> clazz) {
    return new TypeName(clazz.getPackageName(), clazz.getSimpleName());
  }

  /**
   * Maps the declared type parameters of the given type element into GenericParameterDto list.
   *
   * @param type the type element whose type parameters to map
   * @param context the processing context
   * @return list of GenericParameterDto representing the type parameters
   */
  public static List<GenericParameterDto> map2GenericParameterDtos(
      TypeElement type, ProcessingContext context) {
    return type.getTypeParameters().stream()
        .map(tp -> map2GenericParameterDto(tp, context))
        .toList();
  }

  /**
   * Maps a single {@code TypeParameterElement} to {@code GenericParameterDto}.
   *
   * @param tp the type parameter element to map
   * @param context the processing context
   * @return a GenericParameterDto representing the type parameter
   */
  public static GenericParameterDto map2GenericParameterDto(
      TypeParameterElement tp, ProcessingContext context) {
    GenericParameterDto g = new GenericParameterDto();
    g.setName(tp.getSimpleName().toString());
    tp.getBounds().stream()
        .filter(b -> !"java.lang.Object".equals(b.toString()))
        .map(b -> extractType(b, context))
        .forEach(g::addUpperBound);
    return g;
  }

  /**
   * Mapping a method parameter to {@code
   * org.javahelpers.simple.builders.processor.dtos.MethodParameterDto}.
   *
   * @param param Parameter variable to be mapped
   * @param context processing context
   * @return MethodParameterDto holding the information of the param.
   */
  public static MethodParameterDto map2MethodParameter(
      VariableElement param, ProcessingContext context) {
    MethodParameterDto result = new MethodParameterDto();
    result.setParameterName(param.getSimpleName().toString());
    TypeMirror typeMirror = param.asType();
    TypeName typeName = extractType(typeMirror, context);
    if (typeName == null) {
      return null;
    }
    result.setParameterTypeName(typeName);
    return result;
  }

  /**
   * Maps a list of {@code TypeMirror} to a list of simple-builder {@code TypeName}s using {@link
   * #extractType(TypeMirror, ProcessingContext)}.
   */
  private static List<TypeName> extractTypeForList(
      List<TypeMirror> typeMirrors, ProcessingContext context) {
    List<TypeName> result = new ArrayList<>(typeMirrors.size());
    for (TypeMirror tm : typeMirrors) {
      result.add(extractType(tm, context));
    }
    return result;
  }

  /**
   * Checks if the given type implements List, Set, or Map interfaces and wraps it in the
   * appropriate specialized TypeName class.
   *
   * <p>This method correctly extracts the type arguments used by the collection interface, not the
   * class's declared type parameters. For example, {@code CustomList<X,Y> extends ArrayList<Y>}
   * will extract Y as the List element type, not both X and Y.
   *
   * @param rawType the base TypeName (preserves concrete class information)
   * @param argTypes the generic type arguments from the class declaration (may not match interface)
   * @param typeMirror the TypeMirror to check for interface implementation
   * @param context the processing context
   * @return a specialized TypeName (TypeNameList, TypeNameSet, TypeNameMap) if applicable, or a
   *     TypeNameGeneric/rawType otherwise
   */
  private static TypeName wrapInCollectionTypeIfApplicable(
      TypeName rawType, List<TypeName> argTypes, TypeMirror typeMirror, ProcessingContext context) {
    TypeElement listElement = context.getTypeElement("java.util.List");
    TypeElement setElement = context.getTypeElement("java.util.Set");
    TypeElement mapElement = context.getTypeElement("java.util.Map");

    if (listElement != null) {
      TypeMirror listType = context.erasure(listElement.asType());
      if (context.isAssignable(context.erasure(typeMirror), listType)) {
        // Check if this is the List interface itself
        TypeElement typeElement = (TypeElement) context.asElement(typeMirror);
        boolean isInterface = typeElement.equals(listElement);

        // Check if type has a constructor accepting Collection (like ArrayList(Collection))
        boolean hasConstructor = !isInterface && hasCollectionConstructor(typeElement, context);

        if (isInterface || hasConstructor) {
          // Extract the actual List element type from the interface
          List<TypeName> interfaceTypeArgs =
              extractInterfaceTypeArguments(typeMirror, listElement, context);
          TypeName elementType = interfaceTypeArgs.isEmpty() ? null : interfaceTypeArgs.get(0);
          // Store all class type parameters AND the extracted element type
          return new TypeNameList(rawType, argTypes, elementType);
        }
        // Custom List implementation without Collection constructor - treat as generic type
        return argTypes.isEmpty() ? rawType : new TypeNameGeneric(rawType, argTypes);
      }
    }

    if (setElement != null) {
      TypeMirror setType = context.erasure(setElement.asType());
      if (context.isAssignable(context.erasure(typeMirror), setType)) {
        // Check if this is the Set interface itself
        TypeElement typeElement = (TypeElement) context.asElement(typeMirror);
        boolean isInterface = typeElement.equals(setElement);

        // Check if type has a constructor accepting Collection (like HashSet(Collection))
        boolean hasConstructor = !isInterface && hasCollectionConstructor(typeElement, context);

        if (isInterface || hasConstructor) {
          // Extract the actual Set element type from the interface
          List<TypeName> interfaceTypeArgs =
              extractInterfaceTypeArguments(typeMirror, setElement, context);
          TypeName elementType = interfaceTypeArgs.isEmpty() ? null : interfaceTypeArgs.get(0);
          // Store all class type parameters AND the extracted element type
          return new TypeNameSet(rawType, argTypes, elementType);
        }
        // Custom Set implementation without Collection constructor - treat as generic type
        return argTypes.isEmpty() ? rawType : new TypeNameGeneric(rawType, argTypes);
      }
    }

    if (mapElement != null) {
      TypeMirror mapType = context.erasure(mapElement.asType());
      if (context.isAssignable(context.erasure(typeMirror), mapType)) {
        // Check if this is the Map interface itself
        TypeElement typeElement = (TypeElement) context.asElement(typeMirror);
        boolean isInterface = typeElement.equals(mapElement);

        // Check if type has a constructor accepting Map (like HashMap(Map))
        boolean hasConstructor = !isInterface && hasMapConstructor(typeElement, context);

        if (isInterface || hasConstructor) {
          // Extract the actual Map key and value types from the interface
          List<TypeName> interfaceTypeArgs =
              extractInterfaceTypeArguments(typeMirror, mapElement, context);
          TypeName keyType = interfaceTypeArgs.isEmpty() ? null : interfaceTypeArgs.get(0);
          TypeName valueType = interfaceTypeArgs.size() < 2 ? null : interfaceTypeArgs.get(1);
          // Store all class type parameters AND the extracted key/value types
          return new TypeNameMap(rawType, argTypes, keyType, valueType);
        }
        // Custom Map implementation without Map constructor - treat as generic type
        return argTypes.isEmpty() ? rawType : new TypeNameGeneric(rawType, argTypes);
      }
    }

    // Not a collection type - return generic or raw type
    return argTypes.isEmpty() ? rawType : new TypeNameGeneric(rawType, argTypes);
  }

  /**
   * Checks if a type has a constructor that accepts a Collection parameter.
   *
   * <p>This allows us to detect if we can generate helper methods for List/Set types by checking if
   * it has a constructor like {@code ArrayList(Collection<? extends E>)}.
   *
   * @param typeElement the type to check
   * @param context the processing context
   * @return true if the type has a constructor accepting Collection
   */
  private static boolean hasCollectionConstructor(
      TypeElement typeElement, ProcessingContext context) {
    TypeElement collectionElement = context.getTypeElement("java.util.Collection");
    if (collectionElement == null) {
      return false;
    }

    TypeMirror collectionType = context.erasure(collectionElement.asType());

    return typeElement.getEnclosedElements().stream()
        .filter(element -> element.getKind() == javax.lang.model.element.ElementKind.CONSTRUCTOR)
        .map(element -> (javax.lang.model.element.ExecutableElement) element)
        .anyMatch(
            constructor -> {
              if (constructor.getParameters().size() != 1) {
                return false;
              }
              TypeMirror paramType = constructor.getParameters().get(0).asType();
              TypeMirror erasedParamType = context.erasure(paramType);
              // Parameter should be Collection or a supertype (like Iterable)
              return context.isSameType(erasedParamType, collectionType)
                  || context.isAssignable(erasedParamType, collectionType);
            });
  }

  /**
   * Checks if a type has a constructor that accepts a Map parameter.
   *
   * <p>This allows us to detect if we can generate helper methods for Map types by checking if it
   * has a constructor like {@code HashMap(Map<? extends K, ? extends V>)}.
   *
   * @param typeElement the type to check
   * @param context the processing context
   * @return true if the type has a constructor accepting Map
   */
  private static boolean hasMapConstructor(TypeElement typeElement, ProcessingContext context) {
    TypeElement mapElement = context.getTypeElement("java.util.Map");
    if (mapElement == null) {
      return false;
    }

    TypeMirror mapType = context.erasure(mapElement.asType());

    return typeElement.getEnclosedElements().stream()
        .filter(element -> element.getKind() == javax.lang.model.element.ElementKind.CONSTRUCTOR)
        .map(element -> (javax.lang.model.element.ExecutableElement) element)
        .anyMatch(
            constructor -> {
              if (constructor.getParameters().size() != 1) {
                return false;
              }
              TypeMirror paramType = constructor.getParameters().get(0).asType();
              TypeMirror erasedParamType = context.erasure(paramType);
              // Parameter should be Map
              return context.isSameType(erasedParamType, mapType)
                  || context.isAssignable(erasedParamType, mapType);
            });
  }

  /**
   * Extracts the type arguments used by a specific interface from a type's supertype hierarchy.
   *
   * <p>For example, given {@code CustomList<X,Y> extends ArrayList<Y>} and the {@code List}
   * interface, this returns [Y], not [X,Y].
   *
   * @param typeMirror the type to examine
   * @param targetInterface the interface element (e.g., List, Set, Map)
   * @param context the processing context
   * @return list of type arguments used by the interface, or empty list if raw type
   */
  public static List<TypeName> extractInterfaceTypeArguments(
      TypeMirror typeMirror, TypeElement targetInterface, ProcessingContext context) {
    // Walk the supertype hierarchy to find the specific instantiation of the target interface
    TypeMirror found = findSupertype(typeMirror, targetInterface, context);

    if (found instanceof DeclaredType declaredType) {
      List<? extends TypeMirror> typeArgs = declaredType.getTypeArguments();
      if (!typeArgs.isEmpty()) {
        return extractTypeForList(new ArrayList<>(typeArgs), context);
      }
    }

    return List.of(); // Raw type
  }

  /**
   * Finds the specific supertype that matches the target interface in the type hierarchy.
   *
   * @param typeMirror the type to search from
   * @param targetInterface the interface to find
   * @param context the processing context
   * @return the matching supertype, or null if not found
   */
  private static TypeMirror findSupertype(
      TypeMirror typeMirror, TypeElement targetInterface, ProcessingContext context) {
    if (!(typeMirror instanceof DeclaredType)) {
      return null;
    }

    TypeElement typeElement = (TypeElement) context.asElement(typeMirror);
    if (typeElement.equals(targetInterface)) {
      return typeMirror;
    }

    // Check direct supertypes (superclass and interfaces)
    for (TypeMirror supertype : context.directSupertypes(typeMirror)) {
      TypeMirror found = findSupertype(supertype, targetInterface, context);
      if (found != null) {
        return found;
      }
    }

    return null;
  }

  private static TypeName extractType(TypeMirror typeOfParameter, ProcessingContext context) {
    return typeOfParameter.accept(
        new SimpleTypeVisitor14<TypeName, Void>() {

          @Override
          public TypeName visitPrimitive(PrimitiveType t, Void p) {
            return switch (t.getKind()) {
              case BOOLEAN -> TypeNamePrimitive.BOOLEAN;
              case BYTE -> TypeNamePrimitive.BYTE;
              case SHORT -> TypeNamePrimitive.SHORT;
              case INT -> TypeNamePrimitive.INT;
              case LONG -> TypeNamePrimitive.LONG;
              case CHAR -> TypeNamePrimitive.CHAR;
              case FLOAT -> TypeNamePrimitive.FLOAT;
              case DOUBLE -> TypeNamePrimitive.DOUBLE;
              default -> throw new IllegalStateException("Unsupported Primitive type");
            };
          }

          @Override
          public TypeName visitDeclared(DeclaredType t, Void p) {
            TypeElement elementOfParameter = (TypeElement) context.asElement(typeOfParameter);
            String simpleClassName = elementOfParameter.getSimpleName().toString();
            String packageName = context.getPackageName(elementOfParameter);
            TypeName rawType = new TypeName(packageName, simpleClassName);
            TypeMirror enclosingType = t.getEnclosingType();
            TypeName enclosing =
                (enclosingType.getKind() != NONE)
                        && !t.asElement().getModifiers().contains(Modifier.STATIC)
                    ? enclosingType.accept(this, null)
                    : null;
            if (t.getTypeArguments().isEmpty() && !(enclosing instanceof TypeNameGeneric)) {
              return wrapInCollectionTypeIfApplicable(rawType, List.of(), typeOfParameter, context);
            }

            List<TypeMirror> typesExtracted = new ArrayList<>(t.getTypeArguments());
            if (typesExtracted.isEmpty()) {
              return wrapInCollectionTypeIfApplicable(rawType, List.of(), typeOfParameter, context);
            } else {
              List<TypeName> argTypes = extractTypeForList(typesExtracted, context);
              return wrapInCollectionTypeIfApplicable(rawType, argTypes, typeOfParameter, context);
            }
          }

          @Override
          public TypeNameArray visitArray(ArrayType t, Void p) {
            return new TypeNameArray(extractType(t.getComponentType(), context));
          }

          @Override
          public TypeName visitTypeVariable(TypeVariable t, Void p) {
            String name = t.asElement().getSimpleName().toString();
            return new TypeNameVariable(name);
          }

          @Override
          protected TypeName defaultAction(TypeMirror e, Void p) {
            throw new IllegalArgumentException("Unexpected type mirror: " + e);
          }
        },
        null);
  }
}
