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

package org.javahelpers.simple.builders.internal.dtos;

/** Types of methods. Each type matches a specific way body of generated methods. */
public enum MethodTypes {
  /** Simple Proxy. Just calling the method on the inner instance. */
  PROXY,
  /**
   * Supplier-Pattern. Following supplier pattern and calling the setter-function on inner instance
   * by supplier-result.
   */
  SUPPLIER,
  /**
   * Consumer-Pattern. Creating an instance of parameter and providing a consumer for the caller.
   * The provided instance is afterwards applied to builder target.
   */
  CONSUMER,
  /**
   * Consumer-Pattern with builder. Creating an instance of a builder and providing that to the
   * caller. The build-function of that builder is executed afterwards and applied to builder
   * target.
   */
  CONSUMER_BY_BUILDER;
}
