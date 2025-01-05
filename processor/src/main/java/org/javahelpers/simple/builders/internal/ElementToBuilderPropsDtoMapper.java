package org.javahelpers.simple.builders.internal;

import static javax.lang.model.type.TypeKind.VOID;
import static org.javahelpers.simple.builders.internal.AnnotationValidator.validateAnnotatedElement;

import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.apache.commons.lang3.StringUtils;
import org.javahelpers.simple.builders.internal.dtos.BuilderDefinitionDto;
import org.javahelpers.simple.builders.internal.dtos.MethodDto;
import org.javahelpers.simple.builders.internal.dtos.MethodParameterDto;
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
      if (isNoMethodOfObjectClass(mth) && hasNoThrowablesDeclared(mth) && hasNoReturnValue(mth)) {
        result.addMethod(mapFromElement(mth, elementUtils, typeUtils));
      }
    }

    return result;
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

  private static MethodDto mapFromElement(
      ExecutableElement mth, Elements elementUtils, Types typeUtils) {
    MethodDto result = new MethodDto();
    result.setMemberName(mth.getSimpleName());
    List<? extends VariableElement> parameters = mth.getParameters();
    parameters.stream().map(v -> mapMethodParameter(v, elementUtils)).forEach(result::addParameter);
    return result;
  }

  private static MethodParameterDto mapMethodParameter(
      VariableElement param, Elements elementUtils) {
    MethodParameterDto result = new MethodParameterDto();
    result.setParameterName(param.getSimpleName());
    TypeMirror typeOfParameter = param.asType();
    String packageName = elementUtils.getPackageOf(param).getQualifiedName().toString();
    String simpleClassName = StringUtils.removeStart(typeOfParameter.toString(), packageName + ".");
    result.setParameterTypeName(new TypeName(packageName, simpleClassName));
    // TODO Builder erkennen
    return result;
  }
}
