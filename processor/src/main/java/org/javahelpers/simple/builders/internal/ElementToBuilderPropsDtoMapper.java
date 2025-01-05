package org.javahelpers.simple.builders.internal;

import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PROTECTED;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.type.TypeKind.VOID;
import static org.javahelpers.simple.builders.internal.AnnotationValidator.validateAnnotatedElement;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.apache.commons.lang3.StringUtils;
import org.javahelpers.simple.builders.annotations.BuilderForDtos;
import org.javahelpers.simple.builders.internal.dtos.BuilderDefinitionDto;
import org.javahelpers.simple.builders.internal.dtos.FieldDto;
import org.javahelpers.simple.builders.internal.dtos.MethodDto;
import org.javahelpers.simple.builders.internal.dtos.MethodParameterDto;
import org.javahelpers.simple.builders.internal.dtos.SupplierTypes;
import org.javahelpers.simple.builders.internal.dtos.TypeName;

public class ElementToBuilderPropsDtoMapper {
  private static final String BUILDER_SUFFIX = "Builder";

  public static BuilderDefinitionDto extractFromElement(
      Element annotatedElement, Elements elementUtils, Types typeUtils) throws BuilderException {
    validateAnnotatedElement(annotatedElement);
    TypeElement annotatedType = (TypeElement) annotatedElement;

    BuilderDefinitionDto result = new BuilderDefinitionDto();
    String packageName = elementUtils.getPackageOf(annotatedType).getQualifiedName().toString();
    String simpleClassName =
        StringUtils.removeStart(annotatedElement.getSimpleName().toString(), packageName + ".");
    result.setBuilderTypeName(new TypeName(packageName, simpleClassName + BUILDER_SUFFIX));
    result.setBuildingTargetTypeName(new TypeName(packageName, simpleClassName));

    List<? extends Element> allMembers = elementUtils.getAllMembers(annotatedType);
    List<ExecutableElement> methods = ElementFilter.methodsIn(allMembers);

    for (ExecutableElement mth : methods) {
      // nur public
      if (isMethodRelevantForBuilder(mth)) {
        if (isSimpleSetter(mth)) {
          result.addField(mapFieldFromElement(mth, elementUtils, typeUtils));
        } else {
          result.addMethod(mapMethodFromElement(mth, elementUtils, typeUtils));
        }
      }
    }

    return result;
  }

  private static boolean isMethodRelevantForBuilder(ExecutableElement mth) {
    return isNoMethodOfObjectClass(mth)
        && hasNoThrowablesDeclared(mth)
        && hasNoReturnValue(mth)
        && isNotPrivate(mth)
        && isNotStatic(mth);
  }

  private static boolean isNoMethodOfObjectClass(ExecutableElement mth) {
    String simpleNameOfParent = mth.getEnclosingElement().getSimpleName().toString();
    return !(StringUtils.equals("java.lang.Object", simpleNameOfParent)
        || StringUtils.equals("Object", simpleNameOfParent));
  }

  private static boolean hasNoThrowablesDeclared(ExecutableElement mth) {
    return mth.getThrownTypes().isEmpty();
  }

  private static boolean hasNoReturnValue(ExecutableElement mth) {
    return mth.getReturnType().getKind() == VOID;
  }

  private static boolean isNotPrivate(ExecutableElement mth) {
    return !mth.getModifiers().contains(PRIVATE);
  }

  private static boolean isNotStatic(ExecutableElement mth) {
    return !mth.getModifiers().contains(STATIC);
  }

  private static MethodDto mapMethodFromElement(
      ExecutableElement mth, Elements elementUtils, Types typeUtils) {
    String methodName = mth.getSimpleName().toString();
    List<? extends VariableElement> parameters = mth.getParameters();

    MethodDto result = new MethodDto();
    result.setMethodName(methodName);
    result.setModifier(mapRelevantModifier(mth.getModifiers()));
    parameters.stream()
        .map(v -> mapMethodParameter(v, elementUtils, typeUtils))
        .forEach(result::addParameter);
    return result;
  }

  private static FieldDto mapFieldFromElement(
      ExecutableElement mth, Elements elementUtils, Types typeUtils) {
    String methodName = mth.getSimpleName().toString();
    List<? extends VariableElement> parameters = mth.getParameters();

    FieldDto result = new FieldDto();
    result.setFieldName(StringUtils.uncapitalize(StringUtils.removeStart(methodName, "set")));
    result.setFieldSetterName(methodName);
    MethodParameterDto parameter = mapMethodParameter(parameters.get(0), elementUtils, typeUtils);
    result.setFieldType(parameter.getParameterType());
    result.setModifier(mapRelevantModifier(mth.getModifiers()));
    result.setSupplierType(estimatePossibleSupplier(parameters.get(0), elementUtils, typeUtils));
    parameter.getBuilderType().ifPresent(result::setFieldBuilderType);
    return result;
  }

  private static boolean isSimpleSetter(ExecutableElement mth) {
    return StringUtils.startsWith(mth.getSimpleName(), "set") && mth.getParameters().size() == 1;
  }

  private static Modifier mapRelevantModifier(Set<Modifier> modifier) {
    if (modifier.contains(PUBLIC)) {
      return PUBLIC;
    } else if (modifier.contains(PROTECTED)) {
      return PROTECTED;
    }
    return null;
  }

  private static MethodParameterDto mapMethodParameter(
      VariableElement param, Elements elementUtils, Types typeUtils) {
    MethodParameterDto result = new MethodParameterDto();
    result.setParameterName(param.getSimpleName().toString());

    TypeMirror typeOfParameter = param.asType();
    String packageName = elementUtils.getPackageOf(param).getQualifiedName().toString();
    String simpleClassName = StringUtils.removeStart(typeOfParameter.toString(), packageName + ".");
    result.setParameterTypeName(new TypeName(packageName, simpleClassName));

    Element elementOfParameter = typeUtils.asElement(typeOfParameter);
    Optional<AnnotationMirror> foundBuilderAnnotation =
        findAnnotation(elementOfParameter, BuilderForDtos.class);
    boolean isClassWithoutGenerics = StringUtils.containsNone(simpleClassName, "<");
    if (foundBuilderAnnotation.isPresent() && isClassWithoutGenerics) {
      result.setBuilderType(new TypeName(packageName, simpleClassName + BUILDER_SUFFIX));
    }
    return result;
  }

  private static SupplierTypes estimatePossibleSupplier(
      VariableElement param, Elements elementUtils, Types typeUtils) {
    TypeMirror typeMirror = param.asType();
    TypeElement typeElement = (TypeElement) typeUtils.asElement(typeMirror);
    String packageName = elementUtils.getPackageOf(typeElement).toString();

    // No supplier for primitive kinds
    if (typeMirror.getKind().isPrimitive()) {
      return SupplierTypes.NONE;
    }

    boolean isNotJavaClass = !StringUtils.startsWith(packageName, "java");
    if (hasEmptyConstructor(typeElement, elementUtils) && isNotJavaClass) {
      return SupplierTypes.FIELDTYPE_EMPTY_CONSTRUCTOR;
    }

    if (implementsInterface(typeMirror, List.class, elementUtils, typeUtils)) {
      return SupplierTypes.ARRAYLIST;
    }

    if (implementsInterface(typeMirror, Set.class, elementUtils, typeUtils)) {
      return SupplierTypes.HASHSET;
    }

    if (implementsInterface(typeMirror, Map.class, elementUtils, typeUtils)) {
      return SupplierTypes.HASHMAP;
    }

    return SupplierTypes.NONE;
  }

  private static boolean implementsInterface(
      TypeMirror typeMirror, Class<?> interfaceClass, Elements elementUtils, Types typeUtils) {
    TypeElement infType = elementUtils.getTypeElement(interfaceClass.getCanonicalName());
    DeclaredType infTypeWithWildcard =
        (interfaceClass.getTypeParameters().length == 1)
            ? typeUtils.getDeclaredType(infType, typeUtils.getWildcardType(null, null))
            : typeUtils.getDeclaredType(
                infType,
                typeUtils.getWildcardType(null, null),
                typeUtils.getWildcardType(null, null));
    return typeUtils.isAssignable(typeMirror, infTypeWithWildcard);
  }

  private static boolean hasEmptyConstructor(TypeElement typeElement, Elements elementUtils) {
    List<ExecutableElement> constructors =
        ElementFilter.constructorsIn(elementUtils.getAllMembers(typeElement));
    return constructors.stream().anyMatch(c -> c.getParameters().isEmpty());
  }

  private static Optional<AnnotationMirror> findAnnotation(
      Element abstractMethodElement, Class<? extends Annotation> annotationClass) {

    for (AnnotationMirror annotationMirror : abstractMethodElement.getAnnotationMirrors()) {
      if (annotationMirror
          .getAnnotationType()
          .toString()
          .equals(annotationClass.getCanonicalName())) {
        return Optional.of(annotationMirror);
      }
    }

    return Optional.empty();
  }
}
