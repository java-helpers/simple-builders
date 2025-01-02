package org.javahelpers.simple.builders.processor;

import static org.javahelpers.simple.builders.internal.ElementToBuilderPropsDtoMapper.extractFromElement;

import com.google.auto.service.AutoService;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.javahelpers.simple.builders.annotations.BuilderForDtos;
import org.javahelpers.simple.builders.internal.BuilderException;
import org.javahelpers.simple.builders.internal.BuilderPropsDto;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@SupportedAnnotationTypes("org.javahelpers.simple.builders.annotations.BuilderForDtos")
public class BuilderProcessor extends AbstractProcessor {
  private Types typeUtils;
  private Elements elementUtils;
  private Filer filer;
  private Messager messager;

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    this.typeUtils = processingEnv.getTypeUtils();
    this.elementUtils = processingEnv.getElementUtils();
    this.filer = processingEnv.getFiler();
    this.messager = processingEnv.getMessager();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(BuilderForDtos.class)) {
      try {
        process(annotatedElement);
      } catch (BuilderException ex) {
        // TODO Logging
      }
    }
    return true;
  }

  private void process(Element annotatedElement) throws BuilderException {

    BuilderPropsDto builderProbs = extractFromElement(annotatedElement, elementUtils, typeUtils);
  }
}
