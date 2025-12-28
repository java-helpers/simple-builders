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
package org.javahelpers.simple.builders.processor;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils.loadGeneratedSource;

import com.google.testing.compile.Compilation;
import javax.tools.JavaFileObject;
import org.javahelpers.simple.builders.processor.testing.ProcessorAsserts;
import org.javahelpers.simple.builders.processor.testing.ProcessorTestUtils;
import org.junit.jupiter.api.Test;

/**
 * Tests for sealed types support (Java 17+).
 *
 * <p>Verifies that builders are generated correctly for classes that:
 *
 * <ul>
 *   <li>Implement sealed interfaces
 *   <li>Extend sealed abstract classes
 *   <li>Are part of sealed type hierarchies with multiple implementations
 * </ul>
 *
 * @see <a href="https://github.com/java-helpers/simple-builders/issues/95">Issue #95</a>
 */
class SealedTypesTest {

  private static Compilation compile(JavaFileObject... sources) {
    return ProcessorTestUtils.createCompiler().compile(sources);
  }

  @Test
  void classImplementingSealedInterface_builderGenerated() {
    JavaFileObject sealedInterface =
        ProcessorTestUtils.forSource(
            """
            package test;
            public sealed interface Vehicle permits Car, Truck {
              String getType();
            }
            """);

    JavaFileObject carClass =
        ProcessorTestUtils.forSource(
            """
            package test;
            @org.javahelpers.simple.builders.core.annotations.SimpleBuilder
            public final class Car implements Vehicle {
              private final String brand;
              private final int year;

              public Car(String brand, int year) {
                this.brand = brand;
                this.year = year;
              }

              public String getBrand() { return brand; }
              public int getYear() { return year; }

              @Override
              public String getType() { return "Car"; }
            }
            """);

    JavaFileObject truckClass =
        ProcessorTestUtils.forSource(
            """
            package test;
            public final class Truck implements Vehicle {
              private final int capacity;

              public Truck(int capacity) {
                this.capacity = capacity;
              }

              public int getCapacity() { return capacity; }

              @Override
              public String getType() { return "Truck"; }
            }
            """);

    Compilation compilation = compile(sealedInterface, carClass, truckClass);
    assertThat(compilation).succeeded();

    String generatedCode = loadGeneratedSource(compilation, "CarBuilder");

    ProcessorAsserts.assertContaining(
        generatedCode,
        "public class CarBuilder",
        "public CarBuilder brand(String brand)",
        "public CarBuilder year(int year)",
        "public Car build()");
  }

  @Test
  void classExtendingSealedAbstractClass_builderGenerated() {
    JavaFileObject sealedAbstractClass =
        ProcessorTestUtils.forSource(
            """
            package test;
            public sealed abstract class Animal permits Dog, Cat {
              private final String name;

              protected Animal(String name) {
                this.name = name;
              }

              public String getName() { return name; }
              public abstract String makeSound();
            }
            """);

    JavaFileObject dogClass =
        ProcessorTestUtils.forSource(
            """
            package test;
            @org.javahelpers.simple.builders.core.annotations.SimpleBuilder
            public final class Dog extends Animal {
              private final String breed;

              public Dog(String name, String breed) {
                super(name);
                this.breed = breed;
              }

              public String getBreed() { return breed; }

              @Override
              public String makeSound() { return "Woof"; }
            }
            """);

    JavaFileObject catClass =
        ProcessorTestUtils.forSource(
            """
            package test;
            public final class Cat extends Animal {
              public Cat(String name) {
                super(name);
              }

              @Override
              public String makeSound() { return "Meow"; }
            }
            """);

    Compilation compilation = compile(sealedAbstractClass, dogClass, catClass);
    assertThat(compilation).succeeded();

    String generatedCode = loadGeneratedSource(compilation, "DogBuilder");

    ProcessorAsserts.assertContaining(
        generatedCode,
        "public class DogBuilder",
        "public DogBuilder name(String name)",
        "public DogBuilder breed(String breed)",
        "public Dog build()");
  }

  @Test
  void multipleImplementationsInSealedHierarchy_allBuildersGenerated() {
    JavaFileObject sealedInterface =
        ProcessorTestUtils.forSource(
            """
            package test;
            public sealed interface Shape permits Circle, Rectangle {
              double area();
            }
            """);

    JavaFileObject circleClass =
        ProcessorTestUtils.forSource(
            """
            package test;
            @org.javahelpers.simple.builders.core.annotations.SimpleBuilder
            public final class Circle implements Shape {
              private final double radius;

              public Circle(double radius) {
                this.radius = radius;
              }

              public double getRadius() { return radius; }

              @Override
              public double area() { return Math.PI * radius * radius; }
            }
            """);

    JavaFileObject rectangleClass =
        ProcessorTestUtils.forSource(
            """
            package test;
            @org.javahelpers.simple.builders.core.annotations.SimpleBuilder
            public final class Rectangle implements Shape {
              private final double width;
              private final double height;

              public Rectangle(double width, double height) {
                this.width = width;
                this.height = height;
              }

              public double getWidth() { return width; }
              public double getHeight() { return height; }

              @Override
              public double area() { return width * height; }
            }
            """);

    Compilation compilation = compile(sealedInterface, circleClass, rectangleClass);
    assertThat(compilation).succeeded();

    String circleBuilder = loadGeneratedSource(compilation, "CircleBuilder");
    ProcessorAsserts.assertContaining(
        circleBuilder,
        "public class CircleBuilder",
        "public CircleBuilder radius(double radius)",
        "public Circle build()");

    String rectangleBuilder = loadGeneratedSource(compilation, "RectangleBuilder");
    ProcessorAsserts.assertContaining(
        rectangleBuilder,
        "public class RectangleBuilder",
        "public RectangleBuilder width(double width)",
        "public RectangleBuilder height(double height)",
        "public Rectangle build()");
  }

  @Test
  void sealedInterfaceWithComplexHierarchy_builderGenerated() {
    JavaFileObject sealedInterface =
        ProcessorTestUtils.forSource(
            """
            package test;
            public sealed interface Payment permits CreditCardPayment, CashPayment {
              double getAmount();
            }
            """);

    JavaFileObject creditCardClass =
        ProcessorTestUtils.forSource(
            """
            package test;
            @org.javahelpers.simple.builders.core.annotations.SimpleBuilder
            public final class CreditCardPayment implements Payment {
              private final double amount;
              private final String cardNumber;
              private final String cvv;

              public CreditCardPayment(double amount, String cardNumber, String cvv) {
                this.amount = amount;
                this.cardNumber = cardNumber;
                this.cvv = cvv;
              }

              @Override
              public double getAmount() { return amount; }
              public String getCardNumber() { return cardNumber; }
              public String getCvv() { return cvv; }
            }
            """);

    JavaFileObject cashClass =
        ProcessorTestUtils.forSource(
            """
            package test;
            @org.javahelpers.simple.builders.core.annotations.SimpleBuilder
            public final class CashPayment implements Payment {
              private final double amount;
              private final String currency;

              public CashPayment(double amount, String currency) {
                this.amount = amount;
                this.currency = currency;
              }

              @Override
              public double getAmount() { return amount; }
              public String getCurrency() { return currency; }
            }
            """);

    Compilation compilation = compile(sealedInterface, creditCardClass, cashClass);
    assertThat(compilation).succeeded();

    String creditCardBuilder = loadGeneratedSource(compilation, "CreditCardPaymentBuilder");
    ProcessorAsserts.assertContaining(
        creditCardBuilder,
        "public class CreditCardPaymentBuilder",
        "public CreditCardPaymentBuilder amount(double amount)",
        "public CreditCardPaymentBuilder cardNumber(String cardNumber)",
        "public CreditCardPaymentBuilder cvv(String cvv)",
        "public CreditCardPayment build()");

    String cashBuilder = loadGeneratedSource(compilation, "CashPaymentBuilder");
    ProcessorAsserts.assertContaining(
        cashBuilder,
        "public class CashPaymentBuilder",
        "public CashPaymentBuilder amount(double amount)",
        "public CashPaymentBuilder currency(String currency)",
        "public CashPayment build()");
  }

  @Test
  void nonSealedSubclassOfSealedClass_builderGenerated() {
    JavaFileObject sealedClass =
        ProcessorTestUtils.forSource(
            """
            package test;
            public sealed abstract class BaseEntity permits ConcreteEntity {
              private final long id;

              protected BaseEntity(long id) {
                this.id = id;
              }

              public long getId() { return id; }
            }
            """);

    JavaFileObject concreteClass =
        ProcessorTestUtils.forSource(
            """
            package test;
            @org.javahelpers.simple.builders.core.annotations.SimpleBuilder
            public non-sealed class ConcreteEntity extends BaseEntity {
              private final String data;

              public ConcreteEntity(long id, String data) {
                super(id);
                this.data = data;
              }

              public String getData() { return data; }
            }
            """);

    Compilation compilation = compile(sealedClass, concreteClass);
    assertThat(compilation).succeeded();

    String generatedCode = loadGeneratedSource(compilation, "ConcreteEntityBuilder");

    ProcessorAsserts.assertContaining(
        generatedCode,
        "public class ConcreteEntityBuilder",
        "public ConcreteEntityBuilder id(long id)",
        "public ConcreteEntityBuilder data(String data)",
        "public ConcreteEntity build()");
  }
}
