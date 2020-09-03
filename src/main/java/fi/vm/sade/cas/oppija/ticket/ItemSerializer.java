package fi.vm.sade.cas.oppija.ticket;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.WritableTypeId;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.pac4j.saml.profile.SAML2Profile;

import java.io.IOException;

import static com.fasterxml.jackson.core.JsonToken.START_OBJECT;

public class ItemSerializer extends StdSerializer<SAML2Profile> {

    public ItemSerializer() {
        this(null);
    }

    public ItemSerializer(Class<SAML2Profile> t) {
        super(t);
    }

    @Override
    public void serialize(
            SAML2Profile value, JsonGenerator jgen, SerializerProvider provider)
            throws IOException, JsonProcessingException {

        jgen.writeString(value.toString());
    }

    @Override
    public void serializeWithType(SAML2Profile value, JsonGenerator gen,
                                  SerializerProvider provider, TypeSerializer typeSer)
            throws IOException, JsonProcessingException {

        //serialize(value, gen, provider); // call your customized serialize method
        gen.writeString(value.toString());
    }
}