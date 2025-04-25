package api.server.enums.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import api.server.enums.GenericEnum;
import java.io.IOException;

public class GenericEnumJsonSerializer extends JsonSerializer<GenericEnum> {
    public GenericEnumJsonSerializer() {
    }

    public void serialize(GenericEnum value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        gen.writeStartObject();
        gen.writeFieldName("key");
        gen.writeObject(value.name());
        gen.writeFieldName("value");
        gen.writeObject(value.getValue());
        gen.writeFieldName("description");
        gen.writeObject(value.getDescription());
        gen.writeEndObject();
    }
}