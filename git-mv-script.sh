#!/bin/bash

# Script to properly move files using git mv to preserve history
# This script should be executed from the project root directory

echo "Starting git mv operations to preserve file history..."

# Create destination directories first
mkdir -p "processor/src/main/java/org/javahelpers/simple/builders/processor/model/core"
mkdir -p "processor/src/main/java/org/javahelpers/simple/builders/processor/model/method"
mkdir -p "processor/src/main/java/org/javahelpers/simple/builders/processor/model/type"
mkdir -p "processor/src/main/java/org/javahelpers/simple/builders/processor/model/integration"
mkdir -p "processor/src/main/java/org/javahelpers/simple/builders/processor/model/annotation"
mkdir -p "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/field"
mkdir -p "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/builder"
mkdir -p "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/helper"
mkdir -p "processor/src/main/java/org/javahelpers/simple/builders/processor/processing"
mkdir -p "processor/src/main/java/org/javahelpers/simple/builders/processor/analysis"
mkdir -p "processor/src/main/java/org/javahelpers/simple/builders/processor/validation"
mkdir -p "processor/src/main/java/org/javahelpers/simple/builders/processor/classgen/javapoet"

echo "Created destination directories"

# Move model classes from dtos to model subpackages
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/dtos/BuilderConfiguration.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/model/core/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/dtos/BuilderDefinitionDto.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/model/core/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/dtos/FieldDto.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/model/core/"

git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/dtos/MethodDto.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/model/method/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/dtos/MethodParameterDto.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/model/method/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/dtos/MethodCodeDto.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/model/method/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/dtos/MethodCodePlaceholder.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/model/method/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/dtos/MethodCodeStringPlaceholder.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/model/method/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/dtos/MethodCodeTypePlaceholder.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/model/method/"

git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/dtos/TypeName.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/model/type/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/dtos/TypeNameArray.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/model/type/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/dtos/TypeNameCollection.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/model/type/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/dtos/TypeNameGeneric.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/model/type/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/dtos/TypeNameList.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/model/type/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/dtos/TypeNameMap.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/model/type/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/dtos/TypeNamePrimitive.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/model/type/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/dtos/TypeNameSet.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/model/type/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/dtos/TypeNameVariable.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/model/type/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/dtos/GenericParameterDto.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/model/type/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/dtos/NestedTypeDto.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/model/type/"

git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/dtos/JacksonModuleDefinitionDto.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/model/integration/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/dtos/JacksonModuleEntryDto.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/model/integration/"

git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/dtos/AnnotationDto.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/model/annotation/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/dtos/InterfaceName.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/model/annotation/"

# Move generator classes to subpackages
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/BasicSetterGenerator.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/field/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/BuilderEnhancer.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/builder/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/FieldConsumerGenerator.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/field/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/ListConsumerGenerator.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/field/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/MapConsumerGenerator.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/field/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/SetConsumerGenerator.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/field/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/StringBuilderConsumerGenerator.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/field/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/AddToCollectionGenerator.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/field/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/ArrayConversionGenerator.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/field/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/ArrayBuilderConsumerGenerator.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/field/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/MethodGeneratorUtil.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/field/"

git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/StringFormatHelperGenerator.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/helper/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/OptionalHelperGenerator.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/helper/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/SupplierMethodGenerator.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/helper/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/VarArgsHelperGenerator.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/helper/"

git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/GeneratedAnnotationEnhancer.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/builder/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/BuilderImplementationAnnotationEnhancer.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/builder/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/JacksonAnnotationEnhancer.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/builder/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/InterfaceEnhancer.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/builder/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/ClassJavaDocEnhancer.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/builder/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/CoreMethodsEnhancer.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/builder/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/WithInterfaceEnhancer.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/builder/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/ConditionalEnhancer.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/builder/"

# Move util classes to appropriate packages
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/util/BuilderConfigurationReader.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/processing/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/util/BuilderDefinitionCreator.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/processing/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/util/CompilerArgumentsReader.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/processing/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/util/ProcessingContext.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/processing/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/util/ProcessingLogger.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/processing/"

git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/util/JavaLangAnalyser.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/analysis/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/util/JavaLangMapper.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/analysis/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/util/FieldAnnotationExtractor.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/analysis/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/util/TypeNameAnalyser.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/analysis/"

git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/util/AnnotationValidator.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/validation/"

git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/util/JavaCodeGenerator.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/classgen/javapoet/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/util/JavapoetMapper.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/classgen/javapoet/"

# Move exceptions
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/exceptions/BuilderException.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/classgen/javapoet/"
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/exceptions/JavapoetMapperException.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/classgen/javapoet/"

# Move enums
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/enums/CompilerArgumentsEnum.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/processing/"

# Move ComponentFilter to generators package
git mv "processor/src/main/java/org/javahelpers/simple/builders/processor/util/ComponentFilter.java" "processor/src/main/java/org/javahelpers/simple/builders/processor/generators/"

echo "All git mv operations completed!"
echo "Now you can commit these changes to preserve the file history."
