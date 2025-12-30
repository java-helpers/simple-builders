package org.javahelpers.simple.builders.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JacksonIntegrationTest {

    @Test
    void shouldDeserializeUsingGeneratedModule() throws Exception {
        // Given
        ObjectMapper mapper = new ObjectMapper();
        // Register the generated module
        mapper.registerModule(new SimpleBuildersJacksonModule());

        String json = "{\"name\":\"Alice\",\"age\":30}";

        // When
        JacksonIntegrationDto dto = mapper.readValue(json, JacksonIntegrationDto.class);

        // Then
        assertNotNull(dto);
        assertEquals("Alice", dto.getName());
        assertEquals(30, dto.getAge());
    }
}
