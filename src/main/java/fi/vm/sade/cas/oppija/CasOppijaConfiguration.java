package fi.vm.sade.cas.oppija;

import io.micrometer.observation.ObservationRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.ticket.serialization.TicketSerializationExecutionPlanConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@ComponentScan
public class CasOppijaConfiguration {
    @Bean
    public TicketSerializationExecutionPlanConfigurer ticketSerializationExecutionPlanConfigurer() {
        LOGGER.info("Initializing ticketSerializationExecutionPlanConfigurer");
        return plan -> {
            plan.registerTicketSerializer(new CasOppijaTransientSessionTicketSerializer());
        };
    }

    @Bean
    public ObservationRegistry observationRegistry() {
        LOGGER.info("Initializing observationRegistry");
        // Disable all observations
        return ObservationRegistry.NOOP;
    }
}
