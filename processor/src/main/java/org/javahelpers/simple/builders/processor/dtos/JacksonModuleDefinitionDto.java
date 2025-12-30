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

package org.javahelpers.simple.builders.processor.dtos;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Definition for generating a Jackson SimpleModule. Contains the target package and the entries to
 * be registered in the module.
 */
public class JacksonModuleDefinitionDto {
  private final String targetPackage;
  private final Set<JacksonModuleEntryDto> entries = new HashSet<>();

  public JacksonModuleDefinitionDto(String targetPackage) {
    this.targetPackage = targetPackage;
  }

  public String getTargetPackage() {
    return targetPackage;
  }

  public Set<JacksonModuleEntryDto> getEntries() {
    return entries;
  }

  public void addEntry(JacksonModuleEntryDto entry) {
    this.entries.add(entry);
  }

  public void addAllEntries(Collection<JacksonModuleEntryDto> entries) {
    this.entries.addAll(entries);
  }
}
