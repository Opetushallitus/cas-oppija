package fi.vm.sade.cas.oppija.ticket;

import com.fasterxml.jackson.databind.ObjectMapper;
import fi.vm.sade.cas.oppija.ticket.JacksonTicketSerializer;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.ticket.*;
import org.apereo.cas.ticket.expiration.builder.TransientSessionTicketExpirationPolicyBuilder;
import org.apereo.cas.ticket.factory.DefaultTransientSessionTicketFactory;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class JacksonTicketSerializerTest {

    private JacksonTicketSerializer ticketSerializer;

    @BeforeEach
    public void setup() {
            ticketSerializer = new JacksonTicketSerializer();
    }

    @Test
    public void transientSessionTicket() {
        TransientSessionTicketFactory transientSessionTicketFactory = new DefaultTransientSessionTicketFactory(new ExpirationPolicyBuilder<TransientSessionTicket>(){

            @Override
            public ExpirationPolicy buildTicketExpirationPolicy() {
                return new NeverExpiresExpirationPolicy();
            }

            @Override
            public Class<TransientSessionTicket> getTicketType() {
                return TransientSessionTicket.class;
            }
        });
        Service service = new WebApplicationServiceFactory().createService("service123");
        // notBefore -> {ZonedDateTime@17866} "2020-08-20T07:29:14.039Z"
        LocalDateTime date = LocalDateTime.of(2014, 12, 20, 2, 30);
        ZonedDateTime zDate = ZonedDateTime.of(2020, 8, 20, 7, 29, 14, 39000000,ZoneId.of("Z"));
        Map<String, Serializable> properties = Map.of(
                "stringProperty", "value1",
                "integerProperty", 2,
                "localDateProperty", LocalDate.now(),
                "jodaLocalDateProperty", org.joda.time.LocalDate.now(),
                "localDatetimeProp", date,
                "zonedDate", zDate);
        TransientSessionTicket transientSessionTicket = transientSessionTicketFactory.create(service, properties);
        String transientSessionTicketAsJson = ticketSerializer.toJson(transientSessionTicket);
        System.out.println(transientSessionTicketAsJson);
        Ticket transientSessionTicketFromJson = ticketSerializer.fromJson(transientSessionTicketAsJson, null);
        assertThat(transientSessionTicketFromJson).isInstanceOf(TransientSessionTicket.class).isEqualByComparingTo(transientSessionTicket)
                .returns(null, Ticket::getTicketGrantingTicket);
    }
}
