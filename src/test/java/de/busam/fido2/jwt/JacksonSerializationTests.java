package de.busam.fido2.jwt;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

/**
 * See https://github.com/FasterXML/jackson-future-ideas/issues/46
 * JSONAutoDetect is a workaround to serialize Data Records with Jackson
 *
 * @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
 * But this Annotation doesn't work for deserialization.
 * Therefore the JsonProperty Annotations must be used in records!
 * e.g. {@link de.busam.fido2.model.user.User} record
 * <p>
 * FIXME: Additionally the Issue above describes also Problems with lists...
 **/
public class JacksonSerializationTests {
    private static final Logger LOGGER = LoggerFactory.getLogger(JWTController.class.getName());

    @Test
    public void testSerializeDataRecord() throws JsonProcessingException {
        String expectedResult = """
                {"username":"test","password":"test1"}\
                """;
        String result = new ObjectMapper()
                .writeValueAsString(new Credentials("test", "test1"));
        assertEquals(expectedResult, result);
    }

    @Test
    public void testDeserializeDataRecord() throws JsonProcessingException {
        String input = """
                {"username":"test","password":"test1"}
                """;
        Credentials c = new ObjectMapper().readValue(input, Credentials.class);
        assertEquals("test", c.username());
        assertEquals("test1", c.password());
    }
}
