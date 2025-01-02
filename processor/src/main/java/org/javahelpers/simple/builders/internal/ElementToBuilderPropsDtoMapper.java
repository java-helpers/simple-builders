package org.javahelpers.simple.builders.internal;

import static javax.lang.model.type.TypeKind.VOID;
import static org.javahelpers.simple.builders.internal.AnnotationValidator.validateAnnotatedElement;

import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.apache.commons.lang3.StringUtils;

public class ElementToBuilderPropsDtoMapper {
  private static final String BUILDER_SUFFIX = "Builder";

  public static BuilderPropsDto extractFromElement(
      Element annotatedElement, Elements elementUtils, Types typeUtils) throws BuilderException {
    validateAnnotatedElement(annotatedElement);
    TypeElement annotatedType = (TypeElement) annotatedElement;

    BuilderPropsDto result = new BuilderPropsDto();
    result.setClazzForBuilder(null); // TODO
    result.setPackageName(elementUtils.getPackageOf(annotatedType).getQualifiedName());
    result.setBuilderClassName(annotatedElement.getSimpleName() + BUILDER_SUFFIX);

    List<? extends Element> allMembers = elementUtils.getAllMembers(annotatedType);
    List<ExecutableElement> methods = ElementFilter.methodsIn(allMembers);

    for (ExecutableElement mth : methods) {
      if (isNoMethodOfObjectClass(mth) && hasNoThrowablesDeclared(mth) && hasNoReturnValue(mth)) {
        result.addMember(mapFromElement(mth, elementUtils, typeUtils));
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

  private static MemberDto mapFromElement(
      ExecutableElement mth, Elements elementUtils, Types typeUtils) {
    MemberDto result = new MemberDto();
    result.setMemberName(mth.getSimpleName());
    List<? extends VariableElement> parameters = mth.getParameters();
    parameters.stream().map(v -> mapMethodParameter(v)).forEach(result::addParameter);
    return result;
  }

  private static BuilderParameterDto mapMethodParameter(VariableElement param) {
    BuilderParameterDto result = new BuilderParameterDto();
    result.setParameterName(param.getSimpleName());
    result.setParameterTypeName(null); // TODO
    // TODO Builder erkennen
    return result;
  }
}
