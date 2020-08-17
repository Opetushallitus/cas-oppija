package fi.vm.sade.cas.oppija.configuration.action;

import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.exception.http.RedirectionAction;
import org.pac4j.core.exception.TechnicalException;
import org.pac4j.saml.client.SAML2Client;
import org.pac4j.saml.profile.SAML2Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.util.Optional;

import static fi.vm.sade.cas.oppija.CasOppijaConstants.REQUEST_SCOPE_ATTRIBUTE_SAML_LOGOUT;

/**
 * SAML logout action. Expects SAML profile to be in request scope ({@link SamlLogoutPrepareAction}).
 * Implementation is mostly derived from DelegatedAuthenticationSAML2ClientLogoutAction.
 */
public class SamlLogoutExecuteAction extends AbstractAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(SamlLogoutExecuteAction.class);

    private final Clients clients;

    public SamlLogoutExecuteAction(Clients clients) {
        this.clients = clients;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        try {
            var request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            var response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
            //var context = WebUtils.getPac4jJ2EContext(request, response);
            var context = new JEEContext(request, response); // TODO Oikein??


            Client<?> client;
            SAML2Profile profile = requestContext.getRequestScope().get(REQUEST_SCOPE_ATTRIBUTE_SAML_LOGOUT, SAML2Profile.class);
            try {
                var currentClientName = profile == null ? null : profile.getClientName();
                client = currentClientName == null ? null : clients.findClient(currentClientName).orElse(null);
            } catch (final TechnicalException e) {
                LOGGER.debug("No SAML2 client found: " + e.getMessage(), e);
                client = null;
            }

            if (client instanceof SAML2Client) {
                var saml2Client = (SAML2Client) client;
                LOGGER.debug("Located SAML2 client [{}]", saml2Client);
                final Optional<RedirectionAction> action = saml2Client.getLogoutAction(context, profile, null);

                if(action.isPresent()) {
                    // LOGGER.debug("Preparing logout message to send is [{}]", action.get().getLocation());
                    return handleLogout(action.get(), requestContext);
                }

            } else {
                LOGGER.debug("The current client is not a SAML2 client or it cannot be found at all, no logout action will be executed.");
            }
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return null;
    }
    // TODO fix tshis
    protected Event handleLogout(RedirectionAction action, RequestContext context) {
        switch (action.getCode()) {
            case 302:
                WebUtils.putLogoutRedirectUrl(context, null);
                return null;
            default:
                throw new IllegalArgumentException("Unhandled logout request code: " + action.getCode());
        }
    }

}
