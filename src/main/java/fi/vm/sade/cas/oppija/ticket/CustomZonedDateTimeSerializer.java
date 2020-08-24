package fi.vm.sade.cas.oppija.ticket;

        import com.fasterxml.jackson.core.JsonGenerator;
        import com.fasterxml.jackson.core.JsonProcessingException;
        import com.fasterxml.jackson.databind.SerializerProvider;
        import com.fasterxml.jackson.databind.ser.std.StdSerializer;
        import org.joda.time.DateTime;
        import org.joda.time.DateTimeZone;

        import java.io.IOException;
        import java.time.Instant;
        import java.time.ZoneOffset;
        import java.time.ZonedDateTime;
        import java.util.Date;
        import java.util.TimeZone;


public final class CustomZonedDateTimeSerializer extends StdSerializer<ZonedDateTime> {
    public CustomZonedDateTimeSerializer() {
        this(null);
    }

    public CustomZonedDateTimeSerializer(Class<ZonedDateTime> t) {
        super(t);
    }
    @Override
    public void serialize(ZonedDateTime zonedDateTime,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
        DateTime d = new DateTime(
                zonedDateTime.toInstant().toEpochMilli(),
                DateTimeZone.forTimeZone(TimeZone.getTimeZone(zonedDateTime.getZone())));
        jsonGenerator.writeString(d.toString());
    }
};