# Simple Builders - Java builder generation just on compile

[![License](https://img.shields.io/badge/License-MIT%202.0-yellowgreen.svg)](https://github.com/java-helpers/simple-builders/blob/main/LICENSE.txt)

* [What is Simple Builders?](#what-is-simple-builders)
* [Requirements](#requirements)
* [Using Simple Builders](#using-simple-builders)
  * [Configuration with Maven](#configuration-with-maven)
* [Used libraries](#used-libraries)
* [Links](#links)
* [Licensing](#licensing)

## What is Simple Builders?

Simple Builders is a Java [annotation processor](https://docs.oracle.com/en/java/javase/17/docs/specs/man/javac.html#annotation-processing) designed to generate type-safe and high-performance builders for Java classes.

## Requirements

Simple Builders requires Java 17 or later.

## Using Simple Builders

For generation of a builder for a class, just add the `SimpleBuilder` annotation to it:

```java
@SimpleBuilder
public class PersonDto {

    private String name;
    private List<String> nickNames;
    private MannschaftDto mannschaft;

    public void setName(String name) {
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public void setNicKNames(List<String> nickNames) {
        this.nickNames = nickNames;
    }

    public List<String> getNicKNames() {
        return this.nickNames;
    }

    public void setMannschaft(MannschaftDto mannschaft) {
        this.mannschaft = mannschaft;
    }

    public MannschaftDto getMannschaft(){
        return this.mannschaft;
    }
}
```

At compile time Simple Builders will generate a source code file for building this DTO. The generated builder is using the setters of the original DTO to modify the values of the target objects. There is no reflection used on runtime. 

Result of generated builder for the example above:
```java
/**
 * Builder for {@code org.javahelpers.simple.builders.example.PersonDto}.
 */
@Generated("Generated by org.javahelpers.simple.builders.processor.BuilderProcessor")
@BuilderImplementation(
    forClass = PersonDto.class
)
class PersonDtoBuilder implements IBuilderBase<PersonDto> {
  /**
   * Inner instance of builder.
   */
  private final PersonDto instance;

  /**
   * Initialisation of builder for {@code org.javahelpers.simple.builders.example.PersonDto} by a instance.
   * 
   * @param instance object instance for initialisiation
   */
  public PersonDtoBuilder(PersonDto instance) {
    this.instance = instance;
  }

  /**
   * Empty constructor of builder for {@code org.javahelpers.simple.builders.example.PersonDto}.
   */
  public PersonDtoBuilder() {
    this.instance = new PersonDto();
  }

  /**
   * Calling <code>setName</code> on dto-instance with parameters.
   * 
   * @param name value for name.
   * @return current instance of builder
   */
  public PersonDtoBuilder name(String name) {
    instance.setName(name);
    return this;
  }

  /**
   * Calling <code>setName</code> on instance with value of supplier.
   * 
   * @param nameSupplier supplier for name.
   * @return current instance of builder
   */
  public PersonDtoBuilder name(Supplier<String> nameSupplier) {
      instance.setName(nameSupplier.get());
      return this;
  }

  /**
   * Calling <code>setNickNames</code> on dto-instance with parameters.
   * 
   * @param nickNames value for nickNames.
   * @return current instance of builder
   */
  public PersonDtoBuilder nickNames(List<String> nickNames) {
    instance.setNickNames(nickNames);
    return this;
  }

  /**
   * Calling <code>setNickNames</code> on dto-instance with builder result value.
   * 
   * @param nickNamesBuilderConsumer consumer providing instance of a builder for field <code>nickNames</code>.
   * @return current instance of builder
   */
  public PersonDtoBuilder nickNames(Consumer<ArrayListBuilder<String>> nickNamesBuilderConsumer) {
    ArrayListBuilder<String> builder = new ArrayListBuilder<>();
    nickNamesBuilderConsumer.accept(builder);
    instance.setNickNames(builder.build());
    return this;
  }

  /**
   * Calling <code>setNickNames</code> on instance with value of supplier.
   * 
   * @param nickNamesSupplier supplier for nickNames.
   * @return current instance of builder
   */
  public PersonDtoBuilder nickNames(Supplier<List<String>> nickNamesSupplier) {
      instance.setNickNames(nickNamesSupplier.get());
      return this;
  }

  /**
   * Calling <code>setMannschaft</code> on dto-instance with parameters.
   * 
   * @param mannschaft value for mannschaft.
   * @return current instance of builder
   */
  public PersonDtoBuilder mannschaft(MannschaftDto mannschaft) {
    instance.setMannschaft(mannschaft);
    return this;
  }

  /**
   * Calling <code>setMannschaft</code> on dto-instance with builder result value.
   * 
   * @param mannschaftBuilderConsumer consumer providing instance of a builder for field <code>mannschaft</code>.
   * @return current instance of builder
   */
  public PersonDtoBuilder mannschaft(Consumer<MannschaftDtoBuilder> mannschaftBuilderConsumer) {
    MannschaftDtoBuilder builder = new MannschaftDtoBuilder();
    mannschaftBuilderConsumer.accept(builder);
    instance.setMannschaft(builder.build());
    return this;
  }

  /**
   * Calling <code>setMannschaft</code> on instance with value of supplier.
   * 
   * @param mannschaftSupplier supplier for field <code>mannschaft</code>.
   * @return current instance of builder
   */
  public PersonDtoBuilder mannschaft(Supplier<MannschaftDto> mannschaftSupplier) {
      instance.setMannschaft(mannschaftSupplier.get());
      return this;
  }

  @Override
  public PersonDto build() {
    return instance;
  }

  /**
   * Creating a new builder for {@code org.javahelpers.simple.builders.example.PersonDto}.
   * 
   * @return builder for {@code org.javahelpers.simple.builders.example.PersonDto}
   */
  public static PersonDtoBuilder create() {
    PersonDto instance = new PersonDto();
    return new PersonDtoBuilder(instance);
  }
}
```

### Configuration with Maven

For Maven-based projects, add the following to your POM file in order to use MapStruct (the dependencies are available at Maven Central):

```xml
...
<properties>
    <org.simple-builders.version>0.1.0</org.simple-builders.version>
</properties>
...
<dependencies>
    <dependency>
        <groupId>org.javahelpers</groupId>
        <artifactId>simple-builders-annotations</artifactId>
        <version>${org.simple-builders.version}</version>
    </dependency>
</dependencies>
...
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.13.0</version>
            <configuration>
                <source>17</source>
                <target>17</target>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.javahelpers</groupId>
                        <artifactId>simple-builders-processor</artifactId>
                        <version>${org.simple-builders.version}</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
...
```

If you don't work with a dependency management tool, you can obtain a distribution bundle from [Releases page](https://github.com/java-helpers/simple-builders/releases).

## Used libraries

* [Apache Common Lang](https://github.com/apache/commons-lang)
* [Plantir's Javapoet](https://github.com/palantir/javapoet)
* [Google's AutoService](https://github.com/google/auto/tree/main/service)

## Links

* [Source code](https://github.com/java-helpers/simple-builders/)
* [Downloads](https://github.com/java-helpers/simple-builders/releases)
* [Issue tracker](https://github.com/java-helpers/simple-builders/issues)
* [CI build](https://github.com/java-helpers/simple-builders/actions/)

## Licensing

MapStruct is licensed under the MIT License; you may not use this project except in compliance with the License. You may obtain a copy of the License at http://www.opensource.org/licenses/mit-license.php.
