/*
 * MIT License
 *
 * Copyright (c) 2026 Andreas Igel
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

package org.javahelpers.simple.builders.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.javahelpers.simple.builders.example.external.ExternalAddress;
import org.junit.jupiter.api.Test;

/**
 * Demonstrates using a builder generated via {@code @SimpleBuilderFor} for a type that cannot be
 * annotated directly.
 */
class ExternalAddressBuilderTest {

  @Test
  void buildsExternalType() {
    ExternalAddress address =
        ExternalAddressBuilder.create()
            .street("Main Street 1")
            .city("Springfield")
            .zipCode("12345")
            .build();

    assertEquals("Main Street 1", address.getStreet());
    assertEquals("Springfield", address.getCity());
    assertEquals("12345", address.getZipCode());
  }

  @Test
  void initializesBuilderFromInstance() {
    ExternalAddress original = new ExternalAddress();
    original.setStreet("Main Street 1");
    original.setCity("Springfield");
    original.setZipCode("12345");

    ExternalAddress copy = new ExternalAddressBuilder(original).city("Shelbyville").build();

    assertEquals("Main Street 1", copy.getStreet());
    assertEquals("Shelbyville", copy.getCity());
    assertEquals("12345", copy.getZipCode());
  }
}
