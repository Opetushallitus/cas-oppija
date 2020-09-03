package fi.vm.sade.cas.oppija.ticket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fi.vm.sade.cas.oppija.surrogate.SurrogateData;
import fi.vm.sade.cas.oppija.surrogate.SurrogateImpersonatorData;
import fi.vm.sade.cas.oppija.surrogate.SurrogateRequestData;
import org.apereo.cas.authentication.principal.SimpleWebApplicationServiceImpl;
import org.apereo.cas.ticket.Ticket;
import org.pac4j.saml.profile.SAML2Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.util.*;

@Component
public class JacksonTicketSerializer implements TicketSerializer {

    private final ObjectMapper objectMapper;

    public JacksonTicketSerializer() {
        PolymorphicTypeValidator ptv =
                BasicPolymorphicTypeValidator.builder()
                        .allowIfBaseType(Ticket.class)
                        .allowIfSubType(HashMap.class)
                        .allowIfSubType(HashSet.class)
                        .allowIfSubType(TreeMap.class)
                        .allowIfSubType(Temporal.class)
                        .allowIfSubType(org.joda.time.LocalDate.class)
                        .allowIfSubType(SurrogateData.class)
                        .allowIfSubType(SAML2Profile.class)
                        .allowIfSubType(SimpleWebApplicationServiceImpl.class)
                        .allowIfSubType(SurrogateRequestData.class)
                        .allowIfSubType(SurrogateImpersonatorData.class)
                        .allowIfSubType(ArrayList.class)
                        .allowIfSubType(List.class)
                        .build();
        //SimpleModule module = new SimpleModule().addSerializer(ZonedDateTime.class, new CustomZonedDateTimeSerializer());;
        SimpleModule module2 = new SimpleModule().addSerializer(SAML2Profile.class, new ItemSerializer());;
        this.objectMapper = JsonMapper.builder() // new style
                .activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL)
                .build()
                .registerModules(new JavaTimeModule(), module2).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .findAndRegisterModules();
                //.registerModules(new JavaTimeModule(), module2).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                //.registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    }

    // protected JacksonTicketSerializer(ObjectMapper objectMapper) {
       // this.objectMapper = objectMapper;
    //}

    @Override
    public String toJson(Ticket ticket) {
        try {
            return objectMapper.writeValueAsString(ticket);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Ticket fromJson(String ticketJson, String ticketGrantingTicketJson) {
        try {
            if (ticketGrantingTicketJson != null) {
                Ticket ticket = objectMapper.readValue(ticketJson, Ticket.class);
                ObjectReader objectReader = objectMapper.readerForUpdating(ticket);
                JsonNode ticketGrantingTicketNode = objectMapper.createObjectNode()
                        .set("ticketGrantingTicket", objectMapper.readTree(ticketGrantingTicketJson));
                return objectReader.readValue(ticketGrantingTicketNode);
            }
            return objectMapper.readValue(ticketJson, Ticket.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }
}
