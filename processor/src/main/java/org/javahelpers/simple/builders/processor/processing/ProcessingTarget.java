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

package org.javahelpers.simple.builders.processor.processing;

import org.javahelpers.simple.builders.processor.model.core.BuilderConfiguration;

/**
 * Per-target processing state for the type whose builder is currently being generated.
 *
 * <p>{@link org.javahelpers.simple.builders.processor.BuilderProcessor} sets one instance per
 * processed type on the {@link ProcessingContext} before extraction starts, so downstream analysis
 * can reach it without threading both values through every call.
 *
 * @param configuration the resolved builder configuration for the current target
 * @param builderPackage the package the generated builder is written to. For {@code @SimpleBuilder}
 *     targets this is the processed type's own package; for {@code @SimpleBuilderFor} targets it is
 *     the holder's package. The latter cannot be derived from the processed element itself, which
 *     is why it is carried explicitly here
 */
public record ProcessingTarget(BuilderConfiguration configuration, String builderPackage) {}
