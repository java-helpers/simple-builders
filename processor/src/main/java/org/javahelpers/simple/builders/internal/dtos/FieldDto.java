package org.javahelpers.simple.builders.internal.dtos;

import java.util.Optional;
import javax.lang.model.element.Modifier;

public class FieldDto {
  private Optional<Modifier> modifier = Optional.empty();
  private String fieldName;
  private String fieldSetterName;
  private TypeName fieldType;
  private TypeName fieldBuilderType;

  public String getFieldName() {
    return fieldName;
  }

  public void setFieldName(String fieldName) {
    this.fieldName = fieldName;
  }

  public String getFieldSetterName() {
    return fieldSetterName;
  }

  public void setFieldSetterName(String fieldSetterName) {
    this.fieldSetterName = fieldSetterName;
  }

  public TypeName getFieldType() {
    return fieldType;
  }

  public void setFieldType(TypeName fieldType) {
    this.fieldType = fieldType;
  }

  public Optional<Modifier> getModifier() {
    return modifier;
  }

  public void setModifier(Modifier modifier) {
    this.modifier = Optional.ofNullable(modifier);
  }

  public Optional<TypeName> getFieldBuilderType() {
    return Optional.ofNullable(fieldBuilderType);
  }

  public void setFieldBuilderType(TypeName fieldBuilderType) {
    this.fieldBuilderType = fieldBuilderType;
  }
}
