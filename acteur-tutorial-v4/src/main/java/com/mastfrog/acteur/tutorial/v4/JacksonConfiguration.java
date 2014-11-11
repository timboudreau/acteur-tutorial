package com.mastfrog.acteur.tutorial.v4;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.mastfrog.jackson.JacksonConfigurer;
import java.io.IOException;
import org.bson.types.ObjectId;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tim Boudreau
 */
@ServiceProvider(service = JacksonConfigurer.class)
public class JacksonConfiguration implements JacksonConfigurer {

    @Override
    public ObjectMapper configure(ObjectMapper om) {
        om.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);
        SimpleModule sm = new SimpleModule("mongo", new Version(1, 0, 0, null,
                "com.mastfrog", "acteur-tutorial"));
        sm.addSerializer(new ObjectMapperSerializer());
        om.registerModule(sm);
        return om;
    }

    private static class ObjectMapperSerializer extends JsonSerializer<ObjectId> {

        @Override
        public Class<ObjectId> handledType() {
            return ObjectId.class;
        }

        @Override
        public void serialize(ObjectId t, JsonGenerator jg, SerializerProvider sp)
                throws IOException, JsonProcessingException {
            String id = t.toStringMongod();
            jg.writeString(id);
        }
    }
}
