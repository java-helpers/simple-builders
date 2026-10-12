/*
 * MIT License
 *
 * Copyright (c) 2025-2026 Andreas Igel
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JacksonIntegrationTest {

    @Test
    void shouldFailWithoutJacksonModule() {
        // Given
        ObjectMapper mapper = new ObjectMapper();
        // NOTE: NOT registering the generated module

        String json = "{\"name\":\"Alice\",\"age\":30}";

        // When & Then
        InvalidDefinitionException exception = assertThrows(
            InvalidDefinitionException.class,
            () -> mapper.readValue(json, JacksonIntegrationDto.class)
        );
        
        // Should fail because Jackson can't access protected constructor
        assertNotNull(exception.getMessage());
        assertTrue(exception.getMessage().contains("no Creators, like default constructor, exist"));
    }

    @Test
    void shouldSucceedWithJacksonModule() throws Exception {
        // Given
        ObjectMapper mapper = new ObjectMapper();
        // Register the generated module
        mapper.registerModule(new SimpleBuildersJacksonModule());

        String json = "{\"name\":\"Alice\",\"age\":30}";

        // When
        JacksonIntegrationDto dto = mapper.readValue(json, JacksonIntegrationDto.class);

        // Then
        assertNotNull(dto);
        assertEquals("Alice", dto.name());
        assertEquals(30, dto.age());
    }
}
