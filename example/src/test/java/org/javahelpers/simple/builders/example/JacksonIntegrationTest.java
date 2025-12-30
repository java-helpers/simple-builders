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
