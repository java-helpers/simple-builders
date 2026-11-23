/*
 * MIT License
 *
 * Copyright (c) 2025-2026 Andreas Igel
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
package org.javahelpers.codegen.analysis;

import java.util.ArrayList;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.SimpleTypeVisitor14;
import org.apache.commons.lang3.Strings;
import org.javahelpers.codegen.model.annotation.AnnotationDto;
import org.javahelpers.codegen.model.method.MethodParameterDto;
import org.javahelpers.codegen.model.type.GenericParameterDto;
import org.javahelpers.codegen.model.type.TypeName;
import org.javahelpers.codegen.model.type.TypeNameArray;
import org.javahelpers.codegen.model.type.TypeNameGeneric;
import org.javahelpers.codegen.model.type.TypeNameList;
import org.javahelpers.codegen.model.type.TypeNameMap;
import org.javahelpers.codegen.model.type.TypeNameNested;
import org.javahelpers.codegen.model.type.TypeNamePrimitive;
import org.javahelpers.codegen.model.type.TypeNameSet;
import org.javahelpers.codegen.model.type.TypeNameVariable;
import org.javahelpers.codegen.model.type.TypeNameWildcard;

/** Helper functions to create codegen {@link TypeName}s from java.lang types. */
public final class JavaLangMapper {
  /** Private constructor to prevent instantiation of utility class. */
  private JavaLangMapper() {
    // Utility class
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
   * @param context the type mapping context
   * @return list of GenericParameterDto representing the type parameters
   */
  public static List<GenericParameterDto> map2GenericParameterDtos(
      TypeElement type, TypeMappingContext context) {
    return type.getTypeParameters().stream()
        .map(tp -> map2GenericParameterDto(tp, context))
        .toList();
  }

  /**
   * Maps a single {@code TypeParameterElement} to {@code GenericParameterDto}.
   *
   * @param tp the type parameter element to map
   * @param context the type mapping context
   * @return a GenericParameterDto representing the type parameter
   */
  public static GenericParameterDto map2GenericParameterDto(
      TypeParameterElement tp, TypeMappingContext context) {
    GenericParameterDto g = new GenericParameterDto();
    g.setName(tp.getSimpleName().toString());
    tp.getBounds().stream()
        .filter(b -> !"java.lang.Object".equals(b.toString()))
        .map(b -> extractType(b, context))
        .forEach(g::addUpperBound);
    return g;
  }

  /**
   * Mapping a method parameter to {@code org.javahelpers.codegen.model.method.MethodParameterDto}.
   *
   * @param param Parameter variable to be mapped
   * @param context the type mapping context
   * @return MethodParameterDto holding the information of the param.
   */
  public static MethodParameterDto map2MethodParameter(
      VariableElement param, TypeMappingContext context) {
    MethodParameterDto result = new MethodParameterDto();
    result.setParameterName(param.getSimpleName().toString());
    TypeMirror typeMirror = param.asType();
    TypeName typeName = extractType(typeMirror, context);
    if (typeName == null) {
      return null;
    }

    setEmptyConstructorInfo(typeName, param, context);

    result.setParameterTypeName(typeName);
    return result;
  }

  /**
   * Maps a {@code TypeElement} to its {@code TypeName} holding package and simple name only,
   * without resolving target or constructor information.
   *
   * @param typeElement the TypeElement to map
   * @param context the type mapping context
   * @return TypeName of the type element, or null if the type element is null
   */
  public static TypeName mapToTypeName(TypeElement typeElement, TypeMappingContext context) {
    if (typeElement == null) {
      return null;
    }
    String packageName = context.getPackageName(typeElement);
    String qualifiedName = typeElement.getQualifiedName().toString();
    String className = Strings.CS.removeStart(qualifiedName, packageName + ".");
    return new TypeName(packageName, className);
  }

  /**
   * Maps a {@code TypeElement} to a codegen {@code TypeName}.
   *
   * @param typeElement the TypeElement to map
   * @param context the type mapping context
   * @return TypeName holding the information of the type element, or null if mapping fails
   */
  public static TypeName map2TypeName(TypeElement typeElement, TypeMappingContext context) {
    if (typeElement == null) {
      return null;
    }

    TypeMirror typeMirror = typeElement.asType();
    TypeName typeName = extractType(typeMirror, context);

    if (typeName != null) {
      setEmptyConstructorInfo(typeName, typeElement, context);
    }

    return typeName;
  }

  /**
   * Sets empty constructor info for concrete classes with empty constructors.
   *
   * @param typeName the TypeName to enhance
   * @param typeElement the type element to check
   * @param context the type mapping context
   */
  private static void setEmptyConstructorInfo(
      TypeName typeName, TypeElement typeElement, TypeMappingContext context) {
    // Non-static inner classes need an enclosing instance, so they are never directly instantiable.
    if (!typeName.hasEmptyConstructor()
        && isConcreteClass(typeElement)
        && !(isMember(typeElement) && isNotStatic(typeElement))
        && !TypeNameAnalyser.isJavaClass(typeName)
        && JavaLangAnalyser.hasEmptyConstructor(typeElement, context)) {
      typeName.setHasEmptyConstructor(true);
    }
  }

  /**
   * Creates a derived TypeName by appending a suffix to the simple name of the given TypeElement,
   * placed in the same package. Useful for generating companion types such as builders, serializers
   * or mappers from an annotated source type.
   *
   * @param typeElement the type element to derive the name from
   * @param context the type mapping context
   * @param suffix the class name suffix to append (e.g. "Builder", "Serializer")
   * @return the derived TypeName
   */
  public static TypeName createDerivedTypeName(
      TypeElement typeElement, TypeMappingContext context, String suffix) {
    String builderClassName = typeElement.getSimpleName().toString() + suffix;
    String packageName = context.getPackageName(typeElement);
    return new TypeName(packageName, builderClassName);
  }

  /**
   * Sets builder and constructor information on the TypeName.
   *
   * @param typeName the TypeName to enhance with builder/constructor info
   * @param param the VariableElement representing the parameter
   * @param context the type mapping context
   */
  private static void setEmptyConstructorInfo(
      TypeName typeName, VariableElement param, TypeMappingContext context) {
    Element element = context.asElement(param.asType());
    if (element instanceof TypeElement typeElement) {
      setEmptyConstructorInfo(typeName, typeElement, context);
    }
  }

  /**
   * Builds a {@link TypeName} for a type element, representing nested classes as {@link
   * TypeNameNested} so the enclosing-class qualifier is kept structurally (e.g. {@code Outer.Inner}
   * or deeper chains like {@code Outer.Middle.Inner}).
   *
   * @param typeElement the type element to map
   * @param packageName the package of the outermost enclosing type
   * @return TypeName or TypeNameNested for nested classes
   */
  private static TypeName toTypeName(TypeElement typeElement, String packageName) {
    String simpleName = typeElement.getSimpleName().toString();
    Element enclosingElement = typeElement.getEnclosingElement();
    if (enclosingElement instanceof TypeElement enclosingType) {
      return new TypeNameNested(toTypeName(enclosingType, packageName), simpleName);
    }
    return new TypeName(packageName, simpleName);
  }

  private static boolean isMember(TypeElement element) {
    return element.getNestingKind() == NestingKind.MEMBER;
  }

  private static boolean isNotStatic(TypeElement element) {
    return !element.getModifiers().contains(Modifier.STATIC);
  }

  private static TypeName toTypeName(DeclaredType type, TypeMappingContext context) {
    TypeElement element = (TypeElement) type.asElement();
    if (isMember(element) && isNotStatic(element)) {
      return new TypeNameNested(
          extractType(type.getEnclosingType(), context), element.getSimpleName().toString());
    }
    return toTypeName(element, context.getPackageName(element));
  }

  /**
   * Maps a list of {@code TypeMirror} to a list of codegen {@code TypeName}s using {@link
   * #extractType(TypeMirror, TypeMappingContext)}.
   */
  private static List<TypeName> extractTypeForList(
      List<TypeMirror> typeMirrors, TypeMappingContext context) {
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
   * @param context the type mapping context
   * @return a specialized TypeName (TypeNameList, TypeNameSet, TypeNameMap) if applicable, or a
   *     TypeNameGeneric/rawType otherwise
   */
  private static TypeName wrapInCollectionTypeIfApplicable(
      TypeName rawType,
      List<TypeName> argTypes,
      TypeMirror typeMirror,
      TypeMappingContext context) {
    TypeName listWrapper = tryWrapAsList(rawType, argTypes, typeMirror, context);
    if (listWrapper != null) {
      return listWrapper;
    }

    TypeName setWrapper = tryWrapAsSet(rawType, argTypes, typeMirror, context);
    if (setWrapper != null) {
      return setWrapper;
    }

    TypeName mapWrapper = tryWrapAsMap(rawType, argTypes, typeMirror, context);
    if (mapWrapper != null) {
      return mapWrapper;
    }

    // Not a collection type - return generic or raw type
    return createFallbackTypeName(rawType, argTypes);
  }

  /**
   * Attempts to wrap the type as a TypeNameList if it implements List and has appropriate
   * constructor.
   *
   * @return TypeNameList if applicable, null otherwise
   */
  private static TypeName tryWrapAsList(
      TypeName rawType,
      List<TypeName> argTypes,
      TypeMirror typeMirror,
      TypeMappingContext context) {
    TypeElement listElement = context.getTypeElement("java.util.List");
    if (listElement == null) {
      return null;
    }

    TypeMirror listType = context.erasure(listElement.asType());
    if (!context.isAssignable(context.erasure(typeMirror), listType)) {
      return null;
    }

    TypeElement typeElement = (TypeElement) context.asElement(typeMirror);
    if (!shouldWrapAsCollectionType(typeElement, listElement, context)) {
      return createFallbackTypeName(rawType, argTypes);
    }

    List<TypeName> interfaceTypeArgs =
        extractInterfaceTypeArguments(typeMirror, listElement, context);
    TypeName elementType = interfaceTypeArgs.isEmpty() ? null : interfaceTypeArgs.get(0);
    return new TypeNameList(rawType, argTypes, elementType);
  }

  /**
   * Attempts to wrap the type as a TypeNameSet if it implements Set and has appropriate
   * constructor.
   *
   * @return TypeNameSet if applicable, null otherwise
   */
  private static TypeName tryWrapAsSet(
      TypeName rawType,
      List<TypeName> argTypes,
      TypeMirror typeMirror,
      TypeMappingContext context) {
    TypeElement setElement = context.getTypeElement("java.util.Set");
    if (setElement == null) {
      return null;
    }

    TypeMirror setType = context.erasure(setElement.asType());
    if (!context.isAssignable(context.erasure(typeMirror), setType)) {
      return null;
    }

    TypeElement typeElement = (TypeElement) context.asElement(typeMirror);
    if (!shouldWrapAsCollectionType(typeElement, setElement, context)) {
      return createFallbackTypeName(rawType, argTypes);
    }

    List<TypeName> interfaceTypeArgs =
        extractInterfaceTypeArguments(typeMirror, setElement, context);
    TypeName elementType = interfaceTypeArgs.isEmpty() ? null : interfaceTypeArgs.get(0);
    return new TypeNameSet(rawType, argTypes, elementType);
  }

  /**
   * Attempts to wrap the type as a TypeNameMap if it implements Map and has appropriate
   * constructor.
   *
   * @return TypeNameMap if applicable, null otherwise
   */
  private static TypeName tryWrapAsMap(
      TypeName rawType,
      List<TypeName> argTypes,
      TypeMirror typeMirror,
      TypeMappingContext context) {
    TypeElement mapElement = context.getTypeElement("java.util.Map");
    if (mapElement == null) {
      return null;
    }

    TypeMirror mapType = context.erasure(mapElement.asType());
    if (!context.isAssignable(context.erasure(typeMirror), mapType)) {
      return null;
    }

    TypeElement typeElement = (TypeElement) context.asElement(typeMirror);
    boolean isInterface = typeElement.equals(mapElement);
    boolean hasConstructor =
        !isInterface && hasConstructorWithParameterOfType(typeElement, "java.util.Map", context);

    if (!isInterface && !hasConstructor) {
      return createFallbackTypeName(rawType, argTypes);
    }

    List<TypeName> interfaceTypeArgs =
        extractInterfaceTypeArguments(typeMirror, mapElement, context);
    TypeName keyType = interfaceTypeArgs.isEmpty() ? null : interfaceTypeArgs.get(0);
    TypeName valueType = interfaceTypeArgs.size() < 2 ? null : interfaceTypeArgs.get(1);
    return new TypeNameMap(rawType, argTypes, keyType, valueType);
  }

  /**
   * Checks if a type should be wrapped as a specialized collection type (List or Set).
   *
   * @param typeElement the type to check
   * @param interfaceElement the collection interface (List or Set)
   * @param context the type mapping context
   * @return true if the type is the interface itself or has a Collection constructor
   */
  private static boolean shouldWrapAsCollectionType(
      TypeElement typeElement, TypeElement interfaceElement, TypeMappingContext context) {
    boolean isInterface = typeElement.equals(interfaceElement);
    boolean hasConstructor =
        !isInterface
            && hasConstructorWithParameterOfType(typeElement, "java.util.Collection", context);
    return isInterface || hasConstructor;
  }

  /**
   * Creates a fallback TypeName when a type cannot be wrapped as a specialized collection type.
   *
   * @param rawType the raw type
   * @param argTypes the type arguments
   * @return TypeNameGeneric if argTypes is not empty, otherwise rawType
   */
  private static TypeName createFallbackTypeName(TypeName rawType, List<TypeName> argTypes) {
    return argTypes.isEmpty() ? rawType : new TypeNameGeneric(rawType, argTypes);
  }

  /**
   * Checks if a type has a constructor that accepts a parameter of the specified type.
   *
   * <p>This allows us to detect if we can generate helper methods for collection/map types by
   * checking if it has a constructor like {@code ArrayList(Collection<? extends E>)} or {@code
   * HashMap(Map<? extends K, ? extends V>)}.
   *
   * @param typeElement the type to check
   * @param parameterTypeName the fully qualified name of the parameter type (e.g.,
   *     "java.util.Collection")
   * @param context the type mapping context
   * @return true if the type has a constructor accepting the specified parameter type
   */
  private static boolean hasConstructorWithParameterOfType(
      TypeElement typeElement, String parameterTypeName, TypeMappingContext context) {
    TypeElement parameterElement = context.getTypeElement(parameterTypeName);
    if (parameterElement == null) {
      return false;
    }

    TypeMirror parameterType = context.erasure(parameterElement.asType());

    return typeElement.getEnclosedElements().stream()
        .filter(element -> element.getKind() == javax.lang.model.element.ElementKind.CONSTRUCTOR)
        .map(element -> (javax.lang.model.element.ExecutableElement) element)
        .filter(context::isAccessibleFromGeneratedPackage)
        .anyMatch(
            constructor -> {
              if (constructor.getParameters().size() != 1) {
                return false;
              }
              TypeMirror constructorParamType = constructor.getParameters().get(0).asType();
              TypeMirror erasedConstructorParamType = context.erasure(constructorParamType);
              // Parameter should match the specified type or be assignable to it
              return context.isSameType(erasedConstructorParamType, parameterType)
                  || context.isAssignable(erasedConstructorParamType, parameterType);
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
   * @param context the type mapping context
   * @return list of type arguments used by the interface, or empty list if raw type
   */
  public static List<TypeName> extractInterfaceTypeArguments(
      TypeMirror typeMirror, TypeElement targetInterface, TypeMappingContext context) {
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
   * @param context the type mapping context
   * @return the matching supertype, or null if not found
   */
  private static TypeMirror findSupertype(
      TypeMirror typeMirror, TypeElement targetInterface, TypeMappingContext context) {
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

  private static TypeName extractType(TypeMirror typeOfParameter, TypeMappingContext context) {
    TypeName typeName =
        typeOfParameter.accept(
            new SimpleTypeVisitor14<TypeName, Void>() {

              @Override
              public TypeName visitPrimitive(PrimitiveType t, Void p) {
                return switch (t.getKind()) {
                  case BOOLEAN ->
                      new TypeNamePrimitive(TypeNamePrimitive.PrimitiveTypeEnum.BOOLEAN);
                  case BYTE -> new TypeNamePrimitive(TypeNamePrimitive.PrimitiveTypeEnum.BYTE);
                  case SHORT -> new TypeNamePrimitive(TypeNamePrimitive.PrimitiveTypeEnum.SHORT);
                  case INT -> new TypeNamePrimitive(TypeNamePrimitive.PrimitiveTypeEnum.INT);
                  case LONG -> new TypeNamePrimitive(TypeNamePrimitive.PrimitiveTypeEnum.LONG);
                  case CHAR -> new TypeNamePrimitive(TypeNamePrimitive.PrimitiveTypeEnum.CHAR);
                  case FLOAT -> new TypeNamePrimitive(TypeNamePrimitive.PrimitiveTypeEnum.FLOAT);
                  case DOUBLE -> new TypeNamePrimitive(TypeNamePrimitive.PrimitiveTypeEnum.DOUBLE);
                  default -> throw new IllegalStateException("Unsupported Primitive type");
                };
              }

              @Override
              public TypeName visitDeclared(DeclaredType t, Void p) {
                TypeElement elementOfParameter = (TypeElement) context.asElement(typeOfParameter);
                TypeName rawType = toTypeName(t, context);
                if (elementOfParameter.getKind() == ElementKind.ENUM) {
                  rawType.setEnumType(true);
                }
                List<? extends TypeMirror> typeArguments = t.getTypeArguments();
                if (typeArguments.isEmpty()) {
                  return wrapInCollectionTypeIfApplicable(
                      rawType, List.of(), typeOfParameter, context);
                }
                List<TypeName> argTypes =
                    extractTypeForList(new ArrayList<>(typeArguments), context);
                return wrapInCollectionTypeIfApplicable(
                    rawType, argTypes, typeOfParameter, context);
              }

              @Override
              public TypeNameArray visitArray(ArrayType t, Void p) {
                return TypeNameArray.of(extractType(t.getComponentType(), context));
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

              @Override
              public TypeName visitWildcard(WildcardType t, Void p) {
                if (t.getExtendsBound() != null) {
                  return TypeNameWildcard.extendsBound(extractType(t.getExtendsBound(), context));
                }
                if (t.getSuperBound() != null) {
                  return TypeNameWildcard.superBound(extractType(t.getSuperBound(), context));
                }
                return new TypeNameWildcard();
              }
            },
            null);

    if (typeName != null && context.shouldCopyTypeAnnotations()) {
      List<AnnotationDto> annotations =
          FieldAnnotationExtractor.extractAnnotations(typeOfParameter, context);
      annotations.forEach(typeName::addAnnotation);
    }

    return typeName;
  }

  /**
   * Checks if the type element represents a concrete class (not abstract).
   *
   * @param typeElement the type element to check
   * @return true if it's a concrete class
   */
  private static boolean isConcreteClass(TypeElement typeElement) {
    return typeElement != null
        && typeElement.getKind() == javax.lang.model.element.ElementKind.CLASS
        && !typeElement.getModifiers().contains(javax.lang.model.element.Modifier.ABSTRACT);
  }
}
