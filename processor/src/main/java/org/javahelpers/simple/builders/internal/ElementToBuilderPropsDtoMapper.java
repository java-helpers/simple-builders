package org.javahelpers.simple.builders.internal;

import static org.javahelpers.simple.builders.internal.AnnotationValidator.validateAnnotatedElement;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

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

    for (Element e : elementUtils.getAllMembers(annotatedType)) {
      if (e.getKind() == ElementKind.FIELD) {
        result.addMember(mapFromElement((VariableElement) e, elementUtils, typeUtils));
      }
    }

    return result;
  }

  private static MemberDto mapFromElement(
      VariableElement variable, Elements elementUtils, Types typeUtils) {
    MemberDto result = new MemberDto();
    Element asElement = typeUtils.asElement(variable.asType());
    result.setMemberName(variable.getSimpleName());
    result.setHasBuilderAnnotation(false); // TODO
    result.setFullQualifiedType(asElement.getSimpleName());
    return result;
  }
}
