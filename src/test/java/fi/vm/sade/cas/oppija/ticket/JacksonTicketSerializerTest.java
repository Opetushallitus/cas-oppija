package fi.vm.sade.cas.oppija.ticket;

import fi.vm.sade.cas.oppija.ticket.JacksonTicketSerializer;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.principal.WebApplicationServiceFactory;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.Ticket;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.TransientSessionTicketFactory;
import org.apereo.cas.ticket.expiration.builder.TransientSessionTicketExpirationPolicyBuilder;
import org.apereo.cas.ticket.factory.DefaultTransientSessionTicketFactory;
import org.apereo.cas.ticket.expiration.NeverExpiresExpirationPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


import java.io.Serializable;
import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class JacksonTicketSerializerTest {

    private TicketSerializer ticketSerializer;

    @BeforeEach
    public void setup() {
        ticketSerializer = new JacksonTicketSerializer();
    }
    /* TODO
    @Test
    public void transientSessionTicket() {
        TransientSessionTicketExpirationPolicyBuilder.buildTicketExpirationPolicy();
        TransientSessionTicketFactory transientSessionTicketFactory = new DefaultTransientSessionTicketFactory();
        Service service = new WebApplicationServiceFactory().createService("service123");
        Map<String, Serializable> properties = Map.of(
                "stringProperty", "value1",
                "integerProperty", 2,
                "localDateProperty", LocalDate.now(),
                "jodaLocalDateProperty", org.joda.time.LocalDate.now());
        TransientSessionTicket transientSessionTicket = transientSessionTicketFactory.create(service, properties);

        String transientSessionTicketAsJson = ticketSerializer.toJson(transientSessionTicket);
        System.out.println(transientSessionTicketAsJson);
        Ticket transientSessionTicketFromJson = ticketSerializer.fromJson(transientSessionTicketAsJson, null);
        assertThat(transientSessionTicketFromJson).isInstanceOf(TransientSessionTicket.class).isEqualByComparingTo(transientSessionTicket)
                .returns(null, Ticket::getTicketGrantingTicket);
    }

     */

}
