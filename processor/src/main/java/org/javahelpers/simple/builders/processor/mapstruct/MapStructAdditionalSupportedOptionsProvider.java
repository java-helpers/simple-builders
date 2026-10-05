/*
 * MIT License
 *
 * Copyright (c) 2026 Andreas Igel
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons with the following conditions:
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

package org.javahelpers.simple.builders.processor.mapstruct;

import com.google.auto.service.AutoService;
import java.util.Set;
import org.javahelpers.simple.builders.processor.processing.CompilerArgumentsEnum;
import org.mapstruct.ap.spi.AdditionalSupportedOptionsProvider;

/**
 * Declares the simple-builders options the MapStruct SPIs read. MapStruct filters the options map
 * it hands to SPI environments down to the names {@link AdditionalSupportedOptionsProvider}s
 * declare — without this provider {@code -Asimplebuilder.usingMapStructIntegration} would never
 * reach them.
 */
@AutoService(AdditionalSupportedOptionsProvider.class)
public class MapStructAdditionalSupportedOptionsProvider
    implements AdditionalSupportedOptionsProvider {

  @Override
  public Set<String> getAdditionalSupportedOptions() {
    return Set.of(
        CompilerArgumentsEnum.USING_MAPSTRUCT_INTEGRATION.getCompilerArgument(),
        CompilerArgumentsEnum.USING_MAPSTRUCT_INTEGRATION.getOptionName());
  }
}
