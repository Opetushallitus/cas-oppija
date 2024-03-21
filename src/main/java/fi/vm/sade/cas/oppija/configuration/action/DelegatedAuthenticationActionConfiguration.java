package fi.vm.sade.cas.oppija.configuration.action;

import fi.vm.sade.cas.oppija.CasOppijaConstants;
import fi.vm.sade.cas.oppija.CasOppijaUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.pac4j.client.DelegatedClientAuthenticationFailureEvaluator;
import org.apereo.cas.util.LoggingUtils;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationConfigurationContext;
import org.apereo.cas.web.flow.DelegatedClientAuthenticationWebflowManager;
import org.apereo.cas.web.flow.actions.DelegatedClientAuthenticationAction;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.jee.context.JEEContext;
import org.pac4j.saml.exceptions.SAMLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

@Configuration
public class DelegatedAuthenticationActionConfiguration {
    // override default delegatedAuthenticationAction to automatically logout on error
    @Bean
    public Action delegatedAuthenticationAction(
            final DelegatedClientAuthenticationConfigurationContext context,
            final DelegatedClientAuthenticationWebflowManager delegatedClientAuthenticationWebflowManager,
            final DelegatedClientAuthenticationFailureEvaluator failureEvaluator
    ) {
        return new DelegatedClientAuthenticationAction(context, delegatedClientAuthenticationWebflowManager, failureEvaluator) {
            private static final Logger LOGGER = LoggerFactory.getLogger(DelegatedAuthenticationActionConfiguration.class);
            @Override
            public Event doExecuteInternal(RequestContext context) {
                LOGGER.info("Executing DelegatedClientAuthenticationAction");
                var event = super.doExecuteInternal(context);
                LOGGER.info("Event: {}", event);
                return event;
            }
        };
    }
}
